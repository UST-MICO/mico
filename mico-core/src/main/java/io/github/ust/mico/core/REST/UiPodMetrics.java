package io.github.ust.mico.core.REST;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UiPodMetrics {

    private int memoryUsage;
    private boolean available;
}
