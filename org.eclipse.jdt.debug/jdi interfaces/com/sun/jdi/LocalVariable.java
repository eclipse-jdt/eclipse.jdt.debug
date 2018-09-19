/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/LocalVariable.html
 */
public interface LocalVariable extends Mirror, Comparable<LocalVariable> {
	@Override
	public boolean equals(Object arg1);
	public String genericSignature();
	@Override
	public int hashCode();
	public boolean isArgument();
	public boolean isVisible(StackFrame arg1);
	public String name();
	public String signature();
	public Type type() throws ClassNotLoadedException;
	public String typeName();
}
