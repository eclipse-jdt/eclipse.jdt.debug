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
package org.eclipse.jdt.internal.debug.eval.ast.engine;


import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.eval.EvaluationResult;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionSequence;

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

	/**
	 * @see IEvaluationEngine#evaluate(String, IJavaStackFrame, IEvaluationListener, int, boolean)
	 */
	public void evaluate(String snippet, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		ICompiledExpression expression= getCompiledExpression(snippet, frame);
		evaluateExpression(expression, frame, listener, evaluationDetail, hitBreakpoints);
	}
	
	/**
	 * @see IEvaluationEngine#evaluate(String, IJavaObject, IJavaThread, IEvaluationListener, int, boolean)
	 */
	public void evaluate(String snippet, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		ICompiledExpression expression= getCompiledExpression(snippet, thisContext);
		evaluateExpression(expression, thisContext, thread, listener, evaluationDetail, hitBreakpoints);
	}
	
	/**
	 * @see IAstEvaluationEngine#evaluateExpression(ICompiledExpression, IJavaStackFrame, IEvaluationListener, int, boolean)
	 */
	public void evaluateExpression(ICompiledExpression expression, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) throws DebugException {
		RuntimeContext context = new RuntimeContext(getJavaProject(), frame);
		doEvaluation(expression, context, (IJavaThread)frame.getThread(), listener, evaluationDetail, hitBreakpoints);
	}

	/**
	 * @see IAstEvaluationEngine#evaluateExpression(ICompiledExpression, IJavaObject, IJavaThread, IEvaluationListener, int, boolean)
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
				result.addError(EvaluationEngineMessages.getString("ASTEvaluationEngine.Cannot_perform_nested_evaluations")); //$NON-NLS-1$
				listener.evaluationComplete(result);
				return;
			}
			thread.queueRunnable(new EvalRunnable((InstructionSequence)expression, thread, context, listener, evaluationDetail, hitBreakpoints));
		} else {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK, EvaluationEngineMessages.getString("ASTEvaluationEngine.AST_evaluation_engine_cannot_evaluate_expression"), null)); //$NON-NLS-1$
		}
	}

	/**
	 * @see IEvaluationEngine#getCompiledExpression(String, IJavaStackFrame)
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
				if (!isLocalType(localsVar[i].getReferenceTypeName())) {
					locals[numLocals++]= localsVar[i];
				}
			}
			// to solve and remove
			// ******
			String[] localTypesNames= new String[numLocals];
			String[] localVariables= new String[numLocals];
			for (int i = 0; i < numLocals; i++) {
				localVariables[i] = locals[i].getName();
				localTypesNames[i] = locals[i].getReferenceTypeName();
			}
			mapper = new EvaluationSourceGenerator(localTypesNames, localVariables, snippet);
			unit = AST.parseCompilationUnit(mapper.getSource(frame).toCharArray(), mapper.getCompilationUnitName(), javaProject);
		} catch (CoreException e) {
			InstructionSequence expression= new InstructionSequence(snippet);
			expression.addError(e.getStatus().getMessage());
			return expression;
		}
		
		return createExpressionFromAST(snippet, mapper, unit);
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
	

	/**
	 * @see IEvaluationEngine#getCompiledExpression(String, IJavaObject, IJavaThread)
	 */
	public ICompiledExpression getCompiledExpression(String snippet, IJavaObject thisContext) {
		if (thisContext instanceof IJavaArray) {
			InstructionSequence errorExpression= new InstructionSequence(snippet);
			errorExpression.addError(EvaluationEngineMessages.getString("ASTEvaluationEngine.Cannot_perform_an_evaluation_in_the_context_of_an_array_instance_1")); //$NON-NLS-1$
		}
		IJavaProject javaProject = getJavaProject();

		EvaluationSourceGenerator mapper = null;
		CompilationUnit unit = null;

		mapper = new EvaluationSourceGenerator(new String[0], new String[0], snippet);

		try {
			unit = AST.parseCompilationUnit(mapper.getSource(thisContext, javaProject).toCharArray(), mapper.getCompilationUnitName(), javaProject);
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
				if (problem.isError() && problem.getID() != IProblem.VoidMethodReturnsValue) {
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
					errorSequence.addError(EvaluationEngineMessages.getString("ASTEvaluationEngine.Evaluations_must_contain_either_an_expression_or_a_block_of_well-formed_statements_1")); //$NON-NLS-1$
				}
				return errorSequence;
			}
		}
		
		ASTInstructionCompiler visitor = new ASTInstructionCompiler(mapper.getSnippetStart(), snippet);
		unit.accept(visitor);

		return visitor.getInstructions();
	}

	/**
	 * @see IEvaluationEngine#getJavaProject()
	 */
	public IJavaProject getJavaProject() {
		return fProject;
	}

	/**
	 * @see IEvaluationEngine#getDebugTarget()
	 */
	public IJavaDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/**
	 * @see IEvaluationEngine#dispose()
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
			final Interpreter interpreter = new Interpreter((InstructionSequence) fExpression, fContext);
		
			class EvaluationRunnable implements IEvaluationRunnable, ITerminate {
				
				CoreException fException;
				
				public void run(IJavaThread jt, IProgressMonitor pm) {
					try {
						interpreter.execute();
					} catch (CoreException exception) {
						fException = exception;
					} catch (Throwable exception) {
						JDIDebugPlugin.log(exception);
						fException = new CoreException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IStatus.ERROR, EvaluationEngineMessages.getString("ASTEvaluationEngine.Runtime_exception_occurred_during_evaluation._See_log_for_details"), exception)); //$NON-NLS-1$
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

			if (value != null) {
				result.setValue(value);
			} else {
				result.addError(EvaluationEngineMessages.getString("ASTEvaluationEngine.An_unknown_error_occurred_during_evaluation")); //$NON-NLS-1$
			}
			if (exception != null) {
				if (exception instanceof DebugException) {
					result.setException((DebugException)exception);
				} else {
					result.setException(new DebugException(exception.getStatus()));
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
