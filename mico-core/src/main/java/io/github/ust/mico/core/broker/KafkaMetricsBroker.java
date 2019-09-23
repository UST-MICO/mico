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

package io.github.ust.mico.core.broker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.*;
import java.io.IOException;

@Slf4j
@Service
public class KafkaMetricsBroker {

    public static final String ATTRIBUTE_NAME_TOTAL_MESSAGES_COUNT = "Count";
    public static final String ATTRIBUTE_NAME_PER_MINUTE_RATE_SINCE_START = "OneMinuteRate";
    protected static final String TOPIC_NAME_PLACEHOLDER = "<TOPIC_NAME>";
    protected static final String OBJECT_NAME_TOPIC_METRICS = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec,topic=" + TOPIC_NAME_PLACEHOLDER;


    @Autowired
    MBeanServerConnection jmxConnector;

    /**
     * Gets the specified attribute for the topic.
     *
     * @param topicName
     * @param attributeName
     * @return
     * @throws IOException
     * @throws MalformedObjectNameException
     * @throws AttributeNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws InstanceNotFoundException
     */
    public Object getJmxAttribute(String topicName, String attributeName) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        Object returnValue = jmxConnector.getAttribute(new ObjectName(OBJECT_NAME_TOPIC_METRICS.replace(TOPIC_NAME_PLACEHOLDER, topicName)), attributeName);
        log.debug("For the topic '{}', the attribute '{}' has the value '{}'", topicName, attributeName, returnValue);
        return returnValue;
    }

    /**
     * Returns the total message count for this topic
     *
     * @param topicName
     * @return
     * @throws MalformedObjectNameException
     * @throws InstanceNotFoundException
     * @throws IOException
     * @throws ReflectionException
     * @throws AttributeNotFoundException
     * @throws MBeanException
     */
    public long getTotalMessageCount(String topicName) throws MalformedObjectNameException, InstanceNotFoundException, IOException, ReflectionException, AttributeNotFoundException, MBeanException {
        return (Long) this.getJmxAttribute(topicName, ATTRIBUTE_NAME_TOTAL_MESSAGES_COUNT);
    }

    /**
     * Returns the per minute average since start
     *
     * @param topicName
     * @return
     * @throws MalformedObjectNameException
     * @throws InstanceNotFoundException
     * @throws IOException
     * @throws ReflectionException
     * @throws AttributeNotFoundException
     * @throws MBeanException
     */
    public double getMessagesPerMinuteSinceStart(String topicName) throws MalformedObjectNameException, InstanceNotFoundException, IOException, ReflectionException, AttributeNotFoundException, MBeanException {
        return (Double) this.getJmxAttribute(topicName, ATTRIBUTE_NAME_PER_MINUTE_RATE_SINCE_START);
    }
}
