/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi.connect;


import java.util.ArrayList;
import java.util.List;

public class IllegalConnectorArgumentsException extends Exception {
	List fNames;
	
	public IllegalConnectorArgumentsException(String message, List argNames) {
		super(message);
		fNames = argNames;
	}
	
	public IllegalConnectorArgumentsException(String message, String argName) {
		super(message);
		fNames = new ArrayList(1);
		fNames.add(argName);
	}
	
	public List argumentNames() { 
		return fNames;
	}
}
