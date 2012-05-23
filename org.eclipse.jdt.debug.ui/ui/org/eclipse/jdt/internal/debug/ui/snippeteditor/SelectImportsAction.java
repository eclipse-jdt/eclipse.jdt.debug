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
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.PlatformUI;

public class SelectImportsAction extends SnippetAction {
	
	public SelectImportsAction(JavaSnippetEditor editor) {
		super(editor);
		setText(SnippetMessages.getString("SelectImports.label")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("SelectImports.tooltip")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("SelectImports.description")); //$NON-NLS-1$
		ISharedImages sharedImages= JavaUI.getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_OBJS_IMPCONT));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaDebugHelpContextIds.SCRAPBOOK_IMPORTS_ACTION);
	}
	
	/**
	 * @see IAction#run()
	 */
	@Override
	public void run() {
		if (!getEditor().isInJavaProject()) {
			getEditor().reportNotInJavaProjectError();
			return;
		}
		chooseImports();
	} 
	
	private void chooseImports() {
		String[] imports= getEditor().getImports();
		Dialog dialog= new SelectImportsDialog(getEditor(), imports);
		dialog.open();		
	}
	
	/**
	 * @see ISnippetStateChangedListener#snippetStateChanged(JavaSnippetEditor)
	 */
	public void snippetStateChanged(JavaSnippetEditor editor) {
		setEnabled(editor != null && !editor.isEvaluating());
	}
}
