package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.ICodeSnippetRequestor;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaEvaluationListener;
import org.eclipse.jdt.debug.core.IJavaEvaluationResult;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

/**
 * An evaluation context for a stack frame.
 */

public class ThreadEvaluationContext implements ICodeSnippetRequestor, Runnable, IJavaEvaluationResult {	 
	
	/**
	 * The evaluation context for the associated java project
	 */
	protected IEvaluationContext fContext;
	
	/**
	 * The thread to perform the evaluation in
	 */
	protected JDIThread fModelThread;
	
	/**
	 * Problems, source fragments, and kinds
	 */
	protected List fProblems, fSourceFragments, fKinds;
		
	/**
	 * The actual code snippet
	 */
	protected String fSnippet;
	
	/**
	 * The debug exception that describes the evaluation failure, if any
	 */
	protected DebugException fDebugException = null;	 

	/**
	 * The listener that we report back to
	 */
	protected IJavaEvaluationListener fListener;
	
	/**
	 * The resulting value
	 */
	protected IJavaValue fValue;
	
	/**
	 * The (JDI) result
	 */
	protected ObjectReference fResult;
	
	/**
	 * The (JDI) type of the result value
	 */
	protected ClassObjectReference fResultType;
	 	
	/**
	 * Constructs a context for a thread and IEvaluationContext
	 */
	public ThreadEvaluationContext(JDIThread modelThread, IEvaluationContext context) {
		fContext = context;
		fModelThread = modelThread;
		fProblems = new ArrayList();
		fSourceFragments = new ArrayList();
		fKinds = new ArrayList();
	}	
		
	/**
	 * @see IJavaEvaluationAdapter
	 */
	public void evaluate(String expression, IJavaEvaluationListener listener) throws DebugException {
		
		if (getModelThread().isSuspended()) {
			fListener = listener;
			fSnippet = expression;
			
			Thread t = new Thread(this);
			t.start();
		} else {
			getModelThread().requestFailed(JDIDebugModelMessages.getString("ThreadEvaluationContext.thread_not_suspended"), null); //$NON-NLS-1$
		}
	}
	
	/**
	 * Runs the evaluation
	 */
	public void run() {
						
		try {
			doEvaluation();
			convertResult();
		} catch (DebugException e) {
			fDebugException = e;
		}
		
		fListener.evaluationComplete(this);
	}
	
	/**
	 * Do thread specific evaluation
	 */
	protected void doEvaluation() throws DebugException {
		try {
			getEvaluationContext().evaluateCodeSnippet(fSnippet, this, null);
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		}
	}
	
	/**
	 * Makes an IValue from the result of the evaluation
	 */
	protected void convertResult() throws DebugException {
		if (fResult != null) {
			// check the type of the result - if a primitive type, convert it
			String sig = fResultType.reflectedType().signature();
			if (sig.length() == 1) {
				// primitive type
				Field valueField = getValueField(sig);
				if (valueField != null) {
					try {
						Value v= fResult.getValue(valueField);
						fValue = new JDIValue((JDIDebugTarget)getModelThread().getDebugTarget(), v);
						return;
					} catch (RuntimeException e) {
						getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_retreiving_evaluation_result"), new String[] {e.toString()}), e); //$NON-NLS-1$
						return;
					}
				}
			}
			// not a primite type
			fValue = new JDIValue((JDIDebugTarget)getModelThread().getDebugTarget(), fResult);
		} else {
			if (fResultType != null) {
				try {
					ReferenceType ref= fResultType.reflectedType();
					String sig = ref.signature();
					if (sig.equals("V") || sig.equals("Lvoid;")) { //$NON-NLS-2$ //$NON-NLS-1$
						// void
						fValue = new JDIVoidValue((JDIDebugTarget)getModelThread().getDebugTarget());
						return;
					}
				} catch (RuntimeException e) {
					getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_retreiving_evaluation_result_type_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
					return;
				}
			}
			if (hasProblems()) {
				fValue= null;
			} else {
				// null
				fValue = new JDIValue((JDIDebugTarget)getModelThread().getDebugTarget(), null);
			}
		}
	}
	
	/**
	 * Returns the first non static field in the result type with the given signature (we
	 * assume it contains the primitive value we are looking for)
	 */
	protected Field getValueField(String signature) throws DebugException {
		try {
			Iterator iter= fResult.referenceType().fields().iterator();
			while (iter.hasNext()) {
				Field f = (Field)iter.next();
				if (!f.isStatic() && f.signature().equals(signature)) {
					return f;
				}
			}
		} catch (RuntimeException e) {
			getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_locating_result_value"), new String[] {e.toString()}), e); //$NON-NLS-1$
			return null;
		}
		return null;
	}
	
	/**
	 * @see ICodeSnippetRuntime
	 */
	public boolean acceptClassFiles(byte[][] classFiles, String[][] classFileNames, String codeSnippetClassName) {
		// deploy the class files
		try {
			deploy(classFiles, classFileNames);
		} catch (DebugException e) {
			fDebugException = e;
			return false;
		}
		
		// evaluate the snippet, if given a class name
		if (codeSnippetClassName != null) {
			try { 
				runSnippet(codeSnippetClassName);
			} catch (DebugException e) {
				fDebugException = e;
				// dump stack trace if invocation exception
				Throwable underlyingException = e.getStatus().getException();
				if (underlyingException instanceof InvocationException) {
					ObjectReference theException = ((InvocationException)underlyingException).exception();
					if (theException != null) {
						try {
							List methods = theException.referenceType().methodsByName("printStackTrace", "()V"); //$NON-NLS-2$ //$NON-NLS-1$
							if (!methods.isEmpty()) {
								try {
									getModelThread().invokeMethod(null, theException, (Method)methods.get(0), Collections.EMPTY_LIST);
								} catch (DebugException de) {
								}
							}
						} catch (RuntimeException re) {
							getModelThread().logError(re);
						}
					}
				}
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Deploy the class files.
	 *
	 * <p>Currently, this involves writing them to the output folder of the
	 * associated Java Project.
	 *
	 * @exception DebugException if this fails due to a lower level exception.
	 */
	protected void deploy(byte[][] classFiles, String[][] classFileNames) throws DebugException {
		getDebugTarget().deploy(classFiles, classFileNames, getEvaluationContext());
	}
	
	/**
	 * Evaluate the code snippet 
	 */
	protected void runSnippet(String codeSnippetClassName) throws DebugException {
		ClassType codeSnippetClass;
		ObjectReference codeSnippet;
		Method method;
		List arguments;
		ObjectReference codeSnippetRunner;
		VirtualMachine jdiVM = getVM();
		ThreadReference jdiThread = getUnderlyingThread();
		try {
			// Get the code snippet class
			List classes = jdiVM.classesByName(codeSnippetClassName);
			if (classes.size() == 0) {
				// Load the class
				codeSnippetClass = classForName(codeSnippetClassName);
				if (codeSnippetClass == null) {
					getModelThread().requestFailed(JDIDebugModelMessages.getString("ThreadEvaluationContext.unable_to_load_code_snippet"), null); //$NON-NLS-1$
					return;
				}
			} else {
				codeSnippetClass = (ClassType)classes.get(0);
			}

			// Create a new code snippet
			Method constructor = (Method)codeSnippetClass.methodsByName("<init>").get(0); //$NON-NLS-1$
			codeSnippet = getModelThread().newInstance(codeSnippetClass, constructor, new ArrayList());

			// Get the method 'runCodeSnippet' and its arguments		
			method = (Method)codeSnippetClass.methodsByName(RUN_METHOD).get(0);
			arguments = new ArrayList();
			
			// Invoke runCodeSnippet(CodeSnippet)
			getModelThread().invokeMethod(null, codeSnippet, method, arguments);
		
			// Retrieve the result	
			Field resultField = codeSnippetClass.fieldByName(RESULT_VALUE_FIELD);
			fResult = (ObjectReference)codeSnippet.getValue(resultField);
			Field resultTypeField = codeSnippetClass.fieldByName(RESULT_TYPE_FIELD);
			fResultType = (ClassObjectReference)codeSnippet.getValue(resultTypeField);
		} catch (DebugException e) {
			throw e;
		} catch (RuntimeException e) {
			getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_performing_evaluation"), new String[] {e.toString()}), e); //$NON-NLS-1$
			return;
		}
	}
	
	/**
	 * @see ICodeSnippetRequestor
	 */
	public void acceptProblem(IMarker problem, String sourceFragment, int kind) {
		fProblems.add(problem);
		fSourceFragments.add(sourceFragment);
		fKinds.add(new Integer(kind));
	}
	
	/**
	 * Force the load of the code snippet class in the target VM by
	 * emulating a Class.forName(String).
	 *
	 * @exception DebugException on failure
	 */
	protected ClassType classForName(String className) throws DebugException {
		try {
			// get java.lang.Class
			VirtualMachine jdiVM = getVM();
			List classes = jdiVM.classesByName("java.lang.Class"); //$NON-NLS-1$
			if (classes.size() == 0) {
				return null;
			}
			ClassType classClass= (ClassType)classes.get(0);
			List methods= classClass.methodsByName("forName", "(Ljava/lang/String;)Ljava/lang/Class;"); //$NON-NLS-2$ //$NON-NLS-1$
			if (methods.isEmpty()) {
				return null;
			}
			
			Method forName = (Method)methods.get(0);
			ThreadReference jdiThread = getUnderlyingThread();
			StringReference nameArg = jdiVM.mirrorOf(className);
			List args = new ArrayList();
			args.add(nameArg);
			ClassObjectReference classObject = (ClassObjectReference)getModelThread().invokeMethod(classClass, null, forName, args);
			// translate the ClassObjectReference to the ClassType
			ClassType loadedClass = null;
			classes = jdiVM.classesByName(className);
			if (classes.size() > 0) {
				loadedClass = (ClassType)classes.get(0);
			}		
			return loadedClass;
		} catch (RuntimeException e) {
			getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_attempting_to_load_class"), new String[] {e.toString(), className}), e); //$NON-NLS-1$
			return null;
		}
	}
		
	/**
	 * Helper method - returns the jdi thread reference on the target,
	 * based on the "model" thread.
	 */
	protected ThreadReference getUnderlyingThread() {
		return getModelThread().fThread;
	}
	
	/**
	 * Helper method - returns the model thread
	 */
	protected JDIThread getModelThread() {
		return fModelThread;
	}

	public IJavaThread getThread() {
		return getModelThread();
	}
	
	/**
	 * Helper method - returns the jdi VM
	 */
	protected VirtualMachine getVM() {
		return fModelThread.getVM();
	}

	/**
	 * Returns the debug target in which the evaluation is being performed.
	 */
	protected JDIDebugTarget getDebugTarget() {
		return (JDIDebugTarget)fModelThread.getDebugTarget();
	}
	
	/**
	 * Returns the underlying evaluation context for this evaluation.
	 */
	protected IEvaluationContext getEvaluationContext() {	
		return fContext;
	}
	
	/**
	 * @see IJavaEvaluationResult
	 */
	public IJavaValue getValue() {
		return fValue;
	}
	
	/**
	 * @see IJavaEvaluationResult
	 */
	public boolean hasProblems() {
		return !fProblems.isEmpty() || fDebugException != null;
	}
	
	/**
	 * @see IJavaEvaluationResult
	 */
	public IMarker[] getProblems() {
		return (IMarker[])fProblems.toArray(new IMarker[fProblems.size()]);
	}
	
	/**
	 * @see IJavaEvaluationResult
	 */
	public String getSourceFragment(IMarker problem) {
		int index = fProblems.indexOf(problem);
		if (index >=0) {
			return (String)fSourceFragments.get(index);
		} else {
			return null;
		}
	}
	
	/**
	 * @see IJavaEvaluationResult
	 */
	public int getKind(IMarker problem) {
		int index = fProblems.indexOf(problem);
		if (index >=0) {
			return ((Integer)fKinds.get(index)).intValue();
		} else {
			return -1;
		}
	}
	
	/**
	 * @see IJavaEvaluationResult
	 */
	public String getSnippet() {
		return fSnippet;
	}
	
	/**
	 * @see IJavaEvaluationResult
	 */
	public Throwable getException() {
		return fDebugException;
	}
}


