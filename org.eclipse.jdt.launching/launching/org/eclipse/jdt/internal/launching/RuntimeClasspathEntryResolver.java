package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;

/**
 * Proxy to a runtime classpath entry resolver extension.
 */
public class RuntimeClasspathEntryResolver implements IRuntimeClasspathEntryResolver {

	private IConfigurationElement fConfigurationElement;
	
	private IRuntimeClasspathEntryResolver fDelegate;
	
	/**
	 * Constructs a new resolver on the given configuration element
	 */
	public RuntimeClasspathEntryResolver(IConfigurationElement element) {
		fConfigurationElement = element;
	}
	
	/**
	 * @see IRuntimeClasspathEntryResolver#resolveForClasspath(IRuntimeClasspathEntry, ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveForClasspath(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		return getResolver().resolveForClasspath(entry, configuration);
	}

	/**
	 * @see IRuntimeClasspathEntryResolver#resolveForSourceLookupPath(IRuntimeClasspathEntry, ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveForSourceLookupPath(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		return getResolver().resolveForSourceLookupPath(entry, configuration);
	}
	
	/**
	 * Returns the resolver delegate (and creates if required) 
	 */
	protected IRuntimeClasspathEntryResolver getResolver() throws CoreException {
		if (fDelegate == null) {
			fDelegate = (IRuntimeClasspathEntryResolver)fConfigurationElement.createExecutableExtension("class");
		}
		return fDelegate;
	}
	
	/**
	 * Returns the variable name this resolver is registered for, or <code>null</code>
	 */
	public String getVariableName() {
		return fConfigurationElement.getAttribute("variable");
	}
	
	/**
	 * Returns the container id this resolver is registered for, or <code>null</code>
	 */
	public String getContainerId() {
		return fConfigurationElement.getAttribute("container");
	}	

}
