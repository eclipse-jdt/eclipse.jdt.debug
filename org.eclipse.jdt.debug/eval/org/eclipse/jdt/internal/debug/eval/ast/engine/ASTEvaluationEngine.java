/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;


import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.jdt.internal.debug.eval.EvaluationResult;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionSequence;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

public class ASTEvaluationEngine implements IAstEvaluationEngine {
	
	private IJavaProject fProject;
	
	private IJavaDebugTarget fDebugTarget;
	
	public ASTEvaluationEngine(IJavaProject project, IJavaDebugTarget debugTarget) {
		setJavaProject(project);
		setDebugTarget(debugTarget);
	}
	
	public void setJavaProject(IJavaProject project) {
		fProject = project;
	}

	public void setDebugTarget(IJavaDebugTarget debugTarget) {
		fDebugTarget = debugTarget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#evaluate(java.lang.String, org.eclipse.jdt.debug.core.IJavaStackFrame, org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
	public void evaluate(String snippet, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		ICompiledExpression expression= getCompiledExpression(snippet, frame);
		evaluateExpression(expression, frame, listener, evaluationDetail, hitBreakpoints);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#evaluate(java.lang.String, org.eclipse.jdt.debug.core.IJavaObject, org.eclipse.jdt.debug.core.IJavaThread, org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
	public void evaluate(String snippet, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		ICompiledExpression expression= getCompiledExpression(snippet, thisContext);
		evaluateExpression(expression, thisContext, thread, listener, evaluationDetail, hitBreakpoints);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine#evaluateExpression(org.eclipse.jdt.debug.eval.ICompiledExpression, org.eclipse.jdt.debug.core.IJavaStackFrame, org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
	public void evaluateExpression(ICompiledExpression expression, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		RuntimeContext context = new RuntimeContext(getJavaProject(), frame);
		doEvaluation(expression, context, (IJavaThread)frame.getThread(), listener, evaluationDetail, hitBreakpoints);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine#evaluateExpression(org.eclipse.jdt.debug.eval.ICompiledExpression, org.eclipse.jdt.debug.core.IJavaObject, org.eclipse.jdt.debug.core.IJavaThread, org.eclipse.jdt.debug.eval.IEvaluationListener, int, boolean)
	 */
	public void evaluateExpression(ICompiledExpression expression, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		IRuntimeContext context = new JavaObjectRuntimeContext(thisContext, getJavaProject(), thread);
		doEvaluation(expression, context, thread, listener, evaluationDetail, hitBreakpoints);
	}
	
	/**
	 * Evaluates the given expression in the given thread and the given runtime context.
	 */
	private void doEvaluation(ICompiledExpression expression, IRuntimeContext context, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {		
		if (expression instanceof InstructionSequence) {
			// don't queue explicite evaluation if the thread is allready performing an evaluation.
			if (thread.isSuspended() && ((JDIThread)thread).isInvokingMethod() || thread.isPerformingEvaluation() && evaluationDetail == DebugEvent.EVALUATION) {
				EvaluationResult result= new EvaluationResult(this, expression.getSnippet(), thread);
				result.addError(EvaluationEngineMessages.ASTEvaluationEngine_Cannot_perform_nested_evaluations); //$NON-NLS-1$
				listener.evaluationComplete(result);
				return;
			}
			thread.queueRunnable(new EvalRunnable((InstructionSequence)expression, thread, context, listener, evaluationDetail, hitBreakpoints));
		} else {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK, EvaluationEngineMessages.ASTEvaluationEngine_AST_evaluation_engine_cannot_evaluate_expression, null)); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine#getCompiledExpression(java.lang.String, org.eclipse.jdt.debug.core.IJavaStackFrame)
	 */
	public ICompiledExpression getCompiledExpression(String snippet, IJavaStackFrame frame) {
		IJavaProject javaProject = getJavaProject();
		RuntimeContext context = new RuntimeContext(javaProject, frame);

		EvaluationSourceGenerator mapper = null;
		CompilationUnit unit = null;
		try {
			IJavaVariable[] localsVar = context.getLocals();
			int numLocalsVar= localsVar.length;
			// ******
			// to hide problems with local variable declare as instance of Local Types
			IJavaVariable[] locals= new IJavaVariable[numLocalsVar];
			int numLocals= 0;
			for (int i = 0; i < numLocalsVar; i++) {
				try {
					if (!isLocalType(localsVar[i].getReferenceTypeName())) {
						locals[numLocals++]= localsVar[i];
					}
				} catch (DebugException e) {
					// do not throw ClassNotLoadedException
					// nothing we can do, just ignore this local variable
					if (!(e.getStatus().getException() instanceof ClassNotLoadedException)) {
						throw e;
					}
				}
			}
			// to solve and remove
			// ******
			String[] localTypesNames= new String[numLocals];
			String[] localVariables= new String[numLocals];
			for (int i = 0; i < numLocals; i++) {
				localVariables[i] = locals[i].getName();
				localTypesNames[i] = Signature.toString(locals[i].getGenericSignature()).replace('/', '.');
			}
			mapper = new EvaluationSourceGenerator(localTypesNames, localVariables, snippet);
			unit = parseCompilationUnit(mapper.getSource(frame, javaProject).toCharArray(), mapper.getCompilationUnitName(), javaProject);
		} catch (CoreException e) {
			InstructionSequence expression= new InstructionSequence(snippet);
			expression.addError(e.getStatus().getMessage());
			return expression;
		}
		
		return createExpressionFromAST(snippet, mapper, unit);
	}
	
	private CompilationUnit parseCompilationUnit(char[] source, String unitName, IJavaProject project) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		String compilerCompliance= project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		parser.setSource(source);
		parser.setUnitName(unitName);
		parser.setProject(project);
		parser.setResolveBindings(true);
		Map options=JavaCore.getDefaultOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, compilerCompliance);
		options.put(JavaCore.COMPILER_SOURCE, project.getOption(JavaCore.COMPILER_SOURCE, true));
		parser.setCompilerOptions(options);
		return (CompilationUnit) parser.createAST(null);
	}

	// ******
	// to hide problems with local variable declare as instance of Local Types
	private boolean isLocalType(String typeName) {
		StringTokenizer strTok= new StringTokenizer(typeName,"$"); //$NON-NLS-1$
		strTok.nextToken();
		while (strTok.hasMoreTokens()) {
			char char0= strTok.nextToken().charAt(0);
			if ('0' <= char0 && char0 <= '9') {
				return true;
			}
		}
		return false;
	}
	// ******
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine#getCompiledExpression(java.lang.String, org.eclipse.jdt.debug.core.IJavaObject)
	 */
	public ICompiledExpression getCompiledExpression(String snippet, IJavaObject thisContext) {
		try {
			return getCompiledExpression(snippet, (IJavaReferenceType)thisContext.getJavaType());
		} catch (DebugException e) {
			InstructionSequence expression= new InstructionSequence(snippet);
			expression.addError(e.getStatus().getMessage());
			return expression;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine#getCompiledExpression(java.lang.String, org.eclipse.jdt.debug.core.IJavaType)
	 */
	public ICompiledExpression getCompiledExpression(String snippet, IJavaReferenceType type) {
		if (type instanceof IJavaArrayType) {
			InstructionSequence errorExpression= new InstructionSequence(snippet);
			errorExpression.addError(EvaluationEngineMessages.ASTEvaluationEngine_Cannot_perform_an_evaluation_in_the_context_of_an_array_instance_1); //$NON-NLS-1$
		}
		IJavaProject javaProject = getJavaProject();

		EvaluationSourceGenerator mapper = null;
		CompilationUnit unit = null;

		mapper = new EvaluationSourceGenerator(new String[0], new String[0], snippet);

		try {
			unit = parseCompilationUnit(mapper.getSource(type, javaProject).toCharArray(), mapper.getCompilationUnitName(), javaProject);
		} catch (CoreException e) {
			InstructionSequence expression= new InstructionSequence(snippet);
			expression.addError(e.getStatus().getMessage());
			return expression;
		}
		return createExpressionFromAST(snippet, mapper, unit);
	}

	/**
	 * Creates a compiled expression for the given snippet using the given mapper and 
	 * compiliation unit (AST).
	 * @param snippet the code snippet to be compiled
	 * @param mapper the object which will be used to create the expression
	 * @param unit the compilation unit (AST) generated for the snippet
	 */
	private ICompiledExpression createExpressionFromAST(String snippet, EvaluationSourceGenerator mapper, CompilationUnit unit) {
		IProblem[] problems= unit.getProblems();
		if (problems.length != 0) {
			boolean snippetError= false;
			boolean runMethodError= false;
			InstructionSequence errorSequence= new InstructionSequence(snippet);
			int codeSnippetStart= mapper.getSnippetStart();
			int codeSnippetEnd= codeSnippetStart + mapper.getSnippet().length();
			int runMethodStart= mapper.getRunMethodStart();
			int runMethodEnd= runMethodStart + mapper.getRunMethodLength();
			for (int i = 0; i < problems.length; i++) {
				IProblem problem= problems[i];
				int errorOffset= problem.getSourceStart();
				int problemId= problem.getID();
				if (problemId == IProblem.IsClassPathCorrect) {
					errorSequence.addError(problem.getMessage());
					snippetError = true;
				}
				if (problemId == IProblem.VoidMethodReturnsValue
					|| problemId == IProblem.NotVisibleMethod
					|| problemId == IProblem.NotVisibleConstructor
					|| problemId == IProblem.NotVisibleField
					|| problemId == IProblem.NotVisibleType) {
					continue;
				}
				if (problem.isError()) {
					if (codeSnippetStart <= errorOffset && errorOffset <= codeSnippetEnd) {
						errorSequence.addError(problem.getMessage());
						snippetError = true;
					} else if (runMethodStart <= errorOffset && errorOffset <= runMethodEnd) {
						runMethodError = true;
					}
				}
			}
			if (snippetError || runMethodError) {
				if (runMethodError) {
					errorSequence.addError(EvaluationEngineMessages.ASTEvaluationEngine_Evaluations_must_contain_either_an_expression_or_a_block_of_well_formed_statements_1); //$NON-NLS-1$
				}
				return errorSequence;
			}
		}
		
		ASTInstructionCompiler visitor = new ASTInstructionCompiler(mapper.getSnippetStart(), snippet);
		unit.accept(visitor);

		return visitor.getInstructions();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#getJavaProject()
	 */
	public IJavaProject getJavaProject() {
		return fProject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#getDebugTarget()
	 */
	public IJavaDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine#dispose()
	 */
	public void dispose() {
	}
	
	class EvalRunnable implements Runnable {
		
		private InstructionSequence fExpression;
		
		private IJavaThread fThread;
		
		private int fEvaluationDetail;
		
		private boolean fHitBreakpoints;
		
		private IRuntimeContext fContext;
		
		private IEvaluationListener fListener;
		
		public EvalRunnable(InstructionSequence expression, IJavaThread thread, IRuntimeContext context, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) {
			fExpression= expression;
			fThread= thread;
			fContext= context;
			fListener= listener;
			fEvaluationDetail= evaluationDetail;
			fHitBreakpoints= hitBreakpoints;
		}

		public void run() {
			EvaluationResult result = new EvaluationResult(ASTEvaluationEngine.this, fExpression.getSnippet(), fThread);
			if (fExpression.hasErrors()) {
				String[] errors = fExpression.getErrorMessages();
				for (int i = 0, numErrors = errors.length; i < numErrors; i++) {
					result.addError(errors[i]);
				}
				evaluationFinished(result);
				return;
			}
			final Interpreter interpreter = new Interpreter(fExpression, fContext);
		
			class EvaluationRunnable implements IEvaluationRunnable, ITerminate {
				
				CoreException fException;
				
				public void run(IJavaThread jt, IProgressMonitor pm) {
					try {
						interpreter.execute();
					} catch (CoreException exception) {
						fException = exception;
						if (fEvaluationDetail == DebugEvent.EVALUATION && exception.getStatus().getException() instanceof InvocationException) {
							// print the stack trace for the exception if an *explicit* evaluation 
							InvocationException invocationException = (InvocationException)exception.getStatus().getException();
							ObjectReference exObject = invocationException.exception();
							IJavaObject modelObject = (IJavaObject)JDIValue.createValue((JDIDebugTarget)getDebugTarget(), exObject);
							try {
								modelObject.sendMessage("printStackTrace", "()V", null, jt, false); //$NON-NLS-1$ //$NON-NLS-2$
							} catch (DebugException e) {
								// unable to print stack trace
							}
						}
					}
				}
				public void terminate() {
					interpreter.stop();
				}
				public boolean canTerminate() {
					return true;
				}
				public boolean isTerminated() {
					return false;
				}
				
				public CoreException getException() {
					return fException;
				}
			}

			EvaluationRunnable er = new EvaluationRunnable();
			CoreException exception = null;
			try {
				fThread.runEvaluation(er, null, fEvaluationDetail, fHitBreakpoints);
			} catch (DebugException e) {
				exception = e;
			}
		
            IJavaValue value = interpreter.getResult();

			if (exception == null) {
				exception = er.getException();
			}
            
			if (exception != null) {
			    if (exception instanceof DebugException) {
			        result.setException((DebugException)exception);
			    } else {
			        result.setException(new DebugException(exception.getStatus()));
			    }
			} else {   
			    if (value != null) {
			        result.setValue(value);
			    } else {
			        result.addError(EvaluationEngineMessages.ASTEvaluationEngine_An_unknown_error_occurred_during_evaluation); //$NON-NLS-1$
			    }
			}
            
			
			evaluationFinished(result);
		}
		private void evaluationFinished(IEvaluationResult result) {
			// only notify if plugin not yet shutdown - bug# 8693
			if(JDIDebugPlugin.getDefault() != null) {
				fListener.evaluationComplete(result);
			}
		}
		
	}
}
