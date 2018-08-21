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
