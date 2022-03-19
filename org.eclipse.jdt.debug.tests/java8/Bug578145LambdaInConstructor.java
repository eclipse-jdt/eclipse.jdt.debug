import java.util.Arrays;
import java.util.List;

public class Bug578145LambdaInConstructor {
	List<String> names;

	public Bug578145LambdaInConstructor(List<String> originalNames) {
		this.names = originalNames;
		int localInConstructor = 1;

		this.names.stream().forEach((name) -> {
			int localInLambda = 10;
			System.out.println(name); // Add breakpoint here
		});
	}

	public static void main(String[] args) {
		Bug578145LambdaInConstructor instance = new Bug578145LambdaInConstructor(Arrays.asList("Lambda"));
	}
}
