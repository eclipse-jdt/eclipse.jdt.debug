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
package org.eclipse.jdi.internal;

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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jdi.internal.connect.SocketAttachingConnectorImpl;
import org.eclipse.jdi.internal.connect.SocketLaunchingConnectorImpl;
import org.eclipse.jdi.internal.connect.SocketListeningConnectorImpl;
import org.eclipse.jdi.internal.connect.SocketRawLaunchingConnectorImpl;
import org.eclipse.jdt.debug.core.JDIDebugModel;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.spi.Connection;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 */
public class VirtualMachineManagerImpl implements VirtualMachineManager {
	/** Major interface version. */
	public static int MAJOR_INTERFACE_VERSION = 1;
	/** Minor interface version. */
	public static int MINOR_INTERFACE_VERSION = 5;
	/**
	 * PrintWriter where verbose info is written to, null if no verbose must be
	 * given.
	 */
	private PrintWriter fVerbosePrintWriter = null;
	/** List of all VMs that are currently connected. */
	List<VirtualMachine> fConnectedVMs = new ArrayList<VirtualMachine>();
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
					System.out
							.println(JDIMessages.VirtualMachineManagerImpl_Could_not_open_verbose_file___1
									+ fVerboseFile
									+ JDIMessages.VirtualMachineManagerImpl_____2
									+ e); //
				}
			} else {
				out = System.out;
			}
			fVerbosePrintWriter = new PrintWriter(out);
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#majorInterfaceVersion()
	 */
	public int majorInterfaceVersion() {
		return MAJOR_INTERFACE_VERSION;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#minorInterfaceVersion()
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
		if (url == null) {
			return;
		}

		try {
			InputStream stream = url.openStream();
			PropertyResourceBundle prefs = new PropertyResourceBundle(stream);

			try {
				fVerbose = Boolean
						.valueOf(prefs.getString("User.verbose")).booleanValue(); //$NON-NLS-1$
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
	 * @return Returns Timeout value for requests to VM, if not overridden for
	 *         the VM. This value is used to throw the exception
	 *         TimeoutException in JDI calls. NOTE: This is not in compliance
	 *         with the Sun's JDI.
	 */
	public int getGlobalRequestTimeout() {
		try {
			IPreferencesService srvc = Platform.getPreferencesService();
			if(srvc != null) {
				return Platform.getPreferencesService().getInt(
						JDIDebugModel.getPluginIdentifier(), 
						JDIDebugModel.PREF_REQUEST_TIMEOUT, 
						JDIDebugModel.DEF_REQUEST_TIMEOUT, 
						null);
			}
		} catch (NoClassDefFoundError e) {
		}
		// return the hard coded preference if the JDI debug plug-in does not
		// exist
		return JDIDebugModel.DEF_REQUEST_TIMEOUT;
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

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#connectedVirtualMachines()
	 */
	public List<VirtualMachine> connectedVirtualMachines() {
		return fConnectedVMs;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#allConnectors()
	 */
	public List<Connector> allConnectors() {
		List<Connector> result = new ArrayList<Connector>(attachingConnectors());
		result.addAll(launchingConnectors());
		result.addAll(listeningConnectors());
		return result;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#attachingConnectors()
	 */
	public List<AttachingConnector> attachingConnectors() {
		ArrayList<AttachingConnector> list = new ArrayList<AttachingConnector>(1);
		list.add(new SocketAttachingConnectorImpl(this));
		return list;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#launchingConnectors()
	 */
	public List<LaunchingConnector> launchingConnectors() {
		ArrayList<LaunchingConnector> list = new ArrayList<LaunchingConnector>(2);
		list.add(new SocketLaunchingConnectorImpl(this));
		list.add(new SocketRawLaunchingConnectorImpl(this));
		return list;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#listeningConnectors()
	 */
	public List<ListeningConnector> listeningConnectors() {
		ArrayList<ListeningConnector> list = new ArrayList<ListeningConnector>(1);
		list.add(new SocketListeningConnectorImpl(this));
		return list;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#defaultConnector()
	 */
	public LaunchingConnector defaultConnector() {
		return new SocketLaunchingConnectorImpl(this);
	}

	/**
	 * @return Returns PrintWriter to which verbose info must be written, or
	 *         null if no verbose must be given.
	 */
	public PrintWriter verbosePrintWriter() {
		return fVerbosePrintWriter;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#createVirtualMachine(com.sun.jdi.connect.spi.Connection)
	 */
	public VirtualMachine createVirtualMachine(Connection connection) throws IOException {
		VirtualMachineImpl vmImpl = new VirtualMachineImpl(connection);
		return vmImpl;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#createVirtualMachine(com.sun.jdi.connect.spi.Connection, java.lang.Process)
	 */
	public VirtualMachine createVirtualMachine(Connection connection, Process process) throws IOException {
		VirtualMachineImpl vmImpl = new VirtualMachineImpl(connection);
		vmImpl.setLaunchedProcess(process);
		return vmImpl;
	}
}
