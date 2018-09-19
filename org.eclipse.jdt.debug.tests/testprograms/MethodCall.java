/*******************************************************************************
 * Copyright (c) 2018 Simeon Andreev and others.
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

public class MethodCall {

	private int i;
	private int sum = 0;

	public static void main(String[] args) {
		MethodCall mc = new MethodCall();
		mc.go();
	}

	public void go() {
		calculateSum();
	}

	protected void calculateSum() {
		sum += i;
	}
}
