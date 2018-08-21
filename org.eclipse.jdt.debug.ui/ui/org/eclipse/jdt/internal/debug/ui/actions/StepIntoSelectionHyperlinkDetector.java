/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

import org.eclipse.jdt.debug.core.IJavaStackFrame;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import org.eclipse.ui.IEditorInput;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.debug.ui.DebugUITools;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.debug.ui.EvaluationContextManager;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaWordFinder;

/**
 * This is a specialization of a hyperlink detector for the step into selection command
 *
 * @since 3.3
 */
public class StepIntoSelectionHyperlinkDetector extends AbstractHyperlinkDetector {

	/**
	 * Specific implementation of a hyperlink for step into command
	 */
	class StepIntoSelectionHyperlink implements IHyperlink {

		private ITextSelection fSelection = null;

		/**
		 * Constructor
		 * @param region
		 */
		public StepIntoSelectionHyperlink(ITextSelection selection) {
			fSelection = selection;
		}
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
		 */
		@Override
		public IRegion getHyperlinkRegion() {
			return new Region(fSelection.getOffset(), fSelection.getLength());
		}
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
		 */
		@Override
		public String getHyperlinkText() {
			return ActionMessages.StepIntoSelectionHyperlinkDetector_0;
		}
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
		 */
		@Override
		public String getTypeLabel() {
			return null;
		}
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
		 */
		@Override
		public void open() {
			StepIntoSelectionUtils.stepIntoSelection(fSelection);
		}

	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
	 */
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor editor = getAdapter(ITextEditor.class);
		if(editor != null && EvaluationContextManager.getEvaluationContext(JDIDebugUIPlugin.getActiveWorkbenchWindow()) != null) {

			// should only enable step into selection when the current debug context
			// is an instance of IJavaStackFrame
			IAdaptable debugContext = DebugUITools.getDebugContext();
			if (!(debugContext instanceof IJavaStackFrame)) {
				return null;
			}
			IEditorInput input = editor.getEditorInput();
			IJavaElement element = StepIntoSelectionUtils.getJavaElement(input);
			int offset = region.getOffset();
			if(element != null) {
				try {
					IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
					if(document != null) {
						IRegion wregion = JavaWordFinder.findWord(document, offset);
						if(wregion != null) {
							ITextSelection selection = new TextSelection(document, wregion.getOffset(), wregion.getLength());
							IMethod method = StepIntoSelectionUtils.getMethod(selection, element);
							if (method != null) {
								return new IHyperlink[] {new StepIntoSelectionHyperlink(selection)};
							}
						}
					}
				}
				catch(JavaModelException jme) {JDIDebugUIPlugin.log(jme);}
			}
		}
		return null;
	}
}
