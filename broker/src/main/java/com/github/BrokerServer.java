package com.github;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
public class BrokerServer {

    private static final int PORT = 8765;
    public static final int THREAD_POOL_SIZE = 10;

    private ByteBuffer byteBuffer;
    private CharBuffer charBuffer;
    private CharsetDecoder charsetDecoder;
    private StringBuilder stringBuilder;

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    private WorkerPool workerPool;

    public BrokerServer() throws IOException {
        byteBuffer = ByteBuffer.allocate(1024);
        Charset charset = StandardCharsets.UTF_8;
        charsetDecoder = charset.newDecoder();
        stringBuilder = new StringBuilder();

        // NIO setup
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, serverSocketChannel.validOps());

        workerPool = new WorkerPool();
    }


    public void start() throws IOException {
        workerPool.runAll();

        while (true) {
            System.out.println("Waiting for connections...");
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();

                if (selectionKey.isAcceptable()) {
                    workerPool.registerChannel(serverSocketChannel.accept());
                }
                iterator.remove();
            }
        }
    }

    public void stop() {
        try {
            selector.close();
            serverSocketChannel.close();
            workerPool.stop();
        } catch (IOException e) {
            log.error("Error closing broker resource", e);
        }
    }

}
