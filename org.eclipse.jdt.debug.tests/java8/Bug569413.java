import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Bug569413 {

	public static void main(String[] args) {
		new Bug569413().test();
	}

	List<TestClass> packageProcessors = Arrays.asList(new TestClass());
	Map<String, TestClass> basePackages = new HashMap<>();

	void test() {
		packageProcessors.forEach(pp -> {
			Set<String> pkgs = pp.getPackagesToMap();
			pp.getPackagesToMap().forEach(p -> {
				// just to make pkgs variable visible for evaluation
				int a = pkgs.size();
				basePackages.put(p, pp);
			});
		});

		packageProcessors.forEach(pp -> {
			pp.getPackagesToMap().forEach(p -> {
				basePackages.put(p, null);
			});
		});
	}

	static class TestClass {

		public Set<String> getPackagesToMap() {
			return new LinkedHashSet<>(Arrays.asList("ab", "b", "c"));
		}

	}
}
