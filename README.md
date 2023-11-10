# AIO 
AIO is a java library that provides an easy to use asynchronous api based on promises
(as seen in javascript).

The execution environment is based on a ThreadPoolExecutor. Asynchronous IO is based on NIO.

The following Example illustrates how the library works. It prints Hello World after 500ms

```java
Delay.milliseconds(500).then(value -> {
    System.out.println("Hello World");
    return null;
});
```

The following example illustrates the basic implementation of a webserver

```java
ServerSocket.listen(new InetSocketAddress("0.0.0.0", 8080)).applyCallback((exc, server) -> {
    if (exc != null) {
        exc.printStackTrace();
        return;
    }

    server.accept().chain(new CompletionFunc<Socket, Socket>() {
        @Override
        public Promise<Socket> invoke(Throwable exc, Socket sock) {
            if (exc != null) {
                exc.printStackTrace();
            } else {
                ByteBuffer readBuf = ByteBuffer.allocate(4096);
                ByteBuffer writeBuf = ByteBuffer.wrap("HTTP/1.0 200 OK\r\nContent-Type: text/plain\r\n\r\nHello World\r\n".getBytes(StandardCharsets.UTF_8));

                sock.read(readBuf)
                        .then(n -> sock.write(writeBuf))
                        .then(v -> sock.close())
                        .exceptCallback(ex -> ex.printStackTrace());
            }
            return server.accept().chain(this);
        }
    });
});
```
