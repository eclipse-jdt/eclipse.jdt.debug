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
package org.eclipse.jdi.internal.request;

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.AccessWatchpointEventImpl;

import com.sun.jdi.request.AccessWatchpointRequest;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class AccessWatchpointRequestImpl extends WatchpointRequestImpl
		implements AccessWatchpointRequest {
	/**
	 * Creates new AccessWatchpointRequest.
	 * @param vmImpl the VM
	 */
	public AccessWatchpointRequestImpl(VirtualMachineImpl vmImpl) {
		super("AccessWatchpointRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	@Override
	protected final byte eventKind() {
		return AccessWatchpointEventImpl.EVENT_KIND;
	}
}
