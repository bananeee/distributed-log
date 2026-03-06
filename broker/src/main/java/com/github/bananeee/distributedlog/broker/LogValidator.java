package com.github.bananeee.distributedlog.broker;

import com.github.bananeee.distributedlog.common.TopicFetchRequest;
import com.github.bananeee.distributedlog.common.TopicProduce;

public class LogValidator {

    public ValidationResult validateProduceTopic(TopicProduce topic) {
        if (topic == null || topic.getName().isEmpty()) {
            return new ValidationResult(false, "topic is empty");
        }

        if (topic.getPartition().getContent().isEmpty()) {
            return new ValidationResult(false, "missing content");
        }

        return new ValidationResult(true, null);
    }

    public ValidationResult validateFetchTopic(TopicFetchRequest topic) {
        if (topic == null || topic.getName().isEmpty()) {
            return new ValidationResult(false, "topic is empty");
        }

        return new ValidationResult(true, null);
    }

    public record ValidationResult(boolean isValid, String message) { }
}
