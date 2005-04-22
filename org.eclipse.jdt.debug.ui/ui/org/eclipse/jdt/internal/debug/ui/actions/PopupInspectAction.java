/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.debug.internal.ui.views.expression.ExpressionInformationControl;
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
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;


public class PopupInspectAction extends InspectAction implements IInformationProvider {
	
	public static final String ACTION_DEFININIITION_ID = "org.eclipse.jdt.debug.ui.commands.Inspect"; //$NON-NLS-1$
	
	private ITextViewer viewer;
	private JavaInspectExpression expression;

    private InformationPresenter fInformationPresenter;
	
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
			showPopup(result);
		}		
		
		evaluationCleanup();
	}
	
	private InformationPresenter getInformationPresenter() {
	    return fInformationPresenter;
    }
	
	private void setInformationPresenter(InformationPresenter informationPresenter) {
	    fInformationPresenter = informationPresenter;
	}
	
	protected void showPopup(final IEvaluationResult result) {
		final InformationPresenter infoPresenter = new InformationPresenter(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
				expression = new JavaInspectExpression(result);
				ExpressionInformationControl control = new ExpressionInformationControl(page, expression, ACTION_DEFININIITION_ID);
				control.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        getInformationPresenter().uninstall();
                    }
				});
				return control;
			}
		});
		
		setInformationPresenter(infoPresenter);

		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
                if (viewer != null) {
                    Point p = viewer.getSelectedRange();
                    IDocument doc = viewer.getDocument();
                    try {
                        String contentType = TextUtilities.getContentType(doc, infoPresenter.getDocumentPartitioning(), p.x, true);
                        infoPresenter.setInformationProvider(PopupInspectAction.this, contentType);
                        infoPresenter.install(viewer);
                        infoPresenter.showInformation();
                    } catch (BadLocationException e) {
                        return;
                    } finally {
                        viewer = null;
                    }
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
	
    protected IRegion getRegion() {
        Point point = viewer.getSelectedRange();
        return new Region(point.x, point.y);
    }	

}
