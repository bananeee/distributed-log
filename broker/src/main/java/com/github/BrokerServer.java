package com.github;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadFactory;


@Slf4j
public class BrokerServer {

    private static final int PORT = 8765;

    private final ServerSocket serverSocket;
    private final ThreadFactory virtualThreadFactory;

    public BrokerServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            virtualThreadFactory = Thread.ofVirtual().factory();
        } catch (IOException e) {
            log.error("Failed to start server on port {}", PORT, e);
            throw new IllegalStateException("Failed to start server on port " + PORT, e);
        }
    }

    public void start() throws IOException {
        log.info("Broker Server started on port {}", PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread workerThread = virtualThreadFactory.newThread(new Worker(clientSocket));
            workerThread.start();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
