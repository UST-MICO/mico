package io.github.ust.mico.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UiPodMetrics {

    private int memoryUsage;
    private int cpuLoad;
    private boolean available;
}
