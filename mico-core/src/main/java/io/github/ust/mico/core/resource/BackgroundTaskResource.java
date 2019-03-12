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

import io.github.ust.mico.core.broker.BackgroundTaskBroker;
import io.github.ust.mico.core.dto.MicoApplicationJobStatusDTO;
import io.github.ust.mico.core.dto.MicoBackgroundTaskDTO;
import io.github.ust.mico.core.model.MicoBackgroundTask;
import lombok.extern.slf4j.Slf4j;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = "/jobs", produces = MediaTypes.HAL_JSON_VALUE)
public class BackgroundTaskResource {

    private static final String PATH_ID = "id";
    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";

    @Autowired
    private BackgroundTaskBroker backgroundTaskBroker;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoBackgroundTaskDTO>>> getAllJobs() {
        List<MicoBackgroundTask> jobs = backgroundTaskBroker.getAllJobs();
        List<Resource<MicoBackgroundTaskDTO>> jobResources = getJobResourceList(jobs);

        return ResponseEntity.ok(new Resources<>(jobResources, linkTo(methodOn(BackgroundTaskResource.class).getAllJobs()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/jobstatus")
    public ResponseEntity<Resource<MicoApplicationJobStatusDTO>> getJobStatusByApplicationShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName, @PathVariable(PATH_VARIABLE_VERSION) String version) {
        return ResponseEntity.ok(new Resource<>(MicoApplicationJobStatusDTO.valueOf(
            backgroundTaskBroker.getJobStatusByApplicationShortNameAndVersion(shortName, version)), linkTo(methodOn(BackgroundTaskResource.class)
            .getJobStatusByApplicationShortNameAndVersion(shortName, version)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_ID + "}")
    public ResponseEntity<Resource<MicoBackgroundTaskDTO>> getJobById(@PathVariable(PATH_ID) String id) {
        Optional<MicoBackgroundTask> jobOptional = backgroundTaskBroker.getJobById(id);
        if (!jobOptional.isPresent()) {
            // likely to be permanent
            throw new ResponseStatusException(HttpStatus.GONE, "Job with id '" + id + "' was not found!");
        }
        if (jobOptional.get().getStatus() == MicoBackgroundTask.Status.DONE) {
            HttpHeaders responseHeaders = new HttpHeaders();
            try {
                responseHeaders.setLocation(getLocationForJob(jobOptional.get()));
            } catch (URISyntaxException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getReason());
            }
            return new ResponseEntity<>(responseHeaders, HttpStatus.SEE_OTHER);

        }
        return jobOptional.map(job -> new Resource<>(MicoBackgroundTaskDTO.valueOf(job), getJobLinks(job)))
            .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job with id '" + id + "' links not found!"));
    }

    @DeleteMapping("/{" + PATH_ID + "}")
    public ResponseEntity<Resource<MicoBackgroundTaskDTO>> deleteJob(@PathVariable(PATH_ID) String id) {
        ResponseEntity<Resource<MicoBackgroundTaskDTO>> job = getJobById(id);
        backgroundTaskBroker.deleteJob(id);
        job.getBody().removeLinks();
        return job;
    }

    private URI getLocationForJob(MicoBackgroundTask job) throws URISyntaxException {
        return new URI("/services/" + job.getMicoServiceShortName() + "/" + job.getMicoServiceVersion());
    }

    private List<Resource<MicoBackgroundTaskDTO>> getJobResourceList(List<MicoBackgroundTask> jobs) {
        return jobs.stream().map(job -> new Resource<>(MicoBackgroundTaskDTO.valueOf(job), getJobLinks(job)))
            .collect(Collectors.toList());
    }

    private Iterable<Link> getJobLinks(MicoBackgroundTask task) {
        LinkedList<Link> links = new LinkedList<>();

        links.add(linkTo(methodOn(BackgroundTaskResource.class).getJobById(task.getId())).withSelfRel());
        links.add(linkTo(methodOn(BackgroundTaskResource.class).deleteJob(task.getId())).withRel("cancel"));
        links.add(linkTo(methodOn(BackgroundTaskResource.class).getAllJobs()).withRel("jobs"));
        return links;
    }
}
