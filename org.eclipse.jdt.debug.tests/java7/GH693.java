import java.util.HashMap;
import java.util.Map;

public class GH693<U extends Number> {
	U value;

	void test(GH693<U> param, Map<String, GH693<U>> map) {
		System.out.println("test"); // conditional breakpoint here
	}

	static <E extends Number> void testMethodTypeVariable(GH693<E> param) {
		System.out.println("testMethodTypeVariable"); // conditional breakpoint here
	}

	public static void main(String[] args) {
		GH693<Integer> instance = new GH693<Integer>();
		instance.value = 5;
		Map<String, GH693<Integer>> map = new HashMap<String, GH693<Integer>>();
		map.put("key", instance);
		instance.test(instance, map);
		testMethodTypeVariable(instance);
	}
}
