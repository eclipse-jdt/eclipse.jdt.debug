/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/PrimitiveValue.html
 */
public interface PrimitiveValue extends Value {
	public boolean booleanValue();
	public byte byteValue();
	public char charValue();
	public double doubleValue();
	public float floatValue();
	public int intValue();
	public long longValue();
	public short shortValue();
}
