public class HitCountLooper {
	public static void main(String[] args) {
		int i = 0;
		while (i < 20) {
			System.out.println("Main Looping " + i);
			i++;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}