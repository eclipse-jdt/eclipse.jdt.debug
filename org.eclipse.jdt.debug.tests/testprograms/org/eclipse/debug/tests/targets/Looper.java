package org.eclipse.debug.tests.targets;

public class Looper {
	public void loop() {
		int i = 0;
		while (true) {
			System.out.println("Loop " + i);
			i++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
}