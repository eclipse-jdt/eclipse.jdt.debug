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
package org.eclipse.jdt.debug.core;


import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IFilteredStep;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * A stack frame in a thread on a Java virtual machine.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see org.eclipse.debug.core.model.IStackFrame
 */

public interface IJavaStackFrame extends IStackFrame, IJavaModifiers, IFilteredStep {
			
	/**
	 * Drops to this stack frame by popping frames in this 
	 * frame's owning thread until this stack frame is the top stack frame.
	 * The execution location is set to the beginning of this frame's
	 * associated method.
	 *
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The capability is not supported by the target.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
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
	 * Returns whether the method associated with this stack frame
	 * is a constructor.
	 * 
	 * @return whether this stack frame is associated with a constructor
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public boolean isConstructor() throws DebugException;
		
	/**
	 * Returns whether the method associated with this stack frame
	 * has been declared as native.
	 * 
	 * @return whether this stack frame has been declared as native
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public boolean isNative() throws DebugException;
	/**
	 * Returns whether the method associated with this stack frame
	 * is a static initializer.
	 * 
	 * @return whether this stack frame is a static initializer
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public boolean isStaticInitializer() throws DebugException;
	/**
	 * Returns whether the method associated with this stack frame
	 * has been declared as synchronized.
	 *
	 * @return whether this stack frame has been declared as synchronized
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public boolean isSynchronized() throws DebugException;
	/**
	 * Returns whether the method associated with this stack frame
	 * is running code in the VM that is out of synch with the code
	 * in the workspace.
	 * 
	 * @return whether this stack frame is out of synch with the workspace.
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 * @since 2.0
	 */
	public boolean isOutOfSynch() throws DebugException;
	/**
	 * Returns whether the method associated with this stack frame is
	 * obsolete, that is, it is running old bytecodes that have been
	 * replaced in the VM. This can occur when a hot code replace
	 * succeeds but the VM is unable to pop a call to an affected
	 * method from the call stack.
	 * @return whether this stack frame's method is obsolete
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 * @since 2.0
	 */
	public boolean isObsolete() throws DebugException;
	/**
	 * Returns the fully qualified name of the type that declares the method
	 * associated with this stack frame.
	 *
	 * @return declaring type name
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public String getDeclaringTypeName() throws DebugException;
	/**
	 * Returns the fully qualified name of the type that is the receiving object
	 * associated with this stack frame
	 *
	 * @return receiving type name
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public String getReceivingTypeName() throws DebugException;
	
	/**
	 * Returns the JNI signature for the method this stack frame is associated with.
	 *
	 * @return signature
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public String getSignature() throws DebugException;
	
	/**
	 * Returns a list of fully qualified type names of the arguments for the method
	 * associated with this stack frame.
	 *
	 * @return argument type names, or an empty list if this method has no arguments
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public List getArgumentTypeNames() throws DebugException;
	
	/**
	 * Returns the name of the method associated with this stack frame
	 *
	 * @return method name
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public String getMethodName() throws DebugException;
	
	/**
	 * Returns the local, static, or "this" variable with the given
	 * name, or <code>null</code> if unable to resolve a variable with the name.
	 *
	 * @param variableName the name of the variable to search for
	 * @return a variable, or <code>null</code> if none
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public IJavaVariable findVariable(String variableName) throws DebugException;
	
	/**
	 * Returns the line number of the instruction pointer in 
	 * this stack frame that corresponds to the line in the associated source
	 * element in the specified stratum, or <code>-1</code> if line number
	 * information is unavailable.
	 *
	 * @param stratum the stratum to use.
	 * @return line number of instruction pointer in this stack frame, or 
	 * <code>-1</code> if line number information is unavailable
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 * 
	 * @since 3.0
	 */
	public int getLineNumber(String stratum) throws DebugException;
	
	/**
	 * Returns the source name debug attribute associated with the declaring
	 * type of this stack frame, or <code>null</code> if the source name debug
	 * attribute not present.
	 * 
	 * @return source name debug attribute, or <code>null</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public String getSourceName() throws DebugException;
	
	/**
	 * Returns the source name debug attribute associated with the declaring
	 * type of this stack frame in the specified stratum, or <code>null</code>
	 * if the source name debug attribute not present.
	 * 
	 * @param stratum the stratum to use.
	 * @return source name debug attribute, or <code>null</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 * 
	 * @since 3.0
	 */
	public String getSourceName(String stratum) throws DebugException;
	
	/**
	 * Returns the source path debug attribute associated with
	 * this stack frame in the specified stratum, or 
	 * <code>null</code> if the source path is not known.
	 * 
	 * @param stratum the stratum to use.
	 * @return source path debug attribute, or <code>null</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 * @since 3.0
	 */
	public String getSourcePath(String stratum) throws DebugException;
	
	/**
	 * Returns a collection of local variables that are visible
	 * at the current point of execution in this stack frame. The
	 * list includes arguments.
	 * 
	 * @return collection of locals and arguments
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 * @since 2.0
	 */
	public IJavaVariable[] getLocalVariables() throws DebugException;
	
	/**
	 * Returns a reference to the receiver of the method associated
	 * with this stack frame, or <code>null</code> if this stack frame
	 * represents a static method.
	 * 
	 * @return 'this' object, or <code>null</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 */
	public IJavaObject getThis() throws DebugException;
	
	/**
	 * Returns the type in which this stack frame's method is
	 * declared.
	 * 
	 * @return the type in which this stack frame's method is
	 *   declared
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This stack frame is no longer valid. That is, the thread
	 *   containing this stack frame has since been resumed.</li>
	 * </ul>
	 * @since 2.0
	 */
	public IJavaClassType getDeclaringType() throws DebugException;	
	
	/**
	 * Returns whether local variable information was available
	 * when local variables were retrieved from the target for this
	 * frame. Returns <code>true</code> if locals have never been
	 * retrieved. This data is available after the fact, since variable
	 * retrieval is expensive.
	 * 
	 * @return whether local variable information was available
	 * when variables were retrieved from the target. Returns
	 * <code>true</code> if locals have never been retrieved
	 * 
	 * @since 2.0
	 */
	public boolean wereLocalsAvailable();
}


