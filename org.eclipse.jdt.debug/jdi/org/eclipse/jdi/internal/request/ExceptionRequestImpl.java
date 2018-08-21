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
import org.eclipse.jdi.internal.event.ExceptionEventImpl;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.ExceptionRequest;

/**
 * This class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 *
 */
public class ExceptionRequestImpl extends EventRequestImpl implements
		ExceptionRequest {
	/**
	 * Creates new EventRequestManager.
	 */
	public ExceptionRequestImpl(VirtualMachineImpl vmImpl) {
		super("ExceptionRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * Returns exception type for which exception events are requested.
	 */
	@Override
	public ReferenceType exception() {
		return fExceptionFilters.get(0).fException;
	}

	/**
	 * @return Returns true if caught exceptions will be reported.
	 */
	@Override
	public boolean notifyCaught() {
		return fExceptionFilters.get(0).fNotifyCaught;
	}

	/**
	 * @return Returns true if uncaught exceptions will be reported.
	 */
	@Override
	public boolean notifyUncaught() {
		return fExceptionFilters.get(0).fNotifyUncaught;
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	@Override
	protected final byte eventKind() {
		return ExceptionEventImpl.EVENT_KIND;
	}
}
