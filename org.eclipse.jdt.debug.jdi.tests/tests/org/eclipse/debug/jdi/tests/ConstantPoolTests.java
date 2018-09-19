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

import com.sun.jdi.ArrayType;
import com.sun.jdi.ReferenceType;

/**
 * Test cases for the implementation of providing argument information even if
 * no debugging information is present in the new java 1.6 VM
 *
 * @since 3.3
 */
public class ConstantPoolTests extends AbstractJDITest {

	ReferenceType fClass;

	/** setup test info locally **/
	@Override
	public void localSetUp() {}

	/**
	 * test to see if we can get class file version info from a 1.6 VM, and
	 * that we cannot from a pre-1.6 VM
	 */
	public void testCanGetClassFileVersion() {
		if(is16OrGreater()) {
			assertTrue("Should have classfile version info", fVM.canGetClassFileVersion());
		}
		else {
			assertTrue("Should not have classfile version info", !fVM.canGetClassFileVersion());
		}
	}

	/**
	 * test to make sure we can get constant pool information from a 1.6 VM, and
	 * that we cannot get it from a pre-1.6 VM
	 */
	public void testCanGetConstantPool() {
		if(is16OrGreater()) {
			assertTrue("Should have constant pool info", fVM.canGetConstantPool());
		}
		else {
			assertFalse("Should not have constant pool info", fVM.canGetConstantPool());
		}
	}

	/**
	 * test to make sure that if majorVersion is unsupported an UnsupportedOperationException is
	 * thrown.
	 */
	public void testMajorVersionUnsupported() {
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclassload", true);
		fClass = getClass("org.eclipse.debug.jdi.tests.program.RefClass1");
		assertNotNull("RefClass1 should not be null", fClass);
		if(is16OrGreater()) {
			try {
				fClass.majorVersion();
			}
			catch(UnsupportedOperationException uoe) {
				assertTrue("Threw unsupported exception in 1.6 VM", false);
			}
		}
		else {
			try {
				fClass.majorVersion();
				assertTrue("No exception for non 1.6 VM", false);
			}
			catch(UnsupportedOperationException uoe) {}
		}
	}

	/**
	 * test to make sure that majorVersion returns 0 for an arrayType.
	 * this test does not apply to non-16 VMs
	 */
	public void testMajorVersionArrayType() {
		if(!fVM.canGetClassFileVersion()) {
			return;
		}
		ArrayType type = getArrayType();
		assertNotNull("type should not be null", type);
		int ver = type.majorVersion();
		assertTrue("major verison should be 0", ver == 0);
	}

	/**
	 * test to make sure majorVerison works.
	 * this test does not apply to non-1.6VMs
	 */
	public void testMajorVersion() {
		if(!fVM.canGetClassFileVersion()) {
			return;
		}
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclassload", true);
		fClass = getClass("org.eclipse.debug.jdi.tests.program.RefClass1");
		assertNotNull("RefClass1 should not be null", fClass);
		int ver = fClass.majorVersion();
		assertTrue("version cannot be equal to -1", ver != -1);
	}

	/**
	 * test to make sure that if minorVersion is unsupported an UnsupportedIOperationException
	 * is thrown
	 */
	public void testMinorVersionUnsupported() {
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclassload", true);
		fClass = getClass("org.eclipse.debug.jdi.tests.program.RefClass1");
		assertNotNull("RefClass1 should not be null", fClass);
		if(is16OrGreater()) {
			try {
				fClass.minorVersion();
			}
			catch(UnsupportedOperationException uoe) {
				assertTrue("Threw unsupported exception in 1.6 VM", false);
			}
		}
		else {
			try {
				fClass.minorVersion();
				assertTrue("No exception for non 1.6 VM", false);
			}
			catch(UnsupportedOperationException uoe) {}
		}
	}

	/**
	 * test to make sure minorVerison works.
	 * this test does not apply to non-1.6VMs
	 */
	public void testMinorVersion() {
		if(!fVM.canGetClassFileVersion()) {
			return;
		}
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclassload", true);
		fClass = getClass("org.eclipse.debug.jdi.tests.program.RefClass1");
		assertNotNull("RefClass1 should not be null", fClass);
		int ver = fClass.minorVersion();
		assertTrue("version cannot be equal to -1", ver != -1);
	}

	/**
	 * test to make sure that if constantPoolCount is unsupported an UnsupportedIOperationException
	 * is thrown
	 */
	public void testConstantPoolCountSupported() {
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclassload", true);
		fClass = getClass("org.eclipse.debug.jdi.tests.program.RefClass1");
		assertNotNull("RefClass1 should not be null", fClass);
		if(is16OrGreater()) {
			try {
				fClass.constantPoolCount();
			}
			catch(UnsupportedOperationException uoe) {
				assertTrue("Threw unsupported exception in 1.6 VM", false);
			}
		}
		else {
			try {
				fClass.constantPoolCount();
				assertTrue("No exception for non 1.6 VM", false);
			}
			catch(UnsupportedOperationException uoe) {}
		}
	}

	/**
	 * test to ensure the constant pool count is working correctly
	 * this test does not apply to non-1.6 VMs
	 */
	public void testConstantPoolCount() {
		if(!fVM.canGetConstantPool()) {
			return;
		}
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclass4load", true);
		fClass = getClass("org.eclipse.debug.jdi.tests.program.RefClass4");
		assertNotNull("RefClass4 should not be null", fClass);
		fClass.constantPoolCount();
		//for now we don't care about constant pool counts, not likely to have a useful debug extension for this feature,
		//but it is here for completeness
	}

	/**
	 * test to make sure that if constantPool is unsupported an UnsupportedIOperationException
	 * is thrown
	 */
	public void testConstantPoolSupported() {
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclassload", true);
		fClass = getClass("org.eclipse.debug.jdi.tests.program.RefClass1");
		assertNotNull("RefClass1 should not be null", fClass);
		if(is16OrGreater()) {
			try {
				fClass.constantPool();
			}
			catch(UnsupportedOperationException uoe) {
				assertTrue("Threw unsupported exception in 1.6 VM", false);
			}
		}
		else {
			try {
				fClass.constantPool();
				assertTrue("No exception for non 1.6 VM", false);
			}
			catch(UnsupportedOperationException uoe) {}
		}
	}

	/**
	 * test to ensure the constant pool is working correctly
	 * this test does not apply to non-1.6 VMs
	 */
	public void testConstantPool() {
		if(!fVM.canGetConstantPool()) {
			return;
		}
		triggerAndWait(fVM.eventRequestManager().createClassPrepareRequest(), "refclass4load", true);
		fClass = getClass("org.eclipse.debug.jdi.tests.program.RefClass4");
		assertNotNull("RefClass4 should not be null", fClass);
		byte[] bytes = fClass.constantPool();
		assertNotNull("byte array should not be null", bytes);
		assertTrue("byte array should not be less than 1", bytes.length > 0);
		//for now we don't care about constant pool bytes, not likely to have a useful debug extension for this feature,
		//but it is here for completeness
	}
}
