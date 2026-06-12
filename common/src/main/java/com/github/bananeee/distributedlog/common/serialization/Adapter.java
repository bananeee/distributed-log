package com.github.bananeee.distributedlog.common.serialization;

import com.google.protobuf.ByteString;

import java.io.Closeable;
import java.io.IOException;

public abstract class Adapter<T> implements Closeable {

    public abstract ByteString serialize(T data);

    public abstract T deserialize(ByteString bytes);

    @Override
    public void close() throws IOException {}
}
