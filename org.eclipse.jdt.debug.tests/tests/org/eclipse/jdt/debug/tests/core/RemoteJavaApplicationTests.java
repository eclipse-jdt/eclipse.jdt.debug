/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventDetailWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;

import com.sun.jdi.connect.Connector;

/**
 * Tests attaching to a remote java application
 */
public class RemoteJavaApplicationTests extends AbstractDebugTest {

	public RemoteJavaApplicationTests(String name) {
		super(name);
	}

	/**
	 * Tests a Standard (Socket Attach) VM connection.
	 * @throws Exception
	 */
	public void testAttach() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(52, typeName);

		// create launch config to launch VM in debug mode waiting for attach
		ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationWorkingCopy config = type.newInstance(null, "Launch Remote VM");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "Breakpoints");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, get14Project().getElementName());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Djava.compiler=NONE -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8000,suspend=y,server=y");
		// use 'java' instead of 'javaw' to launch tests (javaw is problematic on JDK1.4.2)
		Map<String, String> map = new HashMap<>(1);
		map.put(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND, "java");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, map);
		ILaunchConfiguration launchRemoteVMConfig = config.doSave();

		// create a launch config to do the attach
		type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION);
		config = type.newInstance(null, "Remote Breakpoints");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, get14Project().getElementName());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, true);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR, IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);
		IVMConnector connector = JavaRuntime.getVMConnector(IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);
		Map<String, ? extends Connector.Argument> def = connector.getDefaultArguments();
		Map<String, String> argMap = new HashMap<>(def.size());
		Iterator<String> iter = connector.getArgumentOrder().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Connector.Argument arg = def.get(key);
			argMap.put(key, arg.toString());
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, argMap);
		ILaunchConfiguration attachConfig = config.doSave();

		// launch remote VM
		ILaunch launch = launchRemoteVMConfig.launch(ILaunchManager.RUN_MODE, null);

		// attach
		IJavaThread thread= null;
		try {
			CoreException exception = null;
			int attempts = 0;
			boolean connected = false;
			while ((attempts < 2) && !connected) {
				try {
					attempts++;
					exception = null;
					thread= launchToBreakpoint(attachConfig);
					connected = true;
				} catch (CoreException e) {
					// try again, in case the VM is not yet ready
					exception = e;
					Thread.sleep(2000);
				}
			}
			if (exception != null) {
				throw exception;
			}
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

	/**
	 * Tests a Standard (Socket Listen) VM connection.
	 * @throws Exception
	 */
	public void testListen() throws Exception {
		String typeName = "Breakpoints";
		createLineBreakpoint(52, typeName);

		// create a launch config to listen for the vm
		ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION);
		ILaunchConfigurationWorkingCopy config = type.newInstance(null, "Remote Breakpoints");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, get14Project().getElementName());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, true);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR, IJavaLaunchConfigurationConstants.ID_SOCKET_LISTEN_VM_CONNECTOR);
		IVMConnector connector = JavaRuntime.getVMConnector(IJavaLaunchConfigurationConstants.ID_SOCKET_LISTEN_VM_CONNECTOR);
		Map<String, ? extends Connector.Argument> def = connector.getDefaultArguments();
		Map<String, String> argMap = new HashMap<>(def.size());
		Iterator<String> iter = connector.getArgumentOrder().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Connector.Argument arg = def.get(key);
			argMap.put(key, arg.toString());
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, argMap);
		ILaunchConfiguration listenConfig = config.doSave();

		// create launch config to launch VM that will connect to the waiting listener
		type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		config = type.newInstance(null, "Launch Remote VM");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "Breakpoints");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, get14Project().getElementName());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Djava.compiler=NONE -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8000,suspend=y,server=n");
		// use 'java' instead of 'javaw' to launch tests (javaw is problematic on JDK1.4.2)
		Map<String, String> map = new HashMap<>(1);
		map.put(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND, "java");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, map);
		ILaunchConfiguration launchRemoteVMConfig = config.doSave();

		ILaunch listenerLaunch = null;
		IJavaThread thread = null;


		try {

			DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.MODEL_SPECIFIC, IProcess.class, IJavaLaunchConfigurationConstants.DETAIL_CONFIG_READY_TO_ACCEPT_REMOTE_VM_CONNECTION);
			waiter.setTimeout(DEFAULT_TIMEOUT);

			IProcess process = null;
			CoreException exception = null;
			int attempts = 0;
			boolean connected = false;
			while ((attempts < 2) && !connected) {
				try {
					attempts++;
					exception = null;
					process = (IProcess)launchAndWait(listenConfig,waiter);
					connected = true;
				} catch (CoreException e) {
					// try again, in case the VM is not yet ready
					exception = e;
					Thread.sleep(2000);
				}
			}
			if (exception != null) {
				throw exception;
			}

			assertNotNull("Launch of the not successful", process);
			listenerLaunch = process.getLaunch();


			waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
			waiter.setTimeout(DEFAULT_TIMEOUT);

			exception = null;
			attempts = 0;
			connected = false;
			while ((attempts < 2) && !connected) {
				try {
					attempts++;
					exception = null;
					thread = (IJavaThread)launchAndWait(launchRemoteVMConfig,ILaunchManager.RUN_MODE,waiter,true);
					connected = true;
				} catch (CoreException e) {
					// try again, in case the VM is not yet ready
					exception = e;
					Thread.sleep(2000);
				}
			}
			if (exception != null) {
				throw exception;
			}
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
			if (listenerLaunch != null){
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(listenerLaunch);
			}
		}
	}

}
