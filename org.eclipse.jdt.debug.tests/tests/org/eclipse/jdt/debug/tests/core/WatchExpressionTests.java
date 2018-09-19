/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
     *
     * see bug 81519. This test is flawed as expressions added to the manager
     * are note updated automatically. They are updated by the action that
     * creates the expression, or when a selection change occurrs in the debug view.
     * This test can pass as the ordering of expression addition and selection
     * change events can vary. However, it it attempting to test behavior that
     * does not exist, and is removed from the test suite.
	 */
	public void REMOVEDtestNonDeferredExpression() throws Exception {
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
	 *
	 * THIS TEST HAS BEEN DISABLED DUE TO BUG 228400
	 */
	public void DisabledtestStepping() throws Exception {
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

			// check for errors
			dumpErrors(expression);
			assertFalse("Should not have errors in expression", expression.hasErrors());

			// now step again - should be 1
			waiter = new ExpressionWaiter(DebugEvent.CHANGE, expression);
			stepOver((IJavaStackFrame)thread.getTopStackFrame());
			source = waiter.waitForEvent();
			assertNotNull("Watch expression did not change", source);

			// check for errors
			dumpErrors(expression);
			assertFalse("Should not have errors in expression", expression.hasErrors());

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
	 * Dumps any error messages to the console.
	 *
     * @param expression
     */
    private void dumpErrors(IWatchExpression expression) {
        if (expression.hasErrors()) {
            String[] errorMessages = expression.getErrorMessages();
            for (int i = 0; i < errorMessages.length; i++) {
                String string = errorMessages[i];
                System.out.println(getName() + ": " + string);
            }
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
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Display display = DebugUIPlugin.getStandardDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchPage page = workbench.showPerspective(IDebugUIConstants.ID_DEBUG_PERSPECTIVE, DebugUIPlugin.getActiveWorkbenchWindow());
					page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
                    page.showView(IDebugUIConstants.ID_DEBUG_VIEW);
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
