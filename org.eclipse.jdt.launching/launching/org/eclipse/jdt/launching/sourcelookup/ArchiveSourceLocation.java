package org.eclipse.jdt.launching.sourcelookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.launching.JavaLaunchConfigurationUtils;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
 
/**
 * Locates source elements in an archive (zip) in the local file system. Returns
 * instances of <code>ZipEntryStorage</code>.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @see IJavaSourceLocation
 * @since 2.0
 */
public class ArchiveSourceLocation extends PlatformObject implements IJavaSourceLocation {
	
	/**
	 * Cache of shared zip files. Zip files are closed
	 * when the launching plug-in is shutdown.
	 */
	private static HashMap fZipFileCache = new HashMap(5);

	/**
	 * Returns a zip file with the given name
	 * 
	 * @param name zip file name
	 * @exception IOException if unable to create the specified zip
	 * 	file
	 */
	private static ZipFile getZipFile(String name) throws IOException {
		ZipFile zip = (ZipFile)fZipFileCache.get(name);
		if (zip == null) {
			zip = new ZipFile(name);
			fZipFileCache.put(name, zip);
		}
		return zip;
	}
	
	/**
	 * Closes all zip files that have been opened,
	 * and removes them from the zip file cache.
	 * This method is only to be called by the launching
	 * plug-in.
	 */
	public static void closeArchives() {
		Iterator iter = fZipFileCache.values().iterator();
		while (iter.hasNext()) {
			ZipFile file = (ZipFile)iter.next();
			try {
				file.close();
			} catch (IOException e) {
				LaunchingPlugin.log(e);
			}
		}
		fZipFileCache.clear();
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
	 * @param archive zip file
	 * @param sourceRoot a path to the root source folder in the
	 *  specified archive, or <code>null</code> if the root source folder
	 *  is the root of the archive
	 */
	public ArchiveSourceLocation(String archiveName, String sourceRoot) {
		super();
		setName(archiveName);
		setRootPath(sourceRoot);
	}
		
	/**
	 * @see IJavaSourceLocation#findSourceElement(String)
	 */
	public Object findSourceElement(String name) throws CoreException {
		try {
			if (getArchive() == null) {
				return null;
			}
			
			// guess at source name if an inner type
			String pathStr= name.replace('.', '/');
			int dotIndex= pathStr.lastIndexOf('/');
			int dollarIndex= pathStr.indexOf('$', dotIndex + 1);
			if (dollarIndex >= 0) {
				pathStr = pathStr.substring(0, dollarIndex);
			}		
			pathStr += ".java"; //$NON-NLS-1$
			IPath path = new Path(pathStr); 
			autoDetectRoot(path);
			if (getRootPath() != null) {
				path = getRootPath().append(path);
			}
			ZipEntry entry = getArchive().getEntry(path.toString());
			if (entry != null) {
				return new ZipEntryStorage(getArchive(), entry);
			}
			return null;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, 
				MessageFormat.format(LaunchingMessages.getString("ArchiveSourceLocation.Unable_to_locate_source_element_in_archive_{0}_1"), new String[] {getName()}), e)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Automatically detect the root path, if required.
	 * 
	 * @param path source file name, excluding root path
	 */
	private void autoDetectRoot(IPath path) {
		if (!fRootDetected) {
			Enumeration entries = null;
			try {
				entries = getArchive().entries();
			} catch (IOException e) {
				LaunchingPlugin.log(e);
				return;
			}
			String fileName = path.toString();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();
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
		}
	}

	/**
	 * Returns the archive associated with this source
	 * location.
	 * 
	 * @return zip file
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
	 * folder is the root of the arhcive
	 * 
	 * @return the location of the root source folder within
	 * the archive, or <code>null</code> if the root source
	 * folder is the root of the arhcive
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
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {		
		return object instanceof ArchiveSourceLocation &&
			 getName().equals(((ArchiveSourceLocation)object).getName());
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getName().hashCode();
	}	
	
	/**
	 * @see IJavaSourceLocation#getMemento()
	 */
	public String getMemento() throws CoreException {
		Document doc = new DocumentImpl();
		Element node = doc.createElement("archiveSourceLocation"); //$NON-NLS-1$
		doc.appendChild(node);
		node.setAttribute("archivePath", getName()); //$NON-NLS-1$
		if (getRootPath() != null) {
			node.setAttribute("rootPath", getRootPath().toString()); //$NON-NLS-1$
		}
		
		try {
			return JavaLaunchConfigurationUtils.serializeDocument(doc);
		} catch (IOException e) {
			abort(MessageFormat.format(LaunchingMessages.getString("ArchiveSourceLocation.Unable_to_create_memento_for_archive_source_location_{0}_1"), new String[] {getName()}), e); //$NON-NLS-1$
		}
		// execution will not reach here
		return null;
	}

	/**
	 * @see IJavaSourceLocation#initializeFrom(String)
	 */
	public void initializeFrom(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
												
			String path = root.getAttribute("archivePath"); //$NON-NLS-1$
			if (isEmpty(path)) {
				abort(LaunchingMessages.getString("ArchiveSourceLocation.Unable_to_initialize_source_location_-_missing_archive_path._3"), null); //$NON-NLS-1$
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
		abort(LaunchingMessages.getString("ArchiveSourceLocation.Exception_occurred_initializing_source_location._5"), ex);		 //$NON-NLS-1$
	}

	private boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}
	
	/**
	 * Throws an internal error exception
	 */
	private void abort(String message, Throwable e)	throws CoreException {
		IStatus s = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message, e);
		throw new CoreException(s);		
	}	
}
