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
    public void init(IViewPart view) {
    	if (view instanceof IConsoleView) {
			fView = (IConsoleView) view;
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
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
    public void selectionChanged(IAction action, ISelection selection) {}      
}
