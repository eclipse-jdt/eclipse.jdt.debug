package org.eclipse.jdt.internal.launching;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

public class JavaAppletLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate
													implements IDebugEventSetListener {
		
	private static int fLaunchesCounter = 0;
	private static HashMap fEventSources = new HashMap();

	private File fTempFile;
	private ILaunchConfiguration fCurrentLaunchConfiguration;
	
	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
													throws CoreException {
			
		String mainTypeName = verifyMainTypeName(configuration);

		IJavaProject javaProject = getJavaProject(configuration);
		IType type = JavaLaunchConfigurationUtils.getMainType(mainTypeName, javaProject);
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
		IType javaLangApplet = JavaLaunchConfigurationUtils.getMainType("java.applet.Applet", javaProject); //$NON-NLS-1$
		if (!hierarchy.contains(javaLangApplet)) {
			abort(LaunchingMessages.getString("JavaAppletLaunchConfigurationDelegate.error.not_an_applet"), null, IJavaLaunchConfigurationConstants.ERR_NOT_AN_APPLET); //$NON-NLS-1$
		}

		IVMInstall vm = verifyVMInstall(configuration);

		IVMRunner runner = vm.getVMRunner(mode);
		if (runner == null) {
			if (mode == ILaunchManager.DEBUG_MODE) {
				abort(MessageFormat.format(LaunchingMessages.getString("JavaLocalApplicationLaunchConfigurationDelegate.JRE_{0}_does_not_support_debug_mode._1"), new String[]{vm.getName()}), null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST);  //$NON-NLS-1$
			} else {
				abort(MessageFormat.format(LaunchingMessages.getString("JavaLocalApplicationLaunchConfigurationDelegate.JRE_{0}_does_not_support_run_mode._2"), new String[]{vm.getName()}), null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST);  //$NON-NLS-1$
			}
		}

		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null) {
			workingDirName = workingDir.getAbsolutePath();
		}
		
		if (fLaunchesCounter == 0) {
			DebugPlugin.getDefault().addDebugEventListener(this);
		}
	
		// Program & VM args
		String javaPolicy = getJavaPolicyFile(configuration);
		ExecutionArguments execArgs = new ExecutionArguments(getVMArguments(configuration), ""); //$NON-NLS-1$
		// Classpath
		String[] classpath = getClasspath(configuration);
		
		// Create VM config
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration("sun.applet.AppletViewer", classpath); //$NON-NLS-1$
		runConfig.setProgramArguments(new String[] {buildHTMLFile(configuration)});
		String[] vmArgs = execArgs.getVMArgumentsArray();
		String[] realArgs = new String[vmArgs.length+1];
		System.arraycopy(vmArgs, 0, realArgs, 1, vmArgs.length);
		realArgs[0] = javaPolicy;
		runConfig.setVMArguments(realArgs);
		
		runConfig.setWorkingDirectory(workingDirName);

		// Bootpath
		String[] bootpath = getBootpath(configuration);
		runConfig.setBootClassPath(bootpath);
		
		// Launch the configuration
		this.fCurrentLaunchConfiguration = configuration;
		runner.run(runConfig, launch, monitor);		
		
		// Set default source locator if none specified
		if (launch.getSourceLocator() == null) {
			String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
			if (id == null) {
				javaProject = JavaRuntime.getJavaProject(configuration);
				ISourceLocator sourceLocator = new JavaSourceLocator(javaProject);
				launch.setSourceLocator(sourceLocator);
			}
		}
	}

	/**
	 * Returns the VM arguments specified by the given launch
	 * configuration, as a string. The returned string is empty if
	 * no VM arguments are specified.
	 * 
	 * @param configuration launch configuration
	 * @return the VM arguments specified by the given 
	 *  launch configuration, possibly an empty string
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public String getJavaPolicyFile(ILaunchConfiguration configuration)
		throws CoreException {
			String fileName = getWorkingDirectory(configuration).getAbsolutePath() + File.separator + "java.policy.applet";//$NON-NLS-1$
			File file = new File(fileName);
			if (!file.exists()) {
				// copy it to the working directory
				File test = LaunchingPlugin.getFileInPlugin(new Path("java.policy.applet")); //$NON-NLS-1$
				try {
					byte[] bytes = getFileByteContent(test);
					BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
					outputStream.write(bytes);
					outputStream.close();
				} catch (IOException e) {
					return "";//$NON-NLS-1$
				}
			}
		return "-Djava.security.policy=java.policy.applet";//$NON-NLS-1$
	}

	/**
	 * Using the specified launch configuration, build an HTML file that specifies the
	 * applet to launch.  Return the name of the HTML file.
	 */
	private String buildHTMLFile(ILaunchConfiguration configuration) {
		FileWriter writer = null;
		try {
			String name = getMainTypeName(configuration);
			fTempFile = new File(getWorkingDirectory(configuration).toString(), name + System.currentTimeMillis() + ".html"); //$NON-NLS-1$ //$NON-NLS-2$
			writer = new FileWriter(fTempFile);
			writer.write("<html>\n"); //$NON-NLS-1$
			writer.write("<body>\n"); //$NON-NLS-1$
			writer.write("<applet code="); //$NON-NLS-1$
			writer.write(name);
			writer.write(".class "); //$NON-NLS-1$
			String appletName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_NAME, ""); //$NON-NLS-1$
			if (appletName.length() != 0) {
				writer.write("NAME =\"" + appletName + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			writer.write("width=\""); //$NON-NLS-1$
			writer.write(Integer.toString(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_WIDTH, 200))); //$NON-NLS-1$
			writer.write("\" height=\""); //$NON-NLS-1$
			writer.write(Integer.toString(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_HEIGHT, 200))); //$NON-NLS-1$
			writer.write("\" >\n"); //$NON-NLS-1$
			Map parameters = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS, new HashMap());
			if (parameters.size() != 0) {
				Iterator iterator = parameters.keySet().iterator();
				while(iterator.hasNext()) {
		 			String next = (String) iterator.next();
					writer.write("<param name="); //$NON-NLS-1$
					writer.write(next);
					writer.write(" value="); //$NON-NLS-1$
					writer.write((String) parameters.get(next));
					writer.write(">\n"); //$NON-NLS-1$
				}
			}
			writer.write("</applet>\n"); //$NON-NLS-1$
			writer.write("</body>\n"); //$NON-NLS-1$
			writer.write("</html>\n"); //$NON-NLS-1$
		} catch(IOException e) {
		} catch(CoreException e) {
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
				}
			}
		}
		if (fTempFile == null) {
			return null;
		}
		return fTempFile.getName();
	}
	
	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			Object eventSource = event.getSource();
			switch(event.getKind()) {
				
				// Delete the HTML file used for the launch
				case DebugEvent.TERMINATE :
					if (eventSource != null) {
						File temp = (File) fEventSources.get(eventSource);
						if (temp != null) {
							try {
								fEventSources.remove(eventSource);
								fLaunchesCounter--;
								temp.delete();
							} finally {
								if (fLaunchesCounter == 0) {
									DebugPlugin.getDefault().removeDebugEventListener(this);
								}
							}
						}
					}
					break;
					
				// Track the HTML file used for the launch
				case DebugEvent.CREATE :
					if (eventSource != null) {
						if (eventSource instanceof IProcess) {
							IProcess runtimeProcess = (IProcess) eventSource;
							if (runtimeProcess.getLaunch().getLaunchConfiguration().equals(fCurrentLaunchConfiguration)) {
								fEventSources.put(eventSource, fTempFile);
								fLaunchesCounter++;
							}
						} else if (eventSource instanceof IDebugTarget) {
							IDebugTarget debugTarget = (IDebugTarget) eventSource;
							if (debugTarget.getLaunch().getLaunchConfiguration().equals(fCurrentLaunchConfiguration)) {
								fEventSources.put(eventSource, fTempFile);
								fLaunchesCounter++;
							}
						}
					}
					break;
			}
		}
	}

	/**
	 * Returns the contents of the given file as a byte array.
	 * @throws IOException if a problem occured reading the file.
	 */
	protected static byte[] getFileByteContent(File file) throws IOException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			return getInputStreamAsByteArray(stream, (int) file.length());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * Returns the given input stream's contents as a byte array.
	 * If a length is specified (ie. if length != -1), only length bytes
	 * are returned. Otherwise all bytes in the stream are returned.
	 * Note this doesn't close the stream.
	 * @throws IOException if a problem occured reading the stream.
	 */
	protected static byte[] getInputStreamAsByteArray(InputStream stream, int length)
		throws IOException {
		byte[] contents;
		if (length == -1) {
			contents = new byte[0];
			int contentsLength = 0;
			int bytesRead = -1;
			do {
				int available = stream.available();

				// resize contents if needed
				if (contentsLength + available > contents.length) {
					System.arraycopy(
						contents,
						0,
						contents = new byte[contentsLength + available],
						0,
						contentsLength);
				}

				// read as many bytes as possible
				bytesRead = stream.read(contents, contentsLength, available);

				if (bytesRead > 0) {
					// remember length of contents
					contentsLength += bytesRead;
				}
			} while (bytesRead > 0);

			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(
					contents,
					0,
					contents = new byte[contentsLength],
					0,
					contentsLength);
			}
		} else {
			contents = new byte[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case len is the actual read size.
				len += readSize;
				readSize = stream.read(contents, len, length - len);
			}
		}

		return contents;
	}
	
}