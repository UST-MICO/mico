package io.github.ust.mico.core.web;

import io.github.ust.mico.core.model.MicoBackgroundTask;
import io.github.ust.mico.core.persistence.MicoBackgroundTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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
        List<MicoBackgroundTask> jobs = new ArrayList<>();
        jobRepository.findAll().forEach(jobs::add);
        List<Resource<MicoBackgroundTask>> jobResources = getJobResourceList(jobs);
        return ResponseEntity.ok(new Resources<>(jobResources, linkTo(methodOn(BackgroundTaskController.class).getAllJobs()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_ID + "}")
    public ResponseEntity<Resource<MicoBackgroundTask>> getJobById(@PathVariable(PATH_ID) String id) {
        Optional<MicoBackgroundTask> jobOptional = jobRepository.findById(id);
        if (!jobOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job with id'" + id + "' was not found!");
        }
        return jobOptional.map(job -> new Resource<>(job, getJobLinks(job)))
            .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job with id '" + id + "' links not found!"));
    }


    private List<Resource<MicoBackgroundTask>> getJobResourceList(List<MicoBackgroundTask> applications) {
        return applications.stream().map(application -> new Resource<>(application, getJobLinks(application)))
            .collect(Collectors.toList());
    }

    private Iterable<Link> getJobLinks(MicoBackgroundTask task) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(BackgroundTaskController.class).getJobById(task.getId())).withSelfRel());
        links.add(linkTo(methodOn(BackgroundTaskController.class).getAllJobs()).withRel("jobs"));
        return links;
    }
}
