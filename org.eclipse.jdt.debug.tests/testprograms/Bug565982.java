/*******************************************************************************
 * Copyright (c) 2020 Simeon Andreev and others.
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

public class Bug565982 {

	public static void main(String[] args) {
		Bug565982 o = new Bug565982();
		int n = 5;
		for (int i = 0; i < n; ++i) {
			o.breakpointMethod();
		}
	}

	public void breakpointMethod() {
		System.out.println("set a method entry breakpoint with a hit count in this method");
	}
}
