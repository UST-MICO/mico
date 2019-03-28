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

import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.model.MicoServiceBackgroundJob.Status;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoBackgroundJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

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
        return jobRepository.save(job);
    }

    /**
     * Deletes a job in the database.
     * If the future is still running, cancel it.
     *
     * @param id the id of the job.
     */
    public void deleteJob(String id) {
        Optional<MicoServiceBackgroundJob> jobOptional = getJobById(id);
        if(jobOptional.isPresent()) {
            MicoServiceBackgroundJob job = jobOptional.get();
            if(job.getFuture() != null && !job.getFuture().isCancelled()
                && !job.getFuture().isCompletedExceptionally() && job.getFuture().isDone()) {
                log.warn("Job '{}' is going to be deleted, but it's future is still running -> Cancel it.");
                job.getFuture().cancel(true);
            }
            jobRepository.delete(job);
        }
    }

    /**
     * Retrieves the job status of a {@code MicoApplication}.
     *
     * @param shortName the short name of the {@link MicoApplication}.
     * @param version   the version of the {@link MicoApplication}.
     * @return the {@link MicoApplicationJobStatus} with the status and jobs.
     */
    public MicoApplicationJobStatus getJobStatusByApplicationShortNameAndVersion(String shortName, String version) {
        Optional<MicoApplication> existingApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!existingApplicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }
        MicoApplication micoApplication = existingApplicationOptional.get();

        List<MicoServiceBackgroundJob> jobList = new ArrayList<>();
        for (MicoService micoService : micoApplication.getServices()) {
            jobList.addAll(jobRepository.findByServiceShortNameAndServiceVersion(micoService.getShortName(), micoService.getVersion()));
        }

        List<MicoServiceBackgroundJob.Status> statusList = jobList.stream().map(MicoServiceBackgroundJob::getStatus).distinct().collect(Collectors.toList());

        // TODO: What should be the response if there are no jobs? (see issue mico#634)
        return new MicoApplicationJobStatus(shortName, version, checkStatus(statusList), jobList);
    }

    /**
     * Return a {@code MicoServiceBackgroundJob} for a given {@code MicoService} and {@code MicoServiceBackgroundJob.Type}.
     *
     * @param micoServiceShortName the short name of a {@link MicoService}
     * @param micoServiceVersion   the version of a {@link MicoService}
     * @param type                 the {@link MicoServiceBackgroundJob.Type}
     * @return the optional Job. Is empty if no Job exist for the given {@link MicoService}
     */
    public Optional<MicoServiceBackgroundJob> getJobByMicoService(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundJob.Type type) {
        return jobRepository.findByServiceShortNameAndServiceVersionAndType(micoServiceShortName, micoServiceVersion, type);
    }

    /**
     * Saves a future of a job to the database.
     *
     * @param micoServiceShortName the short name of a {@link MicoService}
     * @param micoServiceVersion   the version of a {@link MicoService}
     * @param future               the future as a {@link CompletableFuture}
     * @param type                 the {@link MicoServiceBackgroundJob.Type}
     */
    public void saveFutureOfJob(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundJob.Type type,
                                CompletableFuture<?> future) {
        Optional<MicoServiceBackgroundJob> jobOptional = getJobByMicoService(micoServiceShortName, micoServiceVersion, type);
        if (jobOptional.isPresent()) {
            MicoServiceBackgroundJob job = jobOptional.get();
            job.setFuture(future);
            saveJob(job);
            log.debug("Saved new future of job '{}' with type '{}' for MicoService '{}' '{}'.",
                job.getId(), type, micoServiceShortName, micoServiceVersion);
        } else {
            log.warn("No job of type '{}' exists for '{}' '{}'.", type, micoServiceShortName, micoServiceVersion);
        }
    }

    /**
     * Saves a new status of a job to the database.
     *
     * @param micoServiceShortName the short name of a {@link MicoService}
     * @param micoServiceVersion   the version of a {@link MicoService}
     * @param type                 the {@link MicoServiceBackgroundJob.Type}
     * @param newStatus            the new {@link MicoServiceBackgroundJob.Status}
     */
    public void saveNewStatus(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundJob.Type type,
                              MicoServiceBackgroundJob.Status newStatus) {
        saveNewStatus(micoServiceShortName, micoServiceVersion, type, newStatus, null);
    }

    /**
     * Saves a new status of a job to the database.
     *
     * @param micoServiceShortName the short name of a {@link MicoService}
     * @param micoServiceVersion   the version of a {@link MicoService}
     * @param type                 the {@link MicoServiceBackgroundJob.Type}
     * @param newStatus            the new {@link MicoServiceBackgroundJob.Status}
     * @param errorMessage         the optional error message if the job has failed
     */
    public void saveNewStatus(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundJob.Type type,
                              MicoServiceBackgroundJob.Status newStatus, @Nullable String errorMessage) {
        Optional<MicoServiceBackgroundJob> jobOptional = getJobByMicoService(micoServiceShortName, micoServiceVersion, type);
        if (jobOptional.isPresent()) {
            MicoServiceBackgroundJob job = jobOptional.get();
            if (!job.getStatus().equals(newStatus)) {
                log.info("Job of '{}' '{}' with type '{}' changed its status: {} → {}.",
                    micoServiceShortName, micoServiceVersion, type, job.getStatus(), newStatus);
                if (newStatus.equals(MicoServiceBackgroundJob.Status.ERROR) && !StringUtils.isEmpty(errorMessage)) {
                    log.warn("Job of '{}' '{}' with type '{}' failed. Reason: {}.",
                        micoServiceShortName, micoServiceVersion, type, errorMessage);
                }
                job.setStatus(newStatus);
                job.setErrorMessage(errorMessage);
                MicoServiceBackgroundJob savedJob = saveJob(job);
                log.debug("Saved new status of job: {}", savedJob);
            }
        } else {
            log.warn("No job of type '{}' exists for '{}' '{}'.", type, micoServiceShortName, micoServiceVersion);
        }
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
