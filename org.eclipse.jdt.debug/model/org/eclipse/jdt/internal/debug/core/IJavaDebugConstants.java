package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Defines constants for the JDI debug model plug-in.
 * <p>
 * Constants only; not intended to be implemented.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */

public interface IJavaDebugConstants {
	
	/**
	 * Java breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.javaBreakpointMarker"</code>).
	 */
	public static final String JAVA_BREAKPOINT= "org.eclipse.jdt.debug.javaBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Java line breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.javaLineBreakpointMarker"</code>).
	 */
	public static final String JAVA_LINE_BREAKPOINT = "org.eclipse.jdt.debug.javaLineBreakpointMarker"; //$NON-NLS-1$
				
	/**
	 * Java run-to-line breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.javaRunToLineBreakpointMarker"</code>).
	 */
	public static final String JAVA_RUN_TO_LINE_BREAKPOINT = "org.eclipse.jdt.debug.javaRunToLineBreakpointMarker"; //$NON-NLS-1$
				
	/**
	 * Java exception breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.javaExceptionBreakpointMarker"</code>).
	 */
	public static final String JAVA_EXCEPTION_BREAKPOINT = "org.eclipse.jdt.debug.javaExceptionBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Java watchpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.javaWatchpointMarker"</code>).
	 */
	public static final String JAVA_WATCHPOINT= "org.eclipse.jdt.debug.javaWatchpointMarker"; //$NON-NLS-1$
	
	/**
	 * Java method entry breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.javaMethodEntryBreakpointMarker"</code>).
	 */
	public static final String JAVA_METHOD_ENTRY_BREAKPOINT = "org.eclipse.jdt.debug.javaMethodEntryBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Pattern breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.patternBreakpointMarker"</code>).
	 */
	public static final String PATTERN_BREAKPOINT = "org.eclipse.jdt.debug.patternBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Snippet support line breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.snippetSupportLineBreakpointMarker"</code>).
	 */
	public static final String SNIPPET_SUPPORT_LINE_BREAKPOINT= "org.eclipse.jdt.debug.snippetSupportLineBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Breakpoint attribute storing the handle identifier of the Java element
	 * corresponding to the type in which a breakpoint is contained
	 * (value <code>"typeHandle"</code>). This attribute is a <code>String</code>.
	 */
	public static final String TYPE_HANDLE = "typeHandle"; //$NON-NLS-1$
	
	/**
	 * Breakpoint attribute storing the handle identifier of the Java element
	 * corresponding to the field on which a breakpoint is set
	 * (value <code>"fieldHandle"</code>). This attribute is a <code>String</code>.
	 */
	public static final String FIELD_HANDLE= "fieldHandle"; //$NON-NLS-1$
	
	/**
	 * Breakpoint attribute storing the handle identifier of the Java element
	 * corresponding to the method in which a breakpoint is contained
	 * (value <code>"methodHandle"</code>). This attribute is a <code>String</code>.
	 */
	public static final String METHOD_HANDLE = "methodHandle"; //$NON-NLS-1$
	
	/**
	 * Breakpoint attribute storing the pattern identifier of the source
	 * file in which a breakpoint is created
	 * (value <code>"patternHandle"</code>). This attribute is a <code>String</code>.
	 */
	public static final String PATTERN = "pattern"; //$NON-NLS-1$
	
	/**
	 * Breakpoint attribute storing the number of debug targets a
	 * breakpoint is installed in (value <code>"installCount"</code>).
	 * This attribute is a <code>int</code>.
	 */
	public static final String INSTALL_COUNT = "installCount"; //$NON-NLS-1$
	
	/**
	 * Breakpoint attribute storing a breakpoint's hit count value
	 * (value <code>"hitCount"</code>). This attribute is stored as an
	 * <code>int</code>.
	 */
	public static final String HIT_COUNT = "hitCount"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the expired value (value <code>"expired"</code>).
	 * This attribute is stored as a <code>boolean</code>. Once a hit count has
	 * been reached, a breakpoint is considered to be "expired".
	 */
	public static final String EXPIRED = "expired"; //$NON-NLS-1$
		
	/**
	 * Exception breakpoint attribute storing the suspend on caught value
	 * (value <code>"caught"</code>). This attribute is stored as a <code>boolean</code>.
	 * When this attribute is <code>true</code>, a caught exception of the associated
	 * type will cause excecution to suspend .
	 */
	public static final String CAUGHT = "caught"; //$NON-NLS-1$
	
	/**
	 * Exception breakpoint attribute storing the suspend on uncaught value
	 * (value <code>"uncaught"</code>). This attribute is stored as a
	 * <code>boolean</code>. When this attribute is <code>true</code>, an uncaught
	 * exception of the associated type will cause excecution to suspend. .
	 */
	public static final String UNCAUGHT = "uncaught"; //$NON-NLS-1$
	
	/**
	 * Exception breakpoint attribute storing the checked value (value <code>"checked"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether an
	 * exception is a checked exception.
	 */
	public static final String CHECKED = "checked"; //$NON-NLS-1$
	
	/**
	 * Watchpoint attribute storing the access value (value <code>"access"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether a
	 * watchpoint is an access watchpoint.
	 */
	public static final String ACCESS= "access"; //$NON-NLS-1$
	
	/**
	 * Watchpoint attribute storing the modification value (value <code>"modification"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether a
	 * watchpoint is a modification watchpoint.
	 */
	public static final String MODIFICATION= "modification"; //$NON-NLS-1$
	
	/**
	 * Watchpoint attribute storing the auto_disabled value (value <code>"auto_disabled"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether a
	 * watchpoint has been auto-disabled (as opposed to being disabled explicitly by the user)
	 */
	public static final String AUTO_DISABLED="auto_disabled"; //$NON-NLS-1$

}
