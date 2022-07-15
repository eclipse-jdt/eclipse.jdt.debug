/*******************************************************************************
 * Copyright (c) 2022 Simeon Andreev and others.
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

/**
 * Snippet for: Bug 575131 - Deadlock during "JDI Expression Evaluation Event Dispatch"
 */
public class SuspendVMConditionalBreakpointsTestSnippet {

	public static final int i = 50;
	public static final int j = 25;

	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int k = 0; k < 5; ++k) {
					System.out.println("thread i=" + i); // suspend VM breakpoint with condition "if (i == 50) { System.out.println(i); } return false;"
				}
			}
		});
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int k = 0; k < 5; ++k) {
					System.out.println("thread j=" + j); // suspend VM breakpoint with condition "if (j == 25) { System.out.println(j); } return false;"
				}
			}
		});
		t1.start();
		t2.start();
		t1.join();
		t2.join();
	}
}
