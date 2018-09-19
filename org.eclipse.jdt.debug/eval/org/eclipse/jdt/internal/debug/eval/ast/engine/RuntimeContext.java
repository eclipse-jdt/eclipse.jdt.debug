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
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;

public class RuntimeContext extends AbstractRuntimeContext {

	/**
	 * Stack frame context
	 */
	private IJavaStackFrame fFrame;

	/**
	 * Creates a runtime context for the given java project and stack frame.
	 *
	 * @param project
	 *            Java project context used to compile expressions in
	 * @param frame
	 *            stack frame used to define locals and receiving type context
	 * @return a new runtime context
	 */
	public RuntimeContext(IJavaProject project, IJavaStackFrame frame) {
		super(project);
		setFrame(frame);
	}

	/**
	 * @see IRuntimeContext#getVM()
	 */
	@Override
	public IJavaDebugTarget getVM() {
		return (IJavaDebugTarget) getFrame().getDebugTarget();
	}

	/**
	 * @see IRuntimeContext#getThis()
	 */
	@Override
	public IJavaObject getThis() throws CoreException {
		return getFrame().getThis();
	}

	/**
	 * @see IRuntimeContext#getReceivingType()
	 */
	@Override
	public IJavaReferenceType getReceivingType() throws CoreException {
		IJavaObject rec = getThis();
		if (rec != null) {
			return (IJavaReferenceType) rec.getJavaType();
		}
		return getFrame().getReferenceType();
	}

	/**
	 * @see IRuntimeContext#getLocals()
	 */
	@Override
	public IJavaVariable[] getLocals() throws CoreException {
		return getFrame().getLocalVariables();
	}

	/**
	 * Sets the stack frame context used to compile/run expressions
	 *
	 * @param frame
	 *            the stack frame context used to compile/run expressions
	 */
	protected IJavaStackFrame getFrame() {
		return fFrame;
	}

	/**
	 * Sets the stack frame context used to compile/run expressions
	 *
	 * @param frame
	 *            the stack frame context used to compile/run expressions
	 */
	private void setFrame(IJavaStackFrame frame) {
		fFrame = frame;
	}

	/**
	 * @see IRuntimeContext#getThread()
	 */
	@Override
	public IJavaThread getThread() {
		return (IJavaThread) getFrame().getThread();
	}

	/**
	 * @see IRuntimeContext#isConstructor()
	 */
	@Override
	public boolean isConstructor() throws CoreException {
		return getFrame().isConstructor();
	}

}
