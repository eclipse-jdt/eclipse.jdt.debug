/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;

import com.ibm.icu.text.MessageFormat;

/**
 * Resolves a container for a JRE classpath container entry.
 */
public class JREContainerInitializer extends ClasspathContainerInitializer {

	/**
	 * @see ClasspathContainerInitializer#initialize(IPath, IJavaProject)
	 */
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		if (JREContainer.DEBUG_JRE_CONTAINER) {
			System.out.println("<JRE_CONTAINER> initialize()"); //$NON-NLS-1$
			System.out.println("\tPath: " + containerPath.toString()); //$NON-NLS-1$
			System.out.println("\tProj: " + project.getProject().getName()); //$NON-NLS-1$
		}
		int size = containerPath.segmentCount();
		if (size > 0) {
			if (containerPath.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
				IVMInstall vm = resolveVM(containerPath);
				JREContainer container = null;
				if (vm != null) {
					if (JREContainer.DEBUG_JRE_CONTAINER) {
						System.out.println("\tResolved VM: " + vm.getName()); //$NON-NLS-1$
					}
					container = new JREContainer(vm, containerPath, project);
				} else {
					if (JREContainer.DEBUG_JRE_CONTAINER) {
						System.out.println("\t*** FAILED RESOLVE VM ***"); //$NON-NLS-1$
					}
				}
				JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {container}, null);
			} else {
				if (JREContainer.DEBUG_JRE_CONTAINER) {
					System.out.println("\t*** INVALID JRE CONTAINER PATH ***"); //$NON-NLS-1$
				}	
			}
		} else {
			if (JREContainer.DEBUG_JRE_CONTAINER) {
				System.out.println("\t*** NO SEGMENTS IN CONTAINER PATH ***"); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Sets the specified class path container for all of the given projects.
	 *  
	 * @param containerPath JRE container path
	 * @param projects projects set the container on
	 * @throws CoreException on failure
	 */
	public void initialize(IPath containerPath, IJavaProject[] projects) throws CoreException {
		int size = containerPath.segmentCount();
		if (size > 0) {
			if (containerPath.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
				int length = projects.length;
				IVMInstall vm = resolveVM(containerPath);
				IClasspathContainer[] containers = new JREContainer[length];
				if (vm != null) {
					if (JREContainer.DEBUG_JRE_CONTAINER) {
						System.out.println("\tResolved VM: " + vm.getName()); //$NON-NLS-1$
					}
					for (int i=0; i<length; i++) {
						containers[i] = new JREContainer(vm, containerPath, projects[i]);
					}
				} else {
					if (JREContainer.DEBUG_JRE_CONTAINER) {
						System.out.println("\t*** FAILED RESOLVE VM ***"); //$NON-NLS-1$
					}
				}
				JavaCore.setClasspathContainer(containerPath, projects, containers, null);
			} else {
				if (JREContainer.DEBUG_JRE_CONTAINER) {
					System.out.println("\t*** INVALID JRE CONTAINER PATH ***"); //$NON-NLS-1$
				}	
			}
		} else {
			if (JREContainer.DEBUG_JRE_CONTAINER) {
				System.out.println("\t*** NO SEGMENTS IN CONTAINER PATH ***"); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Returns the VM install associated with the container path, or <code>null</code>
	 * if it does not exist.
	 */
	public static IVMInstall resolveVM(IPath containerPath) {
		IVMInstall vm = null;
		if (containerPath.segmentCount() > 1) {
			// specific JRE
			String id = getExecutionEnvironmentId(containerPath);
			if (id != null) {
				if (JREContainer.DEBUG_JRE_CONTAINER) {
					System.out.println("<JRE_CONTAINER> resolveVM(IPath)"); //$NON-NLS-1$
					System.out.println("\tEE: " + id); //$NON-NLS-1$
				}
				IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
				IExecutionEnvironment environment = manager.getEnvironment(id);
				if (environment != null) {
					vm = resolveVM(environment);
				} else {
					if (JREContainer.DEBUG_JRE_CONTAINER) {
						System.out.println("\t*** NO ENVIRONMENT ***"); //$NON-NLS-1$
					}
				}
			} else {
				String vmTypeId = getVMTypeId(containerPath);
				String vmName = getVMName(containerPath);
				IVMInstallType vmType = JavaRuntime.getVMInstallType(vmTypeId);
				if (vmType != null) {
					vm = vmType.findVMInstallByName(vmName);
				}
			}
		} else {
			// workspace default JRE
			vm = JavaRuntime.getDefaultVMInstall();
		}		
		return vm;
	}
	
	/**
	 * Returns the VM install bound to the given execution environment
	 * or <code>null</code>.
	 * 
	 * @param environment
	 * @return vm install or <code>null</code>
	 * @since 3.2
	 */
	public static IVMInstall resolveVM(IExecutionEnvironment environment) {
		if (JREContainer.DEBUG_JRE_CONTAINER) {
			System.out.println("<JRE_CONTAINER> resolveVM(IExecutionEnvironment)"); //$NON-NLS-1$
		}
		IVMInstall vm = environment.getDefaultVM();
		if (vm == null) {
			IVMInstall[] installs = environment.getCompatibleVMs();
			// take the first strictly compatible vm if there is no default
			if (installs.length == 0 && JREContainer.DEBUG_JRE_CONTAINER) {
				System.out.println("\t*** NO COMPATIBLE VMS ***"); //$NON-NLS-1$
			}
			for (int i = 0; i < installs.length; i++) {
				IVMInstall install = installs[i];
				if (environment.isStrictlyCompatible(install)) {
					vm = install;
					if (installs.length == 0 && JREContainer.DEBUG_JRE_CONTAINER) {
						System.out.println("\tPerfect Match: " + vm.getName()); //$NON-NLS-1$
					}
					break;
				}
			}
			// use the first vm failing that
			if (vm == null && installs.length > 0) {
				vm = installs[0];
				if (installs.length == 0 && JREContainer.DEBUG_JRE_CONTAINER) {
					System.out.println("\tFirst Match: " + vm.getName()); //$NON-NLS-1$
				}
			}
		} else {
			if (JREContainer.DEBUG_JRE_CONTAINER) {
				System.out.println("\tUser Default VM: " + vm.getName()); //$NON-NLS-1$
			}
		}
		return vm;
	}
	
	/**
	 * Returns the segment from the path containing the execution environment id
	 * or <code>null</code>
	 * 
	 * @param path container path
	 * @return ee id
	 */
	public static String getExecutionEnvironmentId(IPath path) {
		String name = getVMName(path);
		if (name != null) {
			name = decodeEnvironmentId(name);
			IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
			IExecutionEnvironment environment = manager.getEnvironment(name);
			if (environment != null) {
				return environment.getId();
			}
		}
		return null;
	}
	
	/**
	 * Returns whether the given path identifies a vm by exeuction environment.
	 * 
	 * @param path
	 * @return whether the given path identifies a vm by exeuction environment
	 */
	public static boolean isExecutionEnvironment(IPath path) {
		return getExecutionEnvironmentId(path) != null;
	}
	
	/**
	 * Escapes foward slashes in environment id.
	 * 
	 * @param id
	 * @return esaped name
	 */
	public static String encodeEnvironmentId(String id) {
		return id.replace('/', '%');
	}
	
	public static String decodeEnvironmentId(String id) {
		return id.replace('%', '/');
	}
	
	/**
	 * Returns the VM type identifier from the given container ID path.
	 * 
	 * @return the VM type identifier from the given container ID path
	 */
	public static String getVMTypeId(IPath path) {
		return path.segment(1);
	}
	
	/**
	 * Returns the VM name from the given container ID path.
	 * 
	 * @return the VM name from the given container ID path
	 */
	public static String getVMName(IPath path) {
		return path.segment(2);
	}	
	
	/**
	 * The container can be updated if it refers to an existing VM.
	 * 
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#canUpdateClasspathContainer(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
	public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
		if (containerPath != null && containerPath.segmentCount() > 0) {
			if (JavaRuntime.JRE_CONTAINER.equals(containerPath.segment(0))) {
				return resolveVM(containerPath) != null;
			}
		}
		return false;
	}
	
	private static final IStatus READ_ONLY= new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, ClasspathContainerInitializer.ATTRIBUTE_READ_ONLY, new String(), null);
	private static final IStatus NOT_SUPPORTED= new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, ClasspathContainerInitializer.ATTRIBUTE_NOT_SUPPORTED, new String(), null);
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#getAccessRulesStatus(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
	public IStatus getAccessRulesStatus(IPath containerPath, IJavaProject project) {
		return READ_ONLY;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#getSourceAttachmentStatus(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
	public IStatus getSourceAttachmentStatus(IPath containerPath, IJavaProject project) {
		return Status.OK_STATUS;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#getAttributeStatus(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject, java.lang.String)
	 */
	public IStatus getAttributeStatus(IPath containerPath, IJavaProject project, String attributeKey) {
		if (attributeKey.equals(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME)) {
			return Status.OK_STATUS;
		}
		if (attributeKey.equals(JavaRuntime.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY)) {
			return Status.OK_STATUS;
		}
		return NOT_SUPPORTED;
	}

	/**
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#requestClasspathContainerUpdate(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.IClasspathContainer)
	 */
	public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project, IClasspathContainer containerSuggestion) throws CoreException {
		IVMInstall vm = resolveVM(containerPath);
		if (vm == null) { 
			IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST, MessageFormat.format(LaunchingMessages.JREContainerInitializer_JRE_referenced_by_classpath_container__0__does_not_exist__1, new String[]{containerPath.toString()}), null); 
			throw new CoreException(status);
		}
		// update of the vm with new library locations
		
		IClasspathEntry[] entries = containerSuggestion.getClasspathEntries();
		LibraryLocation[] libs = new LibraryLocation[entries.length];
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				IPath path = entry.getPath();
				File lib = path.toFile();
				if (lib.exists() && lib.isFile()) {
					IPath srcPath = entry.getSourceAttachmentPath();
					if (srcPath == null) {
						srcPath = Path.EMPTY;
					}
					IPath rootPath = entry.getSourceAttachmentRootPath();
					if (rootPath == null) {
						rootPath = Path.EMPTY;
					}
					URL javadocLocation = null;
					IClasspathAttribute[] extraAttributes = entry.getExtraAttributes();
					for (int j = 0; j < extraAttributes.length; j++) {
						IClasspathAttribute attribute = extraAttributes[j];
						if (attribute.getName().equals(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME)) {
							String url = attribute.getValue();
							if (url != null && url.trim().length() > 0) {
								try {
									javadocLocation = new URL(url);
								} catch (MalformedURLException e) {
									LaunchingPlugin.log(e);
								}
							}
						}
					}
					libs[i] = new LibraryLocation(path, srcPath, rootPath, javadocLocation);
				} else {
					IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, MessageFormat.format(LaunchingMessages.JREContainerInitializer_Classpath_entry__0__does_not_refer_to_an_existing_library__2, new String[]{entry.getPath().toString()}), null); 
					throw new CoreException(status);
				}
			} else {
				IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, MessageFormat.format(LaunchingMessages.JREContainerInitializer_Classpath_entry__0__does_not_refer_to_a_library__3, new String[]{entry.getPath().toString()}), null); 
				throw new CoreException(status);
			}
		}
		VMStandin standin = new VMStandin(vm);
		standin.setLibraryLocations(libs);
		standin.convertToRealVM();
		JavaRuntime.saveVMConfiguration();
	}

	/**
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#getDescription(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
	public String getDescription(IPath containerPath, IJavaProject project) {
		String tag = getExecutionEnvironmentId(containerPath);
		if (tag == null && containerPath.segmentCount() > 2) {
			tag = getVMName(containerPath);
		}
		if (tag != null) {
			return MessageFormat.format(LaunchingMessages.JREContainer_JRE_System_Library_1, new String[]{tag});
		} 
		return LaunchingMessages.JREContainerInitializer_Default_System_Library_1; 
	}
}
