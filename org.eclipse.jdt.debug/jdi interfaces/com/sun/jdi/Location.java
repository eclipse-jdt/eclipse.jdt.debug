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
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/Location.html
 */
public interface Location extends Mirror, Comparable<Locatable> {
	public long codeIndex();
	public ReferenceType declaringType();
	@Override
	public boolean equals(Object arg1);
	@Override
	public int hashCode();
	public int lineNumber();
	public int lineNumber(String stratum);
	public Method method();
	public String sourceName() throws AbsentInformationException;
	public String sourceName(String stratum) throws AbsentInformationException;
	public String sourcePath() throws AbsentInformationException;
	public String sourcePath(String stratum) throws AbsentInformationException;
}
