package org.eclipse.jdt.internal.debug.ui.snippeteditor;
 
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIEventFilter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Support for launching scrapbook using launch configurations.
 * Not yet used. Will be used once launch configs are fixed
 * to perform verification in UI, rather than config. Currently
 * unable to launch as verification ensures project and main type
 * exist in the workspace, which is not true for the scrapbook.
 */

public class ScrapbookLauncher implements IDebugEventListener {
	
	public static final String SCRAPBOOK_LAUNCH = IJavaDebugUIConstants.PLUGIN_ID + ".scrapbook_launch";
	
	private IJavaLineBreakpoint fMagicBreakpoint;
	
	
	private HashMap fScrapbookToVMs = new HashMap(10);
	private HashMap fVMsToBreakpoints = new HashMap(10);
	private HashMap fVMsToScrapbooks = new HashMap(10);
	private HashMap fVMsToFilters = new HashMap(10);
	
	private static ScrapbookLauncher fgDefault = null;
	
	private ScrapbookLauncher() {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	public static ScrapbookLauncher getDefault() {
		if (fgDefault == null) {
			fgDefault = new ScrapbookLauncher();
		}
		return fgDefault;
	}
	
	/**
	 * Launches a VM for the given srapbook page, in debug mode.
	 * Returns an existing launch if the page is already running.
	 * 
	 * @param file scrapbook page file
	 * @return resulting launch, or <code>null</code> on failure
	 */
	protected ILaunch launch(IFile page) {
				
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
			
		URL pluginInstallURL= JDIDebugUIPlugin.getDefault().getDescriptor().getInstallURL();
		URL jarURL = null;
		try {
			jarURL = new URL(pluginInstallURL, "snippetsupport.jar"); //$NON-NLS-1$
			jarURL = Platform.asLocalURL(jarURL);
		} catch (MalformedURLException e) {
			JDIDebugUIPlugin.errorDialog("Exception occurred launching scrapbook.",
			 new Status(IStatus.ERROR, JDIDebugUIPlugin.getPluginId(), IJavaDebugUIConstants.INTERNAL_ERROR, e.getMessage(), e));
			JDIDebugUIPlugin.log(e);
			return null;
		} catch (IOException e) {
			JDIDebugUIPlugin.errorDialog("Exception occurred launching scrapbook.",
			 new Status(IStatus.ERROR, JDIDebugUIPlugin.getPluginId(), IJavaDebugUIConstants.INTERNAL_ERROR, e.getMessage(), e));
			JDIDebugUIPlugin.log(e);
			return null;
		}
		
		String[] classPath = new String[] {jarURL.getFile()};
		
		return doLaunch(javaProject, page, classPath);
	}

	private ILaunch doLaunch(IJavaProject p, IFile page, String[] classPath) {
		try {
			ILaunchConfigurationType lcType = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			String name = page.getName() + "-Scrapbook-" + System.currentTimeMillis();
			ILaunchConfigurationWorkingCopy wc = lcType.newInstance(null, name);
			wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
						
			IJavaSourceLocation[] locations = JavaSourceLocator.getDefaultSourceLocations(p);
			ISourceLocator sl= new JavaSourceLocator(locations);
			IPath outputLocation =	p.getProject().getPluginWorkingLocation(JDIDebugUIPlugin.getDefault().getDescriptor());
			File f = outputLocation.toFile();
			URL u = null;
			try {
				u = f.toURL();
			} catch (MalformedURLException e) {
				JDIDebugUIPlugin.errorDialog("Exception occurred launching scrapbook.",
			 		new Status(IStatus.ERROR, JDIDebugUIPlugin.getPluginId(), IJavaDebugUIConstants.INTERNAL_ERROR, e.getMessage(), e));				
			 	JDIDebugUIPlugin.log(e);
				return null;
			}
			String[] defaultClasspath = JavaRuntime.computeDefaultRuntimeClassPath(p);
			String[] urls = new String[defaultClasspath.length + 1];
			urls[0] = u.toExternalForm();
			for (int i = 0; i < defaultClasspath.length; i++) {
				f = new File(defaultClasspath[i]);
				try {
					urls[i + 1] = f.toURL().toExternalForm();
				} catch (MalformedURLException e) {
					JDIDebugUIPlugin.errorDialog("Exception occurred launching scrapbook.",
				 		new Status(IStatus.ERROR, JDIDebugUIPlugin.getPluginId(), IJavaDebugUIConstants.INTERNAL_ERROR, e.getMessage(), e));				
				 	JDIDebugUIPlugin.log(e);
				 	return null;
				}
			}
			
			List classpathList = new ArrayList();
			for (int i = 0; i < classPath.length; i++) {
				classpathList.add(classPath[i]);
			}
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpathList);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain");
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, p.getElementName());
			IVMInstall vm = JavaRuntime.getVMInstall(p);
			if (vm == null) {
				vm = JavaRuntime.getDefaultVMInstall();
			}
			if (vm != null) {
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, vm.getId());
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vm.getVMInstallType().getId());
			}
			
			String urlsString = "";
			for (int i = 0; i < urls.length; i++) {
				urlsString += " " + urls[i];
			}
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, urlsString);
			ILaunchConfiguration config = wc.doSave();
			
			ILaunch launch = config.launch(ILaunchManager.DEBUG_MODE, null);
			if (launch != null) {
				IDebugTarget dt = launch.getDebugTarget();
				IBreakpoint magicBreakpoint = createMagicBreakpoint("org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain"); //$NON-NLS-1$
				fScrapbookToVMs.put(page, dt);
				fVMsToScrapbooks.put(dt, page);
				fVMsToBreakpoints.put(dt, magicBreakpoint);
				dt.breakpointAdded(magicBreakpoint);
				IDebugUIEventFilter filter = new ScrapbookEventFilter(launch);
				fVMsToFilters.put(dt, filter);
				DebugUITools.addEventFilter(filter);
				launch.setAttribute(SCRAPBOOK_LAUNCH, SCRAPBOOK_LAUNCH);
				return launch;
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
			ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), SnippetMessages.getString("ScrapbookLauncher.error.title"), "Unable to launch scrapbook VM.", e.getStatus()); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Creates an "invisible" line breakpoint. 
	 */
	IBreakpoint createMagicBreakpoint(String typeName) throws CoreException{
	
		fMagicBreakpoint= JDIDebugModel.createLineBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), typeName, 49, -1, -1, 0, false, null);
		fMagicBreakpoint.setPersisted(false);
		return fMagicBreakpoint;
	}

	/**
	 * @see IDebugEventListener#handleDebugEvent(DebugEvent)
	 */
	public void handleDebugEvent(DebugEvent event) {
		if (event.getSource() instanceof IDebugTarget && event.getKind() == DebugEvent.TERMINATE) {
			cleanup((IDebugTarget)event.getSource());
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
		return (IDebugTarget)fScrapbookToVMs.get(page);
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
		return (IBreakpoint)fVMsToBreakpoints.get(target);
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
			IDebugUIEventFilter filter = (IDebugUIEventFilter)fVMsToFilters.remove(target);
			DebugUITools.removeEventFilter(filter);
			ILaunch launch = target.getLaunch();
			if (launch != null) {
				ILaunchConfiguration config = launch.getLaunchConfiguration();
				if (config != null) {
					try {
						config.delete();
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
					}
				}
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			}
			
		}
	}
}