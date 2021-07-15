import static java.lang.Math.max;

import java.util.stream.Stream;

public class Bug573589Bin {

	public static void main(String[] args) {
		max(10, 11);
		Stream.of(1,2,3).filter(i -> i > 2).map(i -> i * 2).count();
	}
}
