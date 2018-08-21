/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching.environments;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 *
 */
public class EnvironmentMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.launching.environments.EnvironmentMessages"; //$NON-NLS-1$

	private EnvironmentMessages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, EnvironmentMessages.class);
	}

	public static String EnvironmentsManager_0;
	public static String ExecutionEnvironmentVariableResolver_0;
	public static String ExecutionEnvironmentVariableResolver_1;
	public static String ExecutionEnvironmentVariableResolver_2;
}
