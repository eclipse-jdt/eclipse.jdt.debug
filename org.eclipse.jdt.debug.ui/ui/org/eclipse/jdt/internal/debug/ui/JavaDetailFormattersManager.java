/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchWindow;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvocationException;

public class JavaDetailFormattersManager implements IPropertyChangeListener, IDebugEventSetListener, ILaunchesListener {
	/**
	 * The default detail formatters manager.
	 */
	static private JavaDetailFormattersManager fgDefault;
	
	/**
	 * Return the default detail formatters manager.
	 * 
	 * @return default detail formatters manager.
	 */
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
	 * Associate a pair type name/debug target to a compiled expression.
	 */
	private HashMap fCacheMap;
	
	/**
	 * JavaDetailFormattersManager constructor.
	 */
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
						if (!thread.isSuspended() && !thread.isPerformingEvaluation()) {
							listener.detailComputed(objectValue, DebugUIMessages.JavaDetailFormattersManager_9); //$NON-NLS-1$
						} else {
							thread.queueRunnable(new Runnable() {
								public void run() {
									resolveFormatter(objectValue, thread, listener);
								}
							});
						}
					}
				};
				JDIDebugUIPlugin.getStandardDisplay().asyncExec(postEventProcess);
			}
		};
		DebugPlugin.getDefault().asyncExec(postEventDispatch);
	}
	
	private void resolveFormatter(final IJavaValue value, final IJavaThread thread, final IValueDetailListener listener) {
		EvaluationListener evaluationListener= new EvaluationListener(value, thread, listener);
		if (value instanceof IJavaObject && !(value instanceof IJavaArray)) {
			IJavaObject objectValue= (IJavaObject) value;
			try {
				IJavaDebugTarget debugTarget= (IJavaDebugTarget) thread.getDebugTarget();
				// get the compiled expression to use
				Expression expression= getCompiledExpression(objectValue, debugTarget, thread);
				if (expression != null) {
					expression.getEngine().evaluateExpression(expression.getExpression(), objectValue, thread,
							evaluationListener, DebugEvent.EVALUATION_IMPLICIT, false);
					return;
				}
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
				return;
			}
		}
		try {
			evaluationListener.valueToString(value);
		} catch (DebugException e) {
			listener.detailComputed(value, e.getStatus().getMessage());
		}
	}
	
	private IJavaProject getJavaProject(IJavaObject javaValue, IJavaThread thread) throws DebugException {

		
		ISourceLocator locator= javaValue.getLaunch().getSourceLocator();
		if (locator == null) {
			return null;
		}
		Object sourceElement= null;
		if (locator instanceof ISourceLookupDirector) {
			IJavaReferenceType type= (IJavaReferenceType)javaValue.getJavaType();
			if (type instanceof JDIReferenceType) {
				String[] sourcePaths= ((JDIReferenceType) type).getSourcePaths(null);
				if (sourcePaths.length > 0) {
					sourceElement= ((ISourceLookupDirector) locator).getSourceElement(sourcePaths[0]);
				}
			}
			if (!(sourceElement instanceof IJavaElement) && sourceElement instanceof IAdaptable) {
				sourceElement = ((IAdaptable)sourceElement).getAdapter(IJavaElement.class);
			}
		}
		if (sourceElement == null) {
			IStackFrame stackFrame= null;
			IJavaDebugTarget target = (IJavaDebugTarget)javaValue.getDebugTarget().getAdapter(IJavaDebugTarget.class);
			if (target != null) {
				stackFrame = EvaluationContextManager.getEvaluationContext((IWorkbenchWindow)null);
				if (stackFrame == null || !stackFrame.getDebugTarget().equals(target)) {
					stackFrame= thread.getTopStackFrame();
					if (stackFrame != null && !stackFrame.getDebugTarget().equals(target)) {
						stackFrame= null;
					}
				}
			}
			if (stackFrame == null) {
				return null;
			}
			sourceElement = locator.getSourceElement(stackFrame);
			if (!(sourceElement instanceof IJavaElement) && sourceElement instanceof IAdaptable) {
				sourceElement = ((IAdaptable)sourceElement).getAdapter(IJavaElement.class);
			}
		}
		IJavaProject project= null;
		if (sourceElement instanceof IJavaElement) {
			project= ((IJavaElement) sourceElement).getJavaProject();
		} else if (sourceElement instanceof IResource) {
			IJavaProject resourceProject = JavaCore.create(((IResource)sourceElement).getProject());
			if (resourceProject.exists()) {
				project= resourceProject;
			}
		}
		return project;
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
	 * the given type or one of its super types, super interfaces.
	 */
	private String getDetailFormatter(IJavaClassType type) throws DebugException {
		String snippet= getDetailFormatterSuperClass(type);
		if (snippet != null) {
			return snippet;
		}
		IJavaInterfaceType[] allInterfaces= type.getAllInterfaces();
		for (int i= 0; i < allInterfaces.length; i++) {
			DetailFormatter detailFormatter= (DetailFormatter)fDetailFormattersMap.get(allInterfaces[i].getName());
			if (detailFormatter != null && detailFormatter.isEnabled()) {
				return detailFormatter.getSnippet();
			}
		}
		return null;
	}
	
	/**
	 * Return the detail formatter (code snippet) associate with
	 * the given type or one of its super types.
	 */
	private String getDetailFormatterSuperClass(IJavaClassType type) throws DebugException {
		if (type == null) {
			return null;
		}
		DetailFormatter detailFormatter= (DetailFormatter)fDetailFormattersMap.get(type.getName());
		if (detailFormatter != null && detailFormatter.isEnabled()) {
			return detailFormatter.getSnippet();
		}
		return getDetailFormatterSuperClass(type.getSuperclass());
	}
	
	/**
	 * Return the expression which corresponds to the code formatter associated with the type of
	 * the given object or <code>null</code> if none.
	 * 
	 * The code snippet is compiled in the context of the given object.
	 */
	private Expression getCompiledExpression(IJavaObject javaObject, IJavaDebugTarget debugTarget, IJavaThread thread) throws DebugException {
		IJavaClassType type= (IJavaClassType)javaObject.getJavaType();
		String typeName= type.getName();
		Key key= new Key(typeName, debugTarget);
		if (fCacheMap.containsKey(key)) {
			return (Expression) fCacheMap.get(key);
		}
		IJavaProject project= getJavaProject(javaObject, thread);
		if (project != null) {
			String snippet= getDetailFormatter(type);
			if (snippet != null) {
				IAstEvaluationEngine evaluationEngine= JDIDebugPlugin.getDefault().getEvaluationEngine(project, debugTarget);
				ICompiledExpression res= evaluationEngine.getCompiledExpression(snippet, javaObject);
				Expression exp = new Expression(res, evaluationEngine);
				fCacheMap.put(key, exp);
				return exp;
			}
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST) ||
				property.equals(IJDIPreferencesConstants.PREF_SHOW_DETAILS)) {
			populateDetailFormattersMap();
			fCacheMap.clear();
			// If a Java stack frame is selected in the Debug view, fire a change event on
			// it so the variables view will update for any formatter changes.
            IAdaptable selected = DebugUITools.getDebugContext();
            if (selected != null) {
                IJavaStackFrame frame= (IJavaStackFrame) selected.getAdapter(IJavaStackFrame.class);
                if (frame != null) {
                    DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { 
                            new DebugEvent(frame, DebugEvent.CHANGE)
                    });
                }
            }			
		}
	}
	/**
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource() instanceof IJavaDebugTarget && event.getKind() == DebugEvent.TERMINATE) {
				deleteCacheForTarget((IJavaDebugTarget) event.getSource());
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
				if (debugTargets[j] instanceof IJavaDebugTarget) {
					deleteCacheForTarget((IJavaDebugTarget)debugTargets[j]);		
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
	private synchronized void deleteCacheForTarget(IJavaDebugTarget debugTarget) {
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
		private IJavaDebugTarget fDebugTarget;
		
		Key(String typeName, IJavaDebugTarget debugTarget) {
			fTypeName= typeName;
			fDebugTarget= debugTarget;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				Key key= (Key) obj;
				return fTypeName != null && fDebugTarget != null && fTypeName.equals(key.fTypeName) && fDebugTarget.equals(key.fDebugTarget);
			}
			return false;
		}
		
		public int hashCode() {
			return fTypeName.hashCode() / 2 + fDebugTarget.hashCode() / 2;
		}
	}
	
	/**
	 * Stores a compiled expression and evaluation engine used to eval the expression.
	 */
	static private class Expression {
		private ICompiledExpression fExpression;
		private IAstEvaluationEngine fEngine;
		
		Expression(ICompiledExpression expression, IAstEvaluationEngine engine) {
			fExpression = expression;
			fEngine = engine;
		}
		public ICompiledExpression getExpression() {
			return fExpression;
		}
		public IAstEvaluationEngine getEngine() {
			return fEngine;
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
				StringBuffer error= new StringBuffer(DebugUIMessages.JavaDetailFormattersManager_Detail_formatter_error___1); //$NON-NLS-1$
				DebugException exception= result.getException();
				if (exception != null) {
					Throwable throwable= exception.getStatus().getException();
					error.append("\n\t\t"); //$NON-NLS-1$
					if (throwable instanceof InvocationException) {
						error.append(MessageFormat.format(DebugUIMessages.JavaDetailFormattersManager_An_exception_occurred___0__3, new String[] {((InvocationException) throwable).exception().referenceType().name()})); //$NON-NLS-1$
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
						result.append(DebugUIMessages.JavaDetailFormattersManager_null); //$NON-NLS-1$
					} else if (objectValue instanceof IJavaPrimitiveValue) {
						// no need to spawn a thread for a primitive value
						appendJDIPrimitiveValueString(result, objectValue);
					} else if (fThread == null || !fThread.isSuspended()) {
						// no thread available
						result.append(DebugUIMessages.JavaDetailFormattersManager_no_suspended_threads); //$NON-NLS-1$
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
		
		/*
		 * Tries to use Arrays.asList() on target because List has a better toString() to 
		 * display. If not possible (or if array is of a primitive type), appendArrayDetailIndividually
		 * is called.
		 */
		protected void appendArrayDetail(StringBuffer result, IJavaArray arrayValue) throws DebugException {
			IJavaType componentType = null;
			try {
				IJavaArrayType javaArrayType = (IJavaArrayType) arrayValue.getJavaType();
				componentType = javaArrayType.getComponentType();
			} catch (DebugException de) {
				if (de.getStatus().getException() instanceof ClassNotLoadedException) {
					result.append(DebugUIMessages.JavaDetailFormattersManager_0); //$NON-NLS-1$
				} else {
					JDIDebugUIPlugin.log(de);
					result.append(de.getStatus().getMessage());
				}
				return;	
			}
			
			if (!(componentType instanceof IJavaReferenceType)) {
				//if it is an array of primitives, cannot use Arrays.asList()
				appendArrayDetailIndividually(result, arrayValue);
				return;
			}
			
			IJavaDebugTarget target = (IJavaDebugTarget) arrayValue.getDebugTarget();
			
			//Load java.util.Arrays
			IJavaType[] types;
			try {
				types = target.getJavaTypes("java.lang.Class"); //$NON-NLS-1$
			} catch (DebugException de) {
				types = null;
			}
			
			if (types != null && types.length >0) {
				try {
					IJavaClassType type = (IJavaClassType) types[0];
					IJavaValue arg = target.newValue("java.util.Arrays"); //$NON-NLS-1$
					type.sendMessage("forName", "(Ljava/lang/String;)Ljava/lang/Class;", new IJavaValue[] {arg}, fThread);  //$NON-NLS-1$//$NON-NLS-2$
				} catch (DebugException de) {
					//java.util.Arrays didn't load properly. Can't use Arrays.asList()
					appendArrayDetailIndividually(result, arrayValue);
				}
			} else {
				//didn't get java.lang.Class, can't load java.utils.Arrays.
				appendArrayDetailIndividually(result, arrayValue);
			}
			
			types = null;
			types = target.getJavaTypes("java.util.Arrays"); //$NON-NLS-1$
			if (types != null && types.length >0) {
				IJavaClassType type = (IJavaClassType) types[0];
				IJavaObject javaObject;
				try {
					//execute Arrays.asList() on target
					javaObject = (IJavaObject) type.sendMessage("asList", "([Ljava/lang/Object;)Ljava/util/List;", new IJavaValue[] {arrayValue}, fThread); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (DebugException de) {
					//asList() failed.
					appendArrayDetailIndividually(result, arrayValue);
					return;
				}
				appendObjectDetail(result, javaObject);
			} else {
				// didn't get java.util.Arrays. Can't use asList() 
				appendArrayDetailIndividually(result, arrayValue);
			}
		}
		
		/*
		 * Gets all values in array and appends the toString() if it is an array of Objects or the value if primative.
		 * NB - this method is only called by appendArrayDetail which first tries to use Arrays.asList() to minimize
		 * toString() calls on remote target (ie one call to List.toString() instead of one call per item in the array). 
		 */
		private void appendArrayDetailIndividually(StringBuffer result, IJavaArray arrayValue) throws DebugException {
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
				result.append(DebugUIMessages.JavaDetailFormattersManager__unknown_); //$NON-NLS-1$
			} else {
				appendJDIValueString(result, toStringValue);
			}
		}
		
		
		
	}
	
	/**
	 * (non java-doc)
	 * Remove the provided <code>detailFormatter</code> from the map
	 * @param detailFormatter
	 */
	public void removeAssociatedDetailFormatter(DetailFormatter detailFormatter) {
		fDetailFormattersMap.remove(detailFormatter.getTypeName());
		savePreference();
	}
	
}
