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
package org.eclipse.jdt.debug.tests;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.eclipse.jdt.debug.testplugin.TestPluginLauncher;
import org.eclipse.jdt.debug.tests.eval.ArrayAllocationTests;
import org.eclipse.jdt.debug.tests.eval.ArrayAssignementTests;
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
import org.eclipse.jdt.debug.tests.eval.LabelTests;
import org.eclipse.jdt.debug.tests.eval.LocalVarAssignmentTests;
import org.eclipse.jdt.debug.tests.eval.LocalVarValueTests;
import org.eclipse.jdt.debug.tests.eval.LongAssignmentOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.LongOperatorsTests;
import org.eclipse.jdt.debug.tests.eval.LoopTests;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_109;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_134;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_144;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_168;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_192;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_203;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_241;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_268;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_293;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_304;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_343;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_370;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_395;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_406;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_444;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_470;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_495;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_506;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_518;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_54;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_555;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_58;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_581;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_605;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_615;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_653;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_679;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_703;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_713;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_728;
import org.eclipse.jdt.debug.tests.eval.NestedTypeFieldValue_83;
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
import org.eclipse.jdt.debug.tests.eval.TestsArrays;
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
		addTest(new TestSuite(ArrayAssignementTests.class));
		addTest(new TestSuite(ArrayValueTests.class));

		addTest(new TestSuite(NestedTypeFieldValue_54.class));
		addTest(new TestSuite(NestedTypeFieldValue_58.class));
		addTest(new TestSuite(NestedTypeFieldValue_83.class));
		addTest(new TestSuite(NestedTypeFieldValue_109.class));
		addTest(new TestSuite(NestedTypeFieldValue_134.class));
		addTest(new TestSuite(NestedTypeFieldValue_144.class));
		addTest(new TestSuite(NestedTypeFieldValue_168.class));
		addTest(new TestSuite(NestedTypeFieldValue_192.class));
		addTest(new TestSuite(NestedTypeFieldValue_203.class));
		addTest(new TestSuite(NestedTypeFieldValue_241.class));
		addTest(new TestSuite(NestedTypeFieldValue_268.class));
		addTest(new TestSuite(NestedTypeFieldValue_293.class));
		addTest(new TestSuite(NestedTypeFieldValue_304.class));
		addTest(new TestSuite(NestedTypeFieldValue_343.class));
		addTest(new TestSuite(NestedTypeFieldValue_370.class));
		addTest(new TestSuite(NestedTypeFieldValue_395.class));
		addTest(new TestSuite(NestedTypeFieldValue_406.class));
		addTest(new TestSuite(NestedTypeFieldValue_444.class));
		addTest(new TestSuite(NestedTypeFieldValue_470.class));
		addTest(new TestSuite(NestedTypeFieldValue_495.class));
		addTest(new TestSuite(NestedTypeFieldValue_506.class));
		addTest(new TestSuite(NestedTypeFieldValue_518.class));
		addTest(new TestSuite(NestedTypeFieldValue_555.class));
		addTest(new TestSuite(NestedTypeFieldValue_581.class));
		addTest(new TestSuite(NestedTypeFieldValue_605.class));
		addTest(new TestSuite(NestedTypeFieldValue_615.class));
		addTest(new TestSuite(NestedTypeFieldValue_653.class));
		addTest(new TestSuite(NestedTypeFieldValue_679.class));
		addTest(new TestSuite(NestedTypeFieldValue_703.class));
		addTest(new TestSuite(NestedTypeFieldValue_713.class));
		addTest(new TestSuite(NestedTypeFieldValue_728.class));

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
		

	}
	
	public static void main(String[] args) {
		TestPluginLauncher.run(TestPluginLauncher.getLocationFromProperties(), EvalTestSuite.class, args);
	}		

	/**
	 * Runs the tests and collects their result in a TestResult.
	 * The debug tests cannot be run in the UI thread or the event
	 * waiter blocks the UI when a resource changes.
	 */
	public void run(final TestResult result) {
		final Display display = Display.getCurrent();
		Thread thread = null;
		try {
			Runnable r = new Runnable() {
				public void run() {
					for (Enumeration e= tests(); e.hasMoreElements(); ) {
				  		if (result.shouldStop() )
				  			break;
						runTest((Test)e.nextElement(), result);
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
				if (!display.readAndDispatch())
					display.sleep();
			} catch (Throwable e) {
				e.printStackTrace();
			}			
		}		
	}

}

