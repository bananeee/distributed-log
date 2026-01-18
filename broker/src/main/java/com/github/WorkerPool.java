package com.github;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WorkerPool {

    private static final int POOL_SIZE = 10;

    private final List<Worker> workers;
    private int nextWorkerIndex = 0;

    public WorkerPool() {
        workers = new ArrayList<>();
        for (int i = 0; i < POOL_SIZE; i++) {
            workers.add(new Worker());
        }
    }

    public void runAll() {
        for (Worker worker : workers) {
            new Thread(worker).start();
        }
    }

    /**
     * Round robin register channel to workers
     * @param channel
     */
    public void registerChannel(SocketChannel channel) {
        try {
            channel.configureBlocking(false);
        } catch (IOException e) {
            log.error("Failed to configure channel to non-blocking", e);
            return;
        }
        workers.get(nextWorkerIndex).registerChannel(channel);
        nextWorkerIndex = (nextWorkerIndex + 1) % POOL_SIZE;
    }

    public void stop() {
        
    }



}
