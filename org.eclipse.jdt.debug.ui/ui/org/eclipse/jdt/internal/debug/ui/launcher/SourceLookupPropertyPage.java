package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.help.DialogPageContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;


public class SourceLookupPropertyPage extends JavaProjectPropertyPage {
	
	private SourceLookupBlock fSourceLookupBlock;
		
	public SourceLookupPropertyPage() {
	}

	public Control createJavaContents(Composite parent) {
		fSourceLookupBlock= new SourceLookupBlock(getJavaProject());
		Control control= fSourceLookupBlock.createControl(parent);

		WorkbenchHelp.setHelp(parent, new DialogPageContextComputer(this, IJavaHelpContextIds.SOURCE_LOOKUP_PROPERTY_PAGE));						
		return control;
	}
		

	protected void performDefaults() {
		if (fSourceLookupBlock != null) {
			fSourceLookupBlock.initializeFields();
		}
		super.performDefaults();
	}

	public boolean performJavaOk() {
		try {
			if (fSourceLookupBlock != null) {
				fSourceLookupBlock.applyChanges();
			}
			return true;
		} catch (JavaModelException e) {
			JDIDebugUIPlugin.log(e);
		}
		return false;
	}
	
	
	
	
	
}