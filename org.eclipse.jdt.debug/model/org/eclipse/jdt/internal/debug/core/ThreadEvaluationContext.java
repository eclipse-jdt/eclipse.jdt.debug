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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.ICodeSnippetRequestor;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaEvaluationListener;
import org.eclipse.jdt.debug.core.IJavaEvaluationResult;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

/**
 * An evaluation context for a thread.
 */

public class ThreadEvaluationContext implements ICodeSnippetRequestor, Runnable, IJavaEvaluationResult {	 
	
	/**
	 * The evaluation context
	 */
	private IEvaluationContext fContext;
	
	/**
	 * The thread to perform the evaluation in
	 */
	private JDIThread fModelThread;
	
	/**
	 * List of Problems
	 */
	private List fProblems;
	
	/**
	 * List of source fragments
	 */
	private List fSourceFragments;
	
	/**
	 * List of kinds of problems.
	 */
	private List fKinds;
		
	/**
	 * The actual code snippet
	 */
	private String fSnippet;
	
	/**
	 * The debug exception that describes the evaluation failure, if any
	 */
	private DebugException fDebugException = null;	 

	/**
	 * The listener that we report back to
	 */
	private IJavaEvaluationListener fListener;
	
	/**
	 * The resulting value
	 */
	private IJavaValue fValue;
	
	/**
	 * The (JDI) result
	 */
	private ObjectReference fResult;
	
	/**
	 * The (JDI) type of the result value
	 */
	private ClassObjectReference fResultType;
	 	
	/**
	 * Constructs a context for a thread in the given context
	 * 
	 * @param modelThread The associated thread
	 * @param context The evaluation context
	 */
	public ThreadEvaluationContext(JDIThread modelThread, IEvaluationContext context) {
		setContext(context);
		setModelThread(modelThread);
		setProblemsInternal(new ArrayList(0));
		setSourceFragments(new ArrayList(0));
		setKinds(new ArrayList(0));
	}	
	
	/**
	 * Evaluates the given expression and reports the result back to the
	 * given listener. 
	 * Evaluation is run in a separate thread.
	 * 
	  * @exception DebugException If this method fails. Reasons include:<ul>
	 * <li>REQUEST_FAILED - The request failed as the associated thread was
	 * 		not suspended.
	 * </ul>
	 */	
	protected void evaluate(String expression, IJavaEvaluationListener listener) throws DebugException {
		
		if (getModelThread().isSuspended()) {
			setListener(listener);
			setSnippet(expression);
			
			Thread t = new Thread(this);
			t.start();
		} else {
			getModelThread().requestFailed(JDIDebugModelMessages.getString("ThreadEvaluationContext.thread_not_suspended"), null); //$NON-NLS-1$
		}
	}
	
	/**
	 * Runs the evaluation.
	 * 
	 * @see Runnable#run()
	 */
	public void run() {
						
		try {
			doEvaluation();
			convertResult();
		} catch (DebugException e) {
			setException(e);
		}
		
		getListener().evaluationComplete(this);
	}
	
	/**
	 * Do thread specific evaluation
	 */
	protected void doEvaluation() throws DebugException {
		try {
			getEvaluationContext().evaluateCodeSnippet(getSnippet(), this, null);
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		}
	}
	
	/**
	 * Constucts an IValue from the result of the evaluation
	 */
	protected void convertResult() throws DebugException {
		if (getResult() != null) {
			// check the type of the result - if a primitive type, convert it
			String sig = getReflectedTypeSignature();
			if (sig.length() == 1) {
				// primitive type
				Field valueField = getValueField(sig);
				if (valueField != null) {
					try {
						Value v= getResult().getValue(valueField);
						setValue(new JDIValue((JDIDebugTarget)getModelThread().getDebugTarget(), v));
					} catch (RuntimeException e) {
						getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_retreiving_evaluation_result"), new String[] {e.toString()}), e); //$NON-NLS-1$
						// execution will not reach this line, as
						// #targetRequestFailed will will throw an exception
						return;
					}
				}
			} else {
				// not a primite type
				setValue(new JDIValue((JDIDebugTarget)getModelThread().getDebugTarget(), getResult()));
			}
		} else {
			if (getResultType() != null) {
				String sig = getReflectedTypeSignature();
				if (sig.equals("V") || sig.equals("Lvoid;")) { //$NON-NLS-2$ //$NON-NLS-1$
					// void
					setValue(new JDIVoidValue((JDIDebugTarget)getModelThread().getDebugTarget()));
				}
			} else {
				if (hasProblems()) {
					setValue(null);
				} else {
				// null
					setValue(new JDIValue((JDIDebugTarget)getModelThread().getDebugTarget(), null));
				}
			}
		}
	}
	
	protected String getReflectedTypeSignature() throws DebugException {
		try {
			return getResultType().reflectedType().signature();
		} catch (RuntimeException e) {
			getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_retreiving_evaluation_result_type_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;			
		}
	}
	
	/**
	 * Returns the first non static field in the result type with the given signature (we
	 * assume it contains the primitive value we are looking for)
	 */
	protected Field getValueField(String signature) throws DebugException {
		try {
			Iterator iter= getResult().referenceType().fields().iterator();
			while (iter.hasNext()) {
				Field f = (Field)iter.next();
				if (!f.isStatic() && f.signature().equals(signature)) {
					return f;
				}
			}
		} catch (RuntimeException e) {
			getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_locating_result_value"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_locating_result_value"), new String[] {""}), null); //$NON-NLS-1$
		// execution will not reach this line, as
		// #targetRequestFailed will thrown an exception			
		return null;
	}
	
	/**
	 * @see ICodeSnippetRequestor#acceptClassFiles(byte[][], String[][], String)
	 */
	public boolean acceptClassFiles(byte[][] classFiles, String[][] classFileNames, String codeSnippetClassName) {
		// deploy the class files
		try {
			getDebugTarget().deploy(classFiles, classFileNames, getEvaluationContext());
		} catch (DebugException e) {
			setException(e);
			return false;
		}
		
		// evaluate the snippet, if given a class name
		if (codeSnippetClassName != null) {
			try { 
				runSnippet(codeSnippetClassName);
			} catch (DebugException e) {
				setException(e);
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
									JDIDebugPlugin.logError(de);
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
	 * Evaluate the code snippet 
	 */
	protected void runSnippet(String codeSnippetClassName) throws DebugException {
		try {
			ClassType codeSnippetClass= getCodeSnippetClass(codeSnippetClassName);
			ObjectReference codeSnippet = getCodeSnippet(codeSnippetClass);

			// Get the method 'runCodeSnippet' and its arguments		
			Method method = (Method)codeSnippetClass.methodsByName(RUN_METHOD).get(0);
			// Invoke runCodeSnippet(CodeSnippet)
			getModelThread().invokeMethod(null, codeSnippet, method, Collections.EMPTY_LIST);
		
			// Retrieve the result	
			Field resultField = codeSnippetClass.fieldByName(RESULT_VALUE_FIELD);
			setResult((ObjectReference)codeSnippet.getValue(resultField));
			Field resultTypeField = codeSnippetClass.fieldByName(RESULT_TYPE_FIELD);
			setResultType((ClassObjectReference)codeSnippet.getValue(resultTypeField));
		} catch (DebugException e) {
			throw e;
		} catch (RuntimeException e) {
			getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_performing_evaluation"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception						
			return;
		}
	}
	
	protected ClassType getCodeSnippetClass(String codeSnippetClassName) throws DebugException {
		VirtualMachine jdiVM = getModelThread().getVM();
		ThreadReference jdiThread = getUnderlyingThread();
		// Get the code snippet class
		List classes = jdiVM.classesByName(codeSnippetClassName);
		if (classes.size() == 0) {
			// Load the class
			ClassType codeSnippetClass= classForName(codeSnippetClassName);
			if (codeSnippetClass == null) {
				throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
					IDebugStatusConstants.INTERNAL_ERROR, "Code snippet class could not be found", null));
			}
			return codeSnippetClass;
		} else {
			return (ClassType)classes.get(0);
		}
	}
	
	protected ObjectReference getCodeSnippet(ClassType codeSnippetClass) throws DebugException {	
		// Create a new code snippet
		Method constructor = (Method)codeSnippetClass.methodsByName("<init>").get(0); //$NON-NLS-1$
		return getModelThread().newInstance(codeSnippetClass, constructor, Collections.EMPTY_LIST);
	}
	
	/**
	 * @see ICodeSnippetRequestor#acceptProblem(IMarker, String, int)
	 */
	public void acceptProblem(IMarker problem, String sourceFragment, int kind) {
		getProblemsInternal().add(problem);
		getSourceFragments().add(sourceFragment);
		getKinds().add(new Integer(kind));
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
			VirtualMachine jdiVM = getModelThread().getVM();
			List classes = jdiVM.classesByName("java.lang.Class"); //$NON-NLS-1$
			if (classes.size() == 0) {
				getModelThread().requestFailed(JDIDebugModelMessages.getString("ThreadEvaluationContext.unable_to_load_code_snippet"), null); //$NON-NLS-1$
				return null;
			}
			ClassType classClass= (ClassType)classes.get(0);
			List methods= classClass.methodsByName("forName", "(Ljava/lang/String;)Ljava/lang/Class;"); //$NON-NLS-2$ //$NON-NLS-1$
			if (methods.isEmpty()) {
				getModelThread().requestFailed(JDIDebugModelMessages.getString("ThreadEvaluationContext.unable_to_load_code_snippet"), null); //$NON-NLS-1$
				return null;
			}
			
			Method forName = (Method)methods.get(0);
			ThreadReference jdiThread = getUnderlyingThread();
			StringReference nameArg = jdiVM.mirrorOf(className);
			List args = new ArrayList(1);
			args.add(nameArg);
			ClassObjectReference classObject = (ClassObjectReference)getModelThread().invokeMethod(classClass, null, forName, args);
			// translate the ClassObjectReference to the ClassType
			ClassType loadedClass = null;
			classes = jdiVM.classesByName(className);
			if (classes.size() > 0) {
				loadedClass = (ClassType)classes.get(0);
			} else {
				getModelThread().requestFailed(JDIDebugModelMessages.getString("ThreadEvaluationContext.unable_to_load_code_snippet"), null); //$NON-NLS-1$
				return null;
			}	
			return loadedClass;
		} catch (RuntimeException e) {
			getModelThread().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("ThreadEvaluationContext.exception_attempting_to_load_class"), new String[] {e.toString(), className}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will will throw an exception
			return null;
		}
	}
		
	/**
	 * Returns the JDI thread reference on the target,
	 * based on the "model" thread.
	 */
	protected ThreadReference getUnderlyingThread() {
		return getModelThread().getUnderlyingThread();
	}
	
	protected JDIThread getModelThread() {
		return fModelThread;
	}

	/**
	 * @see IJavaEvaluationResult#getThread()
	 */
	public IJavaThread getThread() {
		return fModelThread;
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
	 * @see IJavaEvaluationResult#getValue()
	 */
	public IJavaValue getValue() {
		return fValue;
	}
	
	/**
	 * @see IJavaEvaluationResult#hasProblems()
	 */
	public boolean hasProblems() {
		return !fProblems.isEmpty() || fDebugException != null;
	}
	
	/**
	 * @see IJavaEvaluationResult#getProblems()
	 */
	public IMarker[] getProblems() {
		return (IMarker[])fProblems.toArray(new IMarker[fProblems.size()]);
	}
	
	/**
	 * @see IJavaEvaluationResult#getSourceFragment(IMarker)
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
	 * @see IJavaEvaluationResult#getKind(IMarker)
	 */
	public int getKind(IMarker problem) {
		int index = fProblems.indexOf(problem);
		if (index >= 0) {
			return ((Integer)fKinds.get(index)).intValue();
		} else {
			return -1;
		}
	}
	
	/**
	 * @see IJavaEvaluationResult#getSnippet()
	 */
	public String getSnippet() {
		return fSnippet;
	}
	
	/**
	 * @see IJavaEvaluationResult#getException()
	 */
	public Throwable getException() {
		return fDebugException;
	}
	
	protected List getKinds() {
		return fKinds;
	}

	protected void setKinds(List kinds) {
		fKinds = kinds;
	}

	protected List getSourceFragments() {
		return fSourceFragments;
	}

	protected void setSourceFragments(List sourceFragments) {
		fSourceFragments = sourceFragments;
	}
	
	protected List getProblemsInternal() {
		return fProblems;
	}

	protected void setProblemsInternal(List problems) {
		fProblems = problems;
	}

	protected IEvaluationContext getContext() {
		return fContext;
	}

	protected void setContext(IEvaluationContext context) {
		fContext = context;
	}
	
	protected void setException(DebugException debugException) {
		fDebugException = debugException;
	}

	protected IJavaEvaluationListener getListener() {
		return fListener;
	}

	protected void setListener(IJavaEvaluationListener listener) {
		fListener = listener;
	}

	protected void setModelThread(JDIThread modelThread) {
		fModelThread = modelThread;
	}

	protected ObjectReference getResult() {
		return fResult;
	}

	protected void setResult(ObjectReference result) {
		fResult = result;
	}

	protected ClassObjectReference getResultType() {
		return fResultType;
	}

	protected void setResultType(ClassObjectReference resultType) {
		fResultType = resultType;
	}

	protected void setSnippet(String snippet) {
		fSnippet = snippet;
	}

	protected void setValue(IJavaValue value) {
		fValue = value;
	}
	
	public void acceptAst(CompilationUnitDeclaration ast, CompilationUnitScope scope) {
	}
}