/*******************************************************************************
 * Copyright (c) 2016 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
