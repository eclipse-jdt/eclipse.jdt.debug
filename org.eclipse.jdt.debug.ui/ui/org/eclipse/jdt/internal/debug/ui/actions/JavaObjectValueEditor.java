/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IVariableValueEditor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

/**
 * A variable value editor which prompts the user to enter an expression
 * for evaluation. The result of the evaluation is assigned to the variable.
 */
public class JavaObjectValueEditor implements IVariableValueEditor {

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.IVariableValueEditor#editVariable(org.eclipse.debug.core.model.IVariable, org.eclipse.swt.widgets.Shell)
     */
    public boolean editVariable(IVariable variable, Shell shell) {
        try {
            IJavaVariable javaVariable = (IJavaVariable) variable;
            String signature = javaVariable.getSignature();
            if ("Ljava/lang/String;".equals(signature)) { //$NON-NLS-1$
                StringValueInputDialog dialog= new StringValueInputDialog(shell, javaVariable);
                if (dialog.open() == Window.OK) {
                    String result = dialog.getResult();
                    if (dialog.isUseLiteralValue()) {
	                    variable.setValue(result);
                    } else {
                        setValue(variable, result);
                    }
                }
            } else {
                ExpressionInputDialog dialog= new ExpressionInputDialog(shell, javaVariable);
                if (dialog.open() == Window.OK) {
                    String result = dialog.getResult();
                    setValue(variable, result);
                }
            }
        } catch (DebugException e) {
            handleException(e);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.IVariableValueEditor#saveVariable(org.eclipse.debug.core.model.IVariable, java.lang.String, org.eclipse.swt.widgets.Shell)
     */
    public boolean saveVariable(IVariable variable, String expression, Shell shell) {
        IJavaVariable javaVariable = (IJavaVariable) variable;
        String signature= null;
        try {
            signature = javaVariable.getSignature();
	        if ("Ljava/lang/String;".equals(signature)) { //$NON-NLS-1$
	            return false;
	        }
	        setValue(variable, expression);
        } catch (DebugException e) {
            handleException(e);
        }
        return true;
    }

    /**
     * Evaluates the given expression and sets the given variable's value
     * using the result.
     * 
     * @param variable the variable whose value should be set
     * @param expression the expression to evaluate
     * @throws DebugException if an exception occurs evaluating the expression
     *  or setting the variable's value
     */
    protected void setValue(final IVariable variable, final String expression){
        UIJob job = new UIJob("Setting Variable Value"){ //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					IValue newValue = evaluate(expression);
					if (newValue != null) {
						variable.setValue(newValue);
					} else {
						variable.setValue(expression);
					}
				} catch (DebugException de) {
					handleException(de);
				}
				return Status.OK_STATUS;
			}
        };
        job.setSystem(true);
        job.schedule();
    }

    /**
     * Handles the given exception, which occurred during edit/save.
     */
    protected void handleException(DebugException e) {
        Throwable cause = e.getStatus().getException();
        if (cause instanceof InvalidTypeException) {
            IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, cause.getMessage(), null);
            JDIDebugUIPlugin.statusDialog(ActionMessages.JavaObjectValueEditor_3, status);
        } else {
            JDIDebugUIPlugin.statusDialog(e.getStatus()); 
        }
    }

    /**
     * Evaluates the given snippet. Reports any errors to the user.
     * @param stringValue the snippet to evaluate
     * @return the value that was computed or <code>null</code> if any errors occurred.
     */
    private IValue evaluate(String stringValue) throws DebugException {
        IAdaptable adaptable = DebugUITools.getDebugContext();
        IJavaStackFrame frame= (IJavaStackFrame) adaptable.getAdapter(IJavaStackFrame.class);
        if (frame != null) {
            IJavaThread thread = (IJavaThread) frame.getThread();
            IJavaProject project= getProject(frame);
            if (project != null) {
                final IEvaluationResult[] results= new IEvaluationResult[1];
                IAstEvaluationEngine engine = JDIDebugPlugin.getDefault().getEvaluationEngine(project, (IJavaDebugTarget) thread.getDebugTarget());
                IEvaluationListener listener= new IEvaluationListener() {
                    public void evaluationComplete(IEvaluationResult result) {
                        synchronized (JavaObjectValueEditor.this) {
                            results[0]= result;
                            JavaObjectValueEditor.this.notifyAll();
                        }
                    }
                };
    			synchronized(this) {
                    engine.evaluate(stringValue, frame, listener, DebugEvent.EVALUATION_IMPLICIT, false);
    				try {
    					this.wait();
    				} catch (InterruptedException e) {
    					if (results[0] == null){
	    					IStatus status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ActionMessages.JavaObjectValueEditor_0, e);
	        			    throw new DebugException(status);
    					}
    				}
    			}
    			IEvaluationResult result= results[0];
    			if (result == null) {
    			    return null;
    			}
    			if (result.hasErrors()) {
    			    DebugException exception = result.getException();
    			    StringBuffer buffer = new StringBuffer();
    			    if (exception == null) {
        			    String[] messages = result.getErrorMessages();
        			    for (int i = 0; i < messages.length; i++) {
                            buffer.append(messages[i]).append("\n "); //$NON-NLS-1$
                        }    			    	
    			    } else {
    			    	buffer.append(EvaluateAction.getExceptionMessage(exception));
    			    }
    			    IStatus status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, buffer.toString(), null);
    			    throw new DebugException(status);
    			}
    			return result.getValue();
            }
        }
        return null;
    }
    
    /**
     * (copied from EvaluateAction)
     */
	protected String getExceptionMessage(Throwable exception) {
		if (exception instanceof CoreException) {
			CoreException ce = (CoreException)exception;
			Throwable throwable= ce.getStatus().getException();
			if (throwable instanceof com.sun.jdi.InvocationException) {
				return getInvocationExceptionMessage((com.sun.jdi.InvocationException)throwable);
			} else if (throwable instanceof CoreException) {
				// Traverse nested CoreExceptions
				return getExceptionMessage(throwable);
			}
			return ce.getStatus().getMessage();
		}
		String message= NLS.bind(ActionMessages.Evaluate_error_message_direct_exception, new Object[] { exception.getClass() }); 
		if (exception.getMessage() != null) {
			message= NLS.bind(ActionMessages.Evaluate_error_message_exception_pattern, new Object[] { message, exception.getMessage() }); 
		}
		return message;
	}

	/**
	 * Returns a message for the exception wrapped in an invocation exception
	 */
	protected String getInvocationExceptionMessage(com.sun.jdi.InvocationException exception) {
			InvocationException ie= exception;
			ObjectReference ref= ie.exception();
			return NLS.bind(ActionMessages.Evaluate_error_message_wrapped_exception, new Object[] { ref.referenceType().name() }); 
	}
    
	/**
	 * Return the project associated with the given stack frame.
	 * (copied from JavaWatchExpressionDelegate)
	 */
	private IJavaProject getProject(IJavaStackFrame javaStackFrame) {
		ILaunch launch = javaStackFrame.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator locator= launch.getSourceLocator();
		if (locator == null) {
			return null;
		}

		Object sourceElement = locator.getSourceElement(javaStackFrame);
		if (!(sourceElement instanceof IJavaElement) && sourceElement instanceof IAdaptable) {
			sourceElement = ((IAdaptable)sourceElement).getAdapter(IJavaElement.class);
		}
		if (sourceElement instanceof IJavaElement) {
			return ((IJavaElement) sourceElement).getJavaProject();
		}
		return null;
	}
}
