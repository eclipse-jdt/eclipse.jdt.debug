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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.actions.StepIntoSelectionHandler;

/**
 * Tests 'step into selection'
 */
public class StepIntoSelectionTests extends AbstractDebugTest {
	
	public StepIntoSelectionTests(String name) {
		super(name);
	}

	/**
	 * Step into 'new StepIntoSelectionClass()'
	 * 
	 * @throws Exception
	 */
	public void testStepIntoSourceConstructor() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.StepIntoSelectionClass";
		createLineBreakpoint(21, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			ICompilationUnit cu = getCompilationUnit(getJavaProject(), "src", "org.eclipse.debug.tests.targets", "StepIntoSelectionClass.java");
			IType type = cu.getType("StepIntoSelectionClass");
			IMethod method = type.getMethod("StepIntoSelectionClass", new String[0]);
			assertTrue("Could not find constructor", method.exists());
			
			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;
			
			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in constructor", "<init>", frame.getMethodName());
			
						
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	/**
	 * Step into 'step()'
	 * 
	 * @throws Exception
	 */
	public void testStepIntoSourceMethod() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.StepIntoSelectionClass";
		createLineBreakpoint(22, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			ICompilationUnit cu = getCompilationUnit(getJavaProject(), "src", "org.eclipse.debug.tests.targets", "StepIntoSelectionClass.java");
			IType type = cu.getType("StepIntoSelectionClass");
			IMethod method = type.getMethod("step", new String[0]);
			assertTrue("Could not find method 'step'", method.exists());
			
			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;
			
			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in method 'step'", "step", frame.getMethodName());
			
						
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	/**
	 * Step into 'Vector.addElement(Object)'
	 * 
	 * @throws Exception
	 */
	public void testStepIntoBinaryMethod() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.StepIntoSelectionClass";
		createLineBreakpoint(33, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IType type = getJavaProject().findType("java.util.Vector");
			IMethod method = type.getMethod("addElement", new String[]{"Ljava.lang.Object;"});
			assertTrue("Could not find method 'addElement'", method.exists());
			
			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;
			
			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in method 'addElement'", "addElement", frame.getMethodName());
			
						
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	

	/**
	 * Step into 'new Integer(i)'
	 * 
	 * @throws Exception
	 */
	public void testStepIntoBinaryConstructor() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.StepIntoSelectionClass";
		createLineBreakpoint(33, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IType type = getJavaProject().findType("java.lang.Integer");
			IMethod method = type.getMethod("Integer", new String[]{"I"});
			assertTrue("Could not find method constructor", method.exists());
			
			StepIntoSelectionHandler handler = new StepIntoSelectionHandler(thread, (IJavaStackFrame)thread.getTopStackFrame(), method);
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			handler.step();
			Object source = waiter.waitForEvent();
			assertEquals("Step did not complete", thread, source);
			thread = (IJavaThread)source;
			
			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("Should be in constructor", "<init>", frame.getMethodName());
			
						
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	
}
