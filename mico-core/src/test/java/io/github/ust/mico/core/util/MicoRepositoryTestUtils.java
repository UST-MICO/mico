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

import java.util.List;
import java.util.stream.Collectors;

import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo.ImagePullPolicy;

public class MicoRepositoryTestUtils {

    // ----------------------
    // -> MicoApplication ---
    // ----------------------

    public static final String APPLICATION_SHORT_NAME_0 = "application-0";
    public static final String APPLICATION_SHORT_NAME_1 = "application-1";
    public static final String APPLICATION_SHORT_NAME_2 = "application-2";
    public static final String APPLICATION_SHORT_NAME_3 = "application-3";

    public static final String APPLICATION_VERSION_0 = "application-v1.0.0";
    public static final String APPLICATION_VERSION_1 = "application-v1.1.0";
    public static final String APPLICATION_VERSION_2 = "application-v1.2.0";
    public static final String APPLICATION_VERSION_3 = "application-v1.3.0";

    public static final String APPLICATION_NAME_0 = "application-name-0";
    public static final String APPLICATION_NAME_1 = "application-name-0";
    public static final String APPLICATION_NAME_2 = "application-name-0";
    public static final String APPLICATION_NAME_3 = "application-name-0";


    // ----------------------
    // -> MicoService ---
    // ----------------------

    public static final String SERVICE_SHORT_NAME_0 = "service-0";
    public static final String SERVICE_SHORT_NAME_1 = "service-1";
    public static final String SERVICE_SHORT_NAME_2 = "service-2";
    public static final String SERVICE_SHORT_NAME_3 = "service-3";

    public static final String SERVICE_VERSION_0 = "service-v1.0.0";
    public static final String SERVICE_VERSION_1 = "service-v1.1.0";
    public static final String SERVICE_VERSION_2 = "service-v1.2.0";
    public static final String SERVICE_VERSION_3 = "service-v1.3.0";

    public static final String SERVICE_NAME_0 = "service-name-0";
    public static final String SERVICE_NAME_1 = "service-name-1";
    public static final String SERVICE_NAME_2 = "service-name-2";
    public static final String SERVICE_NAME_3 = "service-name-3";


    public static final MicoApplication matchMicoApplication(List<MicoApplication> micoApplications, MicoApplication micoApplication) {
        return micoApplications
            .stream()
            .filter(a -> a.getShortName().equals(micoApplication.getShortName())
                && a.getVersion().equals(micoApplication.getVersion()))
            .collect(Collectors.toList())
            .get(0);
    }

    public static final MicoService matchMicoService(List<MicoService> micoServices, MicoService micoService) {
        return micoServices
            .stream()
            .filter(s -> s.getShortName().equals(micoService.getShortName())
                && s.getVersion().equals(micoService.getVersion()))
            .collect(Collectors.toList())
            .get(0);
    }

    public static final MicoServiceDeploymentInfo matchMicoServiceDeploymentInfoByService(List<MicoServiceDeploymentInfo> deploymentInfos, MicoService micoService) {
        return deploymentInfos
            .stream()
            .filter(sdi -> sdi.getService().getShortName().equals(micoService.getShortName())
                && sdi.getService().getVersion().equals(micoService.getVersion()))
            .collect(Collectors.toList())
            .get(0);
    }

    public static final MicoServiceInterface matchMicoServiceInterface(List<MicoServiceInterface> serviceInterfaces, String serviceInterfaceName) {
        return serviceInterfaces
            .stream()
            .filter(serviceInterface -> serviceInterface.getServiceInterfaceName().equals(serviceInterfaceName))
            .collect(Collectors.toList())
            .get(0);
    }

    public static final MicoApplication getMicoApplication(int numberOfMicoServices) {
        MicoApplication micoApplication = getPureMicoApplication(0);

        for (int i = 0; i < numberOfMicoServices; i++) {
            MicoService micoService = getPureMicoService(i);
            MicoServiceDeploymentInfo micoServiceDeploymentInfo = getMicoServiceDeploymentInfo(i, micoService);
            micoApplication.getServices().add(micoService);
            micoApplication.getServiceDeploymentInfos().add(micoServiceDeploymentInfo);
        }

        return micoApplication;
    }

    public static final MicoApplication addMicoServicesWithDefaultServiceDeploymentInfo(MicoApplication micoApplication, MicoService... micoServices) {
        for (MicoService micoService : micoServices) {
            micoApplication.getServices().add(micoService);
            micoApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(micoService));
        }

        return micoApplication;
    }

    public static final MicoApplication addMicoServicesWithServiceDeploymentInfo(MicoApplication micoApplication, MicoService... micoServices) {
        for (int i = 0; i < micoServices.length; i++) {
            micoApplication.getServices().add(micoServices[i]);
            micoApplication.getServiceDeploymentInfos().add(getMicoServiceDeploymentInfo(i, micoServices[i]));
        }

        return micoApplication;
    }

    public static final MicoApplication getPureMicoApplication(int number) {
        return new MicoApplication()
            .setShortName(getMicoApplicationShortName(number))
            .setVersion(getMicoApplicationVersion(number))
            .setName(getMicoApplicationName(number));
    }

    public static final MicoService getPureMicoService(int number) {
        return new MicoService()
            .setShortName(getMicoServiceShortName(number))
            .setVersion(getMicoServiceVersion(number))
            .setName(getMicoServiceName(number));
    }

    public static final MicoServiceDeploymentInfo getMicoServiceDeploymentInfo(int number, MicoService micoService) {
        return new MicoServiceDeploymentInfo()
            .setService(micoService)
            .setReplicas(1)
            .setImagePullPolicy(ImagePullPolicy.ALWAYS)
            .setKubernetesDeploymentInfo(getMicoServiceDeploymentInfoKubernetesDeploymentInfo(number))
            .setLabels(CollectionUtils.listOf(getMicoServiceDeploymentInfoLabel(number)))
            .setEnvironmentVariables(CollectionUtils.listOf(getMicoServiceDeploymentInfoEnvironmentVariable(number)))
            .setInterfaceConnections(CollectionUtils.listOf(getMicoServiceDeploymentInfoInterfaceConection(number)));
    }

    public static final MicoService getMicoService(int number) {
        return getPureMicoService(number)
            .setServiceInterfaces(
                CollectionUtils.listOf(getMicoServiceInterface(number),
                    getMicoServiceInterface(number + 1)));
    }

    public static final MicoServiceDependency getMicoServiceDependency(MicoService micoService, MicoService dependendMicoService) {
        return new MicoServiceDependency()
            .setService(micoService)
            .setDependedService(dependendMicoService);
    }

    public static final MicoLabel getMicoServiceDeploymentInfoLabel(int number) {
        return new MicoLabel().setKey("key-sdi-label-" + number).setValue("value-sdi-label-" + number);
    }

    public static final MicoEnvironmentVariable getMicoServiceDeploymentInfoEnvironmentVariable(int number) {
        return new MicoEnvironmentVariable().setName("key-env-label-" + number).setValue("value-env-label-" + number);
    }

    public static final KubernetesDeploymentInfo getMicoServiceDeploymentInfoKubernetesDeploymentInfo(int number) {
        return new KubernetesDeploymentInfo()
            .setDeploymentName("kdi-deployment-name-" + number)
            .setNamespace("kdi-namespace-" + number)
            .setServiceNames(CollectionUtils.listOf("kdi-service-name-" + number + ".a", "kdi-service-name-" + number + ".b"));
    }

    public static final MicoInterfaceConnection getMicoServiceDeploymentInfoInterfaceConection(int number) {
        return new MicoInterfaceConnection()
            .setEnvironmentVariableName("ic-env-name-" + number)
            .setMicoServiceInterfaceName("ic-service-interface-name-" + number)
            .setMicoServiceShortName("ic-service-short-name-" + number);
    }

    private static final MicoServiceInterface getMicoServiceInterface(int number) {
        return new MicoServiceInterface()
            .setServiceInterfaceName("service-interface-name-" + number)
            .setPorts(CollectionUtils.listOf(getMicoServicePort(number), getMicoServicePort(number + 1)));
    }

    private static final MicoServicePort getMicoServicePort(int number) {
        return new MicoServicePort()
            .setPort(Integer.parseInt("800" + number))
            .setType(number % 2 == 0 ? MicoPortType.TCP : MicoPortType.UDP)
            .setTargetPort(Integer.parseInt("900" + number));
    }

    private static final String getMicoApplicationShortName(int number) {
        return "application-" + number;
    }

    private static final String getMicoApplicationVersion(int number) {
        return "application-v1." + number + ".0";
    }

    private static final String getMicoApplicationName(int number) {
        return "application-name-" + number;
    }

    private static final String getMicoServiceShortName(int number) {
        return "service-" + number;
    }

    private static final String getMicoServiceVersion(int number) {
        return "service-v1." + number + ".0";
    }

    private static final String getMicoServiceName(int number) {
        return "service-name-" + number;
    }

}
