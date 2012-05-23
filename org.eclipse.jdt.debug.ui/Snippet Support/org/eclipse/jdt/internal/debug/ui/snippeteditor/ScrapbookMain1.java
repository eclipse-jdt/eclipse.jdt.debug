/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ScrapbookMain1 {
	/**
	 * Evaluates the given class
	 * @param clazz the class to evaluate
	 * @throws ClassNotFoundException if the class cannot be found
	 * @throws NoSuchMethodException if there is no such method
	 * @throws InvocationTargetException if there is a failure to run
	 * @throws IllegalAccessException if there are no permissions to run
	 */
	public static void eval(Class<?> clazz) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method=clazz.getDeclaredMethod("nop", new Class[0]); //$NON-NLS-1$
		method.invoke(null, new Object[0]);
	}
}
