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
package org.eclipse.jdt.internal.launching;


import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;

/**
 * Launch configuration delegate for a remote Java application.
 */
public class JavaRemoteApplicationLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(NLS.bind(LaunchingMessages.JavaRemoteApplicationLaunchConfigurationDelegate_Attaching_to__0_____1, configuration.getName()), 3);
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.subTask(LaunchingMessages.JavaRemoteApplicationLaunchConfigurationDelegate_Verifying_launch_attributes____1);

			String connectorId = getVMConnectorId(configuration);
			IVMConnector connector = null;
			if (connectorId == null) {
				connector = JavaRuntime.getDefaultVMConnector();
			} else {
				connector = JavaRuntime.getVMConnector(connectorId);
			}
			if (connector == null) {
				abort(LaunchingMessages.JavaRemoteApplicationLaunchConfigurationDelegate_Connector_not_specified_2, null, IJavaLaunchConfigurationConstants.ERR_CONNECTOR_NOT_AVAILABLE);
			}

			Map<String, String> argMap = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, (Map<String, String>)null);

	        int connectTimeout = Platform.getPreferencesService().getInt(
	        		LaunchingPlugin.ID_PLUGIN,
	        		JavaRuntime.PREF_CONNECT_TIMEOUT,
	        		JavaRuntime.DEF_CONNECT_TIMEOUT,
	        		null);
	        argMap.put("timeout", Integer.toString(connectTimeout));  //$NON-NLS-1$

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

			monitor.worked(1);

			monitor.subTask(LaunchingMessages.JavaRemoteApplicationLaunchConfigurationDelegate_Creating_source_locator____2);
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);

			// connect to remote VM
			connector.connect(argMap, monitor, launch);

			// check for cancellation
			if (monitor.isCanceled()) {
				IDebugTarget[] debugTargets = launch.getDebugTargets();
	            for (int i = 0; i < debugTargets.length; i++) {
	                IDebugTarget target = debugTargets[i];
	                if (target.canDisconnect()) {
	                    target.disconnect();
	                }
	            }
	            return;
			}
		}
		finally {
			monitor.done();
		}
	}

}
