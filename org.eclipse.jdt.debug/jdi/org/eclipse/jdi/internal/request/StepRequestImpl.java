package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.StepEventImpl;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.StepRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
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
		return ((EventRequestImpl.ThreadStepFilter)fThreadStepFilters.get(0)).fThreadStepDepth;
	}
	
	/**
	 * @return Returns the size of each step.
	 */
	public int size() {
		return ((EventRequestImpl.ThreadStepFilter)fThreadStepFilters.get(0)).fThreadStepSize;
	}
	
	/**
	 * @return Returns ThreadReference of thread in which to step.
	 */
	public ThreadReference thread() {
		return ((EventRequestImpl.ThreadStepFilter)fThreadStepFilters.get(0)).fThread;
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected final byte eventKind() {
		return StepEventImpl.EVENT_KIND;
	}
}
