package com.github;

import java.io.IOException;

public class Application {

    public static void main(String[] args) {
        BrokerServer brokerServer = new BrokerServer();
        try {
            brokerServer.start();
        } catch (IOException e) {
            brokerServer.stop();
            throw new RuntimeException(e);
        }
        System.out.println("Hello, World!");
    }
}
