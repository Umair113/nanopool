package net.nanopool;

public class DefaultContentionHandler implements ContentionHandler {
    
    public void handleContention() {
        System.err.println("PoolingDataSource: contention warning.");
        Thread.yield();
    }
}