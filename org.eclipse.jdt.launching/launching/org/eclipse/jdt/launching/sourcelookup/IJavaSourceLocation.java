package org.eclipse.jdt.launching.sourcelookup;

import org.eclipse.core.runtime.CoreException;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * A source location defines the location of a repository
 * of source code. A source location is capable of retrieving
 * source elements.
 * <p>
 * For example, a source location could be a project, zip/archive
 * file, or a directory in the file system.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IJavaSourceLocation {
	
	/**
	 * Returns an object representing the source code
	 * for a type with the specified name, or <code>null</code>
	 * if none could be found. The name is
	 * a fully qualified type name, and may contain the '$'
	 * character when referring to inner types. For example,
	 * <code>java.lang.String</code>. The source element 
	 * returned is implementation specific - for example, a
	 * resource, a local file, a zip file entry, etc.
	 * 
	 * @param name fully qualified name of the type for which
	 * 		source is being searched for
	 * @return source element
	 * @exception CoreException if an exception occurrs while searching
	 *  for the specified source element
	 */
	public abstract Object findSourceElement(String name) throws CoreException;

}
