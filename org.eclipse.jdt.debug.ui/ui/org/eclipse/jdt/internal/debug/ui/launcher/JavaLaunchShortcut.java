/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;

 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Common behavior for Java launch shortcuts
 * 
 * @since 3.2
 */
public abstract class JavaLaunchShortcut implements ILaunchShortcut {
	
	/**
	 * @param search the java elements to search for a main type
	 * @param mode the mode to launch in
	 * @param editor activated on an editor (or from a selection in a viewer)
	 */
	public void searchAndLaunch(Object[] search, String mode, String selectMessage, String emptyMessage) {
		IType[] types = null;
		try {
			types = findTypes(search, PlatformUI.getWorkbench().getProgressService());
		} 
		catch (InterruptedException e) {return;} 
		catch (CoreException e) {
			MessageDialog.openError(getShell(), LauncherMessages.JavaLaunchShortcut_0, e.getMessage()); 
			return;
		}
		IType type = null;
		if (types.length == 0) {
			MessageDialog.openError(getShell(), LauncherMessages.JavaLaunchShortcut_1, emptyMessage); 
		} 
		else if (types.length > 1) {
			type = chooseType(types, selectMessage);
		} 
		else {
			type = types[0];
		}
		if (type != null) {
			launch(type, mode);
		}
	}	
	
	/**
	 * Finds and returns the launchable types in the given selection of elements.
	 * 
	 * @param elements scope to search for launchable types
	 * @param context progress reporting context
	 * @return launchable types, possibly empty
	 * @exception InterruptedException if the search is canceled
	 * @exception org.eclipse.core.runtime.CoreException if the search fails
	 */
	protected abstract IType[] findTypes(Object[] elements, IRunnableContext context) throws InterruptedException, CoreException;

	/**
	 * Prompts the user to select a type from the given types.
	 * 
	 * @param types the types to choose from
	 * @param title the selection dialog title
	 * 
	 * @return the selected type or <code>null</code> if none.
	 */
	protected IType chooseType(IType[] types, String title) {
		DebugTypeSelectionDialog mmsd = new DebugTypeSelectionDialog(JDIDebugUIPlugin.getShell(), types, title);
		if (mmsd.open() == Window.OK) {
			return (IType)mmsd.getResult()[0];
		}
		return null;
	}
	
	/**
	 * Launches a configuration for the given type
	 */
	protected void launch(IType type, String mode) {
		ILaunchConfiguration config = findLaunchConfiguration(type, getConfigurationType());
		if (config != null) {
			DebugUITools.launch(config, mode);
		}			
	}
	
	/**
	 * Returns the type of configuration this shortcut is applicable to.
	 * 
	 * @return the type of configuration this shortcut is applicable to
	 */
	protected abstract ILaunchConfigurationType getConfigurationType();
	
	/**
	 * Locate a configuration to relaunch for the given type.  If one cannot be found, create one.
	 * 
	 * @return a re-usable config or <code>null</code> if none
	 */
	protected ILaunchConfiguration findLaunchConfiguration(IType type, ILaunchConfigurationType configType) {
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
			// canceled the dialog, in which case this method returns null,
			// since canceling the dialog should also cancel launching anything.
			ILaunchConfiguration config = chooseConfiguration(candidateConfigs);
			if (config != null) {
				return config;
			}
		}
		
		return null;
	}
	
	/**
	 * Show a selection dialog that allows the user to choose one of the specified
	 * launch configurations.  Return the chosen config, or <code>null</code> if the
	 * user canceled the dialog.
	 */
	protected ILaunchConfiguration chooseConfiguration(List configList) {
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle(getTypeSelectionTitle());  
		dialog.setMessage(LauncherMessages.JavaLaunchShortcut_2);
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == Window.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;		
	}
	
	/**
	 * Create and returns a new configuration based on the specified <code>IType</code>.
	 */
	protected abstract ILaunchConfiguration createConfiguration(IType type);
	
	/**
	 * Opens an error dialog on the given exception.
	 * 
	 * @param exception
	 */
	protected void reportErorr(CoreException exception) {
		MessageDialog.openError(getShell(), LauncherMessages.JavaLaunchShortcut_3, exception.getStatus().getMessage());  
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
			searchAndLaunch(new Object[] {je}, mode, getTypeSelectionTitle(), getEditorEmptyMessage());
		}
	}

	/**
	 * @see ILaunchShortcut#launch(ISelection, String)
	 */
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			searchAndLaunch(((IStructuredSelection)selection).toArray(), mode, getTypeSelectionTitle(), getSelectionEmptyMessage());
		}
	}

	/**
	 * Returns the title for type selection dialog for this launch shortcut.
	 * 
	 * @return type selection dialog title
	 */
	protected abstract String getTypeSelectionTitle();
	
	/**
	 * Returns an error message to use when the editor does not contain a launchable type.
	 * 
	 * @return error message
	 */
	protected abstract String getEditorEmptyMessage();	
	
	/**
	 * Returns an error message to use when the selection does not contain a launchable type.
	 * 
	 * @return error message
	 */
	protected abstract String getSelectionEmptyMessage();		
}
