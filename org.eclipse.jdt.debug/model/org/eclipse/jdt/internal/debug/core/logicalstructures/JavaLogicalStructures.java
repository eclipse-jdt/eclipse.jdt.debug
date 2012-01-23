/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILogicalStructureProvider;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.osgi.service.prefs.BackingStoreException;

public class JavaLogicalStructures implements ILogicalStructureProvider {

	// preference values
	static final char IS_SUBTYPE_TRUE = 'T';
	static final char IS_SUBTYPE_FALSE = 'F';

	/**
	 * The list of java logical structures.
	 */
	private static Map<String, List<JavaLogicalStructure>> fJavaLogicalStructureMap;

	/**
	 * The list of java logical structures in this Eclipse install.
	 */
	private static List<JavaLogicalStructure> fPluginContributedJavaLogicalStructures;

	/**
	 * The list of java logical structures defined by the user.
	 */
	private static List<JavaLogicalStructure> fUserDefinedJavaLogicalStructures;

	/**
	 * The list of java logical structures listeners.
	 */
	private static Set<IJavaStructuresListener> fListeners = new HashSet<IJavaStructuresListener>();

	/**
	 * Preference key for the list of user defined Java logical structures
	 * 
	 * @since 3.1
	 */
	private static final String PREF_JAVA_LOGICAL_STRUCTURES = JDIDebugModel
			.getPluginIdentifier() + ".PREF_JAVA_LOGICAL_STRUCTURES"; //$NON-NLS-1$

	/**
	 * Updates user defined logical structures if the preference changes
	 */
	static class PreferenceListener implements IPreferenceChangeListener {
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
		 */
		public void preferenceChange(PreferenceChangeEvent event) {
			if (PREF_JAVA_LOGICAL_STRUCTURES.equals(event.getKey())) {
				initUserDefinedJavaLogicalStructures();
				initJavaLogicalStructureMap();
				Iterator<IJavaStructuresListener> iter = fListeners.iterator();
				while (iter.hasNext()) {
					iter.next().logicalStructuresChanged();
				}
			}
		}
	}

	/**
	 * Get the logical structure from the extension point and the preference
	 * store, and initialize the map.
	 */
	static {
		initPluginContributedJavaLogicalStructure();
		initUserDefinedJavaLogicalStructures();
		initJavaLogicalStructureMap();
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		if(prefs != null) {
			prefs.addPreferenceChangeListener(new PreferenceListener());
		}
	}

	private static void initJavaLogicalStructureMap() {
		fJavaLogicalStructureMap = new HashMap<String, List<JavaLogicalStructure>>();
		addAllLogicalStructures(fPluginContributedJavaLogicalStructures);
		addAllLogicalStructures(fUserDefinedJavaLogicalStructures);
	}

	/**
	 * @param pluginContributedJavaLogicalStructures
	 */
	private static void addAllLogicalStructures(
			List<JavaLogicalStructure> pluginContributedJavaLogicalStructures) {
		for (Iterator<JavaLogicalStructure> iter = pluginContributedJavaLogicalStructures.iterator(); iter
				.hasNext();) {
			addLogicalStructure(iter.next());
		}
	}

	/**
	 * @param structure
	 */
	private static void addLogicalStructure(JavaLogicalStructure structure) {
		String typeName = structure.getQualifiedTypeName();
		List<JavaLogicalStructure> logicalStructure = fJavaLogicalStructureMap.get(typeName);
		if (logicalStructure == null) {
			logicalStructure = new ArrayList<JavaLogicalStructure>();
			fJavaLogicalStructureMap.put(typeName, logicalStructure);
		}
		logicalStructure.add(structure);
	}

	/**
	 * Get the configuration elements for the extension point.
	 */
	private static void initPluginContributedJavaLogicalStructure() {
		fPluginContributedJavaLogicalStructures = new ArrayList<JavaLogicalStructure>();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(JDIDebugPlugin.getUniqueIdentifier(),
						JDIDebugPlugin.EXTENSION_POINT_JAVA_LOGICAL_STRUCTURES);
		IConfigurationElement[] javaLogicalStructureElements = extensionPoint
				.getConfigurationElements();
		for (IConfigurationElement javaLogicalStructureElement : javaLogicalStructureElements) {
			try {
				fPluginContributedJavaLogicalStructures
						.add(new JavaLogicalStructure(
								javaLogicalStructureElement));
			} catch (CoreException e) {
				JDIDebugPlugin.log(e);
			}
		}
	}

	/**
	 * Get the user defined logical structures (from the preference store).
	 */
	private static void initUserDefinedJavaLogicalStructures() {
		fUserDefinedJavaLogicalStructures = new ArrayList<JavaLogicalStructure>();
		String logicalStructuresString = Platform.getPreferencesService().getString(
				JDIDebugPlugin.getUniqueIdentifier(), 
				PREF_JAVA_LOGICAL_STRUCTURES, 
				"",  //$NON-NLS-1$
				null);
		StringTokenizer tokenizer = new StringTokenizer(
				logicalStructuresString, "\0", true); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String type = tokenizer.nextToken();
			tokenizer.nextToken();
			String description = tokenizer.nextToken();
			tokenizer.nextToken();
			String isSubtypeValue = tokenizer.nextToken();
			boolean isSubtype = isSubtypeValue.charAt(0) == IS_SUBTYPE_TRUE;
			tokenizer.nextToken();
			String value = tokenizer.nextToken();
			if (value.charAt(0) == '\0') {
				value = null;
			} else {
				tokenizer.nextToken();
			}
			String variablesCounterValue = tokenizer.nextToken();
			int variablesCounter = Integer.parseInt(variablesCounterValue);
			tokenizer.nextToken();
			String[][] variables = new String[variablesCounter][2];
			for (int i = 0; i < variablesCounter; i++) {
				variables[i][0] = tokenizer.nextToken();
				tokenizer.nextToken();
				variables[i][1] = tokenizer.nextToken();
				tokenizer.nextToken();
			}
			fUserDefinedJavaLogicalStructures.add(new JavaLogicalStructure(
					type, isSubtype, value, description, variables));
		}
	}

	/**
	 * Save the user defined logical structures in the preference store.
	 */
	public static void saveUserDefinedJavaLogicalStructures() {
		StringBuffer logicalStructuresString = new StringBuffer();
		for (Iterator<JavaLogicalStructure> iter = fUserDefinedJavaLogicalStructures.iterator(); iter
				.hasNext();) {
			JavaLogicalStructure logicalStructure = iter
					.next();
			logicalStructuresString.append(
					logicalStructure.getQualifiedTypeName()).append('\0');
			logicalStructuresString.append(logicalStructure.getDescription())
					.append('\0');
			logicalStructuresString.append(
					logicalStructure.isSubtypes() ? IS_SUBTYPE_TRUE
							: IS_SUBTYPE_FALSE).append('\0');
			String value = logicalStructure.getValue();
			if (value != null) {
				logicalStructuresString.append(value);
			}
			logicalStructuresString.append('\0');
			String[][] variables = logicalStructure.getVariables();
			logicalStructuresString.append(variables.length).append('\0');
			for (String[] strings : variables) {
				logicalStructuresString.append(strings[0]).append('\0');
				logicalStructuresString.append(strings[1]).append('\0');
			}
		}
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		if(node != null) {
			node.put(PREF_JAVA_LOGICAL_STRUCTURES, logicalStructuresString.toString());
			try {
				node.flush();
			} catch (BackingStoreException e) {
				JDIDebugPlugin.log(e);
			}
		}
	}

	/**
	 * Return all the defined logical structures.
	 */
	public static JavaLogicalStructure[] getJavaLogicalStructures() {
		JavaLogicalStructure[] logicalStructures = new JavaLogicalStructure[fPluginContributedJavaLogicalStructures
				.size() + fUserDefinedJavaLogicalStructures.size()];
		int i = 0;
		for (Iterator<JavaLogicalStructure> iter = fPluginContributedJavaLogicalStructures.iterator(); iter
				.hasNext();) {
			logicalStructures[i++] = iter.next();
		}
		for (Iterator<JavaLogicalStructure> iter = fUserDefinedJavaLogicalStructures.iterator(); iter
				.hasNext();) {
			logicalStructures[i++] = iter.next();
		}
		return logicalStructures;
	}

	/**
	 * Set the user defined logical structures.
	 */
	public static void setUserDefinedJavaLogicalStructures(
			JavaLogicalStructure[] logicalStructures) {
		fUserDefinedJavaLogicalStructures = Arrays.asList(logicalStructures);
		saveUserDefinedJavaLogicalStructures();
	}

	public static void addStructuresListener(IJavaStructuresListener listener) {
		fListeners.add(listener);
	}

	public static void removeStructuresListener(IJavaStructuresListener listener) {
		fListeners.remove(listener);
	}

	public ILogicalStructureType[] getLogicalStructureTypes(IValue value) {
		if (!(value instanceof IJavaObject)) {
			return new ILogicalStructureType[0];
		}
		IJavaObject javaValue = (IJavaObject) value;
		List<JavaLogicalStructure> logicalStructures = new ArrayList<JavaLogicalStructure>();
		try {
			IJavaType type = javaValue.getJavaType();
			if (!(type instanceof IJavaClassType)) {
				return new ILogicalStructureType[0];
			}
			IJavaClassType classType = (IJavaClassType) type;
			List<JavaLogicalStructure> list = fJavaLogicalStructureMap
					.get(classType.getName());
			if (list != null) {
				logicalStructures.addAll(list);
			}
			IJavaClassType superClass = classType.getSuperclass();
			while (superClass != null) {
				addIfIsSubtype(logicalStructures,
						fJavaLogicalStructureMap.get(superClass
								.getName()));
				superClass = superClass.getSuperclass();
			}
			IJavaInterfaceType[] superInterfaces = classType.getAllInterfaces();
			for (IJavaInterfaceType superInterface : superInterfaces) {
				addIfIsSubtype(logicalStructures,
						fJavaLogicalStructureMap.get(superInterface
								.getName()));
			}
		} catch (DebugException e) {
			JDIDebugPlugin.log(e);
			return new ILogicalStructureType[0];
		}
		return logicalStructures
				.toArray(new ILogicalStructureType[logicalStructures.size()]);
	}

	private void addIfIsSubtype(List<JavaLogicalStructure> logicalStructures, List<JavaLogicalStructure> list) {
		if (list == null) {
			return;
		}
		for(JavaLogicalStructure jls : list) {
			if (jls.isSubtypes()) {
				logicalStructures.add(jls);
			}
		}
	}

}
