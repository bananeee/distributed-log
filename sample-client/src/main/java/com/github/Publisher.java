package com.github;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class Publisher {

    public static final Integer PORT = 8765;

    private final Socket clientSocket;
    private final PrintWriter out;

    public Publisher() {
        try {
            clientSocket = new Socket("localhost", PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new IllegalStateException("Exception when creating publisher", e);
        }
    }

    public void publish(String message) {
        System.out.println("Publishing message: " + message);
        out.println(message);
    }

    public void stop() {
        try {
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            log.error("Exception when stopping publisher", e);
        }
    }
}
