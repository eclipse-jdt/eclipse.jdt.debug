package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.ILaunchWizard;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaApplicationLauncherDelegate;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

/**
 * The wizard specified by the <code>JavaApplication</code> launcher to
 * designate the elements to launch
 */
public class JavaApplicationWizard extends Wizard implements ILaunchWizard {
	protected String fMode;
	protected ILauncher fLauncher;
	protected Object[] fLauncheables;
	protected boolean fLastLaunchSuccessful;

	public JavaApplicationWizard() {
	}

	public JavaApplicationWizard(Object[] launcheables) {
		fLauncheables= launcheables;
	}

	/**
	 * @see Wizard#addPages
	 */
	public void addPages() {
		addPage(new JavaApplicationWizardPage(fLauncheables, getLauncher(), fMode));
	}

	/**
	 * Sets the chosen launcher and elements and performs the launch.
	 */
	public boolean performFinish() {
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					JavaApplicationWizardPage page= (JavaApplicationWizardPage) getContainer().getCurrentPage();
					fLastLaunchSuccessful= fLauncher.launch(page.getElements(), fMode);
				}
			});
		} catch (InvocationTargetException ite) {
			return false;
		} catch (InterruptedException ie) {
			return false;
		}

		return fLastLaunchSuccessful;
	}

	/**
	 * @see ILauncher#getDelegate()
	 */
	protected JavaApplicationLauncherDelegate getLauncher() {
		return (JavaApplicationLauncherDelegate) fLauncher.getDelegate();
	}

	/**
	 * @see ILaunchWizard
	 */
	 public void init(ILauncher launcher, String mode, IStructuredSelection selection) {
		fMode= mode;
		fLauncher= launcher;
		if (fLauncheables == null) {
			fLauncheables= getLauncher().getLaunchableElements(selection, fMode);
		}
		if (fLauncheables == null) {
			fLauncheables= new Object[0];
		}
		setNeedsProgressMonitor(true);
		setWindowTitle(DebugUIUtils.getResourceString("java_application_wizard.title"));
	}
}
