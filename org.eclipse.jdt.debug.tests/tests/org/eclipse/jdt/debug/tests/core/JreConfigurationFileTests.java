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
import org.eclipse.jdt.internal.launching.CompositeId;
import org.eclipse.jdt.internal.launching.JreConfigurationFile;
import org.eclipse.jdt.internal.launching.JreConfigurationFile.ContributedVM;
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
		String javadoc = "https://example.org/jre-config/api/";

		// a VM at a location that does not exist must be skipped
		JreConfigurationFile config = JreConfigurationFile.parse("""
				{ "java.configuration.runtimes": [
				    { "label": "configured", "path": "%s", "sources": "%s", "javadoc": "%s", "vendor": "Test", "default": true },
				    { "label": "missing", "path": "%s" }
				] }
				""".formatted(home, json(sources.getAbsolutePath()), javadoc, json(new File(javaHome(), "does-not-exist").getAbsolutePath())), "test");
		assertTrue(config.addVMs(container));
		assertEquals(1, container.getVMList().size());
		IVMInstall vm = container.getVMList().get(0);
		assertTrue(vm instanceof VMStandin);
		assertEquals("configured", vm.getName());
		assertEquals(javaHome().getAbsoluteFile(), vm.getInstallLocation());
		assertEquals("Test", ((VMStandin) vm).getAttribute(JreConfigurationFile.ATTRIBUTE_VENDOR));
		assertEquals(javadoc, vm.getJavadocLocation().toExternalForm());
		assertEquals(JavaRuntime.getCompositeIdFromVM(vm), container.getDefaultVMInstallCompositeID());
		LibraryLocation[] libraries = vm.getLibraryLocations();
		assertNotNull(libraries);
		assertTrue(libraries.length > 0);
		IPath expectedSource = IPath.fromOSString(sources.getAbsolutePath());
		for (LibraryLocation library : libraries) {
			assertEquals(expectedSource, library.getSystemLibrarySourcePath());
			// the configured javadoc location must also be applied to the explicit library locations
			assertEquals(javadoc, library.getJavadocLocation().toExternalForm());
		}

		// applying the same configuration again must not change anything
		assertFalse(config.addVMs(container));
		assertEquals(1, container.getVMList().size());

		// an existing VM at the same location is updated in place, never duplicated; the rename is deferred
		// until real VMs exist so that the regular change notification can migrate references
		VMDefinitionsContainer existing = new VMDefinitionsContainer();
		VMStandin persisted = new VMStandin(standardType, "persisted-id");
		persisted.setName("old name");
		persisted.setInstallLocation(javaHome());
		existing.addVM(persisted);
		assertTrue(config.addVMs(existing));
		assertEquals(1, existing.getVMList().size());
		assertSame(persisted, existing.getVMList().get(0));
		assertEquals("persisted-id", persisted.getId());
		assertEquals("old name", persisted.getName());
		assertEquals("Test", persisted.getAttribute(JreConfigurationFile.ATTRIBUTE_VENDOR));
	}

	public void testAddVMsAvoidsDuplicateNames() throws Exception {
		IVMInstallType standardType = JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);
		VMDefinitionsContainer container = new VMDefinitionsContainer();
		VMStandin other = new VMStandin(standardType, "other-id");
		other.setName("configured");
		other.setInstallLocation(new File(javaHome(), "does-not-exist"));
		container.addVM(other);

		JreConfigurationFile config = JreConfigurationFile.parse("""
				{ "java.configuration.runtimes": [ { "label": "configured", "path": "%s" } ] }
				""".formatted(json(javaHome().getAbsolutePath())), "test");
		assertTrue(config.addVMs(container));
		assertEquals(2, container.getVMList().size());
		assertEquals("configured", other.getName());
		IVMInstall added = container.getVMList().get(1);
		assertEquals(javaHome().getAbsoluteFile(), added.getInstallLocation());
		assertEquals("configured (2)", added.getName());
	}

	public void testJavadocAppliedToExplicitLibraries() throws Exception {
		IVMInstallType standardType = JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);
		VMDefinitionsContainer container = new VMDefinitionsContainer();
		VMStandin existing = new VMStandin(standardType, "explicit-libs-id");
		existing.setName("explicit");
		existing.setInstallLocation(javaHome());
		LibraryLocation[] defaults = standardType.getDefaultLibraryLocations(javaHome());
		assertTrue(defaults.length > 0);
		// explicit library locations: defaults with a custom source attachment on the first library
		File customSource = File.createTempFile("jre-config-explicit-sources", ".zip");
		customSource.deleteOnExit();
		LibraryLocation[] explicit = defaults.clone();
		LibraryLocation first = defaults[0];
		explicit[0] = new LibraryLocation(first.getSystemLibraryPath(), IPath.fromOSString(customSource.getAbsolutePath()), first.getPackageRootPath(), first.getJavadocLocation(), first.getIndexLocation(), first.getExternalAnnotationsPath());
		existing.setLibraryLocations(explicit);
		assertNotNull(existing.getLibraryLocations());
		container.addVM(existing);

		String javadoc = "https://example.org/explicit/api/";
		JreConfigurationFile config = JreConfigurationFile.parse("""
				{ "java.configuration.runtimes": [ { "label": "explicit", "path": "%s", "javadoc": "%s" } ] }
				""".formatted(json(javaHome().getAbsolutePath()), javadoc), "test");
		assertTrue(config.addVMs(container));
		LibraryLocation[] libraries = existing.getLibraryLocations();
		assertEquals(explicit.length, libraries.length);
		for (int i = 0; i < libraries.length; i++) {
			assertEquals(javadoc, libraries[i].getJavadocLocation().toExternalForm());
			// source attachments are preserved when only the javadoc location changes
			assertEquals(explicit[i].getSystemLibrarySourcePath(), libraries[i].getSystemLibrarySourcePath());
		}
		assertFalse(config.addVMs(container));
	}

	public void testApplyPendingRenamesRenamesRealVM() throws Exception {
		IVMInstallType standardType = JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);
		VMStandin standin = new VMStandin(standardType, "jre-config-rename-id");
		standin.setName("jre-config old name");
		standin.setInstallLocation(javaHome());
		IVMInstall real = standin.convertToRealVM();
		try {
			VMDefinitionsContainer container = new VMDefinitionsContainer();
			container.addVM(new VMStandin(real));
			JreConfigurationFile config = JreConfigurationFile.parse("""
					{ "java.configuration.runtimes": [ { "label": "jre-config new name", "path": "%s" } ] }
					""".formatted(json(javaHome().getAbsolutePath())), "test");
			// nothing changes in the container, the rename is only recorded
			assertFalse(config.addVMs(container));
			assertEquals("jre-config old name", real.getName());
			config.applyPendingRenames();
			assertEquals("jre-config new name", real.getName());
			// renames are applied only once
			real.setName("jre-config old name");
			config.applyPendingRenames();
			assertEquals("jre-config old name", real.getName());
		} finally {
			standardType.disposeVMInstall(real.getId());
			JavaRuntime.saveVMConfiguration();
		}
	}

	public void testInvalidLabelIsSkipped() throws Exception {
		VMDefinitionsContainer container = new VMDefinitionsContainer();
		JreConfigurationFile config = JreConfigurationFile.parse("""
				{ "java.configuration.runtimes": [
				    { "label": "Temurin/JDK21", "path": "%s", "default": true },
				    { "label": "valid", "path": "%s" }
				] }
				""".formatted(json(javaHome().getAbsolutePath()), json(javaHome().getAbsolutePath())), "test");
		assertTrue(config.addVMs(container));
		assertEquals(1, container.getVMList().size());
		assertEquals("valid", container.getVMList().get(0).getName());
		assertNull(container.getDefaultVMInstallCompositeID());
		assertFalse(JreConfigurationFile.validateName("a\\b").isOK());
		assertFalse(JreConfigurationFile.validateName(" ").isOK());
		assertTrue(JreConfigurationFile.validateName("[JDK 21] Temurin 21.0.6+7-LTS").isOK());
	}

	public void testContributedVMsAreLeftToExtension() throws Exception {
		IVMInstallType standardType = JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);
		String home = json(javaHome().getAbsolutePath());
		File otherHome = new File(javaHome(), "does-not-exist");
		List<ContributedVM> contributed = List.of(new ContributedVM("contributed-id", "Contributed JDK", javaHome()));
		String contributedCompositeId = new CompositeId(new String[] { standardType.getId(), "contributed-id" }).toString();

		// fresh workspace: a configured runtime at a contributed location adds no stand-in but may become the default
		VMDefinitionsContainer fresh = new VMDefinitionsContainer();
		JreConfigurationFile config = JreConfigurationFile.parse("""
				{ "java.configuration.runtimes": [ { "label": "configured", "path": "%s", "default": true } ] }
				""".formatted(home), "test");
		assertTrue(config.addVMs(fresh, standardType, contributed));
		assertTrue(fresh.getVMList().isEmpty());
		assertEquals(contributedCompositeId, fresh.getDefaultVMInstallCompositeID());

		// persisted VM at a contributed location: not configured, not renamed, default flag honored for the contribution
		VMDefinitionsContainer persisted = new VMDefinitionsContainer();
		VMStandin existing = new VMStandin(standardType, "contributed-id");
		existing.setName("Contributed JDK");
		existing.setInstallLocation(javaHome());
		persisted.addVM(existing);
		IVMInstall real = existing.convertToRealVM();
		try {
			config = JreConfigurationFile.parse("""
					{ "java.configuration.runtimes": [ { "label": "renamed", "path": "%s", "vendor": "Test", "default": true } ] }
					""".formatted(home), "test");
			assertTrue(config.addVMs(persisted, standardType, contributed));
			assertEquals(1, persisted.getVMList().size());
			assertEquals("Contributed JDK", existing.getName());
			assertNull(existing.getAttribute(JreConfigurationFile.ATTRIBUTE_VENDOR));
			assertEquals(contributedCompositeId, persisted.getDefaultVMInstallCompositeID());
			config.applyPendingRenames();
			assertEquals("Contributed JDK", real.getName());
		} finally {
			standardType.disposeVMInstall(real.getId());
			JavaRuntime.saveVMConfiguration();
		}

		// a configured runtime at another location must not take the name of a contributed VM
		List<ContributedVM> contributedElsewhere = List.of(new ContributedVM("contributed-id", "Contributed JDK", otherHome));
		config = JreConfigurationFile.parse("""
				{ "java.configuration.runtimes": [ { "label": "Contributed JDK", "path": "%s" } ] }
				""".formatted(home), "test");
		VMDefinitionsContainer added = new VMDefinitionsContainer();
		assertTrue(config.addVMs(added, standardType, contributedElsewhere));
		assertEquals(1, added.getVMList().size());
		assertEquals("Contributed JDK (2)", added.getVMList().get(0).getName());
		VMDefinitionsContainer renamed = new VMDefinitionsContainer();
		VMStandin elsewhere = new VMStandin(standardType, "elsewhere-id");
		elsewhere.setName("elsewhere");
		elsewhere.setInstallLocation(javaHome());
		renamed.addVM(elsewhere);
		assertFalse(config.addVMs(renamed, standardType, contributedElsewhere));
		config.applyPendingRenames();
		assertEquals("elsewhere", elsewhere.getName());
	}
}
