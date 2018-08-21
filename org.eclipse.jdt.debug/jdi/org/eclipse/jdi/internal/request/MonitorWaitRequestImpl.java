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
import org.eclipse.jdi.internal.event.MonitorWaitEventImpl;

import com.sun.jdi.request.MonitorWaitRequest;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 *
 * @since 3.3
 */
public class MonitorWaitRequestImpl extends EventRequestImpl implements
		MonitorWaitRequest {

	public MonitorWaitRequestImpl(VirtualMachineImpl vmImpl) {
		super("MonitorWaitRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	@Override
	protected byte eventKind() {
		return MonitorWaitEventImpl.EVENT_KIND;
	}

}
