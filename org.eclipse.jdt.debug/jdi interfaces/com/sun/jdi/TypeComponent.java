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
package com.sun.jdi;

 
public interface TypeComponent extends Mirror , Accessible {
	public ReferenceType declaringType();
	public String genericSignature();
	public boolean isFinal();
	public boolean isStatic();
	public boolean isSynthetic();
	public String name();
	public String signature();
}
