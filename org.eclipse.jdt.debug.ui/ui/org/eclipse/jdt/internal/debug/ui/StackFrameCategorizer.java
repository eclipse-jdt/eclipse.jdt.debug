/*******************************************************************************
 * Copyright (c) 2025 Zsombor Gegesy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zsombor Gegesy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame.Category;
import org.eclipse.jdt.internal.ui.filtertable.Filter;

/**
 * Service to help categorize the stack frames into {@link IJavaStackFrame.Category}, based on the internally stored preferences.
 */
public class StackFrameCategorizer implements IPreferenceChangeListener, IDebugEventSetListener {
	private final static String PREFIX = JDIDebugUIPlugin.getUniqueIdentifier() + ".enable_category_"; //$NON-NLS-1$

	/**
	 * The user specified a filter, can be used for highlighting specific, very important code layers.
	 */
	static final Category CATEGORY_CUSTOM_FILTERED = new Category("CUSTOM_FILTERED", false); //$NON-NLS-1$

	/**
	 * The stack frame represents a synthetic function call, which is not based on actual Java source code.
	 */
	static final Category CATEGORY_SYNTHETIC = new Category("SYNTHETIC", true); //$NON-NLS-1$

	/**
	 * Methods in classes that considered as platform, like code in 'java.*' packages.
	 */
	static final Category CATEGORY_PLATFORM = new Category("PLATFORM", true); //$NON-NLS-1$

	/**
	 * Classes found in a test source folder in the project.
	 */
	static final Category CATEGORY_TEST = new Category("TEST", false); //$NON-NLS-1$

	/**
	 * Classes found in a non-test source folder in the project.
	 */
	static final Category CATEGORY_PRODUCTION = new Category("PRODUCTION", false); //$NON-NLS-1$

	/**
	 * Classes coming from a library, not from the actual project.
	 */
	static final Category CATEGORY_LIBRARY = new Category("LIBRARY", true); //$NON-NLS-1$

	/**
	 * Classes with unknown origin.
	 */
	static final Category CATEGORY_UNKNOWN = new Category("UNKNOWN", true); //$NON-NLS-1$

	/**
	 * Class to decide if a particular class name is part of a list of classes and list of packages.
	 */
	record Filters(String[] filters) {
		boolean match(String fqcName) {
			for (String filter : filters) {
				if (filter.endsWith("*")) { //$NON-NLS-1$
					if (fqcName.startsWith(filter.substring(0, filter.length() - 1))) {
						return true;
					}
				} else {
					if (filter.equals(fqcName)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * The origins of a stack frame's class, derived from where the class's source is located. This is independent of the enable/disable flags, so the
	 * result can be safely cached per class name.
	 */
	private enum SourceOrigin {
		/** The class comes from a test source folder in the project. */
		TEST,
		/** The class comes from a non-test source folder in the project. */
		PRODUCTION,
		/** The class comes from a library (jar, class file), not from the project. */
		LIBRARY,
		/** The source of the class is unknown. */
		UNKNOWN;
	}

	/**
	 * Cache key for a class's {@link SourceOrigin}. It combines the fully qualified class name with the {@link ISourceLocator} of the launch, so
	 * concurrent debug sessions with different source containers are not conflated.
	 */
	record SourceKey(String refTypeName, ISourceLocator sourceLocator) {
	}

	/**
	 * Cache of the {@link SourceOrigin} for a class, keyed by the {@link SourceKey}. The source lookup scans the filesystem recursively and is
	 * very expensive, so the same class is never looked up more than once per breakpoint suspension. The cache is thread safe as frames may be
	 * categorized concurrently on worker threads. Entries are only invalidated when the source containers change, see {@link #clearCache()}.
	 */
	private final Map<SourceKey, SourceOrigin> sourceOriginCache = new ConcurrentHashMap<>();

	private Filters platform;
	private Filters custom;
	private final IPreferencesService preferenceService;
	private final IEclipsePreferences instancePreferences;

	public StackFrameCategorizer(IPreferencesService preferenceService, IEclipsePreferences instancePreferences) {
		this.preferenceService = preferenceService;
		this.instancePreferences = instancePreferences;

		platform = createActivePlatformFilters();
		custom = createActiveCustomFilters();
	}

	/**
	 * Create a {@link Filters} object to decide if a class is part of the 'platform'. The platform definition is stored in the
	 * {@link IEclipsePreferences}. By default, this is the classes provided by the JVM.
	 *
	 */
	private Filters createActivePlatformFilters() {
		return new Filters(getActivePlatformStackFilter());
	}

	/**
	 * @return the list of <b>active</b> filter expressions that defines {@link IJavaStackFrame.Category#PLATFORM}.
	 */
	public String[] getActivePlatformStackFilter() {
		return getStringList(IJDIPreferencesConstants.PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST);
	}

	/**
	 * @return the list of <b>inactive</b> filter expressions that defines {@link IJavaStackFrame.Category#PLATFORM}.
	 */
	public String[] getInactivePlatformStackFilter() {
		return getStringList(IJDIPreferencesConstants.PREF_INACTIVE_PLATFORM_FRAME_FILTER_LIST);
	}

	/**
	 * @return the list of <b>active</b> filter expressions that defines {@link IJavaStackFrame.Category#CUSTOM_FILTERED}.
	 */
	public String[] getActiveCustomStackFilter() {
		return getStringList(IJDIPreferencesConstants.PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST);
	}

	/**
	 * @return the list of <b>inactive</b> filter expressions that defines {@link IJavaStackFrame.Category#CUSTOM_FILTERED}.
	 */
	public String[] getInactiveCustomStackFilter() {
		return getStringList(IJDIPreferencesConstants.PREF_INACTIVE_CUSTOM_FRAME_FILTER_LIST);
	}

	private String[] getStringList(String key) {
		return parseList(preferenceService.getString(JDIDebugUIPlugin.getUniqueIdentifier(), key, "", null)); //$NON-NLS-1$
	}

	/**
	 * Create a {@link Filters} object to decide if a class is considered part of a custom, very important layer, which needs to be highlighted. This
	 * definition is stored in the {@link IEclipsePreferences}. By default, this is an empty list.
	 */
	private Filters createActiveCustomFilters() {
		return new Filters(getActiveCustomStackFilter());
	}

	/**
	 * Categorize the given {@link IJavaStackFrame} into a {@link Category} based on the rules and filters, and where those classes are in the
	 * project. For example if in a source folder, in a library or in a test source folder, etc.
	 */
	public IJavaStackFrame.Category categorize(IJavaStackFrame frame) {
		try {
			var refTypeName = frame.getReferenceType().getName();
			if (isEnabled(CATEGORY_CUSTOM_FILTERED) && custom.match(refTypeName)) {
				return CATEGORY_CUSTOM_FILTERED;
			}
			Category category = categorizeSourceElement(frame, refTypeName);
			// if the category is prod or test, that's the most relevant.
			if (category == CATEGORY_PRODUCTION || category == CATEGORY_TEST) {
				return category;
			}

			if (isEnabled(CATEGORY_SYNTHETIC) && frame.isSynthetic()) {
				return CATEGORY_SYNTHETIC;
			}
			if (isEnabled(CATEGORY_PLATFORM) && platform.match(refTypeName)) {
				return CATEGORY_PLATFORM;
			}
			// Maybe comes from a library or it's unknown.
			return category;
		} catch (DebugException de) {
			JDIDebugUIPlugin.log(de);
			return CATEGORY_UNKNOWN;
		}
	}

	/**
	 * Do the categorization with the help of a {@link org.eclipse.debug.core.model.ISourceLocator} coming from the associated
	 * {@link org.eclipse.debug.core.ILaunch}.
	 */
	private Category categorizeSourceElement(IJavaStackFrame frame, String refTypeName) {
		return switch (getSourceOrigin(frame, refTypeName)) {
		case TEST -> {
			// a source file from a test classpath entry
			if (isEnabled(CATEGORY_TEST)) {
				yield CATEGORY_TEST;
			}
			yield isEnabled(CATEGORY_PRODUCTION) ? CATEGORY_PRODUCTION : CATEGORY_UNKNOWN;
		}
		case PRODUCTION -> isEnabled(CATEGORY_PRODUCTION) ? CATEGORY_PRODUCTION : CATEGORY_UNKNOWN;
		case LIBRARY -> isEnabled(CATEGORY_LIBRARY) ? CATEGORY_LIBRARY : CATEGORY_UNKNOWN;
		case UNKNOWN -> CATEGORY_UNKNOWN;
		};
	}

	/**
	 * Returns the {@link SourceOrigin} of the given frame's class, looking it up from the cache if it was already categorized in this session.
	 */
	private SourceOrigin getSourceOrigin(IJavaStackFrame frame, String refTypeName) {
		ISourceLocator sourceLocator = frame.getLaunch().getSourceLocator();
		SourceKey key = new SourceKey(refTypeName, sourceLocator);
		return sourceOriginCache.computeIfAbsent(key, k -> computeSourceOrigin(frame, sourceLocator));
	}

	/**
	 * Determine the {@link SourceOrigin} of the given frame's class by locating its source element through the launch's source locator.
	 */
	private SourceOrigin computeSourceOrigin(IJavaStackFrame frame, ISourceLocator sourceLocator) {
		if (sourceLocator == null) {
			return SourceOrigin.UNKNOWN;
		}
		var source = sourceLocator.getSourceElement(frame);
		if (source == null) {
			return SourceOrigin.UNKNOWN;
		}
		if (source instanceof IFile file) {
			var jproj = JavaCore.create(file.getProject());
			var cp = jproj.findContainingClasspathEntry(file);
			return cp != null && cp.isTest() ? SourceOrigin.TEST : SourceOrigin.PRODUCTION;
		}
		if (source instanceof IClassFile) {
			return SourceOrigin.LIBRARY;
		}
		return SourceOrigin.UNKNOWN;
	}

	/**
	 * When a debug target terminates, the entries held for its (now stale) source locator are dropped, so the cache does not grow without bound
	 * across debug sessions.
	 */
	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			if (event.getKind() == DebugEvent.TERMINATE && event.getSource() instanceof IJavaDebugTarget target) {
				ISourceLocator locator = target.getLaunch().getSourceLocator();
				sourceOriginCache.keySet().removeIf(key -> key.sourceLocator() == locator);
			}
		}
	}

	public boolean isEnabled(Category category) {
		return preferenceService.getBoolean(JDIDebugUIPlugin.getUniqueIdentifier(), getNameOfTheFlagToEnable(category), true, null);
	}

	private String getNameOfTheFlagToEnable(Category category) {
		return PREFIX + category.name();
	}

	public void setEnabled(Category category, boolean flag) {
		instancePreferences.putBoolean(getNameOfTheFlagToEnable(category), flag);
	}

	/**
	 * Parses the comma separated string into an array of strings
	 *
	 * @param listString
	 *            the comma separated string
	 * @return list
	 */
	private String[] parseList(String listString) {
		List<String> list = new ArrayList<>(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ",");//$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		String prop = event.getKey();
		if (IJDIPreferencesConstants.PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST.equals(prop)) {
			platform = createActivePlatformFilters();
		} else if (IJDIPreferencesConstants.PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST.equals(prop)) {
			custom = createActiveCustomFilters();
		}
	}

	/**
	 * Adds the given class names to the definition of the active list of custom, highlighted classes.
	 *
	 * @param classNames
	 *            name of the classes.
	 */
	public void addTypesToActiveCustomFilters(Set<String> classNames) {
		List<String> actives = new ArrayList<>(List.of(getActiveCustomStackFilter()));
		List<String> inactives = new ArrayList<>(List.of(getInactiveCustomStackFilter()));
		for (String className : classNames) {
			inactives.remove(className);
			if (!actives.contains(className)) {
				actives.add(className);
			}
		}
		setCustomFilters(convert(actives), convert(inactives));
	}

	private void setCustomFilters(String actives, String inactives) {
		instancePreferences.put(IJDIPreferencesConstants.PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST, actives);
		instancePreferences.put(IJDIPreferencesConstants.PREF_INACTIVE_CUSTOM_FRAME_FILTER_LIST, inactives);
	}

	private void setPlatformFilters(String actives, String inactives) {
		instancePreferences.put(IJDIPreferencesConstants.PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST, actives);
		instancePreferences.put(IJDIPreferencesConstants.PREF_INACTIVE_PLATFORM_FRAME_FILTER_LIST, inactives);
	}

	void setPlatformFilters(Filter[] filters) {
		setPlatformFilters(convert(filters, true), convert(filters, false));
	}

	void setCustomFilters(Filter[] filters) {
		setCustomFilters(convert(filters, true), convert(filters, false));
	}

	private static String convert(Filter[] filters, boolean active) {
		return Stream.of(filters).filter(f -> f.isChecked() == active).map(Filter::getName).collect(Collectors.joining(","));//$NON-NLS-1$
	}

	private static String convert(List<String> classNames) {
		return String.join(",", classNames); //$NON-NLS-1$
	}
}
