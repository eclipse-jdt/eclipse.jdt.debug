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
package org.eclipse.jdi.internal.jdwp;

import org.eclipse.jdi.internal.VirtualMachineImpl;

/**
 * This class implements the corresponding Java Debug Wire Protocol (JDWP) ID
 * declared by the JDWP specification.
 * 
 */
public class JdwpMethodID extends JdwpID {
	/**
	 * Creates new JdwpID.
	 */
	public JdwpMethodID(VirtualMachineImpl vmImpl) {
		super(vmImpl);
	}

	/**
	 * @return Returns VM specific size of ID.
	 */
	@Override
	public int getSize() {
		return fVirtualMachine.methodIDSize();
	}

	/**
	 * @return Returns true if ID is null.
	 */
	@Override
	public boolean isNull() {
		return false;
	}
}
