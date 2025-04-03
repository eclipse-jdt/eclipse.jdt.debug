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
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame.Category;
import org.eclipse.jdt.internal.ui.filtertable.Filter;

/**
 * Service to help categorize the stack frames into {@link IJavaStackFrame.Category}, based on the internally stored preferences.
 */
public class StackFrameCategorizer implements IPreferenceChangeListener {
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
			Category category = categorizeSourceElement(frame);
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
	 * {@link org.eclipse.debug.core.ILaunch}. This is how we can find, if the class file is in a jar or comes from a source folder.
	 */
	private Category categorizeSourceElement(IJavaStackFrame frame) {
		var sourceLocator = frame.getLaunch().getSourceLocator();
		if (sourceLocator == null) {
			return CATEGORY_UNKNOWN;
		}
		var source = sourceLocator.getSourceElement(frame);
		if (source == null) {
			return CATEGORY_UNKNOWN;
		}
		if (source instanceof IFile file) {
			if (isEnabled(CATEGORY_TEST)) {
				var jproj = JavaCore.create(file.getProject());
				var cp = jproj.findContainingClasspathEntry(file);
				if (cp != null && cp.isTest()) {
					return CATEGORY_TEST;
				}
			}
			if (isEnabled(CATEGORY_PRODUCTION)) {
				return CATEGORY_PRODUCTION;
			}
		} else if (source instanceof IClassFile && isEnabled(CATEGORY_LIBRARY)) {
			return CATEGORY_LIBRARY;
		}
		return CATEGORY_UNKNOWN;
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
