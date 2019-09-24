package io.github.ust.mico.core;

import io.github.ust.mico.core.broker.DeploymentBroker;
import io.github.ust.mico.core.exception.DeploymentRequirementsNotMetException;
import io.github.ust.mico.core.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("unit-testing")
public class DeploymentBrokerTest {

    @Autowired
    DeploymentBroker deploymentBroker;

    @Test
    public void checkIfKafkaEnabledServiceIsDeployable() throws DeploymentRequirementsNotMetException {
        MicoServiceDeploymentInfo micoServiceDeploymentInfo = new MicoServiceDeploymentInfo().setTopics(Arrays.asList(
            new MicoTopicRole().setTopic(new MicoTopic().setName("inputTopic")).setRole(MicoTopicRole.Role.INPUT),
            new MicoTopicRole().setRole(MicoTopicRole.Role.OUTPUT).setTopic(new MicoTopic().setName("outputTopic"))));
        deploymentBroker.checkIfKafkaEnabledServiceIsDeployable(micoServiceDeploymentInfo);

        micoServiceDeploymentInfo = new MicoServiceDeploymentInfo().setTopics(Arrays.asList(
            new MicoTopicRole().setTopic(new MicoTopic().setName("inputTopic")).setRole(MicoTopicRole.Role.INPUT)))
            .setOpenFaaSFunction(new OpenFaaSFunction().setName("faas-function"));
        deploymentBroker.checkIfKafkaEnabledServiceIsDeployable(micoServiceDeploymentInfo);
    }
}
