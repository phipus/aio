package com.github.phipus.aio.net;

import com.github.phipus.aio.Promise;
import com.github.phipus.aio.iter.AsyncIterator;
import com.github.phipus.aio.iter.IteratorItem;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class SocketAcceptor implements AsyncIterator<Socket> {
    private final AsynchronousServerSocketChannel ch;
    private boolean stopped = false;

    public SocketAcceptor(AsynchronousServerSocketChannel ch) {
        this.ch = ch;
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public Promise<IteratorItem<Socket>> next() {
        if (stopped)
            return Promise.resolve(new IteratorItem<>(null, false));
        return new Promise<>((resolve, reject) -> {
            ch.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void failed(Throwable exc, Void attachment) {
                    reject.invoke(exc);
                }

                @Override
                public void completed(AsynchronousSocketChannel result, Void attachment) {
                    resolve.invoke(new IteratorItem<>(new Socket(result), true));
                }
            });
        });
    }
}
