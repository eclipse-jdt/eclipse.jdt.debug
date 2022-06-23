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
 * Contributors:
 *     Microsoft Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.request;

import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.VirtualMachineImpl;

public abstract class ThreadLifecycleRequestImpl extends EventRequestImpl {
	/**
	 * PlatformThreadsOnly is a preview API of the Java platform. Preview features may be removed in a future release, or upgraded to permanent
	 * features of the Java platform. Since JDWP version 19. For thread start and thread end events, restrict the events so they are only sent for
	 * platform threads.
	 *
	 * @since 3.20
	 */
	public static final byte MODIF_KIND_PLATFORMTHREADSONLY = 13;

	/**
	 * Platform threads filter
	 */
	protected boolean fPlatformThreadsFilter = false;

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

	@Override
	protected void writeModifiers(DataOutputStream outData) throws IOException {
		super.writeModifiers(outData);
		if (fPlatformThreadsFilter && supportsPlatformThreadsFilter()) {
			writeByte(MODIF_KIND_PLATFORMTHREADSONLY, "modifier", modifierKindMap(), outData); //$NON-NLS-1$
		}
	}

	/**
	 * Returns whether JDWP supports platform threads filter (a 19 preview feature).
	 *
	 * @return whether JDWP supports platform threads filter
	 */
	private boolean supportsPlatformThreadsFilter() {
		return ((VirtualMachineImpl) virtualMachine()).isJdwpVersionGreaterOrEqual(19, 0);
	}
}
