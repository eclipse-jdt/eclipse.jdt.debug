package org.eclipse.jdt.internal.debug.ui.snippeteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;

/**
 * Runs a snippet
 */
public class RunAction extends SnippetAction {
	
	public RunAction(JavaSnippetEditor editor) {
		super(editor);
		setText(SnippetMessages.getString("RunAction.label")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("RunAction.tooltip")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("RunAction.description")); //$NON-NLS-1$
		setImageDescriptor(JavaDebugImages.DESC_TOOL_RUNSNIPPET);
	}
	
	/**
	 * @see IAction#run()
	 */
	public void run() {
		getEditor().evalSelection(JavaSnippetEditor.RESULT_RUN);
	} 
}
