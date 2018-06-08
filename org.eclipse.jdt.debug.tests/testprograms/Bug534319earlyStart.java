/*******************************************************************************
 * Copyright (c) 2018 Simeon Andreev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/

public class Bug534319earlyStart {

	public static void main(String[] args) {
		sleep(500);
		Thread t = new Thread(new Runnable() {
			public void run() {
				sleep(250);
				breakpointMethod();
			}
		});

		// we start thread with breakpoint BEFORE all other threads are started
		t.start();

		/*
		 * Spawn threads, some stay, some sleep a bit and leave, hopefully catching problems in the add/remove logic of the Debug View.
		 */
		for (int i = 0; i < 20; i++) {
			final int index = i;
			new Thread(new Runnable() {
				public void run() {
					if (index % 3 == 1) {
						while (true) {
							sleep(250);
						}
					} else if (index % 2 == 1) {
						sleep(250);
					}
				}
			}).start();
		}
		sleep(500);
	}

	static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	public static void breakpointMethod() {
		System.out.println("set a breakpoint here");
	}
}
