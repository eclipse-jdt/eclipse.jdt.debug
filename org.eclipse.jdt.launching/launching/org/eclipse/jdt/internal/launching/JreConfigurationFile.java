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
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.internal.launching.SimpleJsonParser.JsonException;

/**
 * Optional, file based configuration of installed JREs and of the default VM per execution environment.
 * <p>
 * The configuration is a JSON (or JSONC) document containing the two optional top level keys
 * <code>java.configuration.runtimes</code> and <code>java.configuration.ee</code>:
 * </p>
 *
 * <pre>
 * {
 *   "java.configuration.runtimes": [
 *     {
 *       "label":   "[JDK 21] Temurin 21.0.6+7-LTS",   // required - name shown in "Installed JREs"
 *       "path":    "C:\\jdks\\temurin-21",            // required - JDK/JRE home, supports ${variable} substitution
 *       "version": "JavaSE-21",                       // optional - execution environment id, fallback EE default
 *       "vendor":  "Temurin",                         // optional - stored as VM attribute "vendor"
 *       "sources": "C:\\jdks\\temurin-21\\lib\\src.zip", // optional - source attachment for all system libraries
 *       "javadoc": "https://docs.oracle.com/en/java/javase/21/docs/api/", // optional
 *       "vmArgs":  "-Xmx2g",                          // optional
 *       "default": true                               // optional - make this the workspace default JRE
 *     }
 *   ],
 *   "java.configuration.ee": [
 *     { "ee": "JavaSE-21", "path": "C:\\jdks\\temurin-21" }
 *   ]
 * }
 * </pre>
 * <p>
 * The file is located using, in order of precedence:
 * </p>
 * <ol>
 * <li>command line argument <code>-jreConfig &lt;file&gt;</code> (also <code>-jreConfig=&lt;file&gt;</code>)</li>
 * <li>system property <code>org.eclipse.jdt.launching.jreConfig</code></li>
 * <li>environment variable <code>JDT_JRE_CONFIG</code></li>
 * </ol>
 * <p>
 * When none of these is set nothing happens and the behaviour of JDT is unchanged. When a file is configured, the listed
 * runtimes are added to the installed JREs at startup (a runtime whose install location is already known is updated
 * rather than duplicated, VMs added by the user are never removed) and the listed execution environment defaults are
 * applied. Entries that cannot be applied are logged and skipped, they never prevent startup.
 * </p>
 *
 * @since 3.25
 */
public final class JreConfigurationFile {

	/**
	 * System property pointing to the configuration file.
	 */
	public static final String SYSTEM_PROPERTY = "org.eclipse.jdt.launching.jreConfig"; //$NON-NLS-1$

	/**
	 * Environment variable pointing to the configuration file.
	 */
	public static final String ENVIRONMENT_VARIABLE = "JDT_JRE_CONFIG"; //$NON-NLS-1$

	/**
	 * Command line argument pointing to the configuration file.
	 */
	public static final String COMMAND_LINE_ARGUMENT = "-jreConfig"; //$NON-NLS-1$

	/**
	 * VM attribute key under which the configured vendor is stored.
	 */
	public static final String ATTRIBUTE_VENDOR = "vendor"; //$NON-NLS-1$

	static final String KEY_RUNTIMES = "java.configuration.runtimes"; //$NON-NLS-1$
	static final String KEY_ENVIRONMENTS = "java.configuration.ee"; //$NON-NLS-1$

	private static final String ID_PREFIX = "jreConfig-"; //$NON-NLS-1$

	private static final Object LOCK = new Object();
	private static boolean fgInitialized;
	private static JreConfigurationFile fgInstance;

	/**
	 * A configured runtime.
	 *
	 * @param label name of the VM install
	 * @param vendor vendor or <code>null</code>
	 * @param version execution environment id or <code>null</code>
	 * @param path install location
	 * @param sources source attachment or <code>null</code>
	 * @param javadoc javadoc location or <code>null</code>
	 * @param vmArgs default VM arguments or <code>null</code>
	 * @param isDefault whether this runtime is the workspace default
	 */
	public record Runtime(String label, String vendor, String version, File path, File sources, URL javadoc, String vmArgs, boolean isDefault) {
	}

	/**
	 * A configured default VM for an execution environment.
	 *
	 * @param environmentId execution environment id
	 * @param path install location of the VM to use as default
	 */
	public record EnvironmentDefault(String environmentId, File path) {
	}

	private final String fOrigin;
	private final List<Runtime> fRuntimes;
	private final List<EnvironmentDefault> fEnvironmentDefaults;

	private JreConfigurationFile(String origin, List<Runtime> runtimes, List<EnvironmentDefault> environmentDefaults) {
		fOrigin = origin;
		fRuntimes = Collections.unmodifiableList(runtimes);
		fEnvironmentDefaults = Collections.unmodifiableList(environmentDefaults);
	}

	/**
	 * Returns the configuration referenced by command line, system property or environment variable, or
	 * <code>null</code> if none is configured or the configured file cannot be read.
	 *
	 * @return the configuration or <code>null</code>
	 */
	public static JreConfigurationFile getInstance() {
		synchronized (LOCK) {
			if (!fgInitialized) {
				fgInitialized = true;
				fgInstance = load();
			}
			return fgInstance;
		}
	}

	/**
	 * Discards the cached instance so that the next call to {@link #getInstance()} re-reads the configuration. Intended
	 * for tests.
	 */
	public static void reset() {
		synchronized (LOCK) {
			fgInitialized = false;
			fgInstance = null;
		}
	}

	private static JreConfigurationFile load() {
		String location = locate();
		if (location == null) {
			return null;
		}
		File file = new File(location);
		if (!file.isFile()) {
			log(IStatus.WARNING, "JRE configuration file does not exist: " + file); //$NON-NLS-1$
			return null;
		}
		try {
			String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			JreConfigurationFile config = parse(text, file.getAbsolutePath());
			if (LaunchingPlugin.isVMLogging()) {
				log(IStatus.INFO, "Loaded JRE configuration from " + file + ": " + config.fRuntimes.size() + " runtime(s), " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ config.fEnvironmentDefaults.size() + " execution environment default(s)"); //$NON-NLS-1$
			}
			return config;
		} catch (IOException | JsonException | RuntimeException e) {
			log(IStatus.ERROR, "Failed to read JRE configuration file " + file + ": " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	/**
	 * Returns the configured file location (command line, then system property, then environment variable) or
	 * <code>null</code>.
	 *
	 * @return file location or <code>null</code>
	 */
	static String locate() {
		String fromArgs = fromCommandLine(Platform.getCommandLineArgs());
		if (fromArgs != null) {
			return fromArgs;
		}
		String fromProperty = System.getProperty(SYSTEM_PROPERTY);
		if (fromProperty != null && !fromProperty.isBlank()) {
			return fromProperty.trim();
		}
		String fromEnvironment = System.getenv(ENVIRONMENT_VARIABLE);
		if (fromEnvironment != null && !fromEnvironment.isBlank()) {
			return fromEnvironment.trim();
		}
		return null;
	}

	static String fromCommandLine(String[] args) {
		if (args == null) {
			return null;
		}
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (COMMAND_LINE_ARGUMENT.equals(arg) && i + 1 < args.length) {
				return args[i + 1];
			}
			if (arg.startsWith(COMMAND_LINE_ARGUMENT + '=')) {
				String value = arg.substring(COMMAND_LINE_ARGUMENT.length() + 1);
				return value.isBlank() ? null : value;
			}
		}
		return null;
	}

	/**
	 * Parses the given JSON text.
	 *
	 * @param text JSON text
	 * @param origin description of the source, used in log messages
	 * @return the parsed configuration
	 * @throws JsonException if the text is not well formed JSON
	 * @throws IllegalArgumentException if the structure does not match the expected format
	 */
	public static JreConfigurationFile parse(String text, String origin) throws JsonException {
		Object root = SimpleJsonParser.parse(text);
		if (!(root instanceof Map<?, ?> object)) {
			throw new IllegalArgumentException("Root element must be a JSON object"); //$NON-NLS-1$
		}
		List<Runtime> runtimes = new ArrayList<>();
		Object runtimesValue = object.get(KEY_RUNTIMES);
		if (runtimesValue != null) {
			for (Object entry : asList(runtimesValue, KEY_RUNTIMES)) {
				Map<?, ?> runtime = asObject(entry, KEY_RUNTIMES);
				String label = requiredString(runtime, "label", KEY_RUNTIMES); //$NON-NLS-1$
				String path = requiredString(runtime, "path", KEY_RUNTIMES); //$NON-NLS-1$
				String sources = optionalString(runtime, "sources"); //$NON-NLS-1$
				String javadoc = optionalString(runtime, "javadoc"); //$NON-NLS-1$
				URL javadocURL = null;
				if (javadoc != null) {
					try {
						javadocURL = URI.create(substitute(javadoc)).toURL();
					} catch (IllegalArgumentException | MalformedURLException e) {
						throw new IllegalArgumentException("Invalid javadoc URL for runtime '" + label + "': " + javadoc, e); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				Object isDefault = runtime.get("default"); //$NON-NLS-1$
				runtimes.add(new Runtime(label, optionalString(runtime, "vendor"), optionalString(runtime, "version"), //$NON-NLS-1$ //$NON-NLS-2$
						toFile(path), sources == null ? null : toFile(sources), javadocURL, optionalString(runtime, "vmArgs"), //$NON-NLS-1$
						Boolean.TRUE.equals(isDefault)));
			}
		}
		List<EnvironmentDefault> defaults = new ArrayList<>();
		Object defaultsValue = object.get(KEY_ENVIRONMENTS);
		if (defaultsValue != null) {
			for (Object entry : asList(defaultsValue, KEY_ENVIRONMENTS)) {
				Map<?, ?> environment = asObject(entry, KEY_ENVIRONMENTS);
				String ee = requiredString(environment, "ee", KEY_ENVIRONMENTS); //$NON-NLS-1$
				String path = requiredString(environment, "path", KEY_ENVIRONMENTS); //$NON-NLS-1$
				defaults.add(new EnvironmentDefault(ee, toFile(path)));
			}
		}
		return new JreConfigurationFile(origin, runtimes, defaults);
	}

	private static List<?> asList(Object value, String key) {
		if (value instanceof List<?> list) {
			return list;
		}
		throw new IllegalArgumentException('\'' + key + "' must be an array"); //$NON-NLS-1$
	}

	private static Map<?, ?> asObject(Object value, String key) {
		if (value instanceof Map<?, ?> map) {
			return map;
		}
		throw new IllegalArgumentException("Entries of '" + key + "' must be objects"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String requiredString(Map<?, ?> object, String key, String parent) {
		String value = optionalString(object, key);
		if (value == null) {
			throw new IllegalArgumentException("Entries of '" + parent + "' require a non-empty string '" + key + '\''); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return value;
	}

	private static String optionalString(Map<?, ?> object, String key) {
		Object value = object.get(key);
		if (value == null) {
			return null;
		}
		if (!(value instanceof String string)) {
			throw new IllegalArgumentException('\'' + key + "' must be a string"); //$NON-NLS-1$
		}
		return string.isBlank() ? null : string.trim();
	}

	private static File toFile(String path) {
		// keep the configured (possibly symlinked) location, canonical paths are only used for comparisons
		return new File(substitute(path)).getAbsoluteFile();
	}

	private static String substitute(String expression) {
		if (expression.indexOf("${") < 0) { //$NON-NLS-1$
			return expression;
		}
		try {
			return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
		} catch (CoreException e) {
			log(IStatus.WARNING, "Cannot substitute variables in '" + expression + "': " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return expression;
		}
	}

	/**
	 * Returns the configured runtimes in file order.
	 *
	 * @return configured runtimes
	 */
	public List<Runtime> getRuntimes() {
		return fRuntimes;
	}

	/**
	 * Returns the configured execution environment defaults in file order.
	 *
	 * @return configured execution environment defaults
	 */
	public List<EnvironmentDefault> getEnvironmentDefaults() {
		return fEnvironmentDefaults;
	}

	/**
	 * Returns a description of where this configuration was read from.
	 *
	 * @return origin of the configuration
	 */
	public String getOrigin() {
		return fOrigin;
	}

	/**
	 * Adds the configured runtimes to the given container. A runtime whose install location matches a VM already present
	 * in the container updates that VM in place, otherwise a new {@link VMStandin} of the standard VM type is added. If a
	 * runtime is flagged as default, the container's default VM is set accordingly.
	 *
	 * @param container the container being populated during VM initialization
	 * @return whether the container was modified
	 */
	public boolean addVMs(VMDefinitionsContainer container) {
		IVMInstallType standardType = JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);
		if (standardType == null) {
			log(IStatus.ERROR, "Standard VM install type is not available, ignoring JRE configuration " + fOrigin); //$NON-NLS-1$
			return false;
		}
		boolean changed = false;
		for (Runtime runtime : fRuntimes) {
			File home = runtime.path();
			IStatus status = standardType.validateInstallLocation(home);
			if (status.getSeverity() == IStatus.ERROR) {
				log(IStatus.WARNING, "Ignoring runtime '" + runtime.label() + "' from " + fOrigin + ": " + status.getMessage() + " (" + home + ')'); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				continue;
			}
			VMStandin standin = findStandin(container, home);
			if (standin == null) {
				standin = new VMStandin(standardType, uniqueId(container, home));
				standin.setInstallLocation(home);
				container.addVM(standin);
				changed = true;
			}
			changed |= configure(standin, runtime, standardType);
			if (runtime.isDefault()) {
				String compositeId = JavaRuntime.getCompositeIdFromVM(standin);
				if (!compositeId.equals(container.getDefaultVMInstallCompositeID())) {
					container.setDefaultVMInstallCompositeID(compositeId);
					changed = true;
				}
			}
		}
		return changed;
	}

	private boolean configure(VMStandin standin, Runtime runtime, IVMInstallType standardType) {
		boolean changed = false;
		if (!runtime.label().equals(standin.getName())) {
			standin.setName(runtime.label());
			changed = true;
		}
		if (runtime.vendor() != null && !runtime.vendor().equals(standin.getAttribute(ATTRIBUTE_VENDOR))) {
			standin.setAttribute(ATTRIBUTE_VENDOR, runtime.vendor());
			changed = true;
		}
		if (runtime.javadoc() != null && !runtime.javadoc().equals(standin.getJavadocLocation())) {
			standin.setJavadocLocation(runtime.javadoc());
			changed = true;
		}
		if (runtime.vmArgs() != null && !runtime.vmArgs().equals(standin.getVMArgs())) {
			standin.setVMArgs(runtime.vmArgs());
			changed = true;
		}
		if (runtime.sources() != null) {
			LibraryLocation[] libraries = standin.getLibraryLocations();
			if (libraries == null) {
				libraries = standardType.getDefaultLibraryLocations(runtime.path());
			}
			if (libraries != null && libraries.length > 0) {
				IPath sourcePath = IPath.fromOSString(runtime.sources().getAbsolutePath());
				LibraryLocation[] updated = new LibraryLocation[libraries.length];
				boolean librariesChanged = false;
				for (int i = 0; i < libraries.length; i++) {
					LibraryLocation library = libraries[i];
					updated[i] = new LibraryLocation(library.getSystemLibraryPath(), sourcePath, library.getPackageRootPath(), library.getJavadocLocation(), library.getIndexLocation(), library.getExternalAnnotationsPath());
					librariesChanged |= !updated[i].equals(library);
				}
				if (librariesChanged) {
					standin.setLibraryLocations(updated);
					changed = true;
				}
			}
		}
		return changed;
	}

	private static VMStandin findStandin(VMDefinitionsContainer container, File home) {
		for (IVMInstall vm : container.getVMList()) {
			if (vm instanceof VMStandin standin && sameLocation(standin.getInstallLocation(), home)) {
				return standin;
			}
		}
		return null;
	}

	private static String uniqueId(VMDefinitionsContainer container, File home) {
		String base = ID_PREFIX + Integer.toHexString(home.getAbsolutePath().hashCode());
		String id = base;
		int suffix = 2;
		while (containsId(container, id)) {
			id = base + '-' + suffix++;
		}
		return id;
	}

	private static boolean containsId(VMDefinitionsContainer container, String id) {
		for (IVMInstall vm : container.getVMList()) {
			if (id.equals(vm.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the installed VM located at the given directory, or <code>null</code> if none is installed there.
	 *
	 * @param home install location
	 * @return matching VM install or <code>null</code>
	 */
	public static IVMInstall findInstalledVM(File home) {
		for (IVMInstallType type : JavaRuntime.getVMInstallTypes()) {
			for (IVMInstall vm : type.getVMInstalls()) {
				if (sameLocation(vm.getInstallLocation(), home)) {
					return vm;
				}
			}
		}
		return null;
	}

	static boolean sameLocation(File a, File b) {
		if (a == null || b == null) {
			return false;
		}
		if (a.equals(b)) {
			return true;
		}
		try {
			return Objects.equals(a.getCanonicalFile(), b.getCanonicalFile());
		} catch (IOException e) {
			return a.getAbsoluteFile().equals(b.getAbsoluteFile());
		}
	}

	static void log(int severity, String message) {
		log(severity, message, null);
	}

	static void log(int severity, String message, Throwable exception) {
		LaunchingPlugin.log(new Status(severity, LaunchingPlugin.ID_PLUGIN, message, exception));
	}
}
