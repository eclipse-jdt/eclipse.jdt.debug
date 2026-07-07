/*******************************************************************************
 * Copyright (c) 2025, 2026 IBM Corporation.
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
package org.eclipse.jdt.internal.debug.ui.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.core.model.JDIPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class CompareObjectWithClipboard extends ObjectActionDelegate {

	@Override
	public void run(IAction action) {
		IStructuredSelection selection = getCurrentSelection();
		if (selection == null) {
			return;
		}

		Object clpbrd = getClipboard();
		if (clpbrd == null) {
			MessageDialog.openWarning(DebugUIPlugin.getShell(), "Comparison Failed", "Invalid clipboard content"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		if (selection.getFirstElement() instanceof IJavaVariable variable) {
			try {
				handleCompareForValue(variable.getName(), clpbrd.toString(), variable.getValue());
				return;
			} catch (DebugException e) {
				DebugUIPlugin.log(e);
			}
		}

		if (selection.getFirstElement() instanceof IExpression expression) {
			try {
				String cb = clpbrd.toString();
				String title = "Selected Expression"; //$NON-NLS-1$
				if (expression instanceof JavaInspectExpression) {
					handleCompareForValue(title, cb, expression.getValue());
					return;
				}
				IValue expValue = expression.getValue();
				if (expValue != null) {
					compareVariable(expValue.getValueString(), title, cb);
				}
			} catch (DebugException e) {
				DebugUIPlugin.log(e);
			}
		}
	}

	/**
	 * Resolves the string representation of <code>value</code> and opens the compare editor.
	 *
	 * @param title
	 *            label for the left side of the compare editor
	 * @param clipboard
	 *            active clipboard content to compare against
	 * @param value
	 *            value of the selected variable or expression
	 * @throws DebugException
	 *             if evaluating the value or retrieving array elements fails
	 */
	private void handleCompareForValue(String title, String clipboard, IValue value) throws DebugException {

		if (value instanceof IJavaObject javaObject && !(javaObject instanceof JDINullValue)) {
			if (javaObject.getJavaType() instanceof IJavaArrayType) {
				handleCompareForArrayTypes(title, clipboard, value.getVariables());
				return;
			}
			if (!(DebugUITools.getDebugContext() instanceof JDIStackFrame frame)) {
				return;
			}
			IJavaThread thread = (IJavaThread) frame.getThread();
			IJavaValue stringVal = javaObject.sendMessage("toString", "()Ljava/lang/String;", null, thread, false); //$NON-NLS-1$//$NON-NLS-2$
			compareVariable(stringVal.getValueString(), title, clipboard);
		} else if (value instanceof JDIPrimitiveValue javaPrimitive) {
			compareVariable(javaPrimitive.getValueString(), title, clipboard);
		}
	}

	/**
	 * Builds a string representation of the given array variables and opens the compare editor.
	 *
	 * @param title
	 *            Label shown on the left side of the compare editor (variable name or expression text)
	 * @param clipboard
	 *            Active clipboard content to compare against
	 * @param variables
	 *            The array elements as <code>IVariable[]</code>, obtained from the array value
	 * @throws DebugException
	 *             if retrieving a variable's value fails
	 */
	private void handleCompareForArrayTypes(String title, String clipboard, IVariable[] variables) throws DebugException {
		if (variables.length == 0) {
			compareVariable("[]", title, clipboard); //$NON-NLS-1$
			return;
		}
		StringBuilder arrBuilder = new StringBuilder("["); //$NON-NLS-1$
		for (IVariable variableTemp : variables) {
			arrBuilder.append(variableTemp.getValue() + ", "); //$NON-NLS-1$
		}
		arrBuilder.delete(arrBuilder.length() - 2, arrBuilder.length());
		arrBuilder.append("]"); //$NON-NLS-1$
		compareVariable(arrBuilder.toString(), title, clipboard);
	}

	/**
	 * Creates comparable objects
	 *
	 * @param variableValue
	 *            <code>toString()</code> value of the <code>IJavaVariable </code>
	 * @param variableName
	 *            Variable name of the selected <code>IJavaVariable </code>
	 * @param clipboardContent
	 *            Active clipboard content
	 */
	private void compareVariable(String variableValue, String variableName, String clipboardContent) {

		class StringTypedElement implements ITypedElement, IStreamContentAccessor {
			private final String name;
			private final String content;

			public StringTypedElement(String name, String content) {
				this.name = name;
				this.content = content;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public Image getImage() {
				return null;
			}

			@Override
			public String getType() {
				return "variable"; //$NON-NLS-1$
			}

			@Override
			public InputStream getContents() {
				return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			}
		}
		StringTypedElement variable = new StringTypedElement(variableName, variableValue);
		StringTypedElement clipboard = new StringTypedElement("Clipboard", clipboardContent); //$NON-NLS-1$

		CompareConfiguration config = new CompareConfiguration();
		config.setLeftLabel(variableName);
		config.setRightLabel("Contents in clipboard"); //$NON-NLS-1$
		config.setLeftEditable(false);
		config.setRightEditable(false);
		CompareEditorInput compareInput = new CompareEditorInput(config) {
			@Override
			protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				return new DiffNode(variable, clipboard);

			}
		};

		CompareUI.openCompareEditor(compareInput);
	}

	/**
	 * Returns current clipboard text instance or null
	 *
	 * @returns clipboard text contents or null
	 *
	 */
	private Object getClipboard() {
		Clipboard clip = new Clipboard(Display.getDefault());
		try {
			return clip.getContents(TextTransfer.getInstance());
		} finally {
			clip.dispose();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection ss) {
			if (ss.getFirstElement() instanceof IExpression expression) {
				if (expression.getValue() == null) {
					action.setEnabled(false);
					return;
				}
				IDebugTarget target = expression.getDebugTarget();
				if (target == null) {
					action.setEnabled(false);
					return;
				}
				if (target.isTerminated() || target.isDisconnected()) {
					action.setEnabled(false);
				}
			}
		}
	}

}
