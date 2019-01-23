package io.github.ust.mico.core.REST;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UiPodInfo {

    String podName;
    String phase;
    String hostIp;
}
