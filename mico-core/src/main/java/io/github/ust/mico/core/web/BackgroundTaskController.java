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
package io.github.ust.mico.core.web;

import io.github.ust.mico.core.model.MicoBackgroundTask;
import io.github.ust.mico.core.persistence.MicoBackgroundTaskRepository;
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
public class BackgroundTaskController {

    private static final String PATH_ID = "id";

    @Autowired
    private MicoBackgroundTaskRepository jobRepository;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoBackgroundTask>>> getAllJobs() {
        List<MicoBackgroundTask> jobs = jobRepository.findAll();
        List<Resource<MicoBackgroundTask>> jobResources = getJobResourceList(jobs);
        return ResponseEntity.ok(new Resources<>(jobResources, linkTo(methodOn(BackgroundTaskController.class).getAllJobs()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_ID + "}")
    public ResponseEntity<Resource<MicoBackgroundTask>> getJobById(@PathVariable(PATH_ID) String id) {
        Optional<MicoBackgroundTask> jobOptional = jobRepository.findById(id);
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
        return jobOptional.map(job -> new Resource<>(job, getJobLinks(job)))
            .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job with id '" + id + "' links not found!"));
    }

    @DeleteMapping("/{" + PATH_ID + "}")
    public ResponseEntity<Resource<MicoBackgroundTask>> deleteJob(@PathVariable(PATH_ID) String id) {
        ResponseEntity<Resource<MicoBackgroundTask>> job = getJobById(id);
        job.getBody().removeLinks();
        jobRepository.deleteById(id);
        return job;
    }

    private URI getLocationForJob(MicoBackgroundTask job) throws URISyntaxException {
        return new URI("/services/" + job.getMicoServiceShortName() + "/" + job.getMicoServiceVersion());
    }

    private List<Resource<MicoBackgroundTask>> getJobResourceList(List<MicoBackgroundTask> applications) {
        return applications.stream().map(job -> new Resource<>(job, getJobLinks(job)))
            .collect(Collectors.toList());
    }

    private Iterable<Link> getJobLinks(MicoBackgroundTask task) {
        LinkedList<Link> links = new LinkedList<>();

        links.add(linkTo(methodOn(BackgroundTaskController.class).getJobById(task.getId())).withSelfRel());
        links.add(linkTo(methodOn(BackgroundTaskController.class).deleteJob(task.getId())).withRel("cancel"));
        links.add(linkTo(methodOn(BackgroundTaskController.class).getAllJobs()).withRel("jobs"));
        return links;
    }
}
