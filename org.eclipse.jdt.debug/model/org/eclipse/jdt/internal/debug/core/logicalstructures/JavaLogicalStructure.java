/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;

public class JavaLogicalStructure {


	// stack frame context provider
	public static final int INFO_EVALUATION_STACK_FRAME = 111;
	private static IStatus fgNeedStackFrame = new Status(IStatus.INFO, JDIDebugPlugin.getUniqueIdentifier(), INFO_EVALUATION_STACK_FRAME, "Provides thread context for an evaluation", null); //$NON-NLS-1$
	private static IStatusHandler fgStackFrameProvider;

	/**
	 * Fully qualified type name.
	 */
	private final String fType;
	/**
	 * Indicate if this java logical structure should be used on object
	 * instance of subtype of the specified type.
	 */
	private final boolean fSubtypes;
	/**
	 * Code snippet to evaluate to create the logical value.
	 */
	private final String fValue;
	/**
	 * Name and associated code snippet of the variables of the logical value.
	 */
	private final String[][] fVariables;
	
	/**
	 * Performs the evaluations.
	 */
	static class EvaluationBlock implements IEvaluationListener {
		
		private IJavaObject fEvaluationValue;
		private IJavaReferenceType fEvaluationType;
		private IJavaThread fThread;
		private IAstEvaluationEngine fEvaluationEngine;
		private IEvaluationResult fResult;

		public EvaluationBlock(IJavaObject value, IJavaReferenceType type, IJavaThread thread, IAstEvaluationEngine evaluationEngine) {
			fEvaluationValue= value;
			fEvaluationType= type;
			fThread= thread;
			fEvaluationEngine= evaluationEngine;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.debug.eval.IEvaluationListener#evaluationComplete(org.eclipse.jdt.debug.eval.IEvaluationResult)
		 */
		public void evaluationComplete(IEvaluationResult result) {
			synchronized(this) {
				fResult= result;
				this.notify();
			}
		}

		public IJavaValue evaluate(String snippet) throws DebugException {
			ICompiledExpression compiledExpression= fEvaluationEngine.getCompiledExpression(snippet, fEvaluationType);
			if (compiledExpression.hasErrors()) {
				String[] errorMessages = compiledExpression.getErrorMessages();
				for (int i = 0; i < errorMessages.length; i++) {
					JDIDebugPlugin.logDebugMessage(errorMessages[i]);
				}
				return null;
			}
			fResult= null;
			fEvaluationEngine.evaluateExpression(compiledExpression, fEvaluationValue, fThread, this, DebugEvent.EVALUATION_IMPLICIT, false);
			synchronized(this) {
				if (fResult == null) {
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			if (fResult == null) {
				return null;
			}
			if (fResult.hasErrors()) {
				DebugException exception = fResult.getException();
				if (exception != null) {
					JDIDebugPlugin.log(exception);
				}
				return null;
			}
			return fResult.getValue();
		}
	}

	public JavaLogicalStructure(String type, boolean subtypes, String value, String[][] variables) {
		fType= type;
		fSubtypes= subtypes;
		fValue= value;
		fVariables= variables;
	}
	
	/**
	 * @see org.eclipse.debug.core.model.ILogicalStructureTypeDelegate#providesLogicalStructure(IValue)
	 */
	public boolean providesLogicalStructure(IJavaObject value) {
		return getType(value) != null;
	}

	/**
	 * @see org.eclipse.debug.core.model.ILogicalStructureTypeDelegate#getLogicalStructure(IValue)
	 */
	public IJavaValue getLogicalStructure(IJavaObject value) {
		try {
			IJavaReferenceType type = getType(value);
			if (type == null) {
				return null;
			}
			IJavaStackFrame stackFrame= getStackFrame(value);
			if (stackFrame == null) {
				return null;
			}
			
			// find the project the snippets will be compiled in.
			ISourceLocator locator= value.getLaunch().getSourceLocator();
			Object sourceElement= null;
			if (locator instanceof ISourceLookupDirector) {
				if (type instanceof JDIReferenceType) {
					String[] sourcePaths= ((JDIReferenceType) type).getSourcePaths(null);
					if (sourcePaths.length > 0) {
						sourceElement= ((ISourceLookupDirector) locator).getSourceElement(sourcePaths[0]);
					}
				}
			}
			if (sourceElement == null) {
				sourceElement = locator.getSourceElement(stackFrame);
			}
			if (!(sourceElement instanceof IJavaElement) && sourceElement instanceof IAdaptable) {
				sourceElement = ((IAdaptable)sourceElement).getAdapter(IJavaElement.class);
			}
			IJavaProject project= null;
			if (sourceElement instanceof IJavaElement) {
				project= ((IJavaElement) sourceElement).getJavaProject();
			} else if (sourceElement instanceof IResource) {
				IJavaProject resourceProject = JavaCore.create(((IResource)sourceElement).getProject());
				if (resourceProject.exists()) {
					project= resourceProject;
				}
			}
			if (project == null) {
				return null;
			}
			
			IAstEvaluationEngine evaluationEngine= JDIDebugPlugin.getDefault().getEvaluationEngine(project, (IJavaDebugTarget)stackFrame.getDebugTarget());
			
			EvaluationBlock evaluationBlock= new EvaluationBlock(value, type, (IJavaThread)stackFrame.getThread(), evaluationEngine);
			if (fValue == null) {
				// evaluate each variable
				IJavaVariable[] variables= new IJavaVariable[fVariables.length];
				for (int i= 0; i < fVariables.length; i++) {
					variables[i]= new JDIPlaceholderVariable(fVariables[i][0], evaluationBlock.evaluate(fVariables[i][1]));
				}
				return new LogicalObjectStructureValue(value, variables);
			}
			// evaluate the logical value
			return evaluationBlock.evaluate(fValue);

		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
		}
		return null;
	}
	
	private IJavaReferenceType getType(IJavaObject value) {
		try {
			IJavaType type= value.getJavaType();
			if (!(type instanceof IJavaClassType)) {
				return null;
			}
			IJavaClassType classType= (IJavaClassType) type;
			if (classType.getName().equals(fType)) {
				// found the type
				return classType;
			}
			if (!fSubtypes) {
				// if not checking the subtypes, stop here
				return null;
			}
			IJavaClassType superClass= classType.getSuperclass();
			while (superClass != null) {
				if (superClass.getName().equals(fType)) {
					// found the type, it's a super class
					return superClass;
				}
				superClass= superClass.getSuperclass();
			}
			IJavaInterfaceType[] superInterfaces= classType.getAllInterfaces();
			for (int i= 0; i < superInterfaces.length; i++) {
				if (superInterfaces[i].getName().equals(fType)) {
					// found the type, it's a super interface
					return superInterfaces[i];
				}
			}
		} catch (DebugException e) {
			JDIDebugPlugin.log(e);
			return null;
		}
		return null;
	}

	/**
	 * Return the current stack frame context, or a valid stack frame for the given value.
	 */
	private IJavaStackFrame getStackFrame(IValue value) throws CoreException {
		IStatusHandler handler = getStackFrameProvider();
		if (handler != null) {
			IJavaStackFrame stackFrame = (IJavaStackFrame)handler.handleStatus(fgNeedStackFrame, value);
			if (stackFrame != null) {
				return stackFrame;
			}
		}
		IDebugTarget target = value.getDebugTarget();
		IJavaDebugTarget javaTarget = (IJavaDebugTarget) target.getAdapter(IJavaDebugTarget.class);
		if (javaTarget != null) {
			IThread[] threads = javaTarget.getThreads();
			for (int i = 0; i < threads.length; i++) {
				IThread thread = threads[i];
				if (thread.isSuspended()) {
					return (IJavaStackFrame)thread.getTopStackFrame();
				}
			}
		}
		return null;
	}
	
	private static IStatusHandler getStackFrameProvider() {
		if (fgStackFrameProvider == null) {
			fgStackFrameProvider = DebugPlugin.getDefault().getStatusHandler(fgNeedStackFrame);
		}
		return fgStackFrameProvider;
	}

}
