package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Performs single click launching for local java applications.
 */
public class JavaApplicationShortcut implements ILaunchShortcut {
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void searchAndLaunch(Object[] search, String mode) {
		IType[] types = null;
		if (search != null) {
			try {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				types = MainMethodFinder.findTargets(dialog, search);
			} catch (InterruptedException e) {
				return;
			} catch (InvocationTargetException e) {
				MessageDialog.openError(getShell(), LauncherMessages.getString("JavaApplicationAction.Launch_failed_7"), e.getMessage()); //$NON-NLS-1$
				return;
			}
			IType type = null;
			if (types.length == 0) {
				MessageDialog.openError(getShell(), LauncherMessages.getString("JavaApplicationAction.Launch_failed_7"), LauncherMessages.getString("JavaApplicationAction.Launch_failed__no_main_type_found_1")); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (types.length > 1) {
				type = chooseType(types, mode);
			} else {
				type = types[0];
			}
			if (type != null) {
				launch(type, mode);
			}
		}

	}	

	/**
	 * Prompts the user to select a type
	 * 
	 * @return the selected type or <code>null</code> if none.
	 */
	protected IType chooseType(IType[] types, String mode) {
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), new JavaElementLabelProvider());
		dialog.setElements(types);
		dialog.setTitle(LauncherMessages.getString("JavaApplicationAction.Type_Selection_4")); //$NON-NLS-1$
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
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
	 * Launches a configuration for the given type
	 */
	protected void launch(IType type, String mode) {
		try { 
			ILaunchConfiguration config = findLaunchConfiguration(type, mode);
			if (config != null) {
				DebugUITools.saveAndBuildBeforeLaunch();
				config.launch(mode, null);
			}			
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JavaApplicationAction.Launch_failed_7"), e.getStatus()); //$NON-NLS-1$
		}
	}
	
	/**
	 * Locate a configuration to relaunch for the given type.  If one cannot be found, create one.
	 * 
	 * @return a re-useable config or <code>null</code> if none
	 */
	protected ILaunchConfiguration findLaunchConfiguration(IType type, String mode) {
		ILaunchConfigurationType configType = getJavaLaunchConfigType();
		List candidateConfigs = Collections.EMPTY_LIST;
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
			candidateConfigs = new ArrayList(configs.length);
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				if (config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "").equals(type.getFullyQualifiedName())) { //$NON-NLS-1$
					if (config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(type.getJavaProject().getElementName())) { //$NON-NLS-1$
						candidateConfigs.add(config);
					}
				}
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		
		// If there are no existing configs associated with the IType, create one.
		// If there is exactly one config associated with the IType, return it.
		// Otherwise, if there is more than one config associated with the IType, prompt the
		// user to choose one.
		int candidateCount = candidateConfigs.size();
		if (candidateCount < 1) {
			return createConfiguration(type);
		} else if (candidateCount == 1) {
			return (ILaunchConfiguration) candidateConfigs.get(0);
		} else {
			// Prompt the user to choose a config.  A null result means the user
			// cancelled the dialog, in which case this method returns null,
			// since cancelling the dialog should also cancel launching anything.
			ILaunchConfiguration config = chooseConfiguration(candidateConfigs, mode);
			if (config != null) {
				return config;
			}
		}
		
		return null;
	}
	
	/**
	 * Show a selection dialog that allows the user to choose one of the specified
	 * launch configurations.  Return the chosen config, or <code>null</code> if the
	 * user cancelled the dialog.
	 */
	protected ILaunchConfiguration chooseConfiguration(List configList, String mode) {
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle(LauncherMessages.getString("JavaApplicationAction.Launch_Configuration_Selection_1"));  //$NON-NLS-1$
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			dialog.setMessage(LauncherMessages.getString("JavaApplicationAction.Choose_a_launch_configuration_to_debug_2"));  //$NON-NLS-1$
		} else {
			dialog.setMessage(LauncherMessages.getString("JavaApplicationAction.Choose_a_launch_configuration_to_run_3")); //$NON-NLS-1$
		}
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == dialog.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;		
	}
	
	/**
	 * Create & return a new configuration based on the specified <code>IType</code>.
	 */
	protected ILaunchConfiguration createConfiguration(IType type) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getJavaLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(type.getElementName())); 
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, type.getJavaProject().getElementName());
			wc.setAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, IDebugUIConstants.PERSPECTIVE_DEFAULT);
			wc.setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, IDebugUIConstants.PERSPECTIVE_DEFAULT);
			wc.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, JavaUISourceLocator.ID_PROMPTING_JAVA_SOURCE_LOCATOR);
			config = wc.doSave();		
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);			
		}
		return config;
	}
	
	/**
	 * Returns the local java launch config type
	 */
	protected ILaunchConfigurationType getJavaLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);		
	}
	
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Convenience method to get the window that owns this action's Shell.
	 */
	protected Shell getShell() {
		return JDIDebugUIPlugin.getActiveWorkbenchShell();
	}
	
	/**
	 * @see ILaunchShortcut#launch(IEditorPart, String)
	 */
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		IJavaElement je = (IJavaElement) input.getAdapter(IJavaElement.class);
		if (je != null) {
			searchAndLaunch(new Object[] {je}, mode);
		} else {
			// error
		}
		
	}

	/**
	 * @see ILaunchShortcut#launch(ISelection, String)
	 */
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			searchAndLaunch(((IStructuredSelection)selection).toArray(), mode);
		} else {
			// error
		}
	}

}