package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.ui.JavaDebugUI;

/**
 * This class listens for resource changes and deletes 'local java' launch configurations when
 * their main type has been deleted.  This class also contains a number of static helper methods
 * useful for the 'local java' delegate.
 */
public class JavaLocalApplicationLaunchConfigurationHelper implements IResourceChangeListener,
																		 ILaunchConfigurationListener {

	private class LocalJavaConfigurationVisitor implements IResourceDeltaVisitor {
		
		public boolean visit(IResourceDelta delta) {
			
			// If no delta, do nothing and return false since no children 
			if (delta == null) {
				return false;
			}
			
			// If resource is NOT an IFile, do nothing, but do examine children
			IResource resource = delta.getResource();
			if (!(resource instanceof IFile)) {
				return true;
			}			
			
			// So resource IS an IFile.  
			
			
			return true;
		}
	}
	
	/**
	 * Maps <code>IType</code>s to <code>List</code>s of <code>ILaunchCofiguration</code>s
	 */
	private Map fTypeToConfigMap;
	
	private LocalJavaConfigurationVisitor fResourceVisitor ;

	/**
	 * Fill the internal type-to-config map with all currently known local java configs, then
	 * register for resource change & launch configuration events.  Launch configuration events
	 * allow this class to keep its internal map up to date as far as what configurations of 
	 * type 'local java' exist, and resource change events are when this class deletes all
	 * 'local java' type configs that specified the deleted type as their main type.
	 */
	public JavaLocalApplicationLaunchConfigurationHelper() {
		initializeConfigMap();
		ResourcesPlugin.getPlugin().getWorkspace().addResourceChangeListener(this);
		getLaunchManager().addLaunchConfigurationListener(this);
	}
	
	/**
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) {
			try {
				if (fResourceVisitor == null) {
					fResourceVisitor= new LocalJavaConfigurationVisitor();
				}
				delta.accept(fResourceVisitor);
			} catch (CoreException e) {
				DebugPlugin.logError(e);
			}
		}		
	}
	
	/**
	 * @see ILaunchConfigurationListener#launchConfigurationAdded(ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration config) {
		try {
			addConfig(config);
		} catch (CoreException ce) {
		}
	}

	/**
	 * @see ILaunchConfigurationListener#launchConfigurationChanged(ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration config) {		
	}
	
	/**
	 * @see ILaunchConfigurationListener#launchConfigurationRemoved(ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration config) {
		try {
			removeConfig(config);
		} catch (CoreException ce) {
		}
	}

	/**
	 * Populate the map of ITypes to lists of configs.
	 */
	protected void initializeConfigMap() {
		fTypeToConfigMap = new HashMap(10);
		ILaunchConfiguration[] configs = new ILaunchConfiguration[] {};
		try {
			ILaunchConfigurationType type = getConfigurationType();
			configs = getLaunchManager().getLaunchConfigurations(type);			
		} catch (CoreException ce) {			
			return;
		}
		
		for (int i = 0; i < configs.length; i++) {
			try {
				addConfig(configs[i]);
			} catch (CoreException ce) {
			}
		}
	}
	
	/**
	 * Add the specified configuration to the internal map of types to configs
	 */
	protected void addConfig(ILaunchConfiguration configuration) throws CoreException {
		
		// Get the main type
		IType mainType = getMainType(configuration);
		if (mainType == null) {
			return;
		}
			
		// Add the config to the type's list	
		List configList = (List) fTypeToConfigMap.get(mainType);
		if (configList == null) {
			configList = new ArrayList();
			fTypeToConfigMap.put(mainType, configList);
		}
		configList.add(configuration);
	}
	
	/**
	 * Remove the specified configuration from the internal map of types to configs
	 */
	protected void removeConfig(ILaunchConfiguration configuration) throws CoreException {

		// Get the main type
		IType mainType = getMainType(configuration);
		if (mainType == null) {
			return;
		}
			
		// Remove the config from the type's list
		List configList = (List) fTypeToConfigMap.get(mainType);
		if (configList == null) {
			return;
		}
		configList.remove(configuration);
	}
	
	/**
	 * Return the <code>IType</code> referenced in the specified configuration or throw a 
	 * <code>CoreException</code> whose message explains why this couldn't be done.
	 */
	protected static IType getMainType(ILaunchConfiguration configuration) throws CoreException {
		return getMainType(configuration, getJavaProject(configuration));
	}
	
	/**
	 * Return the <code>IJavaProject</code> referenced in the specified configuration or throw a
	 * <code>CoreException</code> whose message explains why this couldn't be done.
	 */
	protected static IJavaProject getJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(JavaDebugUI.PROJECT_ATTR, (String)null);
		if ((projectName == null) || (projectName.trim().length() < 1)) {
			abort("No project specified", null, JavaDebugUI.UNSPECIFIED_PROJECT);
		}			
		IJavaProject javaProject = getJavaModel().getJavaProject(projectName);		
		if ((javaProject == null) || !javaProject.exists()) {
			abort("Invalid project specified", null, JavaDebugUI.NOT_A_JAVA_PROJECT);
		}
		return javaProject;
	}
	
	/**
	 * Return the <code>IType</code> referenced in the specified configuration and contained in 
	 * the specified project or throw a <code>CoreException</code> whose message explains why 
	 * this couldn't be done.
	 */
	protected static IType getMainType(ILaunchConfiguration configuration, IJavaProject javaProject) throws CoreException {
		String mainTypeName = configuration.getAttribute(JavaDebugUI.MAIN_TYPE_ATTR, (String)null);
		if ((mainTypeName == null) || (mainTypeName.trim().length() < 1)) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Main_type_not_specified._1"), null, JavaDebugUI.UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		IType mainType = null;
		try {
			mainType = findType(javaProject, mainTypeName);
		} catch (JavaModelException jme) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Main_type_does_not_exist"), null, JavaDebugUI.UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		if (mainType == null) {
			abort(DebugUIMessages.getString("JavaApplicationLaunchConfigurationDelegate.Main_type_does_not_exist"), null, JavaDebugUI.UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		return mainType;
	}
	
	/**
	 * Find the specified (fully-qualified) type name in the specified java project.
	 */
	protected static IType findType(IJavaProject javaProject, String mainTypeName) throws JavaModelException {
		String pathStr= mainTypeName.replace('.', '/') + ".java"; //$NON-NLS-1$
		IJavaElement javaElement= javaProject.findElement(new Path(pathStr));
		if (javaElement == null) {
			// try to find it as inner type
			String qualifier= Signature.getQualifier(mainTypeName);
			if (qualifier.length() > 0) {
				IType type= findType(javaProject, qualifier); // recursive!
				if (type != null) {
					IType res= type.getType(Signature.getSimpleName(mainTypeName));
					if (res.exists()) {
						return res;
					}
				}
			}
		} else if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			String simpleName= Signature.getSimpleName(mainTypeName);
			return ((ICompilationUnit) javaElement).getType(simpleName);
		} else if (javaElement.getElementType() == IJavaElement.CLASS_FILE) {
			return ((IClassFile) javaElement).getType();
		}
		return null; 
	}
		
	/**
	 * Throws a core exception with the given message and optional
	 * exception. The exception's status code will indicate an error.
	 * 
	 * @param message error message
	 * @param exception cause of the error, or <code>null</code>
	 * @exception CoreException with the given message and underlying
	 *  exception
	 */
	protected static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
		  code, message, exception));
	}
	
	/**
	 * Convenience method to get the <code>ILaunchConfigurationType</code> this helper is concerned with.
	 */
	private static ILaunchConfigurationType getConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType("org.eclipse.jdt.debug.ui.localJavaApplication");	//$NON-NLS-1$	
	}
	
	/**
	 * Convenience method to get the launch mgr.
	 */
	private static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Convenience method to get the java model.
	 */
	private static IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	/**
	 * Convenience method to get the workspace root.
	 */
	private static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
}
