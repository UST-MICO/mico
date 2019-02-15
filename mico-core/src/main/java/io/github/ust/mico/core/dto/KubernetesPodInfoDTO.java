package io.github.ust.mico.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
// TODO: Class comment.
public class KubernetesPodInfoDTO {
    
    // TODO: Add comments for fields.

    private String podName;
    private String phase;
    private String hostIp;
    private String nodeName;
    private KuberenetesPodMetricsDTO metrics;
    
}
