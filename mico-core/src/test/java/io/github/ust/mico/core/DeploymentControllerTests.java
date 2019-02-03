package io.github.ust.mico.core;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.REST.DeploymentController;
import io.github.ust.mico.core.concurrency.MicoCoreBackgroundTaskFactory;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.mapping.MicoKubernetesClient;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.ust.mico.core.TestConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(DeploymentController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
public class DeploymentControllerTests {

    private static final String BASE_PATH = "/applications";

    @Captor
    ArgumentCaptor<Consumer<String>> onSuccessArgumentCaptor;
    @Captor
    ArgumentCaptor<Function<Throwable, Void>> onErrorArgumentCaptor;
    @Captor
    ArgumentCaptor<MicoService> micoServiceArgumentCaptor;
    @Captor
    ArgumentCaptor<MicoServiceInterface> micoServiceInterfaceArgumentCaptor;
    @Captor
    ArgumentCaptor<MicoServiceDeploymentInfo> deploymentInfoArgumentCaptor;

    @Autowired
    private MockMvc mvc;
    @MockBean
    private MicoApplicationRepository applicationRepository;
    @MockBean
    private MicoServiceRepository serviceRepository;
    @MockBean
    private ImageBuilder imageBuilder;
    @MockBean
    private MicoCoreBackgroundTaskFactory factory;
    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @Before
    public void setUp() {
        given(imageBuilder.createImageName(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).willReturn(DOCKER_IMAGE_URI);

        Deployment deployment = new Deployment();
        given(micoKubernetesClient.createMicoService(
            ArgumentMatchers.any(MicoService.class),
            ArgumentMatchers.any(MicoServiceDeploymentInfo.class)))
            .willReturn(deployment);
    }

    @Test
    public void deployApplicationWithOneServiceAndOneServiceInterface() throws Exception {
        MicoService service = getTestService();
        MicoApplication application = getTestApplication(service);

        service.setDockerImageUri(DOCKER_IMAGE_URI);

        given(applicationRepository.findByShortNameAndVersion(TestConstants.SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        given(factory.runAsync(ArgumentMatchers.any(), onSuccessArgumentCaptor.capture(), onErrorArgumentCaptor.capture()))
            .willReturn(CompletableFuture.completedFuture(service));

        mvc.perform(post(BASE_PATH + "/" + TestConstants.SHORT_NAME + "/" + VERSION + "/deploy"))
            .andDo(print())
            .andExpect(status().isOk());

        // Assume asynchronous image build operation was successful -> invoke onSuccess function
        onSuccessArgumentCaptor.getValue().accept(DOCKER_IMAGE_URI);

        verify(serviceRepository, times(1)).save(micoServiceArgumentCaptor.capture());

        MicoService storedMicoService = micoServiceArgumentCaptor.getValue();
        assertNotNull(storedMicoService);
        assertNotNull("DockerImageUri was not set", storedMicoService.getDockerImageUri());
        assertEquals(DOCKER_IMAGE_URI, storedMicoService.getDockerImageUri());

        verify(micoKubernetesClient, times(1)).createMicoService(
            micoServiceArgumentCaptor.capture(),
            deploymentInfoArgumentCaptor.capture());

        MicoService micoServiceToCreate = micoServiceArgumentCaptor.getValue();
        assertNotNull(micoServiceToCreate);
        assertEquals("MicoService that will be created as Kubernetes resources does not match", service, micoServiceToCreate);

        MicoServiceDeploymentInfo deploymentInfo = deploymentInfoArgumentCaptor.getValue();
        assertNotNull(deploymentInfo);
        int actualReplicas = deploymentInfo.getReplicas();
        int expectedReplicas = application.getDeploymentInfo().getServiceDeploymentInfos().get(service.getId()).getReplicas();
        assertEquals("Replicas does not match the definition in the deployment info", expectedReplicas, actualReplicas);

        verify(micoKubernetesClient, times(1)).createMicoServiceInterface(
            micoServiceInterfaceArgumentCaptor.capture(),
            micoServiceArgumentCaptor.capture());

        MicoServiceInterface micoServiceInterfaceToCreate = micoServiceInterfaceArgumentCaptor.getValue();
        assertNotNull(micoServiceInterfaceToCreate);
        assertEquals("MicoServiceInterface that will be created as Kubernetes resources does not match",
            service.getServiceInterfaces().get(0), micoServiceInterfaceToCreate);

        MicoService micoServiceThatIsUsedForInterfaceCreation = micoServiceArgumentCaptor.getValue();
        assertNotNull(micoServiceThatIsUsedForInterfaceCreation);
        assertEquals("MicoService that will be used to create a MicoServiceInterface does not match",
            service, micoServiceThatIsUsedForInterfaceCreation);
    }

    private MicoApplication getTestApplication(MicoService service) {
        MicoApplication application = new MicoApplication()
            .setShortName(TestConstants.SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);
        application.getDeploymentInfo().getServiceDeploymentInfos().put(ID, new MicoServiceDeploymentInfo()
            .setReplicas(1));
        application.getServices().add(service);
        return application;
    }

    private MicoService getTestService() {
        return new MicoService()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(RELEASE)
            .setGitCloneUrl(GIT_TEST_REPO_URL)
            .setDockerfilePath(DOCKERFILE)
            .setServiceInterfaces(CollectionUtils.listOf(
                new MicoServiceInterface()
                    .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
                    .setPorts(CollectionUtils.listOf(
                        new MicoServicePort()
                            .setNumber(80)
                            .setTargetPort(80)
                    ))
            ));
    }
}
