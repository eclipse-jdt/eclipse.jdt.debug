/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/
public class ClosureVariableTest_Bug542989 {
	public static void main(String[] args) {
		java.io.PrintStream out = System.out;
		java.util.function.Consumer<String> printer = (msg) -> {
			out.println(msg); //set breakpoint here
		};
		printer.accept("Hallo World!");
	}
}
