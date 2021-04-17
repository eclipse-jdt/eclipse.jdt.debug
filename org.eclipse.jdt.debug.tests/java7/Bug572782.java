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

	public static void main(String[] args) {
		new ExtendedGeneric();
		new SimpleGeneric();
	}
}