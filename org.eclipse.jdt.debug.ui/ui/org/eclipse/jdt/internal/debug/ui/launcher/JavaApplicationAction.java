package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaEditorActionConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Performs single click launching for local java applications.
 */
public abstract class JavaApplicationAction implements IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow fWindow = null;
	private IEditorPart fEditor = null;
	private IStructuredSelection fSelection = null;

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow = window;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IType[] types = null;
		Object[] search = null;
		if (fSelection == null) {
			if (fEditor != null) {
				IEditorInput input = fEditor.getEditorInput();
				IJavaElement je = (IJavaElement) input.getAdapter(IJavaElement.class);
				if (je != null) {
					search = new Object[] {je};
				}
			}			
		} else {
			search = fSelection.toArray();
		}
		
		if (search != null) {
			try {
				types = MainMethodFinder.findTargets(fWindow, search);
			} catch (InterruptedException e) {
				JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JavaApplicationAction.Launch_failed__no_main_type_found_1"), e); //$NON-NLS-1$
				return;
			} catch (InvocationTargetException e) {
				JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JavaApplicationAction.Launch_failed__no_main_type_found_1"), e); //$NON-NLS-1$
				return;
			}
			IType type = null;
			if (types.length == 0) {
				JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JavaApplicationAction.Launch_failed__no_main_type_found_1"), (Throwable)null); //$NON-NLS-1$
			} else if (types.length > 1) {
				type = chooseType(types);
			} else {
				type = types[0];
			}
			if (type != null) {
				launch(type);
			}
		}

	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fEditor = null;
		fSelection = null;
		boolean enabled = false;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			Iterator iter = ss.iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof IAdaptable) {
					IJavaElement je = (IJavaElement)((IAdaptable)obj).getAdapter(IJavaElement.class);
					if (je != null) {
						enabled = true;
						fSelection = ss;
					}
				}
			}
		} else {
			IWorkbenchPage page = fWindow.getActivePage();
			if (page != null) {
				IEditorPart editor = page.getActiveEditor();
				if (editor != null) {
					String id = editor.getSite().getId();
					if (JavaUI.ID_CU_EDITOR.equals(id) || JavaUI.ID_CF_EDITOR.equals(id)) {
						enabled = true;
						fEditor = editor;
					}
				}
			}
		}
		action.setEnabled(enabled);
	}

	/**
	 * Prompts the user to select a type
	 * 
	 * @return the selected type or <code>null</code> if none.
	 */
	protected IType chooseType(IType[] types) {
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(fWindow.getShell(), new JavaElementLabelProvider());
		dialog.setElements(types);
		dialog.setTitle(LauncherMessages.getString("JavaApplicationAction.Type_Selection_4")); //$NON-NLS-1$
		if (getMode().equals(ILaunchManager.DEBUG_MODE)) {
			dialog.setMessage(LauncherMessages.getString("JavaApplicationAction.Choose_a_&main_type_to_debug__5")); //$NON-NLS-1$
		} else {
			dialog.setMessage(LauncherMessages.getString("JavaApplicationAction.Choose_a_&main_type_to_run__6")); //$NON-NLS-1$
		}
		dialog.setMultipleSelection(false);
		if (dialog.open() == dialog.OK) {
			return (IType)dialog.getFirstResult();
		}
		return null;
	}
	
	/**
	 * Launches the given type
	 */
	protected void launch(IType type) {
		try { 
			ILaunchConfiguration config = findLaunchConfiguration(type);
			if (config == null) {
				ILaunchConfigurationType configType = getJavaLaunchConfigType();
				ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, type.getElementName() + " [" + type.getJavaProject().getElementName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, type.getJavaProject().getElementName());
				IVMInstall vm = JavaRuntime.computeVMInstall(wc);
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vm.getVMInstallType().getId());
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, vm.getId());
				config = wc.doSave();
			}			
			config.launch(getMode(), null);
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JavaApplicationAction.Launch_failed_7"), e.getStatus()); //$NON-NLS-1$
		}
	}
	
	/**
	 * Locate a configuration to relaunch for the given type
	 * 
	 * @return a re-useable config or <code>null</code> if none
	 */
	protected ILaunchConfiguration findLaunchConfiguration(IType type) {
		ILaunchConfigurationType configType = getJavaLaunchConfigType();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				if (config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "").equals(type.getFullyQualifiedName())) { //$NON-NLS-1$
					if (config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(type.getJavaProject().getElementName())) { //$NON-NLS-1$
						return config;
					}
				}
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return null;
	}
	
	/**
	 * Returns the local java launch config type
	 */
	protected ILaunchConfigurationType getJavaLaunchConfigType() {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		return lm.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);		
	}
	
	/**
	 * Returns the mode this action launches in
	 */
	protected abstract String getMode();
}