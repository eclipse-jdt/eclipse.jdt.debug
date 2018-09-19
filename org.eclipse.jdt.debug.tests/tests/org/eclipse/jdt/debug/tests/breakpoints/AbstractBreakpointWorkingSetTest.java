/*******************************************************************************
 *  Copyright (c) 2005, 2007 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * Common code for breakpoint working set tests.
 *
 * @since 3.2
 */
public abstract class AbstractBreakpointWorkingSetTest extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public AbstractBreakpointWorkingSetTest(String name) {
		super(name);
	}

	/**
	 * Creates and returns a breakpoint working set with the given name if not
	 * already in existence.
	 *
	 * @param name working set name
	 */
	protected IWorkingSet createSet(String name) {
		IWorkingSetManager wsmanager = getWorkingSetManager();
		IWorkingSet set = wsmanager.getWorkingSet(name);
		if(set == null) {
			set = wsmanager.createWorkingSet(name, new IAdaptable[] {});
			set.setId(IDebugUIConstants.BREAKPOINT_WORKINGSET_ID);
			wsmanager.addWorkingSet(set);
		}//end if
		return set;
	}

	/**
	 * Retruns the working set manager.
	 *
	 * @return working set manager
	 */
	protected IWorkingSetManager getWorkingSetManager() {
		return PlatformUI.getWorkbench().getWorkingSetManager();
	}
}
