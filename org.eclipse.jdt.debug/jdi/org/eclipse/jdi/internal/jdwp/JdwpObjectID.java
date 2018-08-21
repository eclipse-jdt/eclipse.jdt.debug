/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.jdi.internal.jdwp;

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
	 * @return Returns VM specific size of ID.
	 */
	@Override
	public int getSize() {
		return fVirtualMachine.objectIDSize();
	}

	/**
	 * @return Returns true if ID is null.
	 */
	@Override
	public boolean isNull() {
		return fValue == VALUE_NULL;
	}
}
