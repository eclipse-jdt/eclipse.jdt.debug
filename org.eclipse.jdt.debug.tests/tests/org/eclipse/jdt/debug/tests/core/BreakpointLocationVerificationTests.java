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
		IType type= getJavaProject().findType("Breakpoints");
		assertNotNull("Cannot find type", type);
		CompilationUnit compilationUnit= AST.parseCompilationUnit(type.getCompilationUnit(), false);
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
		testLocation(9, 16, "Breakpoints");
	}
	
	public void testLineMethodSignature() throws Exception {
		testLocation(51, 52, "Breakpoints");
	}
	
	public void testLineInInnerType() throws Exception {
		testLocation(22, 22, "Breakpoints$InnerBreakpoints");
	}
	
	public void testLineInAnnonymousType() throws Exception {
		testLocation(43, 43, "Breakpoints");
	}
	
	public void testLineAfterAllCode() throws Exception {
		testLocation(160, -1, null);
	}
	
	public void testField(int line, int offsetInLine, String expectedFieldName, String expectedTypeName) throws Exception {
		IType type= getJavaProject().findType("WatchItemTests");
		assertNotNull("Cannot find type", type);
		ICompilationUnit unit= type.getCompilationUnit();
		CompilationUnit compilationUnit= AST.parseCompilationUnit(unit, false);
		int offset= new Document(unit.getSource()).getLineOffset(line - 1) + offsetInLine;
		BreakpointFieldLocator locator= new BreakpointFieldLocator(offset);
		compilationUnit.accept(locator);
		String fieldName= locator.getFieldName();
		assertEquals("Wrong File Name", expectedFieldName, fieldName);
		String typeName= locator.getTypeName();
		assertEquals("Wrong Type Name", expectedTypeName, typeName);
	}
	
	public void testFieldLocationOnField() throws Exception {
		testField(22, 19, "fVector", "WatchItemTests");
	}
	
	public void testFieldLocationNotOnField() throws Exception {
		testField(26, 16, null, null);
	}
	
	public void testMethod(int line, int offsetInLine, String expectedMethodName, String expectedTypeName, String expectedMethodSignature) throws Exception {
		IType type= getJavaProject().findType("WatchItemTests");
		assertNotNull("Cannot find type", type);
		ICompilationUnit unit= type.getCompilationUnit();
		CompilationUnit compilationUnit= AST.parseCompilationUnit(unit, false);
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
		testMethod(34, 20, "fillVector", "WatchItemTests", "()V");
	}
		
	public void testMethodOnCode() throws Exception {
		testMethod(36, 19, "fillVector", "WatchItemTests", "()V");
	}
	
	public void testMethodNotOnMethod() throws Exception {
		testMethod(30, 1, null, null, null);
	}
	
	public void testMethodOnMethodSignatureNotAvailable() throws Exception {
		testMethod(25, 23, "main", "WatchItemTests", null);
	}
}
