package org.eclipse.jdt.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An evaluation engine that performs evaluations by
 * deploying class files locally.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */ 
public interface IClassFileEvaluationEngine extends IEvaluationEngine {
	/**
	 * Returns the name of the package in which code snippets are to be compiled and
	 * run. Returns an empty string for the default package (the default if the
	 * package name has never been set). For example, <code>"com.example.myapp"</code>.
	 *
	 * @return the dot-separated package name, or the empty string indicating the
	 *   default package
	 */
	public String getPackageName();
	
	/**
	 * Sets the dot-separated name of the package in which code snippets are 
	 * to be compiled and run. For example, <code>"com.example.myapp"</code>.
	 *
	 * @param packageName the dot-separated package name, or the empty string 
	 *   indicating the default package
	 */
	public void setPackageName(String packageName);
	
}

