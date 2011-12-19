/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi.connect;

import java.util.ArrayList;
import java.util.List;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/IllegalConnectorArgumentsException.html
 */
public class IllegalConnectorArgumentsException extends Exception {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	List<String> fNames;

	public IllegalConnectorArgumentsException(String message, List<String> argNames) {
		super(message);
		fNames = argNames;
	}

	public IllegalConnectorArgumentsException(String message, String argName) {
		super(message);
		fNames = new ArrayList<String>(1);
		fNames.add(argName);
	}

	public List<String> argumentNames() {
		return fNames;
	}
}
