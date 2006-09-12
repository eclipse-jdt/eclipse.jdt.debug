/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ObjectActionDelegate;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * Class to provide new function of viewing all live objects of the selected type in the current VM
 * New feature of 1.6 Mustang VMs
 * 
 * @since 3.3
 *
 */
public class AllInstancesActionDelegate extends ObjectActionDelegate implements IEditorActionDelegate {

	public static final String ACTION_ID = JDIDebugUIPlugin.getUniqueIdentifier() + ".all_instances"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection currentSelection = getCurrentSelection();
		IJavaVariable var = (IJavaVariable) currentSelection.getFirstElement();
		try {
			IJavaType type = var.getJavaType();
			if(type instanceof JDIReferenceType) {
				JDIReferenceType rtype = (JDIReferenceType) type;
				long count = JDIDebugUIPlugin.getDefault().getPreferenceStore().getLong(IJavaDebugUIConstants.PREF_ALLINSTANCES_MAX_COUNT);
				JDIAllInstancesValue aiv = new JDIAllInstancesValue((JDIDebugTarget) type.getDebugTarget(), rtype.getInstances(count));
				InspectPopupDialog ipd = new InspectPopupDialog(getWorkbenchWindow().getShell(), 
						getAnchor((IDebugView) getPart().getAdapter(IDebugView.class)), 
						ACTION_ID, 
						new JavaInspectExpression(var.getName(), aiv));
				ipd.open();
			}
		} catch (DebugException e) {}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {}
	
	/**
	 * Compute an anchor based on selected item in the tree.
	 * 
	 * @param view anchor view
	 * @return anchor point
	 */
    protected Point getAnchor(IDebugView view) {
		Control control = view.getViewer().getControl();
		if (control instanceof Tree) {
			Tree tree = (Tree) control;
			TreeItem[] selection = tree.getSelection();
			if (selection.length > 0) {
				Rectangle bounds = selection[0].getBounds();
				return tree.toDisplay(new Point(bounds.x, bounds.y + bounds.height));
			}
		}
		return control.toDisplay(0, 0);    	
    }
}
