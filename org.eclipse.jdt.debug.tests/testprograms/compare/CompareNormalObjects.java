/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package compare;
import java.util.*;

class A {
	public int custom;
	public A(int custom) {
		this.custom = custom;
	}
}
public class CompareNormalObjects {
	public static void main(String[] ecs) {
		A a1 = new A(1);
		A a2 = new A(2);
		A a3 = a1;
		Integer i1 = Integer.valueOf(10);
		Integer i2 = Integer.valueOf(10);
		Float f1 = Float.valueOf(12f);
		Float f2 = Float.valueOf(112f);
		int p = 100;
	}
}