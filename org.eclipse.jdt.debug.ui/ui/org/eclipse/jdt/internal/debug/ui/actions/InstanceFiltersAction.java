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
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.AbstractDebugCheckboxSelectionDialog;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Action to associate an object with one or more breakpoints.
 */
public class InstanceFiltersAction extends ObjectActionDelegate {

	/**
	 * Dialog that allows the user to select one or more breakpoints that should be restricted
	 * to a specific object instance.
	 */
	class InstanceFilterDialog extends AbstractDebugCheckboxSelectionDialog {

		private final Object fInput;
		private final String fMessage;
		private final IBaseLabelProvider fLabelProvider;

		public InstanceFilterDialog(Shell parentShell, Object input, IBaseLabelProvider labelProvider, String message){
			super(parentShell);
			fInput = input;
			fMessage = message;
			fLabelProvider = labelProvider;
			setShellStyle(getShellStyle() | SWT.RESIZE);
			setShowSelectAllButtons(true);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.AbstractDebugCheckboxSelectionDialog#isValid()
		 */
		@Override
		protected boolean isValid() {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getDialogSettingsId()
		 */
		@Override
		protected String getDialogSettingsId() {
			return IJavaDebugUIConstants.PLUGIN_ID + ".INSTANCE_FILTERS_ACTION_DIALOG"; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getHelpContextId()
		 */
		@Override
		protected String getHelpContextId() {
			return IJavaDebugHelpContextIds.INSTANCE_BREAKPOINT_SELECTION_DIALOG;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerInput()
		 */
		@Override
		protected Object getViewerInput() {
			return fInput;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerLabel()
		 */
		@Override
		protected String getViewerLabel() {
			return fMessage;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getLabelProvider()
		 */
		@Override
		protected IBaseLabelProvider getLabelProvider() {
			return fLabelProvider;
		}
}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	@Override
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
					final IJavaObject object = (IJavaObject)value;
					final List<IJavaBreakpoint> breakpoints = getApplicableBreakpoints(var, object);
					final IDebugModelPresentation modelPresentation= DebugUITools.newDebugModelPresentation();

					if (breakpoints.isEmpty())
					{
						MessageDialog.openInformation(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.InstanceFiltersAction_0, ActionMessages.InstanceFiltersAction_4);
						return;
					}

					InstanceFilterDialog dialog = new InstanceFilterDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), breakpoints, modelPresentation, NLS.bind(ActionMessages.InstanceFiltersAction_1, var.getName())){
						@Override
						public void okPressed() {
							// check if breakpoints have already been restricted to other objects.
							Object[] checkBreakpoint= getCheckBoxTableViewer().getCheckedElements();
							for (int k= 0; k < checkBreakpoint.length; k++) {
								IJavaBreakpoint breakpoint= (IJavaBreakpoint) checkBreakpoint[k];
								try {
									IJavaObject[] instanceFilters= breakpoint.getInstanceFilters();
									boolean sameTarget = false;
									for (int i = 0; i < instanceFilters.length; i++) {
										IJavaObject instanceFilter = instanceFilters[i];
										if (instanceFilter.getDebugTarget().equals(object.getDebugTarget())) {
											sameTarget = true;
											break;
										}
									}
									if (sameTarget) {
										MessageDialog messageDialog= new MessageDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.InstanceFiltersAction_2,
												null, NLS.bind(ActionMessages.InstanceFiltersAction_3, modelPresentation.getText(breakpoint), var.getName()),
											MessageDialog.QUESTION, new String[] { ActionMessages.InstanceFiltersAction_Yes_2, ActionMessages.InstanceFiltersAction_Cancel_3}, //
											0);
										if (messageDialog.open() == Window.OK) {
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
					dialog.setTitle(ActionMessages.InstanceFiltersAction_2);

					// determine initial selection
					List<IJavaBreakpoint> existing = new ArrayList<>();
					Iterator<IJavaBreakpoint> iter = breakpoints.iterator();
					while (iter.hasNext()) {
						IJavaBreakpoint bp = iter.next();
						IJavaObject[] filters = bp.getInstanceFilters();
						for (int i = 0; i < filters.length; i++) {
							if (filters[i].equals(object)) {
								existing.add(bp);
								break;
							}
						}
					}
					dialog.setInitialSelections(existing.toArray());

					if (dialog.open() == Window.OK) {
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
								IJavaBreakpoint bp = iter.next();
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

	protected List<IJavaBreakpoint> getApplicableBreakpoints(IJavaVariable variable, IJavaObject object) {
		List<IJavaBreakpoint> breakpoints = new ArrayList<>();

		try {
			// collect names in type hierarchy
			List<String> superTypeNames = new ArrayList<>();
			IJavaType type = object.getJavaType();
			while (type instanceof IJavaClassType) {
				superTypeNames.add(type.getName());
				type = ((IJavaClassType)type).getSuperclass();
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
					if (valid != null && valid.supportsInstanceFilters()) {
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
