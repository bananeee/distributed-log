package com.github.bananeee.distributedlog.broker;

import com.github.bananeee.distributedlog.common.FetchErrorCode;
import com.github.bananeee.distributedlog.common.FetchRequest;
import com.github.bananeee.distributedlog.common.FetchResponse;
import com.github.bananeee.distributedlog.common.PartitionFetchRequest;
import com.github.bananeee.distributedlog.common.PartitionFetchResponse;
import com.github.bananeee.distributedlog.common.ProduceRequest;
import com.github.bananeee.distributedlog.common.RecordBatch;
import com.github.bananeee.distributedlog.common.RecordDto;
import com.github.bananeee.distributedlog.common.TopicFetchRequest;
import com.github.bananeee.distributedlog.common.TopicFetchResponse;
import com.github.bananeee.distributedlog.common.dto.TopicPartition;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

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

        TopicPartition topicPartition = new TopicPartition()
                .setTopic(request.getTopic().getName())
                .setPartition(request.getTopic().getPartition().getIndex());
        int offset = request.getTopic().getPartition().getIndex();

        ResultOrError<RecordBatch, FetchErrorCode> fetchResult = fetchRecords(topicPartition, offset,
                request.getMaxWaitTime(), request.getMinRecords(), request.getTopic().getPartition().getMaxRecords());

        PartitionFetchResponse partitionFetchResponse;
        if (fetchResult.isError()) {
            log.error("Cannot fetch content for topic {} with offset {}", topicPartition.getTopic(), offset);
            partitionFetchResponse = PartitionFetchResponse.newBuilder()
                    .setIndex(offset)
                    .setErrorCode(fetchResult.getError())
                    .build();
        }
        partitionFetchResponse = PartitionFetchResponse.newBuilder()
                .setIndex(offset)
                .setErrorCode(FetchErrorCode.SUCCESS)
                .setRecordBatch(fetchResult.getResult())
                .build();
        TopicFetchResponse topicFetchResponse = TopicFetchResponse.newBuilder()
                .setName(topicPartition.getTopic())
                .setPartition(partitionFetchResponse)
                .build();
        FetchResponse response = FetchResponse.newBuilder()
                .setTopic(topicFetchResponse)
                .build();
        int msgSize = response.getSerializedSize();

        try {
            ByteBuffer buffer = ByteBuffer.allocate(4 + msgSize);
            buffer.putInt(msgSize);
            buffer.put(response.toByteArray());
            buffer.flip();

            channel.write(buffer);
        } catch (IOException e) {
            log.error("Exception when handle fetch request", e);
        }
    }

    private ResultOrError<RecordBatch, FetchErrorCode> fetchRecords(TopicPartition topicPartition, long offset,
                                                                    long maxWaitTime, int minRecords, int maxRecords) {
        int processTime = 0;
        List<RecordDto> records = new ArrayList<>();
        while (processTime < maxWaitTime) {
            ResultOrError<List<RecordDto>, FetchErrorCode> fetchedResult = logManager.readLogs(topicPartition, offset,
                    maxRecords);
            if (fetchedResult.isError()) {
                return ResultOrError.error(fetchedResult.getError());
            }

            List<RecordDto> fetchedRecords = fetchedResult.getResult();
            records.addAll(fetchedRecords);
            if (records.size() >= minRecords) {
                break;
            }

            processTime++;
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return ResultOrError.success(RecordBatch.newBuilder()
                .setBatchOffset(offset).addAllRecords(records)
                .build());
    }

}
