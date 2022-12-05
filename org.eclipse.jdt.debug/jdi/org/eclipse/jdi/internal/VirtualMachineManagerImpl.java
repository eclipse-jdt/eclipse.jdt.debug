/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jdi.internal.connect.SocketAttachingConnectorImpl;
import org.eclipse.jdi.internal.connect.SocketLaunchingConnectorImpl;
import org.eclipse.jdi.internal.connect.SocketListeningConnectorImpl;
import org.eclipse.jdi.internal.connect.SocketRawLaunchingConnectorImpl;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugOptions;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

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
	List<VirtualMachine> fConnectedVMs = new ArrayList<>();

	/** Name of verbose file. */
	private String fVerboseFile;

	/**
	 * Creates new VirtualMachineManagerImpl.
	 */
	public VirtualMachineManagerImpl() {

		getPreferences();

		// See if verbose info must be given.
		if (isVerboseTracingEnabled()) {
			OutputStream out = null;
			if (fVerboseFile != null && fVerboseFile.length() > 0) {
				try {
					out = new FileOutputStream(fVerboseFile);
				} catch (IOException e) {
					JDIDebugPlugin.logError(JDIMessages.VirtualMachineManagerImpl_Could_not_open_verbose_file___1
									+ fVerboseFile
									+ JDIMessages.VirtualMachineManagerImpl_____2
							, e); //
				}
			}
			if (out == null) {
				fVerbosePrintWriter = new PrintWriter(new StringWriter()) {
					@Override
					public void flush() {
						super.flush();
						StringWriter writer = new StringWriter();
						synchronized (lock) {
							JDIDebugOptions.trace(JDIDebugOptions.DEBUG_JDI_VERBOSE_FLAG, this.out.toString(), null);
							this.out = writer;
							this.lock = writer;
						}
					}
				};

			} else {
				fVerbosePrintWriter = new PrintWriter(out);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#majorInterfaceVersion()
	 */
	@Override
	public int majorInterfaceVersion() {
		return MAJOR_INTERFACE_VERSION;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#minorInterfaceVersion()
	 */
	@Override
	public int minorInterfaceVersion() {
		return MINOR_INTERFACE_VERSION;
	}

	public static boolean isVerboseTracingEnabled() {
		return JDIDebugOptions.DEBUG_JDI_VEBOSE;
	}

	static String getTracingFileName() {
		return JDIDebugOptions.DEBUG_JDI_VEBOSE_FILE;
	}

	/**
	 * Loads the user preferences from .options file
	 */
	private void getPreferences() {
		if (isVerboseTracingEnabled()) {
			fVerboseFile = getTracingFileName();
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
	@Override
	public List<VirtualMachine> connectedVirtualMachines() {
		return fConnectedVMs;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#allConnectors()
	 */
	@Override
	public List<Connector> allConnectors() {
		List<Connector> result = new ArrayList<>(attachingConnectors());
		result.addAll(launchingConnectors());
		result.addAll(listeningConnectors());
		return result;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#attachingConnectors()
	 */
	@Override
	public List<AttachingConnector> attachingConnectors() {
		ArrayList<AttachingConnector> list = new ArrayList<>(1);
		list.add(new SocketAttachingConnectorImpl(this));
		return list;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#launchingConnectors()
	 */
	@Override
	public List<LaunchingConnector> launchingConnectors() {
		ArrayList<LaunchingConnector> list = new ArrayList<>(2);
		list.add(new SocketLaunchingConnectorImpl(this));
		list.add(new SocketRawLaunchingConnectorImpl(this));
		return list;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#listeningConnectors()
	 */
	@Override
	public List<ListeningConnector> listeningConnectors() {
		ArrayList<ListeningConnector> list = new ArrayList<>(1);
		list.add(new SocketListeningConnectorImpl(this));
		return list;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#defaultConnector()
	 */
	@Override
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
	@Override
	public VirtualMachine createVirtualMachine(Connection connection) throws IOException {
		VirtualMachineImpl vmImpl = new VirtualMachineImpl(connection);
		return vmImpl;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachineManager#createVirtualMachine(com.sun.jdi.connect.spi.Connection, java.lang.Process)
	 */
	@Override
	public VirtualMachine createVirtualMachine(Connection connection, Process process) throws IOException {
		VirtualMachineImpl vmImpl = new VirtualMachineImpl(connection);
		vmImpl.setLaunchedProcess(process);
		return vmImpl;
	}
}
