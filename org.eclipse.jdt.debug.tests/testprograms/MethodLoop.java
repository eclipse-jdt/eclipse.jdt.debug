public class MethodLoop {
	
	private int i;
	private int sum = 0;

	public static void main(String[] args) {
		MethodLoop ml = new MethodLoop();
		ml.go();
	}
	
	public void go() {
		for (i = 1; i < 10; i++) {
			calculateSum();
		}
	}
	
	protected void calculateSum() {
		sum += i;
	}
}
