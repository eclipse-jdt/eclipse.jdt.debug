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


import java.util.Map;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IPopupInformationControlAdapter;
import org.eclipse.debug.ui.actions.PopupInformationControl;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
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
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.IHandler;


public class PopupInspectAction extends InspectAction implements IInformationProvider {
	
	public static final String ACTION_DEFININIITION_ID = "org.eclipse.jdt.debug.ui.commands.Inspect"; //$NON-NLS-1$
	
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
				IHandler handler = new AbstractHandler() {
					public Object execute(Map parameter) throws ExecutionException {
						DebugPlugin.getDefault().getExpressionManager().addExpression(expression);	
						showExpressionView();
						return null;
					}				
				};
				
				IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
				expression = new JavaInspectExpression(result);
				IPopupInformationControlAdapter adapter = DebugUITools.newExpressionInformationControlAdapter(page, expression, ActionMessages.getString("PopupInspectAction.3"), ACTION_DEFININIITION_ID); //$NON-NLS-1$
				return new PopupInformationControl(parent, adapter, handler);
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
	
	

}
