import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Bug567801 {
	public static void main(String[] args) {
		testBody();
	}
	private static <T extends Closeable & Serializable> void testBody() {
		List<Integer> numbers = new ArrayList<>();
		List<List<Integer>> listOfNumberList = new ArrayList<>();
		List<? extends Number> extendsList = Arrays.asList(10,20);
		List<? super Integer> superList = Arrays.asList(10,20);
		List<?> wildList = Arrays.asList(10,20);
		List<T> intersectionList = Collections.emptyList();
		List<long[]> parrayList = Arrays.asList(new long[] {100L});
		List<String[]> arrayList = Arrays.asList(new String[] {"100"}, new String[] {"200"});
		IntStream stream = IntStream.of(10);

		numbers.add(11);
		listOfNumberList.add(numbers);
		
		System.out.println(numbers);
	}
}