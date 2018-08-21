/*******************************************************************************
 * Copyright (c) 2017 Till Brychcy and others.
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

public class StepUncaught {
	public static void main(String[] args) {
		try {
			f(); // bp
		} catch (Throwable e) {
		}
		g();
	}

	private static void f() {
		throw new RuntimeException("caught");
	}
	private static void g() {
		throw new RuntimeException("uncaught");
	}
}
