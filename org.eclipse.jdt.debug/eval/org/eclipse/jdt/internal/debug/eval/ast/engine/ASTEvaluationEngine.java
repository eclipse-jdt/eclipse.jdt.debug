/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.internal.debug.eval.EvaluationResult;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionSequence;

public class ASTEvaluationEngine implements IAstEvaluationEngine {

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
	 * @see IEvaluationEngine#evaluate(String, IJavaStackFrame, IEvaluationListener)
	 */
	public void evaluate(String snippet, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail) throws DebugException {
		ICompiledExpression expression= getCompiledExpression(snippet, frame);
		evaluateExpression(expression, frame, listener, evaluationDetail);
	}
	
	/**
	 * @see IEvaluationEngine#evaluate(String, IJavaObject, IJavaThread, IEvaluationListener)
	 */
	public void evaluate(String snippet, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener, int evaluationDetail) throws DebugException {
		ICompiledExpression expression= getCompiledExpression(snippet, thisContext);
		evaluateExpression(expression, thisContext, thread, listener, evaluationDetail);
	}
	
	/**
	 * @see IEvaluationEngine#evaluate(ICompiledExpression, IJavaStackFrame, IEvaluationListener)
	 */
	public void evaluateExpression(ICompiledExpression expression, IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail) throws DebugException {
		RuntimeContext context = new RuntimeContext(getJavaProject(), frame);
		doEvaluation(expression, context, (IJavaThread)frame.getThread(), listener, evaluationDetail);
	}

	/**
	 * @see IEvaluationEngine#evaluate(ICompiledExpression, IJavaObject, IJavaThread, IEvaluationListener)
	 */
	public void evaluateExpression(ICompiledExpression expression, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener, int evaluationDetail) throws DebugException {
		IRuntimeContext context = new JavaObjectRuntimeContext(thisContext, getJavaProject(), thread);
		doEvaluation(expression, context, thread, listener, evaluationDetail);
	}
	
	/**
	 * Evaluates the given expression in the given thread and the given runtime context.
	 */
	private void doEvaluation(ICompiledExpression expression, IRuntimeContext context, IJavaThread thread, IEvaluationListener listener, int evaluationDetail) throws DebugException {		
		getEvaluationThread().evaluate(expression, context, thread, listener, evaluationDetail);
	}
	
	private EvaluationThread getEvaluationThread() {
		Iterator iter= fEvaluationThreads.iterator();
		EvaluationThread thread= null;
		while (iter.hasNext()) {
			thread= (EvaluationThread)iter.next();
			if (!thread.isEvaluating()) {
				return thread;
			}
		}
		thread= new EvaluationThread();
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
	private void evaluationThreadFinished(EvaluationThread thread) {
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
	
	class EvaluationThread {
		private ICompiledExpression fExpression;
		private IRuntimeContext fContext;
		private IJavaThread fThread;
		private IEvaluationListener fListener;
		private int fEvaluationDetail;

		private boolean fEvaluating= false;
		private Thread fEvaluationThread;
		private boolean fStopped= false;
		private Object fLock= new Object();
		
		public boolean isEvaluating() {
			return fEvaluating;
		}
		
		public void stop() {
			fStopped= true;
			synchronized (fLock) {
				fLock.notify();
			}
		}
		
		public void evaluate(ICompiledExpression expression, IRuntimeContext context, IJavaThread thread, IEvaluationListener listener, int evaluationDetail) {
			fExpression= expression;
			fContext= context;
			fThread= thread;
			fListener= listener;
			fEvaluationDetail= evaluationDetail;
			if (fEvaluationThread == null) {
				// Create a new thread
				fEvaluationThread= new Thread(new Runnable() {
					public void run() {
						while (!fStopped) {
							synchronized (fLock) {
								doEvaluation();
								try {
									// Sleep until the next evaluation
									fLock.wait();
								} catch (InterruptedException exception) {
								}
							}
						}
					}
				}, "Evaluation thread"); //$NON-NLS-1$
				fEvaluationThread.start();
			} else {
				// Use the existing thread
				synchronized (fLock) {
					fLock.notify();
				}
			}
		}
		
		public synchronized void doEvaluation() {
			fEvaluating= true;
			EvaluationResult result = new EvaluationResult(ASTEvaluationEngine.this, fExpression.getSnippet(), fThread);
			if (fExpression.hasErrors()) {
				Message[] errors= fExpression.getErrors();
				for (int i= 0, numErrors= errors.length; i < numErrors; i++) {
					result.addError(errors[i]);
				}
				fListener.evaluationComplete(result);
				return;
			}
	
			final IJavaValue[] valuez = new IJavaValue[1];
			final InstructionSequence instructionSet = (InstructionSequence)fExpression;
			IEvaluationRunnable er = new IEvaluationRunnable() {
				public void run(IJavaThread jt, IProgressMonitor pm) {
					valuez[0] = instructionSet.evaluate(fContext);
				}
			};
			CoreException exception = null;
			try {
				fThread.runEvaluation(er, null, fEvaluationDetail);
			} catch (DebugException e) {
				exception = e;
			}
			IJavaValue value = valuez[0];
			

			if (exception == null) {
				exception= instructionSet.getException();
			}
			
			if (value != null) {
				result.setValue(value);
			}
			if (exception != null) {
				result.setException(new DebugException(exception.getStatus()));
			}
			fEvaluating= false;
			evaluationThreadFinished(this);
			fListener.evaluationComplete(result);
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
			int[] localModifiers = new int[numLocals];
			String[] localTypesNames= new String[numLocals];
			String[] localVariables= new String[numLocals];
			for (int i = 0; i < numLocals; i++) {
				localVariables[i] = locals[i].getName();
				localTypesNames[i] = locals[i].getReferenceTypeName();
				localModifiers[i]= 0;
			}
			mapper = new EvaluationSourceGenerator(new String[0], localModifiers, localTypesNames, localVariables, snippet);
			unit = AST.parseCompilationUnit(mapper.getSource(frame).toCharArray(), mapper.getCompilationUnitName(), javaProject);
		} catch (CoreException e) {
			InstructionSequence expression= new InstructionSequence(snippet);
			expression.addError(new Message(e.getStatus().getMessage(), 1));
			return expression;
		}
		
		return createExpressionFromAST(snippet, mapper, unit);
	}

	// ******
	// for hide problems with local variable declare as instance of Local Types
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
		IJavaProject javaProject = getJavaProject();

		EvaluationSourceGenerator mapper = null;
		CompilationUnit unit = null;

		mapper = new EvaluationSourceGenerator(new String[0], new int[0], new String[0], new String[0], snippet);

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
			boolean error= false;
			InstructionSequence errorSequence= new InstructionSequence(snippet);
			int codeSnippetStartOffset= mapper.getStartPosition();
			int codeSnippetEndOffset= codeSnippetStartOffset + mapper.getSnippet().length();
			for (int i = 0; i < messages.length; i++) {
				Message message= messages[i];
				int errorOffset= message.getSourcePosition();
				// TO DO: Internationalize "void method..." error message check
				if (codeSnippetStartOffset <= errorOffset && errorOffset <= codeSnippetEndOffset && !EngineEvaluationMessages.getString("ASTEvaluationEngine.Void_methods_cannot_return_a_value_1").equals(message.getMessage())) { //$NON-NLS-1$
					errorSequence.addError(message);
					error = true;
				}
			}
			if (error) {
				return errorSequence;
			}
		}
		
		ASTInstructionCompiler visitor = new ASTInstructionCompiler(mapper.getStartPosition(), snippet);
		unit.accept(visitor);

		return visitor.getInstructions();
	}

	/*
	 * @see IEvaluationEngine#getJavaProject()
	 */
	public IJavaProject getJavaProject() {
		return fProject;
	}

	/*
	 * @see IEvaluationEngine#getDebugTarget()
	 */
	public IJavaDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/*
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
