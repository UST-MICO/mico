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

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoApplicationJobStatus;
import io.github.ust.mico.core.model.MicoBackgroundTask;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoBackgroundTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Broker to find and delete jobs.
 */
@Slf4j
@Service
public class BackgroundTaskBroker {
    @Autowired
    private MicoBackgroundTaskRepository jobRepository;

    @Autowired
    private MicoApplicationRepository applicationRepository;

    /**
     * Retrieves all jobs saved in database
     *
     * @return list of {@link MicoBackgroundTask}
     */
    public List<MicoBackgroundTask> getAllJobs() {
        return jobRepository.findAll();
    }

    /**
     * retrieves {@link MicoBackgroundTask} by id
     *
     * @param id of task
     * @return the {@link MicoBackgroundTask}
     */
    public Optional<MicoBackgroundTask> getJobById(String id) {
        return jobRepository.findById(id);
    }

    /**
     * deletes {@link MicoBackgroundTask} from database
     *
     * @param id
     * @return
     */
    public Optional<MicoBackgroundTask> deleteJob(String id) {
        Optional<MicoBackgroundTask> job = getJobById(id);
        jobRepository.deleteById(id);
        return job;
    }

    /**
     * retrieves the job status of a {@link MicoApplication}
     *
     * @param shortName of {@link MicoApplication}
     * @param version   of {@link MicoApplication}
     * @return {@link MicoApplicationJobStatus} with status and jobs
     */
    public MicoApplicationJobStatus getJobStatusByApplicationShortNameAndVersion(String shortName, String version) {
        Optional<MicoApplication> existingApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!existingApplicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }
        MicoApplication micoApplication = existingApplicationOptional.get();

        List<MicoBackgroundTask> jobList = new ArrayList<>();
        for (MicoServiceDeploymentInfo deploymentInfo : micoApplication.getServiceDeploymentInfos()) {
            jobList.addAll(jobRepository.findByMicoServiceShortNameAndMicoServiceVersion(deploymentInfo.getService().getShortName(), deploymentInfo.getService().getVersion()));
        }

        List<MicoBackgroundTask.Status> statusList = jobList.stream().map(MicoBackgroundTask::getStatus).distinct().collect(Collectors.toList());

        return new MicoApplicationJobStatus(shortName, version, checkStatus(statusList), jobList);
    }

    /**
     * Return status in the following order
     * error -> pending -> running -> done
     */
    private MicoBackgroundTask.Status checkStatus(List<MicoBackgroundTask.Status> statusList) {
        if (statusList.contains(MicoBackgroundTask.Status.ERROR)) {
            return MicoBackgroundTask.Status.ERROR;
        } else if (statusList.contains(MicoBackgroundTask.Status.PENDING)) {
            return MicoBackgroundTask.Status.PENDING;
        } else if (statusList.contains(MicoBackgroundTask.Status.RUNNING)) {
            return MicoBackgroundTask.Status.RUNNING;
        } else if (statusList.contains(MicoBackgroundTask.Status.DONE)) {
            return MicoBackgroundTask.Status.DONE;
        }
        return MicoBackgroundTask.Status.ERROR;
    }
}
