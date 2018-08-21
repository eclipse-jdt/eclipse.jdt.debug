/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

public class BreakpointListenerTest {

        public static void main(String[] args) {
                foo();  // conditional breakpoint here:  foo(); return false;
                System.out.println("out of foo");
        }

        private static void foo() {
                System.out.println("hello");  // breakpoint here
        }

}
