/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core.service;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.UIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides accessor methods for creating deployments and services in Kubernetes as well as getter methods to retrieve
 * existing Kubernetes deployments and services.
 */
@Slf4j
@Component
public class MicoKubernetesClient {

    /**
     * Prefix that is used for all MICO specific labels.
     * Kubernetes recommends to use such a common prefix.
     *
     * @see <a href="https://kubernetes.io/docs/concepts/overview/working-with-objects/common-labels">Recommended Labels</a>
     */
    private static final String LABEL_PREFIX = "ust.mico/";
    /**
     * The label to get the name of the {@link MicoService}.
     * It is used in conjunction with the version label to select all Kubernetes resources
     * that belong to a specific version of a {@link MicoService}.
     * It is set to the value of the `shortName` property of the {@link MicoService}.
     */
    private static final String LABEL_NAME_KEY = LABEL_PREFIX + "name";
    /**
     * The label to get the current version of the {@link MicoService} (semantic version).
     * It is used in conjunction with the name label to select all Kubernetes resources
     * that belong to a specific version of a {@link MicoService}.
     * It is set to the value of the `version` property of the {@link MicoService}.
     */
    private static final String LABEL_VERSION_KEY = LABEL_PREFIX + "version";
    /**
     * The label to get the name of the {@link MicoServiceInterface}.
     * It is used in conjunction with the name and version label to select the Kubernetes {@link Service} resource
     * that belong to a specific version of a {@link MicoServiceInterface}.
     * It is set to the value of the name property of the {@link MicoServiceInterface}.
     */
    private static final String LABEL_INTERFACE_KEY = LABEL_PREFIX + "interface";
    /**
     * The label to identify the instance of the MICO resource ({@link MicoService} or {@link MicoServiceInterface}).
     * {@link MicoService}:
     * Label is used for the selector field of Kubernetes Deployments to find the Pods to manage.
     * {@link MicoServiceInterface}:
     * Label is used for the selector field of Kubernetes Services to find the Pods to target.
     * <p>
     * It is a unique name (UID) created for each {@link MicoService}.
     */
    private static final String LABEL_INSTANCE_KEY = LABEL_PREFIX + "instance";

    private final MicoKubernetesConfig micoKubernetesConfig;
    private final KubernetesClient kubernetesClient;
    private final MicoServiceRepository serviceRepository;

    @Autowired
    public MicoKubernetesClient(MicoKubernetesConfig micoKubernetesConfig, MicoServiceRepository serviceRepository,
                                KubernetesClient kubernetesClient) {
        this.micoKubernetesConfig = micoKubernetesConfig;
        this.serviceRepository = serviceRepository;
        this.kubernetesClient = kubernetesClient;
    }

    /**
     * Create a Kubernetes deployment based on a {@link MicoServiceDeploymentInfo}.
     *
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return the Kubernetes {@link Deployment} resource object
     */
    public Deployment createMicoService(MicoServiceDeploymentInfo serviceDeploymentInfo) throws KubernetesResourceException {
        MicoService micoService = serviceDeploymentInfo.getService();
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();
        String deploymentUid;

        // Check if there are already Kubernetes deployments for the requested MicoService
        Optional<Deployment> existingDeployment = getDeploymentOfMicoService(micoService);
        if (!existingDeployment.isPresent()) {
            // There is no existing deployment -> create new Kubernetes Deployment with a new name
            deploymentUid = UIDUtils.uidFor(micoService);
        } else {
            // There is already a Kubernetes Deployment -> use existing name and update the Deployment
            String existingDeploymentName = existingDeployment.get().getMetadata().getName();
            log.info("MicoService '{}' in version '{}' is already deployed. Kubernetes Deployment '{}' will be updated.",
                micoService.getShortName(), micoService.getVersion(), existingDeploymentName);

            deploymentUid = existingDeploymentName;
        }

        Deployment deployment = new DeploymentBuilder()
            .withNewMetadata()
            .withName(deploymentUid)
            .withNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace())
            .withLabels(serviceDeploymentInfo.getLabels().stream().collect(
                Collectors.toMap(MicoLabel::getKey, MicoLabel::getValue)))
            .addToLabels(LABEL_NAME_KEY, micoService.getShortName())
            .addToLabels(LABEL_VERSION_KEY, micoService.getVersion())
            .addToLabels(LABEL_INSTANCE_KEY, deploymentUid)
            .endMetadata()
            .withNewSpec()
            .withReplicas(serviceDeploymentInfo.getReplicas())
            .withNewSelector()
            .addToMatchLabels(LABEL_INSTANCE_KEY, deploymentUid)
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .withLabels(serviceDeploymentInfo.getLabels().stream().collect(
                Collectors.toMap(MicoLabel::getKey, MicoLabel::getValue)))
            .addToLabels(LABEL_NAME_KEY, micoService.getShortName())
            .addToLabels(LABEL_VERSION_KEY, micoService.getVersion())
            .addToLabels(LABEL_INSTANCE_KEY, deploymentUid)
            .endMetadata()
            .withNewSpec()
            .withRestartPolicy(serviceDeploymentInfo.getRestartPolicy().toString())
            .withContainers(
                new ContainerBuilder()
                    .withName(micoService.getShortName())
                    .withImage(micoService.getDockerImageUri())
                    .withImagePullPolicy(serviceDeploymentInfo.getImagePullPolicy().toString())
                    .withPorts(createContainerPorts(micoService.getServiceInterfaces()))
                    .withEnv(serviceDeploymentInfo.getEnvironmentVariables().stream().map(
                        environmentVariable -> new EnvVarBuilder()
                            .withName(environmentVariable.getName())
                            .withValue(environmentVariable.getValue())
                            .build())
                        .collect(Collectors.toList()))
                    .build())
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

        Deployment createdDeployment = kubernetesClient.apps().deployments().inNamespace(namespace).createOrReplace(deployment);
        log.debug("Successfully created Kubernetes deployment '{}' in namespace '{}' for MicoService '{}' '{}'",
            createdDeployment.getMetadata().getName(), namespace, micoService.getShortName(), micoService.getVersion());
        return createdDeployment;
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
        String serviceInterfaceName = micoServiceInterface.getServiceInterfaceName();
        Optional<Service> existingService = getInterfaceByNameOfMicoService(micoService, serviceInterfaceName);
        if (!existingService.isPresent()) {
            // There is no existing service -> create new Kubernetes Service with a new name
            serviceInterfaceUid = UIDUtils.uidFor(micoServiceInterface);
        } else {
            // There is already a Kubernetes Service -> use existing name and update the Service
            String existingServiceName = existingService.get().getMetadata().getName();
            log.info("MicoServiceInterface '{}' in version '{}' already exists. Kubernetes Service '{}' will be updated.",
                micoService.getShortName(), micoService.getVersion(), existingServiceName);

            serviceInterfaceUid = existingServiceName;
        }

        // Retrieve deployment corresponding to given MicoService to retrieve
        // the unique run label which will be used for the Kubernetes Service, too.
        Map<String, String> labels = CollectionUtils.mapOf(LABEL_NAME_KEY, micoService.getShortName(), LABEL_VERSION_KEY, micoService.getVersion());
        List<Deployment> matchingDeployments = kubernetesClient.apps().deployments().inNamespace(namespace).withLabels(labels).list().getItems();

        if (matchingDeployments.size() == 0) {
            throw new KubernetesResourceException("There are no deployments for service with name '"
                + micoService.getShortName() + "' and version '" + micoService.getVersion() + "'.");
        } else if (matchingDeployments.size() > 1) {
            throw new KubernetesResourceException("There are multiple deployments for service with name '"
                + micoService.getShortName() + "' and version '" + micoService.getVersion() + "'.");
        }

        String serviceUid = matchingDeployments.get(0).getMetadata().getLabels().get(LABEL_INSTANCE_KEY);
        serviceUid = micoService.getShortName() + serviceUid.substring(serviceUid.lastIndexOf("-"));

        Service service = new ServiceBuilder()
            .withNewMetadata()
            .withName(serviceInterfaceUid)
            .withNamespace(micoKubernetesConfig.getNamespaceMicoWorkspace())
            .addToLabels(LABEL_NAME_KEY, micoService.getShortName())
            .addToLabels(LABEL_VERSION_KEY, micoService.getVersion())
            .addToLabels(LABEL_INTERFACE_KEY, serviceInterfaceName)
            .addToLabels(LABEL_INSTANCE_KEY, serviceUid)
            .endMetadata()
            .withNewSpec()
            .withType("LoadBalancer")
            .withPorts(createServicePorts(micoServiceInterface))
            .addToSelector(LABEL_INSTANCE_KEY, serviceUid)
            .endSpec()
            .build();

        // TODO: Check whether optional fields of MicoServiceInterface have to be used in some way
        // (publicDns, description, protocol, transportProtocol)

        Service createdService = kubernetesClient.services().inNamespace(namespace).createOrReplace(service);
        log.debug("Successfully created Kubernetes service '{}' in namespace '{}' for MicoServiceInterface '{}' of MicoService '{}' '{}'",
            createdService.getMetadata().getName(), namespace, serviceInterfaceName, micoService.getShortName(), micoService.getVersion());
        return createdService;
    }

    /**
     * Checks if a MICO application is already deployed.
     *
     * @param micoApplication the {@link MicoApplication}
     * @return if true the application is deployed.
     * @throws KubernetesResourceException if there is an error while retrieving the Kubernetes objects
     */
    public boolean isApplicationDeployed(MicoApplication micoApplication) throws KubernetesResourceException {
        boolean result = false;

        for (MicoService micoService : serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion())) {
            if (isMicoServiceDeployed(micoService)) {
                result = true;
                break;
            }
        }
        String deploymentStatus = result ? "deployed" : "not deployed";
        log.info("MicoApplication '{}' in version '{}' is {}.",
            micoApplication.getShortName(), micoApplication.getVersion(), deploymentStatus);
        return result;
    }


    /**
     * Checks if a MICO service is already deployed.
     *
     * @param micoService the {@link MicoService}
     * @return {@code true} if the {@link MicoService} is deployed.
     * @throws KubernetesResourceException if there is an error while retrieving the Kubernetes objects
     */
    public boolean isMicoServiceDeployed(MicoService micoService) throws KubernetesResourceException {
        boolean result = false;
        Optional<Deployment> deployment = getDeploymentOfMicoService(micoService);
        if (deployment.isPresent()) {
            result = true;
        }
        String deploymentStatus = result ? "deployed" : "not deployed";
        log.info("MicoService '{}' in version '{}' is {}.",
            micoService.getShortName(), micoService.getVersion(), deploymentStatus);
        return result;
    }

    /**
     * Checks if the {@link MicoService} is already deployed to the Kubernetes cluster. Labels are used for the lookup.
     *
     * @param micoService the {@link MicoService}
     * @return an {@link Optional<Deployment>} with the {@link Deployment} of the Kubernetes service, or an empty {@link Optional<Deployment>} if there is no Kubernetes deployment of the {@link MicoService}.
     */
    public Optional<Deployment> getDeploymentOfMicoService(MicoService micoService) throws KubernetesResourceException {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion()
        );
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();

        List<Deployment> deploymentList = kubernetesClient.apps().deployments().inNamespace(namespace).withLabels(labels).list().getItems();
        log.debug("Found {} Kubernetes deployment(s) that match the labels '{}': '{}'", deploymentList.size(), labels.toString(), deploymentList);

        if (deploymentList.isEmpty()) {
            log.debug("No Kubernetes deployment found for MicoService '{}' '{}'", micoService.getShortName(), micoService.getVersion());
            return Optional.empty();
        } else if (deploymentList.size() == 1) {
            return Optional.of(deploymentList.get(0));
        } else {
            // It should be not possible that there are multiple deployments for the same version of a MicoService.
            log.warn("MicoService '{}' in version '{}' is deployed multiple times: {}",
                micoService.getShortName(), micoService.getVersion(), deploymentList);
            throw new KubernetesResourceException("There are multiple Kubernetes Deployments for MicoService '"
                + micoService.getShortName() + "' '" + micoService.getVersion() + "'.");
        }
    }

    /**
     * Check if the {@link MicoServiceInterface} is already created for the {@link MicoService} in the Kubernetes
     * cluster. Labels are used for the lookup.
     *
     * @param micoService              the {@link MicoService}
     * @param micoServiceInterfaceName the name of a {@link MicoServiceInterface}
     * @return an {@link Optional<Service>} with the Kubernetes {@link Service}, or an emtpy {@link Optional<Service>} if there is no Kubernetes deployment of the {@link Service}.
     */
    public Optional<Service> getInterfaceByNameOfMicoService(MicoService micoService, String micoServiceInterfaceName) throws KubernetesResourceException {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion(),
            LABEL_INTERFACE_KEY, micoServiceInterfaceName
        );
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();
        List<Service> serviceList = kubernetesClient.services().inNamespace(namespace).withLabels(labels).list().getItems();
        log.debug("Found {} Kubernetes service(s) that match the labels '{}': '{}'", serviceList.size(), labels.toString(), serviceList);

        if (serviceList.isEmpty()) {
            log.debug("No Kubernetes Service found for MicoServiceInterface '{}' of MicoService '{}' '{}'",
                micoServiceInterfaceName, micoService.getShortName(), micoService.getVersion());
            return Optional.empty();
        } else if (serviceList.size() == 1) {
            return Optional.of(serviceList.get(0));
        } else {
            // It should be not possible that there are multiple services for the same interface of a MicoService.
            log.warn("MicoServiceInterface '{}' of MicoService '{}' in version '{}' is deployed multiple times: {}",
                micoServiceInterfaceName, micoService.getShortName(), micoService.getVersion(), serviceList);
            throw new KubernetesResourceException("There are multiple Kubernetes Services for MicoServiceInterface '"
                + micoServiceInterfaceName + "' of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "'.");
        }
    }

    /**
     * Looks up if there are any interfaces created for the {@link MicoService} in the Kubernetes cluster. If so, it
     * returns them as a list of Kubernetes {@link Service} objects. Labels are used for the lookup.
     *
     * @param micoService the {@link MicoService}
     * @return the list of Kubernetes {@link Service} objects
     */
    public List<Service> getInterfacesOfMicoService(MicoService micoService) {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion()
        );
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();
        List<Service> serviceList = kubernetesClient.services().inNamespace(namespace).withLabels(labels).list().getItems();
        log.debug("Found {} Kubernetes service(s) that match the labels '{}': '{}'", serviceList.size(), labels.toString(), serviceList);

        return serviceList;
    }

    /**
     * Looks up if the {@link MicoService} is already deployed to the Kubernetes cluster. If so, it returns the list of
     * Kubernetes {@link Pod} objects that belongs to the {@link Deployment}. Labels are used for the lookup.
     *
     * @param micoService the {@link MicoService}
     * @return the list of Kubernetes {@link Pod} objects
     */
    public List<Pod> getPodsCreatedByDeploymentOfMicoService(MicoService micoService) {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion()
        );
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();
        List<Pod> podList = kubernetesClient.pods().inNamespace(namespace).withLabels(labels).list().getItems();
        log.debug("Found {} Kubernetes pod(s) that match the labels '{}': '{}'", podList.size(), labels.toString(), podList);

        return podList;
    }

    /**
     * Creates a list of ports based on the service interfaces.
     * This list of ports is intended for use with a container inside a Kubernetes deployment.
     *
     * @param serviceInterfaces the {@link MicoServiceInterface MicoServiceInterfaces}.
     * @return an {@link ArrayList} with the {@link ContainerPort} instances.
     */
    private List<ContainerPort> createContainerPorts(List<MicoServiceInterface> serviceInterfaces) {
        List<ContainerPort> ports = new ArrayList<>();

        for (MicoServiceInterface serviceInterface : serviceInterfaces) {
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
     * Creates a list of service ports (port, target port and type) based on a MICO service interface. This list of
     * service ports is intended for use with a Kubernetes service.
     *
     * @param serviceInterface the {@link MicoServiceInterface}.
     * @return an {@link ArrayList} with the {@link ServicePort} instances.
     */
    private List<ServicePort> createServicePorts(MicoServiceInterface serviceInterface) {
        List<ServicePort> ports = new ArrayList<>();

        for (MicoServicePort servicePort : serviceInterface.getPorts()) {
            ports.add(new ServicePortBuilder()
                .withPort(servicePort.getPort())
                .withNewTargetPort(servicePort.getTargetPort())
                .withProtocol(servicePort.getType().toString())
                .build());
        }

        return ports;
    }
}
