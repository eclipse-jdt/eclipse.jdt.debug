package org.eclipse.debug.tests.targets;

public class InfiniteLoop {
	public static void main(String[] args) {
		(new InfiniteLoop()).loop();
	}

	public void loop() {
		int i = 0;
		while (true) {
			System.out.println("Looping " + i);
			i++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
}