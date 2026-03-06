package com.github.bananeee.distributedlog.common.serialization;

import com.google.protobuf.ByteString;

public class StringSerializer extends Serializer<String> {

    @Override
    public ByteString serialize(String data) {
        return ByteString.copyFromUtf8(data);
    }
}
