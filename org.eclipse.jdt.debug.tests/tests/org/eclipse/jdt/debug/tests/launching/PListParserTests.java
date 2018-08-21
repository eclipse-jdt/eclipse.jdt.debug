/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.launching;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.MacInstalledJREs;
import org.eclipse.jdt.internal.launching.PListParser;
import org.eclipse.jdt.launching.VMStandin;

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
			HashMap<String, Comparable<?>> map = new HashMap<>();
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

			map = new HashMap<>();
			map.put("JVMArch", "x86_64");
			map.put("JVMBundleID", "com.apple.javajdk16");
			map.put("JVMEnabled", Boolean.TRUE);
			map.put("JVMHomePath", "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home");
			map.put("JVMIsBuiltIn", Boolean.TRUE);
			map.put("JVMName", "Java SE 6");
			map.put("JVMPlatformVersion", "1.6");
			map.put("JVMVersion", "1.6.0_20");
			assertEquals("Incorrect values parsed", map, jres[1]);

			map = new HashMap<>();
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

	/**
	 * Tests that we parse out the correct number of raw entries from the 'lion' plist output
	 *
	 * @throws Exception
	 * @since 3.8
	 */
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

	/**
	 * Tests that we parse out the correct number of raw entries from the 'now leopard' plist output
	 *
	 * @throws Exception
	 * @since 3.8
	 */
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

	/**
	 * Tests that we can parse out certain {@link VMStandin}s from the 'old' style
	 * of plist output.
	 *
	 * @throws Exception
	 * @since 3.8
	 */
	public void testParseJREDescriptors() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/plist.xml"));
		assertNotNull(file);
		assertEquals(true, file.exists());
		VMStandin[] desc = MacInstalledJREs.parseJREInfo(new FileInputStream(file), null);
		assertEquals("There should be 2 JRE descriptions", 2, desc.length);
	}

	/**
	 * Tests that we can parse out certain {@link VMStandin}s from the 'snow leopard' style
	 * of plist output.
	 *
	 * @throws Exception
	 * @since 3.8
	 */
	public void testParseJREDescriptorsSnowLeopard() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/plist-snowleopard.xml"));
		assertNotNull(file);
		assertEquals(true, file.exists());
		VMStandin[] desc = MacInstalledJREs.parseJREInfo(new FileInputStream(file), null);
		assertEquals("There should be 1 JRE description", 1, desc.length);
	}

	/**
	 * Tests that we can parse out certain {@link VMStandin}s from the 'lion' style
	 * of plist output.
	 *
	 * @throws Exception
	 * @since 3.8
	 */
	public void testParseJREDescriptorsLion() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/plist-lion.xml"));
		assertNotNull(file);
		assertEquals(true, file.exists());
		VMStandin[] desc = MacInstalledJREs.parseJREInfo(new FileInputStream(file), null);
		assertEquals("There should be 4 JRE descriptions", 4, desc.length);
	}

	/**
	 * Tests that we can parse out certain {@link VMStandin}s from the plist
	 * output known to be bad - wrong data types.
	 * <br><br>
	 * <code>plist-bad1.xml</code> has a boolean value in place of the VM name for the 1.6 VM,
	 * but we should still recover the remainder of the VMs defined (3 of them)
	 *
	 * @throws Exception
	 * @since 3.8
	 */
	public void testParseJREDescriptorsBad() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/plist-bad1.xml"));
		assertNotNull(file);
		assertEquals(true, file.exists());
		System.out.println("*** EXPECTED SAX EXCEPTION testParseJREDescriptorsBad ***");
		VMStandin[] desc = MacInstalledJREs.parseJREInfo(new FileInputStream(file), null);
		assertEquals("There should be 3 JRE descriptions", 3, desc.length);
	}

	/**
	 * Tests that we can parse out certain {@link VMStandin}s from the plist
	 * output known to be bad - missing element.
	 * <br><br>
	 * <code>plist-bad2.xml</code> is missing a key element - but still has the value for the key.
	 *
	 * @throws Exception
	 * @since 3.8
	 */
	public void testParseJREDescriptorsBad2() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/plist-bad2.xml"));
		assertNotNull(file);
		assertEquals(true, file.exists());
		System.out.println("*** EXPECTED SAX EXCEPTION testParseJREDescriptorsBad2 ***");
		VMStandin[] desc = MacInstalledJREs.parseJREInfo(new FileInputStream(file), null);
		assertEquals("There should be 3 JRE descriptions", 3, desc.length);
	}

	/**
	 * Tests that we can parse out certain {@link VMStandin}s from the plist
	 * output known to be bad - corrupt XML syntax.
	 * <br><br>
	 * <code>plist-bad3.xml</code> has corrupt XML syntax
	 *
	 * @throws Exception
	 * @since 3.8
	 */
	public void testParseJREDescriptorsBad3() throws Exception {
		File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/plist-bad3.xml"));
		assertNotNull(file);
		assertEquals(true, file.exists());
		System.out.println("*** EXPECTED SAX EXCEPTION testParseJREDescriptorsBad3 ***");
		VMStandin[] desc = MacInstalledJREs.parseJREInfo(new FileInputStream(file), null);
		assertEquals("There should be 0 JRE descriptions", 0, desc.length);
	}
}
