/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.ILogicalStructureTypeDelegate;
import org.eclipse.debug.core.model.ILogicalStructureTypeDelegate2;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

public class JavaLogicalStructures implements ILogicalStructureTypeDelegate, ILogicalStructureTypeDelegate2 {
	
	// preference values
	static final char IS_SUBTYPE_TRUE= 'T';
	static final char IS_SUBTYPE_FALSE= 'F';
	
	/**
	 * The list of java logical structures.
	 */
	private static List fJavaLogicalStructures= initJavaLogicalStructures();

	/**
	 * The list of java logical structures in this Eclipse install.
	 */
	private static List fPluginContributedJavaLogicalStructures;
	
	/**
	 * The list of java logical structures defined by the user.
	 */
	private static List fUserDefinedJavaLogicalStructures;
	
	/**
	 * Get the logical structure from the extension point and the preference store.
	 */
	private static List initJavaLogicalStructures() {
		fPluginContributedJavaLogicalStructures= initPluginContributedJavaLogicalStructure();
		fUserDefinedJavaLogicalStructures= initUserDefinedJavaLogicalStructures();
		List logicalStructures= new ArrayList(fPluginContributedJavaLogicalStructures);
		logicalStructures.addAll(fUserDefinedJavaLogicalStructures);
		return logicalStructures;
	}
	
	/**
	 * Get the configuration elements for the extension point.
	 */
	private static List initPluginContributedJavaLogicalStructure() {
		List javaLogicalStructures= new ArrayList();
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.EXTENSION_POINT_JAVA_LOGICAL_STRUCTURES);
		IConfigurationElement[] javaLogicalStructureElements= extensionPoint.getConfigurationElements();
	outer:for (int i= 0; i < javaLogicalStructureElements.length; i++) {
			IConfigurationElement element= javaLogicalStructureElements[i];
			String type= element.getAttribute("type"); //$NON-NLS-1$
			if (type == null) {
				JDIDebugPlugin.log(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.INTERNAL_ERROR, LogicalStructuresMessages.getString("JavaLogicalStructures.0"), null)); //$NON-NLS-1$
				break;
			}
			boolean subtypes= Boolean.valueOf(element.getAttribute("subtypes")).booleanValue(); //$NON-NLS-1$
			String value= element.getAttribute("value"); //$NON-NLS-1$
			String description= element.getAttribute("description"); //$NON-NLS-1$
			if (type == null) {
				JDIDebugPlugin.log(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.INTERNAL_ERROR, LogicalStructuresMessages.getString("JavaLogicalStructures.4"), null)); //$NON-NLS-1$
				break;
			}
			IConfigurationElement[] variableElements= element.getChildren("variable"); //$NON-NLS-1$
			if (value == null && variableElements.length == 0) {
				JDIDebugPlugin.log(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.INTERNAL_ERROR, LogicalStructuresMessages.getString("JavaLogicalStructures.1"), null)); //$NON-NLS-1$
				break;
			}
			String[][] variables= new String[variableElements.length][2];
			for (int j= 0; j < variables.length; j++) {
				String variableName= variableElements[j].getAttribute("name"); //$NON-NLS-1$
				if (variableName == null) {
					JDIDebugPlugin.log(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.INTERNAL_ERROR, LogicalStructuresMessages.getString("JavaLogicalStructures.2"), null)); //$NON-NLS-1$
					break outer;
				}
				variables[j][0]= variableName;
				String variableValue= variableElements[j].getAttribute("value"); //$NON-NLS-1$
				if (variableValue == null) {
					JDIDebugPlugin.log(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.INTERNAL_ERROR, LogicalStructuresMessages.getString("JavaLogicalStructures.3"), null)); //$NON-NLS-1$
					break outer;
				}
				variables[j][1]= variableValue;
			}
			javaLogicalStructures.add(new JavaLogicalStructure(type, subtypes, value, description, variables, true));
		}
		return javaLogicalStructures;
	}
	
	/**
	 * Get the user defined logical structures (from the preference store).
	 */
	static private List initUserDefinedJavaLogicalStructures() {
		List logicalStructures= new ArrayList();
		String logicalStructuresString= JDIDebugModel.getPreferences().getString(JDIDebugModel.PREF_JAVA_LOGICAL_STRUCTURES);
		StringTokenizer tokenizer= new StringTokenizer(logicalStructuresString, "\0", true); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String type= tokenizer.nextToken();
			tokenizer.nextToken();
			String description= tokenizer.nextToken();
			tokenizer.nextToken();
			String isSubtypeValue= tokenizer.nextToken();
			boolean isSubtype= isSubtypeValue.charAt(0) == IS_SUBTYPE_TRUE;
			tokenizer.nextToken();
			String value= tokenizer.nextToken();
			if (value.charAt(0) == '\0') {
				value= null;
			} else {
				tokenizer.nextToken();
			}
			String variablesCounterValue= tokenizer.nextToken();
			int variablesCounter= Integer.parseInt(variablesCounterValue);
			tokenizer.nextToken();
			String[][] variables= new String[variablesCounter][2];
			for (int i= 0; i < variablesCounter; i++) {
				variables[i][0]= tokenizer.nextToken();
				tokenizer.nextToken();
				variables[i][1]= tokenizer.nextToken();
				tokenizer.nextToken();
			}
			logicalStructures.add(new JavaLogicalStructure(type, isSubtype, value, description, variables, false));
		}
		return logicalStructures;
	}
	
	/**
	 * Save the user defined logical structures in the preference store.
	 */
	static private void saveUserDefinedJavaLogicalStructures() {
		StringBuffer logicalStructuresString= new StringBuffer();
		for (Iterator iter= fUserDefinedJavaLogicalStructures.iterator(); iter.hasNext();) {
			JavaLogicalStructure logicalStructure= (JavaLogicalStructure) iter.next();
			logicalStructuresString.append(logicalStructure.getQualifiedTypeName()).append('\0');
			logicalStructuresString.append(logicalStructure.getDescription()).append('\0');
			logicalStructuresString.append(logicalStructure.isSubtypes() ? IS_SUBTYPE_TRUE : IS_SUBTYPE_FALSE).append('\0');
			String value= logicalStructure.getValue();
			if (value != null) {
				logicalStructuresString.append(value);
			}
			logicalStructuresString.append('\0');
			String[][] variables= logicalStructure.getVariables();
			logicalStructuresString.append(variables.length).append('\0');
			for (int i= 0; i < variables.length; i++) {
				String[] strings= variables[i];
				logicalStructuresString.append(strings[0]).append('\0');
				logicalStructuresString.append(strings[1]).append('\0');
			}
		}
		JDIDebugModel.getPreferences().setValue(JDIDebugModel.PREF_JAVA_LOGICAL_STRUCTURES, logicalStructuresString.toString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILogicalStructureTypeDelegate#providesLogicalStructure(org.eclipse.debug.core.model.IValue)
	 */
	public boolean providesLogicalStructure(IValue value) {
		if (!(value instanceof IJavaObject)) {
			return false;
		}
		IJavaObject javaValue= (IJavaObject) value;
		// go through all the java logical structures and return true if one
		// of them provide a logical structure for the given value.
		for (Iterator iter= fJavaLogicalStructures.iterator(); iter.hasNext();) {
			if (((JavaLogicalStructure) iter.next()).providesLogicalStructure(javaValue)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILogicalStructureTypeDelegate#getLogicalStructure(org.eclipse.debug.core.model.IValue)
	 */
	public IValue getLogicalStructure(IValue value) throws CoreException {
		if (!(value instanceof IJavaObject)) {
			return null;
		}
		IJavaObject javaValue= (IJavaObject) value;
		// go through all the java logical structures and return the first
		// value returned.
		for (Iterator iter= fJavaLogicalStructures.iterator(); iter.hasNext();) {
			IValue result= ((JavaLogicalStructure) iter.next()).getLogicalStructure(javaValue);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILogicalStructureTypeDelegate2#getDescription(org.eclipse.debug.core.model.IValue)
	 */
	public String getDescription(IValue value) {
		if (!(value instanceof IJavaObject)) {
			return null;
		}
		IJavaObject javaValue= (IJavaObject) value;
		// go through all the java logical structures and return true if one
		// of them provide a logical structure for the given value.
		for (Iterator iter= fJavaLogicalStructures.iterator(); iter.hasNext();) {
			JavaLogicalStructure javaLogicalStructure = (JavaLogicalStructure) iter.next();
			if (javaLogicalStructure.providesLogicalStructure(javaValue)) {
				return javaLogicalStructure.getDescription();
			}
		}
		return null;
	}
	
	/**
	 * Return the logical structure.
	 */
	static public JavaLogicalStructure[] getJavaLogicalStructures() {
	    return (JavaLogicalStructure[])fJavaLogicalStructures.toArray(new JavaLogicalStructure[fJavaLogicalStructures.size()]);
	}

	/**
	 * Set the user defined logical structure.
	 */
	static public void setUserDefinedJavaLogicalStructures(JavaLogicalStructure[] logicalStructures) {
		fUserDefinedJavaLogicalStructures= Arrays.asList(logicalStructures);
		fJavaLogicalStructures= new ArrayList(fPluginContributedJavaLogicalStructures);
		fJavaLogicalStructures.addAll(fUserDefinedJavaLogicalStructures);
		saveUserDefinedJavaLogicalStructures();
	}

}
