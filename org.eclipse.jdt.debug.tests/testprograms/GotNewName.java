/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

public class GotNewName {

	public static void main(String[] args) {
		GotNewName anObject = new GotNewName();

		try {
			anObject.throwBaby();
		} catch(NullPointerException ne) {
			// do nothing
		}

		anObject.killTime();

		try {
			anObject.throwBaby();
		} catch(NullPointerException ne) {
			// do nothing
		}

		try {
			anObject.throwAnotherBaby();
		} catch(IllegalArgumentException ie) {
			// do nothing
		}
	}

	public void throwBaby() {
		throw new NullPointerException();
	}

	public void throwAnotherBaby() {
		throw new IllegalArgumentException();
	}

	public void killTime() {
		int j = 0;
		while(j < 1000) {
			j++;
		}
	}

}