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
package org.eclipse.jdt.internal.debug.ui.actions;

 
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
	@Override
	protected void displayResult(final IEvaluationResult result) {
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed()) {				
					showExpressionView();
					JavaInspectExpression exp = new JavaInspectExpression(result);
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
		if (getTargetPart().getSite().getId().equals(IDebugUIConstants.ID_EXPRESSION_VIEW)) {
			return;
		}
		IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
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
	
	@Override
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
		Iterator<IJavaVariable> variables = ((IStructuredSelection)selection).iterator();
		while (variables.hasNext()) {
			IJavaVariable var = variables.next();
			try {
				JavaInspectExpression expr = new JavaInspectExpression(var.getName(), (IJavaValue)var.getValue());
				DebugPlugin.getDefault().getExpressionManager().addExpression(expr);
			} catch (DebugException e) {
				JDIDebugUIPlugin.statusDialog(e.getStatus()); 
			}
		}
	
		showExpressionView();
	}
	
	@Override
	protected IDataDisplay getDataDisplay() {
		return getDirectDataDisplay();
	}
}
