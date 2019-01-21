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
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.ust.mico.core.ClusterAwarenessFabric8;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides access to the Kubernetes API.
 */
@Component
public class MicoKubeClient {

    private final KubernetesMappingConfig kubernetesMappingConfig;

    private final ClusterAwarenessFabric8 cluster;

    @Autowired
    public MicoKubeClient(KubernetesMappingConfig kubernetesMappingConfig, ClusterAwarenessFabric8 cluster) {
        this.kubernetesMappingConfig = kubernetesMappingConfig;
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
                    .withNamespace(kubernetesMappingConfig.getNamespaceMicoWorkspace())
                    .addToLabels("app", applicationName)
                .endMetadata()
                .withNewSpec()
                    .withNewReplicas(replicas)
                    .withNewSelector()
                        .addToMatchLabels("app", service.getShortName())
                     .endSelector()
                     .withNewTemplate()
                         .withNewMetadata()
                             .addToLabels("app", service.getShortName())
                         .endMetadata()
                         .withNewSpec()
                             .withContainers(
                                 new ContainerBuilder()
                                     .withName(service.getShortName())
                                     .withImage(kubernetesMappingConfig.getDefaultImageRegistry() + "/" + service.getShortName() + ":" + service.getVersion())
                                     .withPorts(createContainerPorts(service))
                                     .build())
                         .endSpec()
                     .endTemplate()
                .endSpec()
                .build();

        KubernetesClient client = cluster.getClient();
        return client.apps().deployments().inNamespace(kubernetesMappingConfig.getNamespaceMicoWorkspace()).create(deployment);
    }

    /**
     * Create a Kubernetes service based on a MICO service interface.
     *
     * @param serviceInterface the {@link MicoServiceInterface}
     * @param applicationName the application name
     * @return the Kubernetes {@link Service} resource
     */
    public Service createMicoServiceInterface(MicoServiceInterface serviceInterface, String applicationName) {
        Service service = new ServiceBuilder()
                . withNewMetadata()
                    .withName(serviceInterface.getServiceInterfaceName())
                    .addToLabels("app", applicationName)
                .endMetadata()
                .withNewSpec()
                    .withPorts(createServicePorts(serviceInterface))
                .endSpec()
                .build();

        // TODO: Check whether optional fields of MicoServiceInterface have to be used in some way
        // (publicDns, description, protocol, transportProtocol)

        KubernetesClient client = cluster.getClient();
        return client.services().inNamespace(kubernetesMappingConfig.getNamespaceMicoSystem()).create(service);
    }

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
