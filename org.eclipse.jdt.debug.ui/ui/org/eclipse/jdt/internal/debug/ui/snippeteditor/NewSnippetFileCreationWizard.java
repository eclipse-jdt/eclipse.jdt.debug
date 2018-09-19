/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Creates a new snippet page
 */
public class NewSnippetFileCreationWizard extends Wizard implements INewWizard {

	private NewSnippetFileWizardPage fPage;
	private IStructuredSelection fSelection;

	public NewSnippetFileCreationWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(SnippetMessages.getString("NewSnippetFileCreationWizard.title")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		if (fSelection == null) {
			IJavaElement elem= getActiveEditorJavaInput();
			if (elem != null) {
				fSelection= new StructuredSelection(elem);
			} else {
				fSelection= StructuredSelection.EMPTY;
			}
		}
		fPage= new NewSnippetFileWizardPage(fSelection);
		addPage(fPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		return fPage.finish();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fSelection= selection;
		setDefaultPageImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_WIZBAN_NEWSCRAPPAGE));
	}

	/**
	 * If the current active editor edits a Java element return it, else
	 * return null
	 */
	private IJavaElement getActiveEditorJavaInput() {
		IWorkbenchPage page= JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IEditorPart part= page.getActiveEditor();
			if (part != null) {
				IEditorInput editorInput= part.getEditorInput();
				if (editorInput != null) {
					return editorInput.getAdapter(IJavaElement.class);
				}
			}
		}
		return null;
	}
}
