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

package io.github.ust.mico.core.util;

import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo.ImagePullPolicy;

import java.util.List;
import java.util.stream.Collectors;

public class MicoRepositoryTestUtils {

    public static MicoApplication matchMicoApplication(List<MicoApplication> micoApplications, MicoApplication micoApplication) {
        return micoApplications
            .stream()
            .filter(a -> a.getShortName().equals(micoApplication.getShortName())
                && a.getVersion().equals(micoApplication.getVersion()))
            .collect(Collectors.toList())
            .get(0);
    }

    public static MicoService matchMicoService(List<MicoService> micoServices, MicoService micoService) {
        return micoServices
            .stream()
            .filter(s -> s.getShortName().equals(micoService.getShortName())
                && s.getVersion().equals(micoService.getVersion()))
            .collect(Collectors.toList())
            .get(0);
    }

    public static MicoServiceDeploymentInfo matchMicoServiceDeploymentInfoByService(List<MicoServiceDeploymentInfo> deploymentInfos, MicoService micoService) {
        return deploymentInfos
            .stream()
            .filter(sdi -> sdi.getService().getShortName().equals(micoService.getShortName())
                && sdi.getService().getVersion().equals(micoService.getVersion()))
            .collect(Collectors.toList())
            .get(0);
    }

    public static MicoServiceInterface matchMicoServiceInterface(List<MicoServiceInterface> serviceInterfaces, String serviceInterfaceName) {
        return serviceInterfaces
            .stream()
            .filter(serviceInterface -> serviceInterface.getServiceInterfaceName().equals(serviceInterfaceName))
            .collect(Collectors.toList())
            .get(0);
    }

    public static MicoApplication getMicoApplication(int numberOfMicoServices) {
        MicoApplication micoApplication = getPureMicoApplication(0);

        for (int i = 0; i < numberOfMicoServices; i++) {
            MicoService micoService = getPureMicoService(i);
            MicoServiceDeploymentInfo micoServiceDeploymentInfo = getMicoServiceDeploymentInfo(i, micoService);
            micoApplication.getServices().add(micoService);
            micoApplication.getServiceDeploymentInfos().add(micoServiceDeploymentInfo);
        }

        return micoApplication;
    }

    public static MicoApplication addMicoServicesWithDefaultServiceDeploymentInfo(MicoApplication micoApplication, MicoService... micoServices) {
        for (MicoService micoService : micoServices) {
            micoApplication.getServices().add(micoService);
            micoApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
                .setService(micoService)
                .setInstanceId(UIDUtils.uidFor(micoService)));
        }

        return micoApplication;
    }

    public static MicoApplication addMicoServicesWithServiceDeploymentInfo(MicoApplication micoApplication, MicoService... micoServices) {
        for (int i = 0; i < micoServices.length; i++) {
            micoApplication.getServices().add(micoServices[i]);
            micoApplication.getServiceDeploymentInfos().add(getMicoServiceDeploymentInfo(i, micoServices[i]));
        }

        return micoApplication;
    }

    public static MicoApplication getPureMicoApplication(int number) {
        return new MicoApplication()
            .setShortName(getMicoApplicationShortName(number))
            .setVersion(getMicoApplicationVersion(number))
            .setName(getMicoApplicationName(number));
    }

    public static MicoService getPureMicoService(int number) {
        return new MicoService()
            .setShortName(getMicoServiceShortName(number))
            .setVersion(getMicoServiceVersion(number))
            .setName(getMicoServiceName(number));
    }

    public static MicoServiceDeploymentInfo getMicoServiceDeploymentInfo(int number, MicoService micoService) {
        return new MicoServiceDeploymentInfo()
            .setService(micoService)
            .setInstanceId(UIDUtils.uidFor(micoService))
            .setReplicas(1)
            .setImagePullPolicy(ImagePullPolicy.ALWAYS)
            .setKubernetesDeploymentInfo(getMicoServiceDeploymentInfoKubernetesDeploymentInfo(number))
            .setLabels(CollectionUtils.listOf(getMicoServiceDeploymentInfoLabel(number)))
            .setEnvironmentVariables(CollectionUtils.listOf(getMicoServiceDeploymentInfoEnvironmentVariable(number)))
            .setInterfaceConnections(CollectionUtils.listOf(getMicoServiceDeploymentInfoInterfaceConection(number)));
    }

    public static MicoService getMicoService(int number) {
        return getPureMicoService(number)
            .setServiceInterfaces(
                CollectionUtils.listOf(getMicoServiceInterface(number),
                    getMicoServiceInterface(number + 1)));
    }

    public static MicoServiceDependency getMicoServiceDependency(MicoService micoService, MicoService dependendMicoService) {
        return new MicoServiceDependency()
            .setService(micoService)
            .setDependedService(dependendMicoService);
    }

    public static MicoLabel getMicoServiceDeploymentInfoLabel(int number) {
        return new MicoLabel().setKey("key-sdi-label-" + number).setValue("value-sdi-label-" + number);
    }

    public static MicoEnvironmentVariable getMicoServiceDeploymentInfoEnvironmentVariable(int number) {
        return new MicoEnvironmentVariable().setName("key-env-label-" + number).setValue("value-env-label-" + number);
    }

    public static MicoTopic getMicoServiceDeploymentInfoTopic(String name) {
        return new MicoTopic().setName(name);
    }

    public static MicoTopicRole getMicoServiceDeploymentInfoTopicRole(MicoTopic topic, MicoServiceDeploymentInfo sdi, MicoTopicRole.Role role) {
        return new MicoTopicRole()
            .setServiceDeploymentInfo(sdi)
            .setRole(role)
            .setTopic(topic);
    }

    public static KubernetesDeploymentInfo getMicoServiceDeploymentInfoKubernetesDeploymentInfo(int number) {
        return new KubernetesDeploymentInfo()
            .setDeploymentName("kdi-deployment-name-" + number)
            .setNamespace("kdi-namespace-" + number)
            .setServiceNames(CollectionUtils.listOf("kdi-service-name-" + number + ".a", "kdi-service-name-" + number + ".b"));
    }

    public static MicoInterfaceConnection getMicoServiceDeploymentInfoInterfaceConection(int number) {
        return new MicoInterfaceConnection()
            .setEnvironmentVariableName("ic-env-name-" + number)
            .setMicoServiceInterfaceName("ic-service-interface-name-" + number)
            .setMicoServiceShortName("ic-service-short-name-" + number);
    }

    private static MicoServiceInterface getMicoServiceInterface(int number) {
        return new MicoServiceInterface()
            .setServiceInterfaceName("service-interface-name-" + number)
            .setPorts(CollectionUtils.listOf(getMicoServicePort(number), getMicoServicePort(number + 1)));
    }

    private static MicoServicePort getMicoServicePort(int number) {
        return new MicoServicePort()
            .setPort(Integer.parseInt("800" + number))
            .setType(number % 2 == 0 ? MicoPortType.TCP : MicoPortType.UDP)
            .setTargetPort(Integer.parseInt("900" + number));
    }

    private static String getMicoApplicationShortName(int number) {
        return "application-" + number;
    }

    private static String getMicoApplicationVersion(int number) {
        return "application-v1." + number + ".0";
    }

    private static String getMicoApplicationName(int number) {
        return "application-name-" + number;
    }

    private static String getMicoServiceShortName(int number) {
        return "service-" + number;
    }

    private static String getMicoServiceVersion(int number) {
        return "service-v1." + number + ".0";
    }

    private static String getMicoServiceName(int number) {
        return "service-name-" + number;
    }

}
