package org.eclipse.jdt.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import java.util.List;

/**
 * A Java stack frame is an extension of a regular stack
 * frame, providing support specific to the JDI debug model.
 * A Java stack frame is also available as an adapter from
 * stack frames originating for the JDI debug model.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.debug.core.model.IStackFrame
 * @see org.eclipse.core.runtime.IAdaptable 
 */

public interface IJavaStackFrame extends IStackFrame, IJavaModifiers, IJavaEvaluate {
			
	/**
	 * Drops to this stack frame by popping stack frames in this stack
	 * frame's owning thread until this stack frame is the top stack frame.
	 * This effectively resumes this stack frame's thread which may result
	 * in an exception or breakpoint suspending execution.
	 *
	 * @exception DebugException on failure. Reasons include:<ul>
	 * <li>TARGET_REQUEST_FAILED - The request failed in the target
	 * <li>NOT_SUPPORTED - The capability is not supported by the target
	 * </ul>
	 */
	void dropToFrame() throws DebugException;
	
	/**
	 * Returns whether this stack frame currently supports the drop
	 * to frame operation. Note that not all VMs support the operation.
	 *
	 * @return whether this stack frame currently supports drop to frame
	 */
	boolean supportsDropToFrame();
	
	/**
	 * Returns whether this element has been declared as abstract.
	 * 
	 * @return whether this element has been declared as abstract
	 * @exception DebugException if unable to determine if this
	 *   element has been declared as abstract
	 */
	public boolean isAbstract() throws DebugException;
	/**
	 * Returns whether this element has been declared as native.
	 * 
	 * @return whether this element has been declared as native
	 * @exception DebugException if unable to determine if this
	 *   element has been declared as native
	 */
	public boolean isNative() throws DebugException;
	/**
	 * Returns whether this element is a static initializer.
	 * 
	 * @return whether this element is a static initializer
	 * @exception DebugException if unable to determine if this
	 *   element is a static initializer 
	 */
	public boolean isStaticInitializer() throws DebugException;
	/**
	 * Returns whether this element has been declared as synchronized.
	 *
	 * @return whether this element has been declared as synchronized
	 * @exception DebugException if unable to determine if this
	 *   element has been declared as synchronized
	 */
	public boolean isSynchronized() throws DebugException;
	/**
	 * Returns the fully qualified name of the type that declares the method
	 * associated with this stack frame.
	 *
	 * @return declaring type name
	 * @exception DebugException if unable to retrieve this stack frame's
	 *    declaring type name from the target
	 */
	public String getDeclaringTypeName() throws DebugException;
	/**
	 * Returns the fully qualified type name of the receiver object associated
	 * with this stack frame
	 *
	 * @return receiving type name
	 * @exception DebugException if unable to retrieve this stack frame's
	 *    reveiving type name from the target
	 */
	public String getReceivingTypeName() throws DebugException;
	
	/**
	 * Returns the signature for the method this stack frame is associated with.
	 * The signature is in JNI format.
	 *
	 * @return signature
	 * @exception DebugException if unable to retrieve this stack frame's
	 *    signature from the target
	 */
	public String getSignature() throws DebugException;
	
	/**
	 * Returns a list of fully qualified types of the arguments for the method
	 * associated with this stack frame.
	 *
	 * @return argument type names, or an empty list if this method has no arguments
	 * @exception DebugException if unable to retrieve this stack
	 *   frame's argument names from the target
	 */
	public List getArgumentTypeNames() throws DebugException;
	
	/**
	 * Returns the name of the method associated with this stack frame
	 *
	 * @return method name
	 * @exception DebugException if unable to retrieve this stack frame's
	 *    method name from the target
	 */
	public String getMethodName() throws DebugException;
	
	/**
	 * Returns the local, static, or "this" variable with the given
	 * name, or <code>null</code> if unable to resolve a variable with the name.
	 *
	 * @param variableName the name of the variable to search for
	 * @return a variable, or <code>null</code> if none
	 * @exception DebugException if an exception occurrs while searching
	 *    for the variable on the target
	 */
	IVariable findVariable(String variableName) throws DebugException;
	
	/**
	 * Returns the unqualified name of the source file this stack frame is associated 
	 * with, or <code>null</code> if the source name is not known. For example, if the
	 * declaring type associated with this stack frame is "com.example.Example", 
	 * the associated source name would be "Example.java".
	 * 
	 * @return unqualified source file name, or <code>null</code>
	 * @exception DebugException if an exception occurs retrieving the name from the target
	 */
	public String getSourceName() throws DebugException;
}


