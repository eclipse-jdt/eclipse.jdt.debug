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
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/VMStartException.html
 */
public class VMStartException extends Exception {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

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
