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


import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.PopupInformationControl;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;


public class PopupInspectAction extends InspectAction implements IInformationProvider {
	private ITextViewer viewer;
	private JavaInspectExpression expression;
	
	/**
	 * @see EvaluateAction#displayResult(IEvaluationResult)
	 */
	protected void displayResult(final IEvaluationResult result) {
		IWorkbenchPart part = getTargetPart();
		viewer = (ITextViewer) part.getAdapter(ITextViewer.class);
		if (viewer == null) {
			if (part instanceof JavaEditor) {
				viewer = ((JavaEditor)part).getViewer();
			}
		}
		if (viewer == null) {
			super.displayResult(result);
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					showPopup(result);
				}
			});
		}		
	}
	
	protected void showPopup(final IEvaluationResult result) {
		final InformationPresenter infoPresenter = new InformationPresenter(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				IAction action = new MoveResultToViewerAction(new MoveToViewerRunnable());
				action.setText(ActionMessages.getString("PopupInspectAction.3")); //$NON-NLS-1$
				IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
				expression = new JavaInspectExpression(result);
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
					infoPresenter.showInformation();
				} catch (BadLocationException e) {
					return;
				}				
			}
		});
	}
	
	public IRegion getSubject(ITextViewer textViewer, int offset) {
		return getRegion();
	}
	
	public String getInformation(ITextViewer textViewer, IRegion subject) {
//		the ExpressionInformationControlAdapter was constructed with everything that it needs
//		returning null would result in popup not being displayed 
		return "not null";  //$NON-NLS-1$
	}
	
	
	private class MoveToViewerRunnable implements Runnable {
		public void run() {
			DebugPlugin.getDefault().getExpressionManager().addExpression(expression);	
			showExpressionView();
		}
	}

}
