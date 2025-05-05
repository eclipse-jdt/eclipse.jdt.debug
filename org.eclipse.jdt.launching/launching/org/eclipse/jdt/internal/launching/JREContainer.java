/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Frits Jalvingh - Contribution for Bug 459831 - [launching] Support attaching
 *     	external annotations to a JRE container
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.PropertyChangeEvent;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.osgi.util.NLS;

/**
 * JRE Container - resolves a classpath container variable to a JRE
 */
public class JREContainer implements IClasspathContainer {

	/**
	 * Corresponding JRE
	 */
	private IVMInstall fVMInstall = null;

	/**
	 * Container path used to resolve to this JRE
	 */
	private IPath fPath = null;

	/**
	 * The project this container is for
	 */
	private IJavaProject fProject = null;

	/**
	 * Cache of classpath entries per VM install. Cleared when a VM changes.
	 */
	private static Map<IVMInstall, IClasspathEntry[]> fgClasspathEntries = new ConcurrentHashMap<>();

	/**
	 * Variable to return an empty array of <code>IAccessRule</code>s
	 */
	private static IAccessRule[] EMPTY_RULES = new IAccessRule[0];

	/**
	 * Map of {IVMInstall -> Map of {{IExeuctionEnvironment, IAccessRule[][]} -> {IClasspathEntry[]}}
	 */
	private static Map<RuleKey, RuleEntry> fgClasspathEntriesWithRules = new ConcurrentHashMap<>();

	/**
	 * A single key entry for the cache of access rules and classpath entries
	 * A rule key is made up of an <code>IVMInstall</code> and an execution environment id
	 * @since 3.3
	 */
	static class RuleKey {
		private final String fEnvironmentId;
		private final IVMInstall fInstall;

		/**
		 * Constructor
		 * @param install the VM
		 * @param environmentId the environment
		 */
		public RuleKey(IVMInstall install, String environmentId) {
			fInstall = install;
			fEnvironmentId = environmentId;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RuleKey key) {
				return fEnvironmentId.equals(key.fEnvironmentId) && fInstall.equals(key.fInstall);
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return fEnvironmentId.hashCode() + fInstall.hashCode();
		}
	}

	/**
	 * Holds an entry for the cache of access rules/classpath entries.
	 * An entry is made up of an array of classpath entries and the collection of access rules.
	 * @since 3.3
	 */
	static class RuleEntry {
		private IAccessRule[][] fRules = null;
		private IClasspathEntry[] fEntries = null;

		/**
		 * Constructor
		 * @param rules the rules
		 * @param entries the entries
		 */
		public RuleEntry(IAccessRule[][] rules, IClasspathEntry[] entries) {
			fRules = rules;
			fEntries = entries;
		}

		/**
		 * Returns the collection of classpath entries for this RuleEntry
		 * @return the cached array of classpath entries
		 */
		public IClasspathEntry[] getClasspathEntries() {
			return fEntries;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			IAccessRule[][] rules = null;
			if(obj instanceof RuleEntry) {
				rules = ((RuleEntry)obj).fRules;
			}
			if(obj instanceof IAccessRule[][]) {
				rules = (IAccessRule[][]) obj;
			}
			if (fRules == rules) {
				return true;
			}
			if(rules != null) {
				if (fRules.length == rules.length) {
					for (int i = 0; i < fRules.length; i++) {
						if (!rulesEqual(fRules[i], rules[i])){
							return false;
						}
					}
					return true;
				}
			}
			return false;
		}

		/**
		 * Checks if the two arrays of rules are equal (same rules in each position in the array)
		 *
		 * @param a First list of rules to compare, must not be <code>null</code>
		 * @param b Second list of rules to compare, must not be <code>null</code>
		 * @return <code>true</code> if the arrays are equal, <code>false</code> otherwise
		 */
		private static boolean rulesEqual(IAccessRule[] a, IAccessRule[] b){
			if (a == b){
				return true;
			}
			if (a.length != b.length){
				return false;
			}
			for (int j = 0; j < a.length; j++) {
				if (!a[j].equals(b[j])) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Add a VM changed listener to clear cached values when a VM changes or is removed
	 */
	static {
		IVMInstallChangedListener listener = new IVMInstallChangedListener() {

			/* (non-Javadoc)
			 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.IVMInstall)
			 */
			@Override
			public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {}

			/* (non-Javadoc)
			 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
			 */
			@Override
			public void vmAdded(IVMInstall newVm) {}

			/* (non-Javadoc)
			 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
			 */
			@Override
			public void vmChanged(PropertyChangeEvent event) {
				Object source = event.getSource();
				if (source != null) {
					fgClasspathEntries.remove(source);
					removeRuleEntry(source);
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
			 */
			@Override
			public void vmRemoved(IVMInstall removedVm) {
				fgClasspathEntries.remove(removedVm);
				removeRuleEntry(removedVm);
			}

			/**
			 * Removes all occurrences of the given VM found as part key members in the current
			 * cache for classpath entries
			 * @param obj an object which should be castable to IVMInstall
			 */
			private void removeRuleEntry(Object obj) {
				if (obj instanceof IVMInstall install) {
					fgClasspathEntriesWithRules.keySet().removeIf(key -> key.fInstall.equals(install));
				}
			}
		};
		JavaRuntime.addVMInstallChangedListener(listener);
	}

	/**
	 * Returns the classpath entries associated with the given VM
	 * in the context of the given path and project.
	 *
	 * @param vm the VM
	 * @param containerPath the container path resolution is for
	 * @param project project the resolution is for
	 * @return classpath entries
	 */
	private static IClasspathEntry[] getClasspathEntries(IVMInstall vm, IPath containerPath, IJavaProject project) {
		String id = JavaRuntime.getExecutionEnvironmentId(containerPath);
		IClasspathEntry[] entries = null;
		if (id == null) {
			// cache classpath entries per JRE when not bound to an EE
			entries = fgClasspathEntries.get(vm);
			if (entries == null) {
				entries = computeClasspathEntries(vm, project, id);
				fgClasspathEntries.put(vm, entries);
			}
		} else {
			if (LaunchingPlugin.DEBUG_JRE_CONTAINER) {
				LaunchingPlugin.trace("\tEE:\t" + id); //$NON-NLS-1$
			}
			// dynamically compute entries when bound to an EE
			entries = computeClasspathEntries(vm, project, id);
		}
		return entries;
	}

	/**
	 * Computes the classpath entries associated with a VM - one entry per library
	 * in the context of the given path and project.
	 *
	 * @param vm the VM
	 * @param project the project the resolution is for
	 * @param environmentId execution environment the resolution is for, or <code>null</code>
	 * @return classpath entries
	 */
	@SuppressWarnings("unlikely-arg-type")
	private static IClasspathEntry[] computeClasspathEntries(IVMInstall vm, IJavaProject project, String environmentId) {
		LibraryLocation[] libs = vm.getLibraryLocations();
		boolean overrideJavaDoc = false;
		if (libs == null) {
			libs = JavaRuntime.getLibraryLocations(vm);
			overrideJavaDoc = true;
		}
		IAccessRule[][] rules = null;
		if (environmentId != null) {
			// compute access rules for execution environment
			IExecutionEnvironment environment = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(environmentId);
			if (environment != null) {
				rules = environment.getAccessRules(vm, libs, project);
			}
		}
		RuleKey key = null;
		if (vm != null && rules != null && environmentId != null) {
			key = new RuleKey(vm, environmentId);
			RuleEntry entry = fgClasspathEntriesWithRules.get(key);
			if(entry != null && entry.equals(rules)) {
				return entry.getClasspathEntries();
			}
		}
		List<IClasspathEntry> entries = new ArrayList<>(libs.length);
		for (int i = 0; i < libs.length; i++) {
			if (!libs[i].getSystemLibraryPath().isEmpty()) {
				IPath sourcePath = libs[i].getSystemLibrarySourcePath();
				if (sourcePath.isEmpty()) {
					sourcePath = null;
				}
				IPath rootPath = libs[i].getPackageRootPath();
				if (rootPath.isEmpty()) {
					rootPath = null;
				}
				// construct the classpath attributes for this library location
				IClasspathAttribute[] attributes = JREContainer.buildClasspathAttributes(vm, libs[i], overrideJavaDoc);
				IAccessRule[] libRules = null;
				if (rules != null) {
					libRules = rules[i];
				} else {
					libRules = EMPTY_RULES;
				}
				entries.add(JavaCore.newLibraryEntry(libs[i].getSystemLibraryPath(), sourcePath, rootPath, libRules, attributes, false));
			}
		}
		IClasspathEntry[] cpEntries = entries.toArray(new IClasspathEntry[entries.size()]);
		if (key != null && rules != null) {
			fgClasspathEntriesWithRules.put(key, new RuleEntry(rules, cpEntries));
		}
		return cpEntries;
	}

	private static IClasspathAttribute[] buildClasspathAttributes(final IVMInstall vm, final LibraryLocation lib, final boolean overrideJavaDoc) {

		List<IClasspathAttribute> classpathAttributes = new LinkedList<>();
		// process the javadoc location
		URL javadocLocation = lib.getJavadocLocation();
		if (overrideJavaDoc && javadocLocation == null) {
			javadocLocation = vm.getJavadocLocation();
		}
		if(javadocLocation != null) {
			IClasspathAttribute javadocCPAttribute = JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, javadocLocation.toExternalForm());
			classpathAttributes.add(javadocCPAttribute);
		}
		// process the index location
		URL indexLocation = lib.getIndexLocation();
		if(indexLocation != null) {
			IClasspathAttribute indexCPLocation = JavaCore.newClasspathAttribute(IClasspathAttribute.INDEX_LOCATION_ATTRIBUTE_NAME, indexLocation.toExternalForm());
			classpathAttributes.add(indexCPLocation);
		}
		IPath annotationsPath = lib.getExternalAnnotationsPath();
		if (null != annotationsPath && !annotationsPath.isEmpty()) {
			IClasspathAttribute xAnnLocation = JavaCore.newClasspathAttribute(IClasspathAttribute.EXTERNAL_ANNOTATION_PATH, annotationsPath.toPortableString());
			classpathAttributes.add(xAnnLocation);
		}

		return classpathAttributes.toArray(new IClasspathAttribute[classpathAttributes.size()]);
	}

	/**
	 * Constructs a JRE classpath container on the given VM install
	 *
	 * @param vm VM install - cannot be <code>null</code>
	 * @param path container path used to resolve this JRE
	 * @param project the project context
	 */
	public JREContainer(IVMInstall vm, IPath path, IJavaProject project) {
		fVMInstall = vm;
		fPath = path;
		fProject = project;
	}

	/**
	 * @see IClasspathContainer#getClasspathEntries()
	 */
	@Override
	public IClasspathEntry[] getClasspathEntries() {
		if (LaunchingPlugin.DEBUG_JRE_CONTAINER) {
			LaunchingPlugin.trace("<JRE_CONTAINER> getClasspathEntries() " + this.toString()); //$NON-NLS-1$
			LaunchingPlugin.trace("\tJRE:\t" + fVMInstall.getName()); //$NON-NLS-1$
			LaunchingPlugin.trace("\tPath:\t" + getPath().toString()); //$NON-NLS-1$
			LaunchingPlugin.trace("\tProj:\t" + fProject.getProject().getName()); //$NON-NLS-1$
		}
		IClasspathEntry[] entries = getClasspathEntries(fVMInstall, getPath(), fProject);
		if (LaunchingPlugin.DEBUG_JRE_CONTAINER) {
			LaunchingPlugin.trace("\tResolved " + entries.length + " entries:");  //$NON-NLS-1$//$NON-NLS-2$
		}
		return entries;
	}

	/**
	 * @see IClasspathContainer#getDescription()
	 */
	@Override
	public String getDescription() {
		String environmentId = JavaRuntime.getExecutionEnvironmentId(getPath());
		String tag = null;
		if (environmentId == null) {
			tag = fVMInstall.getName();
		} else {
			tag = environmentId;
		}
		return NLS.bind(LaunchingMessages.JREContainer_JRE_System_Library_1, tag);
	}

	/**
	 * @see IClasspathContainer#getKind()
	 */
	@Override
	public int getKind() {
		return IClasspathContainer.K_DEFAULT_SYSTEM;
	}

	/**
	 * @see IClasspathContainer#getPath()
	 */
	@Override
	public IPath getPath() {
		return fPath;
	}

}
