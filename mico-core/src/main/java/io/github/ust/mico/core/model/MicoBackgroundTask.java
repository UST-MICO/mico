package io.github.ust.mico.core.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

@Data
@RedisHash("BackgroundJob")
public class MicoBackgroundTask implements Serializable {
    public enum Type {
        IMPORT, BUILD
    }

    public enum Status {
        PENDING, RUNNING, CANCELLED, ERROR, DONE
    }

    @Id
    String id;
    CompletableFuture job;
    MicoService service;
    Status status;
    Type type;

    public MicoBackgroundTask(CompletableFuture job, MicoService service, Type type) {
        this.job = job;
        this.service = service;
        this.type = type;
        this.status = Status.PENDING;
    }

    public void cancelJob() {
        job.cancel(false);
    }

}
