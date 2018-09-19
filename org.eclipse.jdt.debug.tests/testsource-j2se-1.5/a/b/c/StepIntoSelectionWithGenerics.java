/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
package a.b.c;
public class StepIntoSelectionWithGenerics<T> {
	
	class InnerClazz<E> {
		class InnerClazz2<K> {
			public String hello() {
				return "Hello from InnerClazz2";
			}
		}
		public String hello() {
			return "Hello from InnerClazz";
		}
	}
	
    public void hello() {
        System.out.println("Hello");
    }

    public static void main(String[] args) {
    	new StepIntoSelectionWithGenerics<String>().hello();
    	new StepIntoSelectionWithGenerics<String>().new InnerClazz<Integer>().hello();
        new StepIntoSelectionWithGenerics<String>().new InnerClazz<Integer>().new InnerClazz2<Double>().hello();
    }
}