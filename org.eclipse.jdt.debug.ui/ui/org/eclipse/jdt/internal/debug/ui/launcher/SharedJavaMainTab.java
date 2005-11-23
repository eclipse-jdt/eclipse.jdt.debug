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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * Provides general widgets and methods for a Java type launch configuration 
 * 'Main' tab. 
 * This class provides shared functionality for those main tabs which have a 'main type' field on them;
 * such as a main method for a local Java app, or an applet for Java applets
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
	protected void createMainTypeEditor(Composite parent, String text, Button[] buttons) {
		Font font= parent.getFont();
		Group mainGroup= new Group(parent, SWT.NONE);
		mainGroup.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainGroup.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		mainGroup.setLayout(layout);
		mainGroup.setFont(font);
		fMainText = new Text(mainGroup, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fMainText.setLayoutData(gd);
		fMainText.setFont(font);
		fMainText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		fSearchButton = createPushButton(mainGroup, LauncherMessages.AbstractJavaMainTab_2, null); 
		fSearchButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleSearchButtonSelected();
			}
		});
		if(buttons != null) {
			for(int i = 0; i < buttons.length; i++) {
				buttons[i].setParent(mainGroup);
			}//end for
		}//end if
	}
	
	/**
	 * The select button pressed handler
	 */
	protected abstract void handleSearchButtonSelected();

	/**
	 * Set the main type & name attributes on the working copy based on the IJavaElement
	 */
	protected void initializeMainTypeAndName(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		String name = null;
		if (javaElement instanceof IMember) {
			IMember member = (IMember)javaElement;
			if (member.isBinary()) {
				javaElement = member.getClassFile();
			}//end if 
			else {
				javaElement = member.getCompilationUnit();
			}//end else
		}//end if
		if (javaElement instanceof ICompilationUnit || javaElement instanceof IClassFile) {
			try {
				IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{javaElement}, false);
				MainMethodSearchEngine engine = new MainMethodSearchEngine();
				IType[] types = engine.searchMainMethods(getLaunchConfigurationDialog(), scope, false);				
				if (types != null && (types.length > 0)) {
					// Simply grab the first main type found in the searched element
					name = types[0].getFullyQualifiedName();
				}//end if
			} //end try
			catch (InterruptedException ie) {JDIDebugUIPlugin.log(ie);} 
			catch (InvocationTargetException ite) {JDIDebugUIPlugin.log(ite);}
		}//end if
		if (name == null) {
			name = EMPTY_STRING;
		}//end if
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, name);
		if (name.length() > 0) {
			int index = name.lastIndexOf('.');
			if (index > 0) {
				name = name.substring(index + 1);
			}//end if		
			name = getLaunchConfigurationDialog().generateName(name);
			config.rename(name);
		}//end if
	}
	
	/**
	 * Loads the main type from the launch configuration's preference store
	 * @param config the config to load the main type from
	 */
	protected void updateMainTypeFromConfig(ILaunchConfiguration config) {
		String mainTypeName = EMPTY_STRING;
		try {
			mainTypeName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, EMPTY_STRING);
		}//end try 
		catch (CoreException ce) {JDIDebugUIPlugin.log(ce);}	
		fMainText.setText(mainTypeName);	
	}
}
