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


public class VMStartException extends Exception {
	Process fProcess;
	
	public VMStartException(Process proc) {
		fProcess = proc;
	}
	
	public VMStartException(String str, Process proc) {
		super(str);
		fProcess = proc;
	}
	
	public Process process() { 
		return fProcess;
	}
}
