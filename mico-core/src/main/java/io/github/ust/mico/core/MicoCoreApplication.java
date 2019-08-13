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

package io.github.ust.mico.core;

import io.github.ust.mico.core.broker.MicoServiceBroker;
import io.github.ust.mico.core.configuration.KafkaConfig;
import io.github.ust.mico.core.exception.MicoServiceAlreadyExistsException;
import io.github.ust.mico.core.exception.VersionNotSupportedException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoVersion;
import io.github.ust.mico.core.persistence.MicoBackgroundJobRepository;
import io.github.ust.mico.core.service.GitHubCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Entry point for the MICO core application.
 */
@Slf4j
@SpringBootApplication
@EnableNeo4jRepositories(basePackages = "io.github.ust.mico.core.persistence",
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MicoBackgroundJobRepository.class))
@EnableRedisRepositories(basePackages = "io.github.ust.mico.core.persistence",
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MicoBackgroundJobRepository.class))
@EnableScheduling
public class MicoCoreApplication implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    GitHubCrawler gitHubCrawler;
    @Autowired
    MicoServiceBroker micoServiceBroker;
    @Autowired
    KafkaConfig kafkaConfig;

    public static void main(String[] args) {
        SpringApplication.run(MicoCoreApplication.class, args);
    }

    /**
     * @param builder
     * @return
     * @see <a href="https://gist.github.com/RealDeanZhao/38821bc1efeb7e2a9bcd554cc06cdf96">RealDeanZhao/autowire-resttemplate.md</a>
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    public void onApplicationEvent(final ApplicationReadyEvent event) {
        try {
            MicoService kafkaFaasConnector = gitHubCrawler.crawlGitHubRepoLatestRelease(kafkaConfig.getKafkaFaasConnectorUrl());
            List<MicoService> micoServices = micoServiceBroker.getAllVersionsOfServiceFromDatabase(kafkaFaasConnector.getShortName());
            Optional<MicoVersion> highestKafkaFaasConnectorVersion = micoServices.stream().map(micoService -> {
                try {
                    return micoService.getMicoVersion();
                } catch (VersionNotSupportedException e) {
                    e.printStackTrace();
                }
                return null;
            }).max(MicoVersion::compareTo);
            if (kafkaFaasConnector.getMicoVersion().greaterThan(highestKafkaFaasConnectorVersion.get())) {
                micoServiceBroker.persistService(kafkaFaasConnector);
            }
        } catch (IOException | VersionNotSupportedException e) {
            e.printStackTrace();

        }
        // should not happen
        catch (MicoServiceAlreadyExistsException e) {
            log.info(e.getMessage());
        }
        return;
    }
}
