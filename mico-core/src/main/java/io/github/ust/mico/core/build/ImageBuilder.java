package io.github.ust.mico.core.build;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.github.ust.mico.core.ClusterAwarenessFabric8;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class ImageBuilder {

    private final String BUILD_CRD_GROUP = "build.knative.dev";
    private final String BUILD_CRD_NAME = "builds." + BUILD_CRD_GROUP;
    private final String BUILD_CRD_VERSION = "v1alpha1";
    // private final String BUILDTEMPLATE_CRD_NAME = "buildtemplates." + BUILD_CRD_GROUP;

    private final ImageBuilderConfig config;

    private final ClusterAwarenessFabric8 cluster;

    private NonNamespaceOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>> buildClient;

    @Autowired
    public ImageBuilder(ClusterAwarenessFabric8 cluster, ImageBuilderConfig config) {
        this.cluster = cluster;
        this.config = config;
    }

    @PostConstruct
    public void init() throws Exception {
        Optional<CustomResourceDefinition> buildCRD = getBuildCRD();
        if (!buildCRD.isPresent()) {
            log.error("Custom Resource Definition `{}` is not available!", BUILD_CRD_NAME);
            // TODO Change to MICO specific Exception
            throw new Exception("Build resource not available!");
        }

        this.buildClient = cluster.getClient().customResources(buildCRD.get(),
                Build.class, BuildList.class, DoneableBuild.class);

        String resourceScope = buildCRD.get().getSpec().getScope();
        log.debug("Build CRD has scope `{}`", resourceScope);
        boolean resourceNamespaced = false;
        if (resourceScope.equals("Namespaced")) {
            resourceNamespaced = true;
        }
        if (resourceNamespaced) {
            buildClient = ((MixedOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>>)
                    buildClient).inNamespace(config.getBuildExecutionNamespace());
        }
    }

    public Optional<CustomResourceDefinition> getBuildCRD() {
        List<CustomResourceDefinition> crdsItems = getCustomResourceDefinitions();

        for (CustomResourceDefinition crd : crdsItems) {
            ObjectMeta metadata = crd.getMetadata();
            if (metadata != null) {
                String name = metadata.getName();
                System.out.println("    " + name + " => " + metadata.getSelfLink());
                if (BUILD_CRD_NAME.equals(name)) {
                    return Optional.of(crd);
                }
            }
        }
        return Optional.empty();
    }

    public List<CustomResourceDefinition> getCustomResourceDefinitions() {
        KubernetesClient client = cluster.getClient();
        CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        return crdsItems;
    }

//    public void createBuildCRD() {
//        boolean resourceNamespaced = false;
//        CustomResourceDefinition buildCRD = new CustomResourceDefinitionBuilder().
//                withApiVersion("apiextensions.k8s.io/v1beta1").
//                withNewMetadata().withName(BUILD_CRD_NAME).endMetadata().
//                withNewSpec().withGroup(BUILD_CRD_GROUP).withVersion("v1").withScope(resourceScope(resourceNamespaced)).
//                withNewNames().withKind("Dummy").withShortNames("dummy").withPlural("dummies").endNames().endSpec().
//                build();
//
//        KubernetesClient client = cluster.getClient();
//        client.customResourceDefinitions().create(buildCRD);
//        System.out.println("Created CRD " + buildCRD.getMetadata().getName());
//    }

    @Deprecated
    public void createBuildWithYaml(InputStream yaml, String namespace) {

        System.out.println("Namespace: " + namespace);
        cluster.createFromYaml(yaml, namespace);
    }

    public void build(String serviceName, String serviceVersion, String dockerfile, String gitUrl, String gitRevision) throws Exception {

        // TODO Is normalization required?
        String serviceNameNormalized = serviceName.replaceAll("/[^A-Za-z0-9]/", "-");
        String buildName = createBuildName(serviceNameNormalized);
        String destination = createImageName(serviceNameNormalized, serviceVersion);

        String dockerfileNormalized = dockerfile.startsWith("/") ? dockerfile.substring(1) : dockerfile;
        String dockerfilePath = "/workspace/" + dockerfileNormalized;

        createBuild(buildName, destination, dockerfilePath, gitUrl, gitRevision);
    }

    private void createBuild(String buildName, String destination, String dockerfile, String gitUrl, String gitRevision) throws Exception {

        if (buildClient == null) {
            // TODO Change to MICO specific Exception
            throw new Exception("ImageBuilder is not initialized.");
        }

        Build build = Build.builder()
                .spec(BuildSpec.builder()
                        .serviceAccountName(config.getServiceAccountName())
                        .source(Source.builder()
                                .git(GitSourceSpec.builder()
                                        .url(gitUrl)
                                        .revision(gitRevision)
                                        .build())
                                .build())
                        .step(BuildStep.builder()
                                .name(config.getBuildStepName())
                                .image(config.getKanikoExecutorImageUrl())
                                .arg("--dockerfile=" + dockerfile)
                                .arg("--destination=" + destination)
                                .build())
                        .build())
                .build();

        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(buildName);
        build.setMetadata(metadata);

        log.info("Build: " + build.toString());

        ObjectMapper mapper = new YAMLMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, build);
        log.info("Build:" + sw.toString());

        Build created = buildClient.createOrReplace(build);
        System.out.println("Upserted " + created);

        buildClient.withResourceVersion(created.getMetadata().getResourceVersion()).watch(new Watcher<Build>() {
            @Override
            public void eventReceived(Action action, Build resource) {
                System.out.println("==> " + action + " for " + resource);
                if (resource.getSpec() == null) {
                    log.error("No Spec for resource " + resource);
                }
            }

            @Override
            public void onClose(KubernetesClientException cause) {
            }
        });
    }

    private String createBuildName(String serviceNameNormalized) {
        return "build-" + serviceNameNormalized;
    }

    private String createImageName(String serviceNameNormalized, String serviceVersion) {
        return config.getImageRepositoryUrl() + "/" + serviceNameNormalized + ":" + serviceVersion;
    }

    private static String resourceScope(boolean resourceNamespaced) {
        if (resourceNamespaced) {
            return "Namespaced";
        }
        return "Cluster";
    }
}
