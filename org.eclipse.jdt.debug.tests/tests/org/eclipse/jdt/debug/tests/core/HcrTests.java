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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests hot code replace
 */
public class HcrTests extends AbstractDebugTest {
	
	public HcrTests(String name) {
		super(name);
	}

	public void testSimpleHcr() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass";
		createLineBreakpoint(39, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {

				// look at the value of 'x' - it should be "One"
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				IJavaVariable variable = frame.findVariable("x");
				assertNotNull("Could not find 'x'", variable);
				assertEquals("value of 'x' should be 'One'", "One", variable.getValue().getValueString());
				
				// now do the HCR
				ICompilationUnit cu = getCompilationUnit(getJavaProject(), "src", "org.eclipse.debug.tests.targets", "HcrClass.java");
				assertTrue("HcrClass.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"One\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Two\"" + contents.substring(index + 5);
				buffer.setContents(newCode);
				
				// set up event listener for "suspend" (end of HCR)
				DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
				// save contents
				buffer.save(null, false);
				
				Object source = waiter.waitForEvent();
				assertNotNull("HCR did not complete", source);
				assertTrue(source instanceof IJavaThread);
				thread = (IJavaThread)source;
	
				// should have dropped to frame 'one'
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("Should have dropped to method 'one'", "one", frame.getMethodName());
				
				// resume to breakpoint
				thread = resume(thread);
				
				// value of 'x' should now be "Two"
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				variable = frame.findVariable("x");
				assertNotNull("Could not find 'x'", variable);
				assertEquals("value of 'x' should be 'Two'", "Two", variable.getValue().getValueString());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
						
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}

}
