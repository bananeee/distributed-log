package com.github.bananeee.distributedlog.client;

import com.github.bananeee.distributedlog.common.dto.TopicPartition;
import com.github.bananeee.distributedlog.common.serialization.StringAdapter;
import com.github.bananeee.distributedlog.consumer.Consumer;
import com.github.bananeee.distributedlog.consumer.ConsumerConfig;

public class ConsumerClient {
    public static void main(String[] args) throws InterruptedException {


        Consumer<String, String> consumer = new Consumer<>(new ConsumerConfig(), new StringAdapter(), new StringAdapter());
        consumer.assign(new TopicPartition().setTopic("topic1"));
//        Publisher pub1 = new Publisher();
//        pub1.publish("sampleTopic", "samplePartition", "Nice to meet you!");
        Thread.sleep(1000);
//        pub1.publish("sampleTopic", "samplePartition","Nice to meet you second time!");
        System.out.println("Hello World!");

//        pub1.stop();

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
