/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
// AW
package org.eclipse.jdt.internal.debug.ui;

/**
 * Defines constants which are used to refer to values in the plugin's preference bundle.
 */
public interface IPreferencesConstants {
	
	// keys 	
	static final String ATTACH_LAUNCH_PORT= "org.eclipse.jdt.debug.ui.attachlaunch.port"; //$NON-NLS-1$
	static final String ATTACH_LAUNCH_HOST= "org.eclipse.jdt.debug.ui.attachlaunch.host"; //$NON-NLS-1$
	static final String ATTACH_LAUNCH_ALLOW_TERMINATE= "org.eclipse.jdt.debug.ui.attachlaunch.allowTerminate"; //$NON-NLS-1$
		
	static final String SHOW_HEX_VALUES= "org.eclipse.jdt.debug.ui.javaDebug.showHexValues"; //$NON-NLS-1$
	static final String SHOW_CHAR_VALUES= "org.eclipse.jdt.debug.ui.javaDebug.showCharValues"; //$NON-NLS-1$
	static final String SHOW_UNSIGNED_VALUES= "org.eclipse.jdt.debug.ui.javaDebug.showUnsignedValues"; //$NON-NLS-1$
	// Preference update flag useful for IPropertyChangeListeners to
	// by notified of variable rendering preference changes
	static final String VARIABLE_RENDERING = "VARIABLE_RENDERING";
	
}