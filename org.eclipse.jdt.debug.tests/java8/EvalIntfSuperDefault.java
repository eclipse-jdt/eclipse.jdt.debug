/*******************************************************************************
 * Copyright (c) 2015 Jesper Steen Møller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
