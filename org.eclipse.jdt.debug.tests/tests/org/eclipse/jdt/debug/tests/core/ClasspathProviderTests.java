package org.eclipse.jdt.debug.tests.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Tests runtime classpath provider extension point
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathProvider;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

public class ClasspathProviderTests extends AbstractDebugTest {
	
	public ClasspathProviderTests(String name) {
		super(name);
	}

	public void testEmptyProvider() throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration("Breakpoints");
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, "org.eclipse.jdt.debug.tests.EmptyClasspathProvider");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, "org.eclipse.jdt.debug.tests.EmptyClasspathProvider");
		
		IRuntimeClasspathProvider cpProvider = JavaRuntime.getClasspathProvider(wc);
		IRuntimeClasspathProvider spProvider = JavaRuntime.getSourceLookupPathProvider(wc);
		
		assertNotNull("Did not retrieve classpath provider", cpProvider);
		assertNotNull("Did not retrieve source path provider", spProvider);
		
		assertEquals("Classpath should be empty", 0, cpProvider.computeUnresolvedClasspath(config).length);
		assertEquals("Source path should be empty", 0, spProvider.computeUnresolvedClasspath(config).length);
	}
	
	
}
