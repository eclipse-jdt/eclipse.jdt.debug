package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.ILaunchWizard;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

/**
 * The wizard specified by the JDIAttachLauncher to
 * designate the host, port and whether to allow termination of the remove VM.
 */
public class JDIAttachLauncherWizard extends Wizard implements ILaunchWizard {

	private IStructuredSelection fSelection;
	private ILauncher fLauncher;
	
	/**
	 * @see Wizard#addPages()
	 */
	public void addPages() {
		setNeedsProgressMonitor(true);
		addPage(new JDIAttachLauncherWizardPage());
	}

	private ILauncher getILauncher() {
		return fLauncher;
	}

	private void setLauncher(ILauncher launcher) {
		this.fLauncher = launcher;
	}

	/**
	 * Configures the attach launch and starts the launch
	 */
	public boolean performFinish() {
		final boolean[] lastLaunchSuccessful= new boolean[1];
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					JDIAttachLauncherWizardPage page= (JDIAttachLauncherWizardPage) getContainer().getCurrentPage();
					// do the launching
					page.setPreferenceValues();
					JDIAttachLauncher launcher= getLauncher();
					launcher.setPort(page.getPort());
					launcher.setHost(page.getHost());
					launcher.setAllowTerminate(page.getAllowTerminate());
					lastLaunchSuccessful[0]= launcher.doLaunch(getSelection().getFirstElement(), getILauncher());
				}
			});
		} catch (InvocationTargetException ite) {
			return false;
		} catch (InterruptedException ie) {
			return false;
		}
		
		return lastLaunchSuccessful[0];
	}

	/**
	 * @see ILauncher#getDelegate()
	 */
	protected JDIAttachLauncher getLauncher() {
		return (JDIAttachLauncher) getILauncher().getDelegate();
	}

	/**
	 * @see ILaunchWizard#init(ILauncher, String, IStructuredSelection)
	 */
	public void init(ILauncher launcher, String mode, IStructuredSelection selection) {
		setSelection(selection);
		setLauncher(launcher);
		setWindowTitle(DebugUIMessages.getString("JDIAttachLauncherWizard.Remote_Java_Application_1")); //$NON-NLS-1$
	}

	private IStructuredSelection getSelection() {
		return fSelection;
	}

	private void setSelection(IStructuredSelection selection) {
		this.fSelection = selection;
	}
}