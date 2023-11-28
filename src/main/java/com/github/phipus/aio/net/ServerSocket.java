package com.github.phipus.aio.net;

import com.github.phipus.aio.Promise;
import com.github.phipus.aio.iter.AsyncIterator;
import com.github.phipus.aio.iter.IteratorItem;

import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ServerSocket {
    public static Promise<ServerSocket> listen(SocketAddress local) {
        return new Promise<>((resolve, reject) -> {
            try {
                AsynchronousServerSocketChannel ch = AsynchronousServerSocketChannel.open().bind(local);
                resolve.invoke(new ServerSocket(ch));
            } catch (Throwable exc) {
                reject.invoke(exc);
            }

        });
    }

    final AsynchronousServerSocketChannel ch;

    public ServerSocket(AsynchronousServerSocketChannel ch) {
        this.ch = ch;
    }

    public Promise<Socket> accept() {
        return new Promise<>(((resolve, reject) -> {
            ch.accept(null, new CompletionHandler<>() {

                @Override
                public void completed(AsynchronousSocketChannel result, Object attachment) {
                    resolve.invoke(new Socket(result));
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    reject.invoke(exc);
                }
            });
        }));
    }

    public SocketAcceptor acceptAll() {
        return new SocketAcceptor(ch);
    }
}
