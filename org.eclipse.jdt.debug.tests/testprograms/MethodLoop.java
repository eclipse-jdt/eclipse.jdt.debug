/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

public class MethodLoop {
	
	private int i;
	private int sum = 0;

	public static void main(String[] args) {
		MethodLoop ml = new MethodLoop();
		ml.go();
	}
	
	public void go() {
		for (i = 1; i < 10; i++) {
			calculateSum();
		}
	}
	
	protected void calculateSum() {
		sum += i;
	}
}
