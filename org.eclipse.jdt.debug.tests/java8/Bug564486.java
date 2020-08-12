import java.util.stream.Stream;

public class Bug564486 {
    public static void main(String[] args) {
        new Runnable() {
            @Override
            public void run() {
                Stream.of(1, 2, 3).forEach(i -> {
                    System.out.println(args);
                });
            }
        }.run();
    }
}