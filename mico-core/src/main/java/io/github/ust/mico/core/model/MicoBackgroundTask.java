package io.github.ust.mico.core.model;

import java.util.concurrent.CompletableFuture;

public class MicoBackgroundTask {
    enum TYPE{
        IMPORT, BUILD
    }
    enum STATUS{
        PENDING, RUNNING, CANCELLED, ERROR, DONE
    }
    Long id;
    CompletableFuture job;
    MicoService service;
    public MicoBackgroundTask(CompletableFuture job, MicoService service){
        this.job = job;
        this.service =service;
    }
    public void cancelJob(){
        job.cancel(false);
    }

}
