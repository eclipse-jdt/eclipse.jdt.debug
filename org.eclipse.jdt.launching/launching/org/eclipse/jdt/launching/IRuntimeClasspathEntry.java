package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
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
	 */
	public String getMemento();
	
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
	 * Returns the path(s) to the source archive(s) associated with this
	 * entry, or <code>null</code> if this classpath entry has no
	 * source attachment(s).
	 * <p>
	 * Only archive, variable, and library entries may have source attachments.
	 * </p>
	 *
	 * @return the path(s) to the source archive(s), or <code>null</code> if none
	 */
	public IPath[] getSourceAttachmentPaths();

	/**
	 * Returns the path(s) within the source archive(s) where package fragments
	 * are located. An empty path indicates that packages are located at
	 * the root of the source archive(s). Returns a non-<code>null</code> value
	 * if and only if <code>getSourceAttachmentPaths</code> returns 
	 * a non-<code>null</code> value.
	 *
	 * @return the path(s) within the source archive(s), or <code>null</code> if
	 *    not applicable
	 */
	public IPath[] getSourceAttachmentRootPaths();	
	
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
}
