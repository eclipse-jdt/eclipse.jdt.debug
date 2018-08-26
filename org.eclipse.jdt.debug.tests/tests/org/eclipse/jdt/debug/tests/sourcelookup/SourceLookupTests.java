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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.PackageFragmentRootSourceLocation;

/**
 * Tests source lookup in source folders
 */
@SuppressWarnings("deprecation")
public class SourceLookupTests extends AbstractDebugTest {

	public SourceLookupTests(String name) {
		super(name);
	}

	/**
	 * See Bug 53646
	 */
	public void testPackageFragmentRoots() throws Exception {
		IJavaProject javaProject = get14Project();
		IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
		IResource resource = javaProject.getProject().getFile("src/A.jar");
		IPackageFragmentRoot packageFragmentRoot = javaProject.getPackageFragmentRoot(resource);
		assertTrue("package fragment root should exist", packageFragmentRoot.exists());
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			if (packageFragmentRoot.equals(root)) {
				return;
			}
		}
		assertTrue("Failed to locate package fragment root", false);
	}

	/**
	 * see Bug 37545
	 */
	public void testStackFrameReuse() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.CallStack";
		createLineBreakpoint(31, "org.eclipse.debug.tests.targets.ClassOne");
		createLineBreakpoint(31, "org.eclipse.debug.tests.targets.ClassTwo");

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			// get source element for first breakpoint
			IStackFrame[] frames = thread.getStackFrames();
			IStackFrame frame = frames[2];
			ISourceLocator sourceLocator = thread.getLaunch().getSourceLocator();
			Object source1 = sourceLocator.getSourceElement(frame);
			source1 = ((IAdaptable)source1).getAdapter(IJavaElement.class);

			IPackageFragment[] fragments = get14Project().getPackageFragments();
			IPackageFragment fragment = null;
			for (int i = 0; i < fragments.length; i++) {
				if (fragments[i].getElementName().equals("org.eclipse.debug.tests.targets")) {
					fragment = fragments[i];
					break;
				}
			}
			assertNotNull("Did not locate package framgment 'org.eclipse.debug.tests.targets'", fragment);
			ICompilationUnit unit1 = fragment.getCompilationUnit("ClassOne.java");
			assertEquals("Source lookup failed for frame1", unit1, source1);

			// resume to second breakpoint
			thread = resume(thread);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IStackFrame frame2 = thread.getStackFrames()[2];
			Object source2 = sourceLocator.getSourceElement(frame2);
			source2 = ((IAdaptable)source2).getAdapter(IJavaElement.class);
			ICompilationUnit unit2 = fragment.getCompilationUnit("ClassTwo.java");
			assertEquals("Source lookup failed for frame2", unit2, source2);

			// the source elements should not be equal
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests source lookup in a top level type, in the default package.
	 */
	public void testDefTopLevelType() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);

		ICompilationUnit expectedSource = getCompilationUnit(get14Project(), "src", IInternalDebugCoreConstants.EMPTY_STRING, "Breakpoints.java");
		assertTrue("did not find compilation unit for Breakpoints.java", expectedSource.exists());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("Breakpoints"));
	}

	/**
	 * Tests source lookup in an inner type, the default package.
	 */
	public void testDefInnerType() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);

		ICompilationUnit expectedSource = getCompilationUnit(get14Project(), "src", IInternalDebugCoreConstants.EMPTY_STRING, "Breakpoints.java");
		assertTrue("did not find compilation unit for Breakpoints.java", expectedSource.exists());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("Breakpoints$InnerRunnable"));
	}

	/**
	 * Tests source lookup in an anonymous inner type, in the default package.
	 * Must debug since we do not know name of type.
	 */
	public void testDefAnonInnerType() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(43, typeName);
		ICompilationUnit expectedSource = getCompilationUnit(get14Project(), "src", IInternalDebugCoreConstants.EMPTY_STRING, "Breakpoints.java");
		assertTrue("did not find compilation unit for Breakpoints.java", expectedSource.exists());

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			// get source element for top frame
			IStackFrame frame = thread.getTopStackFrame();
			ISourceLocator sourceLocator = thread.getLaunch().getSourceLocator();
			Object source = sourceLocator.getSourceElement(frame);
			assertEquals("Source lookup failed", expectedSource.getResource(), source);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests source lookup in a named local type, in the default package.
	 * Must debug to test this, since we do no know name of local type.
	 */
	public void testDefLocalType() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(30, typeName);
		ICompilationUnit expectedSource = getCompilationUnit(get14Project(), "src", IInternalDebugCoreConstants.EMPTY_STRING, "Breakpoints.java");
		assertTrue("did not find compilation unit for Breakpoints.java", expectedSource.exists());

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			// get source element for top frame
			IStackFrame frame = thread.getTopStackFrame();
			ISourceLocator sourceLocator = thread.getLaunch().getSourceLocator();
			Object source = sourceLocator.getSourceElement(frame);
			assertEquals("Source lookup failed", expectedSource.getResource(), source);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests source lookup in a top level type.
	 */
	public void testTopLevelType() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);

		ICompilationUnit expectedSource = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "SourceLookup.java");
		assertTrue("did not find compilation unit for SourceLookup.java", expectedSource.exists());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.SourceLookup"));
	}

	/**
	 * Tests source lookup in an inner type.
	 */
	public void testInnerType() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);

		ICompilationUnit expectedSource = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "SourceLookup.java");
		assertTrue("did not find compilation unit for SourceLookup.java", expectedSource.exists());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.SourceLookup$Inner"));
	}

	/**
	 * Tests source lookup in an inner inner type.
	 */
	public void testInnerNestedType() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);

		ICompilationUnit expectedSource = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "SourceLookup.java");
		assertTrue("did not find compilation unit for SourceLookup.java", expectedSource.exists());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.SourceLookup$Inner$Nested"));
	}

	/**
	 * Tests source lookup in a top level type, with a $ named class
	 */
	public void testTopLevel$Type() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);

		ICompilationUnit expectedSource = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "Source_$_Lookup.java");
		assertTrue("did not find compilation unit for Source_$_Lookup.java", expectedSource.exists());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.Source_$_Lookup"));
	}

	/**
	 * Tests source lookup in an inner type in a $ named class.
	 */
	public void testInner$Type() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);

		ICompilationUnit expectedSource = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "Source_$_Lookup.java");
		assertTrue("did not find compilation unit for Source_$_Lookup.java", expectedSource.exists());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.Source_$_Lookup$Inner"));
	}

	/**
	 * Tests source lookup in an inner inner type in a $ named class.
	 */
	public void testInnerNested$Type() throws Exception {
		IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
		IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);

		ICompilationUnit expectedSource = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "Source_$_Lookup.java");
		assertTrue("did not find compilation unit for Source_$_Lookup.java", expectedSource.exists());

		assertEquals("Source lookup failed", expectedSource, location.findSourceElement("org.eclipse.debug.tests.targets.Source_$_Lookup$Inner$Nested"));
	}
}
