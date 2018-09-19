/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.debug.jdi.tests;

import com.sun.jdi.VirtualMachine;

/**
 * This class allows to hold on information about the VM
 * that we don't want to loose between 2 tests.
 */
public class VMInformation {
	VirtualMachine fVM;
	Process fLaunchedVM;
	EventReader fEventReader;
	AbstractReader fConsoleReader;
	String fVMType;

	/**
	 * Creates a new VMInformation for the given vm, vm type, launched vm, event reader and console reader.
	 */
	VMInformation(VirtualMachine vm, String vmType, Process launchedVM, EventReader eventReader, AbstractReader consoleReader) {
		fVM = vm;
		fLaunchedVM = launchedVM;
		fEventReader = eventReader;
		fConsoleReader = consoleReader;
		fVMType = vmType;
	}
}
