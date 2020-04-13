public class Bug562056 {
	Object handler = "Hello bug 562056";

	public static void main(String[] args) {
		(new Bug562056()).run();
	}

	private Runnable r = () -> {
		String string = handler.toString(); // breakpoint here, inspect handler.toString()
		System.out.println(string);
	};

	public void run() {
		r.run();
	}
}
