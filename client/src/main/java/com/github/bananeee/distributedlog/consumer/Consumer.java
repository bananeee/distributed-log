package com.github.bananeee.distributedlog.consumer;

import com.github.bananeee.distributedlog.common.FetchRequest;
import com.github.bananeee.distributedlog.common.FetchResponse;
import com.github.bananeee.distributedlog.common.PartitionFetchRequest;
import com.github.bananeee.distributedlog.common.RecordBatch;
import com.github.bananeee.distributedlog.common.TopicFetchRequest;
import com.github.bananeee.distributedlog.common.constant.Constant;
import com.github.bananeee.distributedlog.common.dto.TopicPartition;
import com.github.bananeee.distributedlog.common.serialization.Adapter;
import com.github.bananeee.distributedlog.consumer.dto.ConsumerRecord;
import com.github.bananeee.distributedlog.consumer.dto.ConsumerRecordBatch;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * For current version, consumer will waiting for broker response after sending poll request => consumer : connection is 1 : 1
 * => no multithread poll()
 * TODO: Support consumer group, commit sync/async, heartbeat
 * TODO: Handle 1 consumer - multiple topic
 *
 * Phase 1: Simple blocking (works for single broker, single partition)
 * Phase 2: Add buffer (enables pipelining)
 * Phase 3: Add parallel fetches (multiple brokers)
 * Phase 4: Add background thread (async I/O)
 */
@Slf4j
public class Consumer<K, V> {

    private static final Integer DEFAULT_MAX_BYTES = 1024;

    private final SocketChannel socketChannel;
    private final Adapter<K> keyAdapter;
    private final Adapter<V> valueAdapter;

    // TODO: Get from FetchResult like Kafka
    private ConcurrentMap<TopicPartition, Long> offsetPositionMap;

    private final int minRecords;
    private final int maxRecords;
    private final Duration duration;

    public Consumer(ConsumerConfig config, Adapter<K> keyAdapter, Adapter<V> valueAdapter) {
        this.keyAdapter = keyAdapter;
        this.valueAdapter = valueAdapter;

        this.offsetPositionMap = new ConcurrentHashMap<>();

        this.minRecords = Integer.parseInt(config.getProperty(ConsumerConfig.MAX_PARTITION_FETCH_RECORDS_CONFIG));
        this.maxRecords = Integer.parseInt(config.getProperty(ConsumerConfig.FETCH_MIN_RECORDS_CONFIG));
        this.duration = Duration.ofMillis(Integer.parseInt(config.getProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG)));

        try {
            InetSocketAddress serverAddress = new InetSocketAddress("localhost", Constant.DEFAUTL_BROKER_PORT);
            this.socketChannel = SocketChannel.open(serverAddress);
        } catch (IOException e) {
            log.error("Exception when create consumer", e);
            throw new RuntimeException(e);
        }

    }

    // TODO: Implement
    public Integer position(TopicPartition topicPartition) {
        return 0;
    }

    // TODO: Implement
    public void subscribe(Collection<String> topics) {
//        for (String topic : topics) {
//            offsetPositionMap.put(topic, 0L);
//        }
    }

    // TODO: Implement
    public void unsubscribe(String topic) {
    }

    // TODO: Implement
    public Set<String> subscription() {
        return null;
    }

    // TODO: Implement
    public void assign(TopicPartition topicPartition) {
        offsetPositionMap.put(topicPartition, 0L);
    }

    public Set<TopicPartition> assignment() {
        return offsetPositionMap.keySet();
    }

    /**
     * Long polling method to not block the thread, make the consumer more controllable over throughput
     * TODO: Handle sending large messages, offset 
     *
     * In Kafka, starting offset is last consumed offset.
     * Last consumed offset is defined by:
     * * manually set through seek(TopicPartition, long)
     * * automatically set as the last committed offset
     */
    public ConsumerRecordBatch<K, V> poll() {
        fetch();
        return handleFetchResponse();
    }

    // TODO: Implement
    public void seek(TopicPartition topicPartition, long offset) {

    }

    // TODO: Implement
    public void seekToBegining(Collection<TopicPartition> topicPartitions) {}

    // TODO: Implement
    public void seekToEnd(Collection<TopicPartition> topicPartitions) {}

    // TODO: Implement
    public void commitSync() {}

    // TODO: Implement
    public void commitAsync() {}

    // TODO: Implement
    public Long commited(TopicPartition topicPartition) {
        return 0L;
    }

    public void stop() {
        try {
            this.socketChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fetch() {
        try {
            TopicPartition topicPartition = offsetPositionMap.entrySet().iterator().next().getKey();
            long offset = offsetPositionMap.entrySet().iterator().next().getValue();
            // TODO: Handle partition
            PartitionFetchRequest partitionRequest = PartitionFetchRequest.newBuilder()
                    .setFetchOffset(offset)
                    .setIndex(topicPartition.getPartition())
                    .setMaxRecords(this.maxRecords)
//                    .setMaxBytes(maxBytes)
                    .build();
            TopicFetchRequest topicRequest = TopicFetchRequest.newBuilder()
                    .setPartition(partitionRequest)
                    .setName(topicPartition.getTopic())
                    .build();
            FetchRequest fetchRequest = FetchRequest.newBuilder()
                    .setMaxWaitTime(this.duration.toMillis())
                    .setMinRecords(this.minRecords)
                    .setTopic(topicRequest)
                    .build();
            int msgSize = fetchRequest.getSerializedSize();

            ByteBuffer buffer = ByteBuffer.allocate(msgSize + 4);
            buffer.putInt(msgSize);
            buffer.put(fetchRequest.toByteArray());
            buffer.flip();

            socketChannel.write(buffer);
        } catch (Exception e) {
            log.error("Exception when write to socket channel", e);
        }
    }

    private ConsumerRecordBatch<K, V> handleFetchResponse() {
        ByteBuffer sizeByteBuffer = readFromChannel(4);
        if (sizeByteBuffer == null) {
            return null;
        }
        int msgSize = sizeByteBuffer.getInt();
        ByteBuffer message = readFromChannel(msgSize);

        try {
            FetchResponse response = FetchResponse.parseFrom(message.array());
            return transformFetchResponseToRecords(response);
        } catch (InvalidProtocolBufferException e) {
            log.error("Cannot parse poll result: ", e);
            return null;
        }
    }

    private ByteBuffer readFromChannel(int size) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);

        try {
            int byteRead = socketChannel.read(byteBuffer);
            if (byteRead == -1) {
                closeChannel(socketChannel);
                return null;
            } else if (byteRead == 0) {
                return null;
            }
            byteBuffer.flip();
            return byteBuffer;

        } catch (IOException e) {
            log.error("Read from channel error: ", e);
            return null;
        }
    }

    private ConsumerRecordBatch<K, V> transformFetchResponseToRecords(FetchResponse fetchResponse) {
        RecordBatch recordBatch = fetchResponse.getTopic().getPartition().getRecordBatch();

        ConsumerRecordBatch<K, V> consumerRecordBatch = new ConsumerRecordBatch<>();
        for (var record : recordBatch.getRecordsList()) {
            ConsumerRecord<K, V> consumerRecord = new ConsumerRecord<>();
            consumerRecord.setTopic(fetchResponse.getTopic().getName())
                    .setPartition(fetchResponse.getTopic().getPartition().getIndex())
                    .setKey(keyAdapter.deserialize(record.getKey()))
                    .setValue(valueAdapter.deserialize(record.getValue()))
                    .setTimestamp(record.getTimestamp());
            consumerRecordBatch.add(consumerRecord);
        }
        return consumerRecordBatch;
    }

    private void closeChannel(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            log.error("Failed to close channel", e);
        }
    }

}
