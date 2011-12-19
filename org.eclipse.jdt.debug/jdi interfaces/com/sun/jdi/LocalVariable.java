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
package com.sun.jdi;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/LocalVariable.html
 */
public interface LocalVariable extends Mirror, Comparable<LocalVariable> {
	public boolean equals(Object arg1);
	public String genericSignature();
	public int hashCode();
	public boolean isArgument();
	public boolean isVisible(StackFrame arg1);
	public String name();
	public String signature();
	public Type type() throws ClassNotLoadedException;
	public String typeName();
}
