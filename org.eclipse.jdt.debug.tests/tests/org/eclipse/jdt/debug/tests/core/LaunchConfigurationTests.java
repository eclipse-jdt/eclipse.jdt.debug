package org.eclipse.jdt.debug.tests.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;


 
/**
 * Tests for launch configurations
 */
public class LaunchConfigurationTests extends AbstractDebugTest {
	
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
	 * Creates a local working copy configuration, sets some attributes,
	 * and saves the working copy, and retrieves the attributes.
	 * Copy the configuration and ensure the original still exists.
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
		 ILaunchConfiguration newHandle = wc.doSave();
		 assertTrue("Config should no longer exist", !handle.exists());
		 
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
		 ILaunchConfiguration newHandle = wc.doSave();
		 assertTrue("Config should no longer exist", !handle.exists());
		 
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
		
		
}

