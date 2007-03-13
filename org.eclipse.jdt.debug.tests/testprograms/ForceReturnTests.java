/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.Vector;

public class ForceReturnTests {
	
	public static void main(String[] args) {
		new ForceReturnTests().doit();
		System.out.println("done");
	}
	
	public void doit() {
		int x = returnInt(10);
		System.out.println(x);
		String s = returnString("prefix");
		System.out.println(s);
		Object v = returnVector();
		System.out.println(v);
	}
	
	public int returnInt(int y) {
		int x = 10;
		x = x + y;
		return x;
	}
	
	public String returnString(String prefix) {
		String x = "suffix";
		return prefix + x;
	}
	
	public Object returnVector() {
		Vector v = new Vector();
		v.add("one");
		v.add("two");
		return v;
	}
}