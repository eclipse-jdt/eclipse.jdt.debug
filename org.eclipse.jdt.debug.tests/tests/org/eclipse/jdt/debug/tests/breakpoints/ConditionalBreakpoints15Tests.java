/*******************************************************************************
 * Copyright (c) 2012 Jesper Steen Moller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jesper Steen Moller - initial API and implementation, adapted from
 *     Stefan Mandels contribution in bug 341232, and existing debug tests
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests conditional breakpoints.
 */
public class ConditionalBreakpoints15Tests extends AbstractDebugTest {
	
	/**
	 * Constructor
	 * @param name
	 */
	public ConditionalBreakpoints15Tests(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.tests.AbstractDebugTest#getProjectContext()
	 */
	@Override
	protected IJavaProject getProjectContext() {
		return get15Project();
	}

	/**
	 * Tests a breakpoint with a simple condition
	 * @throws Exception
	 */
	public void testSimpleConditionalBreakpointOnParameterizedType() throws Exception {
		String typeName = "a.b.c.ConditionalsNearGenerics";
		String innerTypeName = "a.b.c.ConditionalsNearGenerics.ItemIterator";
		IJavaLineBreakpoint bp1 = createConditionalLineBreakpoint(33, typeName, "false", true);
		IJavaLineBreakpoint bp2 = createConditionalLineBreakpoint(39, typeName, "false", true);
		IJavaLineBreakpoint bp3 = createConditionalLineBreakpoint(52, innerTypeName, "false", true);
		IJavaLineBreakpoint bp4 = createConditionalLineBreakpoint(53, innerTypeName, "true", true);
		
		IJavaThread thread= null;
		try {
			thread= launchToLineBreakpoint(typeName, bp4); // If compiled correctly, this will jump over bp1-bp3 !!

			bp1.delete();
			bp2.delete();
			bp3.delete();
			bp4.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	}
