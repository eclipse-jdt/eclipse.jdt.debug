/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.debug.ui;

/**
 * Constant definitions for Java debug UI plug-in.
 * <p>
 * Popup menus in the Java debug UI support action contribution via the
 * <code>org.eclipse.ui.popupMenus</code>  extension. Actions may be
 * contributed to any group on the menu. To facilitate insertion of actions
 * in between existing groups, empty groups have been defined
 * in the menu. Each group prefixed by "empty" indicates an empty group.
 */
public interface IJavaDebugUIConstants {

	/**
	 * Display view identifier (value <code>"org.eclipse.debug.ui.DisplayView"</code>).
	 */
	public static final String ID_DISPLAY_VIEW= "org.eclipse.jdt.debug.ui.DisplayView"; //$NON-NLS-1$
	
	public static final String EVALUATION_GROUP= "evaluationGroup";
	
	public static final String ID_JAVA_SNIPPET_EDITOR= "org.eclipse.jdt.debug.ui.SnippetEditor";
	
	public static final String JAVA_SNIPPET_EDITOR_CONTEXT_MENU= "#JavaSnippetEditorContext";
	
	public static final String JAVA_SNIPPET_EDITOR_RULER_MENU= "#JavaSnippetRulerContext";
}
