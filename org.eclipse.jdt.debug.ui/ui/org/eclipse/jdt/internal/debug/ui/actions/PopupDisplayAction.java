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
import org.eclipse.debug.ui.actions.IPopupInformationControlAdapter;
import org.eclipse.debug.ui.actions.PopupInformationControl;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.dialogs.IDialogSettings;
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
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.IHandler;


public class PopupDisplayAction extends DisplayAction implements IInformationProvider {
	
	public static final String ACTION_DEFINITION_ID = "org.eclipse.jdt.debug.ui.commands.Display"; //$NON-NLS-1$
	
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
		final IHandler handler = new AbstractHandler() {
			public Object execute(Map parameter) throws ExecutionException {
				IDataDisplay directDisplay= getDirectDataDisplay();
				Display display= JDIDebugUIPlugin.getStandardDisplay();
				
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
				return null;
			}			
		};
		final InformationPresenter infoPresenter = new InformationPresenter(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new PopupInformationControl(parent, new DisplayInformationControlAdapter(ActionMessages.getString("PopupDisplayAction.2"), ACTION_DEFINITION_ID), handler); //$NON-NLS-1$
			}
		});
		
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

	private class DisplayInformationControlAdapter implements IPopupInformationControlAdapter {
		private StyledText text;
		private String label;
		private String actionDefinitionId;
		
		DisplayInformationControlAdapter(String label, String actionDefinitionId) {
			this.label = label;
			this.actionDefinitionId = actionDefinitionId;
		}
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
			text.setLayoutData(gd);
			
			text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

			return text;
		}

		public void setInformation(String information) {
			if(information != null)
				text.setText(information);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.actions.IPopupInformationControlAdapter#getDialogSettings()
		 */
		public IDialogSettings getDialogSettings() {
			return JDIDebugUIPlugin.getDefault().getDialogSettings();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.actions.IPopupInformationControlAdapter#getLabel()
		 */
		public String getLabel() {
			return label;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.actions.IPopupInformationControlAdapter#getActionDefinitionId()
		 */
		public String getActionDefinitionId() {
			return actionDefinitionId;
		}
	}

	protected void displayStringResult(String currentSnippet, String currentResultString) {
		IWorkbenchPart part = getTargetPart();
		viewer = (ITextViewer) part.getAdapter(ITextViewer.class);
		if (viewer == null) {
			if (part instanceof JavaEditor) {
				viewer = ((JavaEditor)part).getViewer();
			}
		}
		if (viewer == null) {
			super.displayStringResult(currentSnippet, currentResultString);
		} else {
			snippet = currentSnippet;
			resultString = currentResultString;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					showPopup();
				}
			});
		}
	}


}
