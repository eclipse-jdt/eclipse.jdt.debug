/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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

package org.eclipse.jdt.debug.tests.ui.performance;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Tests opening of the launch configuraiton dialog
 */
public class OpenLaunchConfigurationDialogTests extends AbstractDebugPerformanceTest {

    /**
     * Constructor
     * @param name
     */
    public OpenLaunchConfigurationDialogTests(String name) {
        super(name);
    }

    /**
     * The local java application id
     */
    public static String fgIdentifier= "org.eclipse.jdt.launching.localJavaApplication";

    /**
     * Tests opening the LCD on a given launch configuration on the java tab group 100 times
     */
    public void testOpenJavaProgramLaunchConfigurationDialog1() {
        // cold run
        ILaunchConfiguration config = getLaunchConfiguration("Breakpoints");
		IStructuredSelection selection= new StructuredSelection(config);
		for (int i = 0; i < 100; i++) {
		    openLCD(selection, fgIdentifier);
        }

		commitMeasurements();
		assertPerformance();
    }

    /**
     * Tests opening the LCD on a specific launch configuration on the java tab group 1 time
     */
    public void testOpenJavaProgramLaunchConfigurationDialog2() {
        // warm run..depends on testOpenJavaProgramLaunchConfigurationDialog1 for cold start
        ILaunchConfiguration config = getLaunchConfiguration("Breakpoints");
		IStructuredSelection selection = new StructuredSelection(config);
		openLCD(selection, fgIdentifier);
    }

    /**
     * Helper method to open the launch configuration dialog
     * @param selection
     * @param groupIdentifier
     */
    private void openLCD(final IStructuredSelection selection, final String groupIdentifier) {
        //set a status to go to the classpath tab
	    IStatus status = new Status(IStatus.INFO, IJavaDebugUIConstants.PLUGIN_ID, 1000, IInternalDebugCoreConstants.EMPTY_STRING, null);
		LaunchConfigurationsDialog dialog= new LaunchConfigurationsDialog(DebugUIPlugin.getShell(), DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(groupIdentifier));
		dialog.setBlockOnOpen(false);
		dialog.setOpenMode(LaunchConfigurationsDialog.LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_SELECTION);
		dialog.setInitialSelection(selection);
		dialog.setInitialStatus(status);
		startMeasuring();
		dialog.open();
		dialog.close();
		stopMeasuring();
    }
}
