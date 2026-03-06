package com.github.bananeee.distributedlog.broker;

import com.github.bananeee.distributedlog.common.RecordBatch;
import com.github.bananeee.distributedlog.common.RecordDto;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class LogManager {

    // TODO: Support partitions
    private ConcurrentMap<String, List<RecordDto>> topicToLogs;

    public LogManager() {
        this.topicToLogs = new ConcurrentHashMap<>();
    }

    public boolean createTopic(String topicName) {
        if (topicToLogs.containsKey(topicName)) {
            log.warn("Topic {} already exists.", topicName);
            return false;
        }
        topicToLogs.put(topicName, new ArrayList<>());
        log.info("Topic {} created successfully.", topicName);
        return true;
    }

    public ByteString readLog(String topicName, int index) {
        List<ByteString> logs = topicToLogs.get(topicName);
        if (logs == null) {
            log.warn("Topic {} does not exist.", topicName);
            return null;
        }
        if (index < 0 || index >= logs.size()) {
            log.warn("Index {} out of bounds for topic {}.", index, topicName);
            return null;
        }
        return logs.get(index);
    }

    public boolean appendLog(String topicName, RecordBatch batch) {
        // TODO: Create topic manually by admin API
        if (!topicToLogs.containsKey(topicName))  {
            boolean created = createTopic(topicName);
            if (!created) {
                log.error("Failed to create topic {} for appending log.", topicName);
                return false;
            }
        }

        List<RecordDto> logs = topicToLogs.get(topicName);
        logs.add(content);

//        log.info("Appended log to topic {}. All log data: {}", topicName, Arrays.toString(logs.toArray()));

        return true;
    }
}
