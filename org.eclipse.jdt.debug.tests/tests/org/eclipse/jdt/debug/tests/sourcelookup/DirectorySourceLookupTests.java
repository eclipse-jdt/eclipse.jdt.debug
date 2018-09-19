/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.sourcelookup;

import java.io.File;

import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.sourcelookup.DirectorySourceLocation;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.LocalFileStorage;

/**
 * Tests source lookup in directories
 */
@SuppressWarnings("deprecation")
public class DirectorySourceLookupTests extends AbstractDebugTest {

	public DirectorySourceLookupTests(String name) {
		super(name);
	}

	/**
	 * Tests source lookup in a top level type, in the default package.
	 */
	public void testDefTopLevelType() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		File rootFile = root.getResource().getLocation().toFile();
		IJavaSourceLocation location = new DirectorySourceLocation(rootFile);

		ICompilationUnit cu = getCompilationUnit(get14Project(), "src", IInternalDebugCoreConstants.EMPTY_STRING, "Breakpoints.java");
		assertTrue("did not find compilation unit for Breakpoints.java", cu.exists());
		LocalFileStorage expectedSource = new LocalFileStorage(cu.getResource().getLocation().toFile());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("Breakpoints"));
	}

	/**
	 * Tests source lookup in an inner type, the default package.
	 */
	public void testDefInnerType() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		File rootFile = root.getResource().getLocation().toFile();
		IJavaSourceLocation location = new DirectorySourceLocation(rootFile);

		ICompilationUnit cu = getCompilationUnit(get14Project(), "src", IInternalDebugCoreConstants.EMPTY_STRING, "Breakpoints.java");
		assertTrue("did not find compilation unit for Breakpoints.java", cu.exists());
		LocalFileStorage expectedSource = new LocalFileStorage(cu.getResource().getLocation().toFile());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("Breakpoints$InnerRunnable"));
	}


	/**
	 * Tests source lookup in a top level type.
	 */
	public void testTopLevelType() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		File rootFile = root.getResource().getLocation().toFile();
		IJavaSourceLocation location = new DirectorySourceLocation(rootFile);

		ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "SourceLookup.java");
		assertTrue("did not find compilation unit for SourceLookup.java", cu.exists());
		LocalFileStorage expectedSource = new LocalFileStorage(cu.getResource().getLocation().toFile());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.SourceLookup"));
	}

	/**
	 * Tests source lookup in an inner type.
	 */
	public void testInnerType() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		File rootFile = root.getResource().getLocation().toFile();
		IJavaSourceLocation location = new DirectorySourceLocation(rootFile);

		ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "SourceLookup.java");
		assertTrue("did not find compilation unit for SourceLookup.java", cu.exists());
		LocalFileStorage expectedSource = new LocalFileStorage(cu.getResource().getLocation().toFile());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.SourceLookup$Inner"));
	}

	/**
	 * Tests source lookup in an inner, inner type.
	 */
	public void testNestedType() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		File rootFile = root.getResource().getLocation().toFile();
		IJavaSourceLocation location = new DirectorySourceLocation(rootFile);

		ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "SourceLookup.java");
		assertTrue("did not find compilation unit for SourceLookup.java", cu.exists());
		LocalFileStorage expectedSource = new LocalFileStorage(cu.getResource().getLocation().toFile());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.SourceLookup$Inner$Nested"));
	}

	/**
	 * Tests source lookup in a top level type, with a $ named class
	 */
	public void testTopLevel$Type() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		File rootFile = root.getResource().getLocation().toFile();
		IJavaSourceLocation location = new DirectorySourceLocation(rootFile);

		ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "Source_$_Lookup.java");
		assertTrue("did not find compilation unit for Source_$_Lookup.java", cu.exists());
		LocalFileStorage expectedSource = new LocalFileStorage(cu.getResource().getLocation().toFile());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.Source_$_Lookup"));
	}

	/**
	 * Tests source lookup in an inner type in a $ named class.
	 */
	public void testInner$Type() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		File rootFile = root.getResource().getLocation().toFile();
		IJavaSourceLocation location = new DirectorySourceLocation(rootFile);

		ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "Source_$_Lookup.java");
		assertTrue("did not find compilation unit for Source_$_Lookup.java", cu.exists());
		LocalFileStorage expectedSource = new LocalFileStorage(cu.getResource().getLocation().toFile());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.Source_$_Lookup$Inner"));
	}

	/**
	 * Tests source lookup in an inner type in a $ named class.
	 */
	public void testInnerNested$Type() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		File rootFile = root.getResource().getLocation().toFile();
		IJavaSourceLocation location = new DirectorySourceLocation(rootFile);

		ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "Source_$_Lookup.java");
		assertTrue("did not find compilation unit for Source_$_Lookup.java", cu.exists());
		LocalFileStorage expectedSource = new LocalFileStorage(cu.getResource().getLocation().toFile());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.Source_$_Lookup$Inner$Nested"));
	}
}
