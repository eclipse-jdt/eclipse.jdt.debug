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


import org.eclipse.debug.ui.actions.IPopupInformationControlAdapter;
import org.eclipse.debug.ui.actions.PopupInformationControl;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;


public class PopupDisplayAction extends DisplayAction implements IInformationProvider {
	
	private ITextViewer viewer;
	private String snippet;
	private String resultString;

	public String getInformation(ITextViewer textViewer, IRegion subject) {
		return resultString;
	}

	public IRegion getSubject(ITextViewer textViewer, int offset) {
		return getRegion();
	}
	
	private void showPopup() {		
		final IAction action = new Action() {
			public void run() {
				moveToViewer();
			}
		};
		action.setText(ActionMessages.getString("PopupDisplayAction.6")); //$NON-NLS-1$
		action.setToolTipText(ActionMessages.getString("PopupDisplayAction.6")); //$NON-NLS-1$
		
		final InformationPresenter infoPresenter = new InformationPresenter(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new PopupInformationControl(parent, new DisplayInformationControlAdapter(), action);
			}
		});
		

		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				Point p = viewer.getSelectedRange();
				IDocument doc = viewer.getDocument();
				try {
					String contentType = doc.getContentType(p.x);
					infoPresenter.setInformationProvider(PopupDisplayAction.this, contentType);				
					
					infoPresenter.install(viewer);
					infoPresenter.showInformation();
				} catch (BadLocationException e) {
					return;
				}				
			}
		});	
		
		
	}

	public void moveToViewer() {
		final IDataDisplay directDisplay= getDirectDataDisplay();
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed()) {
					IDataDisplay dataDisplay= getDataDisplay();
					if (dataDisplay != null) {
						if (directDisplay == null) {
							dataDisplay.displayExpression(snippet);
						}
						dataDisplay.displayExpressionValue(resultString);
					}
				}
				evaluationCleanup();
			}
		});
	}
	
	private class DisplayInformationControlAdapter implements IPopupInformationControlAdapter {
		private StyledText text;
		
		public boolean hasContents() {
			return (text != null && text.getCharCount() >0);
		}
		
		public boolean isFocusControl() {
			return text.isFocusControl();
		}
		
		public  Composite createInformationComposite(Shell parent) {			
			GridData gd = new GridData(GridData.FILL_BOTH);
			text = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL );
			text.setFont(viewer.getTextWidget().getFont());
//			gd.widthHint = 300;
//			gd.heightHint = 175;
			text.setLayoutData(gd);
			
			text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

			return text;
		}




		public void setInformation(String information) {
			if(information != null)
				text.setText(information);
		}
	}

	protected void displayStringResult(final String snippet,final String resultString) {
		IWorkbenchPart part = getTargetPart();
		viewer = (ITextViewer) part.getAdapter(ITextViewer.class);
		if (viewer == null) {
			if (part instanceof JavaEditor) {
				viewer = ((JavaEditor)part).getViewer();
			}
		}
		if (viewer == null) {
			super.displayStringResult(snippet, resultString);
		} else {
			this.snippet = snippet;
			this.resultString = resultString;
			showPopup();
		}
	}
}
