/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IWorkbenchPart;

public class PopupInspectAction extends InspectAction {

    public static final String ACTION_DEFININITION_ID = "org.eclipse.jdt.debug.ui.commands.Inspect"; //$NON-NLS-1$

    JavaInspectExpression expression;

    /**
     * @see EvaluateAction#displayResult(IEvaluationResult)
     */
    protected void displayResult(final IEvaluationResult result) {
        IWorkbenchPart part = getTargetPart();
        final StyledText styledText = getStyledText(part);
        if (styledText == null) {
            super.displayResult(result);
        } else {
        	expression = new JavaInspectExpression(result);
            JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
                public void run() {
                    showPopup(styledText);
                }
            });
        }

        evaluationCleanup();
    }

    protected void showPopup(StyledText textWidget) {
        DebugPopup displayPopup = new InspectPopupDialog(getShell(), getPopupAnchor(textWidget), ACTION_DEFININITION_ID, expression);
        displayPopup.open();
    }

}
