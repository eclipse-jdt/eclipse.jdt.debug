/*******************************************************************************
 * Copyright (c) 2022 Microsoft Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Microsoft Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.request;

import org.eclipse.jdi.internal.VirtualMachineImpl;

public abstract class ThreadLifecycleRequestImpl extends EventRequestImpl {

	protected ThreadLifecycleRequestImpl(String description, VirtualMachineImpl vmImpl) {
		super(description, vmImpl);
	}

	/**
	 * For thread start and thread end events, restrict the events so they are only sent for platform threads.
	 *
	 * @since 3.20
	 */
	public void addPlatformThreadsOnlyFilter() {
		checkDisabled();
		fPlatformThreadsFilter = true;
	}
}
