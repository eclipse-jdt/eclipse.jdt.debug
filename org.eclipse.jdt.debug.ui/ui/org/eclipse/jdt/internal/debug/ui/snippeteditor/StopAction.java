/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;


import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

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

		setImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_TOOL_TERMSNIPPET));
		setDisabledImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_TOOL_TERMSNIPPET_DISABLED));
		setHoverImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_TOOL_TERMSNIPPET_HOVER));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaDebugHelpContextIds.TERMINATE_SCRAPBOOK_VM_ACTION);
	}
	
	/**
	 * @see IAction#run()
	 */
	@Override
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
