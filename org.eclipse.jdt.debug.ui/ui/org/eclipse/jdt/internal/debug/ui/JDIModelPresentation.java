package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.launching.sourcelookup.LocalFileStorage;
import org.eclipse.jdt.launching.sourcelookup.ZipEntryStorage;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;

/**
 * @see IDebugModelPresentation
 */
public class JDIModelPresentation extends LabelProvider implements IDebugModelPresentation, IDebugEventListener {

	/**
	 * Qualified names presentation property (value <code>"org.eclipse.debug.ui.displayQualifiedNames"</code>).
	 * When <code>DISPLAY_QUALIFIED_NAMES</code> is set to <code>True</code>,
	 * this label provider should use fully qualified type names when rendering elements.
	 * When set to <code>False</code>,this label provider should use simple names
	 * when rendering elements.
	 * @see #setAttribute(String, Object)
	 */
	public final static String DISPLAY_QUALIFIED_NAMES= "DISPLAY_QUALIFIED_NAMES"; //$NON-NLS-1$
	
	protected HashMap fAttributes= new HashMap(3);

	static final Point BIG_SIZE= new Point(22, 16);
	protected ImageDescriptorRegistry fJavaElementImageRegistry= JavaPlugin.getImageDescriptorRegistry();
	protected ImageDescriptorRegistry fDebugImageRegistry= JDIDebugUIPlugin.getImageDescriptorRegistry();

	protected static final String fgStringName= "java.lang.String"; //$NON-NLS-1$
	
	/**
	 * The signature of <code>java.lang.Object#toString()</code>,
	 * used to evaluate 'toString()' for displaying details of values.
	 */
	private static final String fgToStringSignature = "()Ljava/lang/String;"; //$NON-NLS-1$
	/**
	 * The selector of <code>java.lang.Object#toString()</code>,
	 * used to evaluate 'toString()' for displaying details of values.
	 */
	private static final String fgToString = "toString"; //$NON-NLS-1$

	protected JavaElementLabelProvider fJavaLabelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
	
	/**
	 * A pool of threads per VM that are suspended. The
	 * table is updated as threads suspend/resume. Used
	 * to perform 'toString' evaluations. Keys are debug
	 * targets, and values are <code>List</code>s of
	 * threads.
	 */
	private Hashtable fThreadPool; 

	public JDIModelPresentation() {
		super();
	}
	
	/**
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		disposeThreadPool();
	}
	
	/**
	 * If the thread pool was used, it is disposed, and this
	 * presentation is removed as a debug event listener.
	 */
	protected void disposeThreadPool() {
		if (fThreadPool != null) {
			DebugPlugin.getDefault().removeDebugEventListener(this);
			fThreadPool.clear();
		}
	}

	/**
	 * Initializes the thread pool with all suspended Java
	 * threads. Registers this presentation as a debug event
	 * handler.
	 */
	protected void initializeThreadPool() {
		fThreadPool = new Hashtable();
		IDebugTarget[] targets= DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
		for (int i = 0; i < targets.length; i++) {
			if (targets[i] instanceof IJavaDebugTarget) {
				List suspended = new ArrayList();
				fThreadPool.put(targets[i], suspended);
				try {
					IThread[] threads = targets[i].getThreads();
					for (int j = 0; j < threads.length; j++) {
						if (threads[j].isSuspended()) {
							suspended.add(threads[j]);
						}
					}
				} catch (DebugException e) {
					JDIDebugUIPlugin.logError(e);
				}
			}
		}
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/**
	 * @see IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		IJavaThread thread = getEvaluationThread((IJavaDebugTarget)value.getDebugTarget());
		try {
			DefaultJavaValueDetailProvider detailProvider = new DefaultJavaValueDetailProvider();
			detailProvider.computeDetail(value, thread, listener);
		} catch (DebugException de) {
			JDIDebugUIPlugin.logError(de);
		}
	}
	
	/**
	 * Returns the "toString" of the given value
	 * 
	 * @see IDebugModelPresentation#getDetail(IValue)
	 */
	/*
	public String getDetail(IValue v) {
		if (v instanceof IJavaValue) {
			// get a thread for an evaluation
			IJavaValue value = (IJavaValue)v;
			IJavaThread thread = getEvaluationThread((IJavaDebugTarget)value.getDebugTarget());
			if (thread != null) {
				try {
					return evaluateToString(value, thread);
				} catch (DebugException e) {
					// return the exception's message
					return e.getStatus().getMessage();
				}
			}
		}
		return null;
	}
	*/
	
	/**
	 * Returns a thread from the specified VM that can be
	 * used for an evaluationm or <code>null</code> if
	 * none.
	 * <p>
	 * This presentation maintains a pool of suspended
	 * threads per VM. Any suspended thread in the same
	 * VM may be used. The pool is lazily initialized on
	 * the first call to this method.
	 * </p>
	 * 
	 * @param debug target the target in which a thread is 
	 * 	required
	 * @return thread or <code>null</code>
	 */
	protected IJavaThread getEvaluationThread(IJavaDebugTarget target) {
		if (fThreadPool == null) {
			initializeThreadPool();
		}
		List threads = (List)fThreadPool.get(target);
		if (threads != null && !threads.isEmpty()) {
			return (IJavaThread)threads.get(0);
		}
		return null;
	}
	
	/**
	 * Returns the result of sending 'toString' to the given
	 * value (unless the value is null or is a primitive). If the
	 * evaluation takes > 3 seconds, this method returns.
	 * 
	 * @param value the value to send the message 'toString'
	 * @param thread the thread in which to perform the message
	 *  send
	 * @return the result of sending 'toString', as a String
	 * @exception DebugException if thrown by a model element
	 */
	/*
	protected synchronized String evaluateToString(final IJavaValue value, final IJavaThread thread) throws DebugException {
		if (value == null) {
			return "null";
		}
		final IJavaObject object;
		if (value instanceof IJavaObject) {
			object = (IJavaObject)value;
		} else {
			object = null;
		}
		if (object == null || !thread.isSuspended()) {
			// primitive or thread is no longer suspended
			return value.getValueString();
		}
		
		final IJavaValue[] toString = new IJavaValue[1];
		final DebugException[] ex = new DebugException[1];
		Runnable eval= new Runnable() {
			public void run() {
				try {
					toString[0] = object.sendMessage(JDIModelPresentation.fgToString, JDIModelPresentation.fgToStringSignature, null, thread, false);
				} catch (DebugException e) {
					ex[0]= e;
				}					
				synchronized (JDIModelPresentation.this) {
					JDIModelPresentation.this.notifyAll();
				}
			}
		};
		
		Thread evalThread = new Thread(eval);
		evalThread.start();
		try {
			wait(3000);
		} catch (InterruptedException e) {
		}
		
		if (ex[0] != null) {
			throw ex[0];
		}
		
		if (toString[0] != null) {
			return toString[0].getValueString();
		}	
		
		return "Error: timeout evaluating #toString()";
	}
	*/
			
	/**
	 * @see IDebugModelPresentation#getText(Object)
	 */
	public String getText(Object item) {
		try {
			boolean showQualified= isShowQualifiedNames();
			if (item instanceof IJavaVariable) {
				return getVariableText((IJavaVariable) item);
			} else if (item instanceof IStackFrame) {
				StringBuffer label= new StringBuffer(getStackFrameText((IStackFrame) item));
				if (item instanceof IJavaStackFrame) {
					if (((IJavaStackFrame)item).isOutOfSynch()) {
						label.append(DebugUIMessages.getString("JDIModelPresentation._(out_of_synch)_1")); //$NON-NLS-1$
					}
				}
				return label.toString();
			} else if (item instanceof IMarker) {
				IBreakpoint breakpoint = getBreakpoint((IMarker)item);
				if (breakpoint != null) {
					return getBreakpointText(breakpoint);
				}
				return null;
			} else if (item instanceof IBreakpoint) {
				return getBreakpointText((IBreakpoint)item);
			} else if (item instanceof IExpression) {
				return getExpressionText((IExpression)item);
			} else {
				StringBuffer label= new StringBuffer();
				if (item instanceof IJavaThread) {
					label.append(getThreadText((IJavaThread) item, showQualified));
					if (((IJavaThread)item).isOutOfSynch()) {
						label.append(DebugUIMessages.getString("JDIModelPresentation._(out_of_synch)_1")); //$NON-NLS-1$
					} else if (((IJavaThread)item).mayBeOutOfSynch()) {
						label.append(DebugUIMessages.getString("JDIModelPresentation._(may_be_out_of_synch)_2")); //$NON-NLS-1$
					}
				} else if (item instanceof IJavaDebugTarget) {
					label.append(getDebugTargetText((IJavaDebugTarget) item, showQualified));
					if (((IJavaDebugTarget)item).isOutOfSynch()) {
						label.append(DebugUIMessages.getString("JDIModelPresentation._(out_of_synch)_1")); //$NON-NLS-1$
					} else if (((IJavaDebugTarget)item).mayBeOutOfSynch()) {
						label.append(DebugUIMessages.getString("JDIModelPresentation._(may_be_out_of_synch)_2")); //$NON-NLS-1$
					}
				} else if (item instanceof IJavaValue) {
					label.append(getValueText((IJavaValue) item));
				}
				if (item instanceof ITerminate) {
					if (((ITerminate) item).isTerminated()) {
						label.insert(0, DebugUIMessages.getString("JDIModelPresentation.<terminated>_2")); //$NON-NLS-1$
						return label.toString();
					}
				}
				if (item instanceof IDisconnect) {
					if (((IDisconnect) item).isDisconnected()) {
						label.insert(0, DebugUIMessages.getString("JDIModelPresentation.<disconnected>_4")); //$NON-NLS-1$
						return label.toString();
					}
				}
				if (label.length() > 0) {
					return label.toString();
				}
			}
		} catch (CoreException e) {
			return DebugUIMessages.getString("JDIModelPresentation.<not_responding>_6"); //$NON-NLS-1$
		}
		return null;
	}

	protected IBreakpoint getBreakpoint(IMarker marker) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
		}
	
	/**
	 * Build the text for an IJavaThread.
	 */
	protected String getThreadText(IJavaThread thread, boolean qualified) throws CoreException {
		if (thread.isTerminated()) {
			if (thread.isSystemThread()) {
				return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[({0}]_(Terminated)_7"), thread.getName()); //$NON-NLS-1$
			} else {
				return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[({0}]_(Terminated)_8"), thread.getName()); //$NON-NLS-1$
			}
		}
		if (thread.isStepping()) {
			if (thread.isSystemThread()) {
				return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Stepping)_9"), thread.getName()); //$NON-NLS-1$
			} else {
				return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Stepping)_10"), thread.getName()); //$NON-NLS-1$
			}
		}
		if (!thread.isSuspended()) {
			if (thread.isSystemThread()) {
				return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Running)_11"), thread.getName()); //$NON-NLS-1$
			} else {
				return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Running)_12"), thread.getName()); //$NON-NLS-1$
			}
		}
		IJavaBreakpoint breakpoint= (IJavaBreakpoint)thread.getBreakpoint();
		if (breakpoint != null) {
			String typeName= getMarkerTypeName(breakpoint, qualified);
			if (BreakpointUtils.isProblemBreakpoint(breakpoint)) {
				String message = breakpoint.getMarker().getAttribute(JavaDebugOptionsManager.ATTR_PROBLEM_MESSAGE, DebugUIMessages.getString("JDIModelPresentation.Compilation_error_1")); //$NON-NLS-1$
				if (thread.isSystemThread()) {
					return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Suspended_({1}))_2"), new String[] {thread.getName(), message}); //$NON-NLS-1$
				} else {
					return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Suspended_({1}))_3"), new String[] {thread.getName(), message}); //$NON-NLS-1$
				}
			}			
			if (breakpoint instanceof IJavaExceptionBreakpoint) {
				String exName = ((IJavaExceptionBreakpoint)breakpoint).getExceptionTypeName();
				if (exName == null) {
					exName = typeName;
				} else if (!qualified) {
					int index = exName.lastIndexOf('.');
					exName = exName.substring(index + 1);
				} 
				if (thread.isSystemThread()) {
					return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Suspended_(exception_{1}))_13"), new String[] {thread.getName(), exName}); //$NON-NLS-1$
				} else {
					return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Suspended_(exception_{1}))_14"), new String[] {thread.getName(), exName}); //$NON-NLS-1$
				}
			}
			if (breakpoint instanceof IJavaWatchpoint) {
				IJavaWatchpoint wp = (IJavaWatchpoint)breakpoint;
				String fieldName = wp.getFieldName(); //$NON-NLS-1$
				if (wp.isAccessSuspend(thread.getDebugTarget())) {
					if (thread.isSystemThread()) {
						return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Suspended_(access_of_field_{1}_in_{2}))_16"), new String[] {thread.getName(), fieldName, typeName}); //$NON-NLS-1$
					} else {
						return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Suspended_(access_of_field_{1}_in_{2}))_17"), new String[] {thread.getName(), fieldName, typeName}); //$NON-NLS-1$
					}
				} else {
					// modification
					if (thread.isSystemThread()) {
						return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Suspended_(modification_of_field_{1}_in_{2}))_18"), new String[] {thread.getName(), fieldName, typeName}); //$NON-NLS-1$
					} else {
						return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Suspended_(modification_of_field_{1}_in_{2}))_19"), new String[] {thread.getName(), fieldName, typeName}); //$NON-NLS-1$
					}
				}
			}
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IJavaMethodBreakpoint me= (IJavaMethodBreakpoint)breakpoint;
				String methodName= me.getMethodName();
				if (me.isEntrySuspend(thread.getDebugTarget())) {
					if (thread.isSystemThread()) {
						return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Suspended_(entry_into_method_{1}_in_{2}))_21"), new String[] {thread.getName(), methodName, typeName}); //$NON-NLS-1$
					} else {
						return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Suspended_(entry_into_method_{1}_in_{2}))_22"), new String[] {thread.getName(), methodName, typeName}); //$NON-NLS-1$
					}
				} else {
					if (thread.isSystemThread()) {
						return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Suspended_(exit_of_method_{1}_in_{2}))_21"), new String[] {thread.getName(), methodName, typeName}); //$NON-NLS-1$
					} else {
						return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Suspended_(exit_of_method_{1}_in_{2}))_22"), new String[] {thread.getName(), methodName, typeName}); //$NON-NLS-1$
					}					
				}
			}
			if (breakpoint instanceof IJavaLineBreakpoint) {
				IJavaLineBreakpoint jlbp = (IJavaLineBreakpoint)breakpoint;
				int lineNumber= jlbp.getLineNumber();
				if (lineNumber > -1) {
					if (thread.isSystemThread()) {
						if (BreakpointUtils.isRunToLineBreakpoint(jlbp)) {
							return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Suspended_(run_to_line_{1}_in_{2}))_23"), new String[] {thread.getName(), String.valueOf(lineNumber), typeName}); //$NON-NLS-1$
						} else {
							return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Suspended_(breakpoint_at_line_{1}_in_{2}))_24"), new String[] {thread.getName(), String.valueOf(lineNumber), typeName}); //$NON-NLS-1$
						}
					} else {
						if (BreakpointUtils.isRunToLineBreakpoint(jlbp)) {
							return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Suspended_(run_to_line_{1}_in_{2}))_25"), new String[] {thread.getName(), String.valueOf(lineNumber), typeName}); //$NON-NLS-1$
						} else {
							return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Suspended_(breakpoint_at_line_{1}_in_{2}))_26"), new String[] {thread.getName(), String.valueOf(lineNumber), typeName}); //$NON-NLS-1$
						}
					}
				}
			}
		}

		// Otherwise, it's just suspended
		if (thread.isSystemThread()) {
			return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Suspended)_27"), thread.getName()); //$NON-NLS-1$
		} else {
			return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Suspended)_28"), thread.getName()); //$NON-NLS-1$
		}
	}

	/**
	 * Build the text for an IJavaDebugTarget.
	 */
	protected String getDebugTargetText(IJavaDebugTarget debugTarget, boolean qualified) throws DebugException {
		String labelString= debugTarget.getName();
		if (!qualified) {
			int index= labelString.lastIndexOf('.');
			if (index != -1) {
				labelString= labelString.substring(index + 1);
			}
		}
		if (debugTarget.isSuspended()) {
			labelString += DebugUIMessages.getString("JDIModelPresentation.target_suspended"); //$NON-NLS-1$
		}
		return labelString;
	}

	/**
	 * Build the text for an IJavaValue.
	 */
	protected String getValueText(IJavaValue value) throws DebugException {
		IPreferenceStore store= JDIDebugUIPlugin.getDefault().getPreferenceStore();
		boolean showHexValues= store.getBoolean(IJDIPreferencesConstants.SHOW_HEX_VALUES);		
		boolean showCharValues= store.getBoolean(IJDIPreferencesConstants.SHOW_CHAR_VALUES);
		boolean showUnsignedValues= store.getBoolean(IJDIPreferencesConstants.SHOW_UNSIGNED_VALUES);
		
		String refTypeName= value.getReferenceTypeName();
		String valueString= value.getValueString();
		boolean isString= refTypeName.equals(fgStringName);
		IJavaType type= value.getJavaType();
		String signature= null;
		if (type != null) {
			signature= type.getSignature();
		}
		if ("V".equals(signature)) { //$NON-NLS-1$
			valueString= DebugUIMessages.getString("JDIModelPresentation.(No_explicit_return_value)_30"); //$NON-NLS-1$
		}
		boolean isObject= isObjectValue(signature);
		boolean isArray= value instanceof IJavaArray;
		StringBuffer buffer= new StringBuffer();
		// Always show type name for objects & arrays (but not Strings)
		if ((isObject || isArray) && !isString && (refTypeName.length() > 0)) {
			String qualTypeName= getQualifiedName(refTypeName);
			if (isArray) {
				qualTypeName= adjustTypeNameForArrayIndex(qualTypeName, ((IJavaArray)value).getLength());
			}
			buffer.append(qualTypeName);
			buffer.append(' ');
		}
		
		// Put double quotes around Strings
		if (valueString != null && (isString || valueString.length() > 0)) {
			if (isString) {
				buffer.append('"');
			}
			buffer.append(valueString);
			if (isString) {
				buffer.append('"');
			}
		}
		
		// show unsigned value second, if applicable
		if (showUnsignedValues) {
			buffer= appendUnsignedText(value, buffer);
		}
		// show hex value third, if applicable
		if (showHexValues) {
			buffer= appendHexText(value, buffer);
		}
		// show byte character value last, if applicable
		if (showCharValues) {
			buffer= appendCharText(value, buffer);
		}
		
		return buffer.toString();
	}
	

	private StringBuffer appendUnsignedText(IJavaValue value, StringBuffer buffer) throws DebugException {
		String unsignedText= getValueUnsignedText(value);
		if (unsignedText != null) {
			buffer.append(" ["); //$NON-NLS-1$
			buffer.append(unsignedText);
			buffer.append("]"); //$NON-NLS-1$
		}
		return buffer;	
	}
		
	private StringBuffer appendHexText(IJavaValue value, StringBuffer buffer) throws DebugException {
		String hexText = getValueHexText(value);
		if (hexText != null) {
			buffer.append(" ["); //$NON-NLS-1$
			buffer.append(hexText);
			buffer.append("]"); //$NON-NLS-1$
		}		
		return buffer;
	}
	
	private StringBuffer appendCharText(IJavaValue value, StringBuffer buffer) throws DebugException {
		String charText= getValueCharText(value);
		if (charText != null) {
			buffer.append(" ["); //$NON-NLS-1$
			buffer.append(charText);
			buffer.append("]"); //$NON-NLS-1$
		}		
		return buffer;
	}
	
	/**
	 * Fire a debug event
	 */
	public void fireEvent(DebugEvent event) {
		DebugPlugin.getDefault().fireDebugEvent(event);
	}	

	/**
	 * Given a JNI-style signature String for a IJavaValue, return true
	 * if the signature represents an Object or an array of Objects.
	 */
	protected boolean isObjectValue(String signature) {
		if (signature == null) {
			return false;
		}
		char sigChar= ' ';
		for (int i= 0; i < signature.length(); i++) {
			sigChar= signature.charAt(i);
			if (sigChar == '[') {
				continue;
			}
			break;
		}
		if ((sigChar == 'L') || (sigChar == 'Q')) {
			return true;
		}
		return false;
	}
	
	/**
	 * Given a JNI-style signature String for a IJavaValue, return true
	 * if the signature represents a ByteValue
	 */
	protected boolean isByteValue(String signature) {
		if (signature == null) {
			return false;
		}
		return signature.equals("B"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the type signature for this value if its type is primitive.  
	 * For non-primitive types, null is returned.
	 */
	protected String getPrimitiveValueTypeSignature(IJavaValue value) throws DebugException {
		IJavaType type= value.getJavaType();
		if (type != null) {
			String sig= type.getSignature();
			if (sig != null || sig.length() == 1) {
				return sig;
			}
		}
		return null;
	}
	/**
	 * Returns the character string of a byte or <code>null</code if
	 * the value can not be interpreted as a valid character.
	 */
	protected String getValueCharText(IJavaValue value) throws DebugException {
		String sig= getPrimitiveValueTypeSignature(value);
		if (sig == null) {
			return null;
		}
		String valueString= value.getValueString();
		int intValue= 0;	
		switch (sig.charAt(0)) {
			case 'B' : // byte
				intValue= Integer.parseInt(valueString);
				intValue= intValue & 0xFF; // Only lower 8 bits
				break;
			case 'I' : // int
				intValue= Integer.parseInt(valueString);
				if (intValue > 255 || intValue < 0) {
					return null;
				}
				break;
			case 'S' : // short
				intValue= Integer.parseInt(valueString);
				if (intValue > 255 || intValue < 0) {
					return null;
				}
				break;
			case 'J' :
				long longValue= Long.parseLong(valueString);
				if (longValue > 255 || longValue < 0) {
					// Out of character range
					return null;
				}
				intValue= (int) longValue;
				break;
			default :
				return null;
		};
		StringBuffer charText = new StringBuffer();
		if (Character.getType((char) intValue) == Character.CONTROL) {
			Character ctrl = new Character((char) (intValue + 64));
			charText.append('^'); //$NON-NLS-1$
			charText.append(ctrl);
			switch (intValue) { // common use
				case 0: charText.append(" (NUL)"); break; //$NON-NLS-1$
				case 8: charText.append(" (BS)"); break; //$NON-NLS-1$
				case 9: charText.append(" (TAB)"); break; //$NON-NLS-1$
				case 10: charText.append(" (LF)"); break; //$NON-NLS-1$
				case 13: charText.append(" (CR)"); break; //$NON-NLS-1$
				case 21: charText.append(" (NL)"); break; //$NON-NLS-1$
				case 27: charText.append(" (ESC)"); break; //$NON-NLS-1$
				case 127: charText.append(" (DEL)"); break; //$NON-NLS-1$
			}
		} else {
			charText.append(new Character((char)intValue));
		}
		return charText.toString();
	}

	protected String getMarkerTypeName(IJavaBreakpoint breakpoint, boolean qualified) throws CoreException {
		String typeName= null;
		if (breakpoint instanceof IJavaPatternBreakpoint) {
			typeName = breakpoint.getMarker().getResource().getName();
		} else {
			typeName = breakpoint.getTypeName();
		}
		if (!qualified) {
			int index= typeName.lastIndexOf('.');
			if (index != -1) {
				typeName= typeName.substring(index + 1);
			}
		}
		return typeName;
	}

	/**
	 * Maps a Java element to an appropriate image.
	 * 
	 * @see IDebugModelPresentation#getImage(Object)
	 */
	public Image getImage(Object item) {
		try {
			if (item instanceof IJavaVariable) {
				return getVariableImage((IAdaptable) item);
			}
			if (item instanceof IMarker) {
				IBreakpoint bp = getBreakpoint((IMarker)item);
				if (bp != null && bp instanceof IJavaBreakpoint) {
					return getBreakpointImage((IJavaBreakpoint)bp);
				}
			}
			if (item instanceof IJavaBreakpoint) {
				return getBreakpointImage((IJavaBreakpoint)item);
			}
			if (item instanceof IJavaStackFrame || item instanceof IJavaThread || item instanceof IJavaDebugTarget) {
				return getDebugElementImage(item);
			}
		} catch (CoreException e) {
		}
		return null;
	}

	protected Image getBreakpointImage(IJavaBreakpoint breakpoint) throws CoreException {
		if (breakpoint instanceof IJavaExceptionBreakpoint) {
			return getExceptionBreakpointImage((IJavaExceptionBreakpoint)breakpoint);
		} if (BreakpointUtils.isRunToLineBreakpoint((IJavaLineBreakpoint)breakpoint)) {
			return null;
		} else {
			return getJavaBreakpointImage(breakpoint);
		}
	}

	protected Image getExceptionBreakpointImage(IJavaExceptionBreakpoint exception) throws CoreException {
		if (!exception.isEnabled()) {
			return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED);
		} else if (exception.isChecked()) {
			return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_EXCEPTION);
		} else {
			return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_ERROR);
		}
	}

	protected Image getJavaBreakpointImage(IJavaBreakpoint breakpoint) throws CoreException {
		if (breakpoint instanceof IJavaMethodBreakpoint) {
			IJavaMethodBreakpoint mBreakpoint= (IJavaMethodBreakpoint)breakpoint;
			return getJavaMethodBreakpointImage(mBreakpoint);
		} else if (breakpoint instanceof IJavaWatchpoint) {
			IJavaWatchpoint watchpoint= (IJavaWatchpoint)breakpoint;
			return getJavaWatchpointImage(watchpoint);
		} else {
			int flags= computeBreakpointAdornmentFlags(breakpoint);
			JDIImageDescriptor descriptor= null;
			if (breakpoint.isEnabled()) {
				descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT), flags);
			} else {
				descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED), flags);
			}
			return fDebugImageRegistry.get(descriptor);
		}
	}

	protected Image getJavaMethodBreakpointImage(IJavaMethodBreakpoint mBreakpoint) throws CoreException {
		int flags= computeBreakpointAdornmentFlags(mBreakpoint);
		JDIImageDescriptor descriptor= null;
		if (mBreakpoint.isEnabled()) {
			descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT), flags);
		} else {
			descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED), flags);
		}
			
		return fDebugImageRegistry.get(descriptor);
	}
	
	protected Image getJavaWatchpointImage(IJavaWatchpoint watchpoint) throws CoreException {
		boolean enabled= watchpoint.isEnabled();
		if (watchpoint.isAccess()) {
			if (watchpoint.isModification()) {
				//access and modification
				if (enabled) {
					return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_WATCHPOINT_ENABLED);
				} else {
					return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_WATCHPOINT_DISABLED);
				}
			} else {
				if (enabled) {
					return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_ACCESS_WATCHPOINT_ENABLED);
				} else {
					return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_ACCESS_WATCHPOINT_DISABLED);
				}
			}
		} else if (watchpoint.isModification()) {
			if (enabled) {
				return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_MODIFICATION_WATCHPOINT_ENABLED);
			} else {
				return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_MODIFICATION_WATCHPOINT_DISABLED);
			}
		} else {
			//neither access nor modification
			return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_WATCHPOINT_DISABLED);
		}
	}
	
	protected Image getVariableImage(IAdaptable element) {
		JavaElementImageDescriptor descriptor= new JavaElementImageDescriptor(
			computeBaseImageDescriptor(element), computeAdornmentFlags(element), BIG_SIZE);

		return fJavaElementImageRegistry.get(descriptor);			
	}
	
	/**
	 * Returns the image associated with the given element or <code>null</code>
	 * if none is defined.
	 */
	protected Image getDebugElementImage(Object element) {
		ImageDescriptor image= DebugUITools.getDefaultImageDescriptor(element);
		if (image == null) {
			return null;
		}
		int flags= computeJDIAdornmentFlags(element);
		JDIImageDescriptor descriptor= new JDIImageDescriptor(image, flags);
		return fDebugImageRegistry.get(descriptor);
	}
	
	/**
	 * Returns the adornment flags for the given element.
	 * These flags are used to render appropriate overlay
	 * icons for the element.
	 */
	private int computeJDIAdornmentFlags(Object element) {
		try {
			if (element instanceof IJavaStackFrame) {
				if (((IJavaStackFrame)element).isOutOfSynch()) {
					return JDIImageDescriptor.IS_OUT_OF_SYNCH;
				}
			}
			if (element instanceof IJavaThread) {
				if (((IJavaThread)element).isOutOfSynch()) {
					return JDIImageDescriptor.IS_OUT_OF_SYNCH;
				}
				if (((IJavaThread)element).mayBeOutOfSynch()) {
					return JDIImageDescriptor.MAY_BE_OUT_OF_SYNCH;
				}
			}
			if (element instanceof IJavaDebugTarget) {
				if (((IJavaDebugTarget)element).isOutOfSynch()) {
					return JDIImageDescriptor.IS_OUT_OF_SYNCH;
				}
				if (((IJavaDebugTarget)element).mayBeOutOfSynch()) {
					return JDIImageDescriptor.MAY_BE_OUT_OF_SYNCH;
				}
			}
		} catch (DebugException exception) {
		}
		return 0;
	}
	
	/**
	 * Returns the adornment flags for the given breakpoint.
	 * These flags are used to render appropriate overlay
	 * icons for the breakpoint.
	 */
	private int computeBreakpointAdornmentFlags(IJavaBreakpoint breakpoint)  {
		int flags= 0;
		try {
			if (breakpoint.isEnabled()) {
				flags |= JDIImageDescriptor.ENABLED;
			}
			if (breakpoint.isInstalled()) {
				flags |= JDIImageDescriptor.INSTALLED;
			}
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IJavaMethodBreakpoint mBreakpoint= (IJavaMethodBreakpoint)breakpoint;
				if (mBreakpoint.isEntry()) {
					flags |= JDIImageDescriptor.ENTRY;
				}
				if (mBreakpoint.isExit()) {
					flags |= JDIImageDescriptor.EXIT;
				}
			}
		} catch (CoreException exception) {
			JDIDebugUIPlugin.logError(exception);
		}
		return flags;
	}
	
	private ImageDescriptor computeBaseImageDescriptor(IAdaptable element) {
		IJavaVariable javaVariable= (IJavaVariable) element.getAdapter(IJavaVariable.class);
		if (javaVariable != null) {
			try {
				if (javaVariable.isPublic())
					return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PUBLIC);
				if (javaVariable.isProtected())
					return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PROTECTED);
				if (javaVariable.isPrivate())
					return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PRIVATE);
			} catch (DebugException e) {
			}
		}
		return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_DEFAULT);
	}
	
	private int computeAdornmentFlags(IAdaptable element) {
		int flags= 0;
		IJavaModifiers javaProperties= (IJavaModifiers)element.getAdapter(IJavaModifiers.class);
		try {
			if (javaProperties != null) {
				if (javaProperties.isFinal()) {
					flags |= JavaElementImageDescriptor.FINAL;
				}
				if (javaProperties.isStatic()) {
					flags |= JavaElementImageDescriptor.STATIC;
				}
			}
		} catch(DebugException e) {
			// fall through
		}
		return flags;
	}

	/**
	 * @see IDebugModelPresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput(Object item) {
		try {
			if (item instanceof IMarker) {
				item = getBreakpoint((IMarker)item);
			}
			if (item instanceof IJavaBreakpoint) {
				item= BreakpointUtils.getType((IJavaBreakpoint)item);
			}
			if (item instanceof LocalFileStorage) {
				return new LocalFileStorageEditorInput((LocalFileStorage)item);
			}
			if (item instanceof ZipEntryStorage) {
				return new ZipEntryStorageEditorInput((ZipEntryStorage)item);
			}
			return EditorUtility.getEditorInput(item);
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * @see IDebugModelPresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId(IEditorInput input, Object inputObject) {
		IEditorRegistry registry= PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor descriptor= registry.getDefaultEditor(input.getName());
		if (descriptor != null)
			return descriptor.getId();
		
		return null;
	}

	/**
	 * @see IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute(String id, Object value) {
		if (value == null) {
			return;
		}
		fAttributes.put(id, value);
	}

	protected boolean isShowQualifiedNames() {
		Boolean showQualified= (Boolean) fAttributes.get(DISPLAY_QUALIFIED_NAMES);
		showQualified= showQualified == null ? Boolean.FALSE : showQualified;
		return showQualified.booleanValue();
	}

	protected boolean isShowVariableTypeNames() {
		Boolean show= (Boolean) fAttributes.get(DISPLAY_VARIABLE_TYPE_NAMES);
		show= show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}

	protected String getVariableText(IJavaVariable var) throws DebugException {
		String varLabel= var.getName();
		if (varLabel != null) {
			boolean showTypes= isShowVariableTypeNames();
			int spaceIndex= varLabel.lastIndexOf(' ');
			StringBuffer buff= new StringBuffer();
			String typeName= var.getReferenceTypeName();
			if (showTypes && spaceIndex == -1) {
				typeName= getQualifiedName(typeName);
				if (typeName.length() > 0) {
					buff.append(typeName);
					buff.append(' ');
				}
			}
			if (spaceIndex != -1 && !showTypes) {
				varLabel= varLabel.substring(spaceIndex + 1);
			}
			buff.append(varLabel);

			IJavaValue javaValue= (IJavaValue) var.getValue();
			String valueString= getValueText(javaValue);
			if (valueString.length() > 0) {
				buff.append("= "); //$NON-NLS-1$
				buff.append(valueString);
			}
			return buff.toString();
		}
		return ""; //$NON-NLS-1$
	}
	
	protected String getExpressionText(IExpression expression) throws DebugException {
		String label= expression.getExpressionText();
		if (label != null) {
			boolean showTypes= isShowVariableTypeNames();
			int spaceIndex= label.lastIndexOf(' ');
			StringBuffer buff= new StringBuffer();
			IJavaValue javaValue= (IJavaValue) expression.getValue();
			String typeName= javaValue.getReferenceTypeName();
			if (showTypes && spaceIndex == -1) {
				typeName= getQualifiedName(typeName);
				if (typeName.length() > 0) {
					buff.append(typeName);
					buff.append(' ');
				}
			}
			if (spaceIndex != -1 && !showTypes) {
				label= label.substring(spaceIndex + 1);
			}
			buff.append(label);

			String valueString= getValueText(javaValue);
			if (valueString.length() > 0) {
				buff.append("= "); //$NON-NLS-1$
				buff.append(valueString);
			}
			return buff.toString();
		}
		return ""; //$NON-NLS-1$
	}	

	/**
	 * Given the reference type name of an array type, insert the array length
	 * in between the '[]' for the first dimension and return the result.
	 */
	protected String adjustTypeNameForArrayIndex(String typeName, int arrayIndex) {
		int firstBracket= typeName.indexOf("[]"); //$NON-NLS-1$
		if (firstBracket < 0) {
			return typeName;
		}
		StringBuffer buffer= new StringBuffer(typeName);
		buffer.insert(firstBracket + 1, Integer.toString(arrayIndex));
		return buffer.toString();
	}
	
	protected String getValueUnsignedText(IJavaValue value) throws DebugException {
		String sig= getPrimitiveValueTypeSignature(value);
		if (sig == null) {
			return null;
		}

		switch (sig.charAt(0)) {
			case 'B' : // byte
				int byteVal= Integer.parseInt(value.getValueString());
				if (byteVal < 0) {
					byteVal = byteVal & 0xFF;
					return Integer.toString(byteVal);					
				}
			default :
				return null;
		}
	}

	protected String getValueHexText(IJavaValue value) throws DebugException {
		String sig= getPrimitiveValueTypeSignature(value);
		if (sig == null) {
			return null;
		}

		StringBuffer buff= new StringBuffer();
		switch (sig.charAt(0)) {
			case 'B' :
				buff.append("0x"); //$NON-NLS-1$
				int byteVal = Integer.parseInt(value.getValueString());
				byteVal = byteVal & 0xFF;
				buff.append(Integer.toHexString(byteVal));
				break;
			case 'I' :
				buff.append("0x"); //$NON-NLS-1$
				buff.append(Integer.toHexString(Integer.parseInt(value.getValueString())));
				break;			
			case 'S' :
				buff.append("0x"); //$NON-NLS-1$
				int shortVal = Integer.parseInt(value.getValueString());
				shortVal = shortVal & 0xFFFF;
				buff.append(Integer.toHexString(shortVal));
				break;
			case 'J' :
				buff.append("0x"); //$NON-NLS-1$
				buff.append(Long.toHexString(Long.parseLong(value.getValueString())));
				break;
			case 'C' :
				buff.append("\\u"); //$NON-NLS-1$
				String hexString= Integer.toHexString(value.getValueString().charAt(0));
				int length= hexString.length();
				while (length < 4) {
					buff.append('0');
					length++;
				}
				buff.append(hexString);
				break;
			default:
				return null;
		}
		return buff.toString();
	}

	protected String getBreakpointText(IBreakpoint breakpoint) throws CoreException {

		if (breakpoint instanceof IJavaExceptionBreakpoint) {
			return getExceptionBreakpointText((IJavaExceptionBreakpoint)breakpoint);
		}
		if (breakpoint instanceof IJavaWatchpoint) {
			return getWatchpointText((IJavaWatchpoint)breakpoint);
		} else if (breakpoint instanceof IJavaLineBreakpoint) {
			return getLineBreakpointText((IJavaLineBreakpoint)breakpoint);
		}

		return ""; //$NON-NLS-1$
	}

	protected String getExceptionBreakpointText(IJavaExceptionBreakpoint breakpoint) throws CoreException {
		StringBuffer buffer = new StringBuffer();
		IType type = BreakpointUtils.getType(breakpoint);
		if (type != null) {
			boolean showQualified= isShowQualifiedNames();
			if (showQualified) {
				buffer.append(type.getFullyQualifiedName());
			} else {
				buffer.append(type.getElementName());
			}
		}
		int hitCount= breakpoint.getHitCount();
		if (hitCount > 0) {
			buffer.append(" ["); //$NON-NLS-1$
			buffer.append(DebugUIMessages.getString("JDIModelPresentation.hit_count__59")); //$NON-NLS-1$
			buffer.append(' ');
			buffer.append(hitCount);
			buffer.append(']');
		}		
		appendSuspendPolicy(breakpoint, buffer);
		String state= null;
		boolean c= breakpoint.isCaught();
		boolean u= breakpoint.isUncaught();
		if (c && u) {
			state= DebugUIMessages.getString("JDIModelPresentation.caught_and_uncaught_60"); //$NON-NLS-1$
		} else if (c) {
			state= DebugUIMessages.getString("JDIModelPresentation.caught_61"); //$NON-NLS-1$
		} else if (u) {
			state= DebugUIMessages.getString("JDIModelPresentation.uncaught_62"); //$NON-NLS-1$
		}
		String label= null;
		if (state == null) {
			label= buffer.toString();
		} else {
			String format= DebugUIMessages.getString("JDIModelPresentation.{1}__{0}_63"); //$NON-NLS-1$
			label= MessageFormat.format(format, new Object[] {state, buffer});
		}
		return label;
	}

	protected String getLineBreakpointText(IJavaLineBreakpoint breakpoint) throws CoreException {

		boolean showQualified= isShowQualifiedNames();
		IType type= BreakpointUtils.getType(breakpoint);
		IMember member= BreakpointUtils.getMember(breakpoint);
		if (type != null) {
			StringBuffer label= new StringBuffer();
			if (showQualified) {
				label.append(type.getFullyQualifiedName());
			} else {
				label.append(type.getElementName());
			}
			int lineNumber= breakpoint.getLineNumber();
			if (lineNumber > 0) {
				label.append(" ["); //$NON-NLS-1$
				label.append(DebugUIMessages.getString("JDIModelPresentation.line__65")); //$NON-NLS-1$
				label.append(' ');
				label.append(lineNumber);
				label.append(']');

			}
			int hitCount= breakpoint.getHitCount();
			if (hitCount > 0) {
				label.append(" ["); //$NON-NLS-1$
				label.append(DebugUIMessages.getString("JDIModelPresentation.hit_count__67")); //$NON-NLS-1$
				label.append(' ');
				label.append(hitCount);
				label.append(']');
			}
			
			appendSuspendPolicy(breakpoint,label);
			
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IJavaMethodBreakpoint mbp = (IJavaMethodBreakpoint)breakpoint;
				boolean entry = mbp.isEntry();
				boolean exit = mbp.isExit();
				if (entry && exit) {
					label.append(DebugUIMessages.getString("JDIModelPresentation.entry_and_exit")); //$NON-NLS-1$
				} else if (entry) {
					label.append(DebugUIMessages.getString("JDIModelPresentation.entry")); //$NON-NLS-1$
				} else if (exit) {
					label.append(DebugUIMessages.getString("JDIModelPresentation.exit")); //$NON-NLS-1$
				}
			}
						
			if (member != null) {
				label.append(" - "); //$NON-NLS-1$
				label.append(fJavaLabelProvider.getText(member));
			}
			
			return label.toString();
		}
		return ""; //$NON-NLS-1$

	}
	
	protected String getWatchpointText(IJavaWatchpoint watchpoint) throws CoreException {
		
		String lineInfo= getLineBreakpointText(watchpoint);
		String state= null;
		boolean access= watchpoint.isAccess();
		boolean modification= watchpoint.isModification();
		if (access && modification) {
			state= DebugUIMessages.getString("JDIModelPresentation.access_and_modification_70"); //$NON-NLS-1$
		} else if (access) {
			state= DebugUIMessages.getString("JDIModelPresentation.access_71"); //$NON-NLS-1$
		} else if (modification) {
			state= DebugUIMessages.getString("JDIModelPresentation.modification_72"); //$NON-NLS-1$
		}		
		String label= null;
		if (state == null) {
			label= lineInfo;
		} else {
			String format= DebugUIMessages.getString("JDIModelPresentation.{1}__{0}_73"); //$NON-NLS-1$
			label= MessageFormat.format(format, new Object[] {state, lineInfo});
		}
		return label;	
	}	

	protected String getStackFrameText(IStackFrame stackFrame) throws DebugException {
		IJavaStackFrame frame= (IJavaStackFrame) stackFrame.getAdapter(IJavaStackFrame.class);
		if (frame != null) {
			StringBuffer label= new StringBuffer();

			String dec= frame.getDeclaringTypeName();			
			if (frame.isObsolete()) {
				label.append(DebugUIMessages.getString("JDIModelPresentation.<obsolete_method_in__1")); //$NON-NLS-1$
				label.append(dec);
				label.append('>');
				return label.toString();
			}

			// receiver name
			String rec= frame.getReceivingTypeName();
			label.append(getQualifiedName(rec));

			// append declaring type name if different
			if (!dec.equals(rec)) {
				label.append('(');
				label.append(getQualifiedName(dec));
				label.append(')');
			}

			// append a dot separator and method name
			label.append('.');
			label.append(frame.getMethodName());

			List args= frame.getArgumentTypeNames();
			if (args.isEmpty()) {
				label.append("()"); //$NON-NLS-1$
			} else {
				label.append('(');
				Iterator iter= args.iterator();
				while (iter.hasNext()) {
					label.append(getQualifiedName((String) iter.next()));
					if (iter.hasNext()) {
						label.append(", "); //$NON-NLS-1$
					}
				}
				label.append(')');
			}

			int lineNumber= frame.getLineNumber();
			label.append(' ');
			label.append(DebugUIMessages.getString("JDIModelPresentation.line__76")); //$NON-NLS-1$
			label.append(' ');
			if (lineNumber >= 0) {
				label.append(lineNumber);
			} else {
				label.append(DebugUIMessages.getString("JDIModelPresentation.not_available")); //$NON-NLS-1$
				if (frame.isNative()) {
					label.append(' ');
					label.append(DebugUIMessages.getString("JDIModelPresentation.native_method")); //$NON-NLS-1$
				}
			}
			
			if (!frame.wereLocalsAvailable()) {
				label.append(' ');
				label.append(DebugUIMessages.getString("JDIModelPresentation.local_variables_unavailable")); //$NON-NLS-1$
			}
			
			return label.toString();

		}
		return null;
	}

	protected String getQualifiedName(String qualifiedName) {
		if (!isShowQualifiedNames()) {
			int index= qualifiedName.lastIndexOf('.');
			if (index >= 0) {
				return qualifiedName.substring(index + 1);
			}
		}
		return qualifiedName;
	}

	/**
	 * Plug in the single argument to the resource String for the key to get a formatted resource String
	 */
	public static String getFormattedString(String key, String arg) {
		return getFormattedString(key, new String[] {arg});
	}

	/**
	 * Plug in the arguments to the resource String for the key to get a formatted resource String
	 */
	public static String getFormattedString(String string, String[] args) {
		return MessageFormat.format(string, args);
	}
	
	/**
	 * When a thread suspends, add it to the thread pool for that
	 * VM. When a thread resumes, remove it from the thread pool.
	 * 
	 * @see IDebugEventListener#handleDebugEvent(DebugEvent)
	 */
	public void handleDebugEvent(DebugEvent event) {
		if (event.getSource() instanceof IJavaThread) {
			IJavaThread thread = (IJavaThread)event.getSource();
			if (event.getKind() == DebugEvent.RESUME) {
				List threads = (List)fThreadPool.get(thread.getDebugTarget());
				if (threads != null) {
					threads.remove(thread);
				}
			} else if (event.getKind() == DebugEvent.SUSPEND) {
				IDebugTarget target = thread.getDebugTarget();
				List threads = (List)fThreadPool.get(target);
				if (threads == null) {
					threads = new ArrayList();
					fThreadPool.put(target, threads);
				}
				threads.add(thread);	
			}
		}
	}

	interface IValueDetailProvider {
		public void computeDetail(IValue value, IThread thread, IValueDetailListener listener) throws DebugException;
	}
	
	class DefaultJavaValueDetailProvider implements IValueDetailProvider,
													  ITimeoutListener {		
		private StringBuffer fResultBuffer;
		private boolean fTimedOut = false;
		private Thread fDetailThread;
		private Timer fTimer;
		private IValue fValue;
		private IValueDetailListener fListener;
		private static final int EVAL_TIMEOUT = 3000;
		
		public DefaultJavaValueDetailProvider() {
			fResultBuffer = new StringBuffer(50);
			fTimer = new Timer();
		}
		
		public void timeout() {
			fTimedOut = true;
			fResultBuffer.append(DebugUIMessages.getString("JDIModelPresentation.<timeout>_77")); //$NON-NLS-1$
			notifyListener();
			fTimer.dispose();
		}
		
		private void notifyListener() {
			fListener.detailComputed(fValue, fResultBuffer.toString());
		}
		
		public void computeDetail(IValue value, final IThread thread, IValueDetailListener listener) throws DebugException {
			fValue = value;
			fListener = listener;
			Runnable detailRunnable = new Runnable() {	
				public void run() {		
					fTimer.start(DefaultJavaValueDetailProvider.this, EVAL_TIMEOUT);
					
					if (fValue == null) {
						fResultBuffer.append(DebugUIMessages.getString("JDIModelPresentation.null_78")); //$NON-NLS-1$
					} else if (thread instanceof IJavaThread) {
						IJavaThread javaThread = (IJavaThread) thread;
						if (javaThread.isSuspended()) {
							if (fValue instanceof IJavaArray) {
								appendArrayDetail((IJavaArray)fValue, javaThread);
							} else if (fValue instanceof IJavaObject) {
								appendObjectDetail((IJavaObject)fValue, javaThread);
							} else {
								appendJDIValueString(fValue);															
							}
						} else {
							appendJDIValueString(fValue);							
						}
					} else {
						appendJDIValueString(fValue);
					}
					
					if (!fTimedOut) {
						notifyListener();
						fTimer.dispose();
					}
				}
			};
			
			fDetailThread = new Thread(detailRunnable);
			fDetailThread.start();
		}
		
		protected void appendJDIValueString(IValue value) {
			try {
				String result= value.getValueString();
				fResultBuffer.append(result);
			} catch (DebugException de) {
				fResultBuffer.append(de.getStatus().getMessage());
			}
		}
		
		protected void appendObjectDetail(IJavaObject objectValue, IJavaThread thread) {			
			try {
				IJavaValue toStringValue = objectValue.sendMessage(JDIModelPresentation.fgToString, JDIModelPresentation.fgToStringSignature, null, thread, false);
				if (toStringValue == null) {
					fResultBuffer.append(DebugUIMessages.getString("JDIModelPresentation.<unknown>_80")); //$NON-NLS-1$
				} else {
					appendJDIValueString(toStringValue);
				}
			} catch (DebugException de) {
				fResultBuffer.append(de.getStatus().getMessage());
			}
		}

		protected void appendArrayDetail(IJavaArray arrayValue, IJavaThread thread) {
			fResultBuffer.append('[');
			IJavaValue[] arrayValues;
			try {
				arrayValues = arrayValue.getValues();
			} catch (DebugException de) {
				fResultBuffer.append(de.getStatus().getMessage());
				return;
			}
			for (int i = 0; i < arrayValues.length; i++) {
				IJavaValue value = arrayValues[i];
				if (value instanceof IJavaArray) {
					appendArrayDetail((IJavaArray)value, thread);	
				} else if (value instanceof IJavaObject) {
					appendObjectDetail((IJavaObject)value, thread);
				} else {
					appendJDIValueString(value);
				}
				if (i < arrayValues.length - 1) {
					fResultBuffer.append(',');
					fResultBuffer.append(' ');
				}
			}
			fResultBuffer.append(']');
		}
	}	
	
	protected void appendSuspendPolicy(IJavaBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		if (breakpoint.getSuspendPolicy() == IJavaBreakpoint.SUSPEND_VM) {
			buffer.append(' ');
			buffer.append(DebugUIMessages.getString("JDIModelPresentation.Suspend_VM")); //$NON-NLS-1$
		}
	}
}