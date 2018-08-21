/*******************************************************************************
 * Copyright (c) 2015 Jesper Steen Møller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *******************************************************************************/

interface A {
	default int getOne() {
		return 1;
	}
}

interface B {
	default int getOne() {
		return 2;
	}
}

public class EvalIntfSuperDefault implements A, B {
	public int getOne() {
		return 3; //bp here and inspect B.super.getOne(), ensuring it evaluates to 2
	}
	public static void main(String[] args) {
		EvalIntfSuperDefault i = new EvalIntfSuperDefault();
		System.out.println(i.getOne());
	}
}
