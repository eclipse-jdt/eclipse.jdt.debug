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
package org.eclipse.jdt.debug.ui;


import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.SourceElementLabelProvider;
import org.eclipse.jdt.internal.debug.ui.launcher.SourceElementQualifierProvider;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

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
	 * Launch configuration attribute indicating that this source locator should
	 * locate all source elements that correspond to a stack frame, rather than
	 * the first match. Default value is <code>false</code>.
	 * 
	 * @since 2.1
	 */
	public static final String ATTR_FIND_ALL_SOURCE_ELEMENTS = IJavaDebugUIConstants.PLUGIN_ID + ".ATTR_FIND_ALL_SOURCE_ELEMENTS"; //$NON-NLS-1$

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
	 * Whether to find all source elements for a stack frame (in case of
	 * dupliucates), or just the first match.
	 */
	private boolean fIsFindAllSourceElements = false;
	
	/**
	 * A cache of types to associated source elements (when duplicates arise and
	 * the users chooses a source element, it is remembered).
	 */
	private HashMap fTypesToSource = null;

	/**
	 * Constructs an empty source locator.
	 */
	public JavaUISourceLocator() {
		fSourceLocator = new JavaSourceLocator();
		fAllowedToAsk = true;
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
	public JavaUISourceLocator(
		IJavaProject[] projects,
		boolean includeRequired)
		throws JavaModelException {
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
		fJavaProject = project;
		IJavaSourceLocation[] sls =
			JavaSourceLocator.getDefaultSourceLocations(project);
		fSourceLocator = new JavaSourceLocator(project);
		if (sls != null) {
			fSourceLocator.setSourceLocations(sls);
		}
		fAllowedToAsk = true;
	}

	/**
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement(IStackFrame stackFrame) {
		Object res = findSourceElement(stackFrame);
		if (res == null && fAllowedToAsk) {
			IJavaStackFrame frame =
				(IJavaStackFrame) stackFrame.getAdapter(IJavaStackFrame.class);
			if (frame != null) {
				try {
					if (!frame.isObsolete()) {
						showDebugSourcePage(frame);
						res = fSourceLocator.getSourceElement(stackFrame);
					}
				} catch (DebugException e) {
				}
			}
		}
		return res;
	}

	private Object findSourceElement(IStackFrame stackFrame) {
		if (isFindAllSourceElements()) {
			Object[] sourceElements = fSourceLocator.getSourceElements(stackFrame);
			if (sourceElements == null || sourceElements.length == 0) {
				return null;
			} 
			if (sourceElements.length == 1) {
				return sourceElements[0];
			}
			try {
				IJavaStackFrame frame = (IJavaStackFrame)stackFrame;
				IJavaClassType type = frame.getDeclaringType();
				Object cachedSource = getSourceElement(type);
				if (cachedSource != null) {
					return cachedSource;
				}
				// prompt
				TwoPaneElementSelector dialog = new TwoPaneElementSelector(JDIDebugUIPlugin.getActiveWorkbenchShell(), new SourceElementLabelProvider(),new SourceElementQualifierProvider());
				dialog.setTitle(DebugUIMessages.getString("JavaUISourceLocator.Select_Source_1")); //$NON-NLS-1$
				dialog.setMessage(MessageFormat.format(DebugUIMessages.getString("JavaUISourceLocator.&Select_the_source_that_corresponds_to_{0}_2"), new String[]{type.getName()})); //$NON-NLS-1$
				dialog.setElements(sourceElements);
				dialog.setMultipleSelection(false);
				dialog.setUpperListLabel(DebugUIMessages.getString("JavaUISourceLocator.&Matching_files__3")); //$NON-NLS-1$
				dialog.setLowerListLabel(DebugUIMessages.getString("JavaUISourceLocator.&Location__4")); //$NON-NLS-1$
				dialog.open();
				Object[] result = dialog.getResult();
				if (result == null) {
					return null;
				}
				Object sourceElement = result[0];
				cacheSourceElement(sourceElement, type);
				return sourceElement;
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
				return sourceElements[0];
			}
		} else {
			return fSourceLocator.getSourceElement(stackFrame);
		}
	}
	
	private Object getSourceElement(IJavaClassType type) {
		if (fTypesToSource == null) {
			return null; 
		}
		return fTypesToSource.get(type);
	}
	
	private void cacheSourceElement(Object sourceElement, IJavaClassType type) {
		if (fTypesToSource == null) {
			fTypesToSource = new HashMap();
		}
		fTypesToSource.put(type, sourceElement);
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
					ILaunchConfiguration configuration =
						frame.getLaunch().getLaunchConfiguration();
					JavaSourceLookupDialog dialog =
						new JavaSourceLookupDialog(
							JDIDebugUIPlugin.getActiveWorkbenchShell(),
							message,
							configuration);
					int result = dialog.open();
					if (result == Window.OK) {
						fAllowedToAsk = !dialog.isNotAskAgain();
						JavaUISourceLocator.this.initializeDefaults(
							configuration);
					}
				} catch (CoreException e) {
					// only report an error if the thread has not resumed
					if (e.getStatus().getCode()
						!= IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
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
		String findAll = new Boolean(isFindAllSourceElements()).toString();

		StringBuffer buffer = new StringBuffer();
		buffer.append("<project>"); //$NON-NLS-1$
		buffer.append(handle);
		buffer.append("</project>"); //$NON-NLS-1$
		buffer.append("<findAll>"); //$NON-NLS-1$
		buffer.append(findAll);
		buffer.append("</findAll>"); //$NON-NLS-1$
		buffer.append(memento);
		return buffer.toString();
	}

	/**
	 * @see IPersistableSourceLocator#initializeDefaults(ILaunchConfiguration)
	 */
	public void initializeDefaults(ILaunchConfiguration configuration)
		throws CoreException {
		fSourceLocator.initializeDefaults(configuration);
		fJavaProject = JavaRuntime.getJavaProject(configuration);
		fIsFindAllSourceElements =
			configuration.getAttribute(ATTR_FIND_ALL_SOURCE_ELEMENTS, false);
	}

	/**
	 * @see IPersistableSourceLocator#initializeFromMemento(String)
	 */
	public void initializeFromMemento(String memento) throws CoreException {
		if (memento.startsWith("<project>")) { //$NON-NLS-1$
			int index = memento.indexOf("</project>"); //$NON-NLS-1$
			if (index > 0) {
				String handle = memento.substring(9, index);
				int start = index + 19;
				index = memento.indexOf("</findAll>", start); //$NON-NLS-1$
				if (index > 0) {
					String findAll = memento.substring(start, index);
					Boolean all = new Boolean(findAll);
					String rest = memento.substring(index + 10);
					fJavaProject = (IJavaProject) JavaCore.create(handle);
					fIsFindAllSourceElements = all.booleanValue();
					fSourceLocator.initializeFromMemento(rest);
				}
			}
		} else {
			// OLD FORMAT
			int index = memento.indexOf('\n');
			String handle = memento.substring(0, index);
			String rest = memento.substring(index + 1);
			fJavaProject = (IJavaProject) JavaCore.create(handle);
			fIsFindAllSourceElements = false;
			fSourceLocator.initializeFromMemento(rest);
		}
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

	/**
	 * Returns whether this source locator is configured to search for all
	 * source elements that correspond to a stack frame. When <code>false</code>
	 * is returned, searching stops on the first match. If there is more than
	 * one source element that corresponds to a stack frame, the user is
	 * prompted to choose a source element to open.
	 * 
	 * @return whether this source locator is configured to search for all
	 * source elements that correspond to a stack frame
	 * @since 2.1
	 */
	public boolean isFindAllSourceElements() {
		return fIsFindAllSourceElements;
	}

	/**
	 * Sets whether this source locator is configured to search for all source
	 * elements that correspond to a stack frame, or the first match.
	 * 
	 * @param findAll whether this source locator should search for all source
	 * elements that correspond to a stack frame
	 * @since 2.1
	 */
	public void setFindAllSourceElement(boolean findAll) {
		fIsFindAllSourceElements = findAll;
	}

}
