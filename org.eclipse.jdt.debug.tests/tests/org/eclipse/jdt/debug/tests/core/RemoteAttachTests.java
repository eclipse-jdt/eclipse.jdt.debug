/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;

import com.sun.jdi.connect.Connector;

/**
 * Tests attaching to a remote java application
 */
public class RemoteAttachTests extends AbstractDebugTest {
	
	public RemoteAttachTests(String name) {
		super(name);
	}

	public void testAttach() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(52, typeName);
		
		// create launch config to launch VM in debug mode waiting for attach
		ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationWorkingCopy config = type.newInstance(null, "Launch Remote VM");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "Breakpoints");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, getJavaProject().getElementName());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Djava.compiler=NONE -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8000,suspend=y,server=y");
		ILaunchConfiguration launchRemoteVMConfig = config.doSave();
		
		// create a launch config to do the attach
		type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION);
		config = type.newInstance(null, "Remote Breakpoints");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, getJavaProject().getElementName());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, true);
		IVMConnector connector = JavaRuntime.getVMConnector(IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);
		Map def = connector.getDefaultArguments();
		Map argMap = new HashMap(def.size());
		Iterator iter = connector.getArgumentOrder().iterator();
		while (iter.hasNext()) {
			String key = (String)iter.next();
			Connector.Argument arg = (Connector.Argument)def.get(key);
			argMap.put(key, arg.toString()); 
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, argMap);		
		ILaunchConfiguration attachConfig = config.doSave();		
			
		// launch remote VM
		ILaunch launch = launchRemoteVMConfig.launch(ILaunchManager.RUN_MODE, null);
		
		// attach	
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(attachConfig);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
			ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
			int lineNumber = breakpoint.getLineNumber();
			int stackLine = thread.getTopStackFrame().getLineNumber();
			assertTrue("line numbers of breakpoint and stack frame do not match", lineNumber == stackLine);
			breakpoint.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
		}		
	}

}
