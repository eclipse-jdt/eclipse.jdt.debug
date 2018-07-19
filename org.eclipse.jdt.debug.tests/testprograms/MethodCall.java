/*******************************************************************************
 * Copyright (c) 2018 Simeon Andreev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
