package org.eclipse.jdt.internal.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import java.awt.event.FocusAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.ICodeSnippetRequestor;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.JDIThread;
import org.eclipse.jdt.internal.debug.core.JDIValue;
import org.eclipse.jdt.internal.eval.EvaluationConstants; 

/**
 * An evaluation engine that deploys class files locally
 */

public class LocalEvaluationEngine implements IEvaluationEngine, ICodeSnippetRequestor {	 
	
	/**
	 * The Java project context in which to compile snippets.
	 */
	private IJavaProject fJavaProject;
	
	/**
	 * The debug target on which to execute snippets
	 */
	private IJavaDebugTarget fDebugTarget;
	
	/**
	 * The location in which to deploy snippet class files
	 */
	private File fOutputDirectory;
	
	/**
	 * The listener to notify when the current evaluation
	 * is complete.
	 */
	private IEvaluationListener fListener;
	
	/**
	 * The stack frame context for the current evaluation
	 * or <code>null</code> if there is no stack frame
	 * context.
	 */
	private IJavaStackFrame fStackFrame;
	
	/**
	 * The result of this evaluation
	 */
	private EvaluationResult fResult;
	
	/**
	 * Collection of depolyed snippet class files
	 */
	private List fSnippetFiles;
	
	/**
	 * Evaluation context for the Java project associated
	 * with this evaluation engine.
	 */
	private IEvaluationContext fEvaluationContext;
		
	/**
	 * Cosntructs a new evaluation engine for the given VM in the context
	 * of the specified project. Class files required for the evaluation will
	 * be deployed to the specified directory (which must be on the class
	 * path of the VM in order for evaluation to work).
	 * 
	 * @param project context in which to compile snippets
	 * @param vm debug target in which to evaluate snippets
	 * @param directory location where snippet class files will
	 *  be deployed for execution. The directory must exist
	 */
	public LocalEvaluationEngine(IJavaProject project, IJavaDebugTarget vm, File directory) {
		setJavaProject(project);
		setDebugTarget(vm);
		setOutputDirectory(directory);
	}

	/*
	 * @see ICodeSnippetRequestor#acceptClassFiles(byte[][], String[][], String)
	 */
	public boolean acceptClassFiles(
		byte[][] classFileBytes,
		String[][] classFileCompoundNames,
		String codeSnippetClassName) {
			try {
				deploy(classFileBytes, classFileCompoundNames);
			} catch (DebugException e) {
				getResult().setException(e);
				return false;
			}
			if (codeSnippetClassName != null) {
				// create a new instance of the code snippet class
				// and invoke its 'run' method
				IJavaObject codeSnippetInstance = null;
				try {
					codeSnippetInstance = newInstance(codeSnippetClassName);
					codeSnippetInstance.sendMessage(RUN_METHOD, "()V", null, getThread(), false);
					IVariable[] fields = codeSnippetInstance.getVariables();
					IJavaVariable resultValue = null;
					IJavaVariable resultType = null;
					for (int i = 0; i < fields.length; i++) {
						if (fields[i].getName().equals(RESULT_TYPE_FIELD)) {
							resultType = (IJavaVariable)fields[i];
						}
						if (fields[i].getName().equals(RESULT_VALUE_FIELD)) {
							resultValue = (IJavaVariable)fields[i];
						}
					}
					IJavaValue result = convertResult((IJavaClassObject)resultType.getValue(), (IJavaObject)resultValue.getValue());
					getResult().setValue(result);
				} catch (DebugException e) {
					getResult().setException(e);
					
					Throwable underlyingException = e.getStatus().getException();
					if (underlyingException instanceof InvocationException) {
						ObjectReference theException = ((InvocationException)underlyingException).exception();
						if (theException != null) {
							try {
								try {
									IJavaObject v = (IJavaObject)JDIValue.createValue((JDIDebugTarget)getDebugTarget(), theException);
									v.sendMessage("printStackTrace", "()V", null, getThread(), false);
								} catch (DebugException de) {
									JDIDebugPlugin.logError(de);
								}
							} catch (RuntimeException re) {
								re.printStackTrace();
							}
						}
					}
										
					return false;
				}
			}
			return true;
	}

	/*
	 * @see ICodeSnippetRequestor#acceptProblem(IMarker, String, int)
	 */
	public void acceptProblem(
		IMarker problemMarker,
		String fragmentSource,
		int fragmentKind) {
			getResult().addProblem(problemMarker, fragmentKind, fragmentSource);
	}

	/*
	 * @see ICodeSnippetRequestor#acceptAst(CompilationUnitDeclaration, CompilationUnitScope)
	 */
	public void acceptAst(
		CompilationUnitDeclaration ast,
		CompilationUnitScope scope) {
	}

	/**
	 * @see IEvaluationEngine#getDebugTarget()
	 */
	public IJavaDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/**
	 * Sets the debug target in which snippets are executed.
	 * 
	 * @param debugTarget the debug target in which snippets are executed
	 */
	private void setDebugTarget(IJavaDebugTarget debugTarget) {
		fDebugTarget = debugTarget;
	}

	/**
	 * @see IEvaluationEngine#getJavaProject()
	 */
	public IJavaProject getJavaProject() {
		return fJavaProject;
	}

	/**
	 * Sets the Java project in which snippets are compiled.
	 * 
	 * @param javaProject the Java project in which snippets are compiled
	 */
	private void setJavaProject(IJavaProject javaProject) {
		fJavaProject = javaProject;
	}

	/**
	 * Returns the directory in which snippet class files are
	 * deployed.
	 * 
	 * @return class file deployment directory
	 */
	protected File getOutputDirectory() {
		return fOutputDirectory;
	}

	/**
	 * Sets the directory in which snippet class files are
	 * deployed.
	 * 
	 * @param outputDirectory location to deploy snippet class files
	 */
	private void setOutputDirectory(File outputDirectory) {
		fOutputDirectory = outputDirectory;
	}

	/*
	 * @see IEvaluationEngine#evaluate(String, IJavaThread, IEvaluationListener)
	 */
	public void evaluate(
		String snippet,
		IJavaThread thread,
		IEvaluationListener listener)
		throws DebugException {
			checkDisposed();
			setListener(listener);
			setResult(new EvaluationResult(this, snippet, thread));
			checkThread();
			
			// do the evaluation in a different thread
			Runnable r = new Runnable() {
				public void run() {
					try {
						LocalEvaluationEngine.this.getEvaluationContext().
							evaluateCodeSnippet(LocalEvaluationEngine.this.getSnippet(),
								LocalEvaluationEngine.this, null);
					} catch (JavaModelException e) {
						LocalEvaluationEngine.this.getResult().setException(e);
					}
					LocalEvaluationEngine.this.evaluationComplete();
				}
			};
			
			Thread t = new Thread(r);
			t.start();
			
	}

	/*
	 * @see IEvaluationEngine#evaluate(String, IJavaStackFrame, IEvaluationListener)
	 */
	public void evaluate(
		String snippet,
		IJavaStackFrame frame,
		IEvaluationListener listener)
		throws DebugException {
			checkDisposed();
			setListener(listener);
			setStackFrame(frame);
			setResult(new EvaluationResult(this, snippet, (IJavaThread)frame.getThread()));
			checkThread();
	}
	
	/**
	 * Throws an exception if this engine has already been 
	 * disposed.
	 * 
	 * @exception DebugException if this engine has been disposed
	 */
	protected void checkDisposed() throws DebugException {
		if (isDisposed()) {
			throw new DebugException(
				new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
				IDebugStatusConstants.REQUEST_FAILED, "Evaluation failed - evaluation context has been disposed.", null)
			);
		}
	}
	
	/**
	 * Throws an exception if this engine's current evaluation
	 * thread is not suspended.
	 * 
	 * @exception DebugException if this engine's current evaluation
	 *  thread is not suspended
	 */
	protected void checkThread() throws DebugException {
		if (!getThread().isSuspended()) {
			throw new DebugException(
				new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
				IDebugStatusConstants.REQUEST_FAILED, "Evaluation failed - evaluation thread must be suspended.", null)
			);
		}
	}	

	/**
	 * Deletes deployed class files, and clears state.
	 * 
	 * @see IEvaluationEngine#dispose()
	 */
	public void dispose() {
		List snippetFiles = getSnippetFiles();
		Iterator iter = snippetFiles.iterator();
		while (iter.hasNext()) {
			File file = (File)iter.next();
			file.delete();
		}
		reset();
		setJavaProject(null);
		setDebugTarget(null);
		setOutputDirectory(null);
		setResult(null);
		setEvaluationContext(null);
	}

	/**
	 * Resets this engine for another evaluation.
	 */
	private void reset() {
		setStackFrame(null);
		setListener(null);
	}
	
	/**
	 * Returns the listener to notify when the current
	 * evaluation is complete.
	 * 
	 * @return the listener to notify when the current
	 * evaluation is complete
	 */
	protected IEvaluationListener getListener() {
		return fListener;
	}

	/**
	 * Sets the listener to notify when the current
	 * evaluation is complete.
	 * 
	 * @param listener the listener to notify when the current
	 *  evaluation is complete
	 */
	private void setListener(IEvaluationListener listener) {
		fListener = listener;
	}

	/**
	 * Returns the stack frame context for the current
	 * evaluation, or <code>null</code> if none.
	 * 
	 * @return the stack frame context for the current
	 *  evaluation, or <code>null</code> if none
	 */
	protected IJavaStackFrame getStackFrame() {
		return fStackFrame;
	}

	/**
	 * Sets the stack frame context for the current evaluation.
	 * 
	 * @param stackFrame stack frame context or <code>null</code>
	 *   if none
	 */
	private void setStackFrame(IJavaStackFrame stackFrame) {
		fStackFrame = stackFrame;
	}

	/**
	 * Returns the thread in which the current evaluation is
	 * to be executed.
	 * 
	 * @return the thread in which the current evaluation is
	 *  to be executed
	 */
	protected IJavaThread getThread() {
		return getResult().getThread();
	}
	
	/**
	 * Returns the code snippet being evaluated.
	 * 
	 * @return the code snippet being evaluated.
	 */
	protected String getSnippet() {
		return getResult().getSnippet();
	}	

	/**
	 * Returns the current evaluation result.
	 * 
	 * @return the current evaluation result
	 */
	protected EvaluationResult getResult() {
		return fResult;
	}

	/**
	 * Sets the current evaluation result.
	 * 
	 * @param result the current evaluation result
	 */
	private void setResult(EvaluationResult result) {
		fResult = result;
	}
	
	/**
	 * Deploys the given class files to this engine's
	 * output location, and adds the files to this
	 * engines list of temporary files to be deleted
	 * when disposed.
	 *
	 * @exception DebugException if this fails due to a
	 * lower level exception.
	 */
	protected void deploy(final byte[][] classFiles, final String[][] classFileNames) throws DebugException {

		// create the files in a workspace runnable
		IWorkspace workspace= getJavaProject().getProject().getWorkspace();
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {					
					for (int i = 0; i < classFiles.length; i++) {
						String[] compoundName = classFileNames[i];
						//create required folders
						File dir = LocalEvaluationEngine.this.getOutputDirectory();
						try {
							String pkgDirName = dir.getCanonicalPath();
							for (int j = 0; j < (compoundName.length - 1); j++) {
								pkgDirName += File.separator +  compoundName[j];
							}
							File pkgDir = new File(pkgDirName);
							pkgDir.mkdirs();
							String name = compoundName[compoundName.length - 1] + ".class"; //$NON-NLS-1$
							File classFile = new File(pkgDirName + File.separator + name);
							if (!classFile.exists()) {
								classFile.createNewFile();
							}
							FileOutputStream stream = new FileOutputStream(classFile);
							stream.write(classFiles[i]);
							stream.close();
							LocalEvaluationEngine.this.addSnippetFile(classFile);
						} catch (IOException e) {
							throw new DebugException(
								new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(), IDebugStatusConstants.REQUEST_FAILED, 
									MessageFormat.format("{0} occurred deploying class file for evaluation", new String[] {e.toString()}), e)
							);
						}
					}	
				}
			};
		try {	
			workspace.run(runnable, null);				
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
	}	
	
	/**
	 * Adds the given file to this engine's collection 
	 * of deployed snippet class files, which are to
	 * be deleted when this engine is diposed.
	 * 
	 * @param File snippet class file
	 */
	private void addSnippetFile(File file) {
		if (fSnippetFiles == null) {
			fSnippetFiles = new ArrayList();
		}
		fSnippetFiles.add(file);
	}
	
	

	/**
	 * Returns an evaluation context for this evaluation
	 * engine. An evaluation context is associted with a
	 * specific Java project. The evaluation context is
	 * created lazily on the first access.
	 *  
	 * @return evaluation context
	 */
	protected IEvaluationContext getEvaluationContext() {
		if (fEvaluationContext == null) {
			fEvaluationContext = getJavaProject().newEvaluationContext();
		}
		return fEvaluationContext;
	}
	
	/**
	 * Sets the evaluation context for this evaluation
	 * engine.
	 *  
	 * @param context evaluation context
	 */
	private void setEvaluationContext(IEvaluationContext context) {
		fEvaluationContext = context;
	}
	

	/**
	 * Returns a collection of snippet class file deployed by
	 * this evaluation engine, possibly empty.
	 * 
	 * @return deployed class files
	 */
	protected List getSnippetFiles() {
		if (fSnippetFiles == null) {
			return Collections.EMPTY_LIST;
		} else {
			return fSnippetFiles;
		}
	}
	
	/**
	 * Retursn whether this evaluation engine has been
	 * disposed.
	 * 
	 * @return whether this evaluation engine has been
	 *  disposed
	 */
	protected boolean isDisposed() {
		return getJavaProject() == null;
	}
	
	/**
	 * The evaluation is complete. Notify the current listener
	 * and reset for the next evaluation.
	 */
	protected void evaluationComplete() {
		getListener().evaluationComplete(getResult());
		reset();
	}
	
	/**
	 * Constructs and returns a new instance of the specified
	 * class on the  target VM.
	 * 
	 * @param className fully qualified class name
	 * @return a new instance on the target, as an <code>IJavaValue</code>
	 * @exception DebugException if creation fails
	 */
	protected IJavaObject newInstance(String className) throws DebugException {
		IJavaObject object = null;
		IJavaClassType clazz = null;
		clazz = (IJavaClassType)getDebugTarget().getJavaType(className);
		if (clazz == null) {
			// The class is not loaded on the target VM.
			// Force the load of the class.
			IJavaClassType classClass = (IJavaClassType)getDebugTarget().getJavaType("java.lang.Class");
			if (classClass == null) {
				// unable to load the class
				throw new DebugException(
					new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
					IDebugStatusConstants.REQUEST_FAILED, "Evaluation failed - unable to instantiate code snippet class.", null)
				);				
			}
			IJavaValue[] args = new IJavaValue[] {getDebugTarget().newValue(className)};
			IJavaObject classObject = (IJavaObject)classClass.sendMessage("forName", "(Ljava/lang/String;)Ljava/lang/Class;", args, getThread());
			object = (IJavaObject)classObject.sendMessage("newInstance", "()Ljava/lang/Object;", null, getThread(), false);
		} else {
			object = (IJavaObject)clazz.newInstance("<init>", null, getThread());
		}
		return object;
	}
	
	/**
	 * 
	 */
	protected IJavaValue convertResult(IJavaClassObject resultType, IJavaObject resultObject) throws DebugException {
		if (resultType == null) {
			// there was an exception or compilation problem - no result
			return null;
		}
		// check the type of the result - if a primitive type, convert it
		String sig = resultType.getInstanceType().getSignature();
		if (sig.equals("V") || sig.equals("Lvoid;")) { //$NON-NLS-2$ //$NON-NLS-1$
			// void
			return getDebugTarget().voidValue();
		}
		if (sig.length() == 1) {
			// primitive type - find the instance variable with the
			// signature of the result type we are looking for
			IVariable[] vars = resultObject.getVariables();
			IJavaVariable var = null;
			for (int i = 0; i < vars.length; i++) {
				IJavaVariable jv = (IJavaVariable)vars[i];
				if (!jv.isStatic() && jv.getSignature().equals(sig)) {
					var = jv;
					break;
				}
			}
			if (var != null) {
				return (IJavaValue)var.getValue();
			}
		} else {
			// an object
			return resultObject;
		}
		throw new DebugException(
			new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
			IDebugStatusConstants.REQUEST_FAILED, "Evaluation failed - internal error retreiving result.", null)
		);
	}
	
	
}