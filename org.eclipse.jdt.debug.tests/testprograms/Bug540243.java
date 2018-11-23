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

public class Bug540243 {

	public static void main(String[] args) throws InterruptedException {
		Thread t = new Thread(new Runnable() {
			// method must be synchronized, so that the thread owns a monitor
			public synchronized void run() {
				breakpointMethod();
			}
		});
		t.start();
		t.join();
	}

	public static void breakpointMethod() {
		System.out.println("set a breakpoint here");
	}
}
