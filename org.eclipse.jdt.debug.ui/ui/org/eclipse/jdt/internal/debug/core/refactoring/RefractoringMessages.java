/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
/**
 */
public class RefractoringMessages {
	private static final String BUNDLE_NAME= "org.eclipse.jdt.internal.debug.core.refactoring.RefractoringMessages";//$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE= ResourceBundle
			.getBundle(BUNDLE_NAME);
	/**
	 * 
	 */
	private RefractoringMessages() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		// TODO Auto-generated method stub
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}