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
 * @since 2.0
 */ 
public interface IClassFileEvaluationEngine extends IEvaluationEngine {
	/**
	 * Returns the import declarations for this evaluation context. An empty
	 * list indicates there are no imports. The syntax for the import corresponds to a 
	 * fully qualified type name, or to an on-demand package name as defined by
	 * ImportDeclaration (JLS2 7.5). For example, <code>"java.util.Hashtable"</code>
	 * or <code>"java.util.*"</code>.
	 *
	 * @param imports the list of import names
	 */
	public String[] getImports();
	
	/**
	 * Sets the import declarations for this evaluation context. An empty
	 * list indicates there are no imports. The syntax for the import corresponds to a 
	 * fully qualified type name, or to an on-demand package name as defined by
	 * ImportDeclaration (JLS2 7.5). For example, <code>"java.util.Hashtable"</code>
	 * or <code>"java.util.*"</code>.
	 *
	 * @param imports the list of import names
	 */
	public void setImports(String[] imports);
	
}

