package com.github.bananeee.distributedlog.consumer.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ConsumerRecord<K, V> {
    private String topic;
    private Integer partition;
    private String header;      // reserve
    private K key;
    private V value;
    private Long timestamp;

}
