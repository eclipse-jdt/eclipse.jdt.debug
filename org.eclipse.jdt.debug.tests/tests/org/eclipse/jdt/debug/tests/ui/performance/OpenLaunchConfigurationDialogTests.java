/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.ui.performance;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

public class OpenLaunchConfigurationDialogTests extends AbstractDebugPerformanceTest {

    public OpenLaunchConfigurationDialogTests(String name) {
        super(name);
    }

    public static String fgIdentifier= "org.eclipse.jdt.launching.localJavaApplication";
    
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
    
    public void testOpenJavaProgramLaunchConfigurationDialog2() {
        // warm run..depends on testOpenJavaProgramLaunchConfigurationDialog1 for cold start
        ILaunchConfiguration config = getLaunchConfiguration("Breakpoints");
		IStructuredSelection selection = new StructuredSelection(config);
		openLCD(selection, fgIdentifier);
    }

    private void openLCD(final IStructuredSelection selection, final String groupIdentifier) {
       
        //set a status to go to the classpath tab
	    IStatus status = new Status(IStatus.INFO, IJavaDebugUIConstants.PLUGIN_ID, 1000, "", null); //$NON-NLS-1$
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
