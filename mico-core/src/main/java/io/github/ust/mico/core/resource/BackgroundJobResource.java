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
package io.github.ust.mico.core.resource;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.model.MicoApplicationJobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.github.ust.mico.core.broker.BackgroundJobBroker;
import io.github.ust.mico.core.dto.response.MicoApplicationJobStatusResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceBackgroundJobResponseDTO;
import io.github.ust.mico.core.model.MicoServiceBackgroundJob;

@RestController
@RequestMapping(value = "/jobs", produces = MediaTypes.HAL_JSON_VALUE)
public class BackgroundJobResource {

    private static final String PATH_STATUS = "status";
    private static final String PATH_VARIABLE_ID = "id";
    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";

    @Autowired
    private BackgroundJobBroker backgroundJobBroker;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoServiceBackgroundJobResponseDTO>>> getAllJobs() {
        List<MicoServiceBackgroundJob> jobs = backgroundJobBroker.getAllJobs();
        List<Resource<MicoServiceBackgroundJobResponseDTO>> jobResources = getJobResourceList(jobs);

        return ResponseEntity.ok(new Resources<>(jobResources, linkTo(methodOn(BackgroundJobResource.class).getAllJobs()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_STATUS)
    public ResponseEntity<Resource<MicoApplicationJobStatusResponseDTO>> getJobStatusByApplicationShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName, @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplicationJobStatus jobStatus;
        try {
            jobStatus = backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return ResponseEntity.ok(new Resource<>(new MicoApplicationJobStatusResponseDTO(jobStatus),
            linkTo(methodOn(BackgroundJobResource.class)
                .getJobStatusByApplicationShortNameAndVersion(shortName, version)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_ID + "}")
    public ResponseEntity<Resource<MicoServiceBackgroundJobResponseDTO>> getJobById(@PathVariable(PATH_VARIABLE_ID) String id) {
        Optional<MicoServiceBackgroundJob> jobOptional = backgroundJobBroker.getJobById(id);
        if (!jobOptional.isPresent()) {
            // Likely to be permanent
            throw new ResponseStatusException(HttpStatus.GONE, "Job with id '" + id + "' was not found!");
        } else if (jobOptional.get().getStatus() == MicoServiceBackgroundJob.Status.DONE) {
            HttpHeaders responseHeaders = new HttpHeaders();
            try {
                responseHeaders.setLocation(getLocationForJob(jobOptional.get()));
            } catch (URISyntaxException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getReason());
            }
            return new ResponseEntity<>(responseHeaders, HttpStatus.SEE_OTHER);

        } else {
            return ResponseEntity.ok(new Resource<>(new MicoServiceBackgroundJobResponseDTO(jobOptional.get()), getJobLinks(jobOptional.get())));
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllJobs() {
        backgroundJobBroker.deleteAllJobs();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_ID + "}")
    public ResponseEntity<Void> deleteJob(@PathVariable(PATH_VARIABLE_ID) String id) {
        backgroundJobBroker.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    private URI getLocationForJob(MicoServiceBackgroundJob job) throws URISyntaxException {
        return new URI("/services/" + job.getServiceShortName() + "/" + job.getServiceVersion());
    }

    private List<Resource<MicoServiceBackgroundJobResponseDTO>> getJobResourceList(List<MicoServiceBackgroundJob> jobs) {
        return jobs.stream().map(job -> new Resource<>(new MicoServiceBackgroundJobResponseDTO(job), getJobLinks(job)))
            .collect(Collectors.toList());
    }

    private Iterable<Link> getJobLinks(MicoServiceBackgroundJob job) {
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(BackgroundJobResource.class).getJobById(job.getId())).withSelfRel());
        links.add(linkTo(methodOn(BackgroundJobResource.class).deleteJob(job.getId())).withRel("cancel"));
        links.add(linkTo(methodOn(BackgroundJobResource.class).getAllJobs()).withRel("jobs"));
        return links;
    }
}
