package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.debug.ui.JavaDebugUI;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Defines constants which are used to refer to values in the plugin's preference store.
 */
public interface IJDIPreferencesConstants {
	
	// keys 	
	static final String ATTACH_LAUNCH_PORT= "org.eclipse.jdt.debug.ui.attachlaunch.port"; //$NON-NLS-1$
	static final String ATTACH_LAUNCH_HOST= "org.eclipse.jdt.debug.ui.attachlaunch.host"; //$NON-NLS-1$
	static final String ATTACH_LAUNCH_ALLOW_TERMINATE= "org.eclipse.jdt.debug.ui.attachlaunch.allowTerminate"; //$NON-NLS-1$
		
	static final String SHOW_HEX_VALUES= "org.eclipse.jdt.debug.ui.javaDebug.showHexValues"; //$NON-NLS-1$
	static final String SHOW_CHAR_VALUES= "org.eclipse.jdt.debug.ui.javaDebug.showCharValues"; //$NON-NLS-1$
	static final String SHOW_UNSIGNED_VALUES= "org.eclipse.jdt.debug.ui.javaDebug.showUnsignedValues"; //$NON-NLS-1$
	static final String SUSPEND_ON_UNCAUGHT_EXCEPTIONS= "org.eclipse.jdt.ui.javaDebug.SuspendOnUncaughtExceptions"; //$NON-NLS-1$
	/**
	 * Boolean preference controlling whether to suspend
	 * execution when a compilation error is encountered
	 * (while debugging).
	 */
	public static final String PREF_SUSPEND_ON_COMPILATION_ERRORS= JavaDebugUI.PLUGIN_ID + ".suspend_on_compilation_errors"; //$NON-NLS-1$
	
	// Preference update flag useful for IPropertyChangeListeners to
	// by notified of variable rendering preference changes
	static final String VARIABLE_RENDERING = "VARIABLE_RENDERING"; //$NON-NLS-1$
	static final String ALERT_HCR_FAILED = "org.eclipse.jdt.debug.ui.javaDebug.alertHCRFailed"; //$NON-NLS-1$
	static final String ALERT_OBSOLETE_METHODS = "org.eclipse.jdt.debug.ui.javaDebug.alertObsoleteMethods"; //$NON-NLS-1$
	
}