package com.github;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

@Slf4j
public class Worker implements Runnable {

    private final Socket clientSocket;

    public Worker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        read();
    }

    private void read() {
        try (
//            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String s;
            while ((s = in.readLine()) != null) {
                log.info("{}", s);
            }
        } catch (IOException e) {
            stop();
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            log.error("Error closing client socket", e);
        }
    }

}
