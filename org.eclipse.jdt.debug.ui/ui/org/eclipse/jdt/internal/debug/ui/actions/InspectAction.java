package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * Places the result of an evaluation in the debug expression view.
 */
public class InspectAction extends EvaluateAction {
	
	/**
	 * @see EvaluateAction#displayResult(IEvaluationResult)
	 */
	protected void displayResult(final IEvaluationResult result) {
		final IJavaValue value= result.getValue();
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed()) {				
					showExpressionView();
					JavaInspectExpression exp = new JavaInspectExpression(result.getSnippet().trim(), value);
					DebugPlugin.getDefault().getExpressionManager().addExpression(exp);
				}
				evaluationCleanup();
			}
		});
	}
	
	/**
	 * Make the expression view visible or open one
	 * if required.
	 */
	protected void showExpressionView() {
		IWorkbenchPage page = JDIDebugUIPlugin.getDefault().getActivePage();
		if (page != null) {
			IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
			if (part == null) {
				try {
					page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
				} catch (PartInitException e) {
					reportError(e.getStatus().getMessage());
				}
			} else {
				page.bringToTop(part);
			}
		}
	}
	
	protected void run() {
		IWorkbenchPart part= getTargetPart();
		if (part instanceof JavaSnippetEditor) {
			((JavaSnippetEditor)part).evalSelection(JavaSnippetEditor.RESULT_INSPECT);
			return;
		}
		
		Object selection= getSelectedObject();
		if (!(selection instanceof IStructuredSelection)) {
			super.run();
			return;
		}
		
		//inspecting from the context of the variables view
		Iterator variables = ((IStructuredSelection)selection).iterator();
		while (variables.hasNext()) {
			IJavaVariable var = (IJavaVariable)variables.next();
			try {
				JavaInspectExpression expr = new JavaInspectExpression(var.getName(), (IJavaValue)var.getValue());
				DebugPlugin.getDefault().getExpressionManager().addExpression(expr);
			} catch (DebugException e) {
				JDIDebugUIPlugin.errorDialog(ActionMessages.getString("InspectAction.Exception_occurred_inspecting_variable"), e); //$NON-NLS-1$
			}
		}
	
		if (part.getSite().getId().equals(IDebugUIConstants.ID_EXPRESSION_VIEW)) {
			return;
		}
		IWorkbenchPage page = part.getSite().getPage();
		if (page != null) {
			part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
			if (part != null) {
				page.bringToTop(part);
			}
		}
	}
	
	protected IDataDisplay getDataDisplay() {
		return getDirectDataDisplay();
	}
}
