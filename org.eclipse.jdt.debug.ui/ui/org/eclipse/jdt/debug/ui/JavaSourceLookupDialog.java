/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui;


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
 * This class may be instantiated.
 * </p>
 * @since 2.0.2
 * @see org.eclipse.jface.dialogs.Dialog
 * @deprecated In 3.0, the debug platform provides source lookup facilities that
 *  should be used in place of the Java source lookup support provided in 2.0.
 *  The new facilities provide a source lookup director that coordinates source
 *  lookup among a set of participants, searching a set of source containers.
 *  See the following packages: <code>org.eclipse.debug.core.sourcelookup</code>
 *  and <code>org.eclipse.debug.core.sourcelookup.containers</code>. This class
 *  has been replaced by a dialog in the debug platform - 
 *  <code>org.eclipse.debug.ui.sourcelookup.SourceLookupDialog</code>.
 * @noextend This class is not intended to be subclassed by clients.
 */
@Deprecated
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
	 * changes the source lookup path and presses "OK", the launch configuration
	 * is updated with the new source lookup path. 
	 * 
	 * @param shell the shell to open the dialog on
	 * @param message the message to display in the dialog
	 * @param configuration the launch configuration from which the source lookup
	 *  path is retrieved and (possibly) updated
	 */
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
	 * 
	 * @return whether the "do not ask again" check box is selected in the dialog
	 */
	public boolean isNotAskAgain() {
		return fNotAskAgain;
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Font font = parent.getFont();
		initializeDialogUnits(parent);
		getShell().setText(LauncherMessages.JavaUISourceLocator_selectprojects_title); 
		
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
		fAskAgainCheckBox.setText(LauncherMessages.JavaUISourceLocator_askagain_message); 
		
		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
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
