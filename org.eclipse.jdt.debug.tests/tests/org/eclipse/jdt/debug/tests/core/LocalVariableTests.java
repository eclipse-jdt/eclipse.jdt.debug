/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class LocalVariableTests extends AbstractDebugTest implements IValueDetailListener {
	
	String fDetail = null;
	
	public LocalVariableTests(String name) {
		super(name);
	}

	public void testSimpleVisibility() throws Exception {
		String typeName = "LocalVariablesTests";
		
		ILineBreakpoint bp = createLineBreakpoint(18, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IJavaVariable[] vars = frame.getLocalVariables();
			assertEquals("Should be no visible locals", 0, vars.length);
			
			stepOver(frame);
			frame = (IJavaStackFrame)thread.getTopStackFrame();
			stepOver(frame);
			frame = (IJavaStackFrame)thread.getTopStackFrame();
			
			vars = frame.getLocalVariables();
			assertEquals("Should be one visible local", 1, vars.length);			
			assertEquals("Visible var should be 'i1'", "i1", vars[0].getName());
			
			stepOver(frame);
			frame = (IJavaStackFrame)thread.getTopStackFrame();
			stepOver(frame);
			frame = (IJavaStackFrame)thread.getTopStackFrame();
			
			vars = frame.getLocalVariables();
			assertEquals("Should be two visible locals", 2, vars.length);			
			assertEquals("Visible var 1 should be 'i1'", "i1", vars[0].getName());			
			assertEquals("Visible var 2 should be 'i2'", "i2", vars[1].getName());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	public void testEvaluationAssignments() throws Exception {
		String typeName = "LocalVariablesTests";
		
		ILineBreakpoint bp = createLineBreakpoint(22, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IJavaDebugTarget target = (IJavaDebugTarget)frame.getDebugTarget();
			IVariable i1 = findVariable(frame, "i1");
			assertNotNull("Could not find variable 'i1'", i1);
			assertEquals("'i1' value should be '0'", target.newValue(0), i1.getValue());
			
			IVariable i2 = findVariable(frame, "i2");
			assertNotNull("Could not find variable 'i2'", i2);
			assertEquals("'i2' value should be '1'", target.newValue(1), i2.getValue());
						
			evaluate("i1 = 73;", frame);			
			// the value should have changed
			assertEquals("'i1' value should be '73'", target.newValue(73), i1.getValue());
			
			evaluate("i2 = i1;", frame);
			// the value should have changed
			assertEquals("'i2' value should be '73'", target.newValue(73), i2.getValue());
			
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}		
	
	protected void doArrayDetailTestNonDefPkg(String varName, String expectedDetails) throws Exception {
		doArrayDetailTest(varName, expectedDetails, "org.eclipse.debug.tests.targets.ArrayDetailTests", 64);
	}
	
	protected void doArrayDetailTestDefPkg(String varName, String expectedDetails) throws Exception {
		doArrayDetailTest(varName, expectedDetails, "ArrayDetailTestsDef", 63);
	}	
	
	protected void doArrayDetailTest(String varName, String expectedDetails, String mainName, int lineNumber) throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(lineNumber, mainName);		
		IDebugModelPresentation presentation = DebugUITools.newDebugModelPresentation();
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(mainName, bp);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			frame.getDebugTarget();
			IVariable var = findVariable(frame, varName);
			assertNotNull("Could not find variable " + varName, var);
			IValue value = var.getValue();
			
			synchronized (this) {
				fDetail = null;
				presentation.computeDetail(value, this);
				wait(DEFAULT_TIMEOUT);
			}
			assertNotNull("Details not computed", fDetail);
			assertEquals(expectedDetails, fDetail);
			
		} finally {
			presentation.dispose();
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}	

	public void detailComputed(IValue value, String result) {
		synchronized (this) {
			fDetail = result;
			notifyAll();
		}
		
	}
	
	public void testStringArrayDetails() throws Exception {
		doArrayDetailTestNonDefPkg("strings", "[0, 1, 10, 11, 100]");
	}
	
	public void testIntArrayDetails() throws Exception {
		doArrayDetailTestNonDefPkg("primitives", "[0, 1, 2, 3, 4]");
	}
	
	public void testTopLevelTypeArrayDetails() throws Exception {
		doArrayDetailTestNonDefPkg("outers", "[OutermostObject, OutermostObject, OutermostObject, OutermostObject, OutermostObject]");
	}
	
	public void testInnerTypeDetails() throws Exception {
		doArrayDetailTestNonDefPkg("middle", "[anInnerObject, anInnerObject, anInnerObject, anInnerObject, anInnerObject]");
	}
	
	public void testNestedInnerTypeDetails() throws Exception {
		doArrayDetailTestNonDefPkg("inners", "[aSecondInnerObject, aSecondInnerObject, aSecondInnerObject, aSecondInnerObject, aSecondInnerObject]");
	}
	
	public void testInterfaceTypeDetails() throws Exception {
		doArrayDetailTestNonDefPkg("runs", "[Runnable, Runnable, Runnable, Runnable, Runnable]");
	}
	
	public void testStringArrayDetailsDefPkg() throws Exception {
		doArrayDetailTestDefPkg("strings", "[0, 1, 10, 11, 100]");
	}
	
	public void testIntArrayDetailsDefPkg() throws Exception {
		doArrayDetailTestDefPkg("primitives", "[0, 1, 2, 3, 4]");
	}
	
	public void testTopLevelTypeArrayDetailsDefPkg() throws Exception {
		doArrayDetailTestDefPkg("outers", "[OutermostObject, OutermostObject, OutermostObject, OutermostObject, OutermostObject]");
	}
	
	public void testInnerTypeDetailsDefPkg() throws Exception {
		doArrayDetailTestDefPkg("middle", "[anInnerObject, anInnerObject, anInnerObject, anInnerObject, anInnerObject]");
	}
	
	public void testNestedInnerTypeDetailsDefPkg() throws Exception {
		doArrayDetailTestDefPkg("inners", "[aSecondInnerObject, aSecondInnerObject, aSecondInnerObject, aSecondInnerObject, aSecondInnerObject]");
	}
	
	public void testInterfaceTypeDetailsDefPkg() throws Exception {
		doArrayDetailTestDefPkg("runs", "[Runnable, Runnable, Runnable, Runnable, Runnable]");
	}		
}
