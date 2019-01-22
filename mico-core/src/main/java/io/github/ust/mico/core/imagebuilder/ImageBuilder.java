package io.github.ust.mico.core.imagebuilder;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.github.ust.mico.core.ClusterAwarenessFabric8;
import io.github.ust.mico.core.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.MicoKubernetesConfig;
import io.github.ust.mico.core.NotInitializedException;
import io.github.ust.mico.core.imagebuilder.buildtypes.*;
import io.github.ust.mico.core.model.MicoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Builds container images by using Knative Build and Kaniko
 */
@Slf4j
@Service
public class ImageBuilder {

    private static final String BUILD_STEP_NAME = "build-and-push";
    private static final String BUILD_CRD_GROUP = "build.knative.dev";
    private static final String BUILD_CRD_NAME = "builds." + BUILD_CRD_GROUP;

    private final MicoKubernetesBuildBotConfig buildBotConfig;
    private final MicoKubernetesConfig micoKubernetesConfig;
    private final ClusterAwarenessFabric8 cluster;

    private NonNamespaceOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>> buildClient;
    private ScheduledExecutorService scheduledBuildStatusCheckService;


    /**
     * @param cluster        The Kubernetes cluster object
     * @param buildBotConfig The build bot configuration for the image builder
     * @param micoKubernetesConfig The Kubernetes configuration
     */
    @Autowired
    public ImageBuilder(ClusterAwarenessFabric8 cluster, MicoKubernetesBuildBotConfig buildBotConfig, MicoKubernetesConfig micoKubernetesConfig) {
        this.cluster = cluster;
        this.buildBotConfig = buildBotConfig;
        this.micoKubernetesConfig = micoKubernetesConfig;
    }

    /**
     * Initialize the image builder.
     *
     * @throws NotInitializedException if the image builder was not initialized
     */
    public void init() throws NotInitializedException {
        String namespace = buildBotConfig.getNamespaceBuildExecution();
        String serviceAccountName = buildBotConfig.getServiceAccountName();

        Optional<CustomResourceDefinition> buildCRD = getBuildCRD();
        if (!buildCRD.isPresent()) {
            log.error("Custom Resource Definition `{}` is not available!", BUILD_CRD_NAME);
            throw new NotInitializedException("Build CRD not available!");
        }
        Optional<ServiceAccount> buildServiceAccount = Optional.of(cluster.getServiceAccount(serviceAccountName, namespace));
        if (!buildServiceAccount.isPresent()) {
            log.error("Service account `{}` is not available!", serviceAccountName);
            throw new NotInitializedException("Service account not available!");
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
                buildClient).inNamespace(namespace);
        }

        scheduledBuildStatusCheckService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Returns the build CRD if exists
     *
     * @return the build CRD
     */
    public Optional<CustomResourceDefinition> getBuildCRD() {
        List<CustomResourceDefinition> crdsItems = getCustomResourceDefinitions();

        for (CustomResourceDefinition crd : crdsItems) {
            ObjectMeta metadata = crd.getMetadata();
            if (metadata != null) {
                String name = metadata.getName();
                if (BUILD_CRD_NAME.equals(name)) {
                    log.debug("Found build CRD => {}", metadata.getSelfLink());
                    return Optional.of(crd);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the build object
     *
     * @param buildName the name of the build
     * @return the build object
     */
    public Build getBuild(String buildName) {

        return this.buildClient.withName(buildName).get();
    }


    /**
     * @param micoService the MICO service for which the image should be build
     * @return the resulting build
     * @throws NotInitializedException if the image builder was not initialized
     */
    public Build build(MicoService micoService) throws NotInitializedException, IllegalArgumentException {

        if (StringUtils.isEmpty(micoService.getVcsRoot())) {
            throw new IllegalArgumentException("VcsRoot is missing");
        }

        String namespace = buildBotConfig.getNamespaceBuildExecution();

        String buildName = createBuildName(micoService.getShortName());
        String destination = createImageName(micoService.getShortName(), micoService.getVersion());
        String dockerfilePath;
        if (!StringUtils.isEmpty(micoService.getDockerfilePath())) {
            dockerfilePath = "/workspace/" + micoService.getDockerfilePath();
        } else {
            log.debug("MicoService {} does not have a Dockerfile path set. Assume it is placed in the root directory.", micoService.getShortName());
            dockerfilePath = "/workspace/Dockerfile";
        }
        String gitUrl = micoService.getVcsRoot();
        String gitRevision = micoService.getVersion();

        return createBuild(buildName, destination, dockerfilePath, gitUrl, gitRevision, namespace);
    }

    /**
     * @param buildName   the name of the build
     * @param destination the url of the image destination
     * @param dockerfile  the relative path to the dockerfile
     * @param gitUrl      the URL to the remote git repository
     * @param gitRevision the revision of the git repository. e.g. `master`, commit id or a tag
     * @param namespace   the namespace in which the build is executed
     * @return the resulting build
     * @throws NotInitializedException if the image builder was not initialized
     */
    private Build createBuild(String buildName, String destination, String dockerfile, String gitUrl, String gitRevision, String namespace) throws NotInitializedException {

        if (buildClient == null) {
            throw new NotInitializedException("ImageBuilder is not initialized.");
        }

        Build build = Build.builder()
            .spec(BuildSpec.builder()
                .serviceAccountName(buildBotConfig.getServiceAccountName())
                .source(SourceSpec.builder()
                    .git(GitSourceSpec.builder()
                        .url(gitUrl)
                        .revision(gitRevision)
                        .build())
                    .build())
                .step(BuildStep.builder()
                    .name(BUILD_STEP_NAME)
                    .image(buildBotConfig.getKanikoExecutorImageUrl())
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
        log.info("Build created");
        log.debug("Created build: {} ", createdBuild);

        return createdBuild;
    }

    public CompletableFuture<Boolean> waitUntilBuildIsFinished(Build build) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> completionFuture = new CompletableFuture<>();

        // Create a future that polls every second with a delay of 10 seconds.
        final ScheduledFuture<?> checkFuture = scheduledBuildStatusCheckService.scheduleAtFixedRate(() -> {

            if (build.getStatus() != null && build.getStatus().getCluster() != null) {
                String buildPodName = build.getStatus().getCluster().getPodName();
                String buildNamespace = build.getStatus().getCluster().getNamespace();
                log.debug("Build is executed with pod '{}' in namespace '{}'", buildPodName, buildNamespace);

                Pod buildPod = this.cluster.getPod(buildPodName, buildBotConfig.getNamespaceBuildExecution());

                log.debug("Current build phase: {}", buildPod.getStatus().getPhase());
                if (buildPod.getStatus().getPhase().equals("Succeeded")) {
                    completionFuture.complete(true);
                    // TODO Clean up build (delete build pod)
                } else if (buildPod.getStatus().getPhase().equals("Failed")) {
                    completionFuture.complete(false);
                }
            } else {
                log.error("Build was not started!");
                completionFuture.complete(false);
            }
        }, 10, 1, TimeUnit.SECONDS);

        // TODO Add a proper timeout
        completionFuture.get(60, TimeUnit.SECONDS);

        // When completed cancel future
        completionFuture.whenComplete((result, thrown) -> checkFuture.cancel(true));

        return completionFuture;
    }

    /**
     * Returns a list of custom resource definitions
     *
     * @return the list of custom resource definitions
     */
    private List<CustomResourceDefinition> getCustomResourceDefinitions() {
        KubernetesClient client = cluster.getClient();
        CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();

        log.debug("CRDs: {}", crdsItems);
        return crdsItems;
    }

    /**
     * Creates a image name based on the service name and the service version (used as image tag).
     *
     * @param serviceName    the name of the MICO service
     * @param serviceVersion the version of the MICO service
     * @return the image name
     */
    public String createImageName(String serviceName, String serviceVersion) {
        return micoKubernetesConfig.getImageRepositoryUrl() + "/" + serviceName + ":" + serviceVersion;
    }

    /**
     * Creates a build name based on the service name.
     *
     * @param serviceName the name of the MICO service
     * @return the build name
     */
    private String createBuildName(String serviceName) {
        return "build-" + serviceName;
    }

    /**
     * Delete the build
     *
     * @param buildName the name of the build
     */
    public void deleteBuild(String buildName) {
        buildClient.withName(buildName).delete();
    }

    /**
     * Delete the build
     *
     * @param build the build object
     */
    public void deleteBuild(Build build) {
        buildClient.delete(build);
    }
}
