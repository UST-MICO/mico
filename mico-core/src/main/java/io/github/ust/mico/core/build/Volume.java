package io.github.ust.mico.core.build;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Volume {

    /**
     * 	Volume's name.
     * 	Must be a DNS_LABEL and unique within the pod.
     * 	More info: https://kubernetes.io/docs/concepts/overview/working-with-objects/names/#names
     */
    private String name;

    /**
     * 	VolumeSource represents the location and type of the mounted volume.
     * 	If not specified, the Volume is implied to be an EmptyDir.
     * 	This implied behavior is deprecated and will be removed in a future version.
     */
    private VolumeSource volumeSource;
}
