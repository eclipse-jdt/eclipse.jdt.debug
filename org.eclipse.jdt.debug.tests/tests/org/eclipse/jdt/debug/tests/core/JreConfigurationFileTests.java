/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import static org.junit.Assert.assertThrows;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JreConfigurationFile;
import org.eclipse.jdt.internal.launching.JreConfigurationFile.EnvironmentDefault;
import org.eclipse.jdt.internal.launching.JreConfigurationFile.Runtime;
import org.eclipse.jdt.internal.launching.SimpleJsonParser;
import org.eclipse.jdt.internal.launching.SimpleJsonParser.JsonException;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.internal.launching.VMDefinitionsContainer;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;

/**
 * Tests for the JSON based JRE configuration file ({@link JreConfigurationFile}).
 */
public class JreConfigurationFileTests extends AbstractDebugTest {

	public JreConfigurationFileTests(String name) {
		super(name);
	}

	private static String json(String path) {
		return path.replace("\\", "\\\\");
	}

	private static File javaHome() {
		return new File(System.getProperty("java.home"));
	}

	public void testParseJson() throws Exception {
		Object value = SimpleJsonParser.parse("""
				// comment
				{
				  "a": [1, 2.5, "x\\u0041\\n", true, false, null,], /* block */
				  "b": { "c": -3 },
				}
				""");
		assertTrue(value instanceof Map);
		Map<?, ?> object = (Map<?, ?>) value;
		List<?> a = (List<?>) object.get("a");
		assertEquals(List.of(1L, 2.5d, "xA\n", true, false), a.subList(0, 5));
		assertNull(a.get(5));
		assertEquals(-3L, ((Map<?, ?>) object.get("b")).get("c"));
	}

	public void testParseJsonErrors() {
		assertThrows(JsonException.class, () -> SimpleJsonParser.parse("{"));
		assertThrows(JsonException.class, () -> SimpleJsonParser.parse("{\"a\" 1}"));
		assertThrows(JsonException.class, () -> SimpleJsonParser.parse("[1] x"));
		assertThrows(JsonException.class, () -> SimpleJsonParser.parse("\"unterminated"));
	}

	public void testParseConfiguration() throws Exception {
		String home = json(javaHome().getAbsolutePath());
		String text = """
				{
				  "java.configuration.runtimes": [
				    {
				      "label": "[JDK 21] Temurin 21.0.6+7-LTS",
				      "vendor": "Temurin",
				      "version": "JavaSE-21",
				      "path": "%s",
				      "sources": "%s/lib/src.zip",
				      "javadoc": "https://docs.oracle.com/en/java/javase/21/docs/api/",
				      "vmArgs": "-Xmx1g",
				      "default": true
				    },
				    { "label": "minimal", "path": "%s" }
				  ],
				  "java.configuration.ee": [
				    { "ee": "JavaSE-21", "path": "%s" }
				  ]
				}
				""".formatted(home, home, home, home);
		JreConfigurationFile config = JreConfigurationFile.parse(text, "test");
		assertEquals("test", config.getOrigin());
		assertEquals(2, config.getRuntimes().size());
		Runtime runtime = config.getRuntimes().get(0);
		assertEquals("[JDK 21] Temurin 21.0.6+7-LTS", runtime.label());
		assertEquals("Temurin", runtime.vendor());
		assertEquals("JavaSE-21", runtime.version());
		assertEquals(javaHome().getAbsoluteFile(), runtime.path());
		assertEquals(new File(javaHome(), "lib/src.zip").getAbsoluteFile(), runtime.sources());
		assertEquals("https://docs.oracle.com/en/java/javase/21/docs/api/", runtime.javadoc().toExternalForm());
		assertEquals("-Xmx1g", runtime.vmArgs());
		assertTrue(runtime.isDefault());
		Runtime minimal = config.getRuntimes().get(1);
		assertEquals("minimal", minimal.label());
		assertNull(minimal.vendor());
		assertNull(minimal.sources());
		assertFalse(minimal.isDefault());
		assertEquals(1, config.getEnvironmentDefaults().size());
		EnvironmentDefault ee = config.getEnvironmentDefaults().get(0);
		assertEquals("JavaSE-21", ee.environmentId());
		assertEquals(javaHome().getAbsoluteFile(), ee.path());
	}

	public void testParseConfigurationWithoutKeysIsEmpty() throws Exception {
		JreConfigurationFile config = JreConfigurationFile.parse("{}", "test");
		assertTrue(config.getRuntimes().isEmpty());
		assertTrue(config.getEnvironmentDefaults().isEmpty());
	}

	public void testParseConfigurationRejectsInvalidStructure() {
		assertThrows(IllegalArgumentException.class, () -> JreConfigurationFile.parse("[]", "test"));
		assertThrows(IllegalArgumentException.class, () -> JreConfigurationFile.parse("{\"java.configuration.runtimes\": {}}", "test"));
		assertThrows(IllegalArgumentException.class, () -> JreConfigurationFile.parse("{\"java.configuration.runtimes\": [{\"label\": \"x\"}]}", "test"));
		assertThrows(IllegalArgumentException.class, () -> JreConfigurationFile.parse("{\"java.configuration.ee\": [{\"ee\": \"JavaSE-21\"}]}", "test"));
	}

	public void testAddVMsCreatesUpdatesAndSetsDefault() throws Exception {
		String home = json(javaHome().getAbsolutePath());
		IVMInstallType standardType = JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);
		VMDefinitionsContainer container = new VMDefinitionsContainer();
		// a source attachment different from the default one forces explicit library locations
		File sources = File.createTempFile("jre-config-sources", ".zip");
		sources.deleteOnExit();

		// a VM at a location that does not exist must be skipped
		JreConfigurationFile config = JreConfigurationFile.parse("""
				{ "java.configuration.runtimes": [
				    { "label": "configured", "path": "%s", "sources": "%s", "vendor": "Test", "default": true },
				    { "label": "missing", "path": "%s" }
				] }
				""".formatted(home, json(sources.getAbsolutePath()), json(new File(javaHome(), "does-not-exist").getAbsolutePath())), "test");
		assertTrue(config.addVMs(container));
		assertEquals(1, container.getVMList().size());
		IVMInstall vm = container.getVMList().get(0);
		assertTrue(vm instanceof VMStandin);
		assertEquals("configured", vm.getName());
		assertEquals(javaHome().getAbsoluteFile(), vm.getInstallLocation());
		assertEquals("Test", ((VMStandin) vm).getAttribute(JreConfigurationFile.ATTRIBUTE_VENDOR));
		assertEquals(JavaRuntime.getCompositeIdFromVM(vm), container.getDefaultVMInstallCompositeID());
		LibraryLocation[] libraries = vm.getLibraryLocations();
		assertNotNull(libraries);
		assertTrue(libraries.length > 0);
		IPath expectedSource = IPath.fromOSString(sources.getAbsolutePath());
		for (LibraryLocation library : libraries) {
			assertEquals(expectedSource, library.getSystemLibrarySourcePath());
		}

		// applying the same configuration again must not change anything
		assertFalse(config.addVMs(container));
		assertEquals(1, container.getVMList().size());

		// an existing VM at the same location is updated in place, never duplicated
		VMDefinitionsContainer existing = new VMDefinitionsContainer();
		VMStandin persisted = new VMStandin(standardType, "persisted-id");
		persisted.setName("old name");
		persisted.setInstallLocation(javaHome());
		existing.addVM(persisted);
		assertTrue(config.addVMs(existing));
		assertEquals(1, existing.getVMList().size());
		assertSame(persisted, existing.getVMList().get(0));
		assertEquals("persisted-id", persisted.getId());
		assertEquals("configured", persisted.getName());
	}
}
