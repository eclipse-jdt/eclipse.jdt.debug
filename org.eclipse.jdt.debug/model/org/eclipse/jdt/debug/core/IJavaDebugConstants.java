package org.eclipse.jdt.debug.core;

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
	 * Java line breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.javaLineBreakpoint"</code>).
	 */
	public static final String JAVA_LINE_BREAKPOINT = "org.eclipse.jdt.debug.javaLineBreakpoint";
				
	/**
	 * Java run-to-line breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.javaRunToLineBreakpoint"</code>).
	 */
	public static final String JAVA_RUN_TO_LINE_BREAKPOINT = "org.eclipse.jdt.debug.javaRunToLineBreakpoint";
				
	/**
	 * Java exception breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.javaExceptionBreakpoint"</code>).
	 */
	public static final String JAVA_EXCEPTION_BREAKPOINT = "org.eclipse.jdt.debug.javaExceptionBreakpoint";
	
	/**
	 * Java method entry breakpoint marker type
	 * (value <code>"org.eclipse.jdt.debug.javaMethodEntryBreakpoint"</code>).
	 */
	public static final String JAVA_METHOD_ENTRY_BREAKPOINT = "org.eclipse.jdt.debug.javaMethodEntryBreakpoint";
	
	/**
	 * Breakpoint attribute storing the handle identifier of the Java element
	 * corresponding to the type in which a breakpoint is contained
	 * (value <code>"typeHandle"</code>). This attribute is a <code>String</code>.
	 */
	public static final String TYPE_HANDLE = "typeHandle";
	
	/**
	 * Breakpoint attribute storing the handle identifier of the Java element
	 * corresponding to the method in which a breakpoint is contained
	 * (value <code>"methodHandle"</code>). This attribute is a <code>String</code>.
	 */
	public static final String METHOD_HANDLE = "methodHandle";
	
	/**
	 * Breakpoint attribute storing the number of debug targets a
	 * breakpoint is installed in (value <code>"installCount"</code>).
	 * This attribute is a <code>int</code>.
	 */
	public static final String INSTALL_COUNT = "installCount";
	
	/**
	 * Breakpoint attribute storing a breakpoint's hit count value
	 * (value <code>"hitCount"</code>). This attribute is stored as an
	 * <code>int</code>.
	 */
	public static final String HIT_COUNT = "hitCount";

	/**
	 * Breakpoint attribute storing the expired value (value <code>"expired"</code>).
	 * This attribute is stored as a <code>boolean</code>. Once a hit count has
	 * been reached, a breakpoint is considered to be "expired".
	 */
	public static final String EXPIRED = "expired";
		
	/**
	 * Exception breakpoint attribute storing the suspend on caught value
	 * (value <code>"caught"</code>). This attribute is stored as a <code>boolean</code>.
	 * When this attribute is <code>true</code>, a caught exception of the associated
	 * type will cause excecution to suspend .
	 */
	public static final String CAUGHT = "caught";
	
	/**
	 * Exception breakpoint attribute storing the suspend on uncaught value
	 * (value <code>"uncaught"</code>). This attribute is stored as a
	 * <code>boolean</code>. When this attribute is <code>true</code>, an uncaught
	 * exception of the associated type will cause excecution to suspend. .
	 */
	public static final String UNCAUGHT = "uncaught";
	
	/**
	 * Exception breakpoint attribute storing the checked value (value <code>"checked"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether an
	 * exception is a checked exception.
	 */
	public static final String CHECKED = "checked";

}


