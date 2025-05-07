/*******************************************************************************
 * Copyright (c) 2007, 2020 IBM Corporation and others.
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
package org.eclipse.jdt.debug.ui.launchConfigurations;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.jres.JREMessages;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;

/**
 * A wizard page used to edit the attributes of an installed JRE. A page is
 * provided by JDT to edit standard JREs, but clients may contribute a custom
 * page for a VM install type if required.
 * <p>
 * A VM install page is contributed via the <code>vmInstallPages</code> extension
 * point. Following is an example definition of a VM install page.
 * <pre>
 * &lt;extension point="org.eclipse.jdt.debug.ui.vmInstallPages"&gt;
 *   &lt;vmInstallPage
 *      vmInstallType="org.eclipse.jdt.launching.EEVMType"
 *      class="org.eclipse.jdt.internal.debug.ui.jres.EEVMPage"&gt;
 *   &lt;/vmInstallPage&gt;
 * &lt;/extension&gt;
 * </pre>
 * The attributes are specified as follows:
 * <ul>
 * <li><code>vmInstallType</code> Specifies the VM install type this wizard page is to be used for.
 * 	Unique identifier corresponding to an <code>IVMInstallType</code>'s id.</li>
 * <li><code>class</code> Wizard page implementation. Must be a subclass of
 *  <code>org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage</code>.</li>
 * </ul>
 * <p>
 * Clients contributing a custom VM install page via the <code>vmInstallPages</code>
 * extension point must subclass this class.
 * @since 3.3
 */
public abstract class AbstractVMInstallPage extends WizardPage {

	/**
	 * Name of the original VM being edited, or <code>null</code> if none.
	 */
	private String fOriginalName = null;

	/**
	 * Status of VM name (to notify of name already in use)
	 */
	private IStatus fNameStatus = Status.OK_STATUS;

	private String[] fExistingNames;

	/**
	 * Constructs a new page with the given page name.
	 *
	 * @param pageName the name of the page
	 */
	protected AbstractVMInstallPage(String pageName) {
		super(pageName);
	}

	/**
     * Creates a new wizard page with the given name, title, and image.
     *
     * @param pageName the name of the page
     * @param title the title for this wizard page,
     *   or <code>null</code> if none
     * @param titleImage the image descriptor for the title of this wizard page,
     *   or <code>null</code> if none
     */
	protected AbstractVMInstallPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/**
	 * Called when the VM install page wizard is closed by selecting
	 * the finish button. Implementers typically override this method to
	 * store the page result (new/changed vm install returned in
	 * getSelection) into its model.
	 *
	 * @return if the operation was successful. Only when returned
	 * <code>true</code>, the wizard will close.
	 */
	public abstract boolean finish();

	/**
	 * Returns the edited or created VM install. This method
	 * may return <code>null</code> if no VM install exists.
	 *
	 * @return the edited or created VM install.
	 */
	public abstract VMStandin getSelection();

	/**
	 * Sets the VM install to be edited.
	 *
	 * @param vm the VM install to edit
	 */
	public void setSelection(VMStandin vm) {
		fOriginalName = vm.getName();
	}

	/**
	 * Updates the name status based on the new name. This method should be called by the page each time the VM name changes.
	 *
	 * Use nameChanged(String newName, boolean init)
	 *
	 * @param newName
	 *            new name of VM
	 */
	@Deprecated
	protected void nameChanged(String newName) {
		nameChanged(newName, false);
	}

	/**
	 * Updates the name status based on the new name. This method should be called by the page each time the VM name changes.
	 *
	 * @param newName
	 *            new name of VM
	 * @param init
	 *            <code>true</code> if page is getting initialized else <code>false</code>
	 *
	 * @since 3.11
	 */
	protected void nameChanged(String newName, boolean init) {
		fNameStatus = Status.OK_STATUS;
		if (newName == null || newName.trim().length() == 0) {
			if (init) {
				return;
			}
			int sev = IStatus.ERROR;
			if (fOriginalName == null || fOriginalName.length() == 0) {
				sev = IStatus.WARNING;
			}
			fNameStatus = new Status(sev, JDIDebugUIPlugin.getUniqueIdentifier(), JREMessages.addVMDialog_enterName);
		} else {
			if (isDuplicateName(newName)) {
				fNameStatus = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), JREMessages.addVMDialog_duplicateName);
			} else {
				IStatus s = ResourcesPlugin.getWorkspace().validateName(newName, IResource.FILE);
				if (!s.isOK()) {
					fNameStatus = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), NLS.bind(JREMessages.AddVMDialog_JRE_name_must_be_a_valid_file_name___0__1, s.getMessage()));
				}
			}
		}
		updatePageStatus();
	}

	/**
	 * Returns whether the name is already in use by an existing VM
	 *
	 * @param name new name
	 * @return whether the name is already in use
	 */
	private boolean isDuplicateName(String name) {
		if (fExistingNames != null) {
			for (String n : fExistingNames) {
				if (name.equals(n)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sets the names of existing VMs, not including the VM being edited. This method
	 * is called by the wizard and clients should not call this method.
	 *
	 * @param names existing VM names or an empty array
	 */
	public void setExistingNames(String[] names) {
		fExistingNames = names;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	@Override
	public IWizardPage getNextPage() {
		return null;
	}

	/**
	 * Sets this page's message based on the status severity.
	 *
	 * @param status status with message and severity
	 */
	protected void setStatusMessage(IStatus status) {
		if (status.isOK()) {
			setMessage(status.getMessage());
		} else {
			switch (status.getSeverity()) {
			case IStatus.ERROR:
				setMessage(status.getMessage(), IMessageProvider.ERROR);
				break;
			case IStatus.INFO:
				setMessage(status.getMessage(), IMessageProvider.INFORMATION);
				break;
			case IStatus.WARNING:
				setMessage(status.getMessage(), IMessageProvider.WARNING);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Returns the current status of the name being used for the VM.
	 *
	 * @return status of current VM name
	 */
	protected IStatus getNameStatus() {
		return fNameStatus;
	}

	/**
	 * Updates the status message on the page, based on the status of the VM and other
	 * status provided by the page.
	 */
	protected void updatePageStatus() {
		IStatus max = Status.OK_STATUS;
		for (IStatus status : getVMStatus()) {
			if (status.getSeverity() > max.getSeverity()) {
				max = status;
			}
		}
		if (fNameStatus.getSeverity() > max.getSeverity()) {
			max = fNameStatus;
		}
		if (max.isOK()) {
			setMessage(null, IMessageProvider.NONE);
		} else {
			setStatusMessage(max);
		}
		setPageComplete(max.isOK() || max.getSeverity() == IStatus.INFO);
	}

	/**
	 * Returns a collection of status messages pertaining to the current edit
	 * status of the VM on this page. An empty collection or a collection of
	 * OK status objects indicates all is well.
	 *
	 * @return collection of status objects for this page
	 */
	protected abstract IStatus[] getVMStatus();
}
