package com.github.bananeee.distributedlog.producer;

import com.github.bananeee.distributedlog.common.PartitionProduce;
import com.github.bananeee.distributedlog.common.ProduceAckType;
import com.github.bananeee.distributedlog.common.ProduceRequest;
import com.github.bananeee.distributedlog.common.Record;
import com.github.bananeee.distributedlog.common.RecordBatch;
import com.github.bananeee.distributedlog.common.RecordDto;
import com.github.bananeee.distributedlog.common.TopicProduce;
import com.github.bananeee.distributedlog.common.constant.Constant;
import com.github.bananeee.distributedlog.common.serialization.Serializer;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.time.LocalDate;

/**
 * TODO: Handle offset mode, partition
 * TODO: Buffer pool to store records that haven't been transmitted to server
 * TODO: Configurable
 * A kafka like producer
 * <pre>
 * {@code
 * // Producer
 * Properties props = new Properties();
 * props.put("bootstrap.servers", "localhost:9092");
 * props.put("transactional.id", "my-transactional-id");
 * Producer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
 * producer.initTransactions();
 *
 * try {
 *     producer.beginTransaction();
 *     for (int i = 0; i < 100; i++)
 *          producer.send(new ProducerRecord<>("my-topic", Integer.toString(i), Integer.toString(i)));
 *          producer.commitTransaction();
 *     } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
 *         // We can't recover from these exceptions, so our only option is to close the producer and exit.
 *         producer.close();
 *     } catch (KafkaException e) {
 *         // For all other exceptions, just abort the transaction and try again.
 *         producer.abortTransaction();
 *     }
 *     producer.close();
 * }
 * }
 * </pre>
 */
@Slf4j
public class Producer<K, V> {

    private final SocketChannel socketChannel;
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;
    private final ProduceAckType ackType;

    public Producer(Serializer<K> keySerializer, Serializer<V> valueSerializer, ProduceAckType ackType) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.ackType = ackType;

        try {
            InetSocketAddress serverAddress = new InetSocketAddress("localhost", Constant.DEFAUTL_BROKER_PORT);
            this.socketChannel = SocketChannel.open(serverAddress);
        } catch (IOException e) {
            log.error("Exception when create publisher", e);
            throw new RuntimeException(e);
        }
    }

    // TODO: async send with buffer pool
    // TODO: Handle ack behavior
    public void send(ProducerRecord<K, V> producerRecord) {
        RecordDto record = RecordDto.newBuilder()
                .setKey(keySerializer.serialize(producerRecord.getKey()))
                .setValue(valueSerializer.serialize(producerRecord.getValue()))
                .setTimestamp(Instant.now().toEpochMilli())
                .build();
        RecordBatch batch = RecordBatch.newBuilder()
                .addRecords(record)
                .build();

        PartitionProduce partitionDto = PartitionProduce.newBuilder()
                .setIndex(0)
                .setBatch(batch)
                .build();
        TopicProduce topicDto = TopicProduce.newBuilder()
                .setName(producerRecord.getTopic())
                .setPartition(partitionDto)
                .build();
        ProduceRequest produceRequest = ProduceRequest.newBuilder()
                .setAcks(this.ackType)
                .setTimeoutMs(-1)
                .setTopic(topicDto)
                .build();
        int msgSize = produceRequest.getSerializedSize();

        try {
            // TODO: Handle sending large messages
            ByteBuffer buffer = ByteBuffer.allocate(msgSize + 4);
            buffer.putInt(msgSize);
            buffer.put(produceRequest.toByteArray());
            buffer.flip();

            socketChannel.write(buffer);
        } catch (Exception e) {
            log.error("Exception when publish", e);
        }
    }

//    public void samplePublish(String topic, String partition, String message) {
//        System.out.println("Publishing message: " + message);
//
//        PartitionProduce partitionDto = PartitionProduce.newBuilder()
//                .setIndex(0)
//                .setContent(ByteString.copyFromUtf8(message))
//                .build();
//        TopicProduce topcicDto = TopicProduce.newBuilder()
//                .setName(topic)
//                .setPartition(partitionDto)
//                .build();
//        ProduceRequest produceRequest = ProduceRequest.newBuilder()
//                .setAcks(ProduceAckType.NO_ACK)
//                .setTimeoutMs(-1)
//                .setTopic(topcicDto)
//                .build();
//
//        int msgSize = produceRequest.getSerializedSize();
//
//        try {
//            // TODO: Handle sending large messages
//            ByteBuffer buffer = ByteBuffer.allocate(msgSize + 4);
//            buffer.putInt(msgSize);
//            buffer.put(produceRequest.toByteArray());
//            buffer.flip();
//
//            socketChannel.write(buffer);
//        } catch (Exception e) {
//            log.error("Exception when publish", e);
//        }
//    }

    public void stop() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
