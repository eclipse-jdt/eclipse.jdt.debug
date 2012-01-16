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
package com.sun.jdi.request;

import com.sun.jdi.Mirror;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/request/EventRequest.html
 */
public interface EventRequest extends Mirror {
	public static final int SUSPEND_NONE = 0;
	public static final int SUSPEND_EVENT_THREAD = 1;
	public static final int SUSPEND_ALL = 2;

	public void addCountFilter(int arg1) throws InvalidRequestStateException;

	public void disable();

	public void enable();

	public Object getProperty(Object key);

	public boolean isEnabled();

	public void putProperty(Object key, Object value);

	public void setEnabled(boolean arg1);

	public void setSuspendPolicy(int arg1);

	public int suspendPolicy();
}
