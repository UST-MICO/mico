package io.github.ust.mico.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoApplicationJobStatus {
    String applicationName;
    String applicationVersion;
    MicoBackgroundTask.Status status;
    List<MicoBackgroundTask> jobList;
}
