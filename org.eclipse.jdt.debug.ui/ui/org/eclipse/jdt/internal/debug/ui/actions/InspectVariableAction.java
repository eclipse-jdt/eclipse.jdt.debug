package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Adds a variable to the inspector
 */
public class InspectVariableAction extends Action implements IViewActionDelegate {
	
	private IStructuredSelection fVariables = null;

	/**
	 * Constructor for InspectVariableAction.
	 */
	public InspectVariableAction() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		Iterator variables = getVariables();
		while (variables.hasNext()) {
			IJavaVariable var = (IJavaVariable)variables.next();
			try {
				JavaInspectExpression expr = new JavaInspectExpression(var.getName(), (IJavaValue)var.getValue());
				DebugPlugin.getDefault().getExpressionManager().addExpression(expr);
			} catch (DebugException e) {
				JDIDebugUIPlugin.errorDialog(ActionMessages.getString("InspectVariableAction.Exception_occurred_inspecting_variable"), e.getStatus()); //$NON-NLS-1$
			}
		}
		IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
			if (part != null) {
				page.activate(part);
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(isEnabledFor(selection));
		
	}
	
	/**
	 * Returns whethter this action is enabled for the given selection.
	 * This action only enables for IJavaVariables
	 * 
	 * @return whethter this action is enabled for the given selection
	 */
	protected boolean isEnabledFor(ISelection selection) {
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			fVariables = (IStructuredSelection)selection;
			Iterator elements = fVariables.iterator();
			while (elements.hasNext()) {
				if (!(elements.next() instanceof IJavaVariable)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns an iterator of IJavaVariables currently selected
	 * 
	 * @return an iterator of IJavaVariables currently selected
	 */
	protected Iterator getVariables() {
		return fVariables.iterator();
	}
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
	}

}
