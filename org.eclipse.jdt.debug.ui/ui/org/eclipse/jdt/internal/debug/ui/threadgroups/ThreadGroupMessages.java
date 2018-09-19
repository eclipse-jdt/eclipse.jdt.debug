/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.threadgroups;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 *
 */
public class ThreadGroupMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.ui.threadgroups.ThreadGroupMessages"; //$NON-NLS-1$

	private ThreadGroupMessages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ThreadGroupMessages.class);
	}

	public static String AsyncThreadGroupLabelAdapter_0;
}
