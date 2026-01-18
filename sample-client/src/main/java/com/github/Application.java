package com.github;

/**
 * Hello world!
 *
 */
public class Application {
    public static void main(String[] args) throws InterruptedException {

        Publisher pub1 = new Publisher();
        pub1.publish("Nice to meet you!");
        Thread.sleep(1000);
        pub1.publish("Nice to meet you second time!");
        System.out.println("Hello World!");

        pub1.stop();

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
