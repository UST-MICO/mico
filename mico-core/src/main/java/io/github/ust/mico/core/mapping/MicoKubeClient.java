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
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides access to the Kubernetes API.
 */
@AllArgsConstructor
@Component
public class MicoKubeClient {

    private static final String DEFAULT_NAMESPACE = "default";
    private static final String DEFAULT_REGISTRY = "ustmicoregistry.azurecr.io";

    @Autowired
    private ClusterAwarenessFabric8 cluster;

    /**
     * TODO: Comment
     * 
     * @param service
     * @param replicas
     * @return
     */
    public Deployment createMicoService(MicoService service, int replicas) {
        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(service.getName())
                    .withName(DEFAULT_NAMESPACE)
                    .addToLabels("app", service.getShortName())
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
                                     .withImage(DEFAULT_REGISTRY + "/" + service.getShortName() + ":" + service.getVersion())
                                     .withPorts(createContainerPorts(service))
                                     .build())
                         .endSpec()
                     .endTemplate()
                .endSpec()
                .build();

        KubernetesClient client = cluster.getClient();
        return client.apps().deployments().inNamespace(DEFAULT_NAMESPACE).create(deployment);
    }
    
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
        return client.services().inNamespace(DEFAULT_NAMESPACE).create(service);
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
