/*******************************************************************************
 * Copyright (c) 2016 Till Brychcy and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/

public class StepResult1 {
	class Nested {
		String f() {
			return "f-" + g();
		}
	}

	// private to force usage of a synthetic accessor
	private String g() {
		String prefix = "g-";
		String val1 = prefix + h();
		String val2 = j(false);
		return val1 + "-" + val2; // bp2C
	}

	String h() {
		String prefix = "h-"; // bp1
		try {
			return i(false);
		} catch (Exception e) {
			// when the thread is stopped here, it is exactly 5 bytes after the i(false) invocation in the byte code
			return prefix + e.getMessage();
		}
	}

	String i(boolean flag) {
		if (flag) {
			return "i";
		}
		String value = i(true); // bp3
		throw new RuntimeException(value);
	}

	String j(boolean flag) {
		if (flag) {
			return "j";
		}
		String value = j(true); // bp2A
		return value; // bp2B
	}

	static String x = "x";

	public static String s() {
		return "bla"; // bp4
	}

	interface I {
		String get();
	}

	public void testViaInterface() {
		I i = new I() {
			public String get() {
				return "bla"; // bp5
			};
		};
		i.get();
	}
	public static void main(String[] args) {
		new StepResult1().new Nested().f();
		s();
		"".length();
		new StepResult1().testViaInterface();;
	}
}
