package io.github.ust.mico.core.model;


import io.github.ust.mico.core.dto.request.MicoTopicRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * A Topic represented a kafka-topic
 * Instances of this class are persisted as nodes in the neo4j database
 * <p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class MicoTopic {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Name of the topic
     */
    private String topicName;

    // ----------------------
    // -> Static Creators ---
    // ----------------------

    /**
     * Creates a new {@code MicoTopic} based on a {@code MicoTopicRequestDTO}.
     */

    public static MicoTopic valueOf(MicoTopicRequestDTO topicDto) {
        return new MicoTopic()
            .setTopicName(topicDto.getTopicName());
    }
}
