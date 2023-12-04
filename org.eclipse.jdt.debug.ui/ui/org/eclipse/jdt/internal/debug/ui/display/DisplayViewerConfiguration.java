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
package org.eclipse.jdt.internal.debug.ui.display;


import org.eclipse.jdt.internal.debug.ui.JDIContentAssistPreference;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.contentassist.CurrentFrameContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
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
		super(JDIDebugUIPlugin.getDefault().getJavaTextTools().getColorManager(),
				new ChainedPreferenceStore(new IPreferenceStore[] {
						PreferenceConstants.getPreferenceStore(),
						EditorsUI.getPreferenceStore()}),
				null, IJavaPartitions.JAVA_PARTITIONING);
	}

	/**
	 * Returns the preference store this source viewer configuration is associated with.
	 */
	public IPreferenceStore getTextPreferenceStore() {
		return fPreferenceStore;
	}

	public IContentAssistProcessor getContentAssistantProcessor() {
		return new JavaDebugContentAssistProcessor(new CurrentFrameContext());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant = new ContentAssistant();
		assistant.enableColoredLabels(true);
		IContentAssistProcessor contentAssistProcessor = getContentAssistantProcessor();
		if (contentAssistProcessor instanceof JavaDebugContentAssistProcessor) {
			((JavaDebugContentAssistProcessor) contentAssistProcessor).setContentAssistant(assistant);
		}
		assistant.setContentAssistProcessor(contentAssistProcessor,
			IDocument.DEFAULT_CONTENT_TYPE);

		JDIContentAssistPreference.configure(assistant, getColorManager());

		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(
			getInformationControlCreator(sourceViewer));

		return assistant;
	}
}
