package org.eclipse.jdt.debug.tests;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Test to close the workbench, since debug tests do not run in the UI
 * thread.
 */
public class ProjectCreationDecorator extends AbstractDebugTest {
	
	public ProjectCreationDecorator(String name) {
		super(name);
	}
	
	public void testProjectCreation() throws Exception {
		// delete any pre-existing project
		IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject("DebugTests");
		if (pro.exists()) {
			pro.delete(true, true, null);
		}
		// create project and import source
		fJavaProject = JavaProjectHelper.createJavaProject("DebugTests", "bin");
		IPackageFragmentRoot src = JavaProjectHelper.addSourceContainer(fJavaProject, "src");
		File root = JavaTestPlugin.getDefault().getFileInPlugin(JavaProjectHelper.TEST_SRC_DIR);
		JavaProjectHelper.importFilesFromDirectory(root, src.getPath(), null);
		
		// add rt.jar
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		assertNotNull("No default JRE", vm);
		JavaProjectHelper.addVariableEntry(fJavaProject, new Path(JavaRuntime.JRELIB_VARIABLE), new Path(JavaRuntime.JRESRC_VARIABLE), new Path(JavaRuntime.JRESRCROOT_VARIABLE));
		
		pro = fJavaProject.getProject();
		
		//add A.jar
		root = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testjars"));
		JavaProjectHelper.importFilesFromDirectory(root, src.getPath(), null);
		IPath path = src.getPath().append("A.jar");
		JavaProjectHelper.addLibrary(fJavaProject, path);
		
		// create launch configuration folder
		
		IFolder folder = pro.getFolder("launchConfigurations");
		if (folder.exists()) {
			folder.delete(true,null);
		}
		folder.create(true, true, null);
		
		// delete any existing launch configs
		ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		for (int i = 0; i < configs.length; i++) {
			configs[i].delete();
		}
		
		// create launch configurations
		createLaunchConfiguration("Breakpoints");
		createLaunchConfiguration("InstanceVariablesTests");
		createLaunchConfiguration("LocalVariablesTests");
		createLaunchConfiguration("StaticVariablesTests");
		createLaunchConfiguration("DropTests");
		createLaunchConfiguration("ThrowsNPE");
		createLaunchConfiguration("org.eclipse.debug.tests.targets.Watchpoint");
		createLaunchConfiguration("A");
		createLaunchConfiguration("HitCountLooper");
		createLaunchConfiguration("CompileError");
				
	}
	
	/**
	 * Creates a shared lanuch configuration for the type with the given
	 * name.
	 */
	protected void createLaunchConfiguration(String mainTypeName) throws Exception {
		ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationWorkingCopy config = type.newInstance(getJavaProject().getProject().getFolder("launchConfigurations"), mainTypeName);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeName);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, getJavaProject().getElementName());
		config.setAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, IDebugUIConstants.PERSPECTIVE_NONE);		
		config.setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, IDebugUIConstants.PERSPECTIVE_NONE);		
		config.doSave();
	}
}
