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

import org.eclipse.debug.ui.DebugPopup;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IWorkbenchPart;

public class PopupInspectAction extends InspectAction {

    public static final String ACTION_DEFININIITION_ID = "org.eclipse.jdt.debug.ui.commands.Inspect"; //$NON-NLS-1$

    private ITextViewer viewer;

    JavaInspectExpression expression;

    /**
     * @see EvaluateAction#displayResult(IEvaluationResult)
     */
    protected void displayResult(final IEvaluationResult result) {
        expression = new JavaInspectExpression(result);
        
        IWorkbenchPart part = getTargetPart();
        viewer = (ITextViewer) part.getAdapter(ITextViewer.class);
        if (viewer == null) {
            if (part instanceof JavaEditor) {
                viewer = ((JavaEditor) part).getViewer();
            }
        }
        if (viewer == null) {
            super.displayResult(result);
        } else {
            JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
                public void run() {
                    showPopup();
                }
            });
        }

        evaluationCleanup();
    }

    protected void showPopup() {
        DebugPopup displayPopup = new InspectPopupDialog(getShell(), getPopupAnchor(viewer), ACTION_DEFININIITION_ID, expression);
        displayPopup.open();
    }

}
