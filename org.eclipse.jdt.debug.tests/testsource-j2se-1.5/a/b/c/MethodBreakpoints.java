/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
package a.b.c;

import java.util.ArrayList;
import java.util.List;

public class MethodBreakpoints<T> {

	public static void main(String[] args) {
		staticTypeParameter(new ArrayList<String>());
		MethodBreakpoints<String> breakpoints = new MethodBreakpoints<String>();
		breakpoints.typeParameter("Testing");
		breakpoints.methodTypeParameter(new Integer(34));
	}
	
	public static <X> void staticTypeParameter(List<X> list) {
		System.out.println(list.isEmpty());
	}
	
	public T typeParameter(T t) {
		System.out.println(t);
        return t;
	}
	
	public <K> void methodTypeParameter(K k) {
		System.out.println(k);
	}
	
}
