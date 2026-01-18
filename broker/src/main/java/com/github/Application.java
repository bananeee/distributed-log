package com.github;

import java.io.IOException;

public class Application {

    public static void main(String[] args) {
        try {
            BrokerServer brokerServer = new BrokerServer();
            brokerServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Hello, World!");
    }
}
