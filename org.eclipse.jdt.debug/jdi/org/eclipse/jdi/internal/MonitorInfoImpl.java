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
package org.eclipse.jdi.internal;

import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.MonitorInfo;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 *
 * @since 3.3
 */
public class MonitorInfoImpl extends MirrorImpl implements MonitorInfo {

	private ThreadReference fThread;
	private ObjectReference fMonitor;
	private int fDepth;

	/** constructor **/
	public MonitorInfoImpl(ThreadReference thread, int depth,
			ObjectReference monitor, VirtualMachineImpl virtualMachineImpl) {
		super("MonitorInfoImpl", virtualMachineImpl); //$NON-NLS-1$
		fThread = thread;
		fDepth = depth;
		fMonitor = monitor;
	}

	/**
	 * @see com.sun.jdi.MonitorInfo#monitor()
	 */
	@Override
	public ObjectReference monitor() throws InvalidStackFrameException {
		return fMonitor;
	}

	/**
	 * @see com.sun.jdi.MonitorInfo#stackDepth()
	 */
	@Override
	public int stackDepth() throws InvalidStackFrameException {
		return fDepth;
	}

	/**
	 * @see com.sun.jdi.MonitorInfo#thread()
	 */
	@Override
	public ThreadReference thread() throws InvalidStackFrameException {
		return fThread;
	}
}
