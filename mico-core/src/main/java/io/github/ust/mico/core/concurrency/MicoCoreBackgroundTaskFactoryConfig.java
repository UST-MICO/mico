package io.github.ust.mico.core.concurrency;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;

/**
 * Configuration for {@link MicoCoreBackgroundTaskFactory}.
 */
@Component
@ConfigurationProperties(prefix = "mico-core.concurrency")
@Getter
public class MicoCoreBackgroundTaskFactoryConfig {

    /**
     * The number of threads in the {@link MicoCoreBackgroundTaskFactory}.
     */
    private int threadPoolSize;

}
