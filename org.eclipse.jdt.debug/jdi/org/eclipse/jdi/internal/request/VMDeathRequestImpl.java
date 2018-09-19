/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
