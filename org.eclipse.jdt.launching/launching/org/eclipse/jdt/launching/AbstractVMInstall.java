package org.eclipse.jdt.launching;/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */import java.net.URL;import java.io.File;import org.eclipse.jdt.internal.launching.LaunchingMessages;

/**
 * Abstract implementation of a VM install. * <p> * Clients implenmenting VM installs should subclass this class. * </p>
 */
public abstract class AbstractVMInstall implements IVMInstall {
	private IVMInstallType fType;
	private String fId;
	private String fName;
	private File fInstallLocation;	private LibraryLocation[] fSystemLibraryDescriptions;	private URL fJavadocLocation;	
	/**
	 * Constructs a new VM install.	 * 
	 * @param	type	The type of this VM install.
	 * 					Must not be <code>null</code>
	 * @param	id		The unique identifier of this VM instance
	 * 					Must not be <code>null</code>.
	 * @throws	IllegalArgumentException	if any of the required
	 * 					parameters are <code>null</code>.
	 */
	public AbstractVMInstall(IVMInstallType type, String id) {
		if (type == null)
			throw new IllegalArgumentException(LaunchingMessages.getString("vmInstall.assert.typeNotNull")); //$NON-NLS-1$
		if (id == null)
			throw new IllegalArgumentException(LaunchingMessages.getString("vmInstall.assert.idNotNull")); //$NON-NLS-1$
		fType= type;
		fId= id;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#getId()
	 */
	public String getId() {
		return fId;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#getName()
	 */
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#setName(String)
	 */
	public void setName(String name) {		if (!name.equals(fName)) {			PropertyChangeEvent event = new PropertyChangeEvent(this, IVMInstallChangedListener.PROPERTY_NAME, fName, name);
			fName= name;			JavaRuntime.fireVMChanged(event);		}
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#getInstallLocation()
	 */
	public File getInstallLocation() {
		return fInstallLocation;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#setInstallLocation(File)
	 */
	public void setInstallLocation(File installLocation) {		if (!installLocation.equals(fInstallLocation)) {			PropertyChangeEvent event = new PropertyChangeEvent(this, IVMInstallChangedListener.PROPERTY_INSTALL_LOCATION, fInstallLocation, installLocation);
			fInstallLocation= installLocation;			JavaRuntime.fireVMChanged(event);		}
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMInstall#getVMInstallType()
	 */
	public IVMInstallType getVMInstallType() {
		return fType;
	}

	/* (non-Javadoc)	 * @see IVMInstall#getVMRunner(String)
	 */
	public IVMRunner getVMRunner(String mode) {
		return null;
	}

	/**	 * @see IVMInstall#getLibraryLocations()	 */	public LibraryLocation[] getLibraryLocations() {		return fSystemLibraryDescriptions;	}	/**	 * @see IVMInstall#setLibraryLocations(LibraryLocation[])	 */	public void setLibraryLocations(LibraryLocation[] locations) {		if (locations == fSystemLibraryDescriptions) {			return;		}		if (locations != null && fSystemLibraryDescriptions != null) {			if (locations.length == fSystemLibraryDescriptions.length) {				int i = 0;				boolean equal = true;				while (i < locations.length && equal) {					equal = locations[i].equals(fSystemLibraryDescriptions[i]);					i++;				}				if (equal) {					// no change					return;				}			}		}		PropertyChangeEvent event = new PropertyChangeEvent(this, IVMInstallChangedListener.PROPERTY_LIBRARY_LOCATIONS, fSystemLibraryDescriptions, locations);		fSystemLibraryDescriptions = locations;		JavaRuntime.fireVMChanged(event);			}	/**	 * @see IVMInstall#getJavadocLocation()	 */	public URL getJavadocLocation() {		return fJavadocLocation;	}	/**	 * @see IVMInstall#setJavadocLocation(URL)	 */	public void setJavadocLocation(URL url) {		if (url == fJavadocLocation) {			return;		}		if (url != null && fJavadocLocation != null) {			if (url.equals(fJavadocLocation)) {				// no change				return;			}		}				PropertyChangeEvent event = new PropertyChangeEvent(this, IVMInstallChangedListener.PROPERTY_JAVADOC_LOCATION, fJavadocLocation, url);				fJavadocLocation = url;		JavaRuntime.fireVMChanged(event);	}}
