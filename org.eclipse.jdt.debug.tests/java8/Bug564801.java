import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class Bug564801 {
    public static void main(final String[] args) {
        Arrays.asList(1,2).sort((a, b) -> {
            return a.compareTo(b);
        });

		Arrays.<List<Integer>> asList(Arrays.asList(1), Collections.emptyList()).stream().filter(p -> {
        	Predicate<? extends List<Integer>> predicate = p1 -> p1.isEmpty();
			return p.isEmpty();
        }).count();

    }
}