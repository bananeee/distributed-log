package com.github.bananeee.distributedlog.common.serialization;

import com.google.protobuf.ByteString;

public class StringAdapter extends Adapter<String> {

    @Override
    public ByteString serialize(String data) {
        return ByteString.copyFromUtf8(data);
    }

    @Override
    public String deserialize(ByteString bytes) {
        return bytes.toStringUtf8();
    }
}
