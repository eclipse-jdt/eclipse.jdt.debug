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


import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.debug.ui.actions.PopupInformationControl;
import org.eclipse.debug.ui.actions.IPopupInformationControlAdapter;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;


public class DisplayAction extends EvaluateAction implements IInformationProvider {
	
	private TextViewer viewer;
	private String snippet;
	private String resultString;

	protected void displayResult(final IEvaluationResult evaluationResult) {
		if (evaluationResult.hasErrors()) {
			final Display display = JDIDebugUIPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				public void run() {					
					if (display.isDisposed()) {
						return;
					}
					reportErrors(evaluationResult);
					evaluationCleanup();
				}
			});
			return;
		} 		
		
		
		snippet= evaluationResult.getSnippet();
		IJavaValue resultValue= evaluationResult.getValue();
		try {
			String sig= null;
			IJavaType type= resultValue.getJavaType();
			if (type != null) {
				sig= type.getSignature();
			}
			if ("V".equals(sig)) { //$NON-NLS-1$
				resultString = ActionMessages.getString("DisplayAction.no_result_value"); //$NON-NLS-1$
			} else {
				if (sig != null) {
					resultString= MessageFormat.format(ActionMessages.getString("DisplayAction.type_name_pattern"), new Object[] { resultValue.getReferenceTypeName() }); //$NON-NLS-1$
				} else {
					resultString= ""; //$NON-NLS-1$
				}

				getDebugModelPresentation().computeDetail(resultValue, new IValueDetailListener() {
					public void detailComputed(IValue value, String result) {
						resultString = MessageFormat.format(ActionMessages.getString("DisplayAction.result_pattern"), new Object[] { resultString, result}); //$NON-NLS-1$;
						showPopup();
					}
				});

			}
		} catch (DebugException x) {
			resultString = getExceptionMessage(x);
			showPopup();
		}
	}

	public String getInformation(ITextViewer textViewer, IRegion subject) {
		return snippet + " - " +resultString; //$NON-NLS-1$
	}

	public IRegion getSubject(ITextViewer textViewer, int offset) {
		StyledText textWidget = viewer.getTextWidget();				
		Point selectedRange = textWidget.getSelectionRange();
		IRegion region = JavaWordFinder.findWord(viewer.getDocument(), selectedRange.x);
		return region;
	}
	
	
	private void showPopup() {
		final IAction action = new Action() {
			public void run() {
				moveToViewer();
			}
		};
		action.setText(ActionMessages.getString("DisplayAction.6")); //$NON-NLS-1$
		action.setToolTipText(ActionMessages.getString("DisplayAction.7")); //$NON-NLS-1$
		
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
					infoPresenter.setInformationProvider(DisplayAction.this, contentType);				
					
					infoPresenter.install(viewer);
					infoPresenter.showInformation();
				} catch (BadLocationException e) {
					return;
				}				
			}
		});	
		
		
	}
	
	protected void run() {
		IWorkbenchPart part= getTargetPart();
		if (part instanceof JavaSnippetEditor) {
			((JavaSnippetEditor) part).evalSelection(JavaSnippetEditor.RESULT_DISPLAY);
			return;
		}
		
		JavaEditor editor = (JavaEditor)part;
		viewer = (TextViewer)editor.getViewer();
		super.run();
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
			text = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL );
			text.setFont(parent.getFont());
			text.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_BOTH));
			
			text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

			return text;
		}




		public void setInformation(String information) {
			if(information != null)
				text.setText(information);
		}
	}
	
}
