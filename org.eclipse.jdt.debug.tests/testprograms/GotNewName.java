/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

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
