package io.github.ust.mico.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UiPodInfo {

    String podName;
    String phase;
    String hostIp;
    String nodeName;
    UiPodMetrics metrics;
}
