import java.io.*;

public class TestIO {
	
	public static void main(String[] args) {
		TestIO tio = new TestIO();
		try {
			tio.testBaby();
		} catch (EOFException e) {
		}
	}

	public void testBaby() throws EOFException {
		throw new EOFException("test");
	}
}
