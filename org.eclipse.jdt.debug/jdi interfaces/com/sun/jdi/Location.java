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
package com.sun.jdi;


public interface Location extends Mirror , Comparable {
	public long codeIndex();
	public ReferenceType declaringType();
	public boolean equals(Object arg1);
	public int hashCode();
	public int lineNumber();
	public Method method();
   	public String sourceName() throws AbsentInformationException;
   	public int lineNumber(String stratum);
   	public String sourceName(String stratum) throws AbsentInformationException;
   	public String sourcePath(String stratum) throws AbsentInformationException;
   	public String sourcePath() throws AbsentInformationException;
}
