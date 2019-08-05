package io.github.ust.mico.core.dto.request;

import io.github.ust.mico.core.model.MicoTopic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoTopicRequestDTO {


    private String topicName;

    // -------------------
    // -> Constructors ---
    // -------------------

    /**
     * Creates an instance of {@code MicoTopicRequestDTO} based on a
     * {@code MicoTopic}.
     *
     * @param micoTopic the {@link MicoTopic}.
     */
    public MicoTopicRequestDTO(MicoTopic micoTopic) {
        this.topicName = micoTopic.getTopicName();
    }
}
