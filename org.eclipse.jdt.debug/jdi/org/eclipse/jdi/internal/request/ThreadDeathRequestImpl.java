/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Microsoft Corporation - supports virtual threads
 *******************************************************************************/
package org.eclipse.jdi.internal.request;

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.ThreadDeathEventImpl;

import com.sun.jdi.request.ThreadDeathRequest;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 */
public class ThreadDeathRequestImpl extends ThreadLifecycleRequestImpl implements
		ThreadDeathRequest {
	/**
	 * Creates new ThreadDeathRequest.
	 */
	public ThreadDeathRequestImpl(VirtualMachineImpl vmImpl) {
		super("ThreadDeathRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	@Override
	protected final byte eventKind() {
		return ThreadDeathEventImpl.EVENT_KIND;
	}
}
