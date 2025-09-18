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
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jface.action.IAction;
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
		if (selection.getFirstElement() instanceof IJavaVariable variable) {
			try {
				Object clpbrd = getClipboard();
				if (clpbrd != null) {
					if (variable.getValue() instanceof IJavaObject javaObject) {
						JDIStackFrame frame = (JDIStackFrame) DebugUITools.getDebugContext();
						IJavaThread thread = (IJavaThread) frame.getThread();
						IJavaValue stringVal = javaObject.sendMessage("toString", "()Ljava/lang/String;", null, thread, false); //$NON-NLS-1$//$NON-NLS-2$
						String variableValue = stringVal.getValueString();
						String cb = clpbrd.toString();
						String variableName = variable.getName();
						compareVariable(variableValue, variableName, cb);
					}
				}
			} catch (DebugException e) {
				DebugUIPlugin.log(e);
			}
		}
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
		config.setLeftEditable(false);
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
		return clip.getContents(TextTransfer.getInstance());
	}

}
