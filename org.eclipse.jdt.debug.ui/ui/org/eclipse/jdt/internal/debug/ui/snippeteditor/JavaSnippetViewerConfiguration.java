
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.debug.ui.JDIViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

/**
 *  The source viewer configuration for the Java snippet editor.
 */
public class JavaSnippetViewerConfiguration extends JDIViewerConfiguration {
	
	private JavaSnippetEditor fEditor;
	
	public JavaSnippetViewerConfiguration(JavaTextTools tools, JavaSnippetEditor editor) {
		super(tools);
		fEditor= editor;
	}
	
	/**
	 * @see JDIViewerConfiguration#getContentAssistantProcessor()
	 */
	public IContentAssistProcessor getContentAssistantProcessor() {
		return new JavaSnippetCompletionProcessor(fEditor);
	}
}