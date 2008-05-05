/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * JavaStackTracePageParticipant
 */
public class JavaStackTracePageParticipant implements IConsolePageParticipant {
    
    private CloseConsoleAction fCloseAction;
    private FormatStackTraceActionDelegate fFormatAction;
    private IHandlerActivation fHandlerActivation;
    private IContextActivation fContextActivation;

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#init(org.eclipse.ui.part.IPageBookViewPage, org.eclipse.ui.console.IConsole)
     */
    public void init(IPageBookViewPage page, IConsole console) {
        fCloseAction = new CloseConsoleAction(console);
        
        IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();
        manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fCloseAction);
        
        fFormatAction = new FormatStackTraceActionDelegate((JavaStackTraceConsole) console);
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
        IHandlerService handlerService = (IHandlerService) workbench.getAdapter(IHandlerService.class);
        
        IHandler formatHandler = new AbstractHandler() {
            public Object execute(ExecutionEvent event) throws ExecutionException {
                fFormatAction.run(null);
                return null;
            }
        };
        
        fHandlerActivation = handlerService.activateHandler("org.eclipse.jdt.ui.edit.text.java.format", formatHandler); //$NON-NLS-1$
		
        IContextService contextService = (IContextService) workbench.getAdapter(IContextService.class);
        fContextActivation = contextService.activateContext("org.eclipse.jdt.ui.javaEditorScope"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#deactivated()
	 */
	public void deactivated() {
        // remove EOF submissions
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (fHandlerActivation != null){
			IHandlerService handlerService = (IHandlerService) workbench.getAdapter(IHandlerService.class);
			handlerService.deactivateHandler(fHandlerActivation);
			fHandlerActivation = null;
		}
        if (fContextActivation != null){
        	IContextService contextService = (IContextService) workbench.getAdapter(IContextService.class);
        	contextService.deactivateContext(fContextActivation);
        	fContextActivation = null;
        }
	}

}
