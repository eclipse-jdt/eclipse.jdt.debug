package org.eclipse.jdi.internal.request;

import org.eclipse.jdi.internal.VirtualMachineImpl;

import org.eclipse.jdi.internal.event.VMDeathEventImpl;
import com.sun.jdi.request.VMDeathRequest;

public class VMDeathRequestImpl extends EventRequestImpl implements VMDeathRequest {
	
	public VMDeathRequestImpl(VirtualMachineImpl vmImpl) {
		super("VMDeathRequest", vmImpl);
	}

	/**
	 * @return JDWP event kind
	 */
	protected byte eventKind() {
		return VMDeathEventImpl.EVENT_KIND;
	}

}

