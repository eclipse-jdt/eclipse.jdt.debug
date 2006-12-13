/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
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

import com.ibm.icu.text.MessageFormat;

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
	private static Map fgClasspathEntries = new HashMap(10);
	
	private static IAccessRule[] EMPTY_RULES = new IAccessRule[0];
	
	// debug flags
	public static boolean DEBUG_JRE_CONTAINER = false;
	
	/**
	 * Add a vm changed listener to clear cached values when a VM changes or is removed
	 */
	static {
		IVMInstallChangedListener listener = new IVMInstallChangedListener() {
			public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
			}

			public void vmChanged(PropertyChangeEvent event) {
				if (event.getSource() != null) {
					fgClasspathEntries.remove(event.getSource());
				}
			}

			public void vmAdded(IVMInstall newVm) {
			}

			public void vmRemoved(IVMInstall removedVm) {
				fgClasspathEntries.remove(removedVm);
			}
		};
		JavaRuntime.addVMInstallChangedListener(listener);
	}
	
	/**
	 * Initialize debug flags
	 */
	static {
		DEBUG_JRE_CONTAINER =LaunchingPlugin.DEBUG && "true".equals( //$NON-NLS-1$
		 Platform.getDebugOption("org.eclipse.jdt.launching/debug/classpath/jreContainer")); //$NON-NLS-1$
	}
	
	/**
	 * Returns the classpath entries associated with the given VM
	 * in the context of the given path and project.
	 * 
	 * @param vm
	 * @param containerPath the container path resolution is for
	 * @param project project the resolution is for
	 * @return classpath entries
	 */
	private static IClasspathEntry[] getClasspathEntries(IVMInstall vm, IPath containerPath, IJavaProject project) {
		String id = JavaRuntime.getExecutionEnvironmentId(containerPath);
		IClasspathEntry[] entries = null;
		if (id == null) {
			// cache classpath entries per JRE when not bound to an EE
			entries = (IClasspathEntry[])fgClasspathEntries.get(vm);
			if (entries == null) {
				entries = computeClasspathEntries(vm, project, id);
				fgClasspathEntries.put(vm, entries);
			}
		} else {
			if (DEBUG_JRE_CONTAINER) {
				System.out.println("\tEE:\t" + id); //$NON-NLS-1$
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
	 * @param vm
	 * @param project the project the resolution is for
	 * @param environmentId execution environment the resolution is for, or <code>null</code>
	 * @return classpath entries
	 */
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
		List entries = new ArrayList(libs.length);
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
				URL javadocLocation = libs[i].getJavadocLocation();
				if (overrideJavaDoc && javadocLocation == null) {
					javadocLocation = vm.getJavadocLocation();
				}
				IClasspathAttribute[] attributes = null;
				if (javadocLocation == null) {
					attributes = new IClasspathAttribute[0];
				} else {
					attributes = new IClasspathAttribute[]{JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, javadocLocation.toExternalForm())};
				}
				IAccessRule[] libRules = null;
				if (rules != null) {
					libRules = rules[i];
				} else {
					libRules = EMPTY_RULES;
				}
				entries.add(JavaCore.newLibraryEntry(libs[i].getSystemLibraryPath(), sourcePath, rootPath, libRules, attributes, false));
			}
		}
		return (IClasspathEntry[])entries.toArray(new IClasspathEntry[entries.size()]);		
	}
	
	/**
	 * Constructs a JRE classpath container on the given VM install
	 * 
	 * @param vm vm install - cannot be <code>null</code>
	 * @param path container path used to resolve this JRE
	 */
	public JREContainer(IVMInstall vm, IPath path, IJavaProject project) {
		fVMInstall = vm;
		fPath = path;
		fProject = project;
	}
	
	/**
	 * @see IClasspathContainer#getClasspathEntries()
	 */
	public IClasspathEntry[] getClasspathEntries() {
		if (DEBUG_JRE_CONTAINER) {
			System.out.println("<JRE_CONTAINER> getClasspathEntries() " + this.toString()); //$NON-NLS-1$
			System.out.println("\tJRE:\t" + fVMInstall.getName()); //$NON-NLS-1$
			System.out.println("\tPath:\t" + getPath().toString()); //$NON-NLS-1$
			System.out.println("\tProj:\t" + fProject.getProject().getName()); //$NON-NLS-1$
		}
		IClasspathEntry[] entries = getClasspathEntries(fVMInstall, getPath(), fProject);
		if (DEBUG_JRE_CONTAINER) {
			System.out.println("\tResolved " + entries.length + " entries:");  //$NON-NLS-1$//$NON-NLS-2$
//			for (int i = 0; i < entries.length; i++) {
//				System.out.println("\t\t" + entries[i].getPath().toString() + " +[" + entries[i].getAccessRules().length + " access rules]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			}
		}
		return entries;
	}

	/**
	 * @see IClasspathContainer#getDescription()
	 */
	public String getDescription() {
		String environmentId = JavaRuntime.getExecutionEnvironmentId(getPath());
		String tag = null;
		if (environmentId == null) {
			tag = fVMInstall.getName();
		} else {
			tag = environmentId;
		}
		return MessageFormat.format(LaunchingMessages.JREContainer_JRE_System_Library_1, new String[]{tag});
	}

	/**
	 * @see IClasspathContainer#getKind()
	 */
	public int getKind() {
		return IClasspathContainer.K_DEFAULT_SYSTEM;
	}

	/**
	 * @see IClasspathContainer#getPath()
	 */
	public IPath getPath() {
		return fPath;
	}

}
