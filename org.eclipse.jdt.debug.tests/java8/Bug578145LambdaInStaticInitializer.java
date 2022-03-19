import java.util.Arrays;
import java.util.List;

public class Bug578145LambdaInStaticInitializer {

	static {
		int numberInStaticInitializer = 1;
		List<String> staticList = Arrays.asList("Lambda");

		staticList.stream().forEach((name) -> {
			int numberInLambda = 10;
			System.out.println(name); // Add breakpoint here
		});
	}

	public static void main(String[] args) {
		Bug578145LambdaInStaticInitializer instance = new Bug578145LambdaInStaticInitializer();
	}
}
