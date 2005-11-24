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
package org.eclipse.jdt.internal.launching.environments;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentAnalyzer;

/**
 * Utility class for execution environments.
 * 
 * @since 3.2
 */
public class Environments {
	
	/**
	 * Map of environments keyed by id
	 */
	private static Map fEnvironments = null;
	
	/**
	 * Map of analyzers keyed by id
	 */
	private static Map fAnalyzers = null;
	
	/**
	 * Map of vm installs to lists of compatible environments
	 */
	private static Map fCompatibleEnvironments = null;
	
	/**
	 * Returns all registered execution environments.
	 * 
	 * @return all registered execution environments
	 */
	public synchronized static IExecutionEnvironment[] getExecutionEnvironments() {
		initialize();
		Collection environments = fEnvironments.values();
		return (IExecutionEnvironment[]) environments.toArray(new IExecutionEnvironment[environments.size()]);
	}
	
	/**
	 * Returns the execution environment associated with the given
	 * identifier or <code>null</code> if none.
	 * 
	 * @param id execution environment identifier 
	 * @return execution environment or <code>null</code>
	 */
	public synchronized static IExecutionEnvironment getEnvironment(String id) {
		initialize();
		return (IExecutionEnvironment) fEnvironments.get(id);
	}
	
	/**
	 * Returns the exeuctuion environments associated with the specified
	 * vm install, possibly an empty collection.
	 * 
	 * @param vm vm install
	 * @return exeuctuion environments associated with the specified
	 * vm install, possibly an empty collection
	 */
	public synchronized static IExecutionEnvironment[] getEnvironments(IVMInstall vm) {
		List environments = (List) fCompatibleEnvironments.get(vm);
		if (environments == null) {
			return new IExecutionEnvironment[0];
		}
		return (IExecutionEnvironment[]) environments.toArray(new IExecutionEnvironment[environments.size()]);
	}

	/**
	 * Returns the vm installs that are compatible with the given 
	 * execution environment, possibly an empty collection.
	 * 
	 * @param environment execution environment
	 * @return vm installs that are compatible with the given 
	 * execution environment, possibly an empty collection
	 */
	public synchronized static IVMInstall[] getVMInstalls(IExecutionEnvironment environment) {
		initialize();
		List vms = new ArrayList();
		Iterator iterator = fCompatibleEnvironments.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Entry) iterator.next();
			if (((List)entry.getValue()).contains(environment)) {
				vms.add(entry.getKey());
			}
		}
		return (IVMInstall[]) vms.toArray(new IVMInstall[vms.size()]);
	}
	
	/**
	 * Returns all registered exeuction environment analyzers.
	 * 
	 * @return all registered exeuction environment analyzers
	 */
	public synchronized static IExecutionEnvironmentAnalyzer[] getAnalyzers() { 
		initialize();
		Collection collection = fAnalyzers.values();
		return (IExecutionEnvironmentAnalyzer[]) collection.toArray(new IExecutionEnvironmentAnalyzer[collection.size()]);
	}	
	
	private synchronized static void initialize() {
		if (fEnvironments == null) {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, JavaRuntime.EXTENSION_POINT_EXECUTION_ENVIRONMENTS);
			IConfigurationElement[] configs= extensionPoint.getConfigurationElements();
			fEnvironments = new HashMap(configs.length);
			fAnalyzers = new HashMap(configs.length);
			for (int i = 0; i < configs.length; i++) {
				IConfigurationElement element = configs[i];
				String name = element.getName();
				if (name.equals("environment")) { //$NON-NLS-1$
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (id == null) {
						LaunchingPlugin.log(MessageFormat.format(EnvironmentMessages.Environments_0, new String[]{element.getNamespace()}));
					} else {
						fEnvironments.put(id, new ExecutionEnvironment(element));
					}
				} else if (name.equals("analyzer")) { //$NON-NLS-1$
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (id == null) {
						LaunchingPlugin.log(MessageFormat.format(EnvironmentMessages.Environments_1, new String[]{element.getNamespace()}));
					} else {
						fAnalyzers.put(id, new Analyzer(element));
					}
				}
			}
			// TODO: restore compatibilities from disk
			fCompatibleEnvironments = new HashMap();
		}
	}
	
	/**
	 * Recomputes the environments compatible with the given vm install.
	 * 
	 * @param vm
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public synchronized static IExecutionEnvironment[] analyze(IVMInstall vm, IProgressMonitor monitor) throws CoreException {
		IExecutionEnvironmentAnalyzer[] analyzers = getAnalyzers();
		List environmentList = new ArrayList();
		for (int i = 0; i < analyzers.length; i++) {
			IExecutionEnvironmentAnalyzer analyzer = analyzers[i];
			IExecutionEnvironment[] environments = analyzer.analyze(vm, monitor);
			for (int j = 0; j < environments.length; j++) {
				environmentList.add(environments[j]);
			}
		}
		fCompatibleEnvironments.put(vm, environmentList);
		return (IExecutionEnvironment[]) environmentList.toArray(new IExecutionEnvironment[environmentList.size()]);
	}
}
