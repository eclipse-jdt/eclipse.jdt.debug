/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action which opens preference settings for Java variables.
 */
public class VariableOptionsAction implements IViewActionDelegate {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
	public void init(IViewPart view) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
	public void run(IAction action) {
    	SWTFactory.showPreferencePage("org.eclipse.jdt.debug.ui.JavaDetailFormattersPreferencePage",  //$NON-NLS-1$
    			new String[] {"org.eclipse.jdt.debug.ui.JavaDetailFormattersPreferencePage", //$NON-NLS-1$
    							"org.eclipse.jdt.debug.ui.JavaLogicalStructuresPreferencePage",  //$NON-NLS-1$
    							"org.eclipse.jdt.debug.ui.heapWalking",  //$NON-NLS-1$
    							"org.eclipse.jdt.debug.ui.JavaPrimitivesPreferencePage"}); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
	public void selectionChanged(IAction action, ISelection selection) {
    }
}
