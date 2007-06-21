/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.PropertyChangeEvent;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.launching.environments.CompatibleEnvironment;
import org.eclipse.jdt.launching.environments.IAccessRuleParticipant;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.icu.text.MessageFormat;

/**
 * Utility class for execution environments.
 * 
 * @since 3.2
 */
public class EnvironmentsManager implements IExecutionEnvironmentsManager, IVMInstallChangedListener, IPropertyChangeListener {

	/**
	 * Extension configuration element name.
	 */
	private static final String ANALYZER_ELEMENT = "analyzer"; //$NON-NLS-1$

	/**
	 * Extension configuration element name.
	 */
	static final String ENVIRONMENT_ELEMENT = "environment"; //$NON-NLS-1$
	
	/**
	 * Extension configuration element name.
	 */
	static final String RULE_PARTICIPANT_ELEMENT = "ruleParticipant"; //$NON-NLS-1$	

	private static EnvironmentsManager fgManager = null;
	
	/**
	 * Preference store key for XML storing default environments.
	 */
	private static final String PREF_DEFAULT_ENVIRONMENTS_XML = "org.eclipse.jdt.launching.PREF_DEFAULT_ENVIRONMENTS_XML"; //$NON-NLS-1$
	
	/**
	 * List of environments 
	 */
	private List fEnvironments = null;
	
	/**
	 * List of access rule participants
	 */
	private List fRuleParticipants = null;
	
	/**
	 * Map of environments keyed by id
	 */
	private Map fEnvironmentsMap = null;
	
	/**
	 * Map of analyzers keyed by id
	 */
	private Map fAnalyzers = null;
	
	/**
	 * <code>true</code> while updating the default settings pref
	 */
	private boolean fIsUpdatingDefaults = false;
	
	/**
	 * Whether compatibile environnments have been initialized
	 */
	private boolean fInitializedCompatibilities = false;

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
		return (IExecutionEnvironment[]) fEnvironments.toArray(new IExecutionEnvironment[fEnvironments.size()]);
	}
	
	/**
	 * Returns all access rule participants that are not specific to an execution environment.
	 * 
	 * @return all access rule participants that are not specific to an execution environment.
	 * @since 3.3
	 */
	public synchronized IAccessRuleParticipant[] getAccessRuleParticipants() {
		initializeExtensions();
		return (IAccessRuleParticipant[]) fRuleParticipants.toArray(new IAccessRuleParticipant[fRuleParticipants.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager#getEnvironment(java.lang.String)
	 */
	public synchronized IExecutionEnvironment getEnvironment(String id) {
		initializeExtensions();
		return (IExecutionEnvironment) fEnvironmentsMap.get(id);
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
			fEnvironments = new ArrayList();
			fRuleParticipants = new ArrayList();
			fEnvironmentsMap = new HashMap(configs.length);
			fAnalyzers = new HashMap(configs.length);
			for (int i = 0; i < configs.length; i++) {
				IConfigurationElement element = configs[i];
				String name = element.getName();
				if (name.equals(ENVIRONMENT_ELEMENT)) {
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (id == null) {
						LaunchingPlugin.log(MessageFormat.format("Execution environment must specify \"id\" attribute. Contributed by {0}.", new String[]{element.getContributor().getName()})); //$NON-NLS-1$
					} else {
						IExecutionEnvironment env = new ExecutionEnvironment(element);
						fEnvironments.add(env);
						fEnvironmentsMap.put(id, env);
					}
				} else if (name.equals(ANALYZER_ELEMENT)) {
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (id == null) {
						LaunchingPlugin.log(MessageFormat.format("Execution environment analyzer must specify \"id\" attribute. Contributed by {0}", new String[]{element.getContributor().getName()})); //$NON-NLS-1$
					} else {
						fAnalyzers.put(id, new Analyzer(element));
					}
				} else if (name.equals(RULE_PARTICIPANT_ELEMENT)) {
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (id == null) {
						LaunchingPlugin.log(MessageFormat.format("Execution environment rule participant must specify \"id\" attribute. Contributed by {0}", new String[]{element.getContributor().getName()})); //$NON-NLS-1$
					} else {
						fRuleParticipants.add(new AccessRuleParticipant(element));
					}
				}
			}
		}
	}
	
	/**
	 * Initializes compatibility settings.
	 */
	void initializeCompatibilities() {
		IVMInstallType[] installTypes = JavaRuntime.getVMInstallTypes();
        synchronized (this) {
        	if (!fInitializedCompatibilities) {
	        	fInitializedCompatibilities = true;
	            for (int i = 0; i < installTypes.length; i++) {
	                IVMInstallType type = installTypes[i];
	                IVMInstall[] installs = type.getVMInstalls();
	                for (int j = 0; j < installs.length; j++) {
	                    IVMInstall install = installs[j];
	                    // TODO: progress reporting?
	                    analyze(install, new NullProgressMonitor());
	                }
	            }
	            initializeDefaultVMs();
        	}
        }
	}
	
	/**
	 * Reads persisted default VMs from pref store
	 */
	private synchronized void initializeDefaultVMs() {
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
							ExecutionEnvironment environment = (ExecutionEnvironment) getEnvironment(envId);
							if (environment != null) {
								IVMInstall vm = JavaRuntime.getVMFromCompositeId(vmId);
								if (vm != null) {
									environment.initDefaultVM(vm);
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
		int count = 0;
		try {
			Document doc = DebugPlugin.newDocument();
			Element envs = doc.createElement(DEFAULT_ENVIRONMENTS);
			doc.appendChild(envs);
			IExecutionEnvironment[] environments = getExecutionEnvironments();
			for (int i = 0; i < environments.length; i++) {
				IExecutionEnvironment env = environments[i];
				IVMInstall vm = env.getDefaultVM();
				if (vm != null) {
					count++;
					Element element = doc.createElement(DEFAULT_ENVIRONMENT);
					element.setAttribute(ENVIRONMENT_ID, env.getId());
					element.setAttribute(VM_ID, JavaRuntime.getCompositeIdFromVM(vm));
					envs.appendChild(element);
				}
			}
			if (count > 0) {
				return DebugPlugin.serializeDocument(doc);
			}
		} catch (CoreException e) {
			LaunchingPlugin.log(e);
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Analyzes and compatible execution environments for the given vm install.
	 * 
	 * @param vm
	 * @param monitor
	 */
	private void analyze(IVMInstall vm, IProgressMonitor monitor) {
		Analyzer[] analyzers = getAnalyzers();
		for (int i = 0; i < analyzers.length; i++) {
			Analyzer analyzer = analyzers[i];
			try {
				CompatibleEnvironment[] environments = analyzer.analyze(vm, monitor);
				for (int j = 0; j < environments.length; j++) {
					CompatibleEnvironment compatibleEnvironment = environments[j];
					ExecutionEnvironment environment = (ExecutionEnvironment) compatibleEnvironment.getCompatibleEnvironment();
					environment.add(vm, compatibleEnvironment.isStrictlyCompatbile());					
				}
			} catch (CoreException e) {
				LaunchingPlugin.log(e);
			}
		}	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.IVMInstall)
	 */
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
	 */
	public synchronized void vmChanged(PropertyChangeEvent event) {
		IVMInstall vm = (IVMInstall) event.getSource();
		if (vm instanceof VMStandin) {
			return;
		}
		vmRemoved(vm);
		vmAdded(vm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
	 */
	public synchronized void vmAdded(IVMInstall vm) {
		// TODO: progress reporting?
		if (vm instanceof VMStandin) {
			return;
		}
		analyze(vm, new NullProgressMonitor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
	 */
	public synchronized void vmRemoved(IVMInstall vm) {
		if (vm instanceof VMStandin) {
			return;
		}
		IExecutionEnvironment[] environments = getExecutionEnvironments();
		for (int i = 0; i < environments.length; i++) {
			ExecutionEnvironment environment = (ExecutionEnvironment) environments[i];
			environment.remove(vm);
		}
	}

	synchronized void updateDefaultVMs() {
		try {
			fIsUpdatingDefaults = true;
			LaunchingPlugin.getDefault().getPluginPreferences().setValue(PREF_DEFAULT_ENVIRONMENTS_XML, getDefatulVMsAsXML());
		} finally {
			fIsUpdatingDefaults = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public synchronized void propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
		// don't respond to myself
		if (fIsUpdatingDefaults) {
			return;
		}
		if (event.getProperty().equals(PREF_DEFAULT_ENVIRONMENTS_XML)) {
			initializeDefaultVMs();
		}
	}
		
}
