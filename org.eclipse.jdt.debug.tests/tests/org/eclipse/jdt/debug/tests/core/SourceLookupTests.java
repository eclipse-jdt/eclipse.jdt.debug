/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests source lookup.
 */
public class SourceLookupTests extends AbstractDebugTest {
	
	public SourceLookupTests(String name) {
		super(name);
	}

	/**
	 * see Bug 37545
	 */
	public void testStackFrameReuse() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.CallStack";
		createLineBreakpoint(28, "org.eclipse.debug.tests.targets.ClassOne");
		createLineBreakpoint(28, "org.eclipse.debug.tests.targets.ClassTwo");
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			// get source element for first breakpoint
			IStackFrame[] frames = thread.getStackFrames();
			IStackFrame frame = frames[2]; 
			ISourceLocator sourceLocator = thread.getLaunch().getSourceLocator();
			Object source1 = sourceLocator.getSourceElement(frame);
			
			IPackageFragment[] fragments = getJavaProject().getPackageFragments();
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
			ICompilationUnit unit2 = fragment.getCompilationUnit("ClassTwo.java");
			assertEquals("Source lookup failed for frame2", unit2, source2);
						
			// the source elements should not be equal
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
}
