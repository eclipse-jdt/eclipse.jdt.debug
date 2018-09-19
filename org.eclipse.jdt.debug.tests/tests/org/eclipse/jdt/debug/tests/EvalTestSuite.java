/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Jesper S. Møller - bug 422029: [1.8] Enable debug evaluation support for default methods
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import java.util.Enumeration;

import org.eclipse.jdt.debug.tests.eval.ArrayAllocationTests;
import org.eclipse.jdt.debug.tests.eval.ArrayAssignmentTests;
import org.eclipse.jdt.debug.tests.eval.ArrayValueTests;
import org.eclipse.jdt.debug.tests.eval.BooleanAssignmentOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.BooleanOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.ByteAssignmentOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.ByteOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.CharAssignmentOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.CharOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.DoubleAssignmentOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.DoubleOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.FieldValueTests;
import org.eclipse.jdt.debug.tests.eval.FloatAssignmentOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.FloatOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.IntAssignmentOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.IntOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.Java8Tests;
import org.eclipse.jdt.debug.tests.eval.LabelTests;
import org.eclipse.jdt.debug.tests.eval.LocalVarAssignmentTests;
import org.eclipse.jdt.debug.tests.eval.LocalVarValueTests;
import org.eclipse.jdt.debug.tests.eval.LongAssignmentOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.LongOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.LoopTests;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_120;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_145;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_155;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_179;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_203;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_214;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_252;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_279;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_304;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_315;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_354;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_381;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_406;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_417;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_455;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_481;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_506;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_517;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_529;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_566;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_592;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_616;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_65;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_69;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_690;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_714;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_724;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_739;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_94;
import org.eclipse.jdt.debug.tests.eval.NumericTypesCastTests;
import org.eclipse.jdt.debug.tests.eval.QualifiedFieldValueTests;
import org.eclipse.jdt.debug.tests.eval.QualifiedStaticFieldValueTests;
import org.eclipse.jdt.debug.tests.eval.QualifiedStaticFieldValueTests2;
import org.eclipse.jdt.debug.tests.eval.ShortAssignmentOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.ShortOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.StaticFieldValueTests;
import org.eclipse.jdt.debug.tests.eval.StaticFieldValueTests2;
import org.eclipse.jdt.debug.tests.eval.StringPlusAssignmentOpTests;
import org.eclipse.jdt.debug.tests.eval.StringPlusOpTests;
import org.eclipse.jdt.debug.tests.eval.TestsAnonymousClassVariable;
import org.eclipse.jdt.debug.tests.eval.TestsArrays;
import org.eclipse.jdt.debug.tests.eval.TestsBreakpointConditions;
import org.eclipse.jdt.debug.tests.eval.TestsNestedTypes1;
import org.eclipse.jdt.debug.tests.eval.TestsNestedTypes2;
import org.eclipse.jdt.debug.tests.eval.TestsNumberLiteral;
import org.eclipse.jdt.debug.tests.eval.TestsOperators1;
import org.eclipse.jdt.debug.tests.eval.TestsOperators2;
import org.eclipse.jdt.debug.tests.eval.TestsTypeHierarchy1;
import org.eclipse.jdt.debug.tests.eval.TestsTypeHierarchy2;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_119_1;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_146_1;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_32_1;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_32_2;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_32_3;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_32_4;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_32_5;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_32_6;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_68_1;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_68_2;
import org.eclipse.jdt.debug.tests.eval.TypeHierarchy_68_3;
import org.eclipse.jdt.debug.tests.eval.VariableDeclarationTests;
import org.eclipse.jdt.debug.tests.eval.XfixOperatorsTests;
import org.eclipse.swt.widgets.Display;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Test all areas of the UI.
 */
public class EvalTestSuite extends TestSuite {

	/**
	 * Flag that indicates test are in progress
	 */
	protected boolean fTesting = true;

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 * @return the test suite
	 */
	public static Test suite() {
		return new EvalTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public EvalTestSuite() {
		addTest(new TestSuite(ProjectCreationDecorator.class));
		// Tests included in the automated suite
		addTest(new TestSuite(Java8Tests.class));
		addTest(new TestSuite(TestsOperators1.class));
		addTest(new TestSuite(TestsOperators2.class));
		addTest(new TestSuite(TestsArrays.class));
		addTest(new TestSuite(TestsNestedTypes1.class));
		addTest(new TestSuite(TestsNestedTypes2.class));
		addTest(new TestSuite(TestsTypeHierarchy1.class));
		addTest(new TestSuite(TestsTypeHierarchy2.class));

		// Extended evaluation tests
		addTest(new TestSuite(BooleanOperatorsTests.class));
		addTest(new TestSuite(ByteOperatorsTests.class));
		addTest(new TestSuite(CharOperatorsTests.class));
		addTest(new TestSuite(ShortOperatorsTests.class));
		addTest(new TestSuite(IntOperatorsTests.class));
		addTest(new TestSuite(LongOperatorsTests.class));
		addTest(new TestSuite(FloatOperatorsTests.class));
		addTest(new TestSuite(DoubleOperatorsTests.class));
		addTest(new TestSuite(StringPlusOpTests.class));

		addTest(new TestSuite(LocalVarValueTests.class));
		addTest(new TestSuite(LocalVarAssignmentTests.class));

		addTest(new TestSuite(BooleanAssignmentOperatorsTests.class));
		addTest(new TestSuite(ByteAssignmentOperatorsTests.class));
		addTest(new TestSuite(CharAssignmentOperatorsTests.class));
		addTest(new TestSuite(ShortAssignmentOperatorsTests.class));
		addTest(new TestSuite(IntAssignmentOperatorsTests.class));
		addTest(new TestSuite(LongAssignmentOperatorsTests.class));
		addTest(new TestSuite(FloatAssignmentOperatorsTests.class));
		addTest(new TestSuite(DoubleAssignmentOperatorsTests.class));
		addTest(new TestSuite(StringPlusAssignmentOpTests.class));

		addTest(new TestSuite(XfixOperatorsTests.class));

		addTest(new TestSuite(NumericTypesCastTests.class));

		addTest(new TestSuite(FieldValueTests.class));
		addTest(new TestSuite(QualifiedFieldValueTests.class));
		addTest(new TestSuite(StaticFieldValueTests.class));
		addTest(new TestSuite(StaticFieldValueTests2.class));
		addTest(new TestSuite(QualifiedStaticFieldValueTests.class));
		addTest(new TestSuite(QualifiedStaticFieldValueTests2.class));

		addTest(new TestSuite(ArrayAllocationTests.class));
		addTest(new TestSuite(ArrayAssignmentTests.class));
		addTest(new TestSuite(ArrayValueTests.class));

		addTest(new TestSuite(NestedTypeFieldValue_65.class));
		addTest(new TestSuite(NestedTypeFieldValue_69.class));
		addTest(new TestSuite(NestedTypeFieldValue_94.class));
		addTest(new TestSuite(NestedTypeFieldValue_120.class));
		addTest(new TestSuite(NestedTypeFieldValue_145.class));
		addTest(new TestSuite(NestedTypeFieldValue_155.class));
		addTest(new TestSuite(NestedTypeFieldValue_179.class));
		addTest(new TestSuite(NestedTypeFieldValue_203.class));
		addTest(new TestSuite(NestedTypeFieldValue_214.class));
		addTest(new TestSuite(NestedTypeFieldValue_252.class));
		addTest(new TestSuite(NestedTypeFieldValue_279.class));
		addTest(new TestSuite(NestedTypeFieldValue_304.class));
		addTest(new TestSuite(NestedTypeFieldValue_315.class));
		addTest(new TestSuite(NestedTypeFieldValue_354.class));
		addTest(new TestSuite(NestedTypeFieldValue_381.class));
		addTest(new TestSuite(NestedTypeFieldValue_406.class));
		addTest(new TestSuite(NestedTypeFieldValue_417.class));
		addTest(new TestSuite(NestedTypeFieldValue_455.class));
		addTest(new TestSuite(NestedTypeFieldValue_481.class));
		addTest(new TestSuite(NestedTypeFieldValue_506.class));
		addTest(new TestSuite(NestedTypeFieldValue_517.class));
		addTest(new TestSuite(NestedTypeFieldValue_529.class));
		addTest(new TestSuite(NestedTypeFieldValue_566.class));
		addTest(new TestSuite(NestedTypeFieldValue_592.class));
		addTest(new TestSuite(NestedTypeFieldValue_616.class));
//		addTest(new TestSuite(NestedTypeFieldValue_626.class));
//		addTest(new TestSuite(NestedTypeFieldValue_664.class));
		addTest(new TestSuite(NestedTypeFieldValue_690.class));
		addTest(new TestSuite(NestedTypeFieldValue_714.class));
		addTest(new TestSuite(NestedTypeFieldValue_724.class));
		addTest(new TestSuite(NestedTypeFieldValue_739.class));

		addTest(new TestSuite(TypeHierarchy_32_1.class));
		addTest(new TestSuite(TypeHierarchy_32_2.class));
		addTest(new TestSuite(TypeHierarchy_32_3.class));
		addTest(new TestSuite(TypeHierarchy_32_4.class));
		addTest(new TestSuite(TypeHierarchy_32_5.class));
		addTest(new TestSuite(TypeHierarchy_32_6.class));
		addTest(new TestSuite(TypeHierarchy_68_1.class));
		addTest(new TestSuite(TypeHierarchy_68_2.class));
		addTest(new TestSuite(TypeHierarchy_68_3.class));
		addTest(new TestSuite(TypeHierarchy_119_1.class));
		addTest(new TestSuite(TypeHierarchy_146_1.class));

		addTest(new TestSuite(TestsNumberLiteral.class));

		addTest(new TestSuite(VariableDeclarationTests.class));
		addTest(new TestSuite(LoopTests.class));
		addTest(new TestSuite(LabelTests.class));
		addTest(new TestSuite(TestsAnonymousClassVariable.class));

		addTest(new TestSuite(TestsBreakpointConditions.class));

	}

	/**
	 * Runs the tests and collects their result in a TestResult.
	 * The debug tests cannot be run in the UI thread or the event
	 * waiter blocks the UI when a resource changes.
	 * @see junit.framework.TestSuite#run(junit.framework.TestResult)
	 */
	@Override
	public void run(final TestResult result) {
		final Display display = Display.getCurrent();
		Thread thread = null;
		try {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					for (Enumeration<Test> e= tests(); e.hasMoreElements(); ) {
				  		if (result.shouldStop() ) {
							break;
						}
						runTest(e.nextElement(), result);
					}
					fTesting = false;
					display.wake();
				}
			};
			thread = new Thread(r);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (fTesting) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}

