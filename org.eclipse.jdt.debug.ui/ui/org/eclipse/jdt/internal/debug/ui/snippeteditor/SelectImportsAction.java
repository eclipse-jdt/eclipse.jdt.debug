/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

public class SelectImportsAction extends SnippetAction {
	
	public SelectImportsAction(JavaSnippetEditor editor) {
		super(editor);
		setText(SnippetMessages.getString("SelectImports.label")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("SelectImports.tooltip")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("SelectImports.description")); //$NON-NLS-1$
		ISharedImages sharedImages= JavaUI.getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(sharedImages.IMG_OBJS_IMPCONT));
	}
	
	/**
	 * The user has invoked this action.
	 */
	public void run() {
		if (!fEditor.isInJavaProject()) {
			fEditor.reportNotInJavaProjectError();
			return;
		}
		chooseImports();
	} 
	
	private void chooseImports() {
		String[] imports= fEditor.getImports();
		Dialog dialog= new SelectImportsDialog(fEditor, imports);
		dialog.open();		
	}
	
	public void snippetStateChanged(JavaSnippetEditor editor) {
		setEnabled(editor != null && !editor.isEvaluating());
	}
}
