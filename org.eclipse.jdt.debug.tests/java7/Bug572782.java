import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Bug572782 {
	static class Generic<T> {
		
	}
	
	static class ExtendedGeneric<T extends Generic<ExtendedGeneric<T>>> {
		public ExtendedGeneric() {
			super();
			System.out.println("created " + this);
		}
	}

	static class SimpleGeneric<T extends Generic<T>> {
		public SimpleGeneric() {
			super();
			System.out.println("created " + this);
		}
	}

	static class NthGeneric<T extends Generic<ExtendedGeneric<T>>> {
		public List<T> items;

		public NthGeneric() {
			super();
			System.out.println("created " + this);
			this.items = new ArrayList<>();
		}
		
		public static void nthGeneric() {
			System.out.println("at nthGeneric");
		}
	}
	
	static <T extends List<String>> void foo(T list) {
		System.out.println("at " + list);
	}

	<T extends Set<String>> void boo(T set) {
		System.out.println("at " + set);
	}

	public static void main(String[] args) {
		new ExtendedGeneric();
		new SimpleGeneric();
		new NthGeneric();
		foo(Arrays.asList("1"));
		NthGeneric.nthGeneric();
		(new Bug572782()).boo(new HashSet<String>());
	}
}

