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

package org.eclipse.jdt.internal.debug.ui.display;


public interface IDataDisplay {
	
	/**
	 * Clears the content of this data display.
	 */
	public void clear();
	
	/**
	 * Displays the expression in the content of this data
	 * display.
	 */
	public void displayExpression(String expression);
	
	/**
	 * Displays the expression valur in the content of this data
	 * display.
	 */
	public void displayExpressionValue(String value);
}
