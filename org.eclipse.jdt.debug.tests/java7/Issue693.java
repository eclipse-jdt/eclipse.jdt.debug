import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Issue693 {
	static class Generic<U extends Number> {

		private U u;

		public Generic(U u) {
			this.u = u;
		}

	    void test(Generic<U> generic) {
			System.out.println("tested " + this);
		}
	}

	public static void main(String[] args) {
	    Generic<Integer> generic = new Generic<>(5);
		generic.test(generic);
	}
}