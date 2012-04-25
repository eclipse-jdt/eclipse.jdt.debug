/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.PListParser;

/**
 * Tests the PList Parser.
 */
public class PListParserTests extends AbstractDebugTest {

	/**
	 * Constructs a test
	 * 
	 * @param name test name
	 */
	public PListParserTests(String name) {
		super(name);
	}
	
	/**
	 * Tests parsing of a sample installed JREs plist from the Mac.
	 * 
	 * @throws Exception
	 */
	public void testParseJREs() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/plist.xml"));
		assertNotNull(file);
		assertEquals(true, file.exists());
		Object obj = new PListParser().parse(new FileInputStream(file));
		if (obj instanceof Object[]) {
			Object[] jres = (Object[]) obj;
			assertEquals("Should be 3 entries in the array", 3, jres.length);
			// the first map
			HashMap<String, Comparable<?>> map = new HashMap<String, Comparable<?>>();
			map.put("JVMArch", "i386");
			map.put("JVMBundleID", "com.apple.javajdk15");
			map.put("JVMEnabled", Boolean.TRUE);
			map.put("JVMHomePath", "/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home");
			map.put("JVMIsBuiltIn", Boolean.TRUE);
			map.put("JVMName", "J2SE 5.0");
			map.put("JVMPlatformVersion", "1.5");
			map.put("JVMVersion", "1.5.0_24");
			map.put("test", Boolean.FALSE);
			map.put("testint", new Integer(42));
			assertEquals("Incorrect values parsed", map, jres[0]);
			
			map = new HashMap<String, Comparable<?>>();
			map.put("JVMArch", "x86_64");
			map.put("JVMBundleID", "com.apple.javajdk16");
			map.put("JVMEnabled", Boolean.TRUE);
			map.put("JVMHomePath", "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home");
			map.put("JVMIsBuiltIn", Boolean.TRUE);
			map.put("JVMName", "Java SE 6");
			map.put("JVMPlatformVersion", "1.6");
			map.put("JVMVersion", "1.6.0_20");
			assertEquals("Incorrect values parsed", map, jres[1]);
			
			map = new HashMap<String, Comparable<?>>();
			map.put("JVMArch", "x86_64");
			map.put("JVMBundleID", "com.apple.javajdk15");
			map.put("JVMEnabled", Boolean.TRUE);
			map.put("JVMHomePath", "/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home");
			map.put("JVMIsBuiltIn", Boolean.TRUE);
			map.put("JVMName", "J2SE 5.0");
			map.put("JVMPlatformVersion", "1.5");
			map.put("JVMVersion", "1.5.0_24");
			assertEquals("Incorrect values parsed", map, jres[2]);
		} else {
			assertTrue("Top level object should be an array", false);
		}
	}

	public void testParseLionJREs() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/plist-lion.xml"));
		assertNotNull(file);
		assertEquals(true, file.exists());
		Object obj = new PListParser().parse(new FileInputStream(file));
		if (obj instanceof Object[]) {
			Object[] jres = (Object[]) obj;
			assertEquals("Should be 8 entries in the array", 8, jres.length);
			
		} else {
			assertTrue("Top level object should be an array", false);
		}
	}
	
	public void testParseSnowLeopardJREs() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/plist-snowleopard.xml"));
		assertNotNull(file);
		assertEquals(true, file.exists());
		Object obj = new PListParser().parse(new FileInputStream(file));
		if (obj instanceof Object[]) {
			Object[] jres = (Object[]) obj;
			assertEquals("Should be 2 entries in the array", 2, jres.length);
			
		} else {
			assertTrue("Top level object should be an array", false);
		}
	}
}
