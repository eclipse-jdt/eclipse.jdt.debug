/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListValue;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ObjectActionDelegate;
import org.eclipse.jdt.internal.debug.ui.actions.PopupInspectAction;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Action to browse all references to selected object.
 * 
 * @since 3.3
 */
public class AllReferencesActionDelegate extends ObjectActionDelegate implements IWorkbenchWindowActionDelegate{

	protected IWorkbenchWindow fWindow;
	
	public void run(IAction action) {
		IStructuredSelection currentSelection = getCurrentSelection();
		if (currentSelection != null && (currentSelection.getFirstElement() instanceof IJavaVariable)){
			IJavaVariable var = (IJavaVariable) currentSelection.getFirstElement();
			try {
				JDIReferenceListValue referenceList = new JDIReferenceListValue((IJavaObject) var.getValue());
				InspectPopupDialog ipd = new InspectPopupDialog(getShell(), 
						getAnchor((IDebugView) getPart().getAdapter(IDebugView.class)), 
						PopupInspectAction.ACTION_DEFININITION_ID,
						new JavaInspectExpression(NLS.bind(Messages.AllReferencesActionDelegate_1,new String[]{var.getName()}),referenceList));
				ipd.open();
			} catch (DebugException e) {
				JDIDebugUIPlugin.statusDialog(e.getStatus());
			}
		} else {
			JDIDebugUIPlugin.statusDialog(new Status(IStatus.WARNING,JDIDebugUIPlugin.getUniqueIdentifier(),Messages.AllReferencesActionDelegate_0));
		}
	}
	
	/**
	 * Compute an anchor based on selected item in the tree.
	 * 
	 * @param view anchor view
	 * @return anchor point
	 */
    protected static Point getAnchor(IDebugView view) {
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow = window;
	}

	/**
	 * @return the shell to use for new popups or <code>null</code>
	 */
	private Shell getShell(){
		if (fWindow != null){
			return fWindow.getShell();
		}
		if (getWorkbenchWindow() != null){
			return getWorkbenchWindow().getShell();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ObjectActionDelegate#getPart()
	 */
	@Override
	protected IWorkbenchPart getPart() {
		IWorkbenchPart part = super.getPart();
		if (part != null){
			return part;
		} else if (fWindow != null){
			return fWindow.getActivePage().getActivePart();
		}
		return null;
	}
	
}
