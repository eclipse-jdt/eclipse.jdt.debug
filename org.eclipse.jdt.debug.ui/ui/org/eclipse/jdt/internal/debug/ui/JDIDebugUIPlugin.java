package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.SnippetFileDocumentProvider;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
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
	 * Returns the identifier for the Java Debug UI plug-in
	 * 
	 * @return the identifier for the Java Debug UI plug-in
	 */
	public static String getPluginId() {
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
		log(new Status(IStatus.ERROR, getPluginId(), IJavaDebugUIConstants.INTERNAL_ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e the exception to be logged
	 */	
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IJavaDebugUIConstants.INTERNAL_ERROR, DebugUIMessages.getString("JDIDebugUIPlugin.Internal_Error_1"), e));  //$NON-NLS-1$
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
		return getDefault().getActiveWorkbenchWindow().getActivePage();
	}
	
	
	/**
	 * Returns the active workbench shell
	 * 
	 * @return the active workbench shell
	 */
	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
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
			log(new Status(IStatus.ERROR, getPluginId(), IJavaDebugUIConstants.INTERNAL_ERROR, "Internal message logged from JDT Debug UI: " + message, null));
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
			IStatus status= new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Error logged from JDT Debug UI: ", t); //$NON-NLS-1$	
			ErrorDialog.openError(shell, DebugUIMessages.getString("JDIDebugUIPlugin.Error_1"), message, status); //$NON-NLS-1$
		}
	}
	
	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		super.initializeDefaultPreferences(store);
		
		store.setDefault(IJDIPreferencesConstants.ATTACH_LAUNCH_PORT, "8000"); //$NON-NLS-1$
		store.setDefault(IJDIPreferencesConstants.ATTACH_LAUNCH_HOST, "localhost"); //$NON-NLS-1$
		store.setDefault(IJDIPreferencesConstants.ALERT_HCR_FAILED, true);
		store.setDefault(IJDIPreferencesConstants.ALERT_OBSOLETE_METHODS, true);
		
		JavaDebugPreferencePage.initDefaults(store);
		JavaStepFilterPreferencePage.initDefaults(store);
	}
	
	/**
	 * @see AbstractUIPlugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		initializeHCRListener();
		initializeOptionsManager();
		initializeAdapterManager();
		initializeEvaluationEngineManager();
		initializeJavaModelListener();
		initializeImageRegistry();	
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
	
	private void initializeHCRListener() {
		fHCRListener= new JavaHotCodeReplaceListener();
		JDIDebugModel.addHotCodeReplaceListener(fHCRListener);
	}
	
	private void initializeOptionsManager() throws CoreException {
		JavaDebugOptionsManager.getDefault().startup();
	}
	
	private void initializeAdapterManager() {
		IAdapterManager manager= Platform.getAdapterManager();
		manager.registerAdapters(new JDIDebugUIAdapterFactory(), IJavaSourceLocation.class);
	}
	
	private void initializeEvaluationEngineManager() {
		fEvaluationEngineManager= new JavaEvaluationEngineManager();
	}
	
	private void initializeJavaModelListener() {
		fJavaModelListener= new JavaModelListener();
		JavaCore.addElementChangedListener(fJavaModelListener);
	}
	
	private void initializeImageRegistry() {
		getStandardDisplay().asyncExec(
			new Runnable() {
				public void run() {
					createImageRegistry();
				}
			});	
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
}

