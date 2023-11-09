import com.github.phipus.aio.Delay;
import com.github.phipus.aio.Loop;
import com.github.phipus.aio.Promise;
import com.github.phipus.aio.ResolveFunc;
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


        ServerSocket.listen(new InetSocketAddress("0.0.0.0", 8080)).then(server -> {
            ResolveFunc<Socket, Socket> handleSocket = new ResolveFunc<Socket, Socket>() {
                @Override
                public Promise<Socket> invoke(Socket sock) {
                    sock.write(ByteBuffer.wrap("HTTP/1.0 200 OK\r\nContent-Type: text/plain\r\n\r\nHello World\r\n".getBytes(StandardCharsets.UTF_8))).then(v -> {
                        sock.close();
                        return Promise.resolve(null);
                    });
                    return server.accept().then(this);
                }
            };

            server.accept().then(handleSocket).applyCallback((exc, value) -> {
                if (exc != null) exc.printStackTrace();
            });

            return Promise.resolve(null);
        }).applyCallback(((exc, value) -> {
            if (exc != null) exc.printStackTrace();
        }));

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

