package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

public class DetailsViewerConfiguration extends DisplayViewerConfiguration {

	/**
	 * @see JDIViewerConfiguration#getContentAssistantProcessor()
	 */
	public IContentAssistProcessor getContentAssistantProcessor() {
		return new DetailsCompletionProcessor();
	}
}