//package com.github.bananeee.distributedlog.client;
//
//import com.github.bananeee.distributedlog.common.PartitionProduce;
//import com.github.bananeee.distributedlog.common.ProduceAckType;
//import com.github.bananeee.distributedlog.common.ProduceRequest;
//import com.github.bananeee.distributedlog.common.TopicProduce;
//import com.google.protobuf.ByteString;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.channels.SocketChannel;
//
//@Slf4j
//public class Publisher {
//
//    public static final Integer PORT = 8765;
//
//    private final SocketChannel socketChannel;
//
//    public Publisher() {
//        try {
//            InetSocketAddress serverAddress = new InetSocketAddress("localhost", PORT);
//            this.socketChannel = SocketChannel.open(serverAddress);
//        } catch (IOException e) {
//            log.error("Exception when create publisher", e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void publish(String topic, String partition, String message) {
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
//
//    public void stop() {
//        try {
//            socketChannel.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
