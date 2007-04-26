/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.event;

import org.eclipse.osgi.util.NLS;

public class EventMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdi.internal.event.EventMessages";//$NON-NLS-1$

	public static String EventImpl_Read_invalid_EventKind___1;
	public static String EventIteratorImpl_EventSets_are_unmodifiable_1;
	public static String EventSetImpl_Invalid_suspend_policy_encountered___1;
	public static String EventSetImpl_EventSets_are_unmodifiable_3;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, EventMessages.class);
	}
}
