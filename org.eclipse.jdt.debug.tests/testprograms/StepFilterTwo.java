public class StepFilterTwo {

	private StepFilterThree sf3;

	public StepFilterTwo() {
		sf3 = new StepFilterThree();
	}

	protected void go() {
		sf3.go();
	}
	
	void test() {
		System.out.println("StepFilterTwo.test()");
	}
}

