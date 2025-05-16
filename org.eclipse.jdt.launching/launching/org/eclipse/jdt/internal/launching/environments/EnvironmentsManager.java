/*******************************************************************************
 *  Copyright (c) 2005, 2025 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.environments;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.JavaCore;
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
import org.eclipse.osgi.util.NLS;
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
public class EnvironmentsManager implements IExecutionEnvironmentsManager, IVMInstallChangedListener, IPreferenceChangeListener {

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
	private TreeSet<IExecutionEnvironment> fEnvironments = null;

	private SortedSet<IExecutionEnvironment> supportedEnvironments;

	/**
	 * List of access rule participants
	 */
	private Set<AccessRuleParticipant> fRuleParticipants = null;

	/**
	 * Map of environments keyed by id
	 */
	private Map<String, IExecutionEnvironment> fEnvironmentsMap = null;

	/**
	 * Map of analyzers keyed by id
	 */
	private Map<String, Analyzer> fAnalyzers = null;

	/**
	 * <code>true</code> while updating the default settings preferences
	 */
	private boolean fIsUpdatingDefaults = false;

	/**
	 * Whether compatible environments have been initialized
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
		InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN).addPreferenceChangeListener(this);
	}

	@Override
	public synchronized IExecutionEnvironment[] getExecutionEnvironments() {
		initializeExtensions();
		return fEnvironments.toArray(new IExecutionEnvironment[fEnvironments.size()]);
	}

	@Override
	public synchronized SortedSet<IExecutionEnvironment> getSupportedExecutionEnvironments() {
		initializeExtensions();
		return supportedEnvironments;
	}

	/**
	 * Returns all access rule participants that are not specific to an execution environment.
	 *
	 * @return all access rule participants that are not specific to an execution environment.
	 * @since 3.3
	 */
	public synchronized IAccessRuleParticipant[] getAccessRuleParticipants() {
		initializeExtensions();
		return fRuleParticipants.toArray(new IAccessRuleParticipant[fRuleParticipants.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager#getEnvironment(java.lang.String)
	 */
	@Override
	public synchronized IExecutionEnvironment getEnvironment(String id) {
		initializeExtensions();
		return fEnvironmentsMap.get(id);
	}

	/**
	 * Returns all registered analyzers
	 *
	 * @return all registered analyzers
	 */
	public synchronized Analyzer[] getAnalyzers() {
		initializeExtensions();
		Collection<Analyzer> collection = fAnalyzers.values();
		return collection.toArray(new Analyzer[collection.size()]);
	}

	private String getExecutionEnvironmentCompliance(IExecutionEnvironment executionEnvironment) {
		String desc = executionEnvironment.getId();
		if (desc.indexOf(JavaCore.VERSION_24) != -1) {
			return JavaCore.VERSION_24;
		} else if (desc.indexOf(JavaCore.VERSION_23) != -1) {
			return JavaCore.VERSION_23;
		} else if (desc.indexOf(JavaCore.VERSION_22) != -1) {
			return JavaCore.VERSION_22;
		} else if (desc.indexOf(JavaCore.VERSION_21) != -1) {
			return JavaCore.VERSION_21;
		} else if (desc.indexOf(JavaCore.VERSION_20) != -1) {
			return JavaCore.VERSION_20;
		} else if (desc.indexOf(JavaCore.VERSION_19) != -1) {
			return JavaCore.VERSION_19;
		} else if (desc.indexOf(JavaCore.VERSION_18) != -1) {
			return JavaCore.VERSION_18;
		} else if (desc.indexOf(JavaCore.VERSION_17) != -1) {
			return JavaCore.VERSION_17;
		} else if (desc.indexOf(JavaCore.VERSION_16) != -1) {
			return JavaCore.VERSION_16;
		} else if (desc.indexOf(JavaCore.VERSION_15) != -1) {
			return JavaCore.VERSION_15;
		} else if (desc.indexOf(JavaCore.VERSION_14) != -1) {
			return JavaCore.VERSION_14;
		} else if (desc.indexOf(JavaCore.VERSION_13) != -1) {
			return JavaCore.VERSION_13;
		} else if (desc.indexOf(JavaCore.VERSION_12) != -1) {
			return JavaCore.VERSION_12;
		} else if (desc.indexOf(JavaCore.VERSION_11) != -1) {
			return JavaCore.VERSION_11;
		} else if (desc.indexOf(JavaCore.VERSION_10) != -1) {
			return JavaCore.VERSION_10;
		} else if (desc.indexOf(JavaCore.VERSION_9) != -1) {
			return JavaCore.VERSION_9;
		} else if (desc.indexOf(JavaCore.VERSION_1_8) != -1) {
			return JavaCore.VERSION_1_8;
		} else if (desc.indexOf(JavaCore.VERSION_1_7) != -1) {
			return JavaCore.VERSION_1_7;
		} else if (desc.indexOf(JavaCore.VERSION_1_6) != -1) {
			return JavaCore.VERSION_1_6;
		} else if (desc.indexOf(JavaCore.VERSION_1_5) != -1) {
			return JavaCore.VERSION_1_5;
		} else if (desc.indexOf(JavaCore.VERSION_1_4) != -1) {
			return JavaCore.VERSION_1_4;
		} else if (desc.indexOf(JavaCore.VERSION_1_3) != -1) {
			return JavaCore.VERSION_1_3;
		} else if (desc.indexOf(JavaCore.VERSION_1_2) != -1) {
			return JavaCore.VERSION_1_2;
		} else if (desc.indexOf(JavaCore.VERSION_1_1) != -1) {
			return JavaCore.VERSION_1_1;
		} else if (desc.indexOf("1.0") != -1) { //$NON-NLS-1$
			return "1.0"; //$NON-NLS-1$
		}
		return JavaCore.VERSION_1_3;
	}

	private synchronized void initializeExtensions() {
		if (fEnvironments != null) {
			return;
		}
		Comparator<IExecutionEnvironment> eeComparator = ((Comparator<IExecutionEnvironment>) (o1, o2) -> {
			String compliance1 = getExecutionEnvironmentCompliance(o1);
			String compliance2 = getExecutionEnvironmentCompliance(o2);
			return JavaCore.compareJavaVersions(compliance1, compliance2);
		}).thenComparing(IExecutionEnvironment::getId);

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, JavaRuntime.EXTENSION_POINT_EXECUTION_ENVIRONMENTS);
		IConfigurationElement[] configs = extensionPoint.getConfigurationElements();
		fEnvironments = new TreeSet<>(eeComparator);
		fRuleParticipants = new LinkedHashSet<>();
		fEnvironmentsMap = new HashMap<>(configs.length);
		fAnalyzers = new HashMap<>(configs.length);
		for (int i = 0; i < configs.length; i++) {
			IConfigurationElement element = configs[i];
			String name = element.getName();
			switch (name) {
				case ENVIRONMENT_ELEMENT:
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (id == null) {
						LaunchingPlugin.log(NLS.bind("Execution environment must specify \"id\" attribute. Contributed by {0}.", element.getContributor().getName())); //$NON-NLS-1$
					} else {
						IExecutionEnvironment env = new ExecutionEnvironment(element);
						fEnvironments.add(env);
						fEnvironmentsMap.put(id, env);
					}
					break;
				case ANALYZER_ELEMENT:
					id = element.getAttribute("id"); //$NON-NLS-1$
					if (id == null) {

						LaunchingPlugin.log(NLS.bind("Execution environment analyzer must specify \"id\" attribute. Contributed by {0}", element.getContributor().getName())); //$NON-NLS-1$
					} else {
						fAnalyzers.put(id, new Analyzer(element));
					}
					break;
				case RULE_PARTICIPANT_ELEMENT:
					id = element.getAttribute("id"); //$NON-NLS-1$
					if (id == null) {
						LaunchingPlugin.log(NLS.bind("Execution environment rule participant must specify \"id\" attribute. Contributed by {0}", element.getContributor().getName())); //$NON-NLS-1$
					} else {
						// use a linked hash set to avoid duplicate rule participants
						fRuleParticipants.add(new AccessRuleParticipant(element));
					}
					break;
				default:
					break;
			}
		}

		SortedSet<IExecutionEnvironment> filtered = new TreeSet<>(eeComparator);
		for (IExecutionEnvironment environment : fEnvironments) {
			Map<String, String> options = environment.getComplianceOptions();
			if (options != null && JavaCore.isJavaSourceVersionSupportedByCompiler(options.get(JavaCore.COMPILER_COMPLIANCE))) {
				filtered.add(environment);
			}
		}
		supportedEnvironments = Collections.unmodifiableSortedSet(filtered);
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
	 * Reads persisted default VMs from the preference store
	 */
	private synchronized void initializeDefaultVMs() {
		String xml = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN).get(PREF_DEFAULT_ENVIRONMENTS_XML, ""); //$NON-NLS-1$
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
	 * @return an XML description of default VMs per environment. Returns
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
	 * Analyzes compatible execution environments for the given VM install.
	 *
	 * @param vm the {@link IVMInstall} to find environments for
	 * @param monitor a progress monitor or <code>null</code>
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
	@Override
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
	 */
	@Override
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
	@Override
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
	@Override
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
			InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN).put(PREF_DEFAULT_ENVIRONMENTS_XML, getDefatulVMsAsXML());
		} finally {
			fIsUpdatingDefaults = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
	 */
	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		// don't respond to myself
		if (fIsUpdatingDefaults) {
			return;
		}
		if (event.getKey().equals(PREF_DEFAULT_ENVIRONMENTS_XML)) {
			initializeDefaultVMs();
		}
	}
}
