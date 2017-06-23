/*******************************************************************************
 * Copyright (c) 2017 Till Brychcy and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
