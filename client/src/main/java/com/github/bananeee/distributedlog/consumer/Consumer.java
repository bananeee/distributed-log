package com.github.bananeee.distributedlog.consumer;

import com.github.bananeee.distributedlog.common.PartitionFetchRequest;
import com.github.bananeee.distributedlog.common.TopicFetchRequest;
import com.github.bananeee.distributedlog.common.constant.Constant;
import com.github.bananeee.distributedlog.common.serialization.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TODO: Support consumer group, commit sync/async
 */
@Slf4j
public class Consumer<K, V> {

    private static final Integer DEFAULT_MAX_BYTES = 1024;

    private final SocketChannel socketChannel;
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;
    // TODO: Get from FetchResult like Kafka
    private ConcurrentMap<String, Long> offsetPositionMap;

    public Consumer(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;

        this.offsetPositionMap = new ConcurrentHashMap<>();

        try {
            InetSocketAddress serverAddress = new InetSocketAddress("localhost", Constant.DEFAUTL_BROKER_PORT);
            this.socketChannel = SocketChannel.open(serverAddress);
        } catch (IOException e) {
            log.error("Exception when create consumer", e);
            throw new RuntimeException(e);
        }

    }

    // TODO: Implement
    public Integer position(String topic, int partition) {
        return 0;
    }

    /**
     * Long polling method to not block the thread, make the consumer more controllable over throughput
     * @param topic
     * @param partition
     * @param maxBytes
     * @param duration
     */
    public void poll(String topic, int partition, int maxBytes, Duration duration) {
        long offset = offsetPositionMap.get(getKey(topic, partition));
        maxBytes = maxBytes == 0 ? DEFAULT_MAX_BYTES : maxBytes;

        PartitionFetchRequest partitionRequest = PartitionFetchRequest.newBuilder()
                .setFetchOffset(offset)
                .setIndex(partition)
                .setMaxBytes(maxBytes)
                .build();
        TopicFetchRequest topicRequest = TopicFetchRequest.newBuilder()
                .setPartition(partitionRequest)
                .setName(topic)
                .build();
        int msgSize = topicRequest.getSerializedSize();

        try {
            // TODO: Handle sending large messages
            ByteBuffer buffer = ByteBuffer.allocate(msgSize + 4);
            buffer.putInt(msgSize);
            buffer.put(topicRequest.toByteArray());
            buffer.flip();

            socketChannel.write(buffer);
        } catch (Exception e) {
            log.error("Exception when poll", e);
        }
    }

    private String getKey(String topic, int partition) {
        return topic + partition;
    }
}
