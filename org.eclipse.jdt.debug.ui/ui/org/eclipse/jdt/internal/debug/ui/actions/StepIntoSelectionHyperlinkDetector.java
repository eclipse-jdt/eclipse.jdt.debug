/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.EvaluationContextManager;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaWordFinder;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

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
		
		private IRegion fRegion = null;
		
		/**
		 * Constructor
		 * @param region
		 */
		public StepIntoSelectionHyperlink(IRegion region) {
			fRegion = region;
		}
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
		 */
		public IRegion getHyperlinkRegion() {
			return fRegion;
		}
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
		 */
		public String getHyperlinkText() {
			return ActionMessages.StepIntoSelectionHyperlinkDetector_0;
		}
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
		 */
		public String getTypeLabel() {
			return null;
		}
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
		 */
		public void open() {
			StepIntoSelectionActionDelegate delegate = new StepIntoSelectionActionDelegate(fRegion);
			delegate.init(JDIDebugUIPlugin.getActiveWorkbenchWindow());
			delegate.run(null);
		}
		
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor editor = (ITextEditor) getAdapter(ITextEditor.class);
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
							IMethod method = StepIntoSelectionUtils.getMethod(new TextSelection(document, wregion.getOffset(), wregion.getLength()), element);
							if (method != null) {
								return new IHyperlink[] {new StepIntoSelectionHyperlink(wregion)};
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
