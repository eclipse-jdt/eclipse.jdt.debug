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
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * The workbench menu action for adding an exception breakpoint
 */
public class AddExceptionAction implements IViewActionDelegate, IWorkbenchWindowActionDelegate {
	
	public static final String CAUGHT_CHECKED = "caughtChecked"; //$NON-NLS-1$
	public static final String UNCAUGHT_CHECKED = "uncaughtChecked"; //$NON-NLS-1$
	public static final String DIALOG_SETTINGS = "AddExceptionDialog"; //$NON-NLS-1$
	
	private IWorkbenchWindow fWindow = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			IDialogSettings settings = getDialogSettings();
			AddExceptionTypeDialogExtension ext = new AddExceptionTypeDialogExtension(settings.getBoolean(CAUGHT_CHECKED), settings.getBoolean(UNCAUGHT_CHECKED));
			SelectionDialog dialog = JavaUI.createTypeDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), 
						fWindow, 
						SearchEngine.createWorkspaceScope(), 
						IJavaElementSearchConstants.CONSIDER_CLASSES, false, "*Exception*", ext); //$NON-NLS-1$
			dialog.setTitle(BreakpointMessages.AddExceptionAction_0);
			dialog.setMessage(BreakpointMessages.AddExceptionAction_1);
			dialog.create();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IJavaDebugHelpContextIds.ADD_EXCEPTION_DIALOG);
			if(dialog.open() == IDialogConstants.OK_ID) {
				boolean caught = ext.shouldHandleCaughtExceptions(),
						uncaught = ext.shouldHandleUncaughtExceptions();
				Object[] results = dialog.getResult(); 
				if(results != null && results.length > 0) {
					try {
						createBreakpoint(caught, uncaught, (IType)results[0]);
						settings.put(CAUGHT_CHECKED, caught);
						settings.put(UNCAUGHT_CHECKED, uncaught);
					}
					catch (CoreException e) {JDIDebugUIPlugin.statusDialog(e.getStatus());}
				}
				
			}
		} 
		catch (JavaModelException e1) {}
	}
	
	/**
	 * Returns the existing dialog settings for the persisted state of the caught and uncaught check boxes.
	 * If no section exists then a new one is created
	 * 
	 * @return the dialog settings section for the type dialog extension
	 * 
	 * @since 3.4
	 */
	private IDialogSettings getDialogSettings() {
        IDialogSettings allSetttings = JDIDebugUIPlugin.getDefault().getDialogSettings();
        IDialogSettings section = allSetttings.getSection(DIALOG_SETTINGS);
        if (section == null) {
            section = allSetttings.addNewSection(DIALOG_SETTINGS);
            section.put(CAUGHT_CHECKED, true);
            section.put(UNCAUGHT_CHECKED, true);
        }
        return section;
    }
	
	/**
	 * creates a single breakpoint of the specified type
	 * @param caught if the exception is caught
	 * @param uncaught if the exception is uncaught
	 * @param type the type of the exception
	 * @throws CoreException if an exception occurs
	 * @since 3.2
	 */
	private void createBreakpoint(final boolean caught, final boolean uncaught, final IType type) throws CoreException {
		final IResource resource = BreakpointUtils.getBreakpointResource(type);
		final Map<String, Object> map = new HashMap<String, Object>(10);
		BreakpointUtils.addJavaBreakpointAttributes(map, type);
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(
						JDIDebugModel.getPluginIdentifier());
		IJavaExceptionBreakpoint breakpoint = null;
		boolean exists = false;
		for (int j = 0; j < breakpoints.length; j++) {
			if (breakpoints[j] instanceof IJavaExceptionBreakpoint) {
				breakpoint = (IJavaExceptionBreakpoint) breakpoints[j];
				if (breakpoint.getTypeName().equals(type.getFullyQualifiedName())) {
					exists = true;
					break;
				}
			}
		}
		// If the breakpoint does not exist, add it.  If it does exist, make sure it is enabled.
		if (!exists) {
			Job job = new Job(BreakpointMessages.AddExceptionAction_0) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						JDIDebugModel.createExceptionBreakpoint(resource,
								type.getFullyQualifiedName(), caught,
								uncaught, isChecked(type), true, map);
						return Status.OK_STATUS;
					} catch (CoreException e) {
						return e.getStatus();
					}
				}

			};
			job.setSystem(true);
			job.setPriority(Job.INTERACTIVE);
			job.schedule();
		} else {
			final IJavaBreakpoint existingBreakpoint = breakpoint;
			Job job = new Job(BreakpointMessages.AddExceptionAction_EnableExceptionBreakpoint) {
	            @Override
				protected IStatus run(IProgressMonitor monitor) {
	                try {
	                	existingBreakpoint.setEnabled(true);
	                    return Status.OK_STATUS;
	                } catch (final CoreException e) {
	                	return e.getStatus();
	                }
	            }
	        };
	        job.setSystem(true);
			job.setPriority(Job.INTERACTIVE);
			job.schedule();
		}
	}
	
    /**
     * returns if the exception should be marked as 'checked' or not
     * @param type the type of the exception
     * @return true if the exception is to be 'checked' false otherwise
     * @since 3.2
     */
    public static boolean isChecked(IType type) {
    	 if(type != null) {
 	    	try {
 	            ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
 	            IType curr = type;
 	            while (curr != null) {
 	                String name = curr.getFullyQualifiedName('.');
 	                if ("java.lang.RuntimeException".equals(name) || "java.lang.Error".equals(name)) { //$NON-NLS-2$ //$NON-NLS-1$
 	                    return false;
 	                }
 	                curr = hierarchy.getSuperclass(curr);
 	            }
 	        } 
 	        catch (JavaModelException e) {JDIDebugUIPlugin.log(e);}
         }
    	return true;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fWindow = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow = window;
	}
}
