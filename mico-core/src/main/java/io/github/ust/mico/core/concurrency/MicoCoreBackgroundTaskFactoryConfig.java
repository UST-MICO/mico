package io.github.ust.mico.core.concurrency;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for {@link MicoCoreBackgroundTaskFactory}.
 */
@Component
@ConfigurationProperties(prefix = "mico-core.concurrency")
public class MicoCoreBackgroundTaskFactoryConfig {

    /**
     * The number of threads in the {@link MicoCoreBackgroundTaskFactory}.
     */
    @Getter
    @Setter
    private int threadPoolSize;

}
