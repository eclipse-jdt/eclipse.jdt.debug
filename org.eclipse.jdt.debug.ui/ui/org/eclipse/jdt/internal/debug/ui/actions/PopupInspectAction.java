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
import org.eclipse.jface.action.Action;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;


public class PopupInspectAction extends InspectAction implements IInformationProvider {
	private ITextViewer viewer;
	
	/**
	 * @see EvaluateAction#displayResult(IEvaluationResult)
	 */
	protected void displayResult(IEvaluationResult result) {
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
			showPopup(result);
		}		
	}
	
	protected void showPopup(final IEvaluationResult result) {
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
				action.setText(ActionMessages.getString("PopupInspectAction.1")); //$NON-NLS-1$
				action.setToolTipText(ActionMessages.getString("PopupInspectAction.1")); //$NON-NLS-1$
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
	
	public IRegion getSubject(ITextViewer textViewer, int offset) {
		return getRegion();
	}

	public String getInformation(ITextViewer textViewer, IRegion subject) {
//		the ExpressionInformationControlAdapter was constructed with everything that it needs
//		returning null would result in popup not being displayed 
		return "null";  //$NON-NLS-1$
	}

}
