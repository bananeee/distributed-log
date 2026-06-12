package com.github.bananeee.distributedlog.broker;

import com.github.bananeee.distributedlog.common.FetchErrorCode;
import com.github.bananeee.distributedlog.common.RecordBatch;
import com.github.bananeee.distributedlog.common.RecordDto;
import com.github.bananeee.distributedlog.common.dto.TopicPartition;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
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

    public ResultOrError<List<RecordDto>, FetchErrorCode> readLogs(TopicPartition topicPartition, long offset, int maxRecords) {
        List<RecordDto> logs = topicToLogs.get(topicPartition.getTopic());
        if (logs == null) {
            log.warn("Topic {} does not exist.", topicPartition.getTopic());
            return ResultOrError.error(FetchErrorCode.UNKNOWN_TOPIC_OR_PARTITION);
        }
        if (offset < 0 || offset >= logs.size()) {
            log.warn("Offset {} out of bounds for topic {}.", offset, topicPartition.getTopic());
            return ResultOrError.error(FetchErrorCode.OFFSET_OUT_OF_RANGE);
        }

        int lastOffset = Math.max(logs.size() - 1, (int) offset + maxRecords);
        return ResultOrError.success(logs.subList((int) offset, lastOffset));
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
        logs.addAll(batch.getRecordsList());

        log.info("Appended log to topic {}. All log data: {}", topicName, Arrays.toString(logs.toArray()));

        return true;
    }
}
