/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.HeapWalkingManager;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ObjectActionDelegate;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.ibm.icu.text.MessageFormat;

/**
 * Class to provide new function of viewing all live objects of the selected type in the current VM
 * Feature of 1.6 VMs
 * 
 * @since 3.3
 */
public class AllInstancesActionDelegate extends ObjectActionDelegate implements IEditorActionDelegate {

	public static final String ACTION_ID = JDIDebugUIPlugin.getUniqueIdentifier() + ".all_instances"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection currentSelection = getCurrentSelection();
		Object object = currentSelection.getFirstElement();
		IJavaType type = null;
		Point anchor = null;
		try {
			if(object instanceof IType) {
				IAdaptable adapt = DebugUITools.getDebugContext();
				if(adapt != null) {
					IJavaDebugTarget target = (IJavaDebugTarget) adapt.getAdapter(IJavaDebugTarget.class);
					if(target != null) {
						IType itype = (IType) object;
						IJavaType[] types = target.getJavaTypes(itype.getFullyQualifiedName());
						if(types != null) {
							type = types[0];
						} else {
							MessageDialog.openInformation(JDIDebugUIPlugin.getShell(), Messages.AllInstancesActionDelegate_0, MessageFormat.format(Messages.AllInstancesActionDelegate_1, new String[] {itype.getFullyQualifiedName()}));
						}
					}
				}
			} else if (object instanceof IJavaVariable) {
				IJavaVariable var = (IJavaVariable) currentSelection.getFirstElement();
				type = var.getJavaType();
				anchor = getAnchor((IDebugView) getPart().getAdapter(IDebugView.class));
			}
			
			if(type instanceof JDIReferenceType) {
				JDIReferenceType rtype = (JDIReferenceType) type;
				long count = HeapWalkingManager.getDefault().getAllInstancesMaxCount();
				JDIAllInstancesValue aiv = new JDIAllInstancesValue((JDIDebugTarget) type.getDebugTarget(), rtype.getInstances(count));
				InspectPopupDialog ipd = new InspectPopupDialog(getWorkbenchWindow().getShell(), 
						anchor, 
						"org.eclipse.jdt.debug.ui.commands.Inspect",  //$NON-NLS-1$
						new JavaInspectExpression(MessageFormat.format(Messages.AllInstancesActionDelegate_2, new String[]{type.getName()}), aiv));
				ipd.open();
			}
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e.getStatus());
		}
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
