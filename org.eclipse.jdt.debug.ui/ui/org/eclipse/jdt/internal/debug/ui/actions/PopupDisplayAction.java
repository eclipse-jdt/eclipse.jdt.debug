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

import java.text.MessageFormat;

import org.eclipse.debug.ui.DebugPopup;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.DisplayView;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

public class PopupDisplayAction extends DisplayAction {

    public static final String ACTION_DEFINITION_ID = "org.eclipse.jdt.debug.ui.commands.Display"; //$NON-NLS-1$

    private ITextViewer viewer;

    private String snippet;

    private String resultString;

    public PopupDisplayAction() {
        super();
    }

    private void showPopup() {
        DebugPopup displayPopup = new DisplayPopup(getShell(), viewer);
        displayPopup.open();
    }

    private class DisplayPopup extends DebugPopup {
        public DisplayPopup(Shell shell, ITextViewer viewer) {
            super(shell, viewer);
        }

        protected String getInfoText() {
            IWorkbench workbench = PlatformUI.getWorkbench();
            IBindingService bindingService = (IBindingService) workbench.getAdapter(IBindingService.class);
            TriggerSequence[] bindings = bindingService.getActiveBindingsFor(ACTION_DEFINITION_ID);
            String infoText = null;
            if (bindings.length > 0) {
                 infoText = MessageFormat.format(ActionMessages.PopupDisplayAction_1, new String[] { bindings[0].format(), ActionMessages.PopupDisplayAction_2 });
            }
            return infoText;
        }

        protected String getCommandId() {
            return ACTION_DEFINITION_ID;
        }

        protected void persist() {
            IDataDisplay directDisplay = getDirectDataDisplay();
            Display display = JDIDebugUIPlugin.getStandardDisplay();

            if (!display.isDisposed()) {
                IDataDisplay dataDisplay = getDataDisplay();
                if (dataDisplay != null) {
                    if (directDisplay == null) {
                        dataDisplay.displayExpression(snippet);
                    }
                    dataDisplay.displayExpressionValue(resultString);
                }
            }
        }

        protected Control createDialogArea(Composite parent) {
            GridData gd = new GridData(GridData.FILL_BOTH);
            StyledText text = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
            text.setLayoutData(gd);

            text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

            text.setText(resultString);
            return text;
        }
    }

    protected void displayStringResult(String currentSnippet, String currentResultString) {
        IWorkbenchPart part = getTargetPart();

        if (part instanceof DisplayView) {
            super.displayStringResult(currentSnippet, currentResultString);
            return;
        }
        
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
            evaluationCleanup();
        }
    }

}
