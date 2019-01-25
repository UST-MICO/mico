package io.github.ust.mico.core.mapping;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.ust.mico.core.ClusterAwarenessFabric8;
import io.github.ust.mico.core.MicoKubernetesConfig;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
     * @param applicationName the {@link MicoApplication} short name
     * @param replicas the number of replicas
     * @return the Kubernetes {@link Deployment} resource object
     */
    public Deployment createMicoService(MicoService service, String applicationName, int replicas) {
        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(service.getShortName())
                    .withNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace())
                    .addToLabels("app", service.getShortName())
                    .addToLabels("version", service.getVersion())
                .endMetadata()
                .withNewSpec()
                    .withNewReplicas(replicas)
                    .withNewSelector()
                        .addToMatchLabels("app", service.getShortName())
                        .addToMatchLabels("version", service.getVersion())
                     .endSelector()
                     .withNewTemplate()
                         .withNewMetadata()
                             .addToLabels("app", service.getShortName())
                             .addToLabels("version", service.getVersion())
                         .endMetadata()
                         .withNewSpec()
                             .withContainers(
                                 new ContainerBuilder()
                                     .withName(service.getShortName())
                                     .withImage(service.getDockerImageUri())
                                     .withPorts(createContainerPorts(service))
                                     .build())
                         .endSpec()
                     .endTemplate()
                .endSpec()
                .build();

        KubernetesClient client = cluster.getClient();
        return client.apps().deployments().inNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace()).create(deployment);
    }

    /**
     * Create a Kubernetes service based on a MICO service interface.
     *
     * @param serviceInterface the {@link MicoServiceInterface}
     * @param applicationName the application name
     * @return the Kubernetes {@link Service} resource
     */
    public Service createMicoServiceInterface(MicoServiceInterface serviceInterface, String applicationName, String version) {
        Service service = new ServiceBuilder()
                . withNewMetadata()
                    .withName(serviceInterface.getServiceInterfaceName())
                    .addToLabels("app", applicationName)
                    .addToLabels("version", version)
                .endMetadata()
                .withNewSpec()
                    .withPorts(createServicePorts(serviceInterface))
                .endSpec()
                .build();

        // TODO: Check whether optional fields of MicoServiceInterface have to be used in some way
        // (publicDns, description, protocol, transportProtocol)

        KubernetesClient client = cluster.getClient();
        return client.services().inNamespace(micoKubernetesConfig.getNamespaceMicoSystem()).create(service);
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
