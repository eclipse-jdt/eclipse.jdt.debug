package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.sun.jdi.InvocationException;

public class JavaDetailFormattersManager implements IPropertyChangeListener, IDebugEventSetListener, ILaunchesListener {
	/**
	 * The default detail formatters manager.	 */
	static private JavaDetailFormattersManager fgDefault;
	
	/**
	 * Return the default detail formatters manager.
	 * 
	 * @return default detail formatters manager.	 */
	static public JavaDetailFormattersManager getDefault() {
		if (fgDefault == null) {
			fgDefault= new JavaDetailFormattersManager();
		}
		return fgDefault;
	}

	/**
	 * Map of types to the associated formatter (code snippet).
	 * (<code>String</code> -> <code>String</code>)
	 */
	private HashMap fDetailFormattersMap;
	
	/**
	 * Cache of compiled expressions.
	 * Associate a pair type name/debug target to a compiled expression.	 */
	private HashMap fCacheMap;
	
	/**
	 * JavaDetailFormattersManager constructor.	 */
	private JavaDetailFormattersManager() {
		populateDetailFormattersMap();
		JDIDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
		fCacheMap= new HashMap();
	}
	
	/**
	 * Populate the detail formatters map with data from preferences.
	 */
	private void populateDetailFormattersMap() {
		String[] detailFormattersList= JavaDebugOptionsManager.parseList(JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST));
		fDetailFormattersMap= new HashMap(detailFormattersList.length / 3);
		for (int i= 0, length= detailFormattersList.length; i < length;) {
			String typeName= detailFormattersList[i++];
			String snippet= detailFormattersList[i++].replace('\u0000', ',');
			boolean enabled= ! JavaDetailFormattersPreferencePage.DETAIL_FORMATTER_IS_DISABLED.equals(detailFormattersList[i++]);
			fDetailFormattersMap.put(typeName, new DetailFormatter(typeName, snippet, enabled));
		}
	}

	/**
	 * Compute asynchronously the 'toString' of the given value. If a formatter is associated to
	 * the type of the given value, this formatter is used instead of the <code>toString()</code>
	 * method.
	 * The result is return through the listener.
	 * 
	 * @param objectValue the value to 'format' 
	 * @param thread the thread to use to performed the evaluation
	 * @param listener the listener
	 */	
	public void computeValueDetail(final IJavaValue objectValue, final IJavaThread thread, final IValueDetailListener listener) {
		Runnable postEventDispatch = new Runnable() {
			public void run() {
				Runnable postEventProcess = new Runnable() {
					public void run() {
						thread.queueRunnable(new Runnable() {
							public void run() {
								resolveFormatter(objectValue, thread, listener);
							}
						});
					}
				};
				JDIDebugUIPlugin.getStandardDisplay().asyncExec(postEventProcess);
			}
		};
		DebugPlugin.getDefault().asyncExec(postEventDispatch);
	}
	
	private void resolveFormatter(final IJavaValue value, final IJavaThread thread, final IValueDetailListener listener) {
		ICompiledExpression compiledExpression= null;
		EvaluationListener evaluationListener= new EvaluationListener(value, thread, listener);
		if (value instanceof IJavaObject && !(value instanceof IJavaArray)) {
			IJavaObject objectValue= (IJavaObject) value;
			IJavaProject project= getJavaProject(thread);
			if (project != null) {
				// get the evaluation engine
				JDIDebugTarget debugTarget= (JDIDebugTarget) thread.getDebugTarget();
				IAstEvaluationEngine evaluationEngine= JDIDebugUIPlugin.getDefault().getEvaluationEngine(project, debugTarget);
				// get the compiled expression to use
				try {
					compiledExpression= getCompiledExpression(objectValue, debugTarget, evaluationEngine);
					if (compiledExpression != null) {
						evaluationEngine.evaluateExpression(compiledExpression, objectValue, thread, evaluationListener, DebugEvent.EVALUATION_IMPLICIT, false);
						return;
					}
				} catch (DebugException e) {
					DebugUIPlugin.log(e);
					return;
				}
			}
		}
		try {
			evaluationListener.valueToString(value);
		} catch (DebugException e) {
			if (e.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
				// don't log 'thread not suspended' errors
				JDIDebugUIPlugin.log(e);
			}
			listener.detailComputed(value, e.getStatus().getMessage());
		}
	}
	
	private IJavaProject getJavaProject(IJavaThread thread) {
		ILaunch launch= thread.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator locator= launch.getSourceLocator();
		if (locator == null)
			return null;

		Object sourceElement;
		try {
			IStackFrame frame = thread.getTopStackFrame();
			if (frame == null)
				return null;
			sourceElement= locator.getSourceElement(frame);
		} catch (DebugException e) {
			DebugUIPlugin.log(e);
			return null;
		}
		if (sourceElement instanceof IJavaElement) {
			return ((IJavaElement) sourceElement).getJavaProject();
		}
		return null;
	}



	public boolean hasAssociatedDetailFormatter(IJavaType type) {
		return getAssociatedDetailFormatter(type) != null;
	}
	
	public DetailFormatter getAssociatedDetailFormatter(IJavaType type) {
		String typeName;
		try {
			while (type instanceof IJavaArrayType) {
				type= ((IJavaArrayType)type).getComponentType();
			}
			if (type instanceof IJavaClassType) {
				typeName= type.getName();
			} else {
				return null;
			}
		} catch (DebugException e) {
			return null;
		}
		return (DetailFormatter)fDetailFormattersMap.get(typeName);
	}
	
	public void setAssociatedDetailFormatter(DetailFormatter detailFormatter) {
		fDetailFormattersMap.put(detailFormatter.getTypeName(), detailFormatter);
		savePreference();
	}


	private void savePreference() {
		Collection valuesList= fDetailFormattersMap.values();
		String[] values= new String[valuesList.size() * 3];
		int i= 0;
		for (Iterator iter= valuesList.iterator(); iter.hasNext();) {
			DetailFormatter detailFormatter= (DetailFormatter) iter.next();
			values[i++]= detailFormatter.getTypeName();
			values[i++]= detailFormatter.getSnippet().replace(',','\u0000');
			values[i++]= detailFormatter.isEnabled() ? JavaDetailFormattersPreferencePage.DETAIL_FORMATTER_IS_ENABLED : JavaDetailFormattersPreferencePage.DETAIL_FORMATTER_IS_DISABLED;
		}
		String pref = JavaDebugOptionsManager.serializeList(values);
		JDIDebugUIPlugin.getDefault().getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST, pref);
		JDIDebugUIPlugin.getDefault().savePluginPreferences();
	}
	/**
	 * Return the detail formatter (code snippet) associate with
	 * the given type or one of its super type.
	 */
	private String getDetailFormatter(IJavaClassType type) throws DebugException {
		if (type == null) {
			return null;
		}
		String typeName= type.getName();
		if (fDetailFormattersMap.containsKey(typeName)) {
			DetailFormatter detailFormatter= (DetailFormatter)fDetailFormattersMap.get(typeName);
			if (detailFormatter.isEnabled()) {
				return detailFormatter.getSnippet();
			}
		}
		return getDetailFormatter(type.getSuperclass());
	}

	/**
	 * Return the compiled expression which corresponds to the code formatter associated
	 * with the type of the given object.
	 * The code snippet is compiled in the context of the given object.
	 */
	private ICompiledExpression getCompiledExpression(IJavaObject javaObject, JDIDebugTarget debugTarget, IAstEvaluationEngine evaluationEngine) throws DebugException {
		IJavaClassType type= (IJavaClassType)javaObject.getJavaType();
		String typeName= type.getName();
		Key key= new Key(typeName, debugTarget);
		if (fCacheMap.containsKey(key)) {
			return (ICompiledExpression) fCacheMap.get(key);
		} else {
			String snippet= getDetailFormatter(type);
			if (snippet != null) {
				ICompiledExpression res= evaluationEngine.getCompiledExpression(snippet, javaObject);
				fCacheMap.put(key, res);
				return res;
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST)) {
			populateDetailFormattersMap();
			fCacheMap.clear();
		}
	}
	/**
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource() instanceof JDIDebugTarget && event.getKind() == DebugEvent.TERMINATE) {
				deleteCacheForTarget((JDIDebugTarget) event.getSource());
			}	
		}
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {
		for (int i = 0; i < launches.length; i++) {
			ILaunch launch = launches[i];
			IDebugTarget[] debugTargets= launch.getDebugTargets();
			for (int j = 0; j < debugTargets.length; j++) {
				if (debugTargets[j] instanceof JDIDebugTarget) {
					deleteCacheForTarget((JDIDebugTarget)debugTargets[j]);		
				}
			}
		}
	}

	/**
	 * Remove from the cache compiled expression associated with
	 * the given debug target.
	 * 
	 * @param debugTarget 
	 */
	private synchronized void deleteCacheForTarget(JDIDebugTarget debugTarget) {
		for (Iterator iter= fCacheMap.keySet().iterator(); iter.hasNext();) {
			Key key= (Key) iter.next();
			if ((key).fDebugTarget == debugTarget) {
				iter.remove();
			}
		}
	}

	/**
	 * Object used as the key in the cache map for associate a compiled
	 * expression with a pair type name/debug target
	 */
	static private class Key {
		private String fTypeName;
		private JDIDebugTarget fDebugTarget;
		
		Key(String typeName, JDIDebugTarget debugTarget) {
			fTypeName= typeName;
			fDebugTarget= debugTarget;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				Key key= (Key) obj;
				return fTypeName != null && fDebugTarget != null && fTypeName.equals(key.fTypeName) && fDebugTarget.equals(key.fDebugTarget);
			} else {
				return false;
			}
		}

		public int hashCode() {
			return fTypeName.hashCode() / 2 + fDebugTarget.hashCode() / 2;
		}
	}
	
	/**
	 * Listener use to manage the result of the formatter.
	 * Utilise the 'standart' pretty printer methods to return the result.
	 */
	static private class EvaluationListener implements IEvaluationListener {

		/**
		 * The selector of <code>java.lang.Object#toString()</code>,
		 * used to evaluate 'toString()' for displaying details of values.
		 */
		private static final String fgToString= "toString"; //$NON-NLS-1$


		/**
		 * The signature of <code>java.lang.Object#toString()</code>,
		 * used to evaluate 'toString()' for displaying details of values.
		 */
		private static final String fgToStringSignature= "()Ljava/lang/String;"; //$NON-NLS-1$
			
		private IJavaValue fValue;
		
		private IValueDetailListener fListener;
		
		private IJavaThread fThread;
		
		public EvaluationListener(IJavaValue value, IJavaThread thread, IValueDetailListener listener) {
			fValue= value;
			fThread= thread;
			fListener= listener;
		}

		public void evaluationComplete(IEvaluationResult result) {
			if (result.hasErrors()) {
				StringBuffer error= new StringBuffer(DebugUIMessages.getString("JavaDetailFormattersManager.Detail_formatter_error___1")); //$NON-NLS-1$
				DebugException exception= result.getException();
				if (exception != null) {
					Throwable throwable= exception.getStatus().getException();
					error.append("\n\t\t"); //$NON-NLS-1$
					if (throwable instanceof InvocationException) {
						error.append(MessageFormat.format(DebugUIMessages.getString("JavaDetailFormattersManager.An_exception_occurred__{0}_3"), new String[] {((InvocationException) throwable).exception().referenceType().name()})); //$NON-NLS-1$
					} else {
						error.append(exception.getStatus().getMessage());
					}
				} else {
					String[] errors= result.getErrorMessages();
					for (int i= 0, length= errors.length; i < length; i++) {
						error.append("\n\t\t").append(errors[i]); //$NON-NLS-1$
					}
				}
				fListener.detailComputed(fValue, error.toString());
			} else {
				try {
					valueToString(result.getValue());
				} catch (DebugException e) {
					if (e.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
						// don't log 'thread not suspended' errors
						JDIDebugUIPlugin.log(e);
					}
					fListener.detailComputed(fValue, e.getStatus().getMessage());
				}
			}
		}
		
		public void valueToString(final IJavaValue objectValue) throws DebugException {
			IEvaluationRunnable eval = new IEvaluationRunnable() {
				public void run(IJavaThread thread, IProgressMonitor monitor) throws DebugException {
					StringBuffer result= new StringBuffer();
					if (objectValue.getSignature() == null) {
						// no need to spawn a thread for a null fValue
						result.append(DebugUIMessages.getString("JavaDetailFormattersManager.null")); //$NON-NLS-1$
					} else if (objectValue instanceof IJavaPrimitiveValue) {
						// no need to spawn a thread for a primitive value
						appendJDIPrimitiveValueString(result, objectValue);
					} else if (fThread == null || !fThread.isSuspended()) {
						// no thread available
						result.append(DebugUIMessages.getString("JavaDetailFormattersManager.no_suspended_threads")); //$NON-NLS-1$
						appendJDIValueString(result, objectValue);
					} else if (objectValue instanceof IJavaArray) {
						appendArrayDetail(result, (IJavaArray) objectValue);
					} else if (objectValue instanceof IJavaObject) {
						appendObjectDetail(result, (IJavaObject) objectValue);
					} else {
						appendJDIValueString(result, objectValue);
					}
					fListener.detailComputed(fValue, result.toString());
				}
			};
			fThread.runEvaluation(eval, null, DebugEvent.EVALUATION_IMPLICIT, false);
		}
		
		protected void appendArrayDetail(StringBuffer result, IJavaArray arrayValue) throws DebugException {
			result.append('[');
			IJavaValue[] arrayValues;
			try {
				arrayValues= arrayValue.getValues();
			} catch (DebugException de) {
				JDIDebugUIPlugin.log(de);
				result.append(de.getStatus().getMessage());
				return;
			}
			for (int i= 0; i < arrayValues.length; i++) {
				IJavaValue value= arrayValues[i];
				if (value instanceof IJavaArray) {
					appendArrayDetail(result, (IJavaArray) value);
				} else if (value instanceof IJavaObject) {
					appendObjectDetail(result, (IJavaObject) value);
				} else {
					appendJDIValueString(result, value);
				}
				if (i < arrayValues.length - 1) {
					result.append(',');
					result.append(' ');
				}
			}
			result.append(']');
		}

		protected void appendJDIPrimitiveValueString(StringBuffer result, IJavaValue value) throws DebugException {
			result.append(value.getValueString());
		}


		protected void appendJDIValueString(StringBuffer result, IJavaValue value) throws DebugException {
			result.append(value.getValueString());
		}


		protected void appendObjectDetail(StringBuffer result, IJavaObject objectValue) throws DebugException {
			IJavaValue toStringValue= objectValue.sendMessage(EvaluationListener.fgToString, EvaluationListener.fgToStringSignature, null, fThread, false);
			if (toStringValue == null) {
				result.append(DebugUIMessages.getString("JavaDetailFormattersManager.<unknown>")); //$NON-NLS-1$
			} else {
				appendJDIValueString(result, toStringValue);
			}
		}


	
	}

}
