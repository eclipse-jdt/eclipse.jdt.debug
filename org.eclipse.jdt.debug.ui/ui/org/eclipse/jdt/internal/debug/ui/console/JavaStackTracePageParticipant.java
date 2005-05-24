/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import java.util.Map;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * JavaStackTracePageParticipant
 */
public class JavaStackTracePageParticipant implements IConsolePageParticipant {
    
    private CloseConsoleAction fCloseAction;
    private EnabledSubmission fEnabledSubmission;
    private HandlerSubmission fHandlerSubmission;
    private FormatStackTraceActionDelegate fFormatAction;

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#init(org.eclipse.ui.part.IPageBookViewPage, org.eclipse.ui.console.IConsole)
     */
    public void init(IPageBookViewPage page, IConsole console) {
        fCloseAction = new CloseConsoleAction(console);
        
        IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();
        manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fCloseAction);
        
        fFormatAction = new FormatStackTraceActionDelegate((JavaStackTraceConsole) console);
        IHandler formatHandler = new AbstractHandler() {
            public Object execute(Map parameterValuesByName) throws ExecutionException {
                fFormatAction.run(null);
                return null;
            }
        };
        fEnabledSubmission = new EnabledSubmission(IConsoleConstants.ID_CONSOLE_VIEW, page.getSite().getShell(), null, "org.eclipse.jdt.ui.javaEditorScope"); //$NON-NLS-1$
        fHandlerSubmission = new HandlerSubmission(IConsoleConstants.ID_CONSOLE_VIEW, page.getSite().getShell(), null, "org.eclipse.jdt.ui.edit.text.java.format", formatHandler, Priority.MEDIUM); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#dispose()
     */
    public void dispose() {
        deactivated();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#activated()
	 */
	public void activated() {
        // add EOF submissions
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
		IWorkbenchContextSupport contextSupport = workbench.getContextSupport();
		contextSupport.addEnabledSubmission(fEnabledSubmission);
		commandSupport.addHandlerSubmission(fHandlerSubmission);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#deactivated()
	 */
	public void deactivated() {
        // remove EOF submissions
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
		IWorkbenchContextSupport contextSupport = workbench.getContextSupport();
		commandSupport.removeHandlerSubmission(fHandlerSubmission);
		contextSupport.removeEnabledSubmission(fEnabledSubmission);
	}

}
