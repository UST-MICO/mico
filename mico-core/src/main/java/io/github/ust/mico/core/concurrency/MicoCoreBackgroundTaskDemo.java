package io.github.ust.mico.core.concurrency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Demo class for {@link MicoCoreBackgroundTaskFactory}.
 */
@Component
public class MicoCoreBackgroundTaskDemo {
    
    @Autowired
    private MicoCoreBackgroundTaskFactory factory;
    
    public MicoCoreBackgroundTaskDemo(MicoCoreBackgroundTaskFactory factory) {
        this.factory = factory;
    }
    
    public void demo() {
        // Fire-and-forget
        factory.runAsync(() -> veryLongLastingTask("MICO"));
        
        // Run and only proceed workflow on success
        factory.runAsync(() -> veryLongLastingTask("MICO"), result -> successHandler(result));
        
        // Full result handling - success and error
        factory.runAsync(() -> veryLongLastingTask("MICO"), result -> successHandler(result), e -> { e.printStackTrace(); return null; });
        factory.runAsync(() -> veryLongLastingTask("MICO"), result -> successHandler(result), e -> exceptionHandler(e));
    }
    
    private void successHandler(String s) {
        // Do other stuff
        System.out.println("Result: " + s);
    }
    
    private Void exceptionHandler(Throwable e) {
        e.printStackTrace();
        return null;
    }
    
    private String veryLongLastingTask(String name) {
        try {
            // Simulate "long lasting" task
            Thread.sleep(3000L);
            return "Hello " + name + "!";
        } catch (InterruptedException e) {
            return null;
        }
    }
    
}
