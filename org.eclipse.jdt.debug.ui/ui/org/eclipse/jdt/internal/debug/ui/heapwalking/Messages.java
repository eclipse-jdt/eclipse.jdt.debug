/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.3
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.ui.heapwalking.Messages"; //$NON-NLS-1$

	public static String AllInstancesActionDelegate_0;
	public static String AllInstancesActionDelegate_2;
	public static String AllInstancesActionDelegate_3;
	
	public static String AllReferencesActionDelegate_0;
	public static String AllReferencesActionDelegate_1;

	public static String AllReferencesInViewActionDelegate_0;
	public static String AllReferencesInViewActionDelegate_1;

	public static String InstanceCountActionDelegate_0;

	public static String InstanceCountActionDelegate_1;

	public static String InstanceCountActionDelegate_2;

	public static String InstanceCountActionDelegate_3;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
