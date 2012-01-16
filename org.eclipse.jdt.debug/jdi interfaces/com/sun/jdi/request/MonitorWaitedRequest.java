/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi.request;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/request/MonitorWaitedRequest.html
 */
public interface MonitorWaitedRequest extends EventRequest {
	public void addClassExclusionFilter(String arg1) throws InvalidRequestStateException;
	public void addClassFilter(ReferenceType arg1) throws InvalidRequestStateException;
	public void addClassFilter(String arg1) throws InvalidRequestStateException;
	public void addInstanceFilter(ObjectReference arg1) throws InvalidRequestStateException;
	public void addThreadFilter(ThreadReference arg1) throws InvalidRequestStateException;
}
