package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.SnippetFileDocumentProvider;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Plug-in class for the org.eclipse.jdt.debug.ui plug-in.
 */
public class JDIDebugUIPlugin extends AbstractUIPlugin {

	/**
	 * Java Debug UI plug-in instance
	 */
	private static JDIDebugUIPlugin fgPlugin;
	
	private FileDocumentProvider fSnippetDocumentProvider;
	
	private ImageDescriptorRegistry fImageDescriptorRegistry;
	
	private JavaEvaluationEngineManager fEvaluationEngineManager;
	
	/**
	 * Java Debug UI listeners
	 */
	private IJavaHotCodeReplaceListener fHCRListener;
	private IElementChangedListener fJavaModelListener;
	
	// Map of VMInstallTypeIDs to IConfigurationElements
	protected Map fVmInstallTypePageMap;

	/**
	 * @see Plugin(IPluginDescriptor)
	 */
	public JDIDebugUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
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
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return "org.eclipse.jdt.debug.ui"; //$NON-NLS-1$
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
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
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, DebugUIMessages.getString("JDIDebugUIPlugin.Internal_Error_1"), e));  //$NON-NLS-1$
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
	
	/* (non - Javadoc)
	 * Method declared in AbstractUIPlugin
	 */
	protected ImageRegistry createImageRegistry() {
		return JavaDebugImages.getImageRegistry();
	}	
	
	public IDocumentProvider getSnippetDocumentProvider() {
		if (fSnippetDocumentProvider == null)
			fSnippetDocumentProvider= new SnippetFileDocumentProvider();
		return fSnippetDocumentProvider;
	}	
	
	/**
	 * Logs the given message if in debug mode.
	 * 
	 * @param String message to log
	 */
	public static void logDebugMessage(String message) {
		if (getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			log(new Status(IStatus.ERROR, getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Internal message logged from JDT Debug UI: " + message, null)); //$NON-NLS-1$
		}
	}
	
	public static void errorDialog(String message, IStatus status) {
		log(status);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			ErrorDialog.openError(shell, DebugUIMessages.getString("JDIDebugUIPlugin.Error_1"), message, status); //$NON-NLS-1$
		}
	}
	
	/**
	 * Utility method with conventions
	 */
	public static void errorDialog(String message, Throwable t) {
		log(t);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			IStatus status= new Status(IStatus.ERROR, getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Error logged from JDT Debug UI: ", t); //$NON-NLS-1$	
			ErrorDialog.openError(shell, DebugUIMessages.getString("JDIDebugUIPlugin.Error_1"), message, status); //$NON-NLS-1$
		}
	}
	
	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		super.initializeDefaultPreferences(store);
		JavaDebugPreferencePage.initDefaults(store);
		JavaStepFilterPreferencePage.initDefaults(store);
	}
	
	/**
	 * @see AbstractUIPlugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		JavaDebugOptionsManager.getDefault().startup();
		IAdapterManager manager= Platform.getAdapterManager();
		manager.registerAdapters(new JDIDebugUIAdapterFactory(), IJavaSourceLocation.class);
		fEvaluationEngineManager= new JavaEvaluationEngineManager();
		fJavaModelListener= new JavaModelListener();
		JavaCore.addElementChangedListener(fJavaModelListener);
		fHCRListener= new JavaHotCodeReplaceListener();
		JDIDebugModel.addHotCodeReplaceListener(fHCRListener);
		getStandardDisplay().asyncExec(
			new Runnable() {
				public void run() {
					createImageRegistry();
				}
			});	
	}
	
	/**
	 * @see AbstractUIPlugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		JDIDebugModel.removeHotCodeReplaceListener(fHCRListener);
		JavaCore.removeElementChangedListener(fJavaModelListener);
		JavaDebugOptionsManager.getDefault().shutdown();
		if (fImageDescriptorRegistry != null) {
			fImageDescriptorRegistry.dispose();
		}
		fEvaluationEngineManager.dispose();
		super.shutdown();
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
	 * Returns an evaluation engine for the given project in the given debug target.
	 * 
	 * @see JavaEvaluationEngineManager#getEvaluationEngine(IJavaProject, IJavaDebugTarget)
	 * 
	 * @param project java project
	 * @param target the debug target
	 * @return evalaution engine
	 */
	public IAstEvaluationEngine getEvaluationEngine(IJavaProject project, IJavaDebugTarget target) {
		return fEvaluationEngineManager.getEvaluationEngine(project, target);
	}
	
	/**
	 * Utility method to create and return a selection dialog that allows
	 * selection of a specific Java package.  Empty packages are not returned.
	 * If Java Projects are provided, only packages found within those projects
	 * are included.  If no Java projects are provided, all Java projects in the
	 * workspace are considered.
	 */
	public static ElementListSelectionDialog createAllPackagesDialog(Shell shell, IJavaProject[] originals, final boolean includeDefaultPackage) throws JavaModelException{
		final List packageList = new ArrayList();
		if (originals == null) {
			IWorkspaceRoot wsroot= ResourcesPlugin.getWorkspace().getRoot();
			IJavaModel model= JavaCore.create(wsroot);
			originals= model.getJavaProjects();
		}
		final IJavaProject[] projects= originals;
		final JavaModelException[] exception= new JavaModelException[1];
		ProgressMonitorDialog monitor= new ProgressMonitorDialog(shell);
		IRunnableWithProgress r= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					Set packageNameSet= new HashSet();
					monitor.beginTask(DebugUIMessages.getString("JDIDebugUIPlugin.Searching_1"), projects.length); //$NON-NLS-1$
					for (int i = 0; i < projects.length; i++) {						
						IPackageFragment[] pkgs= projects[i].getPackageFragments();	
						for (int j = 0; j < pkgs.length; j++) {
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
			monitor.run(false, false, r);	
		} catch (InvocationTargetException e) {
			JDIDebugUIPlugin.log(e);
		} catch (InterruptedException e) {
			JDIDebugUIPlugin.log(e);
		}
		if (exception[0] != null) {
			throw exception[0];
		}
		int flags= JavaElementLabelProvider.SHOW_DEFAULT;
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new JavaElementLabelProvider(flags));
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
		IConfigurationElement configElement = (IConfigurationElement) fVmInstallTypePageMap.get(vmInstallTypeID);
		ILaunchConfigurationTab tab = null;
		if (configElement != null) {
			try {
				tab = (ILaunchConfigurationTab) configElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch(CoreException ce) {			 
				log(new Status(Status.ERROR, getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, DebugUIMessages.getString("JDIDebugUIPlugin.An_error_occurred_retrieving_a_VMInstallType_page_1"), ce)); //$NON-NLS-1$
			} 
		}
		return tab;
	}
	
	protected void initializeVMInstallTypePageMap() {
		fVmInstallTypePageMap = new HashMap(10);

		IPluginDescriptor descriptor= JDIDebugUIPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(IJavaDebugUIConstants.EXTENSION_POINT_VM_INSTALL_TYPE_PAGE);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		for (int i = 0; i < infos.length; i++) {
			String id = infos[i].getAttribute("vmInstallTypeID"); //$NON-NLS-1$
			fVmInstallTypePageMap.put(id, infos[i]);
		}		
	}
}

