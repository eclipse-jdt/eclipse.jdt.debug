/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Runs a snippet in the context of a package
 *
 */
public class RunInPackageAction extends SnippetAction {
	
	public RunInPackageAction(JavaSnippetEditor editor) {
		super(editor);
		setText(SnippetMessages.getString("RunInPackage.label")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("RunInPackage.tooltip")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("RunInPackage.description")); //$NON-NLS-1$
		setImageDescriptor(JavaPluginImages.DESC_TOOL_PACKSNIPPET);
	}
	
	/**
	 * The user has invoked this action.
	 */
	public void run() {
		if (!fEditor.isInJavaProject()) {
			fEditor.reportNotInJavaProjectError();
			return;
		}
		Shell s= fEditor.getSite().getShell();
		IPackageFragment result= choosePackage(s);
		if (result != null) {
			fEditor.setPackage(result.getHandleIdentifier());
		}
	} 
	
	private IPackageFragment choosePackage(Shell shell) {
		try {
			IJavaProject p= fEditor.getJavaProject();
			String pkgHandle= fEditor.getPackage();
			String filter = "";
			IPackageFragment packageFragment = null;
			if (pkgHandle != null) {
				packageFragment = (IPackageFragment)JavaCore.create(pkgHandle);
				if (packageFragment != null) {
					filter = packageFragment.getElementName();
				}			
			}
			SelectionDialog dialog= JavaUI.createPackageDialog(shell, p, IJavaElementSearchConstants.CONSIDER_BINARIES | IJavaElementSearchConstants.CONSIDER_REQUIRED_PROJECTS, filter);
			if (pkgHandle != null) {
				dialog.setInitialSelections(new Object[] {packageFragment});
			}

			//fix for 1G472LK: ITPJUI:WIN2000 - Package selection dialog must qualify package names regarding source folders
			dialog.setTitle(SnippetMessages.getString("RunInPackage.dialog.title")); //$NON-NLS-1$
			dialog.setMessage(SnippetMessages.getString("RunInPackage.dialog.message")); //$NON-NLS-1$			
			dialog.open();		
			Object[] res= dialog.getResult();
			if (res != null && res.length > 0) 
				return (IPackageFragment)res[0];
		} catch (JavaModelException e) {
		}
		return null;
	}
	
	public void snippetStateChanged(JavaSnippetEditor editor) {
		setEnabled(editor != null && !editor.isEvaluating());
	}
}
