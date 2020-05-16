package io.github.ust.mico.core.service.imagebuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.MicoService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public interface ImageBuilder {
    @EventListener
    void init(ContextRefreshedEvent cre);

    void init() throws NotInitializedException;

    CompletableFuture<String> build(MicoService micoService) throws NotInitializedException, InterruptedException, ExecutionException, TimeoutException, KubernetesResourceException;

    boolean isInitialized();
}
