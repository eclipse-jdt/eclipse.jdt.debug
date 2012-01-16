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
import org.eclipse.jdi.internal.event.BreakpointEventImpl;

import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.request.BreakpointRequest;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class BreakpointRequestImpl extends EventRequestImpl implements
		BreakpointRequest, Locatable {
	/**
	 * Creates new BreakpointRequest.
	 * @param vmImpl the VM
	 */
	public BreakpointRequestImpl(VirtualMachineImpl vmImpl) {
		super("BreakpointRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns location of Breakpoint Request.
	 */
	public Location location() {
		return fLocationFilters.get(0);
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	@Override
	protected final byte eventKind() {
		return BreakpointEventImpl.EVENT_KIND;
	}

}
