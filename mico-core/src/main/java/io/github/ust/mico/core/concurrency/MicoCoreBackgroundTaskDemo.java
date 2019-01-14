package io.github.ust.mico.core.concurrency;

/**
 * Demo class for {@link MicoCoreBackgroundTaskFactory}.
 */
public class MicoCoreBackgroundTaskDemo {
    
    public static void main(String[] args) {
        MicoCoreBackgroundTaskDemo demo = new MicoCoreBackgroundTaskDemo();
        
        // Fire-and-forget
        MicoCoreBackgroundTaskFactory.runAsync(() -> demo.veryLongLastingTask("MICO"));
        
        // Run and only proceed workflow on success
        MicoCoreBackgroundTaskFactory.runAsync(() -> demo.veryLongLastingTask("MICO"), result -> demo.successHandler(result));
        
        // Full result handling - success and error
        MicoCoreBackgroundTaskFactory.runAsync(() -> demo.veryLongLastingTask("MICO"), result -> demo.successHandler(result), e -> { e.printStackTrace(); return null; });
        MicoCoreBackgroundTaskFactory.runAsync(() -> demo.veryLongLastingTask("MICO"), result -> demo.successHandler(result), e -> demo.exceptionHandler(e));
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
