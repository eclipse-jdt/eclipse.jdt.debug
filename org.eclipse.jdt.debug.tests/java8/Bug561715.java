import java.util.Arrays;
import java.util.function.Predicate;

public class Bug561715 {
	public static void main(String[] args) {
		String y = "111";
		Arrays.asList("111", "222", "aaa").stream().filter(a -> a.equals(y)).count();
	}
}
