package io.github.ust.mico.core.mapping;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import lombok.AllArgsConstructor;

/**
 * Provides access to the Kubernetes API.
 */
@AllArgsConstructor
public class MicoKubeClient {

    private static final String DEFAULT_NAMESPACE = "default";
    private static final String DEFAULT_REGISTRY = "ustmicoregistry.azurecr.io";

    private KubernetesClient client = new DefaultKubernetesClient();

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
        return client.apps().deployments().inNamespace(DEFAULT_NAMESPACE).create(deployment);
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

}
