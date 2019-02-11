package io.github.ust.mico.core.mapping;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.github.ust.mico.core.ClusterAwarenessFabric8;
import io.github.ust.mico.core.MicoKubernetesConfig;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.UIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides accessor methods for creating deployment and services in Kubernetes.
 */
@Slf4j
@Component
public class MicoKubernetesClient {


    /**
     * Labels are used as selectors for Kubernetes deployments, services and pods.
     * The `app` label references to the shortName of the {@link MicoService}.
     * The `version` label references to the version of the {@link MicoService}.
     * The `interface` label references to the name of the {@link MicoServiceInterface}.
     * The `run` label references to the UID that is created for each {@link MicoService}.
     */
    public static final String LABEL_APP_KEY = "app";
    public static final String LABEL_VERSION_KEY = "version";
    public static final String LABEL_INTERFACE_KEY = "interface";
    public static final String LABEL_RUN_KEY = "run";

    private final MicoKubernetesConfig micoKubernetesConfig;
    private final ClusterAwarenessFabric8 cluster;

    @Autowired
    public MicoKubernetesClient(MicoKubernetesConfig micoKubernetesConfig, ClusterAwarenessFabric8 cluster) {
        this.micoKubernetesConfig = micoKubernetesConfig;
        this.cluster = cluster;
    }

    /**
     * Create a Kubernetes deployment based on a MICO service.
     *
     * @param micoService    the {@link MicoService}
     * @param deploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return the Kubernetes {@link Deployment} resource object
     */
    public Deployment createMicoService(MicoService micoService, MicoServiceDeploymentInfo deploymentInfo) throws KubernetesResourceException {
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();
        String deploymentUid;

        // Check if there are already Kubernetes deployments for the requested MicoService
        List<Deployment> alreadyExistingDeployments = getDeployedMicoServices(micoService);
        if (alreadyExistingDeployments.isEmpty()) {
            // Create new Kubernetes Deployment -> create new name for it
            deploymentUid = UIDUtils.uidFor(micoService);
        } else if (alreadyExistingDeployments.size() == 1) {
            // Kubernetes Deployment already exists -> use existing name and update the Deployment
            String existingDeploymentName = alreadyExistingDeployments.get(0).getMetadata().getName();
            log.info("MicoService '{}' in version '{}' is already deployed. Kubernetes Deployment '{}' will be updated.",
                micoService.getShortName(), micoService.getVersion(), existingDeploymentName);

            deploymentUid = existingDeploymentName;
        } else {
            // There is more than one deployment for the MicoService!
            log.warn("MicoService '{}' in version '{}' is already deployed multiple times. Don't know which to replace.",
                micoService.getShortName(), micoService.getVersion());
            throw new KubernetesResourceException("There are multiple Kubernetes Deployments for MicoService '"
                + micoService.getShortName() + "' in version '" + micoService.getVersion() + "'.");
        }

        Deployment deployment = new DeploymentBuilder()
            .withNewMetadata()
            .withName(deploymentUid)
            .withNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace())
            .addToLabels(LABEL_APP_KEY, micoService.getShortName())
            .addToLabels(LABEL_VERSION_KEY, micoService.getVersion())
            .addToLabels(LABEL_RUN_KEY, deploymentUid)
            .endMetadata()
            .withNewSpec()
            .withNewReplicas(deploymentInfo.getReplicas())
            .withNewSelector()
            .addToMatchLabels(LABEL_RUN_KEY, deploymentUid)
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .addToLabels(LABEL_APP_KEY, micoService.getShortName())
            .addToLabels(LABEL_VERSION_KEY, micoService.getVersion())
            .addToLabels(LABEL_RUN_KEY, deploymentUid)
            .endMetadata()
            .withNewSpec()
            .withContainers(
                new ContainerBuilder()
                    // TODO: Use containers from mico service deployment info
                    .withName(micoService.getShortName())
                    .withImage(micoService.getDockerImageUri())
                    .withPorts(createContainerPorts(micoService))
                    .build())
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

        return cluster.createDeployment(deployment, namespace);
    }

    /**
     * Create a Kubernetes service based on a MICO service interface.
     *
     * @param micoServiceInterface the {@link MicoServiceInterface}
     * @param micoService          the {@link MicoService}
     * @return the Kubernetes {@link Service} resource
     */
    public Service createMicoServiceInterface(MicoServiceInterface micoServiceInterface, MicoService micoService) throws KubernetesResourceException {
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();
        String serviceInterfaceUid;

        // Check if there are already Kubernetes services for the requested MicoServiceInterface
        List<Service> alreadyExistingServices = getDeployedMicoServiceInterfaces(micoService, micoServiceInterface);
        if (alreadyExistingServices.isEmpty()) {
            // Create new Kubernetes Service -> create enew name for it
            serviceInterfaceUid = UIDUtils.uidFor(micoServiceInterface);
        } else if (alreadyExistingServices.size() == 1) {
            // Kubernetes Service already exists -> use existing name and update the Service
            String existingServiceName = alreadyExistingServices.get(0).getMetadata().getName();
            log.info("MicoServiceInterface '{}' in version '{}' already exists. Kubernetes Service '{}' will be updated.",
                micoService.getShortName(), micoService.getVersion(), existingServiceName);

            serviceInterfaceUid = existingServiceName;
        } else {
            // There is more than one deployment for the MicoService!
            log.warn("MicoServiceInterface '{}' in version '{}' already exists multiple times. Don't know which to replace.",
                micoService.getShortName(), micoService.getVersion());
            throw new KubernetesResourceException("There are multiple Kubernetes Services for MicoServiceInterface '"
                + micoServiceInterface.getServiceInterfaceName() + "' of MicoService '" + micoService.getShortName() + "' in version '" + micoService.getVersion() + "'.");
        }

        // Retrieve deployment corresponding to given MicoService to retrieve
        // the unique run label which will be used for the Kubernetes Service, too.
        Map<String, String> labels = CollectionUtils.mapOf(LABEL_APP_KEY, micoService.getShortName(), LABEL_VERSION_KEY, micoService.getVersion());
        List<Deployment> matchingDeployments = cluster.getDeploymentsByLabels(labels, namespace).getItems();

        if (matchingDeployments.size() == 0) {
            throw new KubernetesResourceException("There are no deployments for service with name '"
                + micoService.getShortName() + "' and version '" + micoService.getVersion() + "'.");
        } else if (matchingDeployments.size() > 1) {
            throw new KubernetesResourceException("There are multiple deployments for service with name '"
                + micoService.getShortName() + "' and version '" + micoService.getVersion() + "'.");
        }

        String serviceUid = matchingDeployments.get(0).getMetadata().getLabels().get(LABEL_RUN_KEY);
        serviceUid = micoService.getShortName() + serviceUid.substring(serviceUid.lastIndexOf("-"));

        Service service = new ServiceBuilder()
            .withNewMetadata()
            .withName(serviceInterfaceUid)
            .withNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace())
            .addToLabels(LABEL_APP_KEY, micoService.getShortName())
            .addToLabels(LABEL_VERSION_KEY, micoService.getVersion())
            .addToLabels(LABEL_INTERFACE_KEY, micoServiceInterface.getServiceInterfaceName())
            .addToLabels(LABEL_RUN_KEY, serviceUid)
            .endMetadata()
            .withNewSpec()
            .withType("LoadBalancer")
            .withPorts(createServicePorts(micoServiceInterface))
            .addToSelector(LABEL_RUN_KEY, serviceUid)
            .endSpec()
            .build();

        // TODO: Check whether optional fields of MicoServiceInterface have to be used in some way
        // (publicDns, description, protocol, transportProtocol)

        return cluster.createService(service, namespace);
    }

    /**
     * Looks up deployed MICO services by checking the labels of the existing Kubernetes deployments.
     *
     * @param micoService the MICO service
     * @return the list of Kubernetes deployments
     */
    private List<Deployment> getDeployedMicoServices(MicoService micoService) {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_APP_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion()
        );
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();

        List<Deployment> deploymentList = cluster.getDeploymentsByLabels(labels, namespace).getItems();
        log.debug("Found {} Kubernetes deployment(s) that match the labels '{}': '{}'", deploymentList.size(), labels.toString(), deploymentList);
        return deploymentList;
    }

    /**
     * Looks up deployed MICO service interfaces by checking the labels of the existing Kubernetes services.
     *
     * @param micoService          the MICO service
     * @param micoServiceInterface the MICO service interface
     * @return the list of Kubernetes services
     */
    private List<Service> getDeployedMicoServiceInterfaces(MicoService micoService, MicoServiceInterface micoServiceInterface) {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_APP_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion(),
            LABEL_INTERFACE_KEY, micoServiceInterface.getServiceInterfaceName()
        );
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();
        List<Service> serviceList = cluster.getServicesByLabels(labels, namespace).getItems();
        log.debug("Found {} Kubernetes service(s) that match the labels '{}': '{}'", serviceList.size(), labels.toString(), serviceList);
        return serviceList;
    }

    /**
     * Creates a list of ports based on a MICO service. This list of ports
     * is intended for use with a container inside a Kubernetes deployment.
     *
     * @param service the {@link MicoService}.
     * @return an {@link ArrayList} with the {@link ContainerPort} instances.
     */
    private List<ContainerPort> createContainerPorts(MicoService service) {
        List<ContainerPort> ports = new ArrayList<>();

        for (MicoServiceInterface serviceInterface : service.getServiceInterfaces()) {
            for (MicoServicePort servicePort : serviceInterface.getPorts()) {
                ports.add(new ContainerPortBuilder()
                    .withContainerPort(servicePort.getTargetPort())
                    .withProtocol(servicePort.getType().toString())
                    .build());
            }
        }

        return ports;
    }

    /**
     * Creates a list of service ports (port, target port and type) based on a MICO
     * service interface. This list of service ports is intended for use with a
     * Kubernetes service.
     *
     * @param serviceInterface the {@link MicoServiceInterface}.
     * @return an {@link ArrayList} with the {@link ServicePort} instances.
     */
    private List<ServicePort> createServicePorts(MicoServiceInterface serviceInterface) {
        List<ServicePort> ports = new ArrayList<>();

        for (MicoServicePort servicePort : serviceInterface.getPorts()) {
            ports.add(new ServicePortBuilder()
                .withNewPort(servicePort.getNumber())
                .withNewTargetPort(servicePort.getTargetPort())
                .withProtocol(servicePort.getType().toString())
                .build());
        }

        return ports;
    }

}
