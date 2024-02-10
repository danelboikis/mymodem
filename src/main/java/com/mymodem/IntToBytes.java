package com.mymodem;

import java.nio.ByteBuffer;

public class IntToBytes {
    public static byte[] intToBytes(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }
}
