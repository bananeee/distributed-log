package com.github.bananeee.distributedlog.broker;

import com.github.bananeee.distributedlog.common.FetchRequest;
import com.github.bananeee.distributedlog.common.ProduceRequest;
import com.github.bananeee.distributedlog.common.RecordBatch;
import com.github.bananeee.distributedlog.common.TopicFetchRequest;
import com.github.bananeee.distributedlog.common.TopicProduce;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@Slf4j
public class RequestHandler {

    private LogManager logManager;
    private LogValidator logValidator;

    public RequestHandler(LogManager logManager) {
        this.logManager = logManager;
        this.logValidator = new LogValidator();
    }

    public void handle(Any request, SocketChannel channel) throws InvalidProtocolBufferException {
        if (request.is(ProduceRequest.class)) {
            handleProduceRequest(request.unpack(ProduceRequest.class));
        } else if (request.is(TopicFetchRequest.class)) {
            handleFetchRequest(request.unpack(FetchRequest.class), channel);
        } else {
            log.error("No controller assigned to this request: payload - {}", request);
        }
    }

    private void handleProduceRequest(ProduceRequest request) {
        log.info("Received produce request: {}", request.toString());

        LogValidator.ValidationResult validationResult = logValidator.validateProduceTopic(request.getTopic());
        if (!validationResult.isValid()) {
            log.error("Invalid produce request: {}", validationResult.message());
            return;
        }
        String topic = request.getTopic().getName();
        RecordBatch batch = request.getTopic().getPartition().getBatch();
        logManager.appendLog(topic, batch);

    }

    private void handleFetchRequest(FetchRequest request, SocketChannel channel) {
        log.info("Received fetch request: {}", request.toString());

        LogValidator.ValidationResult validationResult = logValidator.validateFetchTopic(request.getTopic());
        if (!validationResult.isValid()) {
            log.error("Invalid fetch request: {}", validationResult.message());
            return;
        }
        String topic = request.getTopic().getName();
        int offset = request.getTopic().getPartition().getIndex();
        ByteString content = logManager.readLog(topic, offset);
        if (content == null) {
            log.error("Cannot fetch content for topic {} with offset {}", topic, offset);
            return;
        }

        try {
            channel.write(content.asReadOnlyByteBuffer());
        } catch (IOException e) {
            log.error("Exception when handle fetch request", e);
        }
    }
}
