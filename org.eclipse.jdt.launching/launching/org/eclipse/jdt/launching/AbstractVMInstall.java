/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */package org.eclipse.jdt.launching;import java.io.File;import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.launching.*;


/**
 * An abstract implementation of IVMInstall.
 * Subclasses should only implement 
 * <code>IVMInstall getVMRunner(String mode)</code>
 */
public abstract class AbstractVMInstall implements IVMInstall {
	private IVMInstallType fType;
	private String fId;
	private String fName;
	private File fInstallLocation;	private int fDebuggerTimeout= 3000;	private LibraryLocation[] fSystemLibraryDescriptions;	
	/**
	 * Constructs a new AbstractVM.
	 * @param	type	The IVMType this vm belongs to.
	 * 					Must not be null
	 * @param	id		The id of this vm instance
	 * 					Must not be null;
	 * @throws	IllegalArgumentException	When any of the 
	 * 					parameters is null.
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
	public void setName(String name) {
		fName= name;
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
	public void setInstallLocation(File installLocation) {
		fInstallLocation= installLocation;
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

	/* (non-Javadoc)	 * @see IVMInstall#setDebuggerTimeout(int)
	 */
	public void setDebuggerTimeout(int milliseconds) {		if (milliseconds < 0)			throw new IllegalArgumentException(LaunchingMessages.getString("vmInstall.assert.timeoutPositive")); //$NON-NLS-1$		fDebuggerTimeout= milliseconds;
	}

	/* (non-Javadoc)	 * @see IVMInstall#getDebuggerTimeout()
	 */
	public int getDebuggerTimeout() {
		return fDebuggerTimeout;
	}
	/**	 * @see IVMInstall#getLibraryLocation()	 * 	 * XXX: to be removed	 */	public LibraryLocation getLibraryLocation() {		LibraryLocation[] locs = getLibraryLocations();		if (locs != null && locs.length > 0) {			return locs[0];		}
		return null;
	}

	/**
	 * @see IVMInstall#setLibraryLocation(LibraryLocation)	 * 	 * XXX: to be removed
	 */
	public void setLibraryLocation(LibraryLocation description) {		if (description == null) {			fSystemLibraryDescriptions = null;		} else {			setLibraryLocations(new LibraryLocation[] {description});		}
	}

	/**	 * @see IVMInstall#getLibraryLocations()	 */	public LibraryLocation[] getLibraryLocations() {		return fSystemLibraryDescriptions;	}	/**	 * @see IVMInstall#setLibraryLocations(LibraryLocation[])	 */	public void setLibraryLocations(LibraryLocation[] locations) {		fSystemLibraryDescriptions = locations;	}}
