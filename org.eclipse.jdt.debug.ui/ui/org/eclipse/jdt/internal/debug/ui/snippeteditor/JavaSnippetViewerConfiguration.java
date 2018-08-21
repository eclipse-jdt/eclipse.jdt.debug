/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Aditya Aswani (Samsung) - Incorrect syntax highlighting in scrapbook
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.snippeteditor;


import org.eclipse.jdt.internal.debug.ui.JDIContentAssistPreference;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 *  The source viewer configuration for the Java snippet editor.
 */
public class JavaSnippetViewerConfiguration extends JavaSourceViewerConfiguration {

	public JavaSnippetViewerConfiguration(JavaTextTools tools, IPreferenceStore preferenceStore, JavaSnippetEditor editor) {
		super(tools.getColorManager(), preferenceStore, editor, IJavaPartitions.JAVA_PARTITIONING);
	}

	/**
	 * @see JDIViewerConfiguration#getContentAssistantProcessor()
	 */
	public IContentAssistProcessor getContentAssistantProcessor() {
		return new JavaSnippetCompletionProcessor((JavaSnippetEditor)getEditor());
	}

	/**
	 * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant = new ContentAssistant();
		assistant.enableColoredLabels(true);
		IContentAssistProcessor contentAssistProcessor = getContentAssistantProcessor();
		if (contentAssistProcessor instanceof JavaSnippetCompletionProcessor) {
			((JavaSnippetCompletionProcessor) contentAssistProcessor).setContentAssistant(assistant);
		}
		assistant.setContentAssistProcessor(
				contentAssistProcessor,
			IDocument.DEFAULT_CONTENT_TYPE);

		JDIContentAssistPreference.configure(assistant, getColorManager());

		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(
			getInformationControlCreator(sourceViewer));

		return assistant;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		return null;
	}
}
