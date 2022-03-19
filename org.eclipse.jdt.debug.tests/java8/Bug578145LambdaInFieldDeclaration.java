import java.util.function.Consumer;

public class Bug578145LambdaInFieldDeclaration {
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			int numberInRunnable = 1;

			Consumer<Integer> myConsumer = (lambdaArg) -> {
				int numberInLambda = 10;
				System.out.println("id = " + lambdaArg); // Add breakpoint here
			};
			myConsumer.accept(numberInRunnable);
		}
	};

	public static void main(String[] args) {
		Bug578145LambdaInFieldDeclaration instance = new Bug578145LambdaInFieldDeclaration();
		instance.runnable.run();
	}
}
