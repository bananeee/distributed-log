package com.github;

/**
 * Hello world!
 *
 */
public class Application {
    public static void main(String[] args) {

        Publisher pub1 = new Publisher();
        pub1.publish("Nice to meet you!");
        pub1.publish("Nice to meet you second time!");
        System.out.println("Hello World!");

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
