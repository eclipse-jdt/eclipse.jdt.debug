/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggles a class prepare breakpoint for a selected type
 * 
 * @since 3.0
 */
public class ToggleClassPrepareBreakpointAction implements IObjectActionDelegate,	IActionDelegate2 {
	
	private ISelection fSelection;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection ss = (IStructuredSelection)fSelection;
		Iterator<IType> iterator = ss.iterator();
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
		while (iterator.hasNext()) {
			IType type = iterator.next();
			IBreakpoint existing = null;
			try {
				for (int i = 0; i < breakpoints.length; i++) {
					IJavaBreakpoint breakpoint = (IJavaBreakpoint) breakpoints[i];
					if (breakpoint instanceof IJavaClassPrepareBreakpoint &&
							type.getFullyQualifiedName().equals(breakpoint.getTypeName())) {
						existing = breakpoint;
						break;
					}
				}
				if (existing != null) {
					existing.delete(); 
				} else {
					int kind = IJavaClassPrepareBreakpoint.TYPE_CLASS;
					if (!type.isClass()) {
						kind = IJavaClassPrepareBreakpoint.TYPE_INTERFACE;
					}
					HashMap<String, Object> map = new HashMap<String, Object>(10);
					BreakpointUtils.addJavaBreakpointAttributes(map, type);
					
					ISourceRange range= type.getNameRange();
					int start= -1;
					int end= -1;
					if (range != null) {
						start= range.getOffset();
						end= start + range.getLength();
					}
					JDIDebugModel.createClassPrepareBreakpoint(BreakpointUtils.getBreakpointResource(type), type.getFullyQualifiedName(), kind, start, end, true, map);
				}
			} catch (CoreException e) {
				
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	    fSelection = selection;
	}
}
