package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import com.sun.jdi.*;import java.util.*;import org.eclipse.core.resources.IMarker;import org.eclipse.debug.core.DebugException;import org.eclipse.jdt.core.JavaModelException;import org.eclipse.jdt.core.eval.ICodeSnippetRequestor;import org.eclipse.jdt.core.eval.IEvaluationContext;import org.eclipse.jdt.debug.core.*;

/**
 * An evaluation context for a stack frame.
 */

public class ThreadEvaluationContext implements ICodeSnippetRequestor, Runnable, IJavaEvaluationResult {
	
	//NLS
	protected static final String PREFIX = "jdi_evaluation.";
	protected static final String ERROR = PREFIX + "error.";
	protected static final String ERROR_EVALUATION = ERROR+ "evaluation";
	protected static final String ERROR_THREAD_NOT_SUSPENDED = ERROR+ "thread_not_suspended";
	 
	
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
			getModelThread().requestFailed(ERROR_THREAD_NOT_SUSPENDED, null);
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
						fValue = new JDIValue(v, getModelThread());
						return;
					} catch (RuntimeException e) {
						getModelThread().targetRequestFailed(ERROR_EVALUATION, e);
					}
				}
			}
			// not a primite type
			fValue = new JDIValue(fResult, getModelThread());
		} else {
			if (fResultType != null) {
				try {
					ReferenceType ref= fResultType.reflectedType();
					String sig = ref.signature();
					if (sig.equals("V") || sig.equals("Lvoid;")) {
						// void
						fValue = new JDIVoidValue(getModelThread());
						return;
					}
				} catch (RuntimeException e) {
					getModelThread().targetRequestFailed(ERROR_EVALUATION, e);
				}
			}
			if (hasProblems()) {
				fValue= null;
			} else {
				// null
				fValue = new JDIValue(null, getModelThread());
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
			getModelThread().targetRequestFailed(ERROR_EVALUATION, e);
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
					getModelThread().requestFailed(DebugJavaUtils.getResourceString("jdi_evaluation.error.evaluation"), null);
					return;
				}
			} else {
				codeSnippetClass = (ClassType)classes.get(0);
			}

			// Create a new code snippet
			Method constructor = (Method)codeSnippetClass.methodsByName("<init>").get(0);
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
			getModelThread().targetRequestFailed(ERROR_EVALUATION, e);
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
			List classes = jdiVM.classesByName("java.lang.Class");
			if (classes.size() == 0) {
				return null;
			}
			ClassType classClass= (ClassType)classes.get(0);
			List methods= classClass.methodsByName("forName", "(Ljava/lang/String;)Ljava/lang/Class;");
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
			getModelThread().targetRequestFailed(ERROR_EVALUATION, e);
		}
		return null;
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


