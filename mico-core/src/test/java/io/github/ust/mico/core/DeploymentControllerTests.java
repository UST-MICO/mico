package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.REST.DeploymentController;
import io.github.ust.mico.core.concurrency.MicoCoreBackgroundTaskFactory;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.buildtypes.Build;
import io.github.ust.mico.core.mapping.MicoKubernetesClient;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoApplicationDeploymentInfo;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static io.github.ust.mico.core.JsonPathBuilder.*;
import static io.github.ust.mico.core.TestConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(DeploymentController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
public class DeploymentControllerTests {

    public static final String APPLICATION_LIST = buildPath(EMBEDDED, "micoApplicationList");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    public static final String VERSION_PATH = buildPath(ROOT, "version");
    public static final String DOCKER_IMAGE_URI_PATH = buildPath(ROOT, "dockerImageUri");
    public static final String DESCRIPTION_PATH = buildPath(ROOT, "description");
    public static final String ID_PATH = buildPath(ROOT, "id");
    public static final String VERSION_MAJOR_PATH = buildPath(ROOT, "version", "majorVersion");
    public static final String VERSION_MINOR_PATH = buildPath(ROOT, "version", "minorVersion");
    public static final String VERSION_PATCH_PATH = buildPath(ROOT, "version", "patchVersion");
    private static final String JSON_PATH_LINKS_SECTION = "$._links.";
    private static final String SELF_HREF = "self.href";
    private static final String BASE_PATH = "/applications";

    // TODO Rename
    private static final String SERVICE_NAME = "hello"; // is used for image name
    private static final String DOCKER_IMAGE_URI = "ustmico/" + SERVICE_NAME + ":" + RELEASE;

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

    @Autowired
    private ObjectMapper mapper;

    @Before
    public void setUp() throws NotInitializedException {
        MicoService service = getTestService();
        Build build = Build.builder().build();
        given(imageBuilder.build(service)).willReturn(build);
        Deployment deployment = new Deployment();
        given(micoKubernetesClient.createMicoService(service, SHORT_NAME, 1)).willReturn(deployment);
    }

    @Test
    public void deployApplicationWithOneService() throws Exception {
        MicoService service = getTestService();
        MicoApplication application = getTestApplication(service);

        MicoService serviceWithImage = service.toBuilder().dockerImageUri(DOCKER_IMAGE_URI).build();
        MicoApplication applicationWithImage = application.toBuilder()
            .service(serviceWithImage)
            .build();

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(serviceRepository.save(any(MicoService.class))).willReturn(serviceWithImage);

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/deploy"))
            //.content(mapper.writeValueAsBytes(application))
            //.contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        verify(serviceRepository).save(argThat((storedService) -> storedService.getDockerfilePath().equals(DOCKER_IMAGE_URI)));
    }

    private MicoApplication getTestApplication(MicoService service) {
        return MicoApplication.builder()
            .shortName(SHORT_NAME)
            .version(VERSION)
            .description(DESCRIPTION)
            .deploymentInfo(MicoApplicationDeploymentInfo.builder()
                .serviceDeploymentInfo(ID, MicoServiceDeploymentInfo.builder()
                    .replicas(1)
                    .build())
                .build())
            .service(service)
            .build();
    }

    private MicoService getTestService() {
        return MicoService.builder()
            .id(ID)
            .shortName(SERVICE_NAME)
            .version(RELEASE)
            .vcsRoot(GIT_TEST_REPO_URL)
            .dockerfilePath(DOCKERFILE)
            .build();
    }
}
