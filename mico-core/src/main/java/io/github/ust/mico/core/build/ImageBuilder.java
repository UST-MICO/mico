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
import io.github.ust.mico.core.NotInitializedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

// TODO Move package
@Slf4j
@Component
public class ImageBuilder {

    private static final String BUILD_STEP_NAME = "build-and-push";
    private static final String BUILD_CRD_GROUP = "build.knative.dev";
    private static final String BUILD_CRD_NAME = "builds." + BUILD_CRD_GROUP;

    private final ImageBuilderConfig config;
    private final ClusterAwarenessFabric8 cluster;

    private NonNamespaceOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>> buildClient;

    @Autowired
    public ImageBuilder(ClusterAwarenessFabric8 cluster, ImageBuilderConfig config) {
        this.cluster = cluster;
        this.config = config;
    }

    public void init() throws NotInitializedException {
        Optional<CustomResourceDefinition> buildCRD = getBuildCRD();
        if (!buildCRD.isPresent()) {
            log.error("Custom Resource Definition `{}` is not available!", BUILD_CRD_NAME);

            throw new NotInitializedException("Build resource not available!");
        }

        this.buildClient = cluster.getClient().customResources(buildCRD.get(),
                Build.class, BuildList.class, DoneableBuild.class);

        String resourceScope = buildCRD.get().getSpec().getScope();
        log.debug("Build CRD has scope `{}`", resourceScope);

        // Resource scope is either 'Namespaced' or 'Cluster'
        boolean resourceNamespaced = false;
        if (resourceScope.equals("Namespaced")) {
            resourceNamespaced = true;
        }
        if (resourceNamespaced) {
            buildClient = ((MixedOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>>)
                    buildClient).inNamespace(config.getBuildExecutionNamespace());
        }

        // TODO Check if required service account is available
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

    public Build getBuild(String buildName) {
        return this.buildClient.withName(buildName).get();
    }

    public List<CustomResourceDefinition> getCustomResourceDefinitions() {
        KubernetesClient client = cluster.getClient();
        CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        return crdsItems;
    }

    /**
     * @param serviceName    the name of the MICO service
     * @param serviceVersion the version of the MICO service
     * @param dockerfile     the relative path to the dockerfile
     * @param gitUrl         the URL to the remote git repository
     * @param gitRevision    the
     * @return
     * @throws NotInitializedException
     */
    public Build build(String serviceName, String serviceVersion, String dockerfile, String gitUrl, String gitRevision) throws NotInitializedException {

        // TODO Add JavaDoc

        // TODO Is normalization required?
        String serviceNameNormalized = serviceName.replaceAll("/[^A-Za-z0-9]/", "-");
        String buildName = createBuildName(serviceNameNormalized);
        String destination = createImageName(serviceNameNormalized, serviceVersion);

        String dockerfileNormalized = dockerfile.startsWith("/") ? dockerfile.substring(1) : dockerfile;
        String dockerfilePath = "/workspace/" + dockerfileNormalized;

        String namespace = config.getBuildExecutionNamespace();

        return createBuild(buildName, destination, dockerfilePath, gitUrl, gitRevision, namespace);
    }

    private Build createBuild(String buildName, String destination, String dockerfile, String gitUrl, String gitRevision, String namespace) throws NotInitializedException {

        if (buildClient == null) {
            // TODO Change to MICO specific NotInitializedException
            throw new NotInitializedException("ImageBuilder is not initialized.");
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
                                .name(BUILD_STEP_NAME)
                                .image(config.getKanikoExecutorImageUrl())
                                .arg("--dockerfile=" + dockerfile)
                                .arg("--destination=" + destination)
                                .build())
                        .build())
                .build();

        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(buildName);
        metadata.setNamespace(namespace);
        build.setMetadata(metadata);

        Build createdBuild = buildClient.createOrReplace(build);
        System.out.println("Upserted " + createdBuild);

        // TODO Check if watcher is required
        buildClient.withResourceVersion(createdBuild.getMetadata().getResourceVersion()).watch(new Watcher<Build>() {
            @Override
            public void eventReceived(Action action, Build resource) {
                System.out.println("==> " + action + " for " + resource);
                if (resource.getSpec() == null) {
                    log.error("No Spec for resource " + resource);
                }
            }

            @Override
            public void onClose(KubernetesClientException cause) {
                log.error("Build failed with cause: " + cause);
            }
        });

        return createdBuild;
    }

    public void deleteBuild(String buildName) {
        buildClient.withName(buildName).delete();
    }

    public void deleteBuild(Build build) {
        buildClient.delete(build);
    }

    public String createImageName(String serviceNameNormalized, String serviceVersion) {
        return config.getImageRepositoryUrl() + "/" + serviceNameNormalized + ":" + serviceVersion;
    }

    private String createBuildName(String serviceNameNormalized) {
        return "build-" + serviceNameNormalized;
    }
}
