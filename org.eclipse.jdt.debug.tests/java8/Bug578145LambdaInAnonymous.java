import java.util.Arrays;
import java.util.List;

public class Bug578145LambdaInAnonymous {

	public static void main(String[] args) {
		int numberInMain = 1;

		new Runnable() {
			@Override
			public void run() {
				List<String> usersInAnonymous = Arrays.asList("Lambda");

				usersInAnonymous.stream().forEach(u -> {
					int numberInLambda = 10;
					System.out.println("user name: " + u); // Add a breakpoint here
				});
			}
		}.run();
	}
}
