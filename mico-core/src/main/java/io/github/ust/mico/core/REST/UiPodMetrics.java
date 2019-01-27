package io.github.ust.mico.core.REST;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UiPodMetrics {

    private int memoryUsage;
    private int cpuLoad;
    private boolean available;
}
