package io.github.ust.mico.core;


import io.github.ust.mico.core.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.github.ust.mico.core.util.MicoRepositoryTestUtils.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles("local")
public class MicoTopicRepositoryTests extends MicoRepositoryTests {


    @Before
    public void setUp() {
        deleteAllData();
    }

    @Commit
    @Test
    public void createTopics() {
        MicoApplication a0 = getPureMicoApplication(0);
        MicoService s0 = getMicoService(0);
        addMicoServicesWithServiceDeploymentInfo(a0, s0);

        MicoServiceDeploymentInfo sdi0 = a0.getServiceDeploymentInfos().get(0);
        MicoTopic t0 = getMicoServiceDeploymentInfoTopic("topic0");
        MicoTopicRole tr0 = getMicoServiceDeploymentInfoTopicRole(t0, sdi0, MicoTopicRole.Role.INPUT);
        sdi0.getTopics().add(tr0);
        applicationRepository.save(a0);

        // Assert based on application repository
        assertEquals(1, topicRepository.count());
        MicoApplication micoApplication = applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get();
        List<MicoTopicRole> topicRoles = micoApplication.getServiceDeploymentInfos().get(0).getTopics();
        assertEquals(1, topicRoles.size());
        assertEquals(MicoTopicRole.Role.INPUT, topicRoles.get(0).getRole());
        assertEquals(sdi0, topicRoles.get(0).getServiceDeploymentInfo());
        assertEquals(t0.getName(), topicRoles.get(0).getTopic().getName());

        // Check that a clean up don't remove the connection
        topicRepository.cleanUp();
        assertEquals(1, topicRepository.count());
    }

    @Commit
    @Test
    public void removeUnnecessaryTopics() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);
        MicoApplication a2 = getPureMicoApplication(2);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);
        MicoService s2 = getMicoService(2);

        // Application #0 includes services #0 and #1
        // Application #1 only includes the service #1
        // Application #2 only includes the service #2
        addMicoServicesWithServiceDeploymentInfo(a0, s0, s1);
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s2);

        // Setup some topics
        MicoTopic t0 = getMicoServiceDeploymentInfoTopic("topic0");
        MicoTopic t1 = getMicoServiceDeploymentInfoTopic("topic1");
        MicoTopic t2 = getMicoServiceDeploymentInfoTopic("topic2");

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        topicRepository.save(t0);
        topicRepository.save(t1);
        topicRepository.save(t2);

        //  3 (created topics)
        assertEquals(3, topicRepository.count());

        // Remove all topics that do not have any relationship with another node
        topicRepository.cleanUp();
        assertEquals(0, topicRepository.count());
        // Check if all applications did not change
        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
        assertEquals(a1, applicationRepository.findByShortNameAndVersion(a1.getShortName(), a1.getVersion()).get());
        assertEquals(a2, applicationRepository.findByShortNameAndVersion(a2.getShortName(), a2.getVersion()).get());
    }
}
