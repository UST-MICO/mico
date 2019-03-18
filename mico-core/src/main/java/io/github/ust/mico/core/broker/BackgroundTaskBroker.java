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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.github.ust.mico.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import io.github.ust.mico.core.model.MicoServiceBackgroundTask.Status;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoBackgroundTaskRepository;

/**
 * Broker to operate with jobs.
 */
@Slf4j
@Service
public class BackgroundTaskBroker {
	
    private final MicoBackgroundTaskRepository jobRepository;

    private final MicoApplicationRepository applicationRepository;

    @Autowired
    public BackgroundTaskBroker(MicoBackgroundTaskRepository jobRepository, MicoApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

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
     * Save a job to the database.
     *
     * @param job the {@link MicoServiceBackgroundTask}
     * @return the saved {@link MicoServiceBackgroundTask}
     */
    public MicoServiceBackgroundTask saveJob(MicoServiceBackgroundTask job) {
        return jobRepository.save(job);
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
     * Return a {@code MicoServiceBackgroundTask} for a given {@code MicoService} and {@code MicoServiceBackgroundTask.Type}.
     *
     * @param micoServiceShortName the short name of a {@link MicoService}
     * @param micoServiceVersion the version of a {@link MicoService}
     * @param type the {@link MicoServiceBackgroundTask.Type}
     * @return the optional task. Is empty if no task exist for the given {@link MicoService}
     */
    public Optional<MicoServiceBackgroundTask> getTaskByMicoService(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundTask.Type type) {
        return jobRepository.findByServiceShortNameAndServiceVersionAndType(micoServiceShortName, micoServiceVersion, type);
    }

    /**
     * Saves a job of a task to the database.
     *
     * @param micoServiceShortName the short name of a {@link MicoService}
     * @param micoServiceVersion the version of a {@link MicoService}
     * @param job the job as a {@link CompletableFuture}
     * @param type the {@link MicoServiceBackgroundTask.Type}
     */
    public void saveJobOfTask(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundTask.Type type,
                              CompletableFuture<?> job) {
        Optional<MicoServiceBackgroundTask> taskOptional = getTaskByMicoService(micoServiceShortName, micoServiceVersion, type);
        if (taskOptional.isPresent()) {
            MicoServiceBackgroundTask task = taskOptional.get();
            task.setJob(job);
            saveJob(task);
            log.debug("Saved new job of task with type '{}' for '{}' '{}' .", type, micoServiceShortName, micoServiceVersion);
        } else {
            log.warn("No task of type '{}' exists for '{}' '{}'.", type, micoServiceShortName, micoServiceVersion);
        }
    }

    /**
     * Saves a new status of a task to the database.
     *
     * @param micoServiceShortName the short name of a {@link MicoService}
     * @param micoServiceVersion the version of a {@link MicoService}
     * @param type the {@link MicoServiceBackgroundTask.Type}
     * @param newStatus the new {@link MicoServiceBackgroundTask.Status}
     */
    public void saveNewStatus(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundTask.Type type,
                              MicoServiceBackgroundTask.Status newStatus) {
        saveNewStatus(micoServiceShortName, micoServiceVersion, type, newStatus, null);
    }

    /**
     * Saves a new status of a task to the database.
     *
     * @param micoServiceShortName the short name of a {@link MicoService}
     * @param micoServiceVersion the version of a {@link MicoService}
     * @param type the {@link MicoServiceBackgroundTask.Type}
     * @param newStatus the new {@link MicoServiceBackgroundTask.Status}
     * @param errorMessage the optional error message if the job has failed
     */
    public void saveNewStatus(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundTask.Type type,
                              MicoServiceBackgroundTask.Status newStatus, @Nullable String errorMessage) {
        Optional<MicoServiceBackgroundTask> taskOptional = getTaskByMicoService(micoServiceShortName, micoServiceVersion, type);
        if (taskOptional.isPresent()) {
            MicoServiceBackgroundTask task = taskOptional.get();
            if(!task.getStatus().equals(newStatus)) {
                log.info("Task of '{}' '{}' with type '{}' changed its status: {} → {}.",
                    micoServiceShortName, micoServiceVersion, type, task.getStatus(), newStatus);
                if(newStatus.equals(MicoServiceBackgroundTask.Status.ERROR) && !StringUtils.isEmpty(errorMessage)) {
                    log.info("Task of '{}' '{}' with type '{}' failed. Reason: {}.",
                        micoServiceShortName, micoServiceVersion, type, errorMessage);
                }
                task.setStatus(newStatus);
                task.setErrorMessage(errorMessage);
                MicoServiceBackgroundTask savedTask = saveJob(task);
                log.debug("Saved new status of task: {}", savedTask);
            }
        } else {
            log.warn("No task of type '{}' exists for '{}' '{}'.", type, micoServiceShortName, micoServiceVersion);
        }
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
    private Status checkStatus(List<Status> statusList) {
        if (statusList.contains(Status.ERROR)) {
            return Status.ERROR;
        } else if (statusList.contains(Status.PENDING)) {
            return Status.PENDING;
        } else if (statusList.contains(Status.RUNNING)) {
            return Status.RUNNING;
        } else if (statusList.contains(Status.DONE)) {
            return Status.DONE;
        }
        return Status.UNDEFINED;
    }

}
