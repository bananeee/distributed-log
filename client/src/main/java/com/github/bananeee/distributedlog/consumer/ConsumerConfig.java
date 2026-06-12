package com.github.bananeee.distributedlog.consumer;

import java.util.HashMap;
import java.util.Map;

public class ConsumerConfig {
    public static final String MAX_POLL_RECORD_CONFIG = "max.poll.records";
    public static final String MAX_POLL_INTERVAL_MS_CONFIG = "max.poll.interval.ms";
    public static final String FETCH_MIN_BYTES_CONFIG = "fetch.min.bytes";
    public static final String MAX_PARTITION_FETCH_BYTES_CONFIG = "max.partition.fetch.bytes";
    public static final String FETCH_MAX_WAIT_MS_CONFIG = "fetch.max.wait.ms";

    // FIXME: Temporary
    public static final String MAX_PARTITION_FETCH_RECORDS_CONFIG = "max.partition.fetch.records";
    public static final String FETCH_MIN_RECORDS_CONFIG = "fetch.min.records";

    private final Map<String, String> configs;

    // setup default value
    public ConsumerConfig() {
        configs = new HashMap<>();
        configs.put(MAX_PARTITION_FETCH_RECORDS_CONFIG, "10");
        configs.put(FETCH_MIN_RECORDS_CONFIG, "2");
        configs.put(FETCH_MAX_WAIT_MS_CONFIG, "300_000");
    }

    public void setProperty(String key, String value) {
        configs.put(key, value);
    }

    public String getProperty(String key) {
        return configs.get(key);
    }

}
