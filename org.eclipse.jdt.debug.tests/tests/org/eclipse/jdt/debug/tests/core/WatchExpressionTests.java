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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.testplugin.ExpressionWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;


/**
 * WatchExpressionTests
 */
public class WatchExpressionTests extends AbstractDebugTest {
	
	public WatchExpressionTests(String name) {
		super(name);
	}
	
	/**
	 * Test a watch expression that is created before a program is executed.
	 */
	public void testDeferredExpression() throws Exception {
		IWatchExpression expression = getExpressionManager().newWatchExpression("((Integer)fVector.get(3)).intValue()");
		getExpressionManager().addExpression(expression);
		String typeName = "WatchItemTests";
		createLineBreakpoint(42, typeName);
		IJavaThread thread= null;
		try {
			DebugElementEventWaiter waiter = new ExpressionWaiter(DebugEvent.CHANGE, expression);
			waiter.setTimeout(60000);
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread); 
			Object source = waiter.waitForEvent();
			assertNotNull("Watch expression did not change", source);
			IValue value = expression.getValue();
			// create comparison value
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			IJavaValue compare = target.newValue(3);
			assertEquals("Watch expression should be Integer(3)", compare, value);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			removeAllExpressions();
		}				
	}
	
	/**
	 * Test a watch expression that is created while a program is suspended.
	 */
	public void testNonDeferredExpression() throws Exception {
		String typeName = "WatchItemTests";
		createLineBreakpoint(42, typeName);
		IJavaThread thread= null;
		IWatchExpression expression = null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			// create the expression, waiter, and then add it (to be evaluated)
			expression = getExpressionManager().newWatchExpression("((Integer)fVector.get(3)).intValue()");
			DebugElementEventWaiter waiter = new ExpressionWaiter(DebugEvent.CHANGE, expression);
			getExpressionManager().addExpression(expression);
			 
			Object source = waiter.waitForEvent();
			assertNotNull("Watch expression did not change", source);
			IValue value = expression.getValue();
			// create comparison value
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			IJavaValue compare = target.newValue(3);
			assertEquals("Watch expression should be Integer(3)", compare, value);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			removeAllExpressions();
		}				
	}
	
	/**
	 * Test a watch expression updates while stepping.
	 */
	public void testStepping() throws Exception {
		IWatchExpression expression = getExpressionManager().newWatchExpression("i");
		getExpressionManager().addExpression(expression);
		String typeName = "WatchItemTests";
		createLineBreakpoint(37, typeName);
		IJavaThread thread= null;
		try {
			DebugElementEventWaiter waiter = new ExpressionWaiter(DebugEvent.CHANGE, expression);
			waiter.setTimeout(60000);
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread); 
			Object source = waiter.waitForEvent();
			assertNotNull("Watch expression did not change", source);
			IValue value = expression.getValue();
			// create comparison value
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			IJavaValue compare = target.newValue(0);
			assertEquals("Watch expression should be 0", compare, value);
			
			// now step once - should still be 0
			waiter = new ExpressionWaiter(DebugEvent.CHANGE, expression);
			stepOver((IJavaStackFrame)thread.getTopStackFrame());
			source = waiter.waitForEvent();
			assertNotNull("Watch expression did not change", source);
			
			// now step again - should be 1
			waiter = new ExpressionWaiter(DebugEvent.CHANGE, expression);
			stepOver((IJavaStackFrame)thread.getTopStackFrame());
			source = waiter.waitForEvent();
			assertNotNull("Watch expression did not change", source);
			
			value = expression.getValue();			
			// create comparison value
			compare = target.newValue(1);
			assertEquals("Watch expression should be 1", compare, value);
						
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			removeAllExpressions();
		}				
	}
		
	/**
	 * Returns the expression manager
	 * 
	 * @return expression manager
	 */
	protected IExpressionManager getExpressionManager() {
		return DebugPlugin.getDefault().getExpressionManager();
	}
	
	/**
	 * Ensure the expression view is visible
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Display display = DebugUIPlugin.getStandardDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				try {
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchPage page = workbench.showPerspective(IDebugUIConstants.ID_DEBUG_PERSPECTIVE, DebugUIPlugin.getActiveWorkbenchWindow());
					page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
				} catch (WorkbenchException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Removes all expressions from the manager
	 */
	protected void removeAllExpressions() {
		IExpressionManager manager = getExpressionManager();
		manager.removeExpressions(manager.getExpressions());
	}

}
