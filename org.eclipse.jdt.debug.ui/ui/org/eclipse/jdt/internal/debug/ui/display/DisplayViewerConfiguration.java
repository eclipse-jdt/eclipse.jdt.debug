package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.debug.ui.JDIViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

/**
 *  The source viewer configuration for the Display view
 */
public class DisplayViewerConfiguration extends JDIViewerConfiguration {
	
	private DisplayView fView;
	
	public DisplayViewerConfiguration(JavaTextTools tools, DisplayView view) {
		super(tools);
		fView= view;
	}

	/**
	 * @see JDIViewerConfiguration#getContentAssistantProcessor()
	 */
	public IContentAssistProcessor getContentAssistantProcessor() {

		return new DisplayCompletionProcessor(fView);
	}
}