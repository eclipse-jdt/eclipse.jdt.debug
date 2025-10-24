/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *     Jesper Steen Moller - Enhancement 254677 - filter getters/setters
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;

/**
 * Defines constants which are used to refer to values in the plugin's preference store.
 */
public interface IJDIPreferencesConstants {

	/**
	 * Boolean preference controlling whether to suspend
	 * execution when an uncaught Java exceptions encountered
	 * (while debugging).
	 */
	public static final String PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS= IJavaDebugUIConstants.PLUGIN_ID + ".javaDebug.SuspendOnUncaughtExceptions"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether to suspend
	 * execution when a compilation error is encountered
	 * (while debugging).
	 */
	public static final String PREF_SUSPEND_ON_COMPILATION_ERRORS = IJavaDebugUIConstants.PLUGIN_ID + ".suspend_on_compilation_errors"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether synthetic
	 * methods are to be filtered when stepping (and step
	 * filters are enabled).
	 */
	public static final String PREF_FILTER_SYNTHETICS = IJavaDebugUIConstants.PLUGIN_ID + ".filter_synthetics"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether static
	 * initializers are to be filtered when stepping (and step
	 * filters are enabled).
	 */
	public static final String PREF_FILTER_STATIC_INITIALIZERS = IJavaDebugUIConstants.PLUGIN_ID + ".filter_statics"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether simple getters
	 * are to be filtered when stepping (and step
	 * filters are enabled).
	 */
	public static final String PREF_FILTER_GETTERS = IJavaDebugUIConstants.PLUGIN_ID + ".filter_get"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether simple setters
	 * are to be filtered when stepping (and step
	 * filters are enabled).
	 */
	public static final String PREF_FILTER_SETTERS = IJavaDebugUIConstants.PLUGIN_ID + ".filter_setters"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether constructors
	 * are to be filtered when stepping (and step
	 * filters are enabled).
	 */
	public static final String PREF_FILTER_CONSTRUCTORS = IJavaDebugUIConstants.PLUGIN_ID + ".filter_constructors"; //$NON-NLS-1$


	/**
	 * Boolean preference controlling whether a step landing in a filtered
	 * location proceeds through to an un-filtered location, or returns.
	 *
	 * @since 3.3
	 */
	public static final String PREF_STEP_THRU_FILTERS = IJavaDebugUIConstants.PLUGIN_ID + ".step_thru_filters"; //$NON-NLS-1$

	public static final String PREF_COLLAPSE_STACK_FRAMES = IJavaDebugUIConstants.PLUGIN_ID + ".collapse_stack_frames"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating whether to colorize the stack frames in the debug view.
	 *
	 */
	public static final String PREF_COLORIZE_STACK_FRAMES = IJavaDebugUIConstants.PLUGIN_ID + ".colorize_stack_frames"; //$NON-NLS-1$

	/**
	 * List of active step filters. A String containing a comma separated list of fully qualified type names/patterns.
	 */
	public static final String PREF_ACTIVE_FILTERS_LIST = IJavaDebugUIConstants.PLUGIN_ID + ".active_filters"; //$NON-NLS-1$

	/**
	 * List of inactive step filters. A String containing a comma
	 * separated list of fully qualified type names/patterns.
	 */
	public static final String PREF_INACTIVE_FILTERS_LIST = IJavaDebugUIConstants.PLUGIN_ID + ".inactive_filters"; //$NON-NLS-1$

	/**
	 * List of active filters for custom stack frame categorization. A String containing a comma separated list of fully qualified type
	 * names/patterns.
	 *
	 * @since 3.22
	 */
	public static final String PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST = IJavaDebugUIConstants.PLUGIN_ID + ".active_custom_frames_filters"; //$NON-NLS-1$

	/**
	 * List of inactive filters for custom stack frame categorization. A String containing a comma separated list of fully qualified type
	 * names/patterns.
	 *
	 * @since 3.22
	 */
	public static final String PREF_INACTIVE_CUSTOM_FRAME_FILTER_LIST = IJavaDebugUIConstants.PLUGIN_ID + ".inactive_custom_frames_filters"; //$NON-NLS-1$

	/**
	 * List of active filters for custom stack frame categorization. A String containing a comma separated list of fully qualified type
	 * names/patterns.
	 *
	 * @since 3.22
	 */
	public static final String PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST = IJavaDebugUIConstants.PLUGIN_ID + ".active_platform_frames_filters"; //$NON-NLS-1$

	/**
	 * List of inactive filters for custom stack frame categorization. A String containing a comma separated list of fully qualified type
	 * names/patterns.
	 *
	 * @since 3.22
	 */
	public static final String PREF_INACTIVE_PLATFORM_FRAME_FILTER_LIST = IJavaDebugUIConstants.PLUGIN_ID + ".inactive_platform_frames_filters"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether to alert
	 * with a dialog when hot code replace fails.
	 */
	public static final String PREF_ALERT_HCR_FAILED = IJavaDebugUIConstants.PLUGIN_ID + ".javaDebug.alertHCRFailed"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether to alert
	 * with a dialog when hot code replace is not supported.
	 */
	public static final String PREF_ALERT_HCR_NOT_SUPPORTED = IJavaDebugUIConstants.PLUGIN_ID + ".javaDebug.alertHCRNotSupported"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether to alert
	 * with a dialog when hot code replace results in
	 * obsolete methods.
	 */
	public static final String PREF_ALERT_OBSOLETE_METHODS = IJavaDebugUIConstants.PLUGIN_ID + "javaDebug.alertObsoleteMethods"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether the debugger shows
	 * qualifed names. When <code>true</code> the debugger
	 * will show qualified names in newly opened views.
	 *
	 * @since 2.0
	 */
	public static final String PREF_SHOW_QUALIFIED_NAMES= IJavaDebugUIConstants.PLUGIN_ID + ".show_qualified_names"; //$NON-NLS-1$

	/**
	 * List of defined detail formatters.A String containing a comma
	 * separated list of fully qualified type names, the associated
	 * code snippet and an 'enabled' flag.
	 */
	public static final String PREF_DETAIL_FORMATTERS_LIST= IJavaDebugUIConstants.PLUGIN_ID + ".detail_formatters"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating whether (non-final) static varibles should be shown
	 * in variable views. A view may over-ride this preference, and if so, stores
	 * its preference, prefixed by view id.
	 *
	 * @since 2.1
	 */
	public static final String PREF_SHOW_STATIC_VARIABLES= IJavaDebugUIConstants.PLUGIN_ID + ".show_static_variables"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating whether constant (final static) varibles should be shown
	 * in variable views. A view may over-ride this preference, and if so, stores
	 * its preference, prefixed by view id.
	 *
	 * @since 2.1
	 */
	public static final String PREF_SHOW_CONSTANTS= IJavaDebugUIConstants.PLUGIN_ID + ".show_constants"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating whether null array entries should be shown
	 * in variable views. A view may over-ride this preference, and if so, stores
	 * its preference, prefixed by view id.
	 *
	 * @since 3.0
	 */
	public static final String PREF_SHOW_NULL_ARRAY_ENTRIES = IJavaDebugUIConstants.PLUGIN_ID + ".show_null_entries"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating whether hex values should be shown for primitives
	 * in variable views. A view may over-ride this preference, and if so, stores
	 * its preference, prefixed by view id.
	 *
	 * @since 2.1
	 */
	public static final String PREF_SHOW_HEX = IJavaDebugUIConstants.PLUGIN_ID + ".show_hex"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating whether char values should be shown for primitives
	 * in variable views. A view may over-ride this preference, and if so, stores
	 * its preference, prefixed by view id.
	 *
	 * @since 2.1
	 */
	public static final String PREF_SHOW_CHAR = IJavaDebugUIConstants.PLUGIN_ID + ".show_char"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating whether unsigned values should be shown for primitives
	 * in variable views. A view may over-ride this preference, and if so, stores
	 * its preference, prefixed by view id.
	 *
	 * @since 2.1
	 */
	public static final String PREF_SHOW_UNSIGNED = IJavaDebugUIConstants.PLUGIN_ID + ".show_unsigned"; //$NON-NLS-1$

	/**
	 * String preference indication when and where variable details should appear.
	 * Valid values include:
	 * <ul>
	 *   <li><code>INLINE_ALL</code> to show inline details for all variables
	 *   <li><code>INLINE_FORMATTERS</code> to show inline details for variables with formatters
	 *   <li><code>DETAIL_PANE</code> to show details only in the detail pane
	 * </ul>
	 */
	public static final String PREF_SHOW_DETAILS = IJavaDebugUIConstants.PLUGIN_ID + ".show_details"; //$NON-NLS-1$
	/**
	 * "Show detail" preference values.
	 */
	public static final String INLINE_ALL="INLINE_ALL"; //$NON-NLS-1$
	public static final String INLINE_FORMATTERS="INLINE_FORMATTERS"; //$NON-NLS-1$
	public static final String DETAIL_PANE="DETAIL_PANE"; //$NON-NLS-1$

	/**
	 * Common dialog settings
	 */
	public static final String DIALOG_ORIGIN_X = "DIALOG_ORIGIN_X"; //$NON-NLS-1$
	public static final String DIALOG_ORIGIN_Y = "DIALOG_ORIGIN_Y"; //$NON-NLS-1$
	public static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
	public static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$


	/**
	 * Boolean preference controlling whether to alert
	 * with a dialog when unable to install a breakpoint
	 * (line info not available, ...)
	 */
	public static final String PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT = IJavaDebugUIConstants.PLUGIN_ID + ".prompt_unable_to_install_breakpoint"; //$NON-NLS-1$

	public static final String PREF_PROMPT_BEFORE_MODIFYING_FINAL_FIELDS = IJavaDebugUIConstants.PLUGIN_ID + ".prompt_before_modifying_final_fields"; //$NON-NLS-1$

	public static final String PREF_THREAD_MONITOR_IN_DEADLOCK_COLOR= "org.eclipse.jdt.debug.ui.InDeadlockColor"; //$NON-NLS-1$

	public static final String PREF_LABELED_OBJECT_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".LabeledObject"; //$NON-NLS-1$

	public static final String PREF_TEST_STACK_FRAME_FG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".TestStackFrameFgColor"; //$NON-NLS-1$
	public static final String PREF_TEST_STACK_FRAME_BG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".TestStackFrameBgColor"; //$NON-NLS-1$

	public static final String PREF_LIB_STACK_FRAME_FG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".LibraryStackFrameFgColor"; //$NON-NLS-1$
	public static final String PREF_LIB_STACK_FRAME_BG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".LibraryStackFrameBgColor"; //$NON-NLS-1$

	public static final String PREF_SYNT_STACK_FRAME_FG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".SyntheticStackFrameFgColor"; //$NON-NLS-1$
	public static final String PREF_SYNT_STACK_FRAME_BG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".SyntheticStackFrameBgColor"; //$NON-NLS-1$

	public static final String PREF_PLATFORM_STACK_FRAME_FG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".PlatformStackFrameFgColor"; //$NON-NLS-1$
	public static final String PREF_PLATFORM_STACK_FRAME_BG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".PlatformStackFrameBgColor"; //$NON-NLS-1$

	public static final String PREF_PRODUCTION_STACK_FRAME_FG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".ProductionStackFrameFgColor"; //$NON-NLS-1$
	public static final String PREF_PRODUCTION_STACK_FRAME_BG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".ProductionStackFrameBgColor"; //$NON-NLS-1$

	public static final String PREF_CUSTOM_FILTERED_STACK_FRAME_FG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".CustomFilteredStackFrameFgColor"; //$NON-NLS-1$
	public static final String PREF_CUSTOM_FILTERED_STACK_FRAME_BG_COLOR = IJavaDebugUIConstants.PLUGIN_ID + ".CustomFilteredStackFrameBgColor"; //$NON-NLS-1$

	/**
	 * @since 3.2
	 */
	public static final String PREF_OPEN_INSPECT_POPUP_ON_EXCEPTION = IJavaDebugUIConstants.PLUGIN_ID + ".open_inspect_popup_on_exception"; //$NON-NLS-1$

	/**
	 * Boolean  preference controlling whether the java stack trace
	 * console should be formatted when ever a paste occurs.
	 * @since 3.3
	 */
	public static final String PREF_AUTO_FORMAT_JSTCONSOLE = IJavaDebugUIConstants.PLUGIN_ID + ".auto_format_jstconsole"; //$NON-NLS-1$;

	/**
	 * Boolean preference controlling whether to prompt with a dialog when deleting a conditional
	 * breakpoint.
	 */
	public static final String PREF_PROMPT_DELETE_CONDITIONAL_BREAKPOINT= IJavaDebugUIConstants.PLUGIN_ID + ".prompt_delete_conditional_breakpoint"; //$NON-NLS-1$

}
