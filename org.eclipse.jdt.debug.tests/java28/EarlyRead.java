/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************/
public class EarlyRead {

	static class Base {
		Base() {
			System.out.println("Base");
		}
	}

	static class Derived extends Base {
		int x;
		float y;
		boolean b;
		char c;
		String name;

		Derived(int seed) {
			b = true;
			IO.print(b);
			super();
			y = x + 1;
			x = seed * 2;
			name = "d" + y;

			c = 's';
			int sum = x + x;

			IO.print(x + "," + y + "," + name + "," + sum + c + b);
		}
	}

	static class Derived2 extends Base {
		int x; float y; boolean b; char c; String name;
		Derived2(int seed) { seed++; super(); }
	}

	static class WithInitializer extends Base {
		int a = 1;                      // initializer above the constructor


		WithInitializer() {             // line 57
			IO.print(a);
		}
	}

	public static void main(String[] args) {
		new Base();
		new Derived2(1);
		new WithInitializer();
		new Derived(5);
		new Derived(1);
	}
}
