/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.ast.model.ICompiledExpression;
import org.eclipse.jdt.debug.eval.ast.model.IValue;
import org.eclipse.jdt.debug.eval.ast.model.IVariable;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTAPIVisitor;
import org.eclipse.jdt.internal.debug.eval.ast.engine.InstructionSequence;
import org.eclipse.jdt.internal.debug.eval.EvaluationResult;


/**
 * @version 	1.0
 * @author
 */
public class ASTAPIEvaluationEngine implements IEvaluationEngine {

	private IJavaProject fProject;
	
	private IJavaDebugTarget fDebugTarget;

	public ASTAPIEvaluationEngine(IJavaProject project, IJavaDebugTarget debugTarget) {
		setJavaProject(project);
		setDebugTarget(debugTarget);
	}
	
	public void setJavaProject(IJavaProject project) {
		fProject = project;
	}

	public void setDebugTarget(IJavaDebugTarget debugTarget) {
		fDebugTarget = debugTarget;
	}

	/*
	 * @see IEvaluationEngine#evaluate(String, IJavaThread, IEvaluationListener)
	*/ 
	public void evaluate(String snippet, IJavaThread thread, IEvaluationListener listener) throws DebugException {
	}

	/*
	 * @see IEvaluationEngine#evaluate(String, IJavaStackFrame, IEvaluationListener)
	 */
	public void evaluate(String snippet, IJavaStackFrame frame, IEvaluationListener listener) throws DebugException {
		ICompiledExpression expression= getCompiledExpression(snippet, frame);
		evaluate(expression, frame, listener);
	}


	/*
	 * @see IEvaluationEngine#evaluate(ICompiledExpression, IJavaStackFrame, IEvaluationListener)
	 */
	public void evaluate(final ICompiledExpression expression, final IJavaStackFrame frame, final IEvaluationListener listener) {
		Thread evaluationThread= new Thread(new Runnable() {
			public void run() {
				EvaluationResult result = new EvaluationResult(ASTAPIEvaluationEngine.this, expression.getSnippet(), (IJavaThread)frame.getThread());
				if (expression.hasErrors()) {
					Message[] errors= expression.getErrors();
					for (int i= 0, numErrors= errors.length; i < numErrors; i++) {
						result.addError(errors[i]);
					}
					listener.evaluationComplete(result);
					return;
				}
				IJavaProject javaProject = getJavaProject();
				RuntimeContext context = new RuntimeContext(javaProject, frame);
		
				IValue value = null;
				value= expression.evaluate(context);
				CoreException exception= expression.getException();
				
				if (value != null) {
					IJavaValue jv = ((EvaluationValue)value).getJavaValue();
					result.setValue(jv);
				}
				result.setException(exception);
				listener.evaluationComplete(result);
			}
		}, "Evaluation thread");
		evaluationThread.start();
	}

	/*
	 * @see IEvaluationEngine#getCompiledExpression(String, IJavaStackFrame)
	 */
	public ICompiledExpression getCompiledExpression(String snippet, IJavaStackFrame frame) throws DebugException {
		IJavaProject javaProject = getJavaProject();
		RuntimeContext context = new RuntimeContext(javaProject, frame);

		ASTCodeSnippetToCuMapper mapper = null;
		CompilationUnit unit = null;

		try {
			
			IVariable[] locals = context.getLocals();
			int numLocals= locals.length;
			int[] localModifiers = new int[locals.length];
			String[] localTypesNames= new String[numLocals];
			String[] localVariables= new String[numLocals];
			for (int i = 0; i < numLocals; i++) {
				localVariables[i] = locals[i].getName();
				localTypesNames[i] = ((EvaluationValue)locals[i].getValue()).getJavaValue().getReferenceTypeName();
				localModifiers[i]= 0;
			}
			
			mapper = new ASTCodeSnippetToCuMapper(new String[0], frame.getDeclaringType().getName(), localModifiers, localTypesNames, localVariables, frame, snippet);

			unit = AST.parseCompilationUnit(mapper.getSource().toCharArray(), mapper.getCompilationUnitName(), javaProject);
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
		
		Message[] messages= unit.getMessages();
		if (messages.length != 0) {
			boolean error= false;
			InstructionSequence errorSequence= new InstructionSequence(snippet);
			for (int i = 0; i < messages.length; i++) {
				Message message= messages[i];
				// bad test for remove the "Void methods cannot return a value" error
				if (!"Void methods cannot return a value".equals(message.getMessage())) {
					errorSequence.addError(message);
					error = true;
				}
			}
			if (error) {
				return errorSequence;
			}
		}
		
		ASTAPIVisitor visitor = new ASTAPIVisitor(mapper.getStartPosition(), snippet);
		unit.accept(visitor);

		return visitor.getInstructions();
	}

	/*
	 * @see IEvaluationEngine#evaluate(String, IJavaObject, IJavaThread, IEvaluationListener)
	 */
	public void evaluate(String snippet, IJavaObject thisContext, IJavaThread thread, IEvaluationListener listener) throws DebugException {
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
	}

}
