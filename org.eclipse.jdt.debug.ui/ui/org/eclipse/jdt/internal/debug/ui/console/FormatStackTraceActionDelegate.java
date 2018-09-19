/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleView;

public class FormatStackTraceActionDelegate implements IViewActionDelegate {

    private JavaStackTraceConsole fConsole;
    private IConsoleView fView;

    public FormatStackTraceActionDelegate() {}

    public FormatStackTraceActionDelegate(JavaStackTraceConsole console) {
        fConsole = console;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
	public void init(IViewPart view) {
    	if (view instanceof IConsoleView) {
			fView = (IConsoleView) view;
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
	public void run(IAction action) {
    	if (fConsole != null) {
    		fConsole.format();
    	}
    	else if (fView != null) {
    		IConsole console = fView.getConsole();
    		if (console instanceof JavaStackTraceConsole) {
				fConsole = (JavaStackTraceConsole) console;
				fConsole.format();
			}
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
	public void selectionChanged(IAction action, ISelection selection) {}
}
