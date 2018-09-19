/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
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

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.ZipEntryStorage;

/**
 * Tests source lookup in archives
 */
@SuppressWarnings("deprecation")
public class ArchiveSourceLookupTests extends AbstractDebugTest {

	public ArchiveSourceLookupTests(String name) {
		super(name);
	}

	/**
	 * Tests source lookup in a top level type, in the default package.
	 */
	public void testDefTopLevelType() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/testJarWithOutRoot.jar"));
		ArchiveSourceLocation location = new ArchiveSourceLocation(file.getAbsolutePath(), null);

		ZipEntryStorage storage = (ZipEntryStorage) location.findSourceElement("Breakpoints");
		assertEquals("Did not find source", "Breakpoints.java", storage.getName());
	}

	/**
	 * Tests source lookup in an inner type, the default package.
	 */
	public void testDefInnerType() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/testJarWithOutRoot.jar"));
		ArchiveSourceLocation location = new ArchiveSourceLocation(file.getAbsolutePath(), null);

		ZipEntryStorage storage = (ZipEntryStorage) location.findSourceElement("Breakpoints$InnerRunnable");
		assertEquals("Did not find source", "Breakpoints.java", storage.getName());
	}


	/**
	 * Tests source lookup in a top level type.
	 */
	public void testTopLevelType() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/testJarWithOutRoot.jar"));
		ArchiveSourceLocation location = new ArchiveSourceLocation(file.getAbsolutePath(), null);

		ZipEntryStorage storage = (ZipEntryStorage) location.findSourceElement("org.eclipse.debug.tests.targets.SourceLookup");
		assertEquals("Did not find source", "SourceLookup.java", storage.getName());
	}

	/**
	 * Tests source lookup in an inner type.
	 */
	public void testInnerType() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/testJarWithOutRoot.jar"));
		ArchiveSourceLocation location = new ArchiveSourceLocation(file.getAbsolutePath(), null);

		ZipEntryStorage storage = (ZipEntryStorage) location.findSourceElement("org.eclipse.debug.tests.targets.SourceLookup$Inner");
		assertEquals("Did not find source", "SourceLookup.java", storage.getName());
	}

	/**
	 * Tests source lookup in an inner, inner type.
	 */
	public void testNestedType() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/testJarWithOutRoot.jar"));
		ArchiveSourceLocation location = new ArchiveSourceLocation(file.getAbsolutePath(), null);

		ZipEntryStorage storage = (ZipEntryStorage) location.findSourceElement("org.eclipse.debug.tests.targets.SourceLookup$Inner$Nested");
		assertEquals("Did not find source", "SourceLookup.java", storage.getName());
	}

	/**
	 * Tests source lookup in a top level type, with a $ named class
	 */
	public void testTopLevel$Type() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/testJarWithOutRoot.jar"));
		ArchiveSourceLocation location = new ArchiveSourceLocation(file.getAbsolutePath(), null);

		ZipEntryStorage storage = (ZipEntryStorage) location.findSourceElement("org.eclipse.debug.tests.targets.Source_$_Lookup");
		assertEquals("Did not find source", "Source_$_Lookup.java", storage.getName());
	}

	/**
	 * Tests source lookup in an inner type in a $ named class.
	 */
	public void testInner$Type() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/testJarWithOutRoot.jar"));
		ArchiveSourceLocation location = new ArchiveSourceLocation(file.getAbsolutePath(), null);

		ZipEntryStorage storage = (ZipEntryStorage) location.findSourceElement("org.eclipse.debug.tests.targets.Source_$_Lookup$Inner");
		assertEquals("Did not find source", "Source_$_Lookup.java", storage.getName());
	}

	/**
	 * Tests source lookup in an inner type in a $ named class.
	 */
	public void testInnerNested$Type() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/testJarWithOutRoot.jar"));
		ArchiveSourceLocation location = new ArchiveSourceLocation(file.getAbsolutePath(), null);

		ZipEntryStorage storage = (ZipEntryStorage) location.findSourceElement("org.eclipse.debug.tests.targets.Source_$_Lookup$Inner$Nested");
		assertEquals("Did not find source", "Source_$_Lookup.java", storage.getName());
	}
}
