package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaTargetPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.launching.sourcelookup.LocalFileStorage;
import org.eclipse.jdt.launching.sourcelookup.ZipEntryStorage;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.VMDisconnectedException;

/**
 * @see IDebugModelPresentation
 */
public class JDIModelPresentation extends LabelProvider implements IDebugModelPresentation {

	/**
	 * Qualified names presentation property (value <code>"DISPLAY_QUALIFIED_NAMES"</code>).
	 * When <code>DISPLAY_QUALIFIED_NAMES</code> is set to <code>True</code>,
	 * this label provider should use fully qualified type names when rendering elements.
	 * When set to <code>False</code>, this label provider should use simple
	 * names when rendering elements.
	 * @see #setAttribute(String, Object)
	 */
	public final static String DISPLAY_QUALIFIED_NAMES= "DISPLAY_QUALIFIED_NAMES"; //$NON-NLS-1$
	
	/**
	 * Qualified names presentation property (value <code>"SHOW_HEX_VALUES"</code>).
	 * When <code>SHOW_HEX_VALUES</code> is set to <code>True</code>,
	 * this label provider should show hexadecimal values rendering elements.
	 * When set to <code>False</code>, this label provider should not
	 * show hexadecimal values when rendering elements.
	 * @see #setAttribute(String, Object)
	 * @since 2.1
	 */
	public final static String SHOW_HEX_VALUES= "SHOW_HEX_VALUES"; //$NON-NLS-1$
	
	/**
	 * Qualified names presentation property (value <code>"SHOW_CHAR_VALUES"</code>).
	 * When <code>SHOW_CHAR_VALUES</code> is set to <code>True</code>,
	 * this label provider should show ASCII values when rendering character
	 * elements. When set to <code>False</code>, this label provider should not
	 * show ASCII values when rendering elements.
	 * @see #setAttribute(String, Object)
	 * @since 2.1
	 */
	public final static String SHOW_CHAR_VALUES= "SHOW_CHAR_VALUES"; //$NON-NLS-1$
	
	/**
	 * Qualified names presentation property (value <code>"SHOW_UNSIGNED_VALUES"</code>).
	 * When <code>SHOW_UNSIGNED_VALUES</code> is set to <code>True</code>,
	 * this label provider should show unsigned values when rendering
	 * byte elements. When set to <code>False</code>, this label provider should
	 * not show unsigned values when rendering byte elements.
	 * @see #setAttribute(String, Object)
	 * @since 2.1
	 */
	public final static String SHOW_UNSIGNED_VALUES= "SHOW_UNSIGNED_VALUES"; //$NON-NLS-1$
	
	protected HashMap fAttributes= new HashMap(3);
	
	/**
	 * A mapping of detail listeners (IValueDetailListener) to the values (IValue) that they
	 * are currently expecting details from.
	 */
	private HashMap fRequestedValues= new HashMap();
	
	/**
	 * A map of <code>IJavaThread</code>s to the detail computers active/waiting to
	 * perform a detail evaluation. When one evaluation completes, another may
	 * start (as evaluations must be serial).
	 */
	private static HashMap fgDetailQueue = new HashMap(5);
	
	static final Point BIG_SIZE= new Point(22, 16);
	protected ImageDescriptorRegistry fJavaElementImageRegistry= JavaPlugin.getImageDescriptorRegistry();
	protected org.eclipse.jdt.internal.debug.ui.ImageDescriptorRegistry fDebugImageRegistry= JDIDebugUIPlugin.getImageDescriptorRegistry();

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
	
	public JDIModelPresentation() {
		super();
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
			JDIDebugUIPlugin.log(de);
		}
	}
	
	/**
	 * Returns a thread from the specified VM that can be
	 * used for an evaluationm or <code>null</code> if
	 * none.
	 * 
	 * @param debug target the target in which a thread is 
	 * 	required
	 * @return thread or <code>null</code>
	 */
	protected IJavaThread getEvaluationThread(IJavaDebugTarget target) {
		IAdaptable context = DebugUITools.getDebugContext();
		IJavaThread thread = null;
		if (context != null) {
			if (context instanceof IJavaStackFrame) {
				thread = (IJavaThread)((IJavaStackFrame)context).getThread();		
			} else if (context instanceof IJavaThread) {
				thread = (IJavaThread)context;
			}
			if (thread != null && (!thread.getDebugTarget().equals(target) || !thread.isSuspended())) {
				// can only use suspended threads in the same target
				thread = null;
			}
		}
		if (thread == null) {
			try {
				IThread[] threads = target.getThreads();
				for (int i = 0; i < threads.length; i++) {
					if (threads[i].isSuspended()) {
						thread = (IJavaThread)threads[i];
						break;
					}
				}
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		return thread;
	}
			
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
			} else if (item instanceof JavaWatchExpression) {
				return getWatchExpressionText((JavaWatchExpression)item);
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
			if (!(e.getStatus().getException() instanceof VMDisconnectedException)) {
				JDIDebugUIPlugin.log(e);
			}
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
		if (thread.isPerformingEvaluation()) {
			if (thread.isSystemThread()) {
				return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Evaluating)_9"), thread.getName()); //$NON-NLS-1$
			} else {
				return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Evaluating)_10"), thread.getName()); //$NON-NLS-1$
			}
		}
		if (!thread.isSuspended()) {
			if (thread.isSystemThread()) {
				return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.System_Thread_[{0}]_(Running)_11"), thread.getName()); //$NON-NLS-1$
			} else {
				return getFormattedString(DebugUIMessages.getString("JDIModelPresentation.Thread_[{0}]_(Running)_12"), thread.getName()); //$NON-NLS-1$
			}
		}
		IBreakpoint[] breakpoints= thread.getBreakpoints();
		if (breakpoints.length > 0) {
			IJavaBreakpoint breakpoint= (IJavaBreakpoint)breakpoints[0];
			for (int i= 0, numBreakpoints= breakpoints.length; i < numBreakpoints; i++) {
				if (BreakpointUtils.isProblemBreakpoint(breakpoints[i])) {
					// If a compilation error breakpoint exists, display it instead of the first breakpoint
					breakpoint= (IJavaBreakpoint)breakpoints[i];
					break;
				}
			}
			String typeName= getMarkerTypeName(breakpoint, qualified);
			if (BreakpointUtils.isProblemBreakpoint(breakpoint)) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				IMarker problem = null;
				if (frame != null) {
					problem = JavaDebugOptionsManager.getDefault().getProblem(frame);
				}
				String message = null;
				if (problem == null) {
					message = DebugUIMessages.getString("JDIModelPresentation.Compilation_error_1"); //$NON-NLS-1$
				} else {
					message = problem.getAttribute(IMarker.MESSAGE, DebugUIMessages.getString("JDIModelPresentation.Compilation_error_1")); //$NON-NLS-1$
				}
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
		if (debugTarget.isSuspended()) {
			labelString += DebugUIMessages.getString("JDIModelPresentation.target_suspended"); //$NON-NLS-1$
		}
		return labelString;
	}

	/**
	 * Build the text for an IJavaValue.
	 */
	protected String getValueText(IJavaValue value) throws DebugException {
		
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
		if (isShowUnsignedValues()) {
			buffer= appendUnsignedText(value, buffer);
		}
		// show hex value third, if applicable
		if (isShowHexValues()) {
			buffer= appendHexText(value, buffer);
		}
		// show byte character value last, if applicable
		if (isShowCharValues()) {
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
		
	protected StringBuffer appendHexText(IJavaValue value, StringBuffer buffer) throws DebugException {
		String hexText = getValueHexText(value);
		if (hexText != null) {
			buffer.append(" ["); //$NON-NLS-1$
			buffer.append(hexText);
			buffer.append("]"); //$NON-NLS-1$
		}		
		return buffer;
	}
	
	protected StringBuffer appendCharText(IJavaValue value, StringBuffer buffer) throws DebugException {
		String charText= getValueCharText(value);
		if (charText != null) {
			buffer.append(" ["); //$NON-NLS-1$
			buffer.append(charText);
			buffer.append("]"); //$NON-NLS-1$
		}		
		return buffer;
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
			if (item instanceof IJavaValue) {
				return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PUBLIC);
			}
			if (item instanceof IExpression) {
				return getExpressionImage(item);
			}
		} catch (CoreException e) {
			if (!(e.getStatus().getException() instanceof VMDisconnectedException)) {
				JDIDebugUIPlugin.log(e);
			}
		}
		return null;
	}

	protected Image getBreakpointImage(IJavaBreakpoint breakpoint) throws CoreException {
		if (breakpoint instanceof IJavaExceptionBreakpoint) {
			return getExceptionBreakpointImage((IJavaExceptionBreakpoint)breakpoint);
		} 
		
		if (breakpoint instanceof IJavaLineBreakpoint && BreakpointUtils.isRunToLineBreakpoint((IJavaLineBreakpoint)breakpoint)) {
			return null;
		} else {
			return getJavaBreakpointImage(breakpoint);
		}
	}

	protected Image getExceptionBreakpointImage(IJavaExceptionBreakpoint exception) throws CoreException {
		int flags= computeBreakpointAdornmentFlags(exception);
		JDIImageDescriptor descriptor= null;
		if ((flags & JDIImageDescriptor.ENABLED) == 0) {
			descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJS_EXCEPTION_DISABLED, flags);
		} else if (exception.isChecked()) {
			descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJS_EXCEPTION, flags);
		} else {
			descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJS_ERROR, flags);
		}
		return fDebugImageRegistry.get(descriptor);
	}

	protected Image getJavaBreakpointImage(IJavaBreakpoint breakpoint) throws CoreException {
		if (breakpoint instanceof IJavaMethodBreakpoint) {
			IJavaMethodBreakpoint mBreakpoint= (IJavaMethodBreakpoint)breakpoint;
			return getJavaMethodBreakpointImage(mBreakpoint);
		} else if (breakpoint instanceof IJavaWatchpoint) {
			IJavaWatchpoint watchpoint= (IJavaWatchpoint)breakpoint;
			return getJavaWatchpointImage(watchpoint);
		} else if (breakpoint instanceof IJavaMethodEntryBreakpoint) {
			IJavaMethodEntryBreakpoint meBreakpoint = (IJavaMethodEntryBreakpoint)breakpoint;
			return getJavaMethodEntryBreakpointImage(meBreakpoint);
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
	
	protected Image getJavaMethodEntryBreakpointImage(IJavaMethodEntryBreakpoint mBreakpoint) throws CoreException {
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
		int flags= computeBreakpointAdornmentFlags(watchpoint);
		JDIImageDescriptor descriptor= null;
		boolean enabled= (flags & JDIImageDescriptor.ENABLED) != 0;
		if (watchpoint.isAccess()) {
			if (watchpoint.isModification()) {
				//access and modification
				if (enabled) {
					descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJS_WATCHPOINT_ENABLED, flags);
				} else {
					descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJS_WATCHPOINT_DISABLED, flags);
				}
			} else {
				if (enabled) {
					descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJS_ACCESS_WATCHPOINT_ENABLED, flags);
				} else {
					descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJS_ACCESS_WATCHPOINT_DISABLED, flags);
				}
			}
		} else if (watchpoint.isModification()) {
			if (enabled) {
				descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJS_MODIFICATION_WATCHPOINT_ENABLED, flags);
			} else {
				descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJS_MODIFICATION_WATCHPOINT_DISABLED, flags);
			}
		} else {
			//neither access nor modification
			descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJS_WATCHPOINT_DISABLED, flags);
		}
		return fDebugImageRegistry.get(descriptor);
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
		ImageDescriptor image= null;
		if (element instanceof IJavaThread) {
			IJavaThread thread = (IJavaThread)element;
			if (thread.isSuspended() && !thread.isPerformingEvaluation()) {
				image= DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED);
			} else if (thread.isTerminated()) {
				image= DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED);
			} else {
				image= DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
			}
		} else {
			image= DebugUITools.getDefaultImageDescriptor(element);
		}
		if (image == null) {
			return null;
		}
		int flags= computeJDIAdornmentFlags(element);
		JDIImageDescriptor descriptor= new JDIImageDescriptor(image, flags);
		return fDebugImageRegistry.get(descriptor);
	}
	
	/**
	 * Returns the image associated with the given element or <code>null</code>
	 * if none is defined.
	 */
	protected Image getExpressionImage(Object expression) {
		ImageDescriptor image= null;
		if (expression instanceof JavaInspectExpression) {
			image= JavaDebugImages.DESC_OBJ_JAVA_INSPECT_EXPRESSION;
		} else if (expression instanceof JavaWatchExpression) {
			image= DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_EXPRESSION);
		}
		if (image == null) {
			return null;
		}
		JDIImageDescriptor descriptor= new JDIImageDescriptor(image, 0);
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
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
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
			if (breakpoint instanceof IJavaLineBreakpoint) {
				if (((IJavaLineBreakpoint)breakpoint).isConditionEnabled()) {
					flags |= JDIImageDescriptor.CONDITIONAL;
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
				if (breakpoint instanceof IJavaMethodEntryBreakpoint) {
					flags |= JDIImageDescriptor.ENTRY;
				}
			} else if (breakpoint instanceof IJavaExceptionBreakpoint) {
				IJavaExceptionBreakpoint eBreakpoint= (IJavaExceptionBreakpoint)breakpoint;
				if (eBreakpoint.isCaught()) {
					flags |= JDIImageDescriptor.CAUGHT;
				}
				if (eBreakpoint.isUncaught()) {
					flags |= JDIImageDescriptor.UNCAUGHT;
				}
				if (eBreakpoint.getExclusionFilters().length > 0 || eBreakpoint.getInclusionFilters().length > 0) {
					flags |= JDIImageDescriptor.SCOPED;
				}
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return flags;
	}
	
	private ImageDescriptor computeBaseImageDescriptor(IAdaptable element) {
		IJavaVariable javaVariable= (IJavaVariable) element.getAdapter(IJavaVariable.class);
		if (javaVariable != null) {
			try {
				if (javaVariable.isLocal())
					return DebugUITools.getDefaultImageDescriptor(javaVariable);
				if (javaVariable.isPublic())
					return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PUBLIC);
				if (javaVariable.isProtected())
					return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PROTECTED);
				if (javaVariable.isPrivate())
					return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PRIVATE);
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
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
			JDIDebugUIPlugin.log(e);
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
				IType type = BreakpointUtils.getType((IJavaBreakpoint)item);
				if (type == null) {
					// if the breakpoint is not associated with a type, use its resource
					item = ((IJavaBreakpoint)item).getMarker().getResource();
				} else {
					item = type;
				}
			}
			if (item instanceof LocalFileStorage) {
				return new LocalFileStorageEditorInput((LocalFileStorage)item);
			}
			if (item instanceof ZipEntryStorage) {
				return new ZipEntryStorageEditorInput((ZipEntryStorage)item);
			}
			return EditorUtility.getEditorInput(item);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
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

	protected boolean isShowHexValues() {
		Boolean show= (Boolean) fAttributes.get(SHOW_HEX_VALUES);
		show= show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}

	protected boolean isShowCharValues() {
		Boolean show= (Boolean) fAttributes.get(SHOW_CHAR_VALUES);
		show= show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}

	protected boolean isShowUnsignedValues() {
		Boolean show= (Boolean) fAttributes.get(SHOW_UNSIGNED_VALUES);
		show= show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}

	protected String getVariableText(IJavaVariable var) {
		String varLabel= DebugUIMessages.getString("JDIModelPresentation<unknown_name>_1"); //$NON-NLS-1$
		try {
			varLabel= var.getName();
		} catch (DebugException exception) {
		}
		boolean showTypes= isShowVariableTypeNames();
		int spaceIndex= varLabel.lastIndexOf(' ');
		StringBuffer buff= new StringBuffer();
		String typeName= DebugUIMessages.getString("JDIModelPresentation<unknown_type>_2"); //$NON-NLS-1$
		try {
			typeName= var.getReferenceTypeName();
			if (showTypes && spaceIndex == -1) {
				typeName= getQualifiedName(typeName);
			}
		} catch (DebugException exception) {
		}
		if (showTypes) {
			buff.append(typeName);
			buff.append(' ');
		}
		if (spaceIndex != -1 && !showTypes) {
			varLabel= varLabel.substring(spaceIndex + 1);
		}
		buff.append(varLabel);

		String valueString= DebugUIMessages.getString("JDIModelPresentation<unknown_value>_3"); //$NON-NLS-1$
		try {
			IJavaValue javaValue= (IJavaValue) var.getValue();
			valueString= getValueText(javaValue);
		} catch (DebugException exception) {
		}
		//do not put the equal sign for array partitions
		if (valueString.length() != 0) {
			buff.append("= "); //$NON-NLS-1$
			buff.append(valueString);
		}
		return buff.toString();
	}
	
	protected String getExpressionText(IExpression expression) throws DebugException {
		String label= '"' + expression.getExpressionText() + '"';
		if (label != null) {
			boolean showTypes= isShowVariableTypeNames();
			StringBuffer buff= new StringBuffer();
			IJavaValue javaValue= (IJavaValue) expression.getValue();
			String typeName=null;
			try {
				typeName= javaValue.getReferenceTypeName();
			} catch (DebugException exception) {
				// ObjectCollectedException is an expected exception which will
				// occur if the inspected object has been garbage collected.
				if (exception.getStatus().getException() instanceof ObjectCollectedException) {
					return DebugUIMessages.getString("JDIModelPresentation.<garbage_collected_object>_6"); //$NON-NLS-1$
				} else {
					throw exception;
				}
			}
			if (showTypes ) {
				typeName= getQualifiedName(typeName);
				if (typeName.length() > 0) {
					buff.append(typeName);
					buff.append(' ');
				}
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
		} else if (breakpoint instanceof IJavaPatternBreakpoint) {
			return getJavaPatternBreakpointText((IJavaPatternBreakpoint)breakpoint);
		} else if (breakpoint instanceof IJavaTargetPatternBreakpoint) {
			return getJavaTargetPatternBreakpointText((IJavaTargetPatternBreakpoint)breakpoint);
		} else if (breakpoint instanceof IJavaLineBreakpoint) {
			return getLineBreakpointText((IJavaLineBreakpoint)breakpoint);
		}

		return ""; //$NON-NLS-1$
	}

	protected String getExceptionBreakpointText(IJavaExceptionBreakpoint breakpoint) throws CoreException {
		StringBuffer buffer = new StringBuffer();
		String typeName = breakpoint.getTypeName();
		buffer.append(getQualifiedName(typeName));
		appendHitCount(breakpoint, buffer);
		appendSuspendPolicy(breakpoint, buffer);
		appendThreadFilter(breakpoint, buffer);
		if (breakpoint.getExclusionFilters().length > 0 || breakpoint.getInclusionFilters().length > 0) {
			buffer.append(DebugUIMessages.getString("JDIModelPresentation._[scoped]_1")); //$NON-NLS-1$
		}
		appendInstanceFilter(breakpoint, buffer);
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

		String typeName= breakpoint.getTypeName();
		IMember member= BreakpointUtils.getMember(breakpoint);
		StringBuffer label= new StringBuffer();
		label.append(getQualifiedName(typeName));
		appendLineNumber(breakpoint, label);
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint,label);
		appendThreadFilter(breakpoint, label);
		appendConditional(breakpoint, label);
		appendInstanceFilter(breakpoint, label);
		
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
	
	protected StringBuffer appendLineNumber(IJavaLineBreakpoint breakpoint, StringBuffer label) throws CoreException {
		int lineNumber= breakpoint.getLineNumber();
		if (lineNumber > 0) {
			label.append(" ["); //$NON-NLS-1$
			label.append(DebugUIMessages.getString("JDIModelPresentation.line__65")); //$NON-NLS-1$
			label.append(' ');
			label.append(lineNumber);
			label.append(']');

		}
		return label;
	}
	
	protected StringBuffer appendHitCount(IJavaBreakpoint breakpoint, StringBuffer label) throws CoreException {
		int hitCount= breakpoint.getHitCount();
		if (hitCount > 0) {
			label.append(" ["); //$NON-NLS-1$
			label.append(DebugUIMessages.getString("JDIModelPresentation.hit_count__67")); //$NON-NLS-1$
			label.append(' ');
			label.append(hitCount);
			label.append(']');
		}
		return label;
	}
	
	protected String getJavaPatternBreakpointText(IJavaPatternBreakpoint breakpoint) throws CoreException {
	
		IResource resource= breakpoint.getMarker().getResource();
		IMember member= BreakpointUtils.getMember(breakpoint);
		StringBuffer label= new StringBuffer(resource.getName());
		appendLineNumber(breakpoint, label);
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint,label);
		appendThreadFilter(breakpoint, label);
					
		if (member != null) {
			label.append(" - "); //$NON-NLS-1$
			label.append(fJavaLabelProvider.getText(member));
		}
		
		return label.toString();
	}

	protected String getJavaTargetPatternBreakpointText(IJavaTargetPatternBreakpoint breakpoint) throws CoreException {
		IMember member= BreakpointUtils.getMember(breakpoint);
		StringBuffer label= new StringBuffer(breakpoint.getSourceName());
		appendLineNumber(breakpoint, label);
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint,label);
		appendThreadFilter(breakpoint, label);
					
		if (member != null) {
			label.append(" - "); //$NON-NLS-1$
			label.append(fJavaLabelProvider.getText(member));
		}
		
		return label.toString();
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
			
			String dec= DebugUIMessages.getString("JDIModelPresentation<unknown_declaring_type>_4"); //$NON-NLS-1$
			try {
				dec= frame.getDeclaringTypeName();
			} catch (DebugException exception) {
			}
			if (frame.isObsolete()) {
				label.append(DebugUIMessages.getString("JDIModelPresentation.<obsolete_method_in__1")); //$NON-NLS-1$
				label.append(dec);
				label.append('>');
				return label.toString();
			}

			// receiver name
			String rec= DebugUIMessages.getString("JDIModelPresentation<unknown_receiving_type>_5"); //$NON-NLS-1$
			try {
				rec= frame.getReceivingTypeName();
			} catch (DebugException exception) {
			}
			label.append(getQualifiedName(rec));

			// append declaring type name if different
			if (!dec.equals(rec)) {
				label.append('(');
				label.append(getQualifiedName(dec));
				label.append(')');
			}

			// append a dot separator and method name
			label.append('.');
			try {
				label.append(frame.getMethodName());
			} catch (DebugException exception) {
				label.append(DebugUIMessages.getString("JDIModelPresentation<unknown_method_name>_6")); //$NON-NLS-1$
			}

			try {
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
			} catch (DebugException exception) {
				label.append(DebugUIMessages.getString("JDIModelPresentation(<unknown_arguements>)_7")); //$NON-NLS-1$
			}

			try {
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
			} catch (DebugException exception) {
				label.append(DebugUIMessages.getString("JDIModelPresentation_<unknown_line_number>_8")); //$NON-NLS-1$
			}
			
			if (!frame.wereLocalsAvailable()) {
				label.append(' ');
				label.append(DebugUIMessages.getString("JDIModelPresentation.local_variables_unavailable")); //$NON-NLS-1$
			}
			
			return label.toString();

		}
		return null;
	}
	
	protected String getWatchExpressionText(JavaWatchExpression watchExpression) throws DebugException {
		StringBuffer result= new StringBuffer();
		result.append('"').append(watchExpression.getExpressionText()).append('"');
		if (watchExpression.isPending()) {
			result.append(DebugUIMessages.getString("JDIModelPresentation._(pending)_1")); //$NON-NLS-1$
		} else if (watchExpression.hasError()) {
			result.append(DebugUIMessages.getString("JDIModelPresentation._<error(s)_during_the_evaluation>_2")); //$NON-NLS-1$
		} else {
			IJavaValue javaValue= (IJavaValue) watchExpression.getValue();
			if (javaValue != null) {
				String typeName=null;
				try {
					typeName= javaValue.getReferenceTypeName();
				} catch (DebugException exception) {
					// ObjectCollectedException is an expected exception which will
					// occur if the inspected object has been garbage collected.
					if (exception.getStatus().getException() instanceof ObjectCollectedException) {
						return DebugUIMessages.getString("JDIModelPresentation.<garbage_collected_object>_6"); //$NON-NLS-1$
					} else {
						throw exception;
					}
				}
				if (isShowVariableTypeNames()) {
					typeName= getQualifiedName(typeName);
					if (typeName.length() > 0) {
						result.insert(0, ' ').insert(0,typeName);
					}
				}
	
				String valueString= getValueText(javaValue);
				if (valueString.length() > 0) {
					result.append("= ").append(valueString); //$NON-NLS-1$
				}
			}
		}
		if (watchExpression.isObsolete()) {
			result.append(DebugUIMessages.getString("JDIModelPresentation._(obsolete)_1")); //$NON-NLS-1$
		}
		if (!watchExpression.isEnabled()) {
			result.append(DebugUIMessages.getString("JDIModelPresentation._(disabled)_3")); //$NON-NLS-1$
		}
		return result.toString();
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
	
	interface IValueDetailProvider {
		public void computeDetail(IValue value, IJavaThread thread, IValueDetailListener listener) throws DebugException;
	}
	
	class DefaultJavaValueDetailProvider implements IValueDetailProvider {		
		private StringBuffer fResultBuffer;
		private Thread fDetailThread;
		private IValue fValue;
		private IJavaThread fJavaThread;
		private IJavaProject fJavaProject;
		private IValueDetailListener fListener;
		private static final int EVAL_TIMEOUT = 3000;		
		
		public DefaultJavaValueDetailProvider() {
			fResultBuffer = new StringBuffer(50);
		}
		
		private void notifyListener() {
			fListener.detailComputed(fValue, fResultBuffer.toString());
			List queue = (List)fgDetailQueue.get(fJavaThread);
			queue.remove(this);
			if (queue.isEmpty()) {
				fgDetailQueue.remove(fJavaThread);
			} else {
				DefaultJavaValueDetailProvider next = (DefaultJavaValueDetailProvider)queue.get(0);
				try {
					next.computeDetail();
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
				}
			}								
		}
		
		public void computeDetail(IValue value, IJavaThread thread, IValueDetailListener listener) throws DebugException {
			
			fValue = value;
			fListener = listener;
			fJavaThread = thread;
			fJavaProject= getJavaProject();
						
			// check the queue to avoid concurrent evaluations
			List queue = (List)fgDetailQueue.get(fJavaThread);
			if (queue == null) {
				queue = new Vector(2);
				fgDetailQueue.put(fJavaThread, queue);
			}
			synchronized (queue) {
				queue.add(this);
				if (queue.size() > 1) {
					return;
				}
			}
				
				
			Runnable r = new Runnable() {
				public void run() {
					JDIDebugUIPlugin.getStandardDisplay().asyncExec(
						new Runnable() {
							public void run() {
								try {
									computeDetail();
								} catch (DebugException e) {
									JDIDebugUIPlugin.log(e);
								}
							}
						}
					);
				}
			};		
			try {
				DebugPlugin.getDefault().asyncExec(r, JDIDebugUIPlugin.getStandardDisplay().getThread());
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		
		public void computeDetail() throws DebugException {	
			if (((IJavaValue)fValue).getSignature() == null) {
				// no need to spawn a thread for a null fValue
				fResultBuffer.append(DebugUIMessages.getString("JDIModelPresentation.null_78")); //$NON-NLS-1$
				notifyListener();
				return;
			} else if (fValue instanceof IJavaPrimitiveValue) {
				// no need to spawn a thread for a primitive value
				appendJDIPrimitiveValueString(fValue);
				notifyListener();
				return;
			} else if (fJavaThread == null || !fJavaThread.isSuspended()) {
				// no thread available
				fResultBuffer = new StringBuffer(DebugUIMessages.getString("JDIModelPresentation.no_suspended_threads_1")); //$NON-NLS-1$
				appendJDIValueString(fValue);
				notifyListener();
				return;
			}
			
			fRequestedValues.put(fListener, fValue);
			
			Runnable detailRunnable = new Runnable() {	
				public void run() {
					IJavaValue detailFormatterResult= null;
					if (fValue instanceof IJavaObject && !(fValue instanceof IJavaArray)) {
						// try to use the detail formatters system on the object
						try {
							detailFormatterResult= JavaDetailFormattersManager.getDefault().getValueDetail((IJavaObject)fValue, fJavaThread, fJavaProject);
						} catch (DebugException e) {
							handleDebugException(e, (IJavaValue)fValue);
							return;
						}
						if (detailFormatterResult != null) {
							if (detailFormatterResult instanceof IJavaPrimitiveValue) {
								appendJDIPrimitiveValueString(detailFormatterResult);
								if (fValue == fRequestedValues.remove(fListener)) {
									notifyListener();
								}
								return;
							}
						}
					}
					final IValue value= detailFormatterResult == null ? fValue : detailFormatterResult;
					IEvaluationRunnable er = new IEvaluationRunnable() {
						public void run(IJavaThread jt, IProgressMonitor pm) {
							if (value instanceof IJavaArray) {
								appendArrayDetail((IJavaArray)fValue, jt);
							} else if (value instanceof IJavaObject) {
								try {
									appendObjectDetail((IJavaObject)value, jt);
								} catch (DebugException e) {
									handleDebugException(e, (IJavaValue)fValue);
								}
							} else {
								appendJDIValueString(value);
							}
						}
					};
					try {
						fJavaThread.runEvaluation(er, null, DebugEvent.EVALUATION_IMPLICIT, false);
					} catch (DebugException e) {
						handleDebugException(e, (IJavaValue)fValue);
					}
					if (fValue == fRequestedValues.remove(fListener)) {
						// If another evaluation occurs before this one finished,
						// don't display this result
						notifyListener();
					}
				}
			};
			
			fDetailThread = new Thread(detailRunnable);
			fDetailThread.start();

		}

		private IJavaProject getJavaProject() {
			IAdaptable context = DebugUITools.getDebugContext();
			if (context instanceof IThread) {
				try {
					context = ((IThread)context).getTopStackFrame();
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
			if (context == null) {
				return null;
			}
			IJavaStackFrame stackFrame = (IJavaStackFrame) context.getAdapter(IJavaStackFrame.class);
			ILaunch launch = stackFrame.getLaunch();
			if (launch == null) {
				return null;
			}
			ISourceLocator locator= launch.getSourceLocator();
			if (locator == null)
				return null;
			
			Object sourceElement = locator.getSourceElement(stackFrame);
			if (sourceElement instanceof IJavaElement) {
				return ((IJavaElement) sourceElement).getJavaProject();
			}			
			return null;
		}
	
				
		protected void appendJDIPrimitiveValueString(IValue value) {
			if (value instanceof IJavaValue) {
				try {
					fResultBuffer.append(getValueText(((IJavaValue)value)));
				} catch (DebugException de) {
					JDIDebugUIPlugin.log(de);
					fResultBuffer.append(de.getStatus().getMessage());
				}
			} else {
				appendJDIValueString(value);
			}
		}
		
		protected void appendJDIValueString(IValue value) {
			try {
				String result= value.getValueString();
				fResultBuffer.append(result);
			} catch (DebugException de) {
				JDIDebugUIPlugin.log(de);
				fResultBuffer.append(de.getStatus().getMessage());
			}
		}
		
		protected void appendObjectDetail(IJavaObject objectValue, IJavaThread thread) throws DebugException {			
			IJavaValue toStringValue = objectValue.sendMessage(JDIModelPresentation.fgToString, JDIModelPresentation.fgToStringSignature, null, thread, false);
			if (toStringValue == null) {
				fResultBuffer.append(DebugUIMessages.getString("JDIModelPresentation.<unknown>_80")); //$NON-NLS-1$
			} else {
				appendJDIValueString(toStringValue);
			}
		}
		
		protected void handleDebugException(DebugException de, IJavaValue value) {
			String reason = null;
			switch (de.getStatus().getCode()) {
				case IJavaThread.ERR_THREAD_NOT_SUSPENDED:
					reason = DebugUIMessages.getString("JDIModelPresentation.thread_not_suspended_2"); //$NON-NLS-1$
					break;
				case IJavaThread.ERR_NESTED_METHOD_INVOCATION:
					reason = DebugUIMessages.getString("JDIModelPresentation.evaluation_in_progress_3"); //$NON-NLS-1$
					break;
				case IJavaThread.ERR_INCOMPATIBLE_THREAD_STATE:
					reason = DebugUIMessages.getString("JDIModelPresentation.thread_must_be_suspended_by_breakpoint_or_step_1"); //$NON-NLS-1$
					break;
				default:
					reason = de.getStatus().getMessage();
					break;
			}
			fResultBuffer = new StringBuffer(reason);
		}

		protected void appendArrayDetail(IJavaArray arrayValue, IJavaThread thread) {
			fResultBuffer.append('[');
			IJavaValue[] arrayValues;
			try {
				arrayValues = arrayValue.getValues();
			} catch (DebugException de) {
				JDIDebugUIPlugin.log(de);
				fResultBuffer.append(de.getStatus().getMessage());
				return;
			}
			try {
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
			} catch (DebugException e) {
				handleDebugException(e, arrayValue);
			}
		}
	}	
	
	protected void appendSuspendPolicy(IJavaBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		if (breakpoint.getSuspendPolicy() == IJavaBreakpoint.SUSPEND_VM) {
			buffer.append(' ');
			buffer.append(DebugUIMessages.getString("JDIModelPresentation.Suspend_VM")); //$NON-NLS-1$
		}
	}
	
	protected void appendThreadFilter(IJavaBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		if (breakpoint.getThreadFilters().length != 0) {
			buffer.append(' ');
			buffer.append(DebugUIMessages.getString("JDIModelPresentation.thread_filtered")); //$NON-NLS-1$
		}
	}
	
	protected void appendConditional(IJavaLineBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		if (breakpoint.isConditionEnabled() && breakpoint.getCondition() != null) {
			buffer.append(' ');
			buffer.append(DebugUIMessages.getString("JDIModelPresentation.[conditional]_2")); //$NON-NLS-1$
		}
	}
	
	protected void appendInstanceFilter(IJavaBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		IJavaObject[] instances = breakpoint.getInstanceFilters();
		for (int i = 0; i < instances.length; i++) {
			String instanceText= instances[i].getValueString();
			if (instanceText != null) {
				buffer.append(' ');
				buffer.append(MessageFormat.format(DebugUIMessages.getString("JDIModelPresentation.instance_1"), new String[] {instanceText})); //$NON-NLS-1$
			}				
		}
	}
}