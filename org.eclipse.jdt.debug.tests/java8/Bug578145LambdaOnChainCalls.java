import java.util.Arrays;
import java.util.List;

public class Bug578145LambdaOnChainCalls {
	public static void main(String[] args) {
		int numberInMain = 1;
		List<String> users = Arrays.asList("Lambda");

		users.stream().forEach(u -> {
			int numberInLambda = 10;
			System.out.println("user name: " + u); // Add a breakpoint here
		});
	}
}
