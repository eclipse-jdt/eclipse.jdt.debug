package org.eclipse.jdt.internal.debug.eval.ast.engine;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public class RuntimeContext implements IRuntimeContext {

	/**
	 * Java project context
	 */
	private IJavaProject fProject;
	
	/**
	 * Stack frame context
	 */
	private IJavaStackFrame fFrame;

	/**
	 * Creates a runtime context for the given java project and 
	 * stack frame.
	 * 
	 * @param project Java project context used to compile expressions in
	 * @param frame stack frame used to define locals and receiving type
	 *  context
	 * @return a new runtime context
	 */
	public RuntimeContext(IJavaProject project, IJavaStackFrame frame) {
		setProject(project);
		setFrame(frame);
	}
	
	/**
	 * @see IRuntimeContext#getVM()
	 */
	public IJavaDebugTarget getVM() {
		return (IJavaDebugTarget)getFrame().getDebugTarget();
	}

	/**
	 * @see IRuntimeContext#getThis()
	 */
	public IJavaObject getThis() throws CoreException {
		return getFrame().getThis();
	}

	/**
	 * @see IRuntimeContext#getReceivingType()
	 */
	public IJavaClassType getReceivingType() throws CoreException {
		IJavaObject rec = getThis();
		if (rec != null) {
			return (IJavaClassType)rec.getJavaType();
		}
		return getFrame().getDeclaringType();
	}

	/**
	 * @see IRuntimeContext#getLocals()
	 */
	public IJavaVariable[] getLocals() throws CoreException {
		return getFrame().getLocalVariables();
	}

	/**
	 * @see IRuntimeContext#getProject()
	 */
	public IJavaProject getProject() {
		return fProject;
	}
	
	/**
	 * Sets the project context used to compile expressions
	 * 
	 * @param project the project context used to compile expressions
	 */
	private void setProject(IJavaProject project) {
		fProject = project;
	}
	
	/**
	 * Sets the stack frame context used to compile/run expressions
	 * 
	 * @param frame the stack frame context used to compile/run expressions
	 */
	protected IJavaStackFrame getFrame() {
		return fFrame;
	}	
		
	/**
	 * Sets the stack frame context used to compile/run expressions
	 * 
	 * @param frame the stack frame context used to compile/run expressions
	 */
	private void setFrame(IJavaStackFrame frame) {
		fFrame = frame;
	}	

	/**
	 * @see IRuntimeContext#getThread()
	 */
	public IJavaThread getThread() {
		return (IJavaThread)getFrame().getThread();
	}

	/**
	 * @see IRuntimeContext#isConstructor()
	 */
	public boolean isConstructor() throws CoreException {
		return getFrame().isConstructor();
	}

}

