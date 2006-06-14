/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

/**
 * Test cases for the implementation of providing argumebnt information even if 
 * no debugging information is present in the new java 1.6 VM
 * 
 * @since 3.3
 */
public class ContendedMonitorTests extends AbstractJDITest {

	/** setup test info locally **/
	public void localSetUp() {}

	/**
	 * test to see if a the 1.6 VM can get monitor events info and that 
	 * a non-1.6VM cannot.
	 */
	public void testCanRequestMonitorEvents() {
		if(fVM.version().indexOf("1.6") > -1) {
			assertTrue("Should have ability to request monitor events info", fVM.canRequestMonitorEvents());
		}
		else {
			assertTrue("Should not have ability to request monitor events info", !fVM.canRequestMonitorEvents());
		}
	}
	
}
