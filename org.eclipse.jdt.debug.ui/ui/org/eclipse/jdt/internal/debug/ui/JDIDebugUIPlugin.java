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
package org.eclipse.jdt.internal.debug.ui;

 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.breakpoints.ExceptionInspector;
import org.eclipse.jdt.internal.debug.ui.breakpoints.JavaBreakpointTypeAdapterFactory;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathEntryAdapterFactory;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaContendedMonitor;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaOwnedMonitor;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaOwningThread;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaWaitingThread;
import org.eclipse.jdt.internal.debug.ui.monitors.MonitorsAdapterFactory;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.SnippetFileDocumentProvider;
import org.eclipse.jdt.internal.debug.ui.sourcelookup.JavaDebugShowInAdapterFactory;
import org.eclipse.jdt.internal.debug.ui.threadgroups.TargetAdapterFactory;
import org.eclipse.jdt.internal.debug.ui.threadgroups.ThreadGroupAdapterFactory;
import org.eclipse.jdt.internal.debug.ui.variables.ColumnPresentationAdapterFactory;
import org.eclipse.jdt.internal.debug.ui.variables.JavaDebugElementAdapterFactory;
import org.eclipse.jdt.internal.launching.DefaultProjectClasspathEntry;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Plug-in class for the org.eclipse.jdt.debug.ui plug-in.
 */
public class JDIDebugUIPlugin extends AbstractUIPlugin {

	/**
	 * Unique identifier constant (value <code>"org.eclipse.jdt.debug.ui"</code>)
	 * for the JDI Debug plug-in.
	 */
	private static final String PI_JDI_DEBUG = "org.eclipse.jdt.debug.ui"; //$NON-NLS-1$
	
	/**
	 * Id for inspect command.
	 * 
	 * @since 3.3
	 */
	public static final String COMMAND_INSPECT = "org.eclipse.jdt.debug.ui.commands.Inspect"; //$NON-NLS-1$
	
	/**
	 * Java Debug UI plug-in instance
	 */
	private static JDIDebugUIPlugin fgPlugin;
	
	private IDocumentProvider fSnippetDocumentProvider;
	
	private ImageDescriptorRegistry fImageDescriptorRegistry;
	
	private ActionFilterAdapterFactory fActionFilterAdapterFactory;
	private JavaSourceLocationWorkbenchAdapterFactory fSourceLocationAdapterFactory;
	private JavaBreakpointWorkbenchAdapterFactory fBreakpointAdapterFactory;
	
	private IDebugModelPresentation fUtilPresentation;
	
	/**
	 * Java Debug UI listeners
	 */
	private IJavaHotCodeReplaceListener fHCRListener;
	
	// Map of VMInstallTypeIDs to IConfigurationElements
	protected Map<String, IConfigurationElement> fVmInstallTypePageMap;
	
	/**
	 * Whether this plugin is in the process of shutting
	 * down.
	 */
	private boolean fShuttingDown= false;

	/**
	 * Singleton text tools for debug plug-in.
	 */
	private JavaTextTools fTextTools = null;
	
	/**
	 * @see Plugin()
	 */
	public JDIDebugUIPlugin() {
		super();
		setDefault(this);
	}
	
	/**
	 * Sets the Java Debug UI plug-in instance
	 * 
	 * @param plugin the plugin instance
	 */
	private static void setDefault(JDIDebugUIPlugin plugin) {
		fgPlugin = plugin;
	}
	
	/**
	 * Returns the Java Debug UI plug-in instance
	 * 
	 * @return the Java Debug UI plug-in instance
	 */
	public static JDIDebugUIPlugin getDefault() {
		return fgPlugin;
	}
	
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		return PI_JDI_DEBUG;
	}
	
	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message the error message to log
	 */
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e the exception to be logged
	 */	
	public static void log(Throwable e) {
		if (e instanceof CoreException) {
			log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e.getCause()));
		} else {
			log(new Status(IStatus.ERROR, getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Internal Error", e));   //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the active workbench window
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}	
	
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null) {
			return w.getActivePage();
		}
		return null;
	}
	
	
	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	@Override
	protected ImageRegistry createImageRegistry() {
		return JavaDebugImages.getImageRegistry();
	}	
	
	public IDocumentProvider getSnippetDocumentProvider() {
		if (fSnippetDocumentProvider == null) {
			fSnippetDocumentProvider= new SnippetFileDocumentProvider();
		}
		return fSnippetDocumentProvider;
	}
	
	public static void statusDialog(IStatus status) {
		switch (status.getSeverity()) {
		case IStatus.ERROR:
			statusDialog(DebugUIMessages.JDIDebugUIPlugin_Error_1, status);
			break;
		case IStatus.WARNING:
			statusDialog(DebugUIMessages.JDIDebugUIPlugin_0, status);
			break;
		case IStatus.INFO:
			statusDialog(DebugUIMessages.JDIDebugUIPlugin_4, status);
			break;
		}		
	}
	public static void statusDialog(String title, IStatus status) {
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			switch (status.getSeverity()) {
			case IStatus.ERROR:
				ErrorDialog.openError(shell, title, null, status);
				break;
			case IStatus.WARNING:
				MessageDialog.openWarning(shell, title, status.getMessage());
				break;
			case IStatus.INFO:
				MessageDialog.openInformation(shell, title, status.getMessage());
				break;
			}
		}		
	}
		
	/**
	 * Creates a new internal error status with the specified message and throwable, then displays
	 * it in a status dialog.
	 * 
	 * @param message error message
	 * @param t throwable cause or <code>null</code>
	 */
	public static void errorDialog(String message, Throwable t) {
		IStatus status= new Status(IStatus.ERROR, getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, message, t);
		statusDialog(status);
	}
	
	/**
	 * Opens an error dialog with the given title and message.
	 */
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		IStatus status;
		if (t instanceof CoreException) {
			status= ((CoreException)t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message= null;
			}
		} else {
			status= new Status(IStatus.ERROR, getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "Error within Debug UI: ", t); //$NON-NLS-1$
			log(status);
		}
		ErrorDialog.openError(shell, title, message, status);
	}
	
	/**
	 * Creates an extension.  If the extension plugin has not
	 * been loaded a busy cursor will be activated during the duration of
	 * the load.
	 *
	 * @param element the config element defining the extension
	 * @param classAttribute the name of the attribute carrying the class
	 * @return the extension object
	 */
	public static Object createExtension(final IConfigurationElement element, final String classAttribute) throws CoreException {
		// If plugin has been loaded create extension.
		// Otherwise, show busy cursor then create extension.
		Bundle bundle = Platform.getBundle(element.getContributor().getName());
		if (bundle.getState() == Bundle.ACTIVE) {
			return element.createExecutableExtension(classAttribute);
		}
		
		final Object [] ret = new Object[1];
		final CoreException [] exc = new CoreException[1];
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				try {
					ret[0] = element.createExecutableExtension(classAttribute);
				} catch (CoreException e) {
					exc[0] = e;
				}
			}
		});
		if (exc[0] != null) {
			throw exc[0];
		}
		return ret[0];
	}	
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IAdapterManager manager= Platform.getAdapterManager();
		fActionFilterAdapterFactory= new ActionFilterAdapterFactory();
		manager.registerAdapters(fActionFilterAdapterFactory, IMember.class);
		manager.registerAdapters(fActionFilterAdapterFactory, IJavaVariable.class);
		manager.registerAdapters(fActionFilterAdapterFactory, IJavaStackFrame.class);
		manager.registerAdapters(fActionFilterAdapterFactory, IJavaThread.class);
		manager.registerAdapters(fActionFilterAdapterFactory, JavaInspectExpression.class);
		fSourceLocationAdapterFactory = new JavaSourceLocationWorkbenchAdapterFactory();
		manager.registerAdapters(fSourceLocationAdapterFactory, IJavaSourceLocation.class);
		fBreakpointAdapterFactory= new JavaBreakpointWorkbenchAdapterFactory();
		manager.registerAdapters(fBreakpointAdapterFactory, IJavaBreakpoint.class);
        IAdapterFactory typeFactory = new JavaBreakpointTypeAdapterFactory();
        manager.registerAdapters(typeFactory, IJavaBreakpoint.class);
        
        IAdapterFactory monitorFactory = new MonitorsAdapterFactory();
        manager.registerAdapters(monitorFactory, IJavaThread.class);
        manager.registerAdapters(monitorFactory, JavaContendedMonitor.class);
        manager.registerAdapters(monitorFactory, JavaOwnedMonitor.class);
        manager.registerAdapters(monitorFactory, JavaOwningThread.class);
        manager.registerAdapters(monitorFactory, JavaWaitingThread.class);
        manager.registerAdapters(monitorFactory, IJavaStackFrame.class);
        
        IAdapterFactory targetFactory = new TargetAdapterFactory();
        manager.registerAdapters(targetFactory, IJavaDebugTarget.class);
             
        IAdapterFactory groupFactory = new ThreadGroupAdapterFactory();
        manager.registerAdapters(groupFactory, IJavaThreadGroup.class);
        
        IAdapterFactory showInFactory = new JavaDebugShowInAdapterFactory();
        manager.registerAdapters(showInFactory, IJavaStackFrame.class);
        
        IAdapterFactory columnFactory = new ColumnPresentationAdapterFactory();
        manager.registerAdapters(columnFactory, IJavaVariable.class);
        manager.registerAdapters(columnFactory, IJavaStackFrame.class);
        
        IAdapterFactory entryFactory = new ClasspathEntryAdapterFactory();
        manager.registerAdapters(entryFactory, DefaultProjectClasspathEntry.class);
        
        IAdapterFactory variableFactory = new JavaDebugElementAdapterFactory();
        manager.registerAdapters(variableFactory, IJavaVariable.class);
        manager.registerAdapters(variableFactory, IJavaValue.class);
        manager.registerAdapters(variableFactory, JavaInspectExpression.class);
        
		fHCRListener= new JavaHotCodeReplaceListener();
		JDIDebugModel.addHotCodeReplaceListener(fHCRListener);
		
		// initialize exception inspector handler
		new ExceptionInspector();
		
		ResourcesPlugin.getWorkspace().addSaveParticipant(getUniqueIdentifier(), new ISaveParticipant() {
			public void doneSaving(ISaveContext sc) {}
			public void prepareToSave(ISaveContext sc)	throws CoreException {}
			public void rollback(ISaveContext sc) {}
			public void saving(ISaveContext sc) throws CoreException {
				IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(getUniqueIdentifier());
				if(prefs != null) {
					try {
						prefs.flush();
					}
					catch (BackingStoreException e) {
						log(e);
					}
				}
			}
		});
		JavaDebugOptionsManager.getDefault().startup();
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			setShuttingDown(true);
			JDIDebugModel.removeHotCodeReplaceListener(fHCRListener);
			JavaDebugOptionsManager.getDefault().shutdown();
			if (fImageDescriptorRegistry != null) {
				fImageDescriptorRegistry.dispose();
			}
			IAdapterManager manager= Platform.getAdapterManager();
			manager.unregisterAdapters(fActionFilterAdapterFactory);
			manager.unregisterAdapters(fSourceLocationAdapterFactory);
			manager.unregisterAdapters(fBreakpointAdapterFactory);
			if (fUtilPresentation != null) {
				fUtilPresentation.dispose();
			} 
			if (fTextTools != null) {
				fTextTools.dispose();
			}
			ResourcesPlugin.getWorkspace().removeSaveParticipant(getUniqueIdentifier());
		} finally {
			super.stop(context);
		}
	}
	
	/**
	 * Returns whether this plug-in is in the process of
	 * being shutdown.
	 *
	 * @return whether this plug-in is in the process of
	 *  being shutdown
	 */
	protected boolean isShuttingDown() {
		return fShuttingDown;
	}

	/**
	 * Sets whether this plug-in is in the process of
	 * being shutdown.
	 *
	 * @param value whether this plug-in is in the process of
	 *  being shutdown
	 */
	private void setShuttingDown(boolean value) {
		fShuttingDown = value;
	}
	
	/**
	 * Returns the image descriptor registry used for this plugin.
	 */
	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		if (getDefault().fImageDescriptorRegistry == null) {
			getDefault().fImageDescriptorRegistry = new ImageDescriptorRegistry();
		}
		return getDefault().fImageDescriptorRegistry;
	}
	
	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display;
		display= Display.getCurrent();
		if (display == null)
			display= Display.getDefault();
		return display;		
	}
	
	/**
	 * Returns the currently active workbench window shell or <code>null</code>
	 * if none.
	 * 
	 * @return the currently active workbench window shell or <code>null</code>
	 */
	public static Shell getShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 0) {
				return windows[0].getShell();
			}
		} 
		else {
			return window.getShell();
		}
		return null;
	}	
	
	/**
	 * Utility method to create and return a selection dialog that allows
	 * selection of a specific Java package.  Empty packages are not returned.
	 * If Java Projects are provided, only packages found within those projects
	 * are included.  If no Java projects are provided, all Java projects in the
	 * workspace are considered.
	 */
	public static ElementListSelectionDialog createAllPackagesDialog(Shell shell, IJavaProject[] originals, final boolean includeDefaultPackage) throws JavaModelException{
		final List<IPackageFragment> packageList = new ArrayList<IPackageFragment>();
		if (originals == null) {
			IWorkspaceRoot wsroot= ResourcesPlugin.getWorkspace().getRoot();
			IJavaModel model= JavaCore.create(wsroot);
			originals= model.getJavaProjects();
		}
		final IJavaProject[] projects= originals;
		final JavaModelException[] exception= new JavaModelException[1];
		final boolean[] monitorCanceled = new boolean[] {false};
		IRunnableWithProgress r= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					Set<String> packageNameSet= new HashSet<String>();
					monitor.beginTask(DebugUIMessages.JDIDebugUIPlugin_Searching_1, projects.length); 
					for (int i = 0; i < projects.length; i++) {						
						IPackageFragment[] pkgs= projects[i].getPackageFragments();	
						for (int j = 0; j < pkgs.length; j++) {
							if (monitor.isCanceled()) {
								monitorCanceled[0] = true;
								return;
							}
							IPackageFragment pkg = pkgs[j];
							if (!pkg.hasChildren() && (pkg.getNonJavaResources().length > 0)) {
								continue;
							}
							String pkgName= pkg.getElementName();
							if (!includeDefaultPackage && pkgName.length() == 0) {
								continue;
							}
							if (packageNameSet.add(pkgName)) {
								packageList.add(pkg);
							}
						}
						monitor.worked(1);
					}
					monitor.done();
				} catch (JavaModelException jme) {
					exception[0]= jme;
				}
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(r);
		} catch (InvocationTargetException e) {
			JDIDebugUIPlugin.log(e);
		} catch (InterruptedException e) {
			JDIDebugUIPlugin.log(e);
		}
		if (exception[0] != null) {
			throw exception[0];
		}
		if (monitorCanceled[0]) {
			return null;
		}
		
		int flags= JavaElementLabelProvider.SHOW_DEFAULT;
		PackageSelectionDialog dialog= new PackageSelectionDialog(shell, new JavaElementLabelProvider(flags));
		dialog.setIgnoreCase(false);
		dialog.setElements(packageList.toArray()); // XXX inefficient
		return dialog;
	}
	
	/**
	 * Return an object that implements <code>ILaunchConfigurationTab</code> for the
	 * specified vm install type ID.  
	 */
	public ILaunchConfigurationTab getVMInstallTypePage(String vmInstallTypeID) {
		if (fVmInstallTypePageMap == null) {	
			initializeVMInstallTypePageMap();
		}
		IConfigurationElement configElement = fVmInstallTypePageMap.get(vmInstallTypeID);
		ILaunchConfigurationTab tab = null;
		if (configElement != null) {
			try {
				tab = (ILaunchConfigurationTab) configElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch(CoreException ce) {			 
				log(new Status(IStatus.ERROR, getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "An error occurred retrieving a VMInstallType page.", ce));  //$NON-NLS-1$
			} 
		}
		return tab;
	}
	
	protected void initializeVMInstallTypePageMap() {
		fVmInstallTypePageMap = new HashMap<String, IConfigurationElement>(10);

		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(getUniqueIdentifier(), IJavaDebugUIConstants.EXTENSION_POINT_VM_INSTALL_TYPE_PAGE);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		for (int i = 0; i < infos.length; i++) {
			String id = infos[i].getAttribute("vmInstallTypeID"); //$NON-NLS-1$
			fVmInstallTypePageMap.put(id, infos[i]);
		}		
	}
	
	/**
	 * Returns a shared utility Java debug model presentation. Clients should not
	 * dispose the presentation.
	 * 
	 * @return a Java debug model presentation
	 */
	public IDebugModelPresentation getModelPresentation() {
		if (fUtilPresentation == null) {
			fUtilPresentation = DebugUITools.newDebugModelPresentation(JDIDebugModel.getPluginIdentifier());
		}
		return fUtilPresentation;
	}
	
	/**
	 * Displays the given preference page.
	 * 
	 * @param id pref page id
	 * @param page pref page
	 * @deprecated use <code>JDIDebugUIPlugin#showPreferencePage(String pageId)</code>, which uses the <code>PreferenceUtils</code> framework for opening pages.
	 */
	@Deprecated
	public static void showPreferencePage(String id, IPreferencePage page) {
		final IPreferenceNode targetNode = new PreferenceNode(id, page);
		
		PreferenceManager manager = new PreferenceManager();
		manager.addToRoot(targetNode);
		final PreferenceDialog dialog = new PreferenceDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), manager);
		final boolean [] result = new boolean[] { false };
		BusyIndicator.showWhile(JDIDebugUIPlugin.getStandardDisplay(), new Runnable() {
			public void run() {
				dialog.create();
				dialog.setMessage(targetNode.getLabelText());
				result[0]= (dialog.open() == Window.OK);
			}
		});		
	}	
	
	/**
	 * Displays the given preference page
	 * @param pageId the fully qualified id of the preference page, e.g. <code>org.eclipse.jdt.debug.ui.preferences.VMPreferencePage</code>
	 * 
	 * @since 3.3
	 */
	public static void showPreferencePage(String pageId) {
		PreferencesUtil.createPreferenceDialogOn(getShell(), pageId, new String[] { pageId }, null).open();
	}
	
	/**
	 * Returns the text tools used by this plug-in
	 * 
	 * @return
	 */
	public JavaTextTools getJavaTextTools() {
		if (fTextTools == null) {
			fTextTools = new JavaTextTools(PreferenceConstants.getPreferenceStore());
		}
		return fTextTools;
	}
}

