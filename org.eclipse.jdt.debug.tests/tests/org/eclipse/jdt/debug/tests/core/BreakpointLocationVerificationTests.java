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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.actions.BreakpointFieldLocator;
import org.eclipse.jdt.internal.debug.ui.actions.BreakpointMethodLocator;
import org.eclipse.jdt.internal.debug.ui.actions.ValidBreakpointLocationLocator;
import org.eclipse.jface.text.Document;

/**
 * Tests breakpoint location locator.
 */
public class BreakpointLocationVerificationTests extends AbstractDebugTest {
	
	public BreakpointLocationVerificationTests(String name) {
		super(name);
	}

	private void testLocation(int lineToTry, int expectedLineNumber, String expectedTypeName) throws JavaModelException {
		IType type= getJavaProject().findType("BreakpointsLocation");
		assertNotNull("Cannot find type", type);
		CompilationUnit compilationUnit= AST.parseCompilationUnit(type.getCompilationUnit(), false, null, null);
		ValidBreakpointLocationLocator locator= new ValidBreakpointLocationLocator(compilationUnit, lineToTry);
		compilationUnit.accept(locator);
		int lineNumber= locator.getValidLocation();		
		assertEquals("Wrong line number", expectedLineNumber, lineNumber);
		String typeName= locator.getFullyQualifiedTypeName();
		assertEquals("Wrong type name", expectedTypeName, typeName);
	}

	/**
	 * Test line before type declaration
	 * 
	 * @throws Exception
	 */
	public void testLineBeforeTypeDeclaration() throws Exception {
		testLocation(9, 18, "BreakpointsLocation");
	}
	
	public void testLineMethodSignature() throws Exception {
		testLocation(32, 33, "BreakpointsLocation");
	}
	
	public void testLineInInnerType() throws Exception {
		testLocation(25, 25, "BreakpointsLocation$InnerClass");
	}
	
	public void testLineInAnnonymousType() throws Exception {
		testLocation(39, 39, "BreakpointsLocation");
	}
	
	public void testLineAfterAllCode() throws Exception {
		// ********* this test need to be updated everytime BreakpointsLocation.java is modified *************
		testLocation(74, -1, null);
		// ******************************
	}
	
	public void testLineVariableDeclarationWithAssigment() throws Exception {
		testLocation(43, 44, "BreakpointsLocation");
	}
	
	public void testLineFieldDeclarationWithAssigment() throws Exception {
		testLocation(51, 53, "BreakpointsLocation");
	}
	
	public void testLineExpressionReplacedByConstant1() throws Exception {
		testLocation(62, 62, "BreakpointsLocation");
	}
	
	public void testLineExpressionReplacedByConstant2() throws Exception {
		testLocation(64, 62, "BreakpointsLocation");
	}
	
	public void testLineExpressionNotReplacedByConstant1() throws Exception {
		testLocation(70, 70, "BreakpointsLocation");
	}
	
	public void testLineExpressionNotReplacedByConstant2() throws Exception {
		testLocation(72, 72, "BreakpointsLocation");
	}
	
	public void testLineLitteral1() throws Exception {
		testLocation(46, 46, "BreakpointsLocation");
	}
	
	public void testLineLitteral2() throws Exception {
		testLocation(55, 55, "BreakpointsLocation");
	}
	
	public void testField(int line, int offsetInLine, String expectedFieldName, String expectedTypeName) throws Exception {
		IType type= getJavaProject().findType("BreakpointsLocation");
		assertNotNull("Cannot find type", type);
		ICompilationUnit unit= type.getCompilationUnit();
		CompilationUnit compilationUnit= AST.parseCompilationUnit(unit, false, null, null);
		int offset= new Document(unit.getSource()).getLineOffset(line - 1) + offsetInLine;
		BreakpointFieldLocator locator= new BreakpointFieldLocator(offset);
		compilationUnit.accept(locator);
		String fieldName= locator.getFieldName();
		assertEquals("Wrong File Name", expectedFieldName, fieldName);
		String typeName= locator.getTypeName();
		assertEquals("Wrong Type Name", expectedTypeName, typeName);
	}
	
	public void testFieldLocationOnField() throws Exception {
		testField(30, 20, "fList", "BreakpointsLocation");
	}
	
	public void testFieldLocationNotOnField() throws Exception {
		testField(33, 18, null, null);
	}
	
	public void testMethod(int line, int offsetInLine, String expectedMethodName, String expectedTypeName, String expectedMethodSignature) throws Exception {
		IType type= getJavaProject().findType("BreakpointsLocation");
		assertNotNull("Cannot find type", type);
		ICompilationUnit unit= type.getCompilationUnit();
		CompilationUnit compilationUnit= AST.parseCompilationUnit(unit, false, null, null);
		int offset= new Document(unit.getSource()).getLineOffset(line - 1) + offsetInLine;
		BreakpointMethodLocator locator= new BreakpointMethodLocator(offset);
		compilationUnit.accept(locator);
		String methodName= locator.getMethodName();
		assertEquals("Wrong method name", expectedMethodName, methodName);
		String typeName= locator.getTypeName();
		assertEquals("Wrong type name", expectedTypeName, typeName);
		String methodSignature= locator.getMethodSignature();
		assertEquals("Wrong method signature", expectedMethodSignature, methodSignature);
	}
	
	public void testMethodOnSignature() throws Exception {
		testMethod(17, 20, "test1", "BreakpointsLocation", "()V");
	}
		
	public void testMethodOnCode() throws Exception {
		testMethod(19, 17, "test1", "BreakpointsLocation", "()V");
	}
	
	public void testMethodNotOnMethod() throws Exception {
		testMethod(30, 1, null, null, null);
	}
	
	public void testMethodOnMethodSignatureNotAvailable() throws Exception {
		testMethod(32, 1, "test2", "BreakpointsLocation", null);
	}
}
