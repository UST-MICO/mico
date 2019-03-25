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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.ust.mico.core.service.imagebuilder.buildtypes.Build;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.KubernetesDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.UIDUtils;
import lombok.extern.slf4j.Slf4j;

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


    /**
     * The revision history limit specifies the number of old ReplicaSets to retain to allow rollback.
     * Setting this field to zero means that all old ReplicaSets with 0 replicas will be cleaned up.
     * For more information see https://kubernetes.io/docs/concepts/workloads/controllers/deployment/#revision-history-limit
     */
    private static final Integer REVISION_HISTORY_LIMIT = 0;

    private final MicoKubernetesConfig micoKubernetesConfig;
    private final MicoKubernetesBuildBotConfig buildBotConfig;
    private final KubernetesClient kubernetesClient;
    private final ImageBuilder imageBuilder;
    private final MicoApplicationRepository applicationRepository;
    private final MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;
    private final KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;

    @Autowired
	public MicoKubernetesClient(MicoKubernetesConfig micoKubernetesConfig, MicoKubernetesBuildBotConfig buildBotConfig,
		KubernetesClient kubernetesClient, ImageBuilder imageBuilder, MicoApplicationRepository applicationRepository,
	    MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository, KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository) {
		this.micoKubernetesConfig = micoKubernetesConfig;
		this.buildBotConfig = buildBotConfig;
		this.kubernetesClient = kubernetesClient;
		this.imageBuilder = imageBuilder;
		this.applicationRepository = applicationRepository;
		this.serviceDeploymentInfoRepository = serviceDeploymentInfoRepository;
		this.kubernetesDeploymentInfoRepository = kubernetesDeploymentInfoRepository;
	}

    /**
     * Create a Kubernetes deployment based on a {@link MicoServiceDeploymentInfo}.
     *
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return the Kubernetes {@link Deployment} resource object
     */
    public Deployment createMicoService(MicoServiceDeploymentInfo serviceDeploymentInfo) throws KubernetesResourceException {
        MicoService micoService = serviceDeploymentInfo.getService();
        if (micoService == null) {
            throw new IllegalArgumentException("MicoService of service deployment information must not be null!");
        }
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
            .withRevisionHistoryLimit(REVISION_HISTORY_LIMIT)
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
        log.debug("Successfully created / updated Kubernetes deployment '{}' in namespace '{}' for MicoService '{}' '{}'",
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

        Service createdService = kubernetesClient.services().inNamespace(namespace).createOrReplace(service);
        log.debug("Successfully created / updated Kubernetes service '{}' in namespace '{}' for MicoServiceInterface '{}' of MicoService '{}' '{}'",
            createdService.getMetadata().getName(), namespace, serviceInterfaceName, micoService.getShortName(), micoService.getVersion());
        return createdService;
    }

    /**
     * Creates or updates all interface connections of the given {@code MicoApplication}.
     *
     * @param micoApplication the {@link MicoApplication}
     */
    public void createOrUpdateInterfaceConnections(MicoApplication micoApplication) {
        for (MicoServiceDeploymentInfo serviceDeploymentInfo : micoApplication.getServiceDeploymentInfos()) {
            MicoService micoService = serviceDeploymentInfo.getService();
            log.debug("MicoService '{}' '{}' of MicoApplication '{}' '{}' has {} interface connection(s).",
                micoService.getShortName(), micoService.getVersion(), micoApplication.getShortName(), micoApplication.getVersion(), serviceDeploymentInfo.getInterfaceConnections().size());
            for (MicoInterfaceConnection interfaceConnection : serviceDeploymentInfo.getInterfaceConnections()) {
                String targetMicoServiceShortName = interfaceConnection.getMicoServiceShortName();
                String targetMicoServiceInterfaceName = interfaceConnection.getMicoServiceInterfaceName();
                String environmentVariableName = interfaceConnection.getEnvironmentVariableName();
                Optional<MicoService> targetMicoServiceOptional = micoApplication.getServices().stream().filter(
                    service -> service.getShortName().equals(targetMicoServiceShortName)).findFirst();
                if (!targetMicoServiceOptional.isPresent()) {
                    log.error("Application '{}' '{}' does not include MicoService '{}'. Can't update interface connections of this MicoService.",
                        micoApplication.getShortName(), micoApplication.getVersion(), targetMicoServiceShortName);
                    continue;
                }
                MicoService targetMicoService = targetMicoServiceOptional.get();
                Optional<MicoServiceInterface> targetMicoServiceInterfaceOptional = targetMicoService.getServiceInterfaces().stream().filter(
                    serviceInterface -> serviceInterface.getServiceInterfaceName().equals(targetMicoServiceInterfaceName)).findFirst();
                if (!targetMicoServiceInterfaceOptional.isPresent()) {
                    log.error("MicoService '{}' of application '{}' '{}' does not provide an interface with the name '{}'. " +
                            "Can't update interface connections of this MicoServiceInterface.",
                        targetMicoServiceShortName, micoApplication.getShortName(), micoApplication.getVersion(), targetMicoServiceInterfaceName);
                    continue;
                }
                MicoServiceInterface targetServiceInterface = targetMicoServiceInterfaceOptional.get();
                log.info("Create / update interface connection between MicoService '{}' '{}' and interface '{}' of MicoService '{}' '{}'.",
                    micoService.getShortName(), micoService.getVersion(), targetMicoServiceInterfaceName,
                    targetMicoService.getShortName(), targetMicoService.getVersion());
                updateDnsEnvVar(micoService, targetMicoService, targetServiceInterface, environmentVariableName);
            }
        }
    }

    /**
     * Sets or updates the DNS environment variable for the given interface connection.
     *
     * @param micoServiceToUpdate        the {@link MicoService} to update
     * @param targetMicoService          the {@link MicoService} that is targeted
     * @param targetMicoServiceInterface the {@link MicoServiceInterface} that is targeted
     * @param environmentVariableName    the environment variable name
     */
    private void updateDnsEnvVar(MicoService micoServiceToUpdate, MicoService targetMicoService,
                                 MicoServiceInterface targetMicoServiceInterface, String environmentVariableName) {
        try {
            Optional<Deployment> deploymentToUpdateOptional = getDeploymentOfMicoService(micoServiceToUpdate);
            if (!deploymentToUpdateOptional.isPresent()) {
                log.error("There is no Kubernetes deployment for MicoService '{}' '{}'. Can't update DNS environment variable.",
                    micoServiceToUpdate.getShortName(), micoServiceToUpdate.getVersion());
                return;
            }
            Optional<Service> kubernetesServiceOptional = getInterfaceByNameOfMicoService(targetMicoService, targetMicoServiceInterface.getServiceInterfaceName());
            if (!kubernetesServiceOptional.isPresent()) {
                log.error("There is no Kubernetes service for interface '{}' of MicoService '{}' '{}'. Can't update DNS environment variable.",
                    targetMicoServiceInterface.getServiceInterfaceName(), micoServiceToUpdate.getShortName(), micoServiceToUpdate.getVersion());
                return;
            }

            Deployment deploymentToUpdate = deploymentToUpdateOptional.get();
            Service targetKubernetesService = kubernetesServiceOptional.get();

            String namespace = targetKubernetesService.getMetadata().getNamespace();
            String kubernetesServiceName = targetKubernetesService.getMetadata().getName();
            String dns = kubernetesServiceName + "." + namespace + ".svc.cluster.local";
            log.debug("For the connection between '{}' '{}' and the interface '{}' of '{}' '{}' the DNS record '{}' is used.",
                micoServiceToUpdate.getShortName(), micoServiceToUpdate.getVersion(), targetMicoServiceInterface,
                targetMicoService.getShortName(), targetMicoService.getVersion(), dns);

            Optional<Container> containerToUpdateOptional = deploymentToUpdate.getSpec().getTemplate().getSpec().getContainers().stream().filter(
                c -> c.getName().equals(micoServiceToUpdate.getShortName())).findFirst();
            if(!containerToUpdateOptional.isPresent()) {
                log.error("Expected container '{}' of MicoService '{}' '{}' does not exist (existing containers '{}'). Can't update DNS environment variable.",
                    micoServiceToUpdate.getShortName(), micoServiceToUpdate.getShortName(), micoServiceToUpdate.getVersion(),
                    deploymentToUpdate.getSpec().getTemplate().getSpec().getContainers().stream().map(Container::getName).collect(Collectors.toList()));
                return;
            }
            Container containerToUpdate = containerToUpdateOptional.get();
            List<EnvVar> envVarList = containerToUpdate.getEnv();
            Optional<EnvVar> dnsEnvVarOptional = envVarList.stream().filter(envVar -> envVar.getName().equals(environmentVariableName)).findFirst();
            boolean updateRequired = true;
            if (dnsEnvVarOptional.isPresent()) {
                EnvVar dnsEnvVar = dnsEnvVarOptional.get();
                if (dnsEnvVar.getValue().equals(dns)) {
                    log.debug("DNS is already up to date. Update not required.");
                    updateRequired = false;
                } else {
                    log.debug("Deployment of MicoService '{}' '{}' contains a different value for the environment variable '{}'. " +
                            "It will be updated: '{}' → '{}'.",
                        micoServiceToUpdate.getShortName(), micoServiceToUpdate.getVersion(), dnsEnvVar.getName(), dnsEnvVar.getValue(), dns);
                    dnsEnvVar.setValue(dns);
                }
            } else {
                log.debug("Set new DNS environment variable '{}' to Kubernetes deployment of MicoService '{}' '{}': '{}'",
                    environmentVariableName, micoServiceToUpdate.getShortName(), micoServiceToUpdate.getVersion(), dns);
                envVarList.add(new EnvVarBuilder().withName(environmentVariableName).withValue(dns).build());
            }
            if (updateRequired) {
                containerToUpdate.setEnv(envVarList);
                log.debug("Deployment after setting env: {}", deploymentToUpdate);
                kubernetesClient.apps().deployments().inNamespace(namespace).createOrReplace(deploymentToUpdate);
                log.debug("Updated Kubernetes deployment with new DNS environment variable.");
            }
        } catch (KubernetesResourceException e) {
            log.error("Failed to set DNS environment variable for interface " + targetMicoServiceInterface.getServiceInterfaceName()
                + " of MicoService '" + micoServiceToUpdate.getShortName() + "' '" + micoServiceToUpdate.getVersion()
                + "'. Caused by: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a MICO application is already deployed.
     *
     * @param micoApplication the {@link MicoApplication}
     * @return {@code true} if the application is deployed.
     */
    public boolean isApplicationDeployed(MicoApplication micoApplication) {
        boolean result = false;

        List<MicoServiceDeploymentInfo> serviceDeploymentInfos = serviceDeploymentInfoRepository.findAllByApplication(
            micoApplication.getShortName(), micoApplication.getVersion());

        int expectedNumberOfServiceDeployments = serviceDeploymentInfos.size();
        if (expectedNumberOfServiceDeployments > 0) {
            int actualNumberOfServiceDeployments = 0;
            for (MicoServiceDeploymentInfo serviceDeploymentInfo : serviceDeploymentInfos) {
                MicoService micoService = serviceDeploymentInfo.getService();
                if (serviceDeploymentInfo.getKubernetesDeploymentInfo() == null) {
                    log.warn("There is no Kubernetes deployment information set for MicoService '{}' '{}'.",
                        micoService.getShortName(), micoService.getVersion());
                    continue;
                }

                // Check if the stored information are still valid.
                Optional<KubernetesDeploymentInfo> kubernetesDeploymentInfoOptional = updateKubernetesDeploymentInfo(serviceDeploymentInfo);
                if (kubernetesDeploymentInfoOptional.isPresent()) {
                    KubernetesDeploymentInfo kubernetesDeploymentInfo = kubernetesDeploymentInfoOptional.get();
                    log.debug("MicoService '{}' '{}' is deployed. The Kubernetes Deployment '{}' and Kubernetes Service(s) '{}' exist in the namespace '{}'.",
                        micoService.getShortName(), micoService.getVersion(), kubernetesDeploymentInfo.getDeploymentName(),
                        kubernetesDeploymentInfo.getServiceNames(), kubernetesDeploymentInfo.getNamespace());
                    actualNumberOfServiceDeployments++;
                }
            }
            if (actualNumberOfServiceDeployments == expectedNumberOfServiceDeployments) {
                result = true;
            }
        } else {
            log.warn("There are no service deployment information for MicoApplication '{}' '{}'",
                micoApplication.getShortName(), micoApplication.getVersion());
        }

        String deploymentStatus = result ? "deployed" : "not deployed";
        log.info("MicoApplication '{}' in version '{}' is {}.",
            micoApplication.getShortName(), micoApplication.getVersion(), deploymentStatus);
        return result;
    }

    /**
     * Checks if the current {@link KubernetesDeploymentInfo} of the provided {@link MicoServiceDeploymentInfo}
     * is up to date, stores the updated deployment information in the database and returns it.
     *
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return the updated {@link KubernetesDeploymentInfo}. Is {@code empty} if there is no deployment anymore.
     */
    private Optional<KubernetesDeploymentInfo> updateKubernetesDeploymentInfo(MicoServiceDeploymentInfo serviceDeploymentInfo) {
        MicoService micoService = serviceDeploymentInfo.getService();
        KubernetesDeploymentInfo currentKubernetesDeploymentInfo = serviceDeploymentInfo.getKubernetesDeploymentInfo();

        String namespace = currentKubernetesDeploymentInfo.getNamespace();
        String deploymentName = currentKubernetesDeploymentInfo.getDeploymentName();
        List<String> serviceNames = currentKubernetesDeploymentInfo.getServiceNames();

        if (namespace == null) {
            throw new IllegalArgumentException("There is no namespace set for MicoService " +
                "'" + micoService.getShortName() + "' '" + micoService.getVersion() + "'!");
        }
        if (deploymentName == null) {
            throw new IllegalArgumentException("There is no deployment name set for MicoService " +
                "'" + micoService.getShortName() + "' '" + micoService.getVersion() + "'!");
        }
        if (serviceNames == null) {
            throw new IllegalArgumentException("There are no Kubernetes Services set for MicoService " +
                "'" + micoService.getShortName() + "' '" + micoService.getVersion() + "'!");
        }

        Deployment actualKubernetesDeployment = null;
        List<Service> actualKubernetesServices = new ArrayList<>();
        if (kubernetesClient.namespaces().withName(namespace).get() != null) {
            actualKubernetesDeployment = kubernetesClient.apps().deployments().inNamespace(namespace).withName(deploymentName).get();
            if (actualKubernetesDeployment == null) {
                log.warn("Deployment '{}' of MicoService '{}' '{}' doesn't exist anymore!",
                    deploymentName, micoService.getShortName(), micoService.getVersion());
            }

            for (String serviceName : serviceNames) {
                Service actualKubernetesService = kubernetesClient.services().inNamespace(namespace).withName(serviceName).get();
                if (actualKubernetesService != null) {
                    actualKubernetesServices.add(actualKubernetesService);
                } else {
                    log.warn("Kubernetes service '{}' of MicoService '{}' '{}' doesn't exist anymore",
                        serviceName, micoService.getShortName(), micoService.getVersion());
                }
            }
        } else {
            log.warn("Namespace '{}' of deployment of MicoService '{}' '{}' doesn't exist anymore!",
                namespace, micoService.getShortName(), micoService.getVersion());
        }

        // Consider a deployment only as valid if there is a Kubernetes Deployment and at least one Kubernetes Service.
        boolean isDeploymentValid = actualKubernetesDeployment != null && actualKubernetesServices.size() > 0;
        if (isDeploymentValid) {
            KubernetesDeploymentInfo updatedKubernetesDeploymentInfo = new KubernetesDeploymentInfo()
                .setId(currentKubernetesDeploymentInfo.getId())
                .setNamespace(namespace)
                .setDeploymentName(actualKubernetesDeployment.getMetadata().getName())
                .setServiceNames(actualKubernetesServices.stream().map(svc -> svc.getMetadata().getName()).collect(Collectors.toList()));
            if(!currentKubernetesDeploymentInfo.equals(updatedKubernetesDeploymentInfo)) {
                log.info("Deployment information of MicoService '{}' '{}' has changed.",
                    micoService.getShortName(), micoService.getVersion());
                // Save the updated KubernetesDeploymentInfo to the database
                KubernetesDeploymentInfo savedKubernetesDeploymentInfo = kubernetesDeploymentInfoRepository.save(updatedKubernetesDeploymentInfo);
                log.debug("Updated Kubernetes deployment information of MicoService '{}' '{}': {}",
                    micoService.getShortName(), micoService.getVersion(), savedKubernetesDeploymentInfo.toString());
            }
            return Optional.of(updatedKubernetesDeploymentInfo);
        } else {
            log.warn("Actual Kubernetes deployment of MicoService '{}' '{}' is not valid!",
                micoService.getShortName(), micoService.getVersion());
            kubernetesDeploymentInfoRepository.delete(currentKubernetesDeploymentInfo);
            log.debug("Deleted outdated Kubernetes deployment information of MicoService '{}' '{}'",
                micoService.getShortName(), micoService.getVersion());
            return Optional.empty();
        }
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
        log.debug("MicoService '{}' in version '{}' is {}.",
            micoService.getShortName(), micoService.getVersion(), deploymentStatus);
        return result;
    }

    /**
     * Checks if the {@link MicoService} is already deployed to the Kubernetes cluster. Labels are used for the lookup.
     *
     * @param micoService the {@link MicoService}
     * @return an {@link Optional<Deployment>} with the {@link Deployment} of the Kubernetes service, or an empty {@link Optional<Deployment>} if there is no Kubernetes deployment of the {@link MicoService}.
     */
    public Optional<Deployment> getDeploymentOfMicoService(MicoService micoService) throws
        KubernetesResourceException {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion()
        );
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();

        List<Deployment> deploymentList = kubernetesClient.apps().deployments().inNamespace(namespace).withLabels(labels).list().getItems();
        log.debug("Found {} Kubernetes deployment(s) that match the labels '{}'.", deploymentList.size(), labels.toString());

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
     * @return an {@link Optional<Service>} with the Kubernetes {@link Service},
     * or an empty {@link Optional<Service>} if there is no Kubernetes {@link Service} for this {@link MicoServiceInterface}.
     */
    public Optional<Service> getInterfaceByNameOfMicoService(MicoService micoService, String micoServiceInterfaceName) throws KubernetesResourceException {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion(),
            LABEL_INTERFACE_KEY, micoServiceInterfaceName
        );
        String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();
        List<Service> serviceList = kubernetesClient.services().inNamespace(namespace).withLabels(labels).list().getItems();
        log.debug("Found {} Kubernetes service(s) that match the labels '{}'.", serviceList.size(), labels.toString());

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
        log.debug("Found {} Kubernetes service(s) that match the labels '{}'.", serviceList.size(), labels.toString());

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
        log.debug("Found {} Kubernetes pod(s) that match the labels '{}'.", podList.size(), labels.toString());

        return podList;
    }

    /**
     * Retrieves the yaml for a MicoService, contains the interfaces if they exist.
     *
     * @param micoService the {@link MicoService}
     * @return the kubernetes YAML for the {@link MicoService}.
     * @throws KubernetesResourceException if there is an error while retrieving the Kubernetes objects
     * @throws JsonProcessingException if there is a error processing the content.
     */
    public String getYaml(MicoService micoService) throws KubernetesResourceException, JsonProcessingException {
        StringBuilder yaml = new StringBuilder();
        Optional<Deployment> deploymentOptional = getDeploymentOfMicoService(micoService);
        if (deploymentOptional.isPresent()) {
            yaml.append(SerializationUtils.dumpWithoutRuntimeStateAsYaml(deploymentOptional.get()));
        }
        List<Service> kubernetesServices = getInterfacesOfMicoService(micoService);
        for (Service kubernetesService : kubernetesServices) {
            yaml.append(SerializationUtils.dumpWithoutRuntimeStateAsYaml(kubernetesService));
        }
        return yaml.toString();
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
    
    /**
     * Undeploys an application. Note that {@link MicoService MicoServices}
     * included in this application will not be undeployed, if and only if
     * they are included in at least one other application. In this case
     * the corresponding Kubernetes deployment will be scaled in.
     * 
     * @param application the {@link MicoApplication}.
     */
    public void undeployApplication(MicoApplication application) {
        log.debug("Start undeployment of MicoApplication '{}' '{}'.",
            application.getShortName(), application.getVersion());

    	for (MicoService service : application.getServices()) {
    		// Get all service deployment infos for this service,
    		// if there are multiple service deployment infos
            // with known Kubernetes deployment information,
    		// this service is used by multiple applications, i.e.,
    		// this service can't be simply undeployed but has to be
    		// scaled in instead.
			List<MicoServiceDeploymentInfo> serviceDeploymentInfos = serviceDeploymentInfoRepository.findAllByService(service.getShortName(), service.getVersion());

			// Service deployment info of the currently processed service
			Optional<MicoServiceDeploymentInfo> serviceDeploymentInfoOptional = serviceDeploymentInfoRepository.findByApplicationAndService(
				application.getShortName(), application.getVersion(), service.getShortName(), service.getVersion());
			
			// No service deployment info would mean that this service is not deployed
			if (!serviceDeploymentInfoOptional.isPresent() || serviceDeploymentInfos.isEmpty()) {
				log.error("Deployment info for MicoService '{}' in version '{}' is not available in the database.",
				    service.getShortName(), service.getVersion());
				throw new IllegalStateException(
					"Service deployment info for MicoApplication '" 
						+ application.getShortName() + "' '" + application.getVersion()
						+ "' and MicoService '" + service.getShortName() + "' '" + service.getVersion() + "' does not exist!");
			} else {
				MicoServiceDeploymentInfo serviceDeploymentInfo = serviceDeploymentInfoOptional.get();
                if(serviceDeploymentInfo.getKubernetesDeploymentInfo() == null) {
                    log.info("MicoService '{}' '{}' is not deployed for the MicoApplication '{}' '{}'. No undeployment/scaling required.",
                        service.getShortName(), service.getVersion(), application.getShortName(), application.getVersion());
                    continue;
                }
                // Check which applications are deployed and are actually using this service
                List<MicoApplication> applicationsUsingThisService = applicationRepository.findAllByUsedService(service.getShortName(), service.getVersion());
                List<MicoApplication> otherDeployedApplicationsUsingThisService = applicationsUsingThisService.stream()
                    .filter(app -> !(app.getShortName().equals(application.getShortName()) && app.getVersion().equals(application.getVersion()))
                        && isApplicationDeployed(app)).collect(Collectors.toList());

                if (otherDeployedApplicationsUsingThisService.isEmpty()) {
					// Service is not used by other deployed applications -> simply undeploy it
					log.debug("MicoService '{}' in version '{}' is not used by other MicoApplications.",
					    service.getShortName(), service.getVersion());
					undeploy(serviceDeploymentInfo);
				} else {
                    // Service used by multiple applications -> scale in
                    log.debug("MicoService '{}' in version '{}' is also used by {} other deployed MicoApplication(s): {}",
                        service.getShortName(), service.getVersion(),
                        otherDeployedApplicationsUsingThisService.size(),
                        otherDeployedApplicationsUsingThisService.stream()
                            .map(app -> "'" + app.getShortName() + "' '" + app.getVersion() + "'").collect(Collectors.toList())
                    );
					
					// Calculate the current total number of requested replicas for 
					// the currently processed service, which is the sum of requested
					// replicas of all applications that are using the current service
					// and are actually deployed.
					int currentTotalRequestedReplicas = serviceDeploymentInfo.getReplicas();
					for (MicoApplication otherApplicationUsingThisService : otherDeployedApplicationsUsingThisService) {
                        currentTotalRequestedReplicas += getServiceDeploymentInfo(otherApplicationUsingThisService, service).getReplicas();
					}
					log.debug("Currently {} replica(s) of MicoService '{}' in version '{}' are requested based on the given service deployment information.",
                        currentTotalRequestedReplicas, service.getShortName(), service.getVersion());
					
					// The updated number of total requested replicas for the current service
					// is the current total minus the replicas of the current service.
					int updatedTotalRequestedReplicas = currentTotalRequestedReplicas - serviceDeploymentInfo.getReplicas();
                    log.debug("Scale in MicoService '{}' in version '{}': {} → {}",
                        service.getShortName(), service.getVersion(), currentTotalRequestedReplicas, updatedTotalRequestedReplicas);

					// Actual scaling
					scale(serviceDeploymentInfo, updatedTotalRequestedReplicas);
				}
			}
    	}
    }
    
    /**
     * Scales a Kubernetes deployment for a {@code MicoService} to
     * a given number of replicas. If the specified number of replicas
     * is {@code 0}, the {@code MicoService} will be undeployed.
     * 
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}.
     * @param scaleToNumberOfReplicas the updated number of requested replicas
     * 		  for the Kubernetes deployment for the {@link MicoService}.
     */
    private void scale(MicoServiceDeploymentInfo serviceDeploymentInfo, int scaleToNumberOfReplicas) {
    	if (scaleToNumberOfReplicas < 0) {
    		log.warn("Illegal number of requested replicas, no scaling action will be performed.");
    		throw new IllegalArgumentException("Number of replicas must never be negative!");
    	} else if (scaleToNumberOfReplicas == 0) {
    		log.debug("Number of requested replicas is 0, service will be undeployed.");
    		undeploy(serviceDeploymentInfo);
    	} else {
    		log.debug("Scale in/out deployment of MicoService '{}' in version '{}' to {} replica(s) (namespace: '{}', deployment: '{}').",
				serviceDeploymentInfo.getService().getShortName(), serviceDeploymentInfo.getService().getVersion(),
				scaleToNumberOfReplicas,
                serviceDeploymentInfo.getKubernetesDeploymentInfo().getNamespace(),
                serviceDeploymentInfo.getKubernetesDeploymentInfo().getDeploymentName());
            kubernetesClient
                .apps()
                .deployments()
                .inNamespace(serviceDeploymentInfo.getKubernetesDeploymentInfo().getNamespace())
                .withName(serviceDeploymentInfo.getKubernetesDeploymentInfo().getDeploymentName())
                .scale(scaleToNumberOfReplicas);
        }
    }
    
    /**
     * Undeploys a {@link MicoService} by deleting all associated Kubernetes
     * resources: {@link Deployment}, {@link Service}, {@link Build}.
     * 
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     */
    private void undeploy(MicoServiceDeploymentInfo serviceDeploymentInfo) {
    	KubernetesDeploymentInfo kubernetesDeploymentInfo = serviceDeploymentInfo.getKubernetesDeploymentInfo();

        // Delete Kubernetes Deployment
        log.debug("Delete the Kubernetes deployment '{}' of MicoService '{}' in version '{}'.",
            kubernetesDeploymentInfo.getDeploymentName(), serviceDeploymentInfo.getService().getShortName(),
            serviceDeploymentInfo.getService().getVersion());
    	kubernetesClient
    		.apps()
    		.deployments()
    		.inNamespace(kubernetesDeploymentInfo.getNamespace())
    		.withName(kubernetesDeploymentInfo.getDeploymentName())
            .delete();

        // Delete Kubernetes Services
        for (String kubernetesServiceName : kubernetesDeploymentInfo.getServiceNames()) {
            log.debug("Delete the Kubernetes service '{}' of MicoService '{}' in version '{}'.",
                kubernetesServiceName, serviceDeploymentInfo.getService().getShortName(),
                serviceDeploymentInfo.getService().getVersion());
            kubernetesClient
                .services()
                .inNamespace(kubernetesDeploymentInfo.getNamespace())
                .withName(kubernetesServiceName)
                .delete();
        }

        if(buildBotConfig.isBuildCleanUpByUndeploy()) {
            // Clean up Kubernetes Build resources
            log.debug("Clean up build resources for MicoService '{}' in version '{}'.",
                serviceDeploymentInfo.getService().getShortName(), serviceDeploymentInfo.getService().getVersion());
            imageBuilder.deleteBuild(serviceDeploymentInfo.getService());
            kubernetesClient
                .pods()
                .inNamespace(buildBotConfig.getNamespaceBuildExecution())
                .withLabel(ImageBuilder.BUILD_CRD_GROUP + "/buildName", imageBuilder.createBuildName(serviceDeploymentInfo.getService()))
                .delete();
        }

    	// Delete Kubernetes deployment info in database
		log.debug("Delete Kubernetes deployment info in database for MicoService '{}' in version '{}'.",
			serviceDeploymentInfo.getService().getShortName(), serviceDeploymentInfo.getService().getVersion());
		kubernetesDeploymentInfoRepository.delete(serviceDeploymentInfo.getKubernetesDeploymentInfo());
    }
    
    /**
     * Retrieves the service deployment information for a given
     * {@code MicoApplication} and {@code MicoService} from the database.
     * 
     * @param application the {@link MicoApplication}.
     * @param service the {@link MicoService}.
     * @return the {@link MicoServiceDeploymentInfo} if it exists.
     * @throws IllegalStateException if the {@code MicoServiceDeploymentInfo}
     * 		   does not exist in the database.
     */
    private MicoServiceDeploymentInfo getServiceDeploymentInfo(MicoApplication application, MicoService service) throws IllegalStateException {
    	Optional<MicoServiceDeploymentInfo> serviceDeploymentInfoOptional =
    		serviceDeploymentInfoRepository.findByApplicationAndService(
				application.getShortName(), application.getVersion(),
				service.getShortName(), service.getVersion());
    	
    	if (!serviceDeploymentInfoOptional.isPresent()) {
    		log.error("Deployment info for MicoApplication '{}' in version '{}' and "
    			+ "MicoService '{}' in version '{}' is not available in the database.",
			    application.getShortName(), application.getVersion(), service.getShortName(), service.getVersion());
    		throw new IllegalStateException("Service deployment info for MicoApplication '" 
				+ application.getShortName() + "' '" + application.getVersion()
				+ "' and MicoService '" + service.getShortName() + "' '" + service.getVersion() + "' does not exist!");
    	}
    	
    	return serviceDeploymentInfoOptional.get();
    }
    
}
