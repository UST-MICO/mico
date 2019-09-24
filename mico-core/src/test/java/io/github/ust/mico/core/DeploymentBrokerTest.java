package io.github.ust.mico.core;

import io.github.ust.mico.core.broker.DeploymentBroker;
import io.github.ust.mico.core.exception.DeploymentRequirementsOfKafkaFaasConnectorNotMetException;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoTopic;
import io.github.ust.mico.core.model.MicoTopicRole;
import io.github.ust.mico.core.model.OpenFaaSFunction;
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
    public void checkIfKafkaFaasConnectorIsDeployable() throws DeploymentRequirementsOfKafkaFaasConnectorNotMetException {
        MicoServiceDeploymentInfo micoServiceDeploymentInfo = new MicoServiceDeploymentInfo().setTopics(Arrays.asList(
            new MicoTopicRole().setRole(MicoTopicRole.Role.INPUT).setTopic(new MicoTopic().setName("inputTopic")),
            new MicoTopicRole().setRole(MicoTopicRole.Role.OUTPUT).setTopic(new MicoTopic().setName("outputTopic"))));
        deploymentBroker.checkIfKafkaFaasConnectorIsDeployable(micoServiceDeploymentInfo);

        micoServiceDeploymentInfo = new MicoServiceDeploymentInfo().setTopics(Arrays.asList(
            new MicoTopicRole().setRole(MicoTopicRole.Role.INPUT).setTopic(new MicoTopic().setName("inputTopic"))))
            .setOpenFaaSFunction(new OpenFaaSFunction().setName("faas-function"));
        deploymentBroker.checkIfKafkaFaasConnectorIsDeployable(micoServiceDeploymentInfo);
    }
}
