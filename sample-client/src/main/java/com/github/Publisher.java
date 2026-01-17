package com.github;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Publisher {

    public static final Integer PORT = 8765;

    public void publish(String message) {
        System.out.println("Publishing message: " + message);

        var serverAddress = new InetSocketAddress("localhost", PORT);
        try (var socketChannel = SocketChannel.open(serverAddress)) {
            Charset charset = StandardCharsets.UTF_8;
            socketChannel.write(charset.encode(message));

        } catch (Exception e) {
            log.error("Exception when publish", e);
        }
    }
}
