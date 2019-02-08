package io.github.ust.mico.core.mapping;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
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

/**
 * Provides accessor methods for creating deployment and services in Kubernetes.
 */
@Component
public class MicoKubernetesClient {

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
     * @param service the {@link MicoService}
     * @param deploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return the Kubernetes {@link Deployment} resource object
     */
    public Deployment createMicoService(MicoService service, MicoServiceDeploymentInfo deploymentInfo) {
        String deploymentUid = UIDUtils.uidFor(service);
        
        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(deploymentUid)
                    .withNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace())
                    .addToLabels("app", service.getShortName())
                    .addToLabels("version", service.getVersion())
                    .addToLabels("run", deploymentUid)
                .endMetadata()
                .withNewSpec()
                    .withNewReplicas(deploymentInfo.getReplicas())
                    .withNewSelector()
                     .addToMatchLabels("run", deploymentUid)
                     .endSelector()
                     .withNewTemplate()
                         .withNewMetadata()
                             .addToLabels("app", service.getShortName())
                             .addToLabels("version", service.getVersion())
                             .addToLabels("run", deploymentUid)
                         .endMetadata()
                         .withNewSpec()
                             .withContainers(
                                 new ContainerBuilder()
                                 // TODO: Use containers from mico service deployment info
                                     .withName(service.getShortName())
                                     .withImage(service.getDockerImageUri())
                                     .withPorts(createContainerPorts(service))
                                     .build())
                         .endSpec()
                     .endTemplate()
                .endSpec()
                .build();

        return cluster.getClient().apps().deployments().inNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace()).createOrReplace(deployment);
    }
    
    /**
     * Create a Kubernetes service based on a MICO service interface.
     *
     * @param micoServiceInterface the {@link MicoServiceInterface}
     * @param micoService the {@link MicoService}
     * @return the Kubernetes {@link Service} resource
     */
    public Service createMicoServiceInterface(MicoServiceInterface micoServiceInterface, MicoService micoService) throws KubernetesResourceException {
        // Unique identifier for the service interface used as its name tag
        String serviceInterfaceUid = UIDUtils.uidFor(micoServiceInterface);
        
        // Retrieve deployment corresponding to given MicoService to retrieve
        // the unique run label which will be used for the Kubernetes Service, too.
        List<Deployment> matchingDeployments = cluster.getClient().apps()
                .deployments()
                .inNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace())
                .withLabels(
                        CollectionUtils.mapOf("app", micoService.getShortName(), "version", micoService.getVersion()))
                .list().getItems();
        
        if (matchingDeployments.size() == 0) {
            throw new KubernetesResourceException("There are no deployments for service with name '"
                    + micoService.getShortName() + "' and version '" + micoService.getVersion() + "'.");
        } else if (matchingDeployments.size() > 1) {
            throw new KubernetesResourceException("There are multiple deployments for service with name '"
                    + micoService.getShortName() + "' and version '" + micoService.getVersion() + "'.");
        }
        
        String serviceUid = matchingDeployments.get(0).getMetadata().getLabels().get("run");
        serviceUid = micoService.getShortName() + serviceUid.substring(serviceUid.lastIndexOf("-"));

        Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withName(serviceInterfaceUid)
                    .withNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace())
                    .addToLabels("app", micoService.getShortName())
                    .addToLabels("version", micoService.getVersion())
                    .addToLabels("run", serviceUid)
                .endMetadata()
                .withNewSpec()
                    .withType("LoadBalancer")
                    .withPorts(createServicePorts(micoServiceInterface))
                    .addToSelector("run", serviceUid)
                .endSpec()
                .build();

        // TODO: Check whether optional fields of MicoServiceInterface have to be used in some way
        // (publicDns, description, protocol, transportProtocol)
        
        return cluster.getClient().services().inNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace()).createOrReplace(service);
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
