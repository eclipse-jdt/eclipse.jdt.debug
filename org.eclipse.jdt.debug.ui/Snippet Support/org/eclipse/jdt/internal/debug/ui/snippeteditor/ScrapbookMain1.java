/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
		// XXX: if changing this class, make sure to check if
		// org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor.SCRAPBOOK_MAIN1_LAST_LINE is still valid
		// and points to the last line of this method with "method.invoke()" call.
		// This class is (as of today) MANUALLY compiled and built into org.eclipse.jdt.debug.ui/snippetsupport.jar
		// which is (as of today) checked into git and NOT rebuilt automatically.

		// See also org.eclipse.jdt.debug.tests.ui.JavaSnippetEditorTest.testEvaluation()
		// that should catch the inconsistency between JavaSnippetEditor and this code
	}
}
