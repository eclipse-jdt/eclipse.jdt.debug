import java.util.Arrays;
import java.util.function.Predicate;

public class Bug560392 {

	public static Predicate<String> breakpointMethod(String key) {
		return (s) -> {
			System.out.println("Key" + key);
			return s.contains(key);
		};
	}

	public static void main(String[] args) {
		Arrays.asList("111", "222", "aaa").stream().filter(breakpointMethod("a")).count();
	}
}
