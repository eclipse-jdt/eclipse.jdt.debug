package org.eclipse.jdt.internal.debug.ui.snippeteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;

/**
 * Stops the VM used to run a snippet.
 *
 */
public class StopAction extends SnippetAction {
	
	public StopAction(JavaSnippetEditor editor) {
		super(editor);
		
		setText(SnippetMessages.getString("StopAction.label"));  //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("StopAction.tooltip")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("StopAction.description"));  //$NON-NLS-1$

		setImageDescriptor(JavaDebugImages.DESC_TOOL_TERMSNIPPET);
	}
	
	/**
	 * The user has invoked this action.
	 */
	public void run() {
		fEditor.shutDownVM();
	}
	
	public void snippetStateChanged(JavaSnippetEditor editor) {
		setEnabled(editor != null && editor.isVMLaunched());
	}
}
