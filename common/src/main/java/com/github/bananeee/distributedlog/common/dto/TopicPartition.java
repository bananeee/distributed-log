package com.github.bananeee.distributedlog.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TopicPartition {

    private String topic;
    private int partition;

    @Override
    public String toString() {
        return topic + "-" + partition;
    }

    @Override
    public int as() {

    }


}
