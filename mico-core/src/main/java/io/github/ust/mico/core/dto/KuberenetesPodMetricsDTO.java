package io.github.ust.mico.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Contains information about CPU/ memory load of a {@link io.fabric8.kubernetes.api.model.Pod}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KuberenetesPodMetricsDTO {

    private int memoryUsage;
    private int cpuLoad;
    private boolean available;
    
}
