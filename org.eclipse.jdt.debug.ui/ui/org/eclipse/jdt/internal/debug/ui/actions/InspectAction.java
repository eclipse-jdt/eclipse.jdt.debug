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
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jface.viewers.ISelection;
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
	 * @see IEvaluationListener#evaluationComplete(IEvaluationResult)
	 */
	public void evaluationComplete(final IEvaluationResult res) {
		final IJavaValue value= res.getValue();
		if (res.hasProblems() || value != null) {
			Display display= Display.getDefault();
			if (display.isDisposed()) {
				return;
			}
			display.asyncExec(new Runnable() {
				public void run() {
					if (res.hasProblems()) {
						reportProblems(res);
					} 
					if (value != null) {
						// make expression view visible
						showExpressionView();
						JavaInspectExpression exp = new JavaInspectExpression(res.getSnippet().trim(), value);
						DebugPlugin.getDefault().getExpressionManager().addExpression(exp);
					}
				}
			});
		}
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
					reportError(e.getStatus());
				}
			} else {
				page.bringToTop(part);
			}
		}
	}

	/**
	 * @see EvaluateAction#getDataDisplay()
	 */
	protected IDataDisplay getDataDisplay() {
		return null;
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
				JDIDebugUIPlugin.log(e.getStatus());
				JDIDebugUIPlugin.errorDialog(ActionMessages.getString("InspectAction.Exception_occurred_inspecting_variable"), e.getStatus()); //$NON-NLS-1$
			}
		}
	
		if (part.getSite().getId().equals("IDebugUIConstants.ID_EXPRESSION_VIEW")) { //$NON-NLS-1$
			return;
		}
		IWorkbenchPage page = part.getSite().getPage();
		if (page != null) {
			part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
			if (part != null) {
				page.activate(part);
			}
		}
	}
}
