package io.github.ust.mico.core;

import io.github.ust.mico.core.model.MicoBackgroundTask;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoBackgroundTaskRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;

import static io.github.ust.mico.core.TestConstants.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoBackgroundTaskTest {
    @Autowired
    MicoBackgroundTaskRepository repo;

    @Test
    public void fetchJobs() {
        for (MicoBackgroundTask t : repo.findAll()) {
            System.out.println(t);
        }
    }

    @Test
    public void saveJobInDb() {
        MicoBackgroundTask task = new MicoBackgroundTask(CompletableFuture.completedFuture("testval"), new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1).setDescription(DESCRIPTION_1), MicoBackgroundTask.Type.BUILD);
        System.out.println(repo == null);
        repo.save(task);
        System.out.println(repo.count());
        for (MicoBackgroundTask t : repo.findAll()) {
            System.out.println(t);
        }
    }
}
