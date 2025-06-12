/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui;


import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

/**
 * A source locator that prompts the user to find source when source cannot
 * be found on the current source lookup path.
 * <p>
 * This class is intended to be instantiated.
 * </p>
 * @since 2.0
 * @deprecated In 3.0, the debug platform provides source lookup facilities that
 *  should be used in place of the Java source lookup support provided in 2.0.
 *  The new facilities provide a source lookup director that coordinates source
 *  lookup among a set of participants, searching a set of source containers.
 *  See the following packages: <code>org.eclipse.debug.core.sourcelookup</code>
 *  and <code>org.eclipse.debug.core.sourcelookup.containers</code>. This class
 *  has been replaced by a Java source lookup director and Java source lookup
 *  participant. To migrate to the new source lookup support clients should
 *  add two new attributes to their launch configuration type extensions:<ul>
 *  <li>sourceLocatorId="org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector"</li>
 *  <li>sourcePathComputerId="org.eclipse.jdt.launching.sourceLookup.javaSourcePathComputer"</li>
 *  </ul>
 *  The source locator id attribute specifies to use the Java source lookup director
 *  for launch configurations of the associated type, and the source path computer id
 *  attribute specifies the class to use when computing a default source lookup
 *  path for a launch configuration. The path computer referenced/provided (by the
 *  above id), computes a default source lookup path based on the support provided in
 *  the 2.0 release - i.e. a configuration's <code>ATTR_SOURCE_PATH_PROVIDER</code>
 *  attribute (if present), or a default source lookup path based on a configuration's
 *  runtime classpath. This class has been replaced by the Java source lookup
 *  director which is an internal class, but can be used via the
 *  <code>sourceLocatorId</code> attribute on a launch configuration type extension.
 * @noextend This class is not intended to be sub-classed by clients.
 */

@Deprecated
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
	private final JavaSourceLocator fSourceLocator;

	/**
	 * Whether the user should be prompted for source.
	 * Initially true, until the user checks the 'do not
	 * ask again' box.
	 */
	private boolean fAllowedToAsk;

	/**
	 * Whether to find all source elements for a stack frame (in case of
	 * duplicates), or just the first match.
	 */
	private boolean fIsFindAllSourceElements = false;

	/**
	 * A cache of types to associated source elements (when duplicates arise and
	 * the users chooses a source element, it is remembered).
	 */
	private HashMap<IJavaReferenceType, Object> fTypesToSource = null;

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
	 * @throws CoreException if the underlying {@link JavaSourceLocator} fails to be created
	 */
	public JavaUISourceLocator(IJavaProject[] projects,	boolean includeRequired) throws CoreException {
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
	@Override
	public Object getSourceElement(IStackFrame stackFrame) {
		Object res = findSourceElement(stackFrame);
		if (res == null && fAllowedToAsk) {
			IJavaStackFrame frame =
 stackFrame.getAdapter(IJavaStackFrame.class);
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
				IJavaReferenceType type = frame.getReferenceType();
				Object cachedSource = getSourceElement(type);
				if (cachedSource != null) {
					return cachedSource;
				}
				// prompt
				TwoPaneElementSelector dialog = new TwoPaneElementSelector(JDIDebugUIPlugin.getActiveWorkbenchShell(), new SourceElementLabelProvider(),new SourceElementQualifierProvider());
				dialog.setTitle(DebugUIMessages.JavaUISourceLocator_Select_Source_1);
				dialog.setMessage(NLS.bind(DebugUIMessages.JavaUISourceLocator__Select_the_source_that_corresponds_to__0__2, type.getName()));
				dialog.setElements(sourceElements);
				dialog.setMultipleSelection(false);
				dialog.setUpperListLabel(DebugUIMessages.JavaUISourceLocator__Matching_files__3);
				dialog.setLowerListLabel(DebugUIMessages.JavaUISourceLocator__Location__4);
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
		}
		return fSourceLocator.getSourceElement(stackFrame);
	}

	private Object getSourceElement(IJavaReferenceType type) {
		if (fTypesToSource == null) {
			return null;
		}
		return fTypesToSource.get(type);
	}

	private void cacheSourceElement(Object sourceElement, IJavaReferenceType type) {
		if (fTypesToSource == null) {
			fTypesToSource = new HashMap<>();
		}
		fTypesToSource.put(type, sourceElement);
	}

	/**
	 * Prompts to locate the source of the given type. Prompts in the UI
	 * thread, since a source lookup could be the result of a conditional
	 * breakpoint looking up source for an evaluation, from the event
	 * dispatch thread.
	 * @param frame the stack frame to show source for
	 *  could not be located
	 */
	private void showDebugSourcePage(final IJavaStackFrame frame) {
		Runnable prompter = new Runnable() {
			@Override
			public void run() {
				try {
					String message = NLS.bind(LauncherMessages.JavaUISourceLocator_selectprojects_message, frame.getDeclaringTypeName());

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
	@Override
	public String getMemento() throws CoreException {
		String memento = fSourceLocator.getMemento();
		String handle = fJavaProject.getHandleIdentifier();
		String findAll = Boolean.toString(isFindAllSourceElements());

		StringBuilder buffer = new StringBuilder();
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
	@Override
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
	@Override
	public void initializeFromMemento(String memento) throws CoreException {
		if (memento.startsWith("<project>")) { //$NON-NLS-1$
			int index = memento.indexOf("</project>"); //$NON-NLS-1$
			if (index > 0) {
				String handle = memento.substring(9, index);
				int start = index + 19;
				index = memento.indexOf("</findAll>", start); //$NON-NLS-1$
				if (index > 0) {
					String findAll = memento.substring(start, index);
					boolean all = Boolean.parseBoolean(findAll);
					String rest = memento.substring(index + 10);
					fJavaProject = (IJavaProject) JavaCore.create(handle);
					fIsFindAllSourceElements = all;
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
	 * Returns the locations that this source locator is currently
	 * searching, in the order that they are searched.
	 *
	 * @return the locations that this source locator is currently
	 * searching, in the order that they are searched
	 */
	public IJavaSourceLocation[] getSourceLocations() {
		return fSourceLocator.getSourceLocations();
	}

	/**
	 * /**
	 * Sets the locations that will be searched, in the order
	 * to be searched.
	 *
	 * @param locations the locations that will be searched, in the order
	 *  to be searched
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
