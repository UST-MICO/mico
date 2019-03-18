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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoApplicationJobStatus;
import io.github.ust.mico.core.model.MicoServiceBackgroundTask;
import io.github.ust.mico.core.model.MicoServiceBackgroundTask.Status;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoBackgroundTaskRepository;

/**
 * Broker to find and delete jobs.
 */
@Service
public class BackgroundTaskBroker {
	
    @Autowired
    private MicoBackgroundTaskRepository jobRepository;

    @Autowired
    private MicoApplicationRepository applicationRepository;

    /**
     * Retrieves all jobs saved in database.
     *
     * @return a {@link List} of {@link MicoServiceBackgroundTask}.
     */
    public List<MicoServiceBackgroundTask> getAllJobs() {
        return jobRepository.findAll();
    }

    /**
     * Retrieves a job by id.
     *
     * @param id the id of the job.
     * @return a {@link MicoServiceBackgroundTask}.
     */
    public Optional<MicoServiceBackgroundTask> getJobById(String id) {
        return jobRepository.findById(id);
    }

    /**
     * Deletes a job in the database.
     *
     * @param id the id of the job.
     */
    public void deleteJob(String id) {
        jobRepository.deleteById(id);
    }

    /**
     * Retrieves the job status of a {@code MicoApplication}.
     *
     * @param shortName the short name of the {@link MicoApplication}.
     * @param version the version of the {@link MicoApplication}.
     * @return the {@link MicoApplicationJobStatus} with the status and jobs.
     */
    public MicoApplicationJobStatus getJobStatusByApplicationShortNameAndVersion(String shortName, String version) {
        Optional<MicoApplication> existingApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!existingApplicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }
        MicoApplication micoApplication = existingApplicationOptional.get();

        List<MicoServiceBackgroundTask> jobList = new ArrayList<>();
        for (MicoServiceDeploymentInfo deploymentInfo : micoApplication.getServiceDeploymentInfos()) {
            jobList.addAll(jobRepository.findByServiceShortNameAndServiceVersion(deploymentInfo.getService().getShortName(), deploymentInfo.getService().getVersion()));
        }

        List<MicoServiceBackgroundTask.Status> statusList = jobList.stream().map(MicoServiceBackgroundTask::getStatus).distinct().collect(Collectors.toList());

        return new MicoApplicationJobStatus(shortName, version, checkStatus(statusList), jobList);
    }

    /**
     * Retrieves the {@code Status} which is most relevant. The order
     * of relevance is as follows:
     * <ol>
     * 	<li>{@link Status#ERROR}</li>
     * 	<li>{@link Status#PENDING}</li>
     * 	<li>{@link Status#RUNNING}</li>
     * 	<li>{@link Status#DONE}</li>
     * </ol>
     * 
     * @param statusList the {@link List} of {@link Status} to check.
     * @return the {@link Status} which is most relevant.
     */
    private MicoServiceBackgroundTask.Status checkStatus(List<MicoServiceBackgroundTask.Status> statusList) {
        if (statusList.contains(MicoServiceBackgroundTask.Status.ERROR)) {
            return MicoServiceBackgroundTask.Status.ERROR;
        } else if (statusList.contains(MicoServiceBackgroundTask.Status.PENDING)) {
            return MicoServiceBackgroundTask.Status.PENDING;
        } else if (statusList.contains(MicoServiceBackgroundTask.Status.RUNNING)) {
            return MicoServiceBackgroundTask.Status.RUNNING;
        } else if (statusList.contains(MicoServiceBackgroundTask.Status.DONE)) {
            return MicoServiceBackgroundTask.Status.DONE;
        }
        return MicoServiceBackgroundTask.Status.ERROR;
    }
    
}
