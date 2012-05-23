/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.sourcelookup;


import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
/**
 * Locates source elements in an archive (zip) in the local file system. Returns
 * instances of <code>ZipEntryStorage</code>.
 * <p>
 * This class may be instantiated.
 * </p>
 * @see IJavaSourceLocation
 * @since 2.0
 * @deprecated In 3.0, the debug platform provides source lookup facilities that
 *  should be used in place of the Java source lookup support provided in 2.0.
 *  The new facilities provide a source lookup director that coordinates source
 *  lookup among a set of participants, searching a set of source containers.
 *  See the following packages: <code>org.eclipse.debug.core.sourcelookup</code>
 *  and <code>org.eclipse.debug.core.sourcelookup.containers</code>. This class
 *  has been replaced by the following classes:
 *  <code>org.eclipse.debug.core.sourcelookup.containers.ArchiveSourceContainer</code>
 *  and <code>org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer</code>.
 * @noextend This class is not intended to be sub-classed by clients.
 */
@Deprecated
public class ArchiveSourceLocation extends PlatformObject implements IJavaSourceLocation {
	
	/**
	 * Cache of shared zip files. Zip files are closed
	 * when the launching plug-in is shutdown.
	 */
	private static HashMap<String, ZipFile> fZipFileCache = new HashMap<String, ZipFile>(5);

	/**
	 * Returns a zip file with the given name
	 * 
	 * @param name zip file name
	 * @return The zip file with the given name
	 * @exception IOException if unable to create the specified zip
	 * 	file
	 */
	private static ZipFile getZipFile(String name) throws IOException {
		synchronized (fZipFileCache) {
			ZipFile zip = fZipFileCache.get(name);
			if (zip == null) {
				zip = new ZipFile(name);
				fZipFileCache.put(name, zip);
			}
			return zip;
		}
	}
	
	/**
	 * Closes all zip files that have been opened,
	 * and removes them from the zip file cache.
	 * This method is only to be called by the launching
	 * plug-in.
	 */
	public static void closeArchives() {
		synchronized (fZipFileCache) {
			Iterator<ZipFile> iter = fZipFileCache.values().iterator();
			while (iter.hasNext()) {
				ZipFile file = iter.next();
				synchronized (file) {
					try {
						file.close();
					} catch (IOException e) {
						LaunchingPlugin.log(e);
					}
				}
			}
			fZipFileCache.clear();
		}
	}
	
	/**
	 * The root source folder in the archive
	 */
	private IPath fRootPath;
	
	/**
	 * Whether the root path has been detected (or set)
	 */
	private boolean fRootDetected = false;
	
	/**
	 * The name of the archive
	 */
	private String fName;

	/**
	 * Constructs a new empty source location to be initialized with 
	 * a memento.
	 */
	public ArchiveSourceLocation() {
	}
		
	/**
	 * Constructs a new source location that will retrieve source
	 * elements from the zip file with the given name.
	 * 
	 * @param archiveName zip file
	 * @param sourceRoot a path to the root source folder in the
	 *  specified archive, or <code>null</code> if the root source folder
	 *  is the root of the archive
	 */
	public ArchiveSourceLocation(String archiveName, String sourceRoot) {
		super();
		setName(archiveName);
		setRootPath(sourceRoot);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation#findSourceElement(java.lang.String)
	 */
	public Object findSourceElement(String name) throws CoreException {
		try {
			if (getArchive() == null) {
				return null;
			}
			
			boolean possibleInnerType = false;
			String pathStr= name.replace('.', '/');
			int lastSlash = pathStr.lastIndexOf('/');
			String typeName = pathStr;
			do {
				IPath entryPath = new Path(typeName + ".java"); //$NON-NLS-1$
				autoDetectRoot(entryPath);
				if (getRootPath() != null) {
					entryPath = getRootPath().append(entryPath);
				}				
				ZipEntry entry = getArchive().getEntry(entryPath.toString());
				if (entry != null) {
					return new ZipEntryStorage(getArchive(), entry);
				}
				int index = typeName.lastIndexOf('$');
				if (index > lastSlash) {
					typeName = typeName.substring(0, index);
					possibleInnerType = true;
				} else {
					possibleInnerType = false;
				}						
			} while (possibleInnerType);						
			return null;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, 
				NLS.bind(LaunchingMessages.ArchiveSourceLocation_Unable_to_locate_source_element_in_archive__0__1, new String[] {getName()}), e)); 
		}
	}
	
	/**
	 * Automatically detect the root path, if required.
	 * 
	 * @param path source file name, excluding root path
	 * @throws CoreException  if unable to detect the root path for this source archive
	 */
	private void autoDetectRoot(IPath path) throws CoreException {
		if (!fRootDetected) {
			ZipFile zip = null;
			try {
				zip = getArchive();
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, 
					NLS.bind(LaunchingMessages.ArchiveSourceLocation_Exception_occurred_while_detecting_root_source_directory_in_archive__0__1, new String[] {getName()}), e)); 
			}
			synchronized (zip) {
				Enumeration<? extends ZipEntry> entries = zip.entries();
				String fileName = path.toString();
				try {
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						String entryName = entry.getName();
						if (entryName.endsWith(fileName)) {
							int rootLength = entryName.length() - fileName.length();
							if (rootLength > 0) {
								String root = entryName.substring(0, rootLength);
								setRootPath(root);
							}
							fRootDetected = true;
							return;
						}
					}
				} catch (IllegalStateException e) {
					throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, 
						NLS.bind(LaunchingMessages.ArchiveSourceLocation_Exception_occurred_while_detecting_root_source_directory_in_archive__0__2, new String[] {getName()}), e)); 
				}
			}
		}
	}

	/**
	 * Returns the archive associated with this source
	 * location.
	 * 
	 * @return zip file
	 * @throws IOException if unable to create the zip
	 * 	file associated with this location
	 */
	protected ZipFile getArchive() throws IOException {
		return getZipFile(getName());
	}
	
	/**
	 * Sets the location of the root source folder within
	 * the archive, or <code>null</code> if the root source
	 * folder is the root of the archive
	 * 
	 * @param path the location of the root source folder within
	 * the archive, or <code>null</code> if the root source
	 * folder is the root of the archive
	 */
	private void setRootPath(String path) {
		if (path == null || path.trim().length() == 0) {
			fRootPath = null;
		} else {
			fRootPath = new Path(path);
			fRootDetected = true;
		}
	}
	
	/**
	 * Returns the location of the root source folder within
	 * the archive, or <code>null</code> if the root source
	 * folder is the root of the archive
	 * 
	 * @return the location of the root source folder within
	 * the archive, or <code>null</code> if the root source
	 * folder is the root of the archive
	 */
	public IPath getRootPath() {
		return fRootPath;
	}	
	
	/**
	 * Returns the name of the archive associated with this 
	 * source location
	 * 
	 * @return the name of the archive associated with this
	 *  source location
	 */
	public String getName() {
		return fName;
	}
	
	/**
	 * Sets the name of the archive associated with this 
	 * source location
	 * 
	 * @param name the name of the archive associated with this
	 *  source location
	 */
	private void setName(String name) {
		fName = name;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {		
		return object instanceof ArchiveSourceLocation &&
			 getName().equals(((ArchiveSourceLocation)object).getName());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getName().hashCode();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation#getMemento()
	 */
	public String getMemento() throws CoreException {
		Document doc = DebugPlugin.newDocument();
		Element node = doc.createElement("archiveSourceLocation"); //$NON-NLS-1$
		doc.appendChild(node);
		node.setAttribute("archivePath", getName()); //$NON-NLS-1$
		if (getRootPath() != null) {
			node.setAttribute("rootPath", getRootPath().toString()); //$NON-NLS-1$
		}
	
		return DebugPlugin.serializeDocument(doc); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation#initializeFrom(java.lang.String)
	 */
	public void initializeFrom(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
												
			String path = root.getAttribute("archivePath"); //$NON-NLS-1$
			if (isEmpty(path)) {
				abort(LaunchingMessages.ArchiveSourceLocation_Unable_to_initialize_source_location___missing_archive_path__3, null); 
			}
			String rootPath = root.getAttribute("rootPath"); //$NON-NLS-1$
			
			setName(path);
			setRootPath(rootPath);
			return;
		} catch (ParserConfigurationException e) {
			ex = e;			
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		abort(LaunchingMessages.ArchiveSourceLocation_Exception_occurred_initializing_source_location__5, ex);		 
	}

	private boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}
	
	/*
	 * Throws an internal error exception
	 */
	private void abort(String message, Throwable e)	throws CoreException {
		IStatus s = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message, e);
		throw new CoreException(s);		
	}	
}
