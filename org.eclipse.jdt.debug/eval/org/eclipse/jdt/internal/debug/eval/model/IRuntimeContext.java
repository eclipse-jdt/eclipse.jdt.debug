package org.eclipse.jdt.internal.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
 
/**
 * The context in which an evaluation is to be performed. An
 * evaluation is performed in the context of an object or class.
 * The evaluation may be in the context of a method, in which case
 * there could be local variables.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */

public interface IRuntimeContext {
	
	/**
	 * Returns the virtual machine in which to perform the
	 * evaluation.
	 * 
	 * @return virtual machine
	 */
	IVirtualMachine getVM();
	
	/**
	 * Returns the receiving object context in which to perform
	 * the evaluation - equivalent to 'this'. Returns <code>null</code>
	 * if the context of an evaluation is in a class rather than
	 * an object.
	 * 
	 * @return 'this', or <code>null</code>
	 * @exception EvaluationException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	IObject getThis() throws CoreException;
	
	/**
	 * Returns the receiving type context in which to perform 
	 * the evaluation. The type of 'this', or in the case of a 
	 * static context, the class in which the evaluation is being
	 * performed.
	 * 
	 * @return receiving class
	 * @exception EvaluationException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	IClassType getReceivingType() throws CoreException;
	
	/**
	 * Returns the local variables visible for the evaluation.
	 * This includes method arguments, if any. Does not return
	 * <code>null</code> returns an empty collection if there
	 * are no locals.
	 * 
	 * @return local variables
	 * @exception EvaluationException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	IVariable[] getLocals() throws CoreException;

	/**
	 * Returns the Java project context in which this expression
	 * should be compiled.
	 * 
	 * @return project
	 */
	IJavaProject getProject();
	
	/**
	 * Returns the thread in which message sends may be performed.
	 * 
	 * @return thread
	 */
	IThread getThread();
	
	/**
	 * Returns whether the context of this evaluation is within
	 * a constructor.
	 * 
	 * @return whether the context of this evaluation is within
	 * a constructor
	 * @exception EvaluationException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	public boolean isConstructor() throws CoreException;
}

