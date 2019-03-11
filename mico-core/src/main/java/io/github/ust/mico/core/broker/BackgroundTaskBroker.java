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

@Slf4j
@Service
public class BackgroundTaskBroker {
    @Autowired
    private MicoBackgroundTaskRepository jobRepository;

    @Autowired
    private MicoApplicationRepository applicationRepository;

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
