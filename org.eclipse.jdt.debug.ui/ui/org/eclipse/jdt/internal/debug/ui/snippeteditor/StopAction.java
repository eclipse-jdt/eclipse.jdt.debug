/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;


import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.ui.help.WorkbenchHelp;

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
		setDisabledImageDescriptor(JavaDebugImages.DESC_TOOL_TERMSNIPPET_DISABLED);
		setHoverImageDescriptor(JavaDebugImages.DESC_TOOL_TERMSNIPPET_HOVER);
		WorkbenchHelp.setHelp(this, IJavaDebugHelpContextIds.TERMINATE_SCRAPBOOK_VM_ACTION);
	}
	
	/**
	 * @see IAction#run()
	 */
	public void run() {
		getEditor().shutDownVM();
	}
	
	/**
	 * @see ISnippetStateChangedListener#snippetStateChanged(JavaSnippetEditor)
	 */
	public void snippetStateChanged(JavaSnippetEditor editor) {
		setEnabled(editor != null && editor.isVMLaunched());
	}
}
