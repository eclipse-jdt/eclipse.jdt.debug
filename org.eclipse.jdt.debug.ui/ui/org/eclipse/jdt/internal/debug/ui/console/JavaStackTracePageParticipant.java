/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageSite;

/**
 * JavaStackTracePageParticipant
 */
public class JavaStackTracePageParticipant implements IConsolePageParticipant {
    
    private CloseConsoleAction fCloseAction;

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#init(org.eclipse.ui.part.IPageSite, org.eclipse.ui.console.IConsole)
     */
    public void init(IPageSite site, IConsole console) {
        fCloseAction = new CloseConsoleAction(console);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#dispose()
     */
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#contextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    public void contextMenuAboutToShow(IMenuManager menu) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#configureToolBar(org.eclipse.jface.action.IToolBarManager)
     */
    public void configureToolBar(IToolBarManager mgr) {
        mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fCloseAction);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return null;
    }

}
