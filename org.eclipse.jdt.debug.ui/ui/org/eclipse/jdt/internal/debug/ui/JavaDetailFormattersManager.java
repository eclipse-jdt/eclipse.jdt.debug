/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
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
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListValue;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.InvocationException;

/**
 * Generates strings for the detail pane of views displaying java elements.
 */
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
	private HashMap<String, DetailFormatter> fDetailFormattersMap;
	
	/**
	 * Cache of compiled expressions.
	 * Associate a pair type name/debug target to a compiled expression.
	 */
	private HashMap<Key, Expression> fCacheMap;
	
	/**
	 * JavaDetailFormattersManager constructor.
	 */
	private JavaDetailFormattersManager() {
		populateDetailFormattersMap();
		JDIDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
		DebugUITools.getPreferenceStore().addPropertyChangeListener(this);
		fCacheMap= new HashMap<Key, Expression>();
	}
	
	/**
	 * Populate the detail formatters map with data from preferences.
	 */
	private void populateDetailFormattersMap() {
		String[] detailFormattersList= JavaDebugOptionsManager.parseList(JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST));
		fDetailFormattersMap= new HashMap<String, DetailFormatter>(detailFormattersList.length / 3);
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
		thread.queueRunnable(new Runnable() {
			public void run() {
				resolveFormatter(objectValue, thread, listener);
			}
		});
	}
	
	private void resolveFormatter(final IJavaValue value, final IJavaThread thread, final IValueDetailListener listener) {
		EvaluationListener evaluationListener= new EvaluationListener(value, thread, listener);
		if (value instanceof IJavaObject) {
			IJavaObject objectValue= (IJavaObject) value;
			try {
				if(value instanceof JDIAllInstancesValue) {
					listener.detailComputed(value, ((JDIAllInstancesValue)value).getDetailString());
					return;
				}
				if(value instanceof JDIReferenceListValue) {
					listener.detailComputed(value, ((JDIReferenceListValue)value).getDetailString());
					return;
				}
				IJavaDebugTarget debugTarget= (IJavaDebugTarget) thread.getDebugTarget();
				// get the compiled expression to use
				Expression expression= getCompiledExpression(objectValue, debugTarget, thread);
				if (expression != null) {
					expression.getEngine().evaluateExpression(expression.getExpression(), objectValue, thread,
							evaluationListener, DebugEvent.EVALUATION_IMPLICIT, false);
					return;
				}
			} catch (CoreException e) {
				listener.detailComputed(value, e.toString());
				return;
			}
		}
		try {
			evaluationListener.valueToString(value);
		} catch (DebugException e) {
			String detail = e.getStatus().getMessage();
			if (e.getStatus().getException() instanceof UnsupportedOperationException) {
				detail = DebugUIMessages.JavaDetailFormattersManager_7;
			} else if (e.getStatus().getCode() == IJavaThread.ERR_INCOMPATIBLE_THREAD_STATE) {
				detail = DebugUIMessages.JavaDetailFormattersManager_6;
			}
			listener.detailComputed(value, detail);
		}
	}
	
	private IJavaProject getJavaProject(IJavaObject javaValue, IJavaThread thread) throws CoreException {

		IType type = null;
		if (javaValue instanceof IJavaArray) {
			IJavaArrayType arrType = (IJavaArrayType) javaValue.getJavaType();
			IJavaType compType = arrType.getComponentType();
			while (compType instanceof IJavaArrayType) {
				compType = ((IJavaArrayType)compType).getComponentType();
			}
			type = JavaDebugUtils.resolveType(compType);
		} else {
			type = JavaDebugUtils.resolveType(javaValue);
		}
		if (type != null) {
			return type.getJavaProject();
		}
		IStackFrame stackFrame= null;
		IJavaDebugTarget target = (IJavaDebugTarget)javaValue.getDebugTarget().getAdapter(IJavaDebugTarget.class);
		if (target != null) {
			stackFrame= thread.getTopStackFrame();
			if (stackFrame != null && !stackFrame.getDebugTarget().equals(target)) {
				stackFrame= null;
			}
		}
		if (stackFrame == null) {
			return null;
		}
		Object sourceElement = JavaDebugUtils.resolveSourceElement(stackFrame, stackFrame.getLaunch());
		if (!(sourceElement instanceof IJavaElement) && sourceElement instanceof IAdaptable) {
			sourceElement = ((IAdaptable)sourceElement).getAdapter(IJavaElement.class);
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
	
	/**
	 * Searches the listing of implemented interfaces to see if one of them has a detail formatter
	 * @param type the type whose interfaces you want to inspect
	 * @return an associated details formatter of <code>null</code> if none is found
	 * @since 3.2
	 */
	public DetailFormatter getDetailFormatterFromInterface(IJavaClassType type) {
		try {
			IJavaInterfaceType[] inter = type.getAllInterfaces();
			Object formatter = null;
			for (int i = 0; i < inter.length; i++) {
				formatter = fDetailFormattersMap.get(inter[i].getName());
				if(formatter != null) {
					return (DetailFormatter) formatter;
				}
			}
			return null;
			}
		catch(DebugException e) {return null;}
	}
	
	/**
	 * Returns if the specified <code>IJavaType</code> has a detail formatter on one of its interfaces
	 * @param type the type to inspect
	 * @return true if there is an existing detail formatter on one of the types' interfaces, false otherwise
	 * @since 3.2
	 */
	public boolean hasInterfaceDetailFormatter(IJavaType type) {
		if(type instanceof IJavaClassType) {
			return getDetailFormatterFromInterface((IJavaClassType) type) != null;
		}
		return false;
	}
	
	/**
	 * Searches the superclass hierarchy to see if any of the specified classes parents have a detail formatter
	 * @param type the current type. Ideally this should be the first parent class.
	 * @return the first detail formatter located walking up the superclass hierarchy or <code>null</code> if none are found
	 * @since 3.2
	 */
	public DetailFormatter getDetailFormatterFromSuperclass(IJavaClassType type) {
		try {
			if(type == null) {
				return null;
			}
			DetailFormatter formatter = fDetailFormattersMap.get(type.getName());
			if(formatter != null && formatter.isEnabled()) {
				return formatter;
			}
			return getDetailFormatterFromSuperclass(type.getSuperclass());
			}
		catch(DebugException e) {return null;}
	}
	
	/**
	 * Returns if one of the parent classes of the specified type has a detail formatter
	 * @param type the type to inspect
	 * @return true if one of the parent classes of the type has a detail formatter, false otherwise
	 * @since 3.2
	 */
	public boolean hasSuperclassDetailFormatter(IJavaType type) {
		if(type instanceof IJavaClassType) {
			return getDetailFormatterFromSuperclass((IJavaClassType) type) != null;
		}
		return false;
	}
	
	public boolean hasAssociatedDetailFormatter(IJavaType type) {
		return getAssociatedDetailFormatter(type) != null;
	}
	
	public DetailFormatter getAssociatedDetailFormatter(IJavaType type) {
		String typeName = ""; //$NON-NLS-1$
		try {
			while (type instanceof IJavaArrayType) {
				type = ((IJavaArrayType)type).getComponentType();
			}
			if (type instanceof IJavaClassType) {
				typeName = type.getName();
			} 
			else {
				return null;
			}
		} 
		catch (DebugException e) {return null;}
		return fDetailFormattersMap.get(typeName);
	}
	
	public void setAssociatedDetailFormatter(DetailFormatter detailFormatter) {
		fDetailFormattersMap.put(detailFormatter.getTypeName(), detailFormatter);
		savePreference();
	}
	
	
	private void savePreference() {
		Collection<DetailFormatter> valuesList= fDetailFormattersMap.values();
		String[] values= new String[valuesList.size() * 3];
		int i= 0;
		for (Iterator<DetailFormatter> iter= valuesList.iterator(); iter.hasNext();) {
			DetailFormatter detailFormatter= iter.next();
			values[i++]= detailFormatter.getTypeName();
			values[i++]= detailFormatter.getSnippet().replace(',','\u0000');
			values[i++]= detailFormatter.isEnabled() ? JavaDetailFormattersPreferencePage.DETAIL_FORMATTER_IS_ENABLED : JavaDetailFormattersPreferencePage.DETAIL_FORMATTER_IS_DISABLED;
		}
		String pref = JavaDebugOptionsManager.serializeList(values);
		JDIDebugUIPlugin.getDefault().getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST, pref);
	}
	
	/**
	 * Return the detail formatter (code snippet) associate with
	 * the given type or one of its super types, super interfaces.
	 * @param type the class type
	 * @return the code snippet for the given type / super type / super interface
	 * @throws DebugException if there is  problem computing the snippet
	 */
	private String getDetailFormatter(IJavaClassType type) throws DebugException {
		String snippet= getDetailFormatterSuperClass(type);
		if (snippet != null) {
			return snippet;
		}
		IJavaInterfaceType[] allInterfaces= type.getAllInterfaces();
		for (int i= 0; i < allInterfaces.length; i++) {
			DetailFormatter detailFormatter= fDetailFormattersMap.get(allInterfaces[i].getName());
			if (detailFormatter != null && detailFormatter.isEnabled()) {
				return detailFormatter.getSnippet();
			}
		}
		return null;
	}
	
	/**
	 * Return the detail formatter (code snippet) associate with
	 * the given type or one of its super types.
	 * @param type the class type
	 * @return the snippet for the given class / super class
	 * @throws DebugException if there is a problem computing the snippet
	 */
	private String getDetailFormatterSuperClass(IJavaClassType type) throws DebugException {
		if (type == null) {
			return null;
		}
		DetailFormatter detailFormatter= fDetailFormattersMap.get(type.getName());
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
	 * @param javaObject the Java object
	 * @param debugTarget the target
	 * @param thread the thread context
	 * @return the compiled expression to be evaluated
	 * @throws CoreException is a problem occurs compiling the expression
	 */
	private Expression getCompiledExpression(IJavaObject javaObject, IJavaDebugTarget debugTarget, IJavaThread thread) throws CoreException {
		IJavaType type = javaObject.getJavaType();
		if (type == null) {
			return null;
		}
		String typeName = type.getName();
		Key key = new Key(typeName, debugTarget);
		if (fCacheMap.containsKey(key)) {
			return fCacheMap.get(key);
		}
		String snippet = null;
		if (type instanceof IJavaClassType) {
			snippet = getDetailFormatter((IJavaClassType) type);
		} else if (type instanceof IJavaArrayType) {
			snippet = getArraySnippet((IJavaArray) javaObject);
		}
		if (snippet != null) {
			IJavaProject project = getJavaProject(javaObject, thread);
			if (project != null) {
				IAstEvaluationEngine evaluationEngine = JDIDebugPlugin
						.getDefault().getEvaluationEngine(project, debugTarget);
				ICompiledExpression res = evaluationEngine
						.getCompiledExpression(snippet, javaObject);
				if (res != null) {
					Expression exp = new Expression(res, evaluationEngine);
					fCacheMap.put(key, exp);
					return exp;
				}
			}
		}
		return null;
	}
	
	protected String getArraySnippet(IJavaArray value) throws DebugException {
		String signature = value.getSignature();
		int nesting = Signature.getArrayCount(signature);
		if (nesting > 1) {
			// for nested primitive arrays, print everything
			String sig = Signature.getElementType(signature);
			if (sig.length() == 1 || "Ljava/lang/String;".equals(sig)) { //$NON-NLS-1$
				// return null so we get to "valueToString(IJavaValue)" for primitive and string types
				return null;
			}
		}
		if (((IJavaArrayType)value.getJavaType()).getComponentType() instanceof IJavaReferenceType) {
			int length = value.getLength();
			// guestimate at max entries to print based on char/space/comma per entry
			int maxLength = getMaxDetailLength();
			if (maxLength > 0){
				int maxEntries = (maxLength / 3) + 1;
				if (length > maxEntries) {
					StringBuffer snippet = new StringBuffer();
					snippet.append("Object[] shorter = new Object["); //$NON-NLS-1$
					snippet.append(maxEntries);
					snippet.append("]; System.arraycopy(this, 0, shorter, 0, "); //$NON-NLS-1$
					snippet.append(maxEntries);
					snippet.append("); "); //$NON-NLS-1$
					snippet.append("return java.util.Arrays.asList(shorter).toString();"); //$NON-NLS-1$
					return snippet.toString();
				}
			}
			return "java.util.Arrays.asList(this).toString()"; //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST) ||
				property.equals(IJDIPreferencesConstants.PREF_SHOW_DETAILS) ||
				property.equals(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH)) {
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
	 * @param debugTarget the target
	 */
	private synchronized void deleteCacheForTarget(IJavaDebugTarget debugTarget) {
		for (Iterator<Key> iter= fCacheMap.keySet().iterator(); iter.hasNext();) {
			Key key= iter.next();
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
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				Key key= (Key) obj;
				return fTypeName != null && fDebugTarget != null && fTypeName.equals(key.fTypeName) && fDebugTarget.equals(key.fDebugTarget);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return fTypeName.hashCode() / 2 + fDebugTarget.hashCode() / 2;
		}
	}
	
	/**
	 * Stores a compiled expression and evaluation engine used to evaluate the expression.
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
	 * Utilizes the 'standard' pretty printer methods to return the result.
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
		
		/**
		 * Signature of a string object
		 */
		private static final String STRING_SIGNATURE = "Ljava/lang/String;"; //$NON-NLS-1$
		
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
				StringBuffer error= new StringBuffer(DebugUIMessages.JavaDetailFormattersManager_Detail_formatter_error___1); 
				DebugException exception= result.getException();
				if (exception != null) {
					Throwable throwable= exception.getStatus().getException();
					error.append("\n\t\t"); //$NON-NLS-1$
					if (throwable instanceof InvocationException) {
						error.append(NLS.bind(DebugUIMessages.JavaDetailFormattersManager_An_exception_occurred___0__3, new String[] {((InvocationException) throwable).exception().referenceType().name()}));
					} else if (throwable instanceof UnsupportedOperationException) {
						error = new StringBuffer();
						error.append(DebugUIMessages.JavaDetailFormattersManager_7);
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
			String nonEvalResult = null;
			StringBuffer result= null;
			if (objectValue.getSignature() == null) {
				// no need to spawn evaluate for a null fValue
				nonEvalResult = DebugUIMessages.JavaDetailFormattersManager_null; 
			} else if (objectValue instanceof IJavaPrimitiveValue) {
				// no need to spawn evaluate for a primitive value
				result = new StringBuffer();
				appendJDIPrimitiveValueString(result, objectValue);
			} else if (fThread == null || !fThread.isSuspended()) {
				// no thread available
				result = new StringBuffer();
				result.append(DebugUIMessages.JavaDetailFormattersManager_no_suspended_threads); 
				appendJDIValueString(result, objectValue);
			} else if (objectValue instanceof IJavaObject && STRING_SIGNATURE.equals(objectValue.getSignature())) {
				// no need to spawn evaluate for a java.lang.String
				result = new StringBuffer();
				appendJDIValueString(result, objectValue);
			}
			if (result != null) {
				nonEvalResult = result.toString();
			}
			if (nonEvalResult != null) {
				fListener.detailComputed(fValue, nonEvalResult);
				return;
			}
			
			IEvaluationRunnable eval = new IEvaluationRunnable() {
				public void run(IJavaThread thread, IProgressMonitor monitor) throws DebugException {
					StringBuffer buf= new StringBuffer();
					if (objectValue instanceof IJavaArray) {
						appendArrayDetail(buf, (IJavaArray) objectValue);
					} else if (objectValue instanceof IJavaObject) {
						appendObjectDetail(buf, (IJavaObject) objectValue);
					} else {
						appendJDIValueString(buf, objectValue);
					}
					fListener.detailComputed(fValue, buf.toString());
				}
			};
			fThread.runEvaluation(eval, null, DebugEvent.EVALUATION_IMPLICIT, false);
		}
		
		/*
		 * Gets all values in array and appends the toString() if it is an array of Objects or the value if primitive.
		 * NB - this method is only called if there is no compiled expression for an array to perform an
		 * Arrays.asList().toString() to minimize toString() calls on remote target (i.e. one call to
		 * List.toString() instead of one call per item in the array). 
		 */
		protected void appendArrayDetail(StringBuffer result, IJavaArray arrayValue) throws DebugException {
			result.append('[');
			boolean partial = false;
			IJavaValue[] arrayValues = null;
			int maxLength = getMaxDetailLength();
			int maxEntries = (maxLength / 3) + 1; // guess at char/comma/space per entry
			int length = -1;
			try {
				length = arrayValue.getLength();
				if (maxLength > 0 && length > maxEntries) {
					partial = true;
					IVariable[] variables = arrayValue.getVariables(0, maxEntries);
					arrayValues = new IJavaValue[variables.length];
					for (int i = 0; i < variables.length; i++) {
						arrayValues[i] = (IJavaValue) variables[i].getValue();
					}
				} else {
					arrayValues= arrayValue.getValues();
				}
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
				if (partial && result.length() > maxLength) {
					break;
				}
			}
			if (!partial) {
				result.append(']');
			}
		}
		
		protected void appendJDIPrimitiveValueString(StringBuffer result, IJavaValue value) throws DebugException {
			result.append(value.getValueString());
		}
		
		
		protected void appendJDIValueString(StringBuffer result, IJavaValue value) throws DebugException {
			result.append(value.getValueString());
		}
		
		
		protected void appendObjectDetail(StringBuffer result, IJavaObject objectValue) throws DebugException {
			if(objectValue instanceof JDINullValue) {
				appendJDIValueString(result, objectValue);
				return;
			}
			// optimize if the result is a string - no need to send toString to a string
			if (STRING_SIGNATURE.equals(objectValue.getSignature())) {
				appendJDIValueString(result, objectValue);
			} else {
				
				IJavaValue toStringValue= objectValue.sendMessage(EvaluationListener.fgToString, EvaluationListener.fgToStringSignature, null, fThread, false);
				if (toStringValue == null) {
					result.append(DebugUIMessages.JavaDetailFormattersManager__unknown_); 
				} else {
					appendJDIValueString(result, toStringValue);
				}
			}
		}
		
		
		
	}
	
	/**
	 * (non java-doc)
	 * Remove the provided <code>detailFormatter</code> from the map
	 * @param detailFormatter the detail formatter
	 */
	public void removeAssociatedDetailFormatter(DetailFormatter detailFormatter) {
		fDetailFormattersMap.remove(detailFormatter.getTypeName());
		savePreference();
	}
	
	/**
	 * Returns the maximum number of chars to display in the details area or 0 if
	 * there is no maximum.
	 * 
	 * @return maximum number of chars to display or 0 for no max
	 */
	private static int getMaxDetailLength() {
		return DebugUITools.getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH);
	}
	
}
