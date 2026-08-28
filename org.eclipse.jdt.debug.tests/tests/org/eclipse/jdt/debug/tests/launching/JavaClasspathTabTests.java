/*******************************************************************************
 * Copyright (c) 2026 Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import java.util.Collections;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.ui.AbstractDebugUiTests;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Tests for {@link JavaClasspathTab}.
 */
public class JavaClasspathTabTests extends AbstractDebugUiTests {

	private static final class TestJavaClasspathTab extends JavaClasspathTab {

		/**
		 * Notifies the tab that its classpath entries changed.
		 */
		private void markEntriesChanged() {
			entriesChanged(fClasspathViewer);
		}

		private IRuntimeClasspathEntry[] getDisplayedEntries() {
			return getModel().getAllEntries();
		}
	}

	/**
	 * Creates the test instance.
	 *
	 * @param name the test name
	 */
	public JavaClasspathTabTests(String name) {
		super(name);
	}

	/**
	 * Verifies that duplicate default entries which differ only in their exported
	 * build-path flag do not make the classpath custom.
	 *
	 * @throws Exception if project setup or the classpath tab interaction fails
	 */
	public void testDefaultClasspathWithExportedAndNonExportedDuplicate() throws Exception {
		IJavaProject application = null;
		IJavaProject dependency = null;
		IJavaProject intermediate = null;
		try {
			application = JavaProjectHelper.createJavaProject("ClasspathApplication", JavaProjectHelper.BIN_DIR); //$NON-NLS-1$
			dependency = JavaProjectHelper.createJavaProject("ClasspathDependency", JavaProjectHelper.BIN_DIR); //$NON-NLS-1$
			intermediate = JavaProjectHelper.createJavaProject("ClasspathIntermediate", JavaProjectHelper.BIN_DIR); //$NON-NLS-1$
			application.getProject().getFolder(LAUNCHCONFIGURATIONS).create(true, true, null);
			JavaProjectHelper.addSourceContainer(application, JavaProjectHelper.SRC_DIR);
			JavaProjectHelper.addSourceContainer(dependency, JavaProjectHelper.SRC_DIR);
			JavaProjectHelper.addSourceContainer(intermediate, JavaProjectHelper.SRC_DIR);

			JavaProjectHelper.addToClasspath(application, JavaCore.newProjectEntry(dependency.getProject().getFullPath()));
			JavaProjectHelper.addToClasspath(application, JavaCore.newProjectEntry(intermediate.getProject().getFullPath()));
			JavaProjectHelper.addToClasspath(intermediate,
					JavaCore.newProjectEntry(dependency.getProject().getFullPath(), new IAccessRule[0], true, new IClasspathAttribute[0], true));
			waitForBuild();

			ILaunchConfiguration configuration = createLaunchConfiguration(application, "ClasspathMain"); //$NON-NLS-1$
			IRuntimeClasspathEntry[] defaultClasspath = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
			assertEquals("Test setup must create duplicate default entries", 2, //$NON-NLS-1$
					countEntriesForProject(defaultClasspath, dependency));

			ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
			applyClasspathTab(configuration, workingCopy, dependency);

			assertFalse("Default classpath attribute must remain unset (actual: " //$NON-NLS-1$
					+ workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true) + ')',
					workingCopy.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH));
			assertFalse("Custom classpath attribute must remain unset", //$NON-NLS-1$
					workingCopy.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH));
		} finally {
			if (application != null && application.exists()) {
				JavaProjectHelper.delete(application);
			}
			if (intermediate != null && intermediate.exists()) {
				JavaProjectHelper.delete(intermediate);
			}
			if (dependency != null && dependency.exists()) {
				JavaProjectHelper.delete(dependency);
			}
		}
	}

	/**
	 * Verifies that a genuinely custom classpath is not incorrectly restored to
	 * the default classpath.
	 *
	 * @throws Exception if launch-configuration setup or classpath tab
	 *             interaction fails
	 */
	public void testCustomClasspath() throws Exception {
		ILaunchConfiguration configuration = createLaunchConfiguration(get14Project(), "CustomClasspath", true); //$NON-NLS-1$
		try {
			IRuntimeClasspathEntry customEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path("/CustomClasspath.jar")); //$NON-NLS-1$
			String customMemento = customEntry.getMemento();
			ILaunchConfigurationWorkingCopy initialWorkingCopy = configuration.getWorkingCopy();
			initialWorkingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			initialWorkingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, Collections.singletonList(customMemento));
			ILaunchConfiguration customConfiguration = initialWorkingCopy.doSave();

			ILaunchConfigurationWorkingCopy workingCopy = customConfiguration.getWorkingCopy();
			applyClasspathTab(customConfiguration, workingCopy, null);

			assertFalse("Custom classpath must remain custom", //$NON-NLS-1$
					workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true));
			assertEquals("Custom classpath entry must remain persisted", Collections.singletonList(customMemento), //$NON-NLS-1$
					workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, Collections.<String>emptyList()));
		} finally {
			configuration.delete();
		}
	}

	/**
	 * Initializes and applies a classpath tab without changing its displayed
	 * entries.
	 *
	 * @param configuration the configuration displayed by the tab
	 * @param workingCopy the configuration to which the tab is applied
	 * @param dependency the dependency whose duplicate entries are expected, or
	 *            <code>null</code> when no duplicate check is required
	 */
	private void applyClasspathTab(ILaunchConfiguration configuration, ILaunchConfigurationWorkingCopy workingCopy, IJavaProject dependency) {
		sync(() -> {
			Composite parent = new Composite(JDIDebugUIPlugin.getActiveWorkbenchShell(), SWT.NONE);
			try {
				TestJavaClasspathTab tab = new TestJavaClasspathTab();
				tab.createControl(parent);
				tab.initializeFrom(configuration);
				if (dependency != null) {
					assertEquals("Classpath tab must de-duplicate the default dependency entries", 1, //$NON-NLS-1$
							countEntriesForProject(tab.getDisplayedEntries(), dependency));
				}
				tab.markEntriesChanged();
				tab.performApply(workingCopy);
			} finally {
				parent.dispose();
			}
		});
	}

	/**
	 * Counts the runtime classpath entries for a project.
	 *
	 * @param entries the runtime classpath entries
	 * @param project the project to look for
	 * @return the number of entries whose path identifies the project
	 */
	private int countEntriesForProject(IRuntimeClasspathEntry[] entries, IJavaProject project) {
		int count = 0;
		IPath projectPath = project.getProject().getFullPath();
		for (IRuntimeClasspathEntry entry : entries) {
			if (projectPath.equals(entry.getPath())) {
				count++;
			}
		}
		return count;
	}
}
