package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * <b>THIS INTERFACE IS YET EXPERIMENTAL AND SUBJECT TO CHANGE</b>.
 * 
 * Represents an entry on the runtime classpath. A runtime classpath entry
 * may refer to one of the following:
 * <ul>
 * 	<li>A Java project (type <code>PROJECT</code>) - a project entry refers
 * 		to all of the built classes in a project, and resolves to the output
 * 		location the associated Java project.</li>
 * 	<li>A jar (type <code>ARCHIVE</code>) - an archive refers to a jar, zip, or
 * 		folder in the workspace or in the local file system containing class
 * 		files. An archive may have attached source.</li>
 * 	<li>A variable (type <code>VARIABLE</code>) - a variable refers to a 
 * 		classpath variable, which may refer to a jar.</li>
 * 	<li>A library (type <code>LIBRARY</code>) - a library refers to classpath
 * 		conatiner variable which refers to a collection of archives derived
 * 		dynamically.</li>
 * </ul>
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 2.0
 */
public interface IRuntimeClasspathEntry {
	
	/**
	 * Identifier for project entries.
	 */
	public static final int PROJECT = 1;
	
	/**
	 * Identifier for archive entries.
	 */
	public static final int ARCHIVE = 2;	
		
	/**
	 * Identifier for variable entries.
	 */
	public static final int VARIABLE = 3;
	
	/**
	 * Identifier for library entries.
	 */
	public static final int LIBRARY = 4;

	/**
	 * Identifier for entries that appear on the
	 * bootstrap path by default.
	 */
	public static final int STANDARD_CLASSES = 1;	
	
	/**
	 * Identifier for entries that should appear on the
	 * bootstrap explicitly.
	 */
	public static final int BOOTSTRAP_CLASSES = 2;	
		
	/**
	 * Identifier for entries that should appear on the
	 * user classpath.
	 */
	public static final int USER_CLASSES = 3;	
	
	/**
	 * Returns this classpath entry's type, one of:
	 * <ul>
	 * <li><code>PROJECT</code></li>
	 * <li><code>ARCHIVE</code></li>
	 * <li><code>VARIABLE</code></li>
	 * <li><code>LIBRARY</code></li>
	 * </ul>
	 * 
	 * @return this classpath entry's type
	 */
	public int getType();
	
	/**
	 * Returns a memento for this classpath entry.
	 * 
	 * @return a memento for this classpath entry
	 * @exception CoreException if an exception occurrs generating a memento
	 */
	public String getMemento() throws CoreException;
	
	/**
	 * Returns the path associated with this entry. The format of the
	 * path returned depends on this entry's type:
	 * <ul>
	 * <li><code>PROJECT</code> - the absolute path of the associated
	 * 		project resource.</li>
	 * <li><code>ARCHIVE</code> - the absolute path of the assoicated archive,
	 * 		which may or may not be in the workspace.</li>
	 * <li><code>FOLDER</code> - the absolute path of the assoicated folder,
	 * 		which may or may not be in the workspace.</li>
	 * <li><code>VARIALBE</code> - the path corresponding to the associated
	 * 		classpath variable entry.</li>
	 * <li><code>LIBRARY</code> - the path corresponding to the associated
	 * 		classpath container variable entry.</li>
	 * </ul>
	 * 
	 * @return the path associated with this entry
	 * @see org.eclipse.jdt.core.IClasspathEntry#getPath()
	 */
	public IPath getPath();
		
	/**
	 * Returns the resource associated with this entry, or <code>null</code>
	 * if none. A project, library, or folder entry may be associated
	 * with a resource.
	 * 
	 * @return the resource associated with this entry, or <code>null</code>
	 */ 
	public IResource getResource();
	
	/**
	 * Returns the path to the source archive associated with this
	 * entry, or <code>null</code> if this classpath entry has no
	 * source attachment.
	 * <p>
	 * Only archive and variable entries may have source attachments.
	 * For archive entries, the result path (if present) locates a source
	 * archive. For variable entries, the result path (if present) has
	 * an analogous form and meaning as the variable path, namely the first segment 
	 * is the name of a classpath variable.
	 * </p>
	 *
	 * @return the path to the source archive, or <code>null</code> if none
	 */
	public IPath getSourceAttachmentPath();

	/**
	 * Sets the path to the source archive associated with this
	 * entry, or <code>null</code> if this classpath entry has no
	 * source attachment.
	 * <p>
	 * Only archive and variable entries may have source attachments.
	 * For archive entries, the path refers to a source
	 * archive. For variable entries, the path has
	 * an analogous form and meaning as the variable path, namely the
	 * first segment  is the name of a classpath variable.
	 * </p>
	 *
	 * @param path the path to the source archive, or <code>null</code> if none
	 */
	public void setSourceAttachmentPath(IPath path);
	
	/**
	 * Returns the path within the source archive where package fragments
	 * are located. An empty path indicates that packages are located at
	 * the root of the source archive. Returns a non-<code>null</code> value
	 * if and only if <code>getSourceAttachmentPath</code> returns 
	 * a non-<code>null</code> value.
	 *
	 * @return root path within the source archive, or <code>null</code> if
	 *    not applicable
	 */
	public IPath getSourceAttachmentRootPath();
	
	/**
	 * Sets the path within the source archive where package fragments
	 * are located. An empty path indicates that packages are located at
	 * the root of the source archive. Only valid if a source attachment
	 * path is also specified.
	 *
	 * @param path root path within the source archive, or <code>null</code>
	 */	
	public void setSourceAttachmentRootPath(IPath path);
	
	/**
	 * Returns a constant indicating where this entry should appear on the 
	 * runtime classpath by default. the bootstrap classpath or user classpath, or whether this entry is
	 * a standard bootstrap entry that does not need to appear on the classpath.
	 * The constant returned is one of:
	 * <ul>
	 * <li><code>STANDARD_CLASSES</code> - a standard entry does not need to appear
	 * 		on the runtime classpath</li>
	 * <li><code>BOOTSTRAP_CLASSES</code> - a bootstrap entry should appear on the
	 * 		boot path</li>
	 * <li><code>USER_CLASSES</code> - a user entry should appear on the path
	 * 		conatining user or application classes</li>
	 * </ul>
	 * 
	 * @return where this entry should appear on the runtime classpath
	 */
	public int getClasspathProperty();
	
	/**
	 * Retunrns absolute resolved entries for this entry, that should appear
	 * on the runtime classpath.
	 * 
	 * @return absolute resolved entries for this entry, that should appear
	 *  on the runtime classpath
	 */
	public String[] getResolvedPaths();
	
	/**
	 * Returns the name of the variable associated with this entry, or <code>null</code>
	 * if this entry is not of type <code>VARIABLE</code> or <code>LIBRARY</code>.
	 * 
	 * @return the name of the variable associated with this entry, or <code>null</code>
	 *  if this entry is not of type <code>VARIABLE</code> or <code>LIBRARY</code>
	 */
	public String getVariableName();
}
