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
package com.sun.jdi.request;


import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;

public interface BreakpointRequest extends EventRequest , Locatable {
	public void addThreadFilter(ThreadReference arg1);
	public Location location();
	public void addInstanceFilter(ObjectReference instance);
}
