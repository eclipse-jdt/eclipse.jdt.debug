/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi;

import java.security.BasicPermission;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/JDIPermission.html
 */
public class JDIPermission extends BasicPermission {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public JDIPermission(String arg1) {
		super(arg1);
	}

	public JDIPermission(String arg1, String arg2) {
		super(arg1, arg2);
	}

}
