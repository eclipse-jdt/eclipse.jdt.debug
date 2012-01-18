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
package org.eclipse.jdt.internal.debug.ui.snippeteditor;
 
 
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.JavaMigrationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

/**
 * Support for launching scrapbook using launch configurations.
 */

public class ScrapbookLauncher implements IDebugEventSetListener {
	
	public static final String SCRAPBOOK_LAUNCH = IJavaDebugUIConstants.PLUGIN_ID + ".scrapbook_launch"; //$NON-NLS-1$
	
	public static final String SCRAPBOOK_FILE_PATH = IJavaDebugUIConstants.PLUGIN_ID + ".scrapbook_file_path"; //$NON-NLS-1$
	
	/**
	 * Persistent property associated with snippet files specifying working directory.
	 * Same format as the associated launch configuration attribute
	 * <code>ATTR_WORKING_DIR</code>.
	 */
	public static final QualifiedName SNIPPET_EDITOR_LAUNCH_CONFIG_HANDLE_MEMENTO = new QualifiedName(IJavaDebugUIConstants.PLUGIN_ID, "snippet_editor_launch_config"); //$NON-NLS-1$
		
	private IJavaLineBreakpoint fMagicBreakpoint;
	
	private HashMap<IFile, IDebugTarget> fScrapbookToVMs = new HashMap<IFile, IDebugTarget>(10);
	private HashMap<IDebugTarget, IBreakpoint> fVMsToBreakpoints = new HashMap<IDebugTarget, IBreakpoint>(10);
	private HashMap<IDebugTarget, IFile> fVMsToScrapbooks = new HashMap<IDebugTarget, IFile>(10);
	
	private static ScrapbookLauncher fgDefault = null;
	
	private ScrapbookLauncher() {
		//see getDefault()
	}
	
	public static ScrapbookLauncher getDefault() {
		if (fgDefault == null) {
			fgDefault = new ScrapbookLauncher();
		}
		return fgDefault;
	}
	
	/**
	 * Launches a VM for the given scrapbook page, in debug mode.
	 * Returns an existing launch if the page is already running.
	 * @param page the scrapbook page file
	 * 
	 * @return resulting launch, or <code>null</code> on failure
	 */
	protected ILaunch launch(IFile page) {

		// clean up orphaned launch configurations
		cleanupLaunchConfigurations();
							
		if (!page.getFileExtension().equals("jpage")) { //$NON-NLS-1$
			showNoPageDialog();
			return null;
		}
		
		IDebugTarget vm = getDebugTarget(page);
		if (vm != null) {
			//already launched
			return vm.getLaunch();
		}
		
		IJavaProject javaProject= JavaCore.create(page.getProject());
			
		URL jarURL = null;
		try {
			jarURL = JDIDebugUIPlugin.getDefault().getBundle().getEntry("snippetsupport.jar"); //$NON-NLS-1$
			jarURL = FileLocator.toFileURL(jarURL);
		} catch (MalformedURLException e) {
			JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e); //$NON-NLS-1$
			return null;
		} catch (IOException e) {
			JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e); //$NON-NLS-1$
			return null;
		}
		
		List<IRuntimeClasspathEntry> cp = new ArrayList<IRuntimeClasspathEntry>(3);
		IRuntimeClasspathEntry supportEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(jarURL.getFile()));
		cp.add(supportEntry);
		// get bootpath entries
		try {
			IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(javaProject);
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].getClasspathProperty() != IRuntimeClasspathEntry.USER_CLASSES) {
					cp.add(entries[i]);
				}
			}
			IRuntimeClasspathEntry[] classPath = cp.toArray(new IRuntimeClasspathEntry[cp.size()]);
			
			return doLaunch(javaProject, page, classPath);
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e); //$NON-NLS-1$
		}
		return null;
	}

	private ILaunch doLaunch(IJavaProject p, IFile page, IRuntimeClasspathEntry[] classPath) {
		try {
			if (fVMsToScrapbooks.isEmpty()) {
				// register for debug events if a scrapbook is not currently running
				DebugPlugin.getDefault().addDebugEventListener(this);
			}
			ILaunchConfiguration config = null;
			ILaunchConfigurationWorkingCopy wc = null;
			try {
				config = getLaunchConfigurationTemplate(page);
				if (config != null) {
					wc = config.getWorkingCopy();		
				}
			} catch (CoreException e) {
				config = null;
				JDIDebugUIPlugin.errorDialog("Unable to retrieve scrapbook settings", e); //$NON-NLS-1$
			}
			
			if (config == null) {
				config = createLaunchConfigurationTemplate(page);
				wc = config.getWorkingCopy();
			}							
			
			IPath outputLocation =	p.getProject().getWorkingLocation(JDIDebugUIPlugin.getUniqueIdentifier());
			File f = outputLocation.toFile();
			URL u = null;
			try {
				u = getEncodedURL(f);
			} catch (MalformedURLException e) {
				JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e); //$NON-NLS-1$
				return null;
			}
			catch(UnsupportedEncodingException usee) {
				JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", usee); //$NON-NLS-1$
				return null;
			}
			String[] defaultClasspath = JavaRuntime.computeDefaultRuntimeClassPath(p);
			String[] urls = new String[defaultClasspath.length + 1];
			urls[0] = u.toExternalForm();
			for (int i = 0; i < defaultClasspath.length; i++) {
				f = new File(defaultClasspath[i]);
				try {
					urls[i + 1] = getEncodedURL(f).toExternalForm();
				} catch (MalformedURLException e) {
					JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e);				 //$NON-NLS-1$
				 	return null;
				}
				catch(UnsupportedEncodingException usee) {
					JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", usee); //$NON-NLS-1$
					return null;
				}
			}
			
			// convert to mementos 
			List<String> classpathList= new ArrayList<String>(classPath.length);
			for (int i = 0; i < classPath.length; i++) {
				classpathList.add(classPath[i].getMemento());
			}
			if(wc == null) {
				wc = config.getWorkingCopy();
			}
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpathList);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, p.getElementName());
			if (wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, (String)null) == null) {
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, "org.eclipse.jdt.debug.ui.scrapbookSourcepathProvider"); //$NON-NLS-1$
			}
			
			StringBuffer urlsString = new StringBuffer();
			for (int i = 0; i < urls.length; i++) {
				urlsString.append(' ');
				urlsString.append(urls[i]);
			}
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, urlsString.toString());
			wc.setAttribute(SCRAPBOOK_LAUNCH, SCRAPBOOK_LAUNCH);
			
			config = wc.doSave();
			
			ILaunch launch = config.launch(ILaunchManager.DEBUG_MODE, null);
			if (launch != null) {
				IDebugTarget dt = launch.getDebugTarget();
				IBreakpoint magicBreakpoint = createMagicBreakpoint("org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain"); //$NON-NLS-1$
				fScrapbookToVMs.put(page, dt);
				fVMsToScrapbooks.put(dt, page);
				fVMsToBreakpoints.put(dt, magicBreakpoint);
				dt.breakpointAdded(magicBreakpoint);
				launch.setAttribute(SCRAPBOOK_LAUNCH, SCRAPBOOK_LAUNCH);
				return launch;
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Creates an "invisible" line breakpoint. 
	 * @param typeName the type name
	 * @return the new 'magic' breakpoint
	 * @throws CoreException if an exception occurs
	 */
	IBreakpoint createMagicBreakpoint(String typeName) throws CoreException{
		//set a breakpoint on the Thread.sleep(100); line of the no-op method of ScrapbookMain
		fMagicBreakpoint= JDIDebugModel.createLineBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), typeName, 59, -1, -1, 0, false, null);
		fMagicBreakpoint.setPersisted(false);
		return fMagicBreakpoint;
	}

	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource() instanceof IDebugTarget && event.getKind() == DebugEvent.TERMINATE) {
				cleanup((IDebugTarget)event.getSource());
			}
		}
	}
	
	/**
	 * Returns the debug target associated with the given
	 * scrapbook page, or <code>null</code> if none.
	 * 
	 * @param page file representing scrapbook page
	 * @return associated debug target or <code>null</code>
	 */
	public IDebugTarget getDebugTarget(IFile page) {
		return fScrapbookToVMs.get(page);
	}
	
	/**
	 * Returns the magic breakpoint associated with the given
	 * scrapbook VM. The magic breakpoint is the location at
	 * which an evaluation begins.
	 * 
	 * @param target a scrapbook debug target 
	 * @return the breakpoint at which an evaluation begins
	 *  or <code>null</code> if none
	 */
	public IBreakpoint getMagicBreakpoint(IDebugTarget target) {
		return fVMsToBreakpoints.get(target);
	}
	
	protected void showNoPageDialog() {
		String title= SnippetMessages.getString("ScrapbookLauncher.error.title"); //$NON-NLS-1$
		String msg= SnippetMessages.getString("ScrapbookLauncher.error.pagenotfound"); //$NON-NLS-1$
		MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(),title, msg);
	}
	
	protected void cleanup(IDebugTarget target) {
		Object page = fVMsToScrapbooks.get(target);
		if (page != null) {
			fVMsToScrapbooks.remove(target);
			fScrapbookToVMs.remove(page);
			fVMsToBreakpoints.remove(target);
			ILaunch launch = target.getLaunch();
			if (launch != null) {
				getLaunchManager().removeLaunch(launch);
			}
			if (fVMsToScrapbooks.isEmpty()) {
				// no need to listen to events if no scrap books running
				DebugPlugin.getDefault().removeDebugEventListener(this);
			}
		}
	}
	
	protected URL getEncodedURL(File file) throws MalformedURLException, UnsupportedEncodingException {
		//looking at File.toURL the delimiter is always '/' 
		// NOT File.separatorChar
		String urlDelimiter= "/"; //$NON-NLS-1$
		String unencoded= file.toURL().toExternalForm();
		StringBuffer encoded= new StringBuffer();
		StringTokenizer tokenizer= new StringTokenizer(unencoded, urlDelimiter);
		
		encoded.append(tokenizer.nextToken()); //file:
		encoded.append(urlDelimiter);
		encoded.append(tokenizer.nextToken()); //drive letter and ':'
		while (tokenizer.hasMoreElements()) {
			encoded.append(urlDelimiter);
			String token= tokenizer.nextToken();
			try {
				encoded.append(URLEncoder.encode(token, ResourcesPlugin.getEncoding()));
			} catch (UnsupportedEncodingException e) {
				encoded.append(URLEncoder.encode(token, "UTF-8")); //$NON-NLS-1$
			}
		}
		if (file.isDirectory()) {
			encoded.append(urlDelimiter);
		}
		return new URL(encoded.toString());
	}
	
	/**
	 * Returns the launch configuration used as a template for launching the
	 * given scrapbook file, or <code>null</code> if none. The template contains
	 * working directory and JRE settings to use when launching the scrapbook.
	 * @param file the backing config file
	 * @return the launch configuration template
	 * @throws CoreException if an exception occurs
	 */
	public static ILaunchConfiguration getLaunchConfigurationTemplate(IFile file) throws CoreException {
		String memento = getLaunchConfigMemento(file);
		if (memento != null) {
			return getLaunchManager().getLaunchConfiguration(memento);
		}
		return null;
	}
	
	/**
	 * Creates and saves template launch configuration for the given scrapbook file.
	 * @param page the backing page
	 * @return the new {@link ILaunchConfiguration} template
	 * @throws CoreException if an exception occurs
	 */
	public static ILaunchConfiguration createLaunchConfigurationTemplate(IFile page) throws CoreException {
		ILaunchConfigurationType lcType = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		String name = NLS.bind(SnippetMessages.getString("ScrapbookLauncher.17"), new String[]{page.getName()}); //$NON-NLS-1$
		ILaunchConfigurationWorkingCopy wc = lcType.newInstance(null, name);
		wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain"); //$NON-NLS-1$			
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, page.getProject().getName());
		wc.setAttribute(SCRAPBOOK_LAUNCH, SCRAPBOOK_LAUNCH);
		wc.setAttribute(SCRAPBOOK_FILE_PATH, page.getFullPath().toString());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, "org.eclipse.jdt.debug.ui.scrapbookSourcepathProvider"); //$NON-NLS-1$
		JavaMigrationDelegate.updateResourceMapping(wc);
		ILaunchConfiguration config = wc.doSave();
		setLaunchConfigMemento(page, config.getMemento());
		return config;		
	}	
		
	/**
	 * Returns the handle memento for the given scrapbook's launch configuration
	 * template, or <code>null</code> if none.
	 * @param file the launch configuration template
	 * @return the {@link String} memento
	 */
	private static String getLaunchConfigMemento(IFile file) {
		try {
			return file.getPersistentProperty(SNIPPET_EDITOR_LAUNCH_CONFIG_HANDLE_MEMENTO);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return null;
	}	
	
	/**
	 * Sets the handle memento for the given scrapbook's launch configuration
	 * template.
	 * @param file the backing file
	 * @param memento the {@link String} memento
	 */
	protected static void setLaunchConfigMemento(IFile file, String memento) {
		try {
			file.setPersistentProperty(SNIPPET_EDITOR_LAUNCH_CONFIG_HANDLE_MEMENTO, memento);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}	

	/**
	 * Returns the launch manager.
	 * @return the launch manager instance
	 */
	protected static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Returns the working directory attribute for the given snippet file,
	 * possibly <code>null</code>.
	 * @param file the backing file
	 * @return the working directory
	 * 
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public static String getWorkingDirectoryAttribute(IFile file) throws CoreException {
		ILaunchConfiguration config = getLaunchConfigurationTemplate(file);
		if (config != null) {
			return config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
		}
		return null;
	}
	
	/**
	 * Returns the VM arguments attribute for the given snippet file,
	 * possibly <code>null</code>.
	 * @param file the backing file
	 * @return the VM arguments
	 * 
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public static String getVMArgsAttribute(IFile file) throws CoreException {
		ILaunchConfiguration config = getLaunchConfigurationTemplate(file);
		if (config != null) {
			return config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
		}
		return null;
	}	
	
	/**
	 * Returns the VM install used to launch the given snippet file.
	 * @param file the backing file
	 * @return the VM install
	 * 
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public static IVMInstall getVMInstall(IFile file) throws CoreException {
		ILaunchConfiguration config = getLaunchConfigurationTemplate(file);
		if (config == null) {
			IJavaProject pro = JavaCore.create(file.getProject());
			return JavaRuntime.getVMInstall(pro);
		}
		return JavaRuntime.computeVMInstall(config);
	}	
	
	/**
	 * Deletes any scrapbook launch configurations for scrap books that
	 * have been deleted. Rather than listening to all resource deltas,
	 * configurations are deleted each time a scrapbook is launched - which is
	 * infrequent.
	 */
	public void cleanupLaunchConfigurations() {
		try {
			ILaunchConfigurationType lcType = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(lcType);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			for (int i = 0; i < configs.length; i++) {
				String path = configs[i].getAttribute(SCRAPBOOK_FILE_PATH, (String)null);
				if (path != null) {
					IPath pagePath = new Path(path);
					IResource res = root.findMember(pagePath);
					if (res == null) {
						// config without a page - delete it
						configs[i].delete();
					}
				}
			}
		} catch (CoreException e) {
			// log quietly
			JDIDebugUIPlugin.log(e);
		}
	}
}
