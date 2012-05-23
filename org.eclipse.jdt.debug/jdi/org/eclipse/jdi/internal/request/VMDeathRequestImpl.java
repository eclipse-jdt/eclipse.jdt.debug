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
import org.eclipse.jdi.internal.event.VMDeathEventImpl;

import com.sun.jdi.request.VMDeathRequest;

public class VMDeathRequestImpl extends EventRequestImpl implements
		VMDeathRequest {

	public VMDeathRequestImpl(VirtualMachineImpl vmImpl) {
		super("VMDeathRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return JDWP event kind
	 */
	@Override
	protected byte eventKind() {
		return VMDeathEventImpl.EVENT_KIND;
	}
}
