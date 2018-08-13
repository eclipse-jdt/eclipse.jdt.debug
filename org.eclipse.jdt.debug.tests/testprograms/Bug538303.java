/*******************************************************************************
 * Copyright (c) 2018 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/

public class Bug538303 {

	public static void main(String[] args) throws InterruptedException {
		Thread t = new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i < 2; ++i) {
					sleep(250);
					breakpointMethod();
				}
			}
		});
		t.start();
		t.join();
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
