/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/**
 * WatchItemContext
 */
public class WatchItemContext {

	private class X {
		private int x = 0;
	}

	private class Y {
		private X x = new X();
		private int y = 0;
	}

	private class Z {
		private Y y = new Y();
		private int z = 0;
	}

	public static void main(String[] args) {
		new WatchItemContext().foo();
	}

	public void foo() {
		X x = new X();
		Y y = new Y();
		Z z = new Z();
		System.out.println(x + " " + y + " " + z);
	}
}