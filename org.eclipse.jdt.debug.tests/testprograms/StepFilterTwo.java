/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

public class StepFilterTwo {

	private StepFilterThree sf3;

	public StepFilterTwo() {
		sf3 = new StepFilterThree();
	}

	protected void go() {
		sf3.go();
	}
	
	void test() {
		System.out.println("StepFilterTwo.test()");
	}
}

