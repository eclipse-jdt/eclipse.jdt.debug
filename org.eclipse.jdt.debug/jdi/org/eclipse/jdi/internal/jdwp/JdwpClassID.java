package org.eclipse.jdi.internal.jdwp;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdi.internal.VirtualMachineImpl;

/**
 * This class implements the corresponding Java Debug Wire Protocol (JDWP) ID
 * declared by the JDWP specification.
 *
 */
public class JdwpClassID extends JdwpReferenceTypeID {
	/**
	 * Creates new JdwpID.
	 */
	public JdwpClassID(VirtualMachineImpl vmImpl) {
		super(vmImpl);
	}
}