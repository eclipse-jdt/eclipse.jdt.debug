/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.sourcelookup;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupFacility;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupResult;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.ui.actions.ToggleBreakpointAdapter;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @since 3.2
 */
public class LambdaStackFrameSourceDisplayAdapter implements ISourceDisplay {

	@Override
	public void displaySource(Object element, IWorkbenchPage page, boolean forceSourceLookup) {
		JDIStackFrame jdiFrame = (JDIStackFrame) element;
		try {
			SourceLookupResult sourceRes = SourceLookupFacility.getDefault().lookup(element, jdiFrame.getLaunch().getSourceLocator(), forceSourceLookup);
			IDocumentProvider provider = JavaUI.getDocumentProvider();
			IEditorInput editorInput = sourceRes.getEditorInput();
			provider.connect(editorInput);
			IDocument document = provider.getDocument(editorInput);
			IRegion region = document.getLineInformation(jdiFrame.getLineNumber() - 1);
			IJavaElement je = JavaUI.getEditorInputJavaElement(editorInput);
			if (je != null) {
				IEditorPart part = JavaUI.openInEditor(je);
				if (part instanceof ITextEditor textEditor) {
					List<LambdaExpression> inLineLambdas = ToggleBreakpointAdapter.findLambdaExpressions(textEditor, region);
					for (LambdaExpression exp : inLineLambdas) {
						IMethodBinding methodBinding = exp.resolveMethodBinding();
						String key = methodBinding.getKey();
						if (key.contains(jdiFrame.getName())) {
							textEditor.selectAndReveal(exp.getStartPosition(), exp.getLength());
							return;
						}
					}
				}
			}
		} catch (CoreException | BadLocationException e) {
			DebugUIPlugin.log(e);
		}
		SourceLookupFacility.getDefault().displaySource(jdiFrame, page, forceSourceLookup);
	}
}
