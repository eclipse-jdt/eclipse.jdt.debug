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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.PropertyChangeEvent;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utility class for execution environments.
 * 
 * @since 3.2
 */
public class EnvironmentsManager implements IExecutionEnvironmentsManager, IVMInstallChangedListener, IPropertyChangeListener {

	private static EnvironmentsManager fgManager = null;
	
	/**
	 * Preference store key for XML storing default environments.
	 */
	private static final String PREF_DEFAULT_ENVIRONMENTS_XML = "org.eclipse.jdt.launching.PREF_DEFAULT_ENVIRONMENTS_XML"; //$NON-NLS-1$
	
	/**
	 * Map of environments keyed by id
	 */
	private Map fEnvironments = null;
	
	/**
	 * Map of analyzers keyed by id
	 */
	private Map fAnalyzers = null;
	
	/**
	 * Map of vm installs to lists of compatible environments
	 */
	private Map fCompatibleEnvironments = null;
	
	/**
	 * Map of environments to default VM for that environment
	 */
	private Map fDefaultVMs = null;
	
	/**
	 * Whether in the process of setting a default VM 
	 */
	private boolean fIsSettingDefaults = false;

	/**
	 * XML attribute
	 */
	private static final String VM_ID = "vmId"; //$NON-NLS-1$

	/**
	 * XML attribute
	 */
	private static final String ENVIRONMENT_ID = "environmentId"; //$NON-NLS-1$

	/**
	 * XML element
	 */
	private static final String DEFAULT_ENVIRONMENT = "defaultEnvironment"; //$NON-NLS-1$

	/**
	 * XML document
	 */
	private static final String DEFAULT_ENVIRONMENTS = "defaultEnvironments"; //$NON-NLS-1$
	
	/**
	 * Returns the singleton environments manager.
	 * 
	 * @return environments manager
	 */
	public static EnvironmentsManager getDefault() {
		if (fgManager == null) {
			fgManager = new EnvironmentsManager();
		}
		return fgManager;
	}
	
	/**
	 * Constructs the new manager.
	 */
	private EnvironmentsManager() {
		JavaRuntime.addVMInstallChangedListener(this);
		LaunchingPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager#getExecutionEnvironments()
	 */
	public synchronized IExecutionEnvironment[] getExecutionEnvironments() {
		initializeExtensions();
		Collection environments = fEnvironments.values();
		return (IExecutionEnvironment[]) environments.toArray(new IExecutionEnvironment[environments.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager#getEnvironment(java.lang.String)
	 */
	public synchronized IExecutionEnvironment getEnvironment(String id) {
		initializeExtensions();
		return (IExecutionEnvironment) fEnvironments.get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager#getEnvironments(org.eclipse.jdt.launching.IVMInstall)
	 */
	public synchronized IExecutionEnvironment[] getEnvironments(IVMInstall vm) {
		initializeCompatibilities();
		List environments = (List) fCompatibleEnvironments.get(vm);
		if (environments == null) {
			return new IExecutionEnvironment[0];
		}
		return (IExecutionEnvironment[]) environments.toArray(new IExecutionEnvironment[environments.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager#getVMInstalls(org.eclipse.jdt.launching.environments.IExecutionEnvironment)
	 */
	public synchronized IVMInstall[] getVMInstalls(IExecutionEnvironment environment) {
		initializeCompatibilities();
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
	 * Returns all registered analyzers 
	 * 
	 * @return
	 */
	public synchronized Analyzer[] getAnalyzers() { 
		initializeExtensions();
		Collection collection = fAnalyzers.values();
		return (Analyzer[]) collection.toArray(new Analyzer[collection.size()]);
	}	
	
	private synchronized void initializeExtensions() {
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
		}
	}
	
	/**
	 * Initializes the cache of JRE/EE compatibilities. Returns whether the cache was
	 * built from scratch.
	 * 
	 * @return whether the compatibilites were built from scratch
	 */
	private synchronized boolean initializeCompatibilities() {
		initializeExtensions();
		if (fCompatibleEnvironments == null) {
			fCompatibleEnvironments = new HashMap();
			initializeDefaultVMs();
			// TODO: restore from preference
			// build from scratch
			IVMInstallType[] installTypes = JavaRuntime.getVMInstallTypes();
			for (int i = 0; i < installTypes.length; i++) {
				IVMInstallType type = installTypes[i];
				IVMInstall[] installs = type.getVMInstalls();
				for (int j = 0; j < installs.length; j++) {
					IVMInstall install = installs[j];
					try {
						// TODO: progress reporting?
						List list = analyze(install, new NullProgressMonitor());
						fCompatibleEnvironments.put(install, list);
					} catch (CoreException e) {
						LaunchingPlugin.log(e);
					}
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Reads persisted default VMs from pref store
	 */
	private synchronized void initializeDefaultVMs() {
		fDefaultVMs = new HashMap();
		String xml = LaunchingPlugin.getDefault().getPluginPreferences().getString(PREF_DEFAULT_ENVIRONMENTS_XML);
		try {
			if (xml.length() > 0) {
				DocumentBuilder parser = LaunchingPlugin.getParser();
				
				Document document = parser.parse(new ByteArrayInputStream(xml.getBytes()));
				Element envs = document.getDocumentElement();
				NodeList list = envs.getChildNodes();
				int length = list.getLength();
				for (int i = 0; i < length; ++i) {
					Node node = list.item(i);
					short type = node.getNodeType();
					if (type == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						if (element.getNodeName().equals(DEFAULT_ENVIRONMENT)) {
							String envId = element.getAttribute(ENVIRONMENT_ID);
							String vmId = element.getAttribute(VM_ID);
							IExecutionEnvironment environment = getEnvironment(envId);
							if (environment != null) {
								IVMInstall vm = JavaRuntime.getVMFromCompositeId(vmId);
								if (vm != null) {
									fDefaultVMs.put(environment, vm);
								}
							}
						}
					}
				}			
			}
		} catch (CoreException e) {
			LaunchingPlugin.log(e);
		} catch (SAXException e) {
			LaunchingPlugin.log(e);
		} catch (IOException e) {
			LaunchingPlugin.log(e);
		}
	}

	/**
	 * Returns an XML description of default VMs per environment. Returns
	 * an empty string when there are none. 
	 */
	private String getDefatulVMsAsXML() {
		if (fDefaultVMs.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		try {
			Document doc = LaunchingPlugin.getDocument();
			Element envs = doc.createElement(DEFAULT_ENVIRONMENTS);
			doc.appendChild(envs);
			Iterator iterator = fDefaultVMs.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry entry = (Entry) iterator.next();
				IExecutionEnvironment env = (IExecutionEnvironment) entry.getKey();
				IVMInstall vm = (IVMInstall) entry.getValue();
				Element element = doc.createElement(DEFAULT_ENVIRONMENT);
				element.setAttribute(ENVIRONMENT_ID, env.getId());
				element.setAttribute(VM_ID, JavaRuntime.getCompositeIdFromVM(vm));
				envs.appendChild(element);
			}
			return LaunchingPlugin.serializeDocument(doc);
		} catch (ParserConfigurationException e) {
			LaunchingPlugin.log(e);
		} catch (IOException e) {
			LaunchingPlugin.log(e);
		} catch (TransformerException e) {
			LaunchingPlugin.log(e);
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Analyzes and returns execution environments for the given vm install.
	 * 
	 * @param vm
	 * @param monitor
	 * @return execution environments for the given vm install
	 * @throws CoreException
	 */
	private List analyze(IVMInstall vm, IProgressMonitor monitor) throws CoreException {
		Analyzer[] analyzers = getAnalyzers();
		List environmentList = new ArrayList();
		for (int i = 0; i < analyzers.length; i++) {
			Analyzer analyzer = analyzers[i];
			IExecutionEnvironment[] environments = analyzer.analyze(vm, monitor);
			for (int j = 0; j < environments.length; j++) {
				environmentList.add(environments[j]);
			}
		}	
		return environmentList;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.IVMInstall)
	 */
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
	 */
	public synchronized void vmChanged(PropertyChangeEvent event) {
		if (fCompatibleEnvironments != null) {
			IVMInstall vm = (IVMInstall) event.getSource();
			vmRemoved(vm);
			vmAdded(vm);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
	 */
	public synchronized void vmAdded(IVMInstall vm) {
		if (fCompatibleEnvironments != null) {
			try {
				// TODO: progress?
				List list = analyze(vm, new NullProgressMonitor());
				fCompatibleEnvironments.put(vm, list);
			} catch (CoreException e) {
				LaunchingPlugin.log(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
	 */
	public synchronized void vmRemoved(IVMInstall vm) {
		if (fCompatibleEnvironments != null) {
			fCompatibleEnvironments.remove(vm);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager#getDefaultVMInstall(org.eclipse.jdt.launching.environments.IExecutionEnvironment)
	 */
	public synchronized IVMInstall getDefaultVMInstall(IExecutionEnvironment environment) {
		initializeCompatibilities();
		return (IVMInstall)fDefaultVMs.get(environment);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager#setDefaultVMInstall(org.eclipse.jdt.launching.environments.IExecutionEnvironment, org.eclipse.jdt.launching.IVMInstall)
	 */
	public synchronized void setDefaultVMInstall(IExecutionEnvironment environment, IVMInstall vm) throws CoreException {
		initializeCompatibilities();
		if (!isCompatible(vm, environment)) {
			IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, 
					MessageFormat.format(EnvironmentMessages.EnvironmentsManager_0, new String[]{environment.getId()}), null);
			throw new CoreException(status);
		}
		fDefaultVMs.put(environment, vm);
		try {
			fIsSettingDefaults = true;
			LaunchingPlugin.getDefault().getPluginPreferences().setValue(PREF_DEFAULT_ENVIRONMENTS_XML, getDefatulVMsAsXML());
		} finally {
			fIsSettingDefaults = false;
		}
		// TODO: this should change container bindings (i.e. trigger builds)
	}
	
	private synchronized boolean isCompatible(IVMInstall vm, IExecutionEnvironment env) {
		List envs = (List) fCompatibleEnvironments.get(vm);
		if (envs != null) {
			return envs.contains(env);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public synchronized void propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
		// don't respond to myself
		if (fIsSettingDefaults) {
			return;
		}
		if (event.getProperty().equals(PREF_DEFAULT_ENVIRONMENTS_XML)) {
			initializeDefaultVMs();
		}
	}
		
}
