package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILauncherDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.ProjectSourceLocator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

public class JDIAttachLauncher implements ILauncherDelegate {

	private String fPort;
	private String fHost;
	private boolean fAllowTerminate;

	/**
	 * Perform the attach launch.
	 */
	protected boolean doLaunch(Object element, ILauncher launcher) {
		AttachingConnector connector= getAttachingConnector();
		

		// determine the launched project from the element
		IResource res= null;
		if (element instanceof IAdaptable) {
			res= (IResource) ((IAdaptable) element).getAdapter(IResource.class);
		}
		if (res != null) {
			element= res.getProject();
		}
		if (!(element instanceof IProject)) {
			return false;
		}

		if (connector != null) {
			Map map= connector.defaultArguments();
			Connector.Argument param= (Connector.Argument) map.get("hostname"); //$NON-NLS-1$
			param.setValue(getHost());
			param= (Connector.Argument) map.get("port"); //$NON-NLS-1$
			param.setValue(getPort());
			try {
				VirtualMachine vm= connector.attach(map);
				StringBuffer vmLabel= new StringBuffer(vm.name());
				vmLabel.append('[');
				vmLabel.append(getHost());
				vmLabel.append(':');
				vmLabel.append(getPort());
				vmLabel.append(']');
				IDebugTarget target= 
					JDIDebugModel.newDebugTarget(vm, vmLabel.toString(), null, allowTermination(), true);
				IJavaProject javaProject= JavaCore.create((IProject)element);
				ISourceLocator sl= new ProjectSourceLocator(javaProject);
				ILaunch launch= new Launch(launcher, ILaunchManager.DEBUG_MODE, element, sl, null, target);
				DebugPlugin.getDefault().getLaunchManager().registerLaunch(launch);
				return true;
			} catch (IOException e) {
				errorDialog(DebugUIMessages.getString("JDIAttachLauncher.Unable_to_connect_to_specified_address_1"), //$NON-NLS-1$
				 		IJDIStatusConstants.CODE_CONNECTION_FAILED, e);
			} catch (IllegalConnectorArgumentsException e) {
				JDIDebugUIPlugin.logError(e);
			}

		} else {
			errorDialog(DebugUIMessages.getString("JDIAttachLauncher.JDI_Shared_Memory_Attaching_Connector_not_available_2"), //$NON-NLS-1$
					 IJDIStatusConstants.CODE_CONNECTION_FAILED, null);
		}

		return false;
	}

	protected boolean doLaunchUsingWizard(Object element, ILauncher launcher) {
		IStructuredSelection ss= new StructuredSelection(element);
		JDIAttachLauncherWizard w= new JDIAttachLauncherWizard();
		w.init(launcher, ILaunchManager.DEBUG_MODE, ss);
		WizardDialog wd= new WizardDialog(JDIDebugUIPlugin.getActiveWorkbenchWindow().getShell(), w);
		wd.open();
		// Return that this launch was successful, since we can't tell
		// if the attach succeeded. If the attach fails, doLaunch()
		// should generate user notification
		return true;
	}

	protected void setPort(String port) {
		fPort= port;
	}
	
	protected String getPort() {
		return fPort;
	}

	protected void setHost(String host) {
		fHost= host;
	}
	
	protected String getHost() {
		return fHost;
	}
	
	/**
	 * Sets whether to allow termination of the remote target
	 * 
	 * @param allowTerminate The allowTerminate to set
	 */
	public void setAllowTerminate(boolean allowTerminate) {
		fAllowTerminate = allowTerminate;
	}

	protected void errorDialog(String message, int code, Throwable exception) {
		Status s= new Status(IStatus.ERROR, "org.eclipse.jdt.ui", IJDIStatusConstants.CODE_CONNECTION_FAILED, message, exception); //$NON-NLS-1$
		String title= DebugUIMessages.getString("JDIAttachLauncher.Remote_Java_Application_3"); //$NON-NLS-1$
		ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchWindow().getShell(), title, message, s);
	}
	
	protected static AttachingConnector getAttachingConnector() {
		AttachingConnector connector= null;
		Iterator iter= Bootstrap.virtualMachineManager().attachingConnectors().iterator();
		while (iter.hasNext()) {
			AttachingConnector lc= (AttachingConnector) iter.next();
			if (lc.name().equals("com.sun.jdi.SocketAttach")) { //$NON-NLS-1$
				connector= lc;
				break;
			}
		}
		return connector;
	}

	/**
	 * @see ILauncherDelegate#getLaunchMemento(Object)
	 */
	public String getLaunchMemento(Object element) {
		if (element instanceof IJavaElement) {
			return ((IJavaElement)element).getHandleIdentifier();
		}
		return null;
	}
	
	/**
	 * @see ILauncherDelegate#getLaunchObject(String)
	 */
	public Object getLaunchObject(String memento) {
		IJavaElement e = JavaCore.create(memento);
		if (e.exists()) {
			return e;
		} else {
			return null;
		}
	}
	
	/**
	 * @see ILauncherDelegate#launch(Object[], String, ILauncher)
	 */
	public boolean launch(Object[] objects, String mode, ILauncher launcher) {
		Object element= null;
		if (objects.length > 0) {
			element= objects[0];
		}
		return doLaunchUsingWizard(element, launcher);
	}
	
	protected boolean allowTermination() {
		return fAllowTerminate;
	}
}
