package org.eclipse.jdt.internal.debug.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Status handler that builds a set of projects and shows the build progress in a 
 * standard ProgressMonitorDialog.
 */
public class ProjectBuilderWithProgressMonitorStatusHandler implements IStatusHandler {

	/**
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		if (!(source instanceof Set)) {
			return null;
		}
		final Set projects = (Set) source;
		ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor monitor) throws InvocationTargetException{
					try {
						Iterator iter = projects.iterator();
						monitor.beginTask("", projects.size() * 100); //$NON-NLS-1$
						while (iter.hasNext()) {
							IProgressMonitor subMontior = new SubProgressMonitor(monitor, 100);
							IProject pro = ((IJavaProject)iter.next()).getProject();
							pro.build(IncrementalProjectBuilder.FULL_BUILD, subMontior);
							subMontior.done();
						}
						monitor.done();
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException ie) {
			// opearation canceled by user
		} catch (InvocationTargetException ite) {
			ExceptionHandler.handle(ite, getShell(), LauncherMessages.getString("VMPreferencePage.Installed_JREs_1"), LauncherMessages.getString("VMPreferencePage.Build_failed._1")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return null;
	}
	
	private Shell getShell() {
		return Display.getCurrent().getActiveShell();	
	}

}
