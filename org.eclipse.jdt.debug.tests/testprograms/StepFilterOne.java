/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

public class StepFilterOne {

	public static void main(String[] args) {
		StepFilterOne sf1 = new StepFilterOne();
		sf1.go();
	}
	
	private void go() {
		StepFilterTwo sf2 = new StepFilterTwo();
		sf2.test();
		sf2.go();
	}
}

