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
package org.eclipse.debug.tests.targets;

/**
 * Used for source lookup tests in non-default package, with non-standard name
 */
public class Source_$_Lookup {
		
	public void foo() {
		System.out.println("foo");
	}
	
	public class Inner {
		
		public void innerFoo() {
			System.out.println("innerFoo");
		}
		
		public class Nested {
			public void nestedFoo() {
				System.out.println("nestedFoo");
			}
		}		
	}

}
