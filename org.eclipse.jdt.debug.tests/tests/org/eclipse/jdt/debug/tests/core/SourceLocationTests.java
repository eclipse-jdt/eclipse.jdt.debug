package org.eclipse.jdt.debug.tests.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.DirectorySourceLocation;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaProjectSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

/**
 * Tests source location creation/restoration.
 */
public class SourceLocationTests extends AbstractDebugTest {
	
	public SourceLocationTests(String name) {
		super(name);
	}

	public void testProjectLocationMemento() throws Exception {
		IJavaSourceLocation location = new JavaProjectSourceLocation(getJavaProject());
		String memento = location.getMemento();
		IJavaSourceLocation restored = new JavaProjectSourceLocation();
		restored.initializeFrom(memento);
		assertEquals("project locations should be equal", location, restored);
	}
	
	public void testDirectoryLocationMemento() throws Exception {
		File dir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		IJavaSourceLocation location = new DirectorySourceLocation(dir);
		String memento = location.getMemento();
		IJavaSourceLocation restored = new DirectorySourceLocation();
		restored.initializeFrom(memento);
		assertEquals("directory locations should be equal", location, restored);
	}	
	
	public void testArchiveLocationMemento() throws Exception {
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		IJavaSourceLocation location = new ArchiveSourceLocation(JavaRuntime.getLibraryLocations(vm)[0].getSystemLibraryPath().toOSString(), null);
		String memento = location.getMemento();
		IJavaSourceLocation restored = new ArchiveSourceLocation();
		restored.initializeFrom(memento);
		assertEquals("archive locations should be equal", location, restored);
	}	
	
	public void testJavaSourceLocatorMemento() throws Exception {
		IJavaSourceLocation location1 = new JavaProjectSourceLocation(getJavaProject());
		File dir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		IJavaSourceLocation location2 = new DirectorySourceLocation(dir);
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		IJavaSourceLocation location3 = new ArchiveSourceLocation(JavaRuntime.getLibraryLocations(vm)[0].getSystemLibraryPath().toOSString(), null);
		
		JavaSourceLocator locator = new JavaSourceLocator(new IJavaSourceLocation[] {location1, location2, location3});
		String memento = locator.getMemento();
		JavaSourceLocator restored = new JavaSourceLocator();
		restored.initiatlizeFromMemento(memento);
		IJavaSourceLocation[] locations = restored.getSourceLocations();
		
		assertEquals("wrong number of source locations", 3, locations.length);
		assertEquals("1st locations not equal", location1, locations[0]);
		assertEquals("2nd locations not equal", location2, locations[1]);
		assertEquals("3rd locations not equal", location3, locations[2]);
	}
}
