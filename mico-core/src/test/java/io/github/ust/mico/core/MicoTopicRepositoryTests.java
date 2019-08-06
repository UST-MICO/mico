package io.github.ust.mico.core;


import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoLabel;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoTopic;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static io.github.ust.mico.core.util.MicoRepositoryTestUtils.*;
import static io.github.ust.mico.core.util.MicoRepositoryTestUtils.getMicoServiceDeploymentInfoLabel;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MicoTopicRepositoryTests extends MicoRepositoryTests {


    @Before
    public void setUp() {
        deleteAllData();
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

        // Setup some labels
        MicoTopic l0 = getMicoServiceDeploymentInfoTopic("topic0");
        MicoTopic l1 = getMicoServiceDeploymentInfoTopic("topic1");
        MicoTopic l2 = getMicoServiceDeploymentInfoTopic("topic2");

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        topicRepository.save(l0);
        topicRepository.save(l1);
        topicRepository.save(l2);

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
