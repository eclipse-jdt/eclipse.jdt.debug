package org.eclipse.jdt.internal.debug.eval.ast.engine;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionSequence;

public class ASTEvaluationEngine implements IAstEvaluationEngine {
	
	/**
	 * Code snippets that return a value generate an exception from
	 * the compiler because void methods cannot return a value. Since the
	 * messages returned by the compilation unit only contain a string and
	 * a position, we have to check if the message's string matches the
	 * message for the "void methods cannot return a value" error.
	 */
	private static String fVoidMessageError= new DefaultProblemFactory(Locale.getDefault()).getLocalizedMessage(105, new String[0]);

	private IJavaProject fProject;
	
	private IJavaDebugTarget fDebugTarget;
	
	private List fEvaluationThreads= new ArrayList();

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
		getEvaluationThread().evaluate(expression, context, thread, listener, evaluationDetail, hitBreakpoints);
	}
	
	/**
	 * Returns an evaluation thread which can be used to perform an evaluation.
	 * This method will return an existing thread if a one exists that is 
	 * currently not performing an evaluation. Otherwise, a new thread will 
	 * be created.
	 */
	private EvaluationThread getEvaluationThread() {
		Iterator iter= fEvaluationThreads.iterator();
		EvaluationThread thread= null;
		while (iter.hasNext()) {
			thread= (EvaluationThread)iter.next();
			if (!thread.isEvaluating()) {
				return thread;
			}
		}
		thread= new EvaluationThread(this);
		fEvaluationThreads.add(thread);
		return thread;
	}
	
	/**
	 * Notifies this evaluation engine that the given evaluation thread
	 * has completed an evaluation. If there are any threads available
	 * (not currently evaluating), the given thread is stopped. Otherwise
	 * the thread is allowed to keep running - it will be reused for the
	 * next evaluation.
	 */
	protected void evaluationThreadFinished(EvaluationThread thread) {
		if (fEvaluationThreads.size() == 1) {
			// Always leave at least one thread running
			return;
		}
		boolean allBusy= true;
		Iterator iter= fEvaluationThreads.iterator();
		EvaluationThread tempThread= null;
		while (iter.hasNext()) {
			tempThread= (EvaluationThread)iter.next();
			if (!tempThread.isEvaluating()) {
				// Another thread is available. The given thread
				// can be stopped
				allBusy= false;
				break;
			}
		}
		if (!allBusy) {
			thread.stop();
			fEvaluationThreads.remove(thread);
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
			expression.addError(new Message(e.getStatus().getMessage(), 1));
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
			errorExpression.addError(new Message(EvaluationEngineMessages.getString("ASTEvaluationEngine.Cannot_perform_an_evaluation_in_the_context_of_an_array_instance_1"), 0)); //$NON-NLS-1$
		}
		IJavaProject javaProject = getJavaProject();

		EvaluationSourceGenerator mapper = null;
		CompilationUnit unit = null;

		mapper = new EvaluationSourceGenerator(new String[0], new String[0], snippet);

		try {
			unit = AST.parseCompilationUnit(mapper.getSource(thisContext, javaProject).toCharArray(), mapper.getCompilationUnitName(), javaProject);
		} catch (CoreException e) {
			InstructionSequence expression= new InstructionSequence(snippet);
			expression.addError(new Message(e.getStatus().getMessage(), 1));
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
		Message[] messages= unit.getMessages();
		if (messages.length != 0) {
			boolean snippetError= false;
			boolean runMethodError= false;
			InstructionSequence errorSequence= new InstructionSequence(snippet);
			int codeSnippetStart= mapper.getSnippetStart();
			int codeSnippetEnd= codeSnippetStart + mapper.getSnippet().length();
			int runMethodStart= mapper.getRunMethodStart();
			int runMethodEnd= runMethodStart + mapper.getRunMethodLength();
			for (int i = 0; i < messages.length; i++) {
				Message message= messages[i];
				int errorOffset= message.getStartPosition();
				// TO DO: Internationalize "void method..." error message check
				if (codeSnippetStart <= errorOffset && errorOffset <= codeSnippetEnd && !fVoidMessageError.equals(message.getMessage())) {
					errorSequence.addError(message);
					snippetError = true;
				} else if (runMethodStart <= errorOffset && errorOffset <= runMethodEnd && !fVoidMessageError.equals(message.getMessage())) {
					runMethodError = true;
				}
			}
			if (snippetError || runMethodError) {
				if (runMethodError) {
					errorSequence.addError(new Message(EvaluationEngineMessages.getString("ASTEvaluationEngine.Evaluations_must_contain_either_an_expression_or_a_block_of_well-formed_statements_1"), 0)); //$NON-NLS-1$
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
		// Stop all evaluation threads.
		Iterator iter= fEvaluationThreads.iterator();
		while (iter.hasNext()) {
			((EvaluationThread)iter.next()).stop();
		}
	}
	
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}
}
