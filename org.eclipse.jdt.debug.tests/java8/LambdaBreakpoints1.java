import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LambdaBreakpoints1 {
	public static void main(String[] args) throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		StringBuilder s = new StringBuilder();
		CompletableFuture<?> f = CompletableFuture.completedFuture("0");
		f = f.thenApply((x) -> {
			s.append(x).append("1");
			return s.toString(); // breakpoint 1
		})
		.thenApply(x ->
			s.append("2") // breakpoint 2
		)
		.thenRun(() -> lastCall(s))
		.thenRunAsync(() -> {}, executor);
		executor.shutdown();
		f.get();
		String result = s.toString();
		System.out.println(result); // breakpoint 4
	}

	private static void lastCall(StringBuilder b) {
		b.append(C.s());
	}

	static class C {
		static String s() {
			if (Boolean.valueOf("true")) // breakpoint 3
				return "3"; // <--- breakpoint 3 should not be here
			return "???";
		}
	}
}
