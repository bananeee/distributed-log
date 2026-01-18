package com.github;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
public class Worker implements Runnable {

    private Selector selector;
    private ByteBuffer byteBuffer;
    private CharsetDecoder charsetDecoder;
    private CharBuffer charBuffer;

    public Worker() {
        try {
            selector = Selector.open();

            byteBuffer = ByteBuffer.allocate(1024);
            Charset charset = StandardCharsets.UTF_8;
            charsetDecoder = charset.newDecoder();
            charBuffer = CharBuffer.allocate(1024);
        } catch (IOException e) {
            log.error("Failed to open selector", e);
        }
    }

    public void registerChannel(SocketChannel channel) {
        try {
            channel.register(selector, SelectionKey.OP_READ);
            selector.wakeup();
        } catch (ClosedChannelException e) {
            log.error("Failed to register channel", e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                // use Iterator to easily remove processed keys
                var iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (!key.isValid()) {
                        iterator.remove();
                        continue;
                    }

                    if (key.isReadable()) {
                        try {
                            printMsg(key);
                        } catch (IOException e) {
                            closeChannel(key);
                        }
                    }

                    iterator.remove(); // Remove the key to avoid re-processing
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void printMsg(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            int bytesRead = channel.read(byteBuffer);
            if (bytesRead == -1) {
                closeChannel(key);
                break;
            }
            if (bytesRead == 0) {
                break;
            }
            byteBuffer.flip();
            readFromBuffer(stringBuilder);
            byteBuffer.compact();
        }
        log.info("Received message: {}", stringBuilder);
    }

    private void closeChannel(SelectionKey key) {
        try {
            key.cancel();
            key.channel().close();
        } catch (IOException e) {
            log.error("Failed to close channel", e);
        }
    }

    private void readFromBuffer(StringBuilder stringBuilder) {
        charsetDecoder.decode(byteBuffer, charBuffer, false);
        charBuffer.flip();
        stringBuilder.append(charBuffer);
        charBuffer.clear();
    }

}
