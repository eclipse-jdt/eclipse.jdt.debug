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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.ILogicalStructureTypeDelegate;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

public class JavaLogicalStructures implements ILogicalStructureTypeDelegate {
	
	/**
	 * The list of java logical structures in this Eclipse install.
	 */
	private static List fJavaLogicalStructures= initJavaLogicalStructureExtension();
	
	/**
	 * Get the configuration elements for the extension point and create the list 
	 * of JavaLogicalStructure.
	 */
	private static List initJavaLogicalStructureExtension() {
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
			IConfigurationElement[] variableElements= element.getChildren("variable"); //$NON-NLS-1$
			if (type == null) {
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
			javaLogicalStructures.add(new JavaLogicalStructure(type, subtypes, value, variables));
		}
		return javaLogicalStructures;
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

}
