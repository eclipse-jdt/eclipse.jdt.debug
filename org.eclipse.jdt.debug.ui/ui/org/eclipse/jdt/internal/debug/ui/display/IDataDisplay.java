
package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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