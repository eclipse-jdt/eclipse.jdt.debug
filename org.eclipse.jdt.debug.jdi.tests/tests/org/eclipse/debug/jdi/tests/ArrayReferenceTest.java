/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

package org.eclipse.debug.jdi.tests;

import java.util.List;
import java.util.ListIterator;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

/**
 * Tests for JDI com.sun.jdi.ArrayReference
 * and JDWP Array command set.
 */
public class ArrayReferenceTest extends AbstractJDITest {

	private ArrayReference fArray;
	private ArrayReference fDoubleArray;

	/**
	 * Creates a new test.
	 */
	public ArrayReferenceTest() {
		super();
	}
	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Get array references
		fArray = getObjectArrayReference();
		fDoubleArray = getNonEmptyDoubleArrayReference();
	}
	/**
	 * Run all tests and output to standard output.
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		new ArrayReferenceTest().runSuite(args);
	}
	/**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "com.sun.jdi.ArrayReference";
	}
	/**
	 * Test JDI getValue(int), getValues(), getValues(int,int)
	 * setValue(Value,int), setValues(List) and setValues(int,List,int,int),
	 * and JDWP 'Array - Get values' and 'Array - Set values'.
	 */
	public void testJDIGetSetDoubleValue() {
		double one = 1.0;
		double pi = 3.1415926535;
		double twos = 2.2;
		double threes = 3.33;
		double cnt = 12345;
		double zero = 0.0;
		double delta = 0.0;

		DoubleValue dbl = (DoubleValue) fDoubleArray.getValue(0);
		assertEquals("testJDIGetSetDoubleValue.1", one, dbl.value(), delta);

		DoubleValue piValue = fVM.mirrorOf(pi);
		DoubleValue cntValue = fVM.mirrorOf(cnt);
		DoubleValue zeroValue = fVM.mirrorOf(zero);
		try {
			fDoubleArray.setValue(0, piValue);
		} catch (ClassNotLoadedException e) {
			assertTrue("testJDIGetSetDoubleValue.3.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("testJDIGetSetDoubleValue.3.2", false);
		}
		DoubleValue value = (DoubleValue) fDoubleArray.getValue(0);
		assertEquals("testJDIGetSetDoubleValue.4.1", value, piValue);
		assertEquals("testJDIGetSetDoubleValue.4.2", pi, value.value(), delta);

		// getValues()
		List<Value> values = fDoubleArray.getValues();
		double[] expected = new double[] { pi, twos, threes };
		ListIterator<Value> iterator = values.listIterator();
		while (iterator.hasNext()) {
			DoubleValue dv = (DoubleValue) iterator.next();
			boolean included = false;
			for (int i = 0; i < expected.length; i++) {
				if (dv.value() == expected[i]) {
					included = true;
					break;
				}
			}
			assertTrue("testJDIGetSetDoubleValue.5." + dv.value(), included);
		}

		// setValues(List)
		List<Value> newValues = values;
		newValues.set(1, cntValue);
		try {
			fDoubleArray.setValues(newValues);
		} catch (ClassNotLoadedException e) {
			assertTrue("testJDIGetSetDoubleValue.7.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("testJDIGetSetDoubleValue.7.2", false);
		}
		values = fDoubleArray.getValues();
		assertEquals("testJDIGetSetDoubleValue.8", values, newValues);

		// getValues(int,int)
		values = fDoubleArray.getValues(1, 2);
		expected = new double[] { cnt, threes };
		iterator = values.listIterator();
		while (iterator.hasNext()) {
			DoubleValue dv = (DoubleValue) iterator.next();
			boolean included = false;
			for (int i = 0; i < expected.length; i++) {
				if (dv.value() == expected[i]) {
					included = true;
					break;
				}
			}
			assertTrue("testJDIGetSetDoubleValue.9." + dv.value(), included);
		}

		// setValues(int,List,int,int)
		newValues = fDoubleArray.getValues(0, 2);
		newValues.set(0, zeroValue);
		try {
			fDoubleArray.setValues(0, newValues, 0, 2);
		} catch (ClassNotLoadedException e) {
			assertTrue("testJDIGetSetDoubleValue.11.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("testJDIGetSetDoubleValue.11.2", false);
		}
		values = fDoubleArray.getValues(0, 2);
		assertEquals("testJDIGetSetDoubleValue.12", values, newValues);
	}
	/**
	 * Test JDI getValue(int), getValues(), getValues(int,int)
	 * setValue(Value,int), setValues(List) and setValues(int,List,int,int),
	 * and JDWP 'Array - Get values' and 'Array - Set values'.
	 */
	public void testJDIGetSetValue() {
		// getValue(int)
		StringReference string = (StringReference) fArray.getValue(0);
		assertEquals("1", "foo", string.value());

		// setValue(int,Value)
		StringReference newValue = null;
		newValue = fVM.mirrorOf("biz");
		try {
			fArray.setValue(0, newValue);
		} catch (ClassNotLoadedException e) {
			assertTrue("2.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("2.2", false);
		}
		StringReference value = (StringReference) fArray.getValue(0);
		assertEquals("3", value, newValue);

		// getValues()
		List<Value> values = fArray.getValues();
		String[] expected = new String[] { "biz", "bar", "hop" };
		ListIterator<Value> iterator = values.listIterator();
		while (iterator.hasNext()) {
			StringReference ref = (StringReference) iterator.next();
			boolean included = false;
			for (int i = 0; i < expected.length; i++) {
				if (ref.value().equals(expected[i])) {
					included = true;
					break;
				}
			}
			assertTrue("4." + ref.value(), included);
		}

		// setValues(List)
		List<Value> newValues = values;
		newValue = fVM.mirrorOf("hip");
		newValues.set(1, newValue);
		try {
			fArray.setValues(newValues);
		} catch (ClassNotLoadedException e) {
			assertTrue("5.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("6.2", false);
		}
		values = fArray.getValues();
		assertEquals("7", values, newValues);

		// getValues(int,int)
		values = fArray.getValues(1, 2);
		expected = new String[] { "hip", "hop" };
		iterator = values.listIterator();
		while (iterator.hasNext()) {
			StringReference ref = (StringReference) iterator.next();
			boolean included = false;
			for (int i = 0; i < expected.length; i++) {
				if (ref.value().equals(expected[i])) {
					included = true;
					break;
				}
			}
			assertTrue("8." + ref.value(), included);
		}

		// setValues(int,List,int,int)
		newValues = fArray.getValues(0, 2);
		newValue = fVM.mirrorOf("rap");
		newValues.set(0, newValue);
		try {
			fArray.setValues(0, newValues, 0, 2);
		} catch (ClassNotLoadedException e) {
			assertTrue("9.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("9.2", false);
		}
		values = fArray.getValues(0, 2);
		assertEquals("10", values, newValues);

		// test null value
		newValues.set(0, null);
		try {
			fArray.setValues(0, newValues, 0, 2);
		} catch (ClassNotLoadedException e) {
			assertTrue("11.1", false);
		} catch (InvalidTypeException e) {
			assertTrue("11.2", false);
		}
		values = fArray.getValues(0, 2);
		assertEquals("12", values.get(0), null);
	}
	/**
	 * Test JDI length() and JDWP 'Array - Get length'.
	 */
	public void testJDILength() {
		int length = fArray.length();
		assertEquals("1", 3, length);
	}
}
