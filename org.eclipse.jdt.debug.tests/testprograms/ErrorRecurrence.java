
public class ErrorRecurrence {

	public static void main(String[] args) {
		m0(); // L5
	}
	static void m0() {
		try {
			m1();
		} finally {
			System.out.println("finally");
		} // L12
	}

	static void m1() {
		try {
			m2();
		} catch (Error e) {
			System.out.println("caught");
			throw e; // L20
		}
	}

	static void m2() {
		System.out.println("before throw");
		throw new Error(); // L26
	}
}
