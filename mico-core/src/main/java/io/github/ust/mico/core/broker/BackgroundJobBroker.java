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

import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.model.MicoServiceBackgroundJob.Status;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoBackgroundJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Broker to operate with jobs.
 */
@Slf4j
@Service
public class BackgroundJobBroker {

    private final MicoBackgroundJobRepository jobRepository;

    private final MicoApplicationRepository applicationRepository;

    @Autowired
    public BackgroundJobBroker(MicoBackgroundJobRepository jobRepository, MicoApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    /**
     * Retrieves all jobs saved in database.
     *
     * @return a {@link List} of {@link MicoServiceBackgroundJob}.
     */
    public List<MicoServiceBackgroundJob> getAllJobs() {
        return jobRepository.findAll();
    }

    /**
     * Retrieves a job by id.
     *
     * @param id the id of the job.
     * @return a {@link MicoServiceBackgroundJob}.
     */
    public Optional<MicoServiceBackgroundJob> getJobById(String id) {
        return jobRepository.findById(id);
    }

    /**
     * Save a job to the database.
     *
     * @param job the {@link MicoServiceBackgroundJob}
     * @return the saved {@link MicoServiceBackgroundJob}
     */
    public MicoServiceBackgroundJob saveJob(MicoServiceBackgroundJob job) {
        MicoServiceBackgroundJob savedJob = jobRepository.save(job);
        log.debug("Saved job: {}", savedJob);
        return savedJob;
    }

    /**
     * Deletes a job in the database.
     * If the future is still running, it will be cancelled.
     *
     * @param id the id of the job.
     */
    public void deleteJob(String id) {
        Optional<MicoServiceBackgroundJob> jobOptional = getJobById(id);
        if (jobOptional.isPresent()) {
            MicoServiceBackgroundJob job = jobOptional.get();
            deleteJob(job);
        }
    }

    /**
     * Delete all jobs in the database.
     * If a future of a job is still running, it will be cancelled.
     */
    public void deleteAllJobs() {
        List<MicoServiceBackgroundJob> jobs = getAllJobs();
        for (MicoServiceBackgroundJob job : jobs) {
            deleteJob(job);
        }
    }

    /**
     * Retrieves the job status of a {@code MicoApplication}.
     *
     * @param shortName the short name of the {@link MicoApplication}.
     * @param version   the version of the {@link MicoApplication}.
     * @return the {@link MicoApplicationJobStatus} with the status and jobs.
     */
    public MicoApplicationJobStatus getJobStatusByApplicationShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException {
        Optional<MicoApplication> existingApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!existingApplicationOptional.isPresent()) {
            throw new MicoApplicationNotFoundException(shortName, version);
        }
        MicoApplication micoApplication = existingApplicationOptional.get();

        List<MicoServiceBackgroundJob> jobList = new ArrayList<>();
        for (MicoService micoService : micoApplication.getServices()) {
            jobList.addAll(jobRepository.findByServiceShortNameAndServiceVersion(micoService.getShortName(), micoService.getVersion()));
        }
        for (MicoServiceDeploymentInfo kfConnectorDeploymentInfo : micoApplication.getKafkaFaasConnectorDeploymentInfos()) {
            MicoService kfConnectorService = kfConnectorDeploymentInfo.getService();
            jobList.addAll(jobRepository.findByServiceShortNameAndServiceVersion(kfConnectorService.getShortName(), kfConnectorService.getVersion()));
        }

        List<MicoServiceBackgroundJob.Status> statusList = jobList.stream().map(MicoServiceBackgroundJob::getStatus).distinct().collect(Collectors.toList());

        // TODO: What should be the response if there are no jobs? (see issue mico#634)
        return new MicoApplicationJobStatus(shortName, version, checkStatus(statusList), jobList);
    }

    /**
     * Return a {@code MicoServiceBackgroundJob} for a given {@code instanceId} and {@code MicoServiceBackgroundJob.Type}.
     *
     * @param instanceId instance id of a {@link MicoServiceDeploymentInfo}
     * @param type       the {@link MicoServiceBackgroundJob.Type}
     * @return the optional job. Is empty if no job exists for the given {@code instanceId}
     */
    public Optional<MicoServiceBackgroundJob> getJobByMicoServiceInstanceId(String instanceId, MicoServiceBackgroundJob.Type type) {
        return jobRepository.findByInstanceIdAndType(instanceId, type);
    }

    /**
     * Return {@code MicoServiceBackgroundJob}s for a given {@code MicoService} and {@code MicoServiceBackgroundJob.Type}.
     *
     * @param micoServiceShortName the short name of a {@link MicoService}
     * @param micoServiceVersion   the version of a {@link MicoService}
     * @param type                 the {@link MicoServiceBackgroundJob.Type}
     * @return the job list. Is empty if no job exists for the given {@link MicoService}
     */
    public List<MicoServiceBackgroundJob> getJobsByMicoService(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundJob.Type type) {
        return jobRepository.findByServiceShortNameAndServiceVersionAndType(micoServiceShortName, micoServiceVersion, type);
    }

    /**
     * Saves a future of a job to the database.
     *
     * @param micoServiceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @param future                    the future as a {@link CompletableFuture}
     * @param type                      the {@link MicoServiceBackgroundJob.Type}
     */
    void saveFutureOfJob(MicoServiceDeploymentInfo micoServiceDeploymentInfo, MicoServiceBackgroundJob.Type type,
                         CompletableFuture<?> future) {
        String micoServiceInstanceId = micoServiceDeploymentInfo.getInstanceId();
        MicoService micoService = micoServiceDeploymentInfo.getService();
        Optional<MicoServiceBackgroundJob> jobOptional = getJobByMicoServiceInstanceId(micoServiceInstanceId, type);
        if (jobOptional.isPresent()) {
            MicoServiceBackgroundJob job = jobOptional.get();
            job.setFuture(future);
            saveJob(job);
            log.debug("Saved new future of job '{}' with type '{}' for MicoService '{}' '{}' with instance id '{}'.",
                job.getId(), type, micoService.getShortName(), micoService.getVersion(), micoServiceInstanceId);
        } else {
            log.warn("No job of type '{}' exists for '{}' '{}' with instance id '{}'.",
                type, micoService.getShortName(), micoService.getVersion(), micoServiceInstanceId);
        }
    }

    /**
     * Saves a new status of a job to the database.
     *
     * @param micoServiceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @param type                      the {@link MicoServiceBackgroundJob.Type}
     * @param newStatus                 the new {@link MicoServiceBackgroundJob.Status}
     */
    void saveNewStatus(MicoServiceDeploymentInfo micoServiceDeploymentInfo, MicoServiceBackgroundJob.Type type,
                       MicoServiceBackgroundJob.Status newStatus) {
        saveNewStatus(micoServiceDeploymentInfo, type, newStatus, null);
    }

    /**
     * Saves a new status of a job to the database.
     *
     * @param micoServiceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @param type                      the {@link MicoServiceBackgroundJob.Type}
     * @param newStatus                 the new {@link MicoServiceBackgroundJob.Status}
     * @param errorMessage              the optional error message if the job has failed
     */
    void saveNewStatus(MicoServiceDeploymentInfo micoServiceDeploymentInfo, MicoServiceBackgroundJob.Type type,
                       MicoServiceBackgroundJob.Status newStatus, @Nullable String errorMessage) {
        String micoServiceInstanceId = micoServiceDeploymentInfo.getInstanceId();
        MicoService micoService = micoServiceDeploymentInfo.getService();
        Optional<MicoServiceBackgroundJob> jobOptional = getJobByMicoServiceInstanceId(micoServiceInstanceId, type);
        if (jobOptional.isPresent()) {
            MicoServiceBackgroundJob job = jobOptional.get();
            if (!job.getStatus().equals(newStatus)) {
                log.info("Job of '{}' '{}' with instance id '{}' with type '{}' changed its status: {} → {}.",
                    micoService.getShortName(), micoService.getVersion(), micoServiceInstanceId, type, job.getStatus(), newStatus);
                if (newStatus.equals(MicoServiceBackgroundJob.Status.ERROR) && !StringUtils.isEmpty(errorMessage)) {
                    log.warn("Job of '{}' '{}' with instance id '{}' with type '{}' failed. Reason: {}",
                        micoService.getShortName(), micoService.getVersion(), micoServiceInstanceId, type, errorMessage);
                }
                job.setStatus(newStatus);
                job.setErrorMessage(errorMessage);
                saveJob(job);
            }
        } else {
            log.warn("No job of type '{}' exists for '{}' '{}' with instance id '{}'.",
                type, micoService.getShortName(), micoService.getVersion(), micoServiceInstanceId);
        }
    }

    /**
     * Deletes the specified job.
     * If the included future is still running, it will be cancelled.
     *
     * @param job the {@link MicoServiceBackgroundJob}
     */
    private void deleteJob(MicoServiceBackgroundJob job) {
        if (job.getFuture() != null && !job.getFuture().isCancelled()
            && !job.getFuture().isCompletedExceptionally() && !job.getFuture().isDone()) {
            log.warn("Job of type '{}' and current status '{}' of MicoService '{}' '{}' with instanceId '{}' is going to be deleted, " +
                    "but it's future is still running -> Cancel it.",
                job.getType(), job.getStatus(), job.getServiceShortName(), job.getServiceVersion(), job.getInstanceId());
            job.getFuture().cancel(true);
        }
        jobRepository.delete(job);
    }

    /**
     * Retrieves the {@code Status} which is most relevant. The order
     * of relevance is as follows:
     * <ol>
     * <li>{@link Status#ERROR}</li>
     * <li>{@link Status#PENDING}</li>
     * <li>{@link Status#RUNNING}</li>
     * <li>{@link Status#DONE}</li>
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
