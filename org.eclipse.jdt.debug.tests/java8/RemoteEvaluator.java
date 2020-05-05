import java.util.Arrays;
import java.util.function.Predicate;

public class RemoteEvaluator {
	public static final Predicate<String> P_EMPTY = s -> s.isEmpty();
	
	public static void main(String[] args) {
		(new RemoteEvaluator()).exec();
	}

	public void exec() {
		(new Inner()).run();
	}
	
	class Inner {
		private final Predicate<String> Q_EMPTY = s -> s.isEmpty();

		public void run() {
			String y = "111";
			Arrays.asList("111", "222", "aaa").stream().filter(a -> a.equals(y)).count();
		}
	}
}
