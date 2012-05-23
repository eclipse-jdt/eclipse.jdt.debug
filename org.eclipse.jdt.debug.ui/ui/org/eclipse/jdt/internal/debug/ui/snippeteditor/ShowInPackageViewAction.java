/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Davids <sdavids@gmx.de> - initial API and implementation
 *     IBM Corporation - bug fixes
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 * This action reveals the snippet editor in the package explorer. 
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @since 3.0
 */
public class ShowInPackageViewAction extends Action {
	
	private JavaSnippetEditor fEditor;
	
	/**
	 * Creates a new <code>ShowInPackageViewAction</code>.
	 * 
	 * @param site the site providing context information for this action
	 */
	public ShowInPackageViewAction() {
		super(SnippetMessages.getString("ShowInPackageViewAction.label")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("ShowInPackageViewAction.description")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("ShowInPackageViewAction.tooltip")); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.SHOW_IN_PACKAGEVIEW_ACTION);	
	}
	
	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 */
	public ShowInPackageViewAction(JavaSnippetEditor editor) {
		this();
		fEditor= editor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		IFile file= fEditor.getFile();
		if (file == null) {
			return;
		}
		PackageExplorerPart view= PackageExplorerPart.openInActivePerspective();
		if (!reveal(view, file)) {
			MessageDialog.openInformation(fEditor.getShell(), getDialogTitle(), SnippetMessages.getString("ShowInPackageViewAction.not_found")); //$NON-NLS-1$
		}
	}

	private boolean reveal(PackageExplorerPart view, Object element) {
		if (view == null) {
			return false;
		}
		view.selectReveal(new StructuredSelection(element));
		IElementComparer comparer= view.getTreeViewer().getComparer();
		Object selected= getSelectedElement(view);
		if (comparer != null ? comparer.equals(element, selected) : element.equals(selected)) {
			return true;
		}
		return false;
	}

	private Object getSelectedElement(PackageExplorerPart view) {
		return ((IStructuredSelection) view.getSite().getSelectionProvider().getSelection()).getFirstElement();
	}
	
	private static String getDialogTitle() {
		return SnippetMessages.getString("ShowInPackageViewAction.dialog.title"); //$NON-NLS-1$
	}
}
