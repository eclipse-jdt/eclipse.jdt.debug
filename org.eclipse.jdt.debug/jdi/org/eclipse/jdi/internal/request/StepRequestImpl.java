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
import org.eclipse.jdi.internal.event.StepEventImpl;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.StepRequest;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class StepRequestImpl extends EventRequestImpl implements StepRequest {
	/**
	 * Creates new StepRequest.
	 */
	public StepRequestImpl(VirtualMachineImpl vmImpl) {
		super("StepRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * Creates new StepRequest, used by subclasses.
	 */
	protected StepRequestImpl(String description, VirtualMachineImpl vmImpl) {
		super(description, vmImpl);
	}

	/**
	 * @return Returns the relative call stack limit.
	 */
	public int depth() {
		return fThreadStepFilters.get(0).fThreadStepDepth;
	}

	/**
	 * @return Returns the size of each step.
	 */
	public int size() {
		return fThreadStepFilters.get(0).fThreadStepSize;
	}

	/**
	 * @return Returns ThreadReference of thread in which to step.
	 */
	public ThreadReference thread() {
		return fThreadStepFilters.get(0).fThread;
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	@Override
	protected final byte eventKind() {
		return StepEventImpl.EVENT_KIND;
	}
}
