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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.core.stringsubstitution.IContextVariable;
import org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager;
import org.eclipse.debug.internal.core.stringsubstitution.IValueVariable;
import org.eclipse.debug.internal.core.stringsubstitution.IValueVariableListener;
import org.eclipse.debug.internal.core.stringsubstitution.StringVariableManager;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests string substitutions
 */
public class StringSubstitutionTests extends AbstractDebugTest implements IValueVariableListener {
	
	// change notification
	public IValueVariable[] fAdded;
	public IValueVariable[] fChanged;
	public IValueVariable[] fRemoved;
	
	public StringSubstitutionTests(String name) {
		super(name);
	}

	/**
	 * Returns the value variable with the given name or <code>null</code>
	 * if none.
	 * 
	 * @param name variable name 
	 * @return value variable with the given name or <code>null</code>
	 * if none
	 */
	protected IValueVariable getValueVariable(String name) {
		return StringVariableManager.getDefault().getValueVariable(name);
	}
	
	/**
	 * Returns the context variable with the given name or <code>null</code>
	 * if none.
	 * 
	 * @param name variable name 
	 * @return context variable with the given name or <code>null</code>
	 * if none
	 */
	protected IContextVariable getContextVariable(String name) {
		return StringVariableManager.getDefault().getContextVariable(name);
	}	
	
	/**
	 * Tests value variable initializer
	 * 
	 * @throws Exception
	 */
	public void testValueInitializer() throws Exception {
		IValueVariable variable = getValueVariable("VALUE_VAR_WITH_INITIALIZER");
		assertNotNull("Missing VALUE_VAR_WITH_INITIALIZER", variable);
		String value = variable.getValue();
		assertEquals("value should be 'initialized-value'", "initialized-value", value);
	}	
	
	/**
	 * Tests value variable with an initial value
	 */
	public void testValueSupplied() throws Exception {
		IValueVariable variable = getValueVariable("VALUE_VAR_WITH_VALUE");
		assertNotNull("Missing VALUE_VAR_WITH_VALUE", variable);
		String value = variable.getValue();
		assertEquals("initial-value", value);		
	}
	
	/**
	 * Tests a context variable with an argument
	 */
	public void testContextWithArg() throws CoreException {
		IContextVariable variable = getContextVariable("SAMPLE_CONTEXT_VAR");
		assertNotNull("Missing SAMPLE_CONTEXT_VAR", variable);
		String value = variable.getValue("ONE");
		assertEquals("the arg is ONE", value);
	}
	
	/**
	 * Tests a context variable with no argument
	 */	
	public void testContextWithoutArg() throws CoreException {
		IContextVariable variable = getContextVariable("SAMPLE_CONTEXT_VAR");
		assertNotNull("Missing SAMPLE_CONTEXT_VAR", variable);
		String value = variable.getValue(null);
		assertEquals("no arg", value);		
	}
	
	/**
	 * Test an expression with no variable references
	 */
	public void testNoReferences() throws CoreException {
		String expression = "no references";
		String result = doSubs(expression);
		assertEquals(expression, result);
	}
	
	/**
	 * Test an expression with a value variable reference
	 */
	public void testValueVarReference() throws CoreException {
		String expression = "something ${VALUE_VAR_WITH_INITIALIZER} else";
		String result = doSubs(expression);
		assertEquals("something initialized-value else", result);
	}
	
	/**
	 * Test an expression with a context variable reference
	 */
	public void testContextVarReferenceNoArgs() throws CoreException {
		String expression = "something ${SAMPLE_CONTEXT_VAR} else";
		String result = doSubs(expression);
		assertEquals("something no arg else", result);
	}
	
	/**
	 * Test an expression with a context variable reference and arg
	 */
	public void testContextVarReferenceWithArg() throws CoreException {
		String expression = "something ${SAMPLE_CONTEXT_VAR:TWO} else";
		String result = doSubs(expression);
		assertEquals("something the arg is TWO else", result);
	}	
	
	/**
	 * Test an expression with multiple references
	 */
	public void testMultipleReferences() throws CoreException {
		String expression = "${SAMPLE_CONTEXT_VAR:TWO} ${VALUE_VAR_WITH_INITIALIZER} ${VALUE_VAR_WITH_VALUE}";
		String result = doSubs(expression);
		assertEquals("the arg is TWO initialized-value initial-value", result);
	}	
		
	/**
	 * Perfrom substitutions on the given expression.
	 * 
	 * @param expression source expression
	 * @return the result after performing substitutions
	 */
	protected String doSubs(String expression) throws CoreException {
		IStringVariableManager manager = StringVariableManager.getDefault();
		return manager.performStringSubstitution(expression);
	}

	/**
	 * Test nested variables
	 */
	public void testNestedReferences() throws CoreException {
		String expression = "${SAMPLE_CONTEXT_VAR:${VALUE_VAR_WITH_VALUE}}";
		String result = doSubs(expression);
		assertEquals("the arg is initial-value", result);
	}
	
	/**
	 * Test recursive resolution
	 */
	public void testRecursiveReferences() throws CoreException {
		IStringVariableManager manager = StringVariableManager.getDefault();
		IValueVariable variable = manager.newValueVariable("my_var", null);
		try {
			manager.addVariables(new IValueVariable[]{variable});
			variable.setValue("${SAMPLE_CONTEXT_VAR:recurse}");
			String expression = "something ${my_var} else";
			String result = doSubs(expression);
			assertEquals("something the arg is recurse else", result);
		} finally {
			manager.removeVariables(new IValueVariable[]{variable});
		}
	}
	
	/**
	 * Test a string with an open ended reference. The open ended expression
	 * will not be translated.
	 */
	public void testOpenEndedBrace() throws CoreException {
		String expression = "${SAMPLE_CONTEXT_VAR:${VALUE_VAR_WITH_VALUE}";
		String result = doSubs(expression);
		assertEquals("${SAMPLE_CONTEXT_VAR:initial-value", result);
	}
	
	/**
	 * Test that we receive proper add notification.
	 * 
	 * @throws CoreException
	 */
	public void testAddNotificaiton() throws CoreException {
		IStringVariableManager manager = StringVariableManager.getDefault();
		List vars = new ArrayList();
		IValueVariable one = manager.newValueVariable("var_one", null);
		IValueVariable two = manager.newValueVariable("var_two", null);
		vars.add(one);
		vars.add(two);
		try {
			manager.addVariables(new IValueVariable[]{one, two});
			assertNotNull("no add notifications", fAdded);
			for (int i = 0; i < fAdded.length; i++) {
				vars.remove(fAdded[i]);
			}
			assertEquals("collection should be empty", 0, vars.size());
		} finally {
			manager.removeVariables(new IValueVariable[]{one, two});
		}
	}
	
	/**
	 * Test that we receive proper change notification.
	 * 
	 * @throws CoreException
	 */
	public void testChangeNotificaiton() throws CoreException {
		IStringVariableManager manager = StringVariableManager.getDefault();
		IValueVariable one = manager.newValueVariable("var_one", null);
		IValueVariable two = manager.newValueVariable("var_two", null);
		try {
			manager.addVariables(new IValueVariable[]{one, two});
			one.setValue("1");
			assertNotNull("no change notifications", fChanged);
			assertEquals("should be 1 change notification", 1, fChanged.length);
			assertEquals(one, fChanged[0]);
			two.setValue("2");
			assertNotNull("no change notifications", fChanged);
			assertEquals("should be 1 change notification", 1, fChanged.length);
			assertEquals(two, fChanged[0]);			
		} finally {
			manager.removeVariables(new IValueVariable[]{one, two});
		}
	}	
	
	/**
	 * Test that we receive proper remove notification.
	 * 
	 * @throws CoreException
	 */
	public void testRemoveNotificaiton() throws CoreException {
		IStringVariableManager manager = StringVariableManager.getDefault();
		List vars = new ArrayList();
		IValueVariable one = manager.newValueVariable("var_one", null);
		IValueVariable two = manager.newValueVariable("var_two", null);
		vars.add(one);
		vars.add(two);
		try {
			manager.addVariables(new IValueVariable[]{one, two});
			manager.removeVariables(new IValueVariable[]{one, two});
			assertNotNull("no remove notifications", fRemoved);
			for (int i = 0; i < fRemoved.length; i++) {
				vars.remove(fRemoved[i]);
			}
			assertEquals("collection should be empty", 0, vars.size());
		} finally {
			manager.removeVariables(new IValueVariable[]{one, two});
		}
	}	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		fAdded = null;
		fChanged = null;
		fRemoved = null;
		IStringVariableManager manager = StringVariableManager.getDefault();
		manager.addValueVariableListener(this);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		IStringVariableManager manager = StringVariableManager.getDefault();
		manager.removeValueVariableListener(this);
		fAdded = null;
		fChanged = null;
		fRemoved = null;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IValueVariableListener#variablesAdded(org.eclipse.debug.internal.core.stringsubstitution.IValueVariable[])
	 */
	public void variablesAdded(IValueVariable[] variables) {
		fAdded = variables;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IValueVariableListener#variablesChanged(org.eclipse.debug.internal.core.stringsubstitution.IValueVariable[])
	 */
	public void variablesChanged(IValueVariable[] variables) {
		fChanged = variables;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IValueVariableListener#variablesRemoved(org.eclipse.debug.internal.core.stringsubstitution.IValueVariable[])
	 */
	public void variablesRemoved(IValueVariable[] variables) {
		fRemoved = variables;
	}

}
