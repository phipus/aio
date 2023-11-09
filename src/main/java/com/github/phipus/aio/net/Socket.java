package com.github.phipus.aio.net;
import com.github.phipus.aio.Promise;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Socket {

    private final AsynchronousSocketChannel ch;
    public Socket(AsynchronousSocketChannel ch) {
        this.ch = ch;
    }

    public Promise<Integer> read(ByteBuffer buf) {
        return new Promise<>(((resolve, reject) -> {
            ch.read(buf, null, new CompletionHandler<>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    resolve.invoke(result);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    reject.invoke(exc);
                }
            });
        }));
    }

    public Promise<Integer> write(ByteBuffer buf) {
        return new Promise<>(((resolve, reject) -> {
            ch.write(buf, null, new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    resolve.invoke(result);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    reject.invoke(exc);
                }
            });
        }));
    }

    public Promise<Void> close() {
        return new Promise<Void>((resolve, reject) -> {
            try {
                ch.close();
                resolve.invoke(null);
            } catch (IOException e) {
                reject.invoke(e);
            }
        });
    }
}
