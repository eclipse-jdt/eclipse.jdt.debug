/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.jdt.internal.ui.wizards.AbstractOpenWizardAction;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.help.WorkbenchHelp;

public class OpenSnippetWizardAction extends AbstractOpenWizardAction {

	public OpenSnippetWizardAction() {
		WorkbenchHelp.setHelp(this, IJavaDebugHelpContextIds.OPEN_SNIPPET_WIZARD_ACTION);
	}
	
	public OpenSnippetWizardAction(String label, Class[] acceptedTypes) {
		super(label, acceptedTypes, false);
		WorkbenchHelp.setHelp(this, IJavaDebugHelpContextIds.OPEN_SNIPPET_WIZARD_ACTION);
	}
	
	protected Wizard createWizard() { 
		return new NewSnippetFileCreationWizard(); 
	}
	
	protected boolean shouldAcceptElement(Object obj) { 
		return !isInArchive(obj);
	}
}