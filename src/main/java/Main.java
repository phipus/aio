import com.github.phipus.aio.*;
import com.github.phipus.aio.net.ServerSocket;
import com.github.phipus.aio.net.Socket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        final boolean fail = args.length != 0 && Objects.equals(args[0], "1");

        Promise.all()
                .add(Promise.resolve("Hello"))
                .add(Delay.milliseconds(500)
                        .then(value -> Promise.resolve("World"))
                ).build().then(values -> {
                    System.out.printf("%s %s\n", values.get(0), values.get(1));
                    return Promise.resolve(null);
                });


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


        Delay.milliseconds(500).then(value -> {
            System.out.println("Hello World");
            return null;
        });

        Delay.milliseconds(1000).then((value) -> {
            System.out.println("Hello World");
            return Delay.milliseconds(10000);
        }).then((value) -> {
            if (fail)
                throw new RuntimeException("blöd gelaufen");
            return Promise.resolve(null);
        }).then(((value) -> {
            System.out.println("Hello World again");
            return Promise.resolve(null);
        })).chain((exc, value) -> {
            if (exc != null) {
                exc.printStackTrace();
            }
            Loop.quit();
            return Promise.resolve(null);
        });
    }

}

