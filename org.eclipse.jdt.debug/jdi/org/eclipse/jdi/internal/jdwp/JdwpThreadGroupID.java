package org.eclipse.jdi.internal.jdwp;

import java.io.*;
import org.eclipse.jdi.internal.VirtualMachineImpl;

/**
 * This class implements the corresponding Java Debug Wire Protocol (JDWP) ID
 * declared by the JDWP specification.
 *
 */
public class JdwpThreadGroupID extends JdwpObjectID {
	/**
	 * Creates new JdwpID.
	 */
	public JdwpThreadGroupID(VirtualMachineImpl vmImpl) {
		super(vmImpl);
	}
}
