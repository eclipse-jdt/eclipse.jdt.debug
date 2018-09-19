/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdi.internal.request;

import org.eclipse.jdi.internal.VirtualMachineImpl;

import com.sun.jdi.Field;
import com.sun.jdi.request.WatchpointRequest;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 *
 */
public abstract class WatchpointRequestImpl extends EventRequestImpl implements
		WatchpointRequest {
	/**
	 * Creates new WatchpointRequest, only used by subclasses.
	 */
	public WatchpointRequestImpl(String description, VirtualMachineImpl vmImpl) {
		super(description, vmImpl);
	}

	/**
	 * @return Returns field for which Watchpoint requests is issued.
	 */
	@Override
	public Field field() {
		return fFieldFilters.get(0);
	}
}
