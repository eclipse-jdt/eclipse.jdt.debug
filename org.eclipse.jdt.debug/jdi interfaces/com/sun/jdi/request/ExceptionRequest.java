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
package com.sun.jdi.request;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/request/ExceptionRequest.html
 */
public interface ExceptionRequest extends EventRequest {
	public void addClassExclusionFilter(String arg1);
	public void addClassFilter(ReferenceType arg1);
	public void addClassFilter(String arg1);
	public void addInstanceFilter(ObjectReference instance);
	public void addThreadFilter(ThreadReference arg1);
	public ReferenceType exception();
	public boolean notifyCaught();
	public boolean notifyUncaught();
}
