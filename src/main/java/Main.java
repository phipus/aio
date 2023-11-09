import com.github.phipus.aio.Delay;
import com.github.phipus.aio.Loop;
import com.github.phipus.aio.Promise;

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

        Delay.milliseconds(1000).then((value) -> {
            System.out.println("Hello World");
            return Delay.milliseconds(2000);
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
