/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

 
import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.PopupInformationControl;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.JavaWordFinder;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;


public class PopupInspectAction extends EvaluateAction implements IInformationProvider {
	private TextViewer viewer;
	
	/**
	 * @see EvaluateAction#displayResult(IEvaluationResult)
	 */
	protected void displayResult(final IEvaluationResult result) {
				
		final InformationPresenter infoPresenter = new InformationPresenter(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				final JavaInspectExpression expression = new JavaInspectExpression(result);
				IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
				IAction action = new Action() {
					public void run() {
						DebugPlugin.getDefault().getExpressionManager().addExpression(expression);	
						showExpressionView();						
					}
				};
				action.setText(ActionMessages.getString("InspectAction.1")); //$NON-NLS-1$
				action.setToolTipText(ActionMessages.getString("InspectAction.2")); //$NON-NLS-1$
				return new PopupInformationControl(parent, DebugUITools.newExpressionInformationControlAdapter(page, expression), action);
			}
		});
		

		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				Point p = viewer.getSelectedRange();
				IDocument doc = viewer.getDocument();
				try {
					String contentType = doc.getContentType(p.x);
					infoPresenter.setInformationProvider(PopupInspectAction.this, contentType);				
					
					infoPresenter.install(viewer);

//					Control control = viewer.getTextWidget();
//					Point pixelSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//					GC gc= new GC(control);
//					gc.setFont(control.getFont());
//					int charWidth= gc.getFontMetrics().getAverageCharWidth();
//					int charHeight = gc.getFontMetrics().getHeight();
//					gc.dispose();					
					
//					infoPresenter.setSizeConstraints(0,0, true, false);

					infoPresenter.showInformation();
				} catch (BadLocationException e) {
					return;
				}				
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
	
	protected void run() {
		IWorkbenchPart part= getTargetPart();
		if (part instanceof JavaSnippetEditor) {
			((JavaSnippetEditor)part).evalSelection(JavaSnippetEditor.RESULT_INSPECT);
			return;
		}
		
		Object selection= getSelectedObject();
		if (!(selection instanceof IStructuredSelection)) {
			if (part instanceof JavaEditor) {
				JavaEditor editor = (JavaEditor)part;
				viewer = (TextViewer)editor.getViewer();
			}
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
	
		showExpressionView();
	}
	
	protected IDataDisplay getDataDisplay() {
		return getDirectDataDisplay();
	}

	public IRegion getSubject(ITextViewer textViewer, int offset) {
		StyledText textWidget = viewer.getTextWidget();				
		Point selectedRange = textWidget.getSelectionRange();
		IRegion region = JavaWordFinder.findWord(viewer.getDocument(), selectedRange.x);
		return region;
	}

	public String getInformation(ITextViewer textViewer, IRegion subject) {
//		the ExpressionInformationControlAdapter was constructed with everything that it needs
//		returning null would result in popup not being displayed 
		return "null";  //$NON-NLS-1$
	}

}
