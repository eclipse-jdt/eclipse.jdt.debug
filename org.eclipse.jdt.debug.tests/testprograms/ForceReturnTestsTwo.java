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
import java.util.Random;
import java.util.Vector;

public class ForceReturnTestsTwo {
	
	public static void main(String[] args) {
		new ForceReturnTestsTwo().doit();
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
		x = x + y + getNumber();
		return x;
	}
	
	public int getNumber() {
		return new Random().nextInt();
	}
	
	public String returnString(String prefix) {
		String x = "suffix" + getString();
		return prefix + x;
	}
	
	public String getString() {
		return "getString()";
	}
	
	public Object returnVector() {
		System.out.println("getting vector");
		return getVector();
	}
	
	public Vector getVector() {
		Vector v = new Vector();
		v.add("one");
		v.add("two");
		return v;
	}
}