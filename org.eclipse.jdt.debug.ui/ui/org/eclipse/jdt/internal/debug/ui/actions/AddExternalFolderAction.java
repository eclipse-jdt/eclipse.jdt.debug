package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * Adds an external folder to the runtime class path.
 */
public class AddExternalFolderAction extends RuntimeClasspathAction {

	public AddExternalFolderAction(RuntimeClasspathViewer viewer) {
		super(ActionMessages.getString("AddExternalFolderAction.Add_External_Folder_1"), viewer); //$NON-NLS-1$
	}	

	/**
	 * Prompts for a folder to add.
	 * 
	 * @see IAction#run()
	 */	
	public void run() {
							
		String lastUsedPath= ""; //$NON-NLS-1$
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		DirectoryDialog dialog= new DirectoryDialog(getShell(), SWT.MULTI);
		dialog.setText(ActionMessages.getString("AddExternalFolderAction.Folder_Selection_3")); //$NON-NLS-1$
		dialog.setFilterPath(lastUsedPath);
		String res= dialog.open();
		if (res == null) {
			return;
		}
			
		IPath filterPath= new Path(dialog.getFilterPath());
		IRuntimeClasspathEntry[] elems= new IRuntimeClasspathEntry[1];
		IPath path= filterPath.append(res).makeAbsolute();	
		elems[0]= JavaRuntime.newArchiveRuntimeClasspathEntry(path);

		//fDialogSettings.put(IUIConstants.DIALOGSTORE_LASTEXTJAR, filterPath.toOSString());
		
		getViewer().addEntries(elems);
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return getViewer().isEnabled();
	}
	
}
