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
import io.github.ust.mico.core.configuration.KafkaFaasConnectorConfig;
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
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
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
    KafkaFaasConnectorConfig kafkaFaasConnectorConfig;

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

    /**
     * Runs when application is ready.
     *
     * @param event the {@link ApplicationReadyEvent}
     */
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        Environment environment = event.getApplicationContext().getEnvironment();
        if (environment.acceptsProfiles(Profiles.of("unit-testing"))) {
            log.info("Unit testing profile is active. Don't add kafka-faas-connector to database.");
            return;
        }
        addKafkaFaasConnectorToDatabase();
    }

    /**
     * Retrieves the latest KafkaFaasConnector and stores it in the service database.
     */
    private void addKafkaFaasConnectorToDatabase() {
        try {
            MicoService kafkaFaasConnector = gitHubCrawler.crawlGitHubRepoLatestRelease(kafkaFaasConnectorConfig.getGithubUrl());
            kafkaFaasConnector.setKafkaEnabled(true);
            List<MicoService> kafkaFaasConnectors = micoServiceBroker.getAllVersionsOfServiceFromDatabase(kafkaFaasConnector.getShortName());

            // Get the newest currently existing KafkaFaasConnector stored in the database.
            Optional<MicoVersion> highestKafkaFaasConnectorVersionOpt = kafkaFaasConnectors.stream().map(micoService -> {
                try {
                    return micoService.getMicoVersion();
                } catch (VersionNotSupportedException e) {
                    log.debug(e.getMessage());
                }
                return null;
            }).filter(Objects::nonNull).max(MicoVersion::compareTo);

            // If there is no version, add it directly to the database.
            // Otherwise check if the retrieved version is newer than the existing ones
            // and only if that's the case add the new version to the database.
            if (!highestKafkaFaasConnectorVersionOpt.isPresent()) {
                micoServiceBroker.persistService(kafkaFaasConnector);
                log.info("Add first {} in version {} to the database.",
                    kafkaFaasConnector.getShortName(), kafkaFaasConnector.getVersion());
            } else {
                if (kafkaFaasConnector.getMicoVersion().greaterThan(highestKafkaFaasConnectorVersionOpt.get())) {
                    micoServiceBroker.persistService(kafkaFaasConnector);
                    log.info("A new {} in version {} exists -> add it to the database.",
                        kafkaFaasConnector.getShortName(), kafkaFaasConnector.getVersion());
                } else {
                    log.debug("Newest {} is already stored in the database in version {}.",
                        kafkaFaasConnector.getShortName(), kafkaFaasConnector.getVersion());
                }
            }
        } catch (IOException | VersionNotSupportedException | MicoServiceAlreadyExistsException e) {
            log.error("Error while trying to add KafkaFaasConnector to the database. Caused by: {}", e.getMessage(), e);
        }
    }
}
