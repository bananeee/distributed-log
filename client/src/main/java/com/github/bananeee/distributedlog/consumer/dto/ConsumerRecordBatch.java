package com.github.bananeee.distributedlog.consumer.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConsumerRecordBatch<K, V> {
    private List<ConsumerRecord<K, V>> records;

    // not thread safe
    public void add(ConsumerRecord<K, V> record) {
        if (records == null) {
            records = new ArrayList<>();
        }
        records.add(record);
    }

}
