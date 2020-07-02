import java.util.function.IntSupplier;

public class AnonymousEvaluator {
	public static void main(String[] args) {
		int calc = calculate(new IntSup());
	}

	public static int calculate(IntSupplier supplier) {
		return supplier.getAsInt() * 2;
	}

	private static class IntSup implements IntSupplier {

		@Override
		public int getAsInt() {
			return 10 * 20;
		}
	}

}
