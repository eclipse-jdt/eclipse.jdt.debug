/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
