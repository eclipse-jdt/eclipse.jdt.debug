package org.eclipse.jdi.internal.jdwp;/*
 * JDI class Implementation
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */



import java.io.*;
import org.eclipse.jdi.internal.VirtualMachineImpl;

/**
 * This class implements the corresponding Java Debug Wire Protocol (JDWP) ID
 * declared by the JDWP specification.
 *
 */
public class JdwpInterfaceID extends JdwpReferenceTypeID {
	/**
	 * Creates new JdwpID.
	 */
	public JdwpInterfaceID(VirtualMachineImpl vmImpl) {
		super(vmImpl);
	}
}