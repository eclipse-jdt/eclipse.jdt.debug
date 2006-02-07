/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui;


import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
 
/**
 * Constant definitions for Java debug UI plug-in.
 * <p>
 * Clients are not intended to implement this interface; constant definitions only.
 * </p>
 * @since 2.0
 */
public interface IJavaDebugUIConstants {
	/**
	 * Plug-in identifier for the Java Debug UI
	 */
	public static final String PLUGIN_ID = JDIDebugUIPlugin.getUniqueIdentifier();

	/**
	 * Extension point identifier for contributions of a UI page that corresponds to a VMInstallType (value <code>"vmInstallTypePage"</code>).
	 */
	public static final String EXTENSION_POINT_VM_INSTALL_TYPE_PAGE = "vmInstallTypePage"; //$NON-NLS-1$

	/**
	 * Display view identifier (value <code>"org.eclipse.jdt.debug.ui.DisplayView"</code>).
	 */
	public static final String ID_DISPLAY_VIEW= PLUGIN_ID + ".DisplayView"; //$NON-NLS-1$
	
	/**
	 * Java snippet editor identifier (value <code>"org.eclipse.jdt.debug.ui.SnippetEditor"</code>)
	 */
	public static final String ID_JAVA_SNIPPET_EDITOR= PLUGIN_ID + ".SnippetEditor"; //$NON-NLS-1$

	/**
	 * Java snippet editor context menu identifier (value <code>"#JavaSnippetEditorContext"</code>).
	 */
	public static final String JAVA_SNIPPET_EDITOR_CONTEXT_MENU= "#JavaSnippetEditorContext"; //$NON-NLS-1$
	
	/**
	 * Java snippet editor ruler menu identifier (value <code>"#JavaSnippetRulerContext"</code>).
	 */	
	public static final String JAVA_SNIPPET_EDITOR_RULER_MENU= "#JavaSnippetRulerContext"; //$NON-NLS-1$

	/**
	 * Identifier for a group of evaluation actions in a menu (value <code>"evaluationGroup"</code>).
	 */
	public static final String EVALUATION_GROUP= "evaluationGroup"; //$NON-NLS-1$
	
	/**
	 * Status code indicating an unexpected internal error (value <code>150</code>).
	 */
	public static final int INTERNAL_ERROR = 150;

	/**
	 * Boolean preference indicating whether the monitor and thread info should be displayed in the debug view.
	 * A view may override this preference, and if so, stores its preference, prefixed by view id.
	 *  
	 * @since 3.2
	 */	
	public static final String PREF_SHOW_MONITOR_THREAD_INFO = PLUGIN_ID + ".show_monitor_thread_info"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating whether system threads should appear visible in the debug view.
	 * 
	 * @since 3.2
	 */	
	public static final String PREF_SHOW_SYSTEM_THREADS = PLUGIN_ID + ".show_system_threads"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating whether thread groups should be displayed in the debug view.
	 * 
	 * @since 3.2
	 */	
	public static final String PREF_SHOW_THREAD_GROUPS = PLUGIN_ID + ".show_thread_group_info"; //$NON-NLS-1$	
	
	
	
}
