/*******************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.sourcelookup;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.DirectorySourceLocation;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaProjectSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;
import org.eclipse.jdt.launching.sourcelookup.PackageFragmentRootSourceLocation;

/**
 * Tests source location creation/restoration.
 */
@SuppressWarnings("deprecation")
public class SourceLocationTests extends AbstractDebugTest {

	public static final String JRE_CONTAINER_1_4_CPE_NAME = "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/J2SE-1.4";
	public static final String JRE_CONTAINER_1_7_CPE_NAME = "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.7";

	public SourceLocationTests(String name) {
		super(name);
	}

	public void testProjectLocationMemento() throws Exception {
		IJavaSourceLocation location = new JavaProjectSourceLocation(get14Project());
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
		IJavaSourceLocation location1 = new JavaProjectSourceLocation(get14Project());
		File dir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		IJavaSourceLocation location2 = new DirectorySourceLocation(dir);
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		IJavaSourceLocation location3 = new ArchiveSourceLocation(JavaRuntime.getLibraryLocations(vm)[0].getSystemLibraryPath().toOSString(), null);

		JavaSourceLocator locator = new JavaSourceLocator(new IJavaSourceLocation[] {location1, location2, location3});
		String memento = locator.getMemento();
		JavaSourceLocator restored = new JavaSourceLocator();
		restored.initializeFromMemento(memento);
		IJavaSourceLocation[] locations = restored.getSourceLocations();

		assertEquals("wrong number of source locations", 3, locations.length);
		assertEquals("1st locations not equal", location1, locations[0]);
		assertEquals("2nd locations not equal", location2, locations[1]);
		assertEquals("3rd locations not equal", location3, locations[2]);
	}

	public void testJavaUISourceLocatorMemento() throws Exception {
		IJavaSourceLocation location1 = new JavaProjectSourceLocation(get14Project());
		File dir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		IJavaSourceLocation location2 = new DirectorySourceLocation(dir);
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		IJavaSourceLocation location3 = new ArchiveSourceLocation(JavaRuntime.getLibraryLocations(vm)[0].getSystemLibraryPath().toOSString(), null);

		JavaUISourceLocator locator = new JavaUISourceLocator(get14Project());
		locator.setSourceLocations(new IJavaSourceLocation[] {location1, location2, location3});
		locator.setFindAllSourceElement(true);

		String memento = locator.getMemento();
		JavaUISourceLocator restored = new JavaUISourceLocator();
		restored.initializeFromMemento(memento);
		IJavaSourceLocation[] locations = restored.getSourceLocations();

		assertEquals("wrong number of source locations", 3, locations.length);
		assertEquals("1st locations not equal", location1, locations[0]);
		assertEquals("2nd locations not equal", location2, locations[1]);
		assertEquals("3rd locations not equal", location3, locations[2]);
		assertTrue("Should find all source locations", locator.isFindAllSourceElements());
	}

	public void testPackageFragmentRootLocationMemento() throws Exception {
		IResource res = get14Project().getProject().getFolder("src");
		IPackageFragmentRoot root = get14Project().getPackageFragmentRoot(res);
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);
		String memento = location.getMemento();
		IJavaSourceLocation restored = new PackageFragmentRootSourceLocation();
		restored.initializeFrom(memento);
		assertEquals("root locations should be equal", location, restored);
	}

	public void testEmptyPackageFragmentRootLocationMemento() throws Exception {
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation();
		String memento = location.getMemento();
		IJavaSourceLocation restored = new PackageFragmentRootSourceLocation();
		restored.initializeFrom(memento);
		assertEquals("root locations should be equal", location, restored);
	}

	public void testPositiveSourceFolderSourceLocation() throws Exception {
		IResource res = get14Project().getProject().getFolder("src");
		IPackageFragmentRoot root = get14Project().getPackageFragmentRoot(res);
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);

		Object source = location.findSourceElement("Breakpoints");
		assertTrue("Did not find source for 'Breakpoints'", source instanceof ICompilationUnit);
		ICompilationUnit cu = (ICompilationUnit)source;
		assertEquals("Did not find source for 'Breakpoints'", cu.getElementName(), "Breakpoints.java");

		source = location.findSourceElement("org.eclipse.debug.tests.targets.InfiniteLoop");
		assertTrue("Did not find source for 'InfiniteLoop'", source instanceof ICompilationUnit);
		cu = (ICompilationUnit)source;
		assertEquals("Did not find source for 'Breakpoints'", cu.getElementName(), "InfiniteLoop.java");
	}

	public void testNegativeSourceFolderSourceLocation() throws Exception {
		IResource res = get14Project().getProject().getFolder("src");
		IPackageFragmentRoot root = get14Project().getPackageFragmentRoot(res);
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);

		Object source = location.findSourceElement("DoesNotExist");
		assertNull("Should not have found source", source);

		source = location.findSourceElement("org.eclipse.DoesNotExist");
		assertNull("Should not have found source", source);
	}

	public void testPositiveSystemLibrarySourceLocation() throws Exception {
		IClasspathEntry[] cpes = get14Project().getRawClasspath();
		IClasspathEntry lib = null;
		for (int i = 0; i < cpes.length; i++) {
			if (cpes[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				if (cpes[i].getPath().equals(new Path(JRE_CONTAINER_1_7_CPE_NAME))) {
					lib = cpes[i];
					break;
				}
			}
		}
		assertNotNull("Could not find JRE_CONTAINER entry", lib);

		IPackageFragmentRoot[] roots = get14Project().findPackageFragmentRoots(lib);
		Object source = null;
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);
			source = location.findSourceElement("java.lang.Object");
			if (source != null) {
				break;
			}
		}

		assertTrue("Did not find source for 'Object'", source instanceof IClassFile);
		IClassFile cf = (IClassFile)source;
		assertEquals("Did not find source for 'Object'", "Object.class", cf.getElementName());

		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);
			source = location.findSourceElement("java.util.Vector$1");
			if (source != null) {
				break;
			}
		}
		assertTrue("Did not find source for 'Vector$1'", source instanceof IClassFile);
		cf = (IClassFile)source;
		assertEquals("Did not find source for 'Vector$1'", "Vector$1.class", cf.getElementName());
	}

	public void testNegativeSystemLibrarySourceLocation() throws Exception {
		IClasspathEntry[] cpes = get14Project().getRawClasspath();
		IClasspathEntry lib = null;
		for (int i = 0; i < cpes.length; i++) {
			if (cpes[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				if (cpes[i].getPath().equals(new Path(JRE_CONTAINER_1_7_CPE_NAME))) {
					lib = cpes[i];
					break;
				}
			}
		}
		assertNotNull("Could not find JRE_CONTAINER entry", lib);

		IPackageFragmentRoot[] roots = get14Project().findPackageFragmentRoots(lib);
		Object source = null;
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);
			source = location.findSourceElement("xyz.abc.Object");
			if (source != null) {
				break;
			}
		}

		assertNull("Should not find source", source);

	}
}
