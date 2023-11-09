/*******************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.breakpoints.ValidBreakpointLocationLocator;
import org.eclipse.jface.text.Document;

/**
 * Tests breakpoint location locator.
 */
public class BreakpointLocationVerificationTests extends AbstractDebugTest {

	/**
	 * Constructor
	 */
	public BreakpointLocationVerificationTests(String name) {
		super(name);
	}

	/**
	 * Parses the specified <code>ICompilationUnit</code> into its respective
	 * <code>CompilationUnit</code>
	 * @return the parsed <code>CompilationUnit</code>
	 */
	private CompilationUnit parseCompilationUnit(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setSource(unit);
		parser.setUnitName(unit.getElementName());
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	/**
	 * Tests that the predefined location is locatable in the specified type
	 */
	private void testLocation(int lineToTry, int expectedLineNumber, String expectedTypeName) throws JavaModelException {
		testLocation(lineToTry, expectedLineNumber, expectedTypeName, expectedTypeName, false);
	}

	/**
	 * Tests that the predefined location is locatable in the specified type
	 */
	private void testLocation(int lineToTry, int expectedLineNumber, String baseTypeName, String expectedTypeName, boolean bestmatch) throws JavaModelException {
		IType type= get14Project().findType(baseTypeName);
		assertNotNull("Cannot find type", type);
		CompilationUnit compilationUnit= parseCompilationUnit(type.getCompilationUnit());
		ValidBreakpointLocationLocator locator= new ValidBreakpointLocationLocator(compilationUnit, lineToTry, true, bestmatch);
		compilationUnit.accept(locator);
		int lineNumber= locator.getLineLocation();
		assertEquals("Wrong line number", expectedLineNumber, lineNumber);
		String typeName= locator.getFullyQualifiedTypeName();
		if (typeName != null) {
			typeName = typeName.replaceAll("\\$", ".");
		}
        if (lineNumber == -1) {
			assertNull("Wrong type name", typeName);
        } else {
            assertEquals("Wrong type name", expectedTypeName, typeName);
        }
	}

	/**
	 * Tests setting a line breakpoint on a final field that is initialized
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=376354
	 */
	public void testFinalFieldWithTypeDecl() throws Exception {
		testLocation(17, 17, "FinalBreakpointLocations");
	}

	/**
	 * Tests setting a line breakpoint on a final field that is initialized looking for best match
	 *
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=376354
	 */
	public void testFinalFieldWithTypeDecla() throws Exception {
		testLocation(17, 17, "FinalBreakpointLocations", "FinalBreakpointLocations", true);
	}

	/**
	 * Tests setting a line breakpoint on an inner type member for the initializer of
	 * a final local field variable
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=376354
	 */
	public void testFinalFieldWithTypeDecl3() throws Exception {
		testLocation(17, 17, "FinalBreakpointLocations");
	}

	/**
	 * Tests setting a line breakpoint on an inner type member for the initializer of looking
	 * for best match
	 * a final local field variable
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=376354
	 */
	public void testFinalFieldWithTypeDecl3a() throws Exception {
		testLocation(17, 17, "FinalBreakpointLocations", "FinalBreakpointLocations", true);
	}

	/**
	 * Tests setting a line breakpoint on an inner-inner type member for the initializer of
	 * a final local field variable
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=376354
	 */
	public void testFinalFieldWithTypeDecl4() throws Exception {
		testLocation(20, 20, "FinalBreakpointLocations");
	}

	/**
	 * Tests setting a line breakpoint on an inner-inner type member for the initializer of
	 * a final local field variable looking for best match
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=376354
	 */
	public void testFinalFieldWithTypeDecl4a() throws Exception {
		testLocation(20, 20, "FinalBreakpointLocations", "FinalBreakpointLocations", true);
	}

	/**
	 * Tests setting a line breakpoint on a final field that has not been initialized
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=376354
	 */
	public void testFinalFieldWithTypeDecl5() throws Exception {
		testLocation(30, 33, "FinalBreakpointLocations");
	}

	/**
	 * Tests setting a line breakpoint on a final field that has not been initialized looking
	 * for best match
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=376354
	 */
	public void testFinalFieldWithTypeDecl5a() throws Exception {
		testLocation(30, 33, "FinalBreakpointLocations", "FinalBreakpointLocations", true);
	}

	/**
	 * Test line before type declaration
	 */
	public void testLineBeforeTypeDeclaration() throws Exception {
		testLocation(12, 21, "BreakpointsLocation");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineMethodSignature() throws Exception {
		testLocation(32, 33, "BreakpointsLocation");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineInInnerType() throws Exception {
		testLocation(28, 28, "BreakpointsLocation.InnerClass");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineInAnnonymousType() throws Exception {
		testLocation(42, 42, "BreakpointsLocation");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineAfterAllCode() throws Exception {
		// ********* this test need to be updated every time BreakpointsLocation.java is modified *************
		testLocation(85, -1, "BreakpointsLocation");
		// ******************************
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineVariableDeclarationWithAssigment() throws Exception {
		testLocation(46, 49, "BreakpointsLocation");
	}

	/**
	 * Tests that a breakpoint is not set on a final field
	 */
	public void testFieldLocationOnFinalField() throws Exception {
		testLocation(16, 16, "org.eclipse.debug.tests.targets.BreakpointsLocationBug344984");
	}

	/**
	 * Tests that a breakpoint is not set on a final field looking
	 * for best match
	 */
	public void testFieldLocationOnFinalFielda() throws Exception {
		testLocation(16, 16, "org.eclipse.debug.tests.targets.BreakpointsLocationBug344984", "org.eclipse.debug.tests.targets.BreakpointsLocationBug344984", true);
	}

	/**
	 * Tests a specific breakpoint location
	 */
    public void testEmptyLabel() throws Exception {
		testLocation(18, 19, "LabelTest");
    }

    /**
	 * Tests a specific breakpoint location
	 */
    public void testNestedEmptyLabels() throws Exception {
		testLocation(22, 24, "LabelTest");
    }

    /**
	 * Tests a specific breakpoint location
	 */
    public void testLabelWithCode() throws Exception {
		testLocation(24, 24, "LabelTest");
    }

    /**
	 * Tests a specific breakpoint location
	 */
	public void testLineFieldDeclarationWithAssigment() throws Exception {
		testLocation(54, 58, "BreakpointsLocation");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineExpressionReplacedByConstant1() throws Exception {
		testLocation(65, 65, "BreakpointsLocation");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineExpressionReplacedByConstant2() throws Exception {
		testLocation(67, 65, "BreakpointsLocation");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineExpressionNotReplacedByConstant1() throws Exception {
		testLocation(73, 73, "BreakpointsLocation");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineExpressionNotReplacedByConstant2() throws Exception {
		testLocation(75, 75, "BreakpointsLocation");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineLitteral1() throws Exception {
		testLocation(49, 49, "BreakpointsLocation");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testLineLitteral2() throws Exception {
		testLocation(58, 58, "BreakpointsLocation");
	}

	/**
	 * Tests a specific breakpoint location
	 */
	public void testInnerStaticClass() throws Exception {
		String version = get14Project().getOption(JavaCore.COMPILER_COMPLIANCE, false);
		if(JavaCore.VERSION_1_5.equals(version) || JavaCore.VERSION_1_6.equals(version)) {
			testLocation(79, 79, "BreakpointsLocation", "BreakpointsLocation.1StaticInnerClass", false);
		}
		else {
			testLocation(82, 82, "BreakpointsLocation", "BreakpointsLocation.1StaticInnerClass", false);
		}
	}

	/**
	 * Tests that an specific field is locatable in a specific type at a given offset and line
	 */
	public void testField(int line, int offsetInLine, String expectedFieldName, String expectedTypeName) throws Exception {
		IType type= get14Project().findType("BreakpointsLocation");
		assertNotNull("Cannot find type", type);
		ICompilationUnit unit= type.getCompilationUnit();
		CompilationUnit compilationUnit= parseCompilationUnit(unit);
		int offset= new Document(unit.getSource()).getLineOffset(line - 1) + offsetInLine;
		BreakpointFieldLocator locator= new BreakpointFieldLocator(offset);
		compilationUnit.accept(locator);
		String fieldName= locator.getFieldName();
		assertEquals("Wrong File Name", expectedFieldName, fieldName);
		String typeName= locator.getTypeName();
		assertEquals("Wrong Type Name", expectedTypeName, typeName);
	}

	/**
	 * Tests that a specific filed is at the correct location
	 */
	public void testFieldLocationOnField() throws Exception {
		testField(30, 20, "fList", "BreakpointsLocation");
	}

	/**
	 * Tests that a specific filed is at the correct location
	 */
	public void testFieldLocationNotOnField() throws Exception {
		testField(36, 21, null, null);
	}

	/**
	 * Tests that a specific method is locatable in the specified type at the given offset and line
	 */
	public void testMethod(int line, int offsetInLine, String expectedMethodName, String expectedTypeName, String expectedMethodSignature) throws Exception {
		IType type= get14Project().findType("BreakpointsLocation");
		assertNotNull("Cannot find type", type);
		ICompilationUnit unit= type.getCompilationUnit();
		CompilationUnit compilationUnit= parseCompilationUnit(unit);
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

	/**
	 * Tests that a specific method is locatable in a specific location
	 */
	public void testMethodOnSignature() throws Exception {
		testMethod(20, 23, "test1", "BreakpointsLocation", "()V");
	}

	/**
	 * Tests that a specific method is locatable in a specific location
	 */
	public void testMethodOnCode() throws Exception {
		testMethod(19, 17, "test1", "BreakpointsLocation", "()V");
	}

	/**
	 * Tests that a specific method is locatable in a specific location
	 */
	public void testMethodNotOnMethod() throws Exception {
		testMethod(30, 1, null, null, null);
	}

	/**
	 * Tests that a specific method is locatable in a specific location
	 */
	public void testMethodOnMethodSignatureNotAvailable() throws Exception {
		testMethod(35, 4, "test2", "BreakpointsLocation", null);
	}
}