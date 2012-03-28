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
package org.eclipse.jdt.internal.launching;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;

import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.JavaRuntime;

public class JavaAppletLaunchConfigurationDelegate extends JavaLaunchDelegate implements IDebugEventSetListener {
		
	/**
	 * Mapping of ILaunch objects to File objects that represent the HTML file
	 * used to initiate the applet launch.  This is used to delete the HTML
	 * file when the launch terminates.
	 */
	private static Map<ILaunch, File> fgLaunchToFileMap = new HashMap<ILaunch, File>();
	
	/**
	 * Used to map temporary file to launch object.
	 */
	private ILaunch fLaunch;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public synchronized void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			fLaunch = launch;
			super.launch(configuration, mode, launch, monitor);
		} catch (CoreException e) {
			cleanup(launch);
			throw e;
		}
		fLaunch = null;
	}

	/**
	 * Returns the system property string for the policy file
	 * 
	 * @param workingDir the working directory
	 * @return system property for the policy file
	 */
	public String getJavaPolicyFile(File workingDir) {
			File file = new File(workingDir, "java.policy.applet");//$NON-NLS-1$ 
			if (!file.exists()) {
				// copy it to the working directory
				File test = LaunchingPlugin.getFileInPlugin(new Path("java.policy.applet")); //$NON-NLS-1$
				BufferedOutputStream outputStream= null;
				try {
					byte[] bytes = getFileByteContent(test);
					outputStream = new BufferedOutputStream(new FileOutputStream(file));
					outputStream.write(bytes);
				} catch (IOException e) {
					return "";//$NON-NLS-1$
				} finally {
					if (outputStream != null) {
						try {
							outputStream.close();
						} catch (IOException e1) {
						}
					}
				}
			}
		return "-Djava.security.policy=java.policy.applet";//$NON-NLS-1$
	}

	/**
	 * Using the specified launch configuration, build an HTML file that specifies the
	 * applet to launch.  Return the name of the HTML file.
	 * 
	 * @param configuration the launch config
	 * @param dir the directory in which to make the file
	 * @return the new HTML file
	 */
	private File buildHTMLFile(ILaunchConfiguration configuration, File dir) {
		FileOutputStream stream = null;
		File tempFile = null;
		try {
			String encoding = getLaunchManager().getEncoding(configuration);
			String name = getAppletMainTypeName(configuration);
			StringBuffer buf = new StringBuffer();
			buf.append("<html>\n"); //$NON-NLS-1$
			buf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding + "\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append("<body>\n"); //$NON-NLS-1$
			buf.append("<applet code="); //$NON-NLS-1$
			buf.append(name);
			buf.append(".class "); //$NON-NLS-1$
			String appletName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_NAME, ""); //$NON-NLS-1$
			if (appletName.length() != 0) {
				buf.append("NAME =\"" + appletName + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			buf.append("width=\""); //$NON-NLS-1$
			buf.append(Integer.toString(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_WIDTH, 200)));
			buf.append("\" height=\""); //$NON-NLS-1$
			buf.append(Integer.toString(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_HEIGHT, 200)));
			buf.append("\" >\n"); //$NON-NLS-1$
			Map<String, String> parameters = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS, new HashMap<String, String>());
			if (parameters.size() != 0) {
				Iterator<Entry<String, String>> iterator = parameters.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> next = iterator.next();
					buf.append("<param name="); //$NON-NLS-1$
					buf.append(getQuotedString(next.getKey()));
					buf.append(" value="); //$NON-NLS-1$
					buf.append(getQuotedString(next.getValue()));
					buf.append(">\n"); //$NON-NLS-1$
				}
			}
			buf.append("</applet>\n"); //$NON-NLS-1$
			buf.append("</body>\n"); //$NON-NLS-1$
			buf.append("</html>\n"); //$NON-NLS-1$

			tempFile = new File(dir, name + System.currentTimeMillis() + ".html"); //$NON-NLS-1$
			stream = new FileOutputStream(tempFile);
			stream.write(buf.toString().getBytes(encoding));
		} catch(IOException e) {
		} catch(CoreException e) {
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch(IOException e) {
				}
			}
		}
		
		return tempFile;
	}

	private String getQuotedString(String string) {
		int singleQuotes = count(string, '\'');
		int doubleQuotes = count(string, '"');
		if (doubleQuotes == 0)
			return '"' + string + '"';
		else if (singleQuotes == 0)
			return '\'' + string + '\'';
		else
			return '"' + convertToHTMLContent(string) + '"';
	}
	
	private static int count(String string, char character) {
		int count = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == character)
				count++;
		}
		return count;
	}

	private static String convertToHTMLContent(String content) {
		content = replace(content, '"', "&quot;"); //$NON-NLS-1$
		content = replace(content, '\'', "&#39;"); //$NON-NLS-1$
		return content;
	}

	private static String replace(String text, char c, String s) {

		int previous = 0;
		int current = text.indexOf(c, previous);

		if (current == -1)
			return text;

		StringBuffer buffer = new StringBuffer();
		while (current > -1) {
			buffer.append(text.substring(previous, current));
			buffer.append(s);
			previous = current + 1;
			current = text.indexOf(c, previous);
		}
		buffer.append(text.substring(previous));

		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			Object eventSource = event.getSource();
			switch(event.getKind()) {
				
				// Delete the HTML file used for the launch
				case DebugEvent.TERMINATE :
					if (eventSource != null) {
						ILaunch launch = null;
						if (eventSource instanceof IProcess) {
							IProcess process = (IProcess) eventSource;
							launch = process.getLaunch();
						} else if (eventSource instanceof IDebugTarget) {
							IDebugTarget debugTarget = (IDebugTarget) eventSource;
							launch = debugTarget.getLaunch();
						}
						if (launch != null) {
							cleanup(launch);
						}
					}
					break;
			}
		}
	}
	
	/**
	 * Cleans up event listener and temporary file for the launch.
	 * 
	 * @param launch the launch
	 */
	private void cleanup(ILaunch launch) {
		File temp = fgLaunchToFileMap.get(launch);
		if (temp != null) {
			try {
				fgLaunchToFileMap.remove(launch);
				temp.delete();
			} finally {
				if (fgLaunchToFileMap.isEmpty()) {
					DebugPlugin.getDefault().removeDebugEventListener(this);
				}
			}
		}		
	}

	/**
	 * Returns the contents of the given file as a byte array.
	 * @param file the file
	 * @return the byte array form the file
	 * @throws IOException if a problem occurred reading the file.
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
	 * @param stream the stream
	 * @param length the length to read
	 * @return the byte array from the stream
	 * @throws IOException if a problem occurred reading the stream.
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getProgramArguments(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		File workingDir = verifyWorkingDirectory(configuration);
		// Construct the HTML file and set its name as a program argument
		File htmlFile = buildHTMLFile(configuration, workingDir);
		if (htmlFile == null) {
			abort(LaunchingMessages.JavaAppletLaunchConfigurationDelegate_Could_not_build_HTML_file_for_applet_launch_1, null, IJavaLaunchConfigurationConstants.ERR_COULD_NOT_BUILD_HTML); 
		}			
		// Add a debug listener if necessary 
		if (fgLaunchToFileMap.isEmpty()) {
			DebugPlugin.getDefault().addDebugEventListener(this);
		}
		// Add a mapping of the launch to the html file 
		fgLaunchToFileMap.put(fLaunch, htmlFile);		
		return htmlFile.getName();		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getVMArguments(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		StringBuffer arguments = new StringBuffer(super.getVMArguments(configuration));
		File workingDir = verifyWorkingDirectory(configuration);
		String javaPolicyFile = getJavaPolicyFile(workingDir);
		arguments.append(" "); //$NON-NLS-1$
		arguments.append(javaPolicyFile);
		return arguments.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getMainTypeName(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_APPLETVIEWER_CLASS, IJavaLaunchConfigurationConstants.DEFAULT_APPLETVIEWER_CLASS);
	}

	/**
	 * Returns the applet's main type name.
	 * 
	 * @param configuration the config
	 * @return the main type name
	 * @throws CoreException if a problem occurs
	 */
	protected String getAppletMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		return super.getMainTypeName(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getDefaultWorkingDirectory(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	protected File getDefaultWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		// default working dir for applets is the project's output directory
		String outputDir = JavaRuntime.getProjectOutputDirectory(configuration);
		if (outputDir == null) {
			// if no project attribute, default to eclipse directory
			return new File(System.getProperty("user.dir"));  //$NON-NLS-1$
		}
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(outputDir);
		if (resource == null || !resource.exists()) {
			//default to eclipse directory
			return new File(System.getProperty("user.dir"));  //$NON-NLS-1$
		}
		return resource.getLocation().toFile(); 
	}
	
	
}
