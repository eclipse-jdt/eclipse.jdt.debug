/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */package org.eclipse.jdt.launching;import java.text.MessageFormat;import java.util.ArrayList;import java.util.List;import org.eclipse.core.runtime.CoreException;import org.eclipse.core.runtime.IConfigurationElement;import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jdt.internal.launching.*;


/**
 * This is an abstract impelementation of the IVMType interface.
 * It handles the managment of IVMInstall instances and fetches
 * the IVMType id from the configuration markup.
 * Subclasses should implement
 * <ul>
 * <li><code>IVMInstall doCreateVMInstall(String id)</code></li>
 * <li><code>String getName()</code></li>
 * <li><code>IStatus validateInstallLocation(File installLocation)</code></li>
 * </ul>
 */

public abstract class AbstractVMInstallType implements IVMInstallType, IExecutableExtension {
	private List fVMs;
	private String fId;
	
	protected AbstractVMInstallType() {
		fVMs= new ArrayList(10);
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMType#getVMs()
	 */
	public IVMInstall[] getVMInstalls() {
		IVMInstall[] vms= new IVMInstall[fVMs.size()];
		return (IVMInstall[])fVMs.toArray(vms);
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMType#disposeVM(String)
	 */
	public void disposeVMInstall(String id) {
		for (int i= 0; i < fVMs.size(); i++) {
			IVMInstall vm= (IVMInstall)fVMs.get(i);
			if (vm.getId().equals(id)) {
				fVMs.remove(i);
				return;
			}
		}
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMType#getVM(String)
	 */
	public IVMInstall findVMInstall(String id) {
		for (int i= 0; i < fVMs.size(); i++) {
			IVMInstall vm= (IVMInstall)fVMs.get(i);
			if (vm.getId().equals(id)) {
				return vm;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMType#createVM(String)
	 */
	public IVMInstall createVMInstall(String id) throws IllegalArgumentException {
		if (findVMInstall(id) != null) {			String format= LaunchingMessages.getString("vmInstallType.duplicateVM"); //$NON-NLS-1$
			throw new IllegalArgumentException(MessageFormat.format(format, new String[] { id }));
		}
		IVMInstall vm= doCreateVMInstall(id);
		fVMs.add(vm);
		return vm;
	}
	
	/* (non-Javadoc)
	 * Subclasses should return a new instance of the appropriate
	 * IVMInstall subclass from this method.
	 * @param	id	The vm's id. The IVMInstall instance that is created must
	 * 				return <code>id</code> from its getId() method.
	 * 				Must not be null.
	 * @return	the newly created IVMInstall instance. Must not return
	 			null.
	 */
	protected abstract IVMInstall doCreateVMInstall(String id);

	/**
	 * Initializes the id parameter from the "id" attribute
	 * in the configuration markup.
	 * Subclasses should not override this method.
	 * @see IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String arg1, Object arg2) throws CoreException {
		fId= config.getAttribute("id"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMType#getId()
	 */
	public String getId() {
		return fId;
	}

}
