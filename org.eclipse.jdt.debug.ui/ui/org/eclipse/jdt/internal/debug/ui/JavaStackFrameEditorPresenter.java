/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
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
package org.eclipse.jdt.internal.debug.ui;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.ui.actions.ToggleBreakpointAdapter;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Handles specialized selection of Java editors during debugging, such as selecting lambda expressions on a breakpoint hit.
 */
public class JavaStackFrameEditorPresenter implements IDebugEditorPresentation {

	@Override
	public boolean addAnnotations(IEditorPart editorPart, IStackFrame frame) {
		if (!(frame instanceof JDIStackFrame jdiFrame && editorPart instanceof ITextEditor textEditor)) {
			return false;
		}
		IDocument document = getDocument(textEditor);
		if (document == null) {
			return false;
		}
		try {
			if (org.eclipse.jdt.internal.debug.core.model.LambdaUtils.isLambdaFrame(jdiFrame)) {
				IEditorInput editorInput = editorPart.getEditorInput();
				if (JavaUI.getEditorInputJavaElement(editorInput) != null) {
					IRegion region = document.getLineInformation(jdiFrame.getLineNumber() - 1);
					List<LambdaExpression> inLineLambdas = ToggleBreakpointAdapter.findLambdaExpressions(textEditor, region);
					for (LambdaExpression exp : inLineLambdas) {
						String key = getMethodBindingKey(exp);
						if (key != null && key.contains(jdiFrame.getName())) {
							textEditor.selectAndReveal(exp.getStartPosition(), exp.getLength());
							return true;
						}
					}
				}
			}
			if (editorPart instanceof ClassFileEditor classFileEditor && document.getLength() == 0) {
				String methodName = jdiFrame.getMethodName();
				if (jdiFrame.isConstructor()) {
					methodName = jdiFrame.getDeclaringTypeName();
					methodName = methodName.substring(methodName.lastIndexOf('.') + 1);
				}
				classFileEditor.highlightInstruction(methodName, jdiFrame.getSignature(), jdiFrame.getCodeIndex());
				return true;
			}
		} catch (CoreException | BadLocationException e) {
			JDIDebugPlugin.log(e);
		}
		return false;
	}

	@Override
	public void removeAnnotations(IEditorPart editorPart, IThread thread) {
		if (!(editorPart instanceof ClassFileEditor classFileEditor)) {
			return;
		}
		IDocument document = getDocument(classFileEditor);
		if (document != null && document.getLength() == 0) {
			classFileEditor.unhighlight();
		}
	}

	private static String getMethodBindingKey(LambdaExpression exp) {
		String key = null;
		IMethodBinding methodBinding = exp.resolveMethodBinding();
		if (methodBinding != null) {
			key = methodBinding.getKey();
		}
		return key;
	}

	private static IDocument getDocument(ITextEditor textEditor) {
		IDocumentProvider documentProvider = textEditor.getDocumentProvider();
		if (documentProvider == null) {
			return null;
		}
		return documentProvider.getDocument(textEditor.getEditorInput());
	}
}
