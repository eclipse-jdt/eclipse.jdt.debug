package org.eclipse.jdt.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.SourceLookupBlock;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog to manipulate the source lookup path for a launch
 * configuration. 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @since 2.0.2
 * @see org.eclipse.jface.dialogs.Dialog
 */
public class JavaSourceLookupDialog extends Dialog {
		
	private SourceLookupBlock fSourceLookupBlock;
	private ILaunchConfiguration fConfiguration;
	private String fMessage;
	private boolean fNotAskAgain;
	private Button fAskAgainCheckBox;
	
	/**
	 * Constructs a dialog to manipulate the source lookup path of the given
	 * launch configuration. The source lookup path is retrieved from the given
	 * launch configuration, based on the attributes
	 * <code>IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH</code> and
	 * <code>IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH</code>. If the user
	 * changes the source lookup path and presses "ok", the launch configuration
	 * is updated with the new source lookup path. 
	 * 	 * @param shell the shell to open the dialog on	 * @param message the message to display in the dialog	 * @param configuration the launch configuration from which the source lookup
	 *  path is retrieved and (possibly) updated	 */
	public JavaSourceLookupDialog(Shell shell, String message, ILaunchConfiguration configuration) {
		super(shell);
		fSourceLookupBlock= new SourceLookupBlock();
		fMessage = message;
		fNotAskAgain= false;
		fAskAgainCheckBox= null;
		fConfiguration = configuration;
	}
	
	/**
	 * Returns whether the "do not ask again" check box is selected in the dialog.
	 * 	 * @return whether the "do not ask again" check box is selected in the dialog	 */
	public boolean isNotAskAgain() {
		return fNotAskAgain;
	}
			
	/**	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)	 */
	protected Control createDialogArea(Composite parent) {
		Font font = parent.getFont();
		initializeDialogUnits(parent);
		getShell().setText(LauncherMessages.getString("JavaUISourceLocator.selectprojects.title")); //$NON-NLS-1$
		
		Composite composite= (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout());
		composite.setFont(font);
		
		int pixelWidth= convertWidthInCharsToPixels(70);
		Label message= new Label(composite, SWT.LEFT + SWT.WRAP);
		message.setText(fMessage);
		GridData data= new GridData();
		data.widthHint= pixelWidth;
		message.setLayoutData(data);
		message.setFont(font);
		
		fSourceLookupBlock.createControl(composite);
		Control inner = fSourceLookupBlock.getControl();
		fSourceLookupBlock.initializeFrom(fConfiguration);
		GridData gd = new GridData(GridData.FILL_BOTH);
		int height = Display.getCurrent().getBounds().height;
		gd.heightHint = (int)(0.4f * height);
		inner.setLayoutData(gd);
		fAskAgainCheckBox= new Button(composite, SWT.CHECK + SWT.WRAP);
		data= new GridData();
		data.widthHint= pixelWidth;
		fAskAgainCheckBox.setLayoutData(data);
		fAskAgainCheckBox.setFont(font);
		fAskAgainCheckBox.setText(LauncherMessages.getString("JavaUISourceLocator.askagain.message")); //$NON-NLS-1$
		
		return composite;
	}

	/**	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()	 */
	protected void okPressed() {
		try {
			if (fAskAgainCheckBox != null) {
				fNotAskAgain= fAskAgainCheckBox.getSelection();
			}
			ILaunchConfigurationWorkingCopy wc = fConfiguration.getWorkingCopy();
			fSourceLookupBlock.performApply(wc);
			if (!fConfiguration.contentsEqual(wc)) {
				fConfiguration = wc.doSave();
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		super.okPressed();
	}
}
