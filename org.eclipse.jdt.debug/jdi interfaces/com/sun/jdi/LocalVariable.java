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

 
public interface LocalVariable extends Mirror , Comparable {
	public boolean equals(Object arg1);
	public String genericSignature();
	public int hashCode();
	public boolean isArgument();
	public boolean isVisible(StackFrame arg1);
	public String name();
	public String signature();
	public com.sun.jdi.Type type() throws ClassNotLoadedException;
	public String typeName();
}
