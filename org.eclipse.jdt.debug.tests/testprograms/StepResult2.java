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

public class StepResult2 {
	public static void main(String[] args) {
		f();
	}

	private static void f() {
		try {
			g();
		} catch (Exception e) {
			// empty
		}
	}

	private static void g() {
		try {
			h();
		} finally {
			"".length();
		}
	}

	private static void h() {
		i();
	}

	private static void i() {
		throw new RuntimeException("hi"); // bp6
	}
}
