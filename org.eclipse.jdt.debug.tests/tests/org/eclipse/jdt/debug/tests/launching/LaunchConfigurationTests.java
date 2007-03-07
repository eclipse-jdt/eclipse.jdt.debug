/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Tests for launch configurations
 */
public class LaunchConfigurationTests extends AbstractDebugTest implements ILaunchConfigurationListener {
	
	/**
	 * The from/to handles during rename operations
	 */
	protected ILaunchConfiguration fFrom;
	protected ILaunchConfiguration fTo;
	
	protected Object fLock = new Object();
	protected ILaunchConfiguration fAdded;
	protected ILaunchConfiguration fRemoved;
	
	/**
	 * Constructor
	 * @param name
	 */
	public LaunchConfigurationTests(String name) {
		super(name);
	}
	
	/** 
	 * Creates and returns a new launch config the given name, local
	 * or shared, with 4 attributes:
	 *  - String1 = "String1"
	 *  - Int1 = 1
	 *  - Boolean1 = true
	 *  - Boolean2 = faslse
	 */
	protected ILaunchConfigurationWorkingCopy newConfiguration(IContainer container, String name) throws CoreException {
		 ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		 assertTrue("Should support debug mode", type.supportsMode(ILaunchManager.DEBUG_MODE));
		 assertTrue("Should support run mode", type.supportsMode(ILaunchManager.RUN_MODE));
		 ILaunchConfigurationWorkingCopy wc = type.newInstance(container, name);
		 wc.setAttribute("String1", "String1");
		 wc.setAttribute("Int1", 1);
		 wc.setAttribute("Boolean1", true);
		 wc.setAttribute("Boolean2", false);
		 assertTrue("Should need saving", wc.isDirty());
		 return wc;
	}
		
	/**
	 * Returns whether the given handle is contained in the specified
	 * array of handles.
	 */
	protected boolean existsIn(ILaunchConfiguration[] configs, ILaunchConfiguration config) {
		for (int i = 0; i < configs.length; i++) {
			if (configs[i].equals(config)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a local working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes.
	 * 
	 * @throws CoreException
	 */
	public void testCreateLocalConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config1");
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists());
		 
		 // retrieve attributes
		 assertEquals("String1 should be String1", handle.getAttribute("String1", "Missing"), "String1");
		 assertEquals("Int1 should be 1", handle.getAttribute("Int1", 0), 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));
		 
		 // ensure new handle is the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle));
		 
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists());
	}
	
	/**
	 * Ensures that a launch configuration returns a complete attribute map
	 * @throws CoreException
	 */
	public void testGetAttributes() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config1");
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists());

		 Map attributes = handle.getAttributes();
		 // retrieve attributes
		 assertEquals("String1 should be String1", "String1", attributes.get("String1"));
		 assertEquals("Int1 should be 1", new Integer(1), attributes.get("Int1"));
		 assertEquals("Boolean1 should be true", Boolean.valueOf(true), attributes.get("Boolean1"));
		 assertEquals("Boolean2 should be false", Boolean.valueOf(false), attributes.get("Boolean2"));

		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists());
	}	
	
	/**
	 * Ensures that set attributes works
	 * @throws CoreException
	 */
	public void testSetAttributes() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config1");
		 Map map = new HashMap();
		 map.put("ATTR1", "ONE");
		 map.put("ATTR2", "TWO");
		 wc.setAttributes(map);
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists());

		 Map attributes = handle.getAttributes();
		 assertEquals("should have two attributes", 2, attributes.size());
		 // retrieve attributes
		 assertEquals("ATTR1 should be ONE", "ONE", attributes.get("ATTR1"));
		 assertEquals("ATTR2 should be TWO", "TWO", attributes.get("ATTR2"));

		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists());
	}	
	
	/**
	 * Ensures that set attributes to <code>null</code> works
	 * @throws CoreException
	 */
	public void testSetNullAttributes() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config1");
		 wc.setAttributes(null);
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists());

		 Map attributes = handle.getAttributes();
		 assertEquals("should have no attributes", 0, attributes.size());
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists());
	}
		
	/**
	 * Creates a local working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes.
	 * Copy the configuration and ensure the original still exists.
	 * @throws CoreException
	 */
	public void testLocalCopy() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "configToCopy");
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists());
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));
		 
		 // ensure new handle is the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle));
		 
		 ILaunchConfigurationWorkingCopy softCopy = handle.copy("CopyOf" + handle.getName());
		 assertNull("Original in copy should be null", softCopy.getOriginal());
		 ILaunchConfiguration hardCopy = softCopy.doSave();

		 // retrieve attributes
		 assertTrue("String1 should be String1", hardCopy.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", hardCopy.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", hardCopy.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !hardCopy.getAttribute("Boolean2", true));		 
		 
		 assertTrue("Original should still exist", handle.exists());
		 
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists());
		 hardCopy.delete();
		 assertTrue("Config should not exist after deletion", !hardCopy.exists());		 		 
	}
		
	/**
	 * Create a config and save it tiwce, ensuring it only
	 * ends up in the index once.
	 * @throws CoreException
	 */
	public void testDoubleSave() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "configDoubleSave");
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists());
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));
		 
		 // ensure new handle is the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle));
		 
		String name = wc.getName();
		wc.rename("newName");
		wc.rename(name);
		assertTrue("Should be dirty", wc.isDirty());
		wc.doSave();
		
		ILaunchConfiguration[] newConfigs = getLaunchManager().getLaunchConfigurations();
		assertTrue("Should be the same number of configs", newConfigs.length == configs.length);
		
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists());
		
	}
		
	/**
	 * Creates a local working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes. Deletes
	 * the configuration and ensures it no longer exists.
	 * @throws CoreException
	 */
	public void testDeleteLocalConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config2delete");
		 ILaunchConfiguration handle = wc.doSave();
		 File file = wc.getLocation().toFile();
		 assertTrue("Configuration file should exist", file.exists());
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));
		 
		 // delete 
		 handle.delete();		 
		 assertTrue("Config should no longer exist", !handle.exists());
		 
		 // ensure handle is not in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should not exist in project index", !existsIn(configs, handle));		 
	}	
	
	/**
	 * Creates a local working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes. Renames
	 * the configuration and ensures it's old config no longer exists,
	 * and that attributes are retrievable from the new (renamed) config.
	 * @throws CoreException
	 */
	public void testRenameLocalConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config2rename");
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists());
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));
		 
		 // rename
		 wc = handle.getWorkingCopy();
		 wc.rename("config-2-rename");
		 addConfigListener();
		 ILaunchConfiguration newHandle = wc.doSave();
		 removeConfigListener();
		 assertTrue("Config should no longer exist", !handle.exists());
		 assertEquals("From should be original", handle, fFrom);
		 assertEquals("To should be new handle", newHandle, fTo);
		 
		 // retrieve new attributes
		 assertTrue("String1 should be String1", newHandle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", newHandle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", newHandle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !newHandle.getAttribute("Boolean2", true));		 

		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Renamed configuration should exist in project index", existsIn(configs, newHandle));		 
		 assertTrue("Original configuration should NOT exist in project index", !existsIn(configs, handle));	
		 
		 // cleanup
		 newHandle.delete();
		 assertTrue("Config should not exist after deletion", !newHandle.exists());		 	 
	}	
	
	/**
	 * Moves a local configuration to a shared location
	 * @throws CoreException
	 */
	public void testMoveLocalToSharedConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config2share");
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists());

		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));

		 // move
		 wc = handle.getWorkingCopy();
		 wc.setContainer(getJavaProject().getProject());
		 addConfigListener();
		 ILaunchConfiguration newHandle = wc.doSave();
		 removeConfigListener();
		 assertTrue("Config should no longer exist", !handle.exists());
		 assertEquals("From should be original", handle, fFrom);
		 assertEquals("To should be new handle", newHandle, fTo);

		 // retrieve new attributes
		 assertTrue("String1 should be String1", newHandle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", newHandle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", newHandle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !newHandle.getAttribute("Boolean2", true));

		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Renamed configuration should exist in project index", existsIn(configs, newHandle));
		 assertTrue("Original configuration should NOT exist in project index", !existsIn(configs, handle));

		 // cleanup
		 newHandle.delete();
		 assertTrue("Config should not exist after deletion", !newHandle.exists());
	}	
	
	/**
	 * Moves a local configuration to a shared location
	 * @throws CoreException
	 */
	public void testMoveSharedToLocalConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getJavaProject().getProject(), "config2local");
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists());

		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));

		 // move
		 wc = handle.getWorkingCopy();
		 wc.setContainer(null);
		 addConfigListener();
		 ILaunchConfiguration newHandle = wc.doSave();
		 removeConfigListener();
		 assertTrue("Config should no longer exist", !handle.exists());
		 assertEquals("From should be original", handle, fFrom);
		 assertEquals("To should be new handle", newHandle, fTo);

		 // retrieve new attributes
		 assertTrue("String1 should be String1", newHandle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", newHandle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", newHandle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !newHandle.getAttribute("Boolean2", true));

		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Renamed configuration should exist in project index", existsIn(configs, newHandle));
		 assertTrue("Original configuration should NOT exist in project index", !existsIn(configs, handle));

		 // cleanup
		 newHandle.delete();
		 assertTrue("Config should not exist after deletion", !newHandle.exists());
	}		
	
	/**
	 * Creates a shared working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes.
	 * @throws CoreException
	 */
	public void testCreateSharedConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getJavaProject().getProject(), "config2");
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists());
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));
		 
 		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle)); 
		 
 		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists());
	}	
	
	/**
	 * Creates a shared working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes.
	 * Copies the configuration and ensures the original still exists.
	 * @throws CoreException
	 */
	public void testSharedCopy() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getJavaProject().getProject(), "config2Copy");
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists());
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));
		 
 		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle)); 
		 
		 // copy 
		 ILaunchConfigurationWorkingCopy softCopy = handle.copy("CopyOf" + handle.getName());
		 ILaunchConfiguration hardCopy = softCopy.doSave();
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", hardCopy.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", hardCopy.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", hardCopy.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !hardCopy.getAttribute("Boolean2", true));		 
		 
		 assertTrue("Original should still exist", handle.exists());
		 
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists());
		 hardCopy.delete();
		 assertTrue("Config should not exist after deletion", !hardCopy.exists());		 		 		 
	}		
	

	/**
	 * Creates a shared working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes. Deletes
	 * the configuration and ensures it no longer exists.
	 * @throws CoreException
	 */
	public void testDeleteSharedConfiguration() throws CoreException {
 		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getJavaProject().getProject(), "shared2delete");
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists());
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));
		 
		 // delete 
		 handle.delete();		 
		 assertTrue("Config should no longer exist", !handle.exists());
		 
		 // ensure handle is not in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should not exist in project index", !existsIn(configs, handle));		 
	}	
	
	/**
	 * Creates a shared working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes. Renames
	 * the configuration and ensures it's old config no longer exists,
	 * and that attributes are retrievable from the new (renamed) config.
	 * @throws CoreException
	 */
	public void testRenameSharedConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(getJavaProject().getProject(), "shared2rename");
		 ILaunchConfiguration handle = wc.doSave();
		 assertTrue("Configuration should exist", handle.exists());
		 
		 // retrieve attributes
		 assertTrue("String1 should be String1", handle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", handle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));
		 
		 // rename
		 wc = handle.getWorkingCopy();
		 wc.rename("shared-2-rename");
		 addConfigListener();
		 ILaunchConfiguration newHandle = wc.doSave();
		 removeConfigListener();
		 assertTrue("Config should no longer exist", !handle.exists());
		 assertEquals("From should be original", handle, fFrom);
		 assertEquals("To should be new handle", newHandle, fTo);		 
		 
		 // retrieve new attributes
		 assertTrue("String1 should be String1", newHandle.getAttribute("String1", "Missing").equals("String1"));
		 assertTrue("Int1 should be 1", newHandle.getAttribute("Int1", 0) == 1);
		 assertTrue("Boolean1 should be true", newHandle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !newHandle.getAttribute("Boolean2", true));		 

		 // ensure new handle is in the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Renamed configuration should exist in project index", existsIn(configs, newHandle));		 
		 assertTrue("Original configuration should NOT exist in project index", !existsIn(configs, handle));		 
		 
		 // cleanup
		 newHandle.delete();
		 assertTrue("Config should not exist after deletion", !newHandle.exists());		 
	}
	
	/** 
	 * Creates a few configs, closes the project and re-opens the
	 * project to ensure the config index is persisted properly
	 * @throws CoreException
	 */
	public void testPersistIndex() throws CoreException {
		ILaunchConfigurationWorkingCopy wc1 = newConfiguration(null, "persist1local");
		ILaunchConfigurationWorkingCopy wc2 = newConfiguration(getJavaProject().getProject(), "persist2shared");
		ILaunchConfiguration lc1 = wc1.doSave();
		ILaunchConfiguration lc2 = wc2.doSave();
		
		IProject project = getJavaProject().getProject();
		ILaunchConfiguration[] before = getLaunchManager().getLaunchConfigurations();
		assertTrue("config should be in index", existsIn(before, lc1));
		assertTrue("config should be in index", existsIn(before, lc2));
		
		project.close(null);
		ILaunchConfiguration[] during = getLaunchManager().getLaunchConfigurations();
		boolean local = true;
		for (int i = 0; i < during.length; i++) {
			local = local && during[i].isLocal();
		}
		assertTrue("Should only be local configs when closed", local);
		
		project.open(null);
		ILaunchConfiguration[] after = getLaunchManager().getLaunchConfigurations();
		assertTrue("Should be same number of configs after openning", after.length == before.length);
		for (int i = 0; i < before.length; i++) {
			assertTrue("Config should exist after openning", existsIn(after, before[i]));
		}

		 // cleanup
		 lc1.delete();
		 assertTrue("Config should not exist after deletion", !lc1.exists());
		 lc2.delete();
		 assertTrue("Config should not exist after deletion", !lc2.exists());		 
		 
		
	}	
		
		
	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		fFrom = getLaunchManager().getMovedFrom(configuration);
		synchronized (fLock) {
		    fAdded = configuration;
		    fLock.notifyAll();
        }
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		fTo = getLaunchManager().getMovedTo(configuration);
		synchronized (fLock) {
		    fRemoved = configuration;
		    fLock.notifyAll();
        }
	}

	protected void addConfigListener() {
		getLaunchManager().addLaunchConfigurationListener(this);
	}
	
	protected void removeConfigListener() {
		getLaunchManager().removeLaunchConfigurationListener(this);
	}
	
	/**
	 * Ensures that a removal notification is sent for a shared config in a project
	 * that is deleted.
	 *  
	 * @throws Exception
	 */
	public void testDeleteProjectWithSharedConfig() throws Exception {
	   IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("DeleteSharedConfig");
	   try {
		   assertFalse("project should not exist yet", project.exists());
		   project.create(null);
		   assertTrue("project should now exist", project.exists());
		   project.open(null);
		   assertTrue("project should be open", project.isOpen());
		   ILaunchConfigurationWorkingCopy wc = newConfiguration(project, "ToBeDeleted");
		   
		   addConfigListener();
		   ILaunchConfiguration configuration = wc.doSave();
		   assertEquals(configuration, fAdded);
		   
		   synchronized (fLock) {
		       fRemoved = null;
		       project.delete(true, false, null);
		       if (fRemoved == null) {
		           fLock.wait(10000);
		       }
		   }
		   assertEquals(configuration, fRemoved);
	   } finally {
	       if (project.exists()) {
	           project.delete(true, false, null);
	       }
	       removeConfigListener();
	   }
	}
	
	/**
	 * Tests a nested working copy.
	 * 
	 * @throws CoreException
	 */
	public void testNestedWorkingCopyLocalConfiguration() throws CoreException {
		 ILaunchConfigurationWorkingCopy wc = newConfiguration(null, "config123");
		 IPath location = wc.getLocation();
		 ILaunchConfiguration handle = wc.doSave();
		 File file = location.toFile();
		 assertTrue("Configuration file should exist", file.exists());
		 
		 // retrieve attributes
		 assertEquals("String1 should be String1", handle.getAttribute("String1", "Missing"), "String1");
		 assertEquals("Int1 should be 1", handle.getAttribute("Int1", 0), 1);
		 assertTrue("Boolean1 should be true", handle.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !handle.getAttribute("Boolean2", true));
		 
		 // ensure new handle is the index
		 ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
		 assertTrue("Configuration should exist in project index", existsIn(configs, handle));
		 
		 // get a working copy
		 wc = handle.getWorkingCopy();
		 ILaunchConfigurationWorkingCopy nested = wc.getWorkingCopy();
		 
		 // verify nested is same as original
		 assertEquals("String1 should be String1", nested.getAttribute("String1", "Missing"), "String1");
		 assertEquals("Int1 should be 1", nested.getAttribute("Int1", 0), 1);
		 assertTrue("Boolean1 should be true", nested.getAttribute("Boolean1", false));
		 assertTrue("Boolean2 should be false", !nested.getAttribute("Boolean2", true));
		 
		 // change an attribute in the nested working copy
		 nested.setAttribute("String1", "StringOne");
		 assertEquals("Wrong attribute value", nested.getAttribute("String1", "Missing"), "StringOne");
		 assertEquals("Wrong attribute value", wc.getAttribute("String1", "Missing"), "String1");
		 assertEquals("Wrong attribute value", handle.getAttribute("String1", "Missing"), "String1");
		 
		 // save back to parent
		 ILaunchConfigurationWorkingCopy parent = nested.getParent();
		 assertEquals("Wrong parent", wc, parent);
		 assertNull("Should have no parent", wc.getParent());
		 nested.doSave();
		 assertEquals("Wrong attribute value", wc.getAttribute("String1", "Missing"), "StringOne");
		 assertEquals("Wrong attribute value", handle.getAttribute("String1", "Missing"), "String1");
		 
		 // check originals
		 assertEquals("Wrong original config" , handle, wc.getOriginal());
		 assertEquals("Wrong original config" , handle, nested.getOriginal());
		 
		 // cleanup
		 handle.delete();
		 assertTrue("Config should not exist after deletion", !handle.exists());
	}	
}


