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
public class JdwpObjectID extends JdwpID {
	/**
	 * Creates new JdwpID.
	 */
	public JdwpObjectID(VirtualMachineImpl vmImpl) {
		super(vmImpl);
	}
	
	/**
	 * @return Returns true if two IDs refer to the same entity in the target VM.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		return object != null && object.getClass().equals(this.getClass()) && fValue == ((JdwpObjectID)object).fValue;
	}
	
	/**
	 * @return Returns VM specific size of ID.
	 */
	public int getSize() {
		return fVirtualMachine.objectIDSize();
	}

	/**
	 * @return Returns true if ID is null.
	 */
	public boolean isNull() {
		return fValue == VALUE_NULL;
 	}
}
