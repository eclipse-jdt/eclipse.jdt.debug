package org.eclipse.jdt.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

/**
 * A source locator that prompts the user to find source when source cannot
 * be found on the current source lookup path.
 * <p>
 * This class is intended to be instantiated. This class is not
 * intended to be subclassed.
 * </p>
 * @since 2.0
 */

public class JavaUISourceLocator implements IPersistableSourceLocator {

	/**
	 * Identifier for the 'Prompting Java Source Locator' extension
	 * (value <code>"org.eclipse.jdt.debug.ui.javaSourceLocator"</code>).
	 */
	public static final String ID_PROMPTING_JAVA_SOURCE_LOCATOR = IJavaDebugUIConstants.PLUGIN_ID + ".javaSourceLocator"; //$NON-NLS-1$
	
	/**
	 * The project being debugged.
	 */
	private IJavaProject fJavaProject; 
	
	/**
	 * Underlying source locator.
	 */
	private JavaSourceLocator fSourceLocator;
	
	/**
	 * Whether the user should be prompted for source.
	 * Initially true, until the user checks the 'do not
	 * ask again' box.
	 */
	private boolean fAllowedToAsk;
	
	/**
	 * Constructs an empty source locator.
	 */
	public JavaUISourceLocator() {
		fSourceLocator = new JavaSourceLocator();
		fAllowedToAsk= true;
	}

	/**
	 * Constructs a new source locator that looks in the
	 * specified project for source, and required projects, if
	 * <code>includeRequired</code> is <code>true</code>.
	 * 
	 * @param projects the projects in which to look for source
	 * @param includeRequired whether to look in required projects
	 * 	as well
	 */
	public JavaUISourceLocator(IJavaProject[] projects, boolean includeRequired) throws JavaModelException {
		fSourceLocator = new JavaSourceLocator(projects, includeRequired);
		fAllowedToAsk = true;
	}	
		
	/**
	 * Constructs a source locator that searches for source
	 * in the given Java project, and all of its required projects,
	 * as specified by its build path or default source lookup
	 * settings.
	 * 
	 * @param project Java project
	 * @exception CoreException if unable to read the project's
	 * 	 build path
	 */
	public JavaUISourceLocator(IJavaProject project) throws CoreException {
		fJavaProject= project;
		IJavaSourceLocation[] sls = JavaSourceLocator.getDefaultSourceLocations(project);
		fSourceLocator= new JavaSourceLocator(project);
		if (sls != null) {
			fSourceLocator.setSourceLocations(sls);
		}
		fAllowedToAsk= true;
	}

	/**
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement(IStackFrame stackFrame) {
		Object res= fSourceLocator.getSourceElement(stackFrame);
		if (res == null && fAllowedToAsk) {
			IJavaStackFrame frame= (IJavaStackFrame)stackFrame.getAdapter(IJavaStackFrame.class);
			if (frame != null) {
				try {
					if (!frame.isObsolete()) {
						showDebugSourcePage(frame);
						res= fSourceLocator.getSourceElement(stackFrame);
					}
				} catch (DebugException e) {
				}
			}
		}
		return res;
	}
	
	/**
	 * Prompts to locate the source of the given type. Prompts in the UI
	 * thread, since a source lookup could be the result of a conditional
	 * breakpoint looking up source for an evaluation, from the event
	 * dispatch thread.
	 * 
	 * @param typeName the name of the type for which source
	 *  could not be located
	 */
	private void showDebugSourcePage(final IJavaStackFrame frame) {
		Runnable prompter = new Runnable() {
			public void run() {
				try {
					String message = LauncherMessages.getFormattedString("JavaUISourceLocator.selectprojects.message", frame.getDeclaringTypeName()); //$NON-NLS-1$
					ILaunchConfiguration configuration = frame.getLaunch().getLaunchConfiguration();
					JavaSourceLookupDialog dialog= new JavaSourceLookupDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), message, configuration);
					int result = dialog.open();
					if (result == JavaSourceLookupDialog.OK) {
						fAllowedToAsk= !dialog.isNotAskAgain();
						JavaUISourceLocator.this.initializeDefaults(configuration);
					}
				} catch (CoreException e) {
					// only report an error if the thread has not resumed
					if (e.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
						JDIDebugUIPlugin.log(e);
					}
				}
			}
		};
		JDIDebugUIPlugin.getStandardDisplay().syncExec(prompter);
	}
		
	/**
	 * @see IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException {
		String memento = fSourceLocator.getMemento();
		String handle = fJavaProject.getHandleIdentifier();
		memento = handle + '\n' + memento;
		return memento;
	}

	/**
	 * @see IPersistableSourceLocator#initializeDefaults(ILaunchConfiguration)
	 */
	public void initializeDefaults(ILaunchConfiguration configuration)
		throws CoreException {
			fSourceLocator.initializeDefaults(configuration);
			fJavaProject = JavaRuntime.getJavaProject(configuration);
	}

	/**
	 * @see IPersistableSourceLocator#initializeFromMemento(String)
	 */
	public void initializeFromMemento(String memento) throws CoreException {
		int index = memento.indexOf('\n');
		String handle = memento.substring(0, index);
		String rest = memento.substring(index + 1);
		fJavaProject = (IJavaProject)JavaCore.create(handle);
		fSourceLocator.initializeFromMemento(rest);
	}
	
	/**
	 * @see JavaSourceLocator#getSourceLocations()
	 */
	public IJavaSourceLocation[] getSourceLocations() {
		return fSourceLocator.getSourceLocations();
	}
	
	/**
	 * @see JavaSourceLocator#setSourceLocations(IJavaSourceLocation[])
	 */
	public void setSourceLocations(IJavaSourceLocation[] locations) {
		fSourceLocator.setSourceLocations(locations);
	}
}

