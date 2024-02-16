/*******************************************************************************
 * Copyright (c) 2005, 2023 IBM Corporation and others.
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
 *     Axel Richard (Obeo) - Bug 41353 - Launch configurations prototypes
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.launcher;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * Provides general widgets and methods for a Java type launch configuration
 * 'Main' tab.
 * This class provides shared functionality for those main tabs which have a 'main type' field on them;
 * such as a main method for a local Java application, or an Applet for Java Applets
 *
 * @since 3.2
 */
public abstract class SharedJavaMainTab extends AbstractJavaMainTab {

	protected Text fMainText;
	private Button fSearchButton;

	/**
	 * Creates the widgets for specifying a main type.
	 *
	 * @param parent the parent composite
	 */
	protected void createMainTypeEditor(Composite parent, String text) {
		Group group = SWTFactory.createGroup(parent, text, 2, 1, GridData.FILL_HORIZONTAL);
		fMainText = SWTFactory.createSingleText(group, 1);
		fMainText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		ControlAccessibleListener.addListener(fMainText, group.getText());
		fSearchButton = createPushButton(group, LauncherMessages.AbstractJavaMainTab_2, null);
		fSearchButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleSearchButtonSelected();
			}
		});
		createMainTypeExtensions(group);
	}

	/**
	 * This method allows the group for main type to be extended with custom controls.
	 * All control added via this method come after the main type text editor and search button in
	 * the order they are added to the parent composite
	 *
	 * @param parent the parent to add to
	 * @since 3.3
	 */
	protected void createMainTypeExtensions(Composite parent) {
		//do nothing by default
	}

	/**
	 * The select button pressed handler
	 */
	protected abstract void handleSearchButtonSelected();

	/**
	 * Set the main type and name attributes on the working copy based on the IJavaElement
	 */
	protected void initializeMainTypeAndName(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		String name = null;
		String moduleName = EMPTY_STRING;
		if (javaElement instanceof IMember) {
			IMember member = (IMember)javaElement;
			if (member.isBinary()) {
				javaElement = member.getClassFile();
			}
			else {
				javaElement = member.getCompilationUnit();
			}
		}
		if (javaElement instanceof ICompilationUnit || javaElement instanceof IClassFile) {
			try {
				IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{javaElement}, false);
				MainMethodSearchEngine engine = new MainMethodSearchEngine();
				IType[] types = engine.searchMainMethods(getLaunchConfigurationDialog(), scope, false);
				if (types != null && (types.length > 0)) {
					// Simply grab the first main type found in the searched element and set the module name
					name = types[0].getFullyQualifiedName('.');
					moduleName = getModuleName(types[0]);

				}
			}
			catch (InterruptedException ie) {JDIDebugUIPlugin.log(ie);}
			catch (InvocationTargetException ite) {JDIDebugUIPlugin.log(ite);}
		}
		if (name == null) {
			name = EMPTY_STRING;
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, name);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_NAME, moduleName);
		if (name.length() > 0) {
			String category = ""; //$NON-NLS-1$
			try {
				category = config.getType().getIdentifier();
			} catch (CoreException e) {
				// do nothing...we won't use qualified name in such a case
			}
			IPreferenceStore preferenceStore = JavaPlugin.getDefault().getPreferenceStore();
			boolean useQualification = false;
			if (category.equals("org.eclipse.jdt.launching.localJavaApplication")) { //$NON-NLS-1$ {
				useQualification = preferenceStore.getBoolean(PreferenceConstants.LAUNCH_NAME_FULLY_QUALIFIED_FOR_APPLICATION);
			} else if (category.equals("org.eclipse.jdt.launching.javaApplet")) { //$NON-NLS-1$
				useQualification = preferenceStore.getBoolean(PreferenceConstants.LAUNCH_NAME_FULLY_QUALIFIED_FOR_APPLET);
			} else if (category.equals("org.eclipse.jdt.junit.launchconfig")) { //$NON-NLS-1$
				useQualification = preferenceStore.getBoolean(PreferenceConstants.LAUNCH_NAME_FULLY_QUALIFIED_FOR_JUNIT_TEST);
			}
			if (!useQualification) {
				int index = name.lastIndexOf('.');
				if (index > 0) {
					name = name.substring(index + 1);
				}
			}
			name = getLaunchConfigurationDialog().generateName(name);
			config.rename(name);
		}
	}

	/**
	 * Loads the main type from the launch configuration's preference store
	 * @param config the config to load the main type from
	 */
	protected void updateMainTypeFromConfig(ILaunchConfiguration config) {
		String mainTypeName = EMPTY_STRING;
		String moduleName = EMPTY_STRING;
		try {
			mainTypeName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, EMPTY_STRING);
			moduleName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_NAME, EMPTY_STRING);
			if (moduleName.isEmpty()) {
				moduleName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
			}
		}
		catch (CoreException ce) {JDIDebugUIPlugin.log(ce);}
		fMainText.setText(mainTypeName);
		fModuleName = moduleName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab#initializeAttributes()
	 *
	 * @since 3.9
	 */
	@Override
	protected void initializeAttributes() {
		super.initializeAttributes();
		getAttributesLabelsForPrototype().put(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, LauncherMessages.SharedJavaMainTab_AttributeLabel_MainTypeName);
	}

	protected String getModuleName(IType type) {
		IJavaElement javaElement = type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		if (javaElement instanceof IPackageFragmentRoot) {
			IModuleDescription moduleDescription = ((IPackageFragmentRoot) (javaElement)).getModuleDescription();
			if (moduleDescription != null) {
				return moduleDescription.getElementName();
			}
		}
		return EMPTY_STRING;
	}
}
