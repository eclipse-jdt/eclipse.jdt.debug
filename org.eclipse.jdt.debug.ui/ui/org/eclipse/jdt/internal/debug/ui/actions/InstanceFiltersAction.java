package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * Action to associate an object with one or more breakpoints.
 */
public class InstanceFiltersAction extends ObjectActionDelegate {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection = getCurrentSelection();
		if (selection == null || selection.size() > 1) {
			return;
		}
		
		Object o = selection.getFirstElement();
		if (o instanceof IJavaVariable) {
			final IJavaVariable var = (IJavaVariable)o;
			try {
				IValue value = var.getValue();
				if (value instanceof IJavaObject) {
					IJavaObject object = (IJavaObject)value;
					final List breakpoints = getApplicableBreakpoints(var, object);
					IStructuredContentProvider content = new IStructuredContentProvider() {
						public void dispose() {};
						
						public Object[] getElements(Object input) {
							return breakpoints.toArray();
						}
						
						public void inputChanged(Viewer viewer, Object a, Object b) {};
					};
					final IDebugModelPresentation modelPresentation= DebugUITools.newDebugModelPresentation();
					ListSelectionDialog dialog = new ListSelectionDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), breakpoints, content, modelPresentation, MessageFormat.format(ActionMessages.getString("InstanceFiltersAction.Restrict_selected_breakpoint(s)_to_object___{0}__1"), new String[] {var.getName()})){ //$NON-NLS-1$
						public void okPressed() {
							// check if breakpoints have already been restricted to other objects.
							Object[] checkBreakpoint= getViewer().getCheckedElements();
							for (int k= 0; k < checkBreakpoint.length; k++) {
								IJavaBreakpoint breakpoint= (IJavaBreakpoint) checkBreakpoint[k];
								try {
									IJavaObject[] instanceFilters= breakpoint.getInstanceFilters();
									if (instanceFilters.length > 0) {
										MessageDialog dialog= new MessageDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("InstanceFiltersAction.Instance_Filter_Breakpoint_Selection_2"), //$NON-NLS-1$
											null, MessageFormat.format(ActionMessages.getString("InstanceFiltersAction.breakpoint_{0}_already_restricted._Reset_the_restriction_to_object_{1}_"), new String[] { modelPresentation.getText(breakpoint), var.getName()}), //$NON-NLS-1$
											MessageDialog.QUESTION, new String[] { ActionMessages.getString("InstanceFiltersAction.Yes_2"), ActionMessages.getString("InstanceFiltersAction.Cancel_3")}, //$NON-NLS-1$ //$NON-NLS-2$
											0);
										if (dialog.open() == 0) {
											for (int i= 0; i < instanceFilters.length; i++) {
												breakpoint.removeInstanceFilter(instanceFilters[i]);
											}
										} else {
											// if 'cancel', do not close the instance filter dialog
											return;
										}
									}
								} catch (CoreException e) {
									JDIDebugUIPlugin.log(e);
								}
							}
							super.okPressed();
						}
					};
					dialog.setTitle(ActionMessages.getString("InstanceFiltersAction.Instance_Filter_Breakpoint_Selection_2")); //$NON-NLS-1$
					
					// determine initial selection
					List existing = new ArrayList();
					Iterator iter = breakpoints.iterator();
					while (iter.hasNext()) {
						IJavaBreakpoint bp = (IJavaBreakpoint)iter.next();
						IJavaObject[] filters = bp.getInstanceFilters();
						for (int i = 0; i < filters.length; i++) {
							if (filters[i].equals(object)) {
								existing.add(bp);
								break;
							}
						}
					}
					dialog.setInitialSelections(existing.toArray());
					
					if (dialog.open() == ListSelectionDialog.OK) {
						Object[] selectedBreakpoints = dialog.getResult();
						if (selectedBreakpoints != null) {
							// add
							for (int i = 0; i < selectedBreakpoints.length; i++) {
								IJavaBreakpoint bp = (IJavaBreakpoint)selectedBreakpoints[i];
								bp.addInstanceFilter(object);
								existing.remove(bp);
							}
							// remove
							iter = existing.iterator();
							while (iter.hasNext()) {
								IJavaBreakpoint bp = (IJavaBreakpoint)iter.next();
								bp.removeInstanceFilter(object);
							}
						}
					}
				} else {
					// only allowed for objects
				}
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
	}
	
	protected List getApplicableBreakpoints(IJavaVariable variable, IJavaObject object) {
		List breakpoints = new ArrayList();
		
		try {
			// collect names in type hierarchy
			List superTypeNames = new ArrayList();
			IJavaClassType type = (IJavaClassType)object.getJavaType();
			while (type != null) {
				superTypeNames.add(type.getName());
				type = type.getSuperclass();
			}
			
			IBreakpoint[] allBreakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
			for (int i = 0; i < allBreakpoints.length; i++) {
				if (allBreakpoints[i] instanceof IJavaBreakpoint) {
					IJavaBreakpoint jbp = (IJavaBreakpoint)allBreakpoints[i];
					IJavaBreakpoint valid = null;
					if (jbp instanceof IJavaWatchpoint && variable instanceof IJavaFieldVariable) {
						IJavaWatchpoint wp = (IJavaWatchpoint)jbp;
						IJavaFieldVariable fv = (IJavaFieldVariable)variable;
						if (variable.getName().equals(wp.getFieldName()) && fv.getDeclaringType().getName().equals(wp.getTypeName())) {
							valid = wp;
						}
					} else if (superTypeNames.contains(jbp.getTypeName()) || jbp instanceof IJavaExceptionBreakpoint) {
						valid = jbp;
					}
					if (valid != null) {
						breakpoints.add(valid);
					}
				}
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		 
		return breakpoints;
	}

}
