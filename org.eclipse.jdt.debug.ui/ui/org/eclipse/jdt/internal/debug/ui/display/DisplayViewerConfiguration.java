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
package org.eclipse.jdt.internal.debug.ui.display;


import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIContentAssistPreference;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

/**
 *  The source viewer configuration for the Display view
 */
public class DisplayViewerConfiguration extends JavaSourceViewerConfiguration {
		
	public DisplayViewerConfiguration() {
		super(JavaPlugin.getDefault().getJavaTextTools().getColorManager(), 
				new ChainedPreferenceStore(new IPreferenceStore[] {PreferenceConstants.getPreferenceStore(), EditorsUI.getPreferenceStore()}),
				null, null);
	}

	public IContentAssistProcessor getContentAssistantProcessor() {
		return new DisplayCompletionProcessor();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(
			getContentAssistantProcessor(),
			IDocument.DEFAULT_CONTENT_TYPE);

		JDIContentAssistPreference.configure(assistant, getColorManager());

		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(
			getInformationControlCreator(sourceViewer));

		return assistant;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDoubleClickStrategy(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		ITextDoubleClickStrategy clickStrat = new ITextDoubleClickStrategy() {
			// Highlight the whole line when double clicked. See Bug#45481 
			public void doubleClicked(ITextViewer viewer) {
				try {
					IDocument doc = viewer.getDocument();
					int caretOffset = viewer.getSelectedRange().x;
					int lineNum = doc.getLineOfOffset(caretOffset);
					int start = doc.getLineOffset(lineNum);
					int length = doc.getLineLength(lineNum);
					viewer.setSelectedRange(start, length);
				} catch (BadLocationException e) {
					DebugUIPlugin.log(e);
				}
			}
		};
		return clickStrat;
	}	
}
