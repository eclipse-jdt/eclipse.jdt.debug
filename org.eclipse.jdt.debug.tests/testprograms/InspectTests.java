/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

public class InspectTests {
	
	static {
		Outer s = new Outer() {
			char get() {
				return getchar(); //bp here
			}
			char getchar() {return 's';}
		};
		s.get();
	}
	
	Outer a = new Outer() {
		char get() {
			return getchar(); //bp here
		}
		char getchar() {return 'a';}
	};
	
	void m1() {
		Outer b = new Outer() {
			char get() {
				return getchar(); //bp here
			}
			char getchar() {return 'b';}
		};
		b.get();
	}
	
	public static void main(String[] args) {
		InspectTests it = new InspectTests();
		it.a.get();
		it.m1();
	}
}

class Outer {
	char get() {return getchar();}
	char getchar() {return 'x';}
}