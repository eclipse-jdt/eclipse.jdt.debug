import java.util.Vector;

public class MultiThreadedException {

	public static void main(String[] args) {
		MultiThreadedException mte = new MultiThreadedException();
		mte.go();
	}
	
	private void go() {
		Thread.currentThread().setName("1stThread");
		
		Thread secondThread = new Thread(new Runnable() {
			public void run() {
				generateNPE();
			}
		});
		secondThread.setName("2ndThread");
		secondThread.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		
		generateNPE();
	}
	
	void generateNPE() {
		Vector vector = null;
		if (1 > 2) {
			vector = new Vector();
		}
		vector.add("item");
	}
}
