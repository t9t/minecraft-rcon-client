package com.github.t9t.minecraftrconclient;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class RconClient implements Closeable {
    private static final int AUTHENTICATION_FAILURE_ID = -1;
    private static final Charset PAYLOAD_CHARSET = StandardCharsets.US_ASCII;

    private static final int TYPE_COMMAND = 2;
    private static final int TYPE_AUTH = 3;

    private final SocketChannel socketChannel;
    private final AtomicInteger currentRequestId;

    private RconClient(SocketChannel socketChannel) {
        this.socketChannel = Objects.requireNonNull(socketChannel, "socketChannel");
        this.currentRequestId = new AtomicInteger(1);
    }

    public static RconClient open(String host, int port, String password) {
        SocketChannel socketChannel;
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        } catch (IOException e) {
            throw new RconClientException("Failed to open socket to " + host + ":" + port, e);
        }

        RconClient rconClient = new RconClient(socketChannel);
        try {
            rconClient.authenticate(password);
        } catch (Exception authException) {
            try {
                rconClient.close();
            } catch (Exception closingException) {
                authException.addSuppressed(closingException);
            }
            throw authException;
        }
        return rconClient;
    }

    public String sendCommand(String command) {
        return send(TYPE_COMMAND, command);
    }

    @Override
    public void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            throw new RconClientException("Failed to close socket channel", e);
        }
    }

    private void authenticate(String password) {
        send(TYPE_AUTH, password);
    }

    private String send(int type, String payload) {
        int requestId = currentRequestId.getAndIncrement();

        ByteBuffer buffer = toByteBuffer(requestId, type, payload);
        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
            throw new RconClientException("Failed to write " + buffer.capacity() + " bytes", e);
        }

        ByteBuffer responseBuffer = readResponse();
        int responseId = responseBuffer.getInt();

        if (responseId == AUTHENTICATION_FAILURE_ID) {
            throw new AuthFailureException();
        }

        if (responseId != requestId) {
            throw new RconClientException("Sent request id " + requestId + " but received " + responseId);
        }

        @SuppressWarnings("unused")
        int responseType = responseBuffer.getInt();

        byte[] bodyBytes = new byte[responseBuffer.remaining()];
        responseBuffer.get(bodyBytes);
        return new String(bodyBytes, PAYLOAD_CHARSET);
    }

    private ByteBuffer readResponse() {
        int size = readData(Integer.BYTES).getInt();
        ByteBuffer dataBuffer = readData(size - (2 * Byte.BYTES));
        ByteBuffer nullsBuffer = readData(2 * Byte.BYTES);

        byte null1 = nullsBuffer.get(0);
        byte null2 = nullsBuffer.get(1);

        if (null1 != 0 || null2 != 0) {
            throw new RconClientException("Expected 2 null bytes but received " + null1 + " and " + null2);
        }

        return dataBuffer;
    }

    private ByteBuffer readData(int size) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        int readCount;
        try {
            readCount = socketChannel.read(buffer);
        } catch (IOException e) {
            throw new RconClientException("Failed to read " + size + " bytes", e);
        }

        if (readCount != size) {
            throw new RconClientException("Expected " + size + " bytes but received " + readCount);
        }

        buffer.position(0);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    private static ByteBuffer toByteBuffer(int requestId, int type, String payload) {
        ByteBuffer buffer = ByteBuffer.allocate((3 * Integer.BYTES) + payload.length() + (2 * Byte.BYTES));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt((2 * Integer.BYTES) + payload.length() + (2 * Byte.BYTES));
        buffer.putInt(requestId);
        buffer.putInt(type);
        buffer.put(payload.getBytes(PAYLOAD_CHARSET));
        buffer.put((byte) 0);
        buffer.put((byte) 0);

        buffer.position(0);
        return buffer;
    }
}
