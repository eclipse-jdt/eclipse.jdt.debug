/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
public class Literals17 {
	int x1 = 1_0;              	// OK (decimal literal)
	int x2 = 0x1_0;            	// OK (hexadecimal literal)
	int x3 = 0_10;             	// OK (octal literal)
	int x4 = 0b10_00;			// OK (binary literal)
	double x5 = 10.5_56D;		// OK (double literal)
	float x6 = 	3.14_15F;		// OK (float literal)
	char x7 = 1_2_3;			// OK (char literal)
	long x8 = 1_0L;				// OK (long literal)
	short x9 = 1_0;				// OK (short literal)
	byte x10 = 0b1_0_0_0;		// OK (byte literal)
	
	public static void main(String[] args) {
		Literals17 literals = new Literals17();
		System.out.println(literals.x1);
	}
}