public class StepFilterOne {

	public static void main(String[] args) {
		StepFilterOne sf1 = new StepFilterOne();
		sf1.go();
	}
	
	private void go() {
		StepFilterTwo sf2 = new StepFilterTwo();
		sf2.test();
		sf2.go();
	}
}

