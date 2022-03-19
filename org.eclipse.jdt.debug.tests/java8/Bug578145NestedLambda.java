import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Bug578145NestedLambda {

	public static void main(String[] args) {
		int numberInMain = 1;

		Consumer<Integer> myConsumer = (id) -> {
			int numberInExternalLambda = 10;

			List<String> users = Arrays.asList("Lambda");
			users.stream().forEach(u -> {
				int numberInInnerLambda = 100;
				System.out.println("user name: " + u); // Add a breakpoint here
			});
		};
		myConsumer.accept(numberInMain);
	}
}
