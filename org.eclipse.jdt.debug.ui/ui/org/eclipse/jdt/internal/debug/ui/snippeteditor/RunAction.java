/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.ui.texteditor.IUpdate;


/**
 * Runs a snippet
 *
 */
public class RunAction extends SnippetAction implements IUpdate {
	
	public RunAction(JavaSnippetEditor editor) {
		super(editor);
		setText(SnippetMessages.getString("RunAction.label")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("RunAction.tooltip")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("RunAction.description")); //$NON-NLS-1$
		setImageDescriptor(JavaDebugImages.DESC_TOOL_RUNSNIPPET);
	}
	
	/**
	 * The user has invoked this action.
	 */
	public void run() {
		fEditor.evalSelection(JavaSnippetEditor.RESULT_RUN);
	} 
}
