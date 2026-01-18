package com.github;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Publisher {

    public static final Integer PORT = 8765;

    private final SocketChannel socketChannel;

    public Publisher() {
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("localhost", PORT);
            this.socketChannel = SocketChannel.open(serverAddress);
        } catch (IOException e) {
            log.error("Exception when create publisher", e);
            throw new RuntimeException(e);
        }
    }

    public void publish(String message) {
        System.out.println("Publishing message: " + message);

        try {
            Charset charset = StandardCharsets.UTF_8;
            socketChannel.write(charset.encode(message));
        } catch (Exception e) {
            log.error("Exception when publish", e);
        }
    }

    public void stop() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
