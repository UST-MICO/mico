package io.github.ust.mico.core.dto.response;

import io.github.ust.mico.core.dto.request.MicoTopicRequestDTO;
import io.github.ust.mico.core.model.MicoTopic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoTopic} for response only use
 */

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Accessors(chain = true)
public class MicoTopicResponseDTO extends MicoTopicRequestDTO {


    // -------------------
    // -> Constructors ---
    // -------------------

    /**
     * Creates an instance of {@code MicoTopicResponseDTO} based on a
     * {@code MicoTopic}.
     *
     * @param micoTopic {@link MicoTopic}.
     */
    public MicoTopicResponseDTO(MicoTopic micoTopic) {
        super(micoTopic);
    }
}
