package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Resolves variable and/or container runtime classpath entries in
 * the context of a launch configuration. A resolver can be declared
 * as an extension (<code>org.eclipse.jdt.launching.runtimeClasspathEntryResolver</code>),
 * or be registered with the <code>JavaRuntime</code> programatically.
 * <p>
 * A resolver is registered for a runtime classpath entry of type
 * <code>VARIABLE</code> and/or <code>CONTAINER</code>. A resolver is
 * consulted when a runtime classpath entry is needs to be resolved..
 * </p>
 * A resolver extension is defined in <code>plugin.xml</code>.
 * Following is an example definition of a runtime classpath entry
 * resolver extension.
 * <pre>
 * &lt;extension point="org.eclipse.jdt.launching.runtimeClasspathEntryResolvers"&gt;
 *   &lt;runtimeClasspathEntryResolver 
 *      id="com.example.ExampleResolver"
 *      class="com.example.ExampleResolverImpl"
 * 		variable="VAR_NAME"
 * 		container="CONTAINER_ID"
 *   &lt;/runtimeClasspathEntryResolvers&gt;
 * &lt;/extension&gt;
 * </pre>
 * The attributes are specified as follows:
 * <ul>
 * <li><code>id</code> specifies a unique identifier for this extension.</li>
 * <li><code>class</code> specifies the fully qualified name of the Java class
 *   that implements <code>IRuntimeClasspathEntryResolver</code>.</li>
 * <li><code>variable</code> name of the classpath variable this resolver
 * 	is registered for.</li>
 * <li><code>container</code> identifier of the classpath container this
 * 	resolver is registered for.</li>
 * </ul>
 * At least one of <code>variable</code> or <code>container</code> must be
 * specified.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 2.0
 */
public interface IRuntimeClasspathEntryResolver {
	
	/**
	 * Returns resolved runtime classpath entries for the given classpath entry,
	 * in the context of the given launch configuration.
	 * 
	 * @param entry runtime classpath entry to resolve, of type
	 * 	<code>VARIABLE</code> or <code>CONTAINTER</code>
	 * @param configuration the context in which the runtime classpath entry
	 * 	needs to be resolved
	 * @return resolved entries (zero or more)
	 * @exception CoreException if unable to resolve the entry  
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException;
	
	/**
	 * Returns a VM install associated with the given classpath entry,
	 * or <code>null</code> if none.
	 * 
	 * @param entry classpath entry
	 * @return vm install associated with entry or <code>null</code> if none
	 * @exception CoreException if unable to resolve a VM
	 */
	public IVMInstall resolveVMInstall(IClasspathEntry entry) throws CoreException;
}
