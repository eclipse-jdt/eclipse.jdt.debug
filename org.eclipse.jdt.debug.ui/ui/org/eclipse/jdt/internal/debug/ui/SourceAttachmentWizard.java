package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard to allow source attachment at debug time.
 */
public class SourceAttachmentWizard extends Wizard {
	
	protected IPackageFragmentRoot fJar;
	protected SourceAttachmentWizardPage fPage;
	
	protected static Map fgNoPrompt = new HashMap();

	public SourceAttachmentWizard(IPackageFragmentRoot jar) {
		fJar = jar;
		setWindowTitle(DebugUIMessages.getString("SourceAttachmentWizard.Source_Attachment_1")); //$NON-NLS-1$
	}

	/**
	 * @see Wizard#addPages()
	 */
	public void addPages() {
		fPage = new SourceAttachmentWizardPage(fJar);
		addPage(fPage);
	}

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		if (fPage.isNoSource()) {
			fgNoPrompt.put(fJar, Boolean.FALSE);
			return true;
		}
		return fPage.performFinish();

	}

	public static boolean isOkToPrompt(IPackageFragmentRoot root) {
		return fgNoPrompt.get(root) == null;
	}
}