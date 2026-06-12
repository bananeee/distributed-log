package com.github.bananeee.distributedlog.client;

import com.github.bananeee.distributedlog.common.ProduceAckType;
import com.github.bananeee.distributedlog.common.serialization.StringAdapter;
import com.github.bananeee.distributedlog.producer.Producer;
import com.github.bananeee.distributedlog.producer.ProducerRecord;

public class ProducerClient {
    public static void main(String[] args) throws InterruptedException {
        Producer<String, String> producer = new Producer<>(new StringAdapter(), new StringAdapter(), ProduceAckType.NO_ACK);
        ProducerRecord<String, String> record = new ProducerRecord<>();
        record.setKey("test_key")
                .setValue("Nice to meet you!")
                .setTopic("topic1");


        producer.send(record);

        Thread.sleep(1000);
        System.out.println("Hello World!");


        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
