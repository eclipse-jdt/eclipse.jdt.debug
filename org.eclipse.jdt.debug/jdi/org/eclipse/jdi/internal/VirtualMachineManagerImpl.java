package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Vector;

import org.eclipse.jdi.internal.connect.SocketAttachingConnectorImpl;
import org.eclipse.jdi.internal.connect.SocketLaunchingConnectorImpl;
import org.eclipse.jdi.internal.connect.SocketListeningConnectorImpl;
import org.eclipse.jdi.internal.connect.SocketRawLaunchingConnectorImpl;

import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.LaunchingConnector;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class VirtualMachineManagerImpl implements VirtualMachineManager {
	/** Major interface version. */
	public static int MAJOR_INTERFACE_VERSION = 1;
	/** Minor interface version. */
	public static int MINOR_INTERFACE_VERSION = 0;
	/** Default for timeout on requests to Virtual Machine. */
	public static int DEFAULT_REQUEST_TIMEOUT = 10000;

	/** Timeout value for requests to VM if not overriden for a particular VM. */
	private int fGlobalRequestTimeout = DEFAULT_REQUEST_TIMEOUT;
	/** PrintWriter where verbose info is written to, null if no verbose must be given. */
	private PrintWriter fVerbosePrintWriter = null;
	/** List of all VMs that are currently connected. */
	Vector fConnectedVMs = new Vector();
	/** True if in verbose mode. */
	private boolean fVerbose = false;
	/** Name of verbose file. */
	private String fVerboseFile = null;

	/**
	 * Creates new VirtualMachineManagerImpl.
	 */
	public VirtualMachineManagerImpl() {
		
		getPreferences();
		
		// See if verbose info must be given.
		if (fVerbose) {
			OutputStream out;
			if (fVerboseFile != null && fVerboseFile.length() > 0) {
				try {
					out = new FileOutputStream(fVerboseFile);
				} catch (IOException e) {
					out = System.out;
					System.out.println(JDIMessages.getString("VirtualMachineManagerImpl.Could_not_open_verbose_file___1") + fVerboseFile + JDIMessages.getString("VirtualMachineManagerImpl.____2") + e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				out = System.out;
			}
			fVerbosePrintWriter = new PrintWriter(out);
		}
	}

	/**
	 * Returns the major version number of the JDI interface.
	 */
	public int majorInterfaceVersion() {
		return MAJOR_INTERFACE_VERSION;
	}
	
	/**
	 * Returns the minor version number of the JDI interface.
	 */
	public int minorInterfaceVersion() {
		return MINOR_INTERFACE_VERSION;
	}
	
	/**
	 * Loads the user preferences from the jdi.ini file.
	 */
	private void getPreferences() {
		// Get jdi.ini info.
		URL url = getClass().getResource("/jdi.ini"); //$NON-NLS-1$
		if (url == null)
			return;
			
		try {
			InputStream stream = url.openStream();
			PropertyResourceBundle prefs = new PropertyResourceBundle(stream);

				
			try {
				fGlobalRequestTimeout = Integer.parseInt(prefs.getString("User.timeout")); //$NON-NLS-1$
			} catch (NumberFormatException e) {
			} catch (MissingResourceException e) {
			}
			
			try {		
				fVerbose = Boolean.valueOf(prefs.getString("User.verbose")).booleanValue(); //$NON-NLS-1$
			} catch (MissingResourceException e) {
			}
			
			try {
				fVerboseFile = prefs.getString("Verbose.out"); //$NON-NLS-1$
			} catch (MissingResourceException e) {
			}

		} catch (IOException e) {
		}
		
	}

	
	/**
	 * @return Returns Timeout value for requests to VM, if not overridden for the VM.
	 * This value is used to throw the exception TimeoutException in JDI calls.
	 * NOTE: This is not in compliance with the Sun's JDI.
	 */
	public int getGlobalRequestTimeout() {
		return fGlobalRequestTimeout;
	}
	
	/**
	 * Adds a VM to the connected VM list. 
	 */
	public void addConnectedVM(VirtualMachineImpl vm) {
		fConnectedVMs.add(vm);
	}

	/**
	 * Removes a VM from the connected VM list. 
	 */
	public void removeConnectedVM(VirtualMachineImpl vm) {
		fConnectedVMs.remove(vm);
	}

	/**
	 * @return Returns all target VMs which are connected to the debugger. 
	 */
	public List connectedVirtualMachines() {
		return fConnectedVMs;
	}

	/**
	 * @return Returns all connectors.
	 */
	public List allConnectors() {
		Vector result = new Vector(attachingConnectors());
		result.addAll(launchingConnectors());
		result.addAll(listeningConnectors());
		return result;
	}

	/**
	 * @return Returns attaching connectors.
	 */
	public List attachingConnectors() {
		ArrayList list = new ArrayList(1);
		list.add(new SocketAttachingConnectorImpl(this));
		return list;
	}
		
	/**
	 * @return Returns launching connectors.
	 */
	public List launchingConnectors() {
		ArrayList list = new ArrayList(2);
		list.add(new SocketLaunchingConnectorImpl(this));
		list.add(new SocketRawLaunchingConnectorImpl(this));
		return list;
	}
		
	/**
	 * @return Returns listening connectors.
	 */
	public List listeningConnectors() {
		ArrayList list = new ArrayList(2);
		list.add(new SocketListeningConnectorImpl(this));
		return list;
	}
	
	/**
	 * @return Returns default connector.
	 */
	public LaunchingConnector defaultConnector() {
		return new SocketLaunchingConnectorImpl(this);
	}
	
	/**
	 * @return Returns PrintWriter to which verbose info must be written, or null if no verbose must be given.
	 */
	public PrintWriter verbosePrintWriter() {
		return fVerbosePrintWriter;
	}
}
