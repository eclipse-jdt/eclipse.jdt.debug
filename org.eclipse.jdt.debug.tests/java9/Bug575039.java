public class Bug575039 {
	public static void main(String[] args) {
		Thread t = new Thread("Hello bug 575039");
		t.start();
	}
}