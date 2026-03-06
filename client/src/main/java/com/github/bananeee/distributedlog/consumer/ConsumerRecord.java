package com.github.bananeee.distributedlog.consumer;

public class ConsumerRecord<K, V> {
    private String topic;
    private Integer partition;
    private String header;      // reserve
    private K key;
    private V value;
    private Long timestamp;

}
