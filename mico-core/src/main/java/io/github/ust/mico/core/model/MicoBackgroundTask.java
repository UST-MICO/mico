package io.github.ust.mico.core.model;

import org.springframework.data.redis.core.RedisHash;

import java.util.concurrent.CompletableFuture;
@RedisHash("BackgroundJob")
public class MicoBackgroundTask {
    enum Type{
        IMPORT, BUILD
    }
    enum Status{
        PENDING, RUNNING, CANCELLED, ERROR, DONE
    }

    Long id;
    CompletableFuture job;
    MicoService service;
    Status status;
    Type type;
    public MicoBackgroundTask(CompletableFuture job, MicoService service){
        this.job = job;
        this.service =service;
    }

    public void cancelJob(){
        job.cancel(false);
    }

}
