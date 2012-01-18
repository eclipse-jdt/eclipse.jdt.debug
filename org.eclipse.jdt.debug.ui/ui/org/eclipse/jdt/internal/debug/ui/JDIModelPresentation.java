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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugModelPresentationExtension;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaStratumLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaTargetPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugModelMessages;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListEntryVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListValue;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaContendedMonitor;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaOwnedMonitor;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaOwningThread;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaWaitingThread;
import org.eclipse.jdt.internal.debug.ui.monitors.NoMonitorInformationElement;
import org.eclipse.jdt.internal.debug.ui.monitors.ThreadMonitorManager;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.SnippetMessages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.sun.jdi.ObjectCollectedException;

/**
 * Determines how to display java elements, including labels, images and editors.
 * @see IDebugModelPresentation
 */
public class JDIModelPresentation extends LabelProvider implements IDebugModelPresentation, IColorProvider, IDebugModelPresentationExtension {

	/**
	 * Qualified names presentation property (value <code>"DISPLAY_QUALIFIED_NAMES"</code>).
	 * When <code>DISPLAY_QUALIFIED_NAMES</code> is set to <code>True</code>,
	 * this label provider should use fully qualified type names when rendering elements.
	 * When set to <code>False</code>, this label provider should use simple
	 * names when rendering elements.
	 * @see #setAttribute(String, Object)
	 */
	public final static String DISPLAY_QUALIFIED_NAMES= "DISPLAY_QUALIFIED_NAMES"; //$NON-NLS-1$
		
	protected HashMap<String, Object> fAttributes= new HashMap<String, Object>(3);
	
	static final Point BIG_SIZE= new Point(16, 16);
	
	private static ImageDescriptorRegistry fgJavaElementImageRegistry;
	private static org.eclipse.jdt.internal.debug.ui.ImageDescriptorRegistry fgDebugImageRegistry;
	
	/**
	 * Flag to indicate if image registry's referenced by this model presentation is initialized
	 */
	private static boolean fInitialized = false;

	protected static final String fgStringName= "java.lang.String"; //$NON-NLS-1$

	/******
	 * This constant is here for experimental purposes only and should not be used.
	 * It is used to store a suffix for a breakpoint's label in its marker.
	 * It is not officially supported and might be removed in the future.
	 * */
	private static final String BREAKPOINT_LABEL_SUFFIX = "JDT_BREAKPOINT_LABEL_SUFFIX"; //$NON-NLS-1$
	
	private JavaElementLabelProvider fJavaLabelProvider;
	
	public JDIModelPresentation() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (fJavaLabelProvider != null) {
			fJavaLabelProvider.dispose();
		}
		fAttributes.clear();
	}

	/**
	 * @see IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		IJavaThread thread = getEvaluationThread((IJavaDebugTarget)value.getDebugTarget());
		if (thread == null) {
			listener.detailComputed(value, DebugUIMessages.JDIModelPresentation_no_suspended_threads); 
		} else {
			JavaDetailFormattersManager.getDefault().computeValueDetail((IJavaValue)value, thread, listener);
		}
	}
	
	/**
	 * Returns a thread from the specified VM that can be
	 * used for an evaluation or <code>null</code> if
	 * none.
	 * 
	 * @param debug target the target in which a thread is 
	 * 	required
	 * @return thread or <code>null</code>
	 */
	public static IJavaThread getEvaluationThread(IJavaDebugTarget target) {
		IJavaStackFrame frame = EvaluationContextManager.getEvaluationContext((IWorkbenchWindow)null);
		IJavaThread thread = null;
		if (frame != null) {
			thread = (IJavaThread) frame.getThread();
		}
		if (thread != null && (!thread.getDebugTarget().equals(target) || (!thread.isSuspended() && !thread.isPerformingEvaluation()))) {
			// can only use suspended threads in the same target
			thread = null;
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
	@Override
	public String getText(Object item) {
		try {
			boolean showQualified= isShowQualifiedNames();
			if (item instanceof IJavaVariable) {
				return getVariableText((IJavaVariable) item);
			} else if (item instanceof IStackFrame) {
				StringBuffer label= new StringBuffer(getStackFrameText((IStackFrame) item));
				if (item instanceof IJavaStackFrame) {
					if (((IJavaStackFrame)item).isOutOfSynch()) {
						label.append(DebugUIMessages.JDIModelPresentation___out_of_synch__1); 
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
			} else if (item instanceof IWatchExpression) {
				return getWatchExpressionText((IWatchExpression)item);
			} else if (item instanceof IExpression) {
				return getExpressionText((IExpression)item);
			} else if (item instanceof JavaOwnedMonitor) {
				return getJavaOwnedMonitorText((JavaOwnedMonitor)item);
			} else if (item instanceof JavaContendedMonitor) {
				return getJavaContendedMonitorText((JavaContendedMonitor)item);
			} else if (item instanceof JavaOwningThread) {
				return getJavaOwningTreadText((JavaOwningThread)item);
			} else if (item instanceof JavaWaitingThread) {
				return getJavaWaitingTreadText((JavaWaitingThread)item);
			} else if (item instanceof NoMonitorInformationElement) {
                return DebugUIMessages.JDIModelPresentation_5; 
            } else {
				StringBuffer label= new StringBuffer();
				if (item instanceof IJavaThread) {
					label.append(getThreadText((IJavaThread) item, showQualified));
					if (((IJavaThread)item).isOutOfSynch()) {
						label.append(DebugUIMessages.JDIModelPresentation___out_of_synch__1); 
					} else if (((IJavaThread)item).mayBeOutOfSynch()) {
						label.append(DebugUIMessages.JDIModelPresentation___may_be_out_of_synch__2); 
					}
				} else if (item instanceof IJavaDebugTarget) {
					label.append(getDebugTargetText((IJavaDebugTarget) item));
					if (((IJavaDebugTarget)item).isOutOfSynch()) {
						label.append(DebugUIMessages.JDIModelPresentation___out_of_synch__1); 
					} else if (((IJavaDebugTarget)item).mayBeOutOfSynch()) {
						label.append(DebugUIMessages.JDIModelPresentation___may_be_out_of_synch__2); 
					}
				} else if (item instanceof IJavaValue) {
					label.append(getValueText((IJavaValue) item));
				}
				if (item instanceof ITerminate) {
					if (((ITerminate) item).isTerminated()) {
						label.insert(0, DebugUIMessages.JDIModelPresentation__terminated__2); 
						return label.toString();
					}
				}
				if (item instanceof IDisconnect) {
					if (((IDisconnect) item).isDisconnected()) {
						label.insert(0, DebugUIMessages.JDIModelPresentation__disconnected__4); 
						return label.toString();
					}
				}
				if (label.length() > 0) {
					return label.toString();
				}
			}
		} catch (CoreException e) {
			return DebugUIMessages.JDIModelPresentation__not_responding__6; 
		}
		return null;
	}

	private String getJavaOwningTreadText(JavaOwningThread thread) throws CoreException {
		return getFormattedString(DebugUIMessages.JDIModelPresentation_0, getThreadText(thread.getThread().getThread(), isShowQualifiedNames())); 
	}

	private String getJavaWaitingTreadText(JavaWaitingThread thread) throws CoreException {
		return getFormattedString(DebugUIMessages.JDIModelPresentation_1, getThreadText(thread.getThread().getThread(), isShowQualifiedNames())); 
	}

	private String getJavaContendedMonitorText(JavaContendedMonitor monitor) throws DebugException {
		return getFormattedString(DebugUIMessages.JDIModelPresentation_2, getValueText(monitor.getMonitor().getMonitor())); 
	}

	private String getJavaOwnedMonitorText(JavaOwnedMonitor monitor) throws DebugException {
		return getFormattedString(DebugUIMessages.JDIModelPresentation_3, getValueText(monitor.getMonitor().getMonitor())); 
	}

	protected IBreakpoint getBreakpoint(IMarker marker) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
		}
	
	/**
	 * Build the text for an IJavaThread.
	 */
	protected String getThreadText(IJavaThread thread, boolean qualified) throws CoreException {
		StringBuffer key = new StringBuffer("thread_"); //$NON-NLS-1$
		String[] args = null;
		IBreakpoint[] breakpoints= thread.getBreakpoints();
		if (thread.isDaemon()) {
			key.append("daemon_"); //$NON-NLS-1$
		}
		if (thread.isSystemThread()) {
			key.append("system_"); //$NON-NLS-1$
		}
		if (thread.isTerminated()) {
			key.append("terminated"); //$NON-NLS-1$
			args = new String[] {thread.getName()}; 
		} else if (thread.isStepping()) {
			key.append("stepping"); //$NON-NLS-1$
			args = new String[] {thread.getName()};
		} else if ((thread instanceof JDIThread && ((JDIThread)thread).isSuspendVoteInProgress()) && !thread.getDebugTarget().isSuspended()) {
			// show running when listener notification is in progress
			key.append("running"); //$NON-NLS-1$
			args = new String[] {thread.getName()};
		} else if (thread.isPerformingEvaluation() && breakpoints.length == 0) {
			key.append("evaluating"); //$NON-NLS-1$
			args = new String[] {thread.getName()};
		} else if (!thread.isSuspended()) {
			key.append("running"); //$NON-NLS-1$
			args = new String[] {thread.getName()};
		} else {
			key.append("suspended"); //$NON-NLS-1$
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
					if (problem != null) {
						key.append("_problem"); //$NON-NLS-1$
						String message = problem.getAttribute(IMarker.MESSAGE, DebugUIMessages.JDIModelPresentation_Compilation_error_1);
						args = new String[] {thread.getName(), message};
					}
				}			
				// check args == null in case the exception is a compilation error
				if (breakpoint instanceof IJavaExceptionBreakpoint && args == null) {
					key.append("_exception"); //$NON-NLS-1$
					String exName = ((IJavaExceptionBreakpoint)breakpoint).getExceptionTypeName();
					if (exName == null) {
						exName = typeName;
					} else if (!qualified) {
						int index = exName.lastIndexOf('.');
						exName = exName.substring(index + 1);
					} 
					args = new String[] {thread.getName(), exName};
				} else if (breakpoint instanceof IJavaWatchpoint) {
					IJavaWatchpoint wp = (IJavaWatchpoint)breakpoint;
					String fieldName = wp.getFieldName();
					args = new String[] {thread.getName(), fieldName, typeName};
					if (wp.isAccessSuspend(thread.getDebugTarget())) {
						key.append("_fieldaccess"); //$NON-NLS-1$
					} else {
						key.append("_fieldmodification"); //$NON-NLS-1$
					}
				} else if (breakpoint instanceof IJavaMethodBreakpoint) {
					IJavaMethodBreakpoint me= (IJavaMethodBreakpoint)breakpoint;
					String methodName= me.getMethodName();
					args = new String[] {thread.getName(), methodName, typeName};
					if (me.isEntrySuspend(thread.getDebugTarget())) {
						key.append("_methodentry"); //$NON-NLS-1$
					} else {
						key.append("_methodexit"); //$NON-NLS-1$
					}
				} else if (breakpoint instanceof IJavaLineBreakpoint) {
					IJavaLineBreakpoint jlbp = (IJavaLineBreakpoint)breakpoint;
					int lineNumber= jlbp.getLineNumber();
					if (lineNumber > -1) {
						args = new String[] {thread.getName(), String.valueOf(lineNumber), typeName};
						if (BreakpointUtils.isRunToLineBreakpoint(jlbp)) {
							key.append("_runtoline"); //$NON-NLS-1$
						} else {
							key.append("_linebreakpoint"); //$NON-NLS-1$
						}
					}
				} else if (breakpoint instanceof IJavaClassPrepareBreakpoint) {
					key.append("_classprepare"); //$NON-NLS-1$
					args = new String[]{thread.getName(), getQualifiedName(breakpoint.getTypeName())}; 
				}
			}
	
			if (args == null) {
				// Otherwise, it's just suspended
				args =  new String[] {thread.getName()};
			}
		}
		try {
			return getFormattedString((String)DebugUIMessages.class.getDeclaredField(key.toString()).get(null), args);
		} catch (IllegalArgumentException e) {
			JDIDebugUIPlugin.log(e);
		} catch (SecurityException e) {
			JDIDebugUIPlugin.log(e);
		} catch (IllegalAccessException e) {
			JDIDebugUIPlugin.log(e);
		} catch (NoSuchFieldException e) {
			JDIDebugUIPlugin.log(e);
		}
		return DebugUIMessages.JDIModelPresentation_unknown_name__1;
	}

	/**
	 * Build the text for an IJavaDebugTarget.
	 */
	protected String getDebugTargetText(IJavaDebugTarget debugTarget) throws DebugException {
		String labelString= debugTarget.getName();
		if (debugTarget.isSuspended()) {
			labelString += DebugUIMessages.JDIModelPresentation_target_suspended; 
		}
		return labelString;
	}

	/**
	 * Build the text for an IJavaValue.
	 * 
	 * @param value the value to get the text for
	 * @return the value string
	 * @throws DebugException if something happens trying to compute the value string
	 */
	public String getValueText(IJavaValue value) throws DebugException {
		String refTypeName= value.getReferenceTypeName();
		String valueString= value.getValueString();
		boolean isString= refTypeName.equals(fgStringName);
		IJavaType type= value.getJavaType();
		String signature= null;
		if (type != null) {
			signature= type.getSignature();
		}
		if ("V".equals(signature)) { //$NON-NLS-1$
			valueString= DebugUIMessages.JDIModelPresentation__No_explicit_return_value__30; 
		}
		boolean isObject= isObjectValue(signature);
		boolean isArray= value instanceof IJavaArray;
		StringBuffer buffer= new StringBuffer();
		if(isUnknown(signature)) {
			buffer.append(signature);
		} else if (isObject && !isString && (refTypeName.length() > 0)) {
			// Don't show type name for instances and references
			if (!(value instanceof JDIReferenceListValue || value instanceof JDIAllInstancesValue)){
				String qualTypeName= getQualifiedName(refTypeName).trim();
				if (isArray) {
					qualTypeName= adjustTypeNameForArrayIndex(qualTypeName, ((IJavaArray)value).getLength());
				}
				buffer.append(qualTypeName);
				buffer.append(' ');
			}
		}
		
		// Put double quotes around Strings
		if (valueString != null && (isString || valueString.length() > 0)) {
			if (isString) {
				buffer.append('"');
			}
			buffer.append(DefaultLabelProvider.escapeSpecialChars(valueString));
			if (isString) {
				buffer.append('"');
				if(value instanceof IJavaObject){
					buffer.append(" "); //$NON-NLS-1$
					buffer.append(NLS.bind(DebugUIMessages.JDIModelPresentation_118, new String[]{String.valueOf(((IJavaObject)value).getUniqueId())})); 
				}
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
		return buffer.toString().trim();
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
	 * Returns <code>true</code> if the given signature is not <code>null</code> and
	 * matches the text '&lt;unknown&gt;'
	 * 
	 * @param signature the signature to compare
	 * @return <code>true</code> if the signature matches '&lt;unknown&gt;'
	 * @since 3.6.1
	 */
	boolean isUnknown(String signature) {
		if(signature == null) {
			return false;
		}
		return JDIDebugModelMessages.JDIDebugElement_unknown.equals(signature);
	}
	
	/**
	 * Given a JNI-style signature String for a IJavaValue, return true
	 * if the signature represents an Object or an array of Objects.
	 * 
	 * @param signature the signature to check
	 * @return <code>true</code> if the signature represents an object <code>false</code> otherwise
	 */
	public static boolean isObjectValue(String signature) {
		if (signature == null) {
			return false;
		}
		String type = Signature.getElementType(signature);
		char sigchar = type.charAt(0);
		if(sigchar == Signature.C_UNRESOLVED || 
				sigchar == Signature.C_RESOLVED) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns whether the image registry's have been retrieved.
	 * 
	 * @return whether image registry's have been retrieved.
	 */
	public static boolean isInitialized() {
		return fgJavaElementImageRegistry != null && fgDebugImageRegistry != null;
	}
	
	/**
	 * Returns the type signature for this value if its type is primitive.  
	 * For non-primitive types, null is returned.
	 */
	protected String getPrimitiveValueTypeSignature(IJavaValue value) throws DebugException {
		IJavaType type= value.getJavaType();
		if (type != null) {
			String sig= type.getSignature();
			if (sig != null && sig.length() == 1) {
				return sig;
			}
		}
		return null;
	}
	/**
	 * Returns the character string of a byte or <code>null</code> if
	 * the value can not be interpreted as a valid character.
	 */
	protected String getValueCharText(IJavaValue value) throws DebugException {
		String sig= getPrimitiveValueTypeSignature(value);
		if (sig == null) {
			return null;
		}
		String valueString= value.getValueString();
		long longValue;
		try {
			longValue= Long.parseLong(valueString);
		} catch (NumberFormatException e) {
			return null;
		}
		switch (sig.charAt(0)) {
			case 'B' : // byte
				longValue= longValue & 0xFF; // Only lower 8 bits
				break;
			case 'I' : // integer
				longValue= longValue & 0xFFFFFFFF; // Only lower 32 bits
				if (longValue > 0xFFFF || longValue < 0) {
					return null;
				}
				break;
			case 'S' : // short
				longValue= longValue & 0xFFFF; // Only lower 16 bits
				break;
			case 'J' :
				if (longValue > 0xFFFF || longValue < 0) {
					// Out of character range
					return null;
				}
				break;
			default :
				return null;
		}
		char charValue= (char)longValue;
		StringBuffer charText = new StringBuffer();
		if (Character.getType(charValue) == Character.CONTROL) {
			Character ctrl = new Character((char) (charValue + 64));
			charText.append('^'); 
			charText.append(ctrl);
			switch (charValue) { // common use
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
			charText.append(new Character(charValue));
		}
		return charText.toString();
	}

	@SuppressWarnings("deprecation")
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
	@Override
	public Image getImage(Object item) {
		
		initImageRegistries();
		
		try {
			if (item instanceof JDIReferenceListVariable) {
				return getReferencesImage(item);
			}
			if (item instanceof JDIReferenceListEntryVariable){
				return getReferenceImage(item);
			}
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
			if (item instanceof JDIThread) {
				JDIThread jt = (JDIThread) item;
				if (jt.getDebugTarget().isSuspended()) {
					return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED);
				}
				if (jt.isSuspendVoteInProgress()) {
					return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
				}
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
			if (item instanceof JavaOwnedMonitor) {
				return getJavaOwnedMonitorImage((JavaOwnedMonitor)item);
			}
			if (item instanceof JavaContendedMonitor) {
				return getJavaContendedMonitorImage((JavaContendedMonitor)item);
			}
			if (item instanceof JavaOwningThread) {
				return getJavaOwningThreadImage((JavaOwningThread)item);
			}
			if (item instanceof JavaWaitingThread) {
				return getJavaWaitingThreadImage((JavaWaitingThread)item);
			}
            if (item instanceof NoMonitorInformationElement) {
                return getDebugImageRegistry().get(new JDIImageDescriptor(
						getImageDescriptor(JavaDebugImages.IMG_OBJS_MONITOR), 0));
            }
		} catch (CoreException e) {
		    // no need to log errors - elements may no longer exist by the time we render them
		}
		return null;
	}

	/**
	 * Initialize image registry's that this model presentation references to
	 */
	private synchronized void initImageRegistries() {
		
		// if not initialized and this is called on the UI thread
		if (!fInitialized && Thread.currentThread().equals(JDIDebugUIPlugin.getStandardDisplay().getThread())) {
			// call get image registry's to force them to be created on the UI thread
			getDebugImageRegistry();
			getJavaElementImageRegistry();
			JavaUI.getSharedImages();
			fInitialized = true;
		}
	}

	/**
	 * @param thread
	 * @return
	 */
	private Image getJavaWaitingThreadImage(JavaWaitingThread thread) {
		JDIImageDescriptor descriptor;
		int flag= JDIImageDescriptor.IN_CONTENTION_FOR_MONITOR | (thread.getThread().isInDeadlock() ? JDIImageDescriptor.IN_DEADLOCK : 0);
		if (thread.isSuspended()) {
			descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED), flag);
		} else {
			descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING), flag);
		}
		return getDebugImageRegistry().get(descriptor);
	}

	/**
	 * @param thread
	 * @return
	 */
	private Image getJavaOwningThreadImage(JavaOwningThread thread) {
		JDIImageDescriptor descriptor;
		int flag= JDIImageDescriptor.OWNS_MONITOR | (thread.getThread().isInDeadlock() ? JDIImageDescriptor.IN_DEADLOCK : 0);
		if (thread.isSuspended()) {
			descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED), flag);
		} else {
			descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING), flag);
		}
		return getDebugImageRegistry().get(descriptor);
	}

	/**
	 * @param monitor
	 * @return
	 */
	private Image getJavaContendedMonitorImage(JavaContendedMonitor monitor) {
		int flag= monitor.getMonitor().isInDeadlock() ? JDIImageDescriptor.IN_DEADLOCK : 0;
		JDIImageDescriptor descriptor= new JDIImageDescriptor(
				getImageDescriptor(JavaDebugImages.IMG_OBJS_CONTENDED_MONITOR), flag);
		return getDebugImageRegistry().get(descriptor);
	}

	/**
	 * @param monitor
	 * @return
	 */
	private Image getJavaOwnedMonitorImage(JavaOwnedMonitor monitor) {
		int flag= monitor.getMonitor().isInDeadlock() ? JDIImageDescriptor.IN_DEADLOCK : 0;
		JDIImageDescriptor descriptor= new JDIImageDescriptor(getImageDescriptor(JavaDebugImages.IMG_OBJS_OWNED_MONITOR), flag);
		return getDebugImageRegistry().get(descriptor);
	}

	protected Image getBreakpointImage(IJavaBreakpoint breakpoint) throws CoreException {
		if (breakpoint instanceof IJavaExceptionBreakpoint) {
			return getExceptionBreakpointImage((IJavaExceptionBreakpoint)breakpoint);
		} else if (breakpoint instanceof IJavaClassPrepareBreakpoint) {
			return getClassPrepareBreakpointImage((IJavaClassPrepareBreakpoint)breakpoint);
		}
		
		if (breakpoint instanceof IJavaLineBreakpoint && BreakpointUtils.isRunToLineBreakpoint((IJavaLineBreakpoint)breakpoint)) {
			return null;
		}
		return getJavaBreakpointImage(breakpoint);
	}

	protected Image getExceptionBreakpointImage(IJavaExceptionBreakpoint exception) throws CoreException {
		int flags= computeBreakpointAdornmentFlags(exception);
		JDIImageDescriptor descriptor= null;
		if ((flags & JDIImageDescriptor.ENABLED) == 0) {
			descriptor= new JDIImageDescriptor(getImageDescriptor(JavaDebugImages.IMG_OBJS_EXCEPTION_DISABLED), flags);
		} else if (exception.isChecked()) {
			descriptor= new JDIImageDescriptor(getImageDescriptor(JavaDebugImages.IMG_OBJS_EXCEPTION), flags);
		} else {
			descriptor= new JDIImageDescriptor(getImageDescriptor(JavaDebugImages.IMG_OBJS_ERROR), flags);
		}
		return getDebugImageRegistry().get(descriptor);
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
			return getDebugImageRegistry().get(descriptor);
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
			
		return getDebugImageRegistry().get(descriptor);
	}
	
	protected Image getJavaMethodEntryBreakpointImage(IJavaMethodEntryBreakpoint mBreakpoint) throws CoreException {
		int flags= computeBreakpointAdornmentFlags(mBreakpoint);
		JDIImageDescriptor descriptor= null;
		if (mBreakpoint.isEnabled()) {
			descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT), flags);
		} else {
			descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED), flags);
		}
			
		return getDebugImageRegistry().get(descriptor);
	}
	
	protected Image getClassPrepareBreakpointImage(IJavaClassPrepareBreakpoint breakpoint) throws CoreException {
		int flags= computeBreakpointAdornmentFlags(breakpoint);
		JDIImageDescriptor descriptor= null;
		if (breakpoint.getMemberType() == IJavaClassPrepareBreakpoint.TYPE_CLASS) {
			descriptor= new JDIImageDescriptor(JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CLASS), flags);
		} else {
			descriptor= new JDIImageDescriptor(JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INTERFACE), flags);
		}
		return getDebugImageRegistry().get(descriptor);
	}	
	
	protected Image getJavaWatchpointImage(IJavaWatchpoint watchpoint) throws CoreException {
		int flags= computeBreakpointAdornmentFlags(watchpoint);
		JDIImageDescriptor descriptor= null;
		boolean enabled= (flags & JDIImageDescriptor.ENABLED) != 0;
		if (watchpoint.isAccess()) {
			if (watchpoint.isModification()) {
				//access and modification
				if (enabled) {
					descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_WATCHPOINT), flags);
				} else {
					descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_WATCHPOINT_DISABLED), flags);
				}
			} else {
				if (enabled) {
					descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_ACCESS_WATCHPOINT), flags);
				} else {
					descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_ACCESS_WATCHPOINT_DISABLED), flags);
				}
			}
		} else if (watchpoint.isModification()) {
			if (enabled) {
				descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_MODIFICATION_WATCHPOINT), flags);
			} else {
				descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_MODIFICATION_WATCHPOINT_DISABLED), flags);
			}
		} else {
			//neither access nor modification
			descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_WATCHPOINT_DISABLED), flags);
		}
		return getDebugImageRegistry().get(descriptor);
	}
	
	protected Image getVariableImage(IAdaptable element) {
		JavaElementImageDescriptor descriptor= new JavaElementImageDescriptor(
			computeBaseImageDescriptor(element), computeAdornmentFlags(element), BIG_SIZE);

		return getJavaElementImageRegistry().get(descriptor);			
	}
	
	/**
	 * Returns the image associated with reference variables being used to display
	 * references to a root object.
	 * 
	 * @param element
	 * @return image associated with reference variables
	 */
	protected Image getReferencesImage(Object element){
		return JavaDebugImages.get(JavaDebugImages.IMG_ELCL_ALL_REFERENCES);
	}
	
	/**
	 * Returns the image associated with reference variables being used to display
	 * references to a root object.
	 * 
	 * @param element
	 * @return image associated with reference variables
	 */
	protected Image getReferenceImage(Object element){
		return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_REFERENCE);
	}	
	
	/**
	 * Returns the image associated with the given element or <code>null</code>
	 * if none is defined.
	 */
	protected Image getDebugElementImage(Object element) {
		ImageDescriptor image= null;
		if (element instanceof IJavaThread) {
			IJavaThread thread = (IJavaThread)element;
			// image also needs to handle suspended quiet
			if (thread.isSuspended() && !thread.isPerformingEvaluation() && !(thread instanceof JDIThread && ((JDIThread)thread).isSuspendVoteInProgress())) {
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
		return getDebugImageRegistry().get(descriptor);
	}
	
	/**
	 * Returns the image associated with the given element or <code>null</code>
	 * if none is defined.
	 */
	protected Image getExpressionImage(Object expression) {
		ImageDescriptor image= null;
		boolean bigSize = false;
		if (expression instanceof JavaInspectExpression) {
			image= JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_OBJ_JAVA_INSPECT_EXPRESSION);
			bigSize = true;
		}
		if (image == null) {
			return null;
		}
		JDIImageDescriptor descriptor= new JDIImageDescriptor(image, 0);
		if (bigSize) {
			descriptor.setSize(BIG_SIZE);
		}
		return getDebugImageRegistry().get(descriptor);
	}

	/**
	 * Returns the adornment flags for the given element.
	 * These flags are used to render appropriate overlay
	 * icons for the element.
	 */
	private int computeJDIAdornmentFlags(Object element) {
		try {
			if (element instanceof IJavaStackFrame) {
				IJavaStackFrame javaStackFrame = ((IJavaStackFrame)element);
				if (javaStackFrame.isOutOfSynch()) {
					return JDIImageDescriptor.IS_OUT_OF_SYNCH;
				}
				if (!javaStackFrame.isObsolete() && javaStackFrame.isSynchronized()) {
					return JDIImageDescriptor.SYNCHRONIZED;
				}
			}
			if (element instanceof IJavaThread) {
				int flag= 0;
				IJavaThread javaThread = ((IJavaThread)element);
				if (ThreadMonitorManager.getDefault().isInDeadlock(javaThread)) {
					flag= JDIImageDescriptor.IN_DEADLOCK;
				}
				if (javaThread.isOutOfSynch()) {
					return flag | JDIImageDescriptor.IS_OUT_OF_SYNCH;
				}
				if (javaThread.mayBeOutOfSynch()) {
					return flag | JDIImageDescriptor.MAY_BE_OUT_OF_SYNCH;
				}
				return flag;
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
		    // no need to log errors - elements may no longer exist by the time we render them
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
		}
		return flags;
	}
	
	private ImageDescriptor computeBaseImageDescriptor(IAdaptable element) {
		IJavaVariable javaVariable= (IJavaVariable) element.getAdapter(IJavaVariable.class);
		if (javaVariable != null) {
			try {
				if (javaVariable.isLocal())
					return JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_OBJS_LOCAL_VARIABLE);
				if (javaVariable.isPublic())
					return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PUBLIC);
				if (javaVariable.isProtected())
					return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PROTECTED);
				if (javaVariable.isPrivate())
					return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PRIVATE);
			} catch (DebugException e) {
			    // no need to log errors - elements may no longer exist by the time we render them
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
			// no need to log errors - elements may no longer exist by the time we render them
		}
		return flags;
	}

	/**
	 * @see IDebugModelPresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput(Object item) {
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
		// for types that correspond to external files, return null so we do not
		// attempt to open a non-existing workspace file on the breakpoint (bug 184934)
		if (item instanceof IType) {
			IType type = (IType) item;
			if (!type.exists()) {
				return null;
			}
		}
		return EditorUtility.getEditorInput(item);
	}

	/**
	 * @see IDebugModelPresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId(IEditorInput input, Object inputObject) {
		try {
			IEditorDescriptor descriptor= IDE.getEditorDescriptor(input.getName());
			return descriptor.getId();
		} catch (PartInitException e) {
			return null;
		}
	}

	/**
	 * @see IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute(String id, Object value) {
		if (value == null) {
			return;
		}
		synchronized (fAttributes) {
			fAttributes.put(id, value);
		}
	}

	protected boolean isShowQualifiedNames() {
		synchronized (fAttributes) {
			Boolean showQualified= (Boolean) fAttributes.get(DISPLAY_QUALIFIED_NAMES);
			showQualified= showQualified == null ? Boolean.FALSE : showQualified;
			return showQualified.booleanValue();
		}
	}

	protected boolean isShowVariableTypeNames() {
		synchronized (fAttributes) {
			Boolean show= (Boolean) fAttributes.get(DISPLAY_VARIABLE_TYPE_NAMES);
			show= show == null ? Boolean.FALSE : show;
			return show.booleanValue();
		}
	}

	protected boolean isShowHexValues() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_SHOW_HEX);
	}

	protected boolean isShowCharValues() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_SHOW_CHAR);
	}

	protected boolean isShowUnsignedValues() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED);
	}

	protected String getVariableText(IJavaVariable var) {
		String varLabel= DebugUIMessages.JDIModelPresentation_unknown_name__1; 
		try {
			varLabel= var.getName();
		} catch (DebugException exception) {
		}

		IJavaValue javaValue= null;
		try {
			javaValue = (IJavaValue) var.getValue();
		} catch (DebugException e1) {
		}
		boolean showTypes= isShowVariableTypeNames();
		StringBuffer buff= new StringBuffer();
		String typeName= DebugUIMessages.JDIModelPresentation_unknown_type__2; 
		try {
			typeName= var.getReferenceTypeName();
			if (showTypes) {
				typeName= getQualifiedName(typeName);
			}
		} catch (DebugException exception) {
		}
		if (showTypes) {
			buff.append(typeName);
			buff.append(' ');
		}
		buff.append(varLabel);

		// add declaring type name if required
		if (var instanceof IJavaFieldVariable) {
			IJavaFieldVariable field = (IJavaFieldVariable)var;
			if (isDuplicateName(field)) {
				try {
					String decl = field.getDeclaringType().getName();
					buff.append(NLS.bind(" ({0})", new String[]{getQualifiedName(decl)})); //$NON-NLS-1$
				} catch (DebugException e) {
				}
			}
		}
		
		String valueString= getFormattedValueText(javaValue);

		//do not put the equal sign for array partitions
		if (valueString.length() != 0) {
			buff.append("= "); //$NON-NLS-1$
			buff.append(valueString);
		}
		return buff.toString();
	}
	
	/**
	 * Returns text for the given value based on user preferences to display
	 * toString() details.
	 * 
	 * @param javaValue
	 * @return text
	 */
	public String getFormattedValueText(IJavaValue javaValue) {
		String valueString= DebugUIMessages.JDIModelPresentation_unknown_value__3; 
		if (javaValue != null) {
			if (isShowLabelDetails(javaValue)) {
	    		valueString = getVariableDetail(javaValue);
	    		if (valueString == null) {
	    			valueString = DebugUIMessages.JDIModelPresentation_unknown_value__3;
	    		}
			} else {
				try {
					valueString= getValueText(javaValue);
				} catch (DebugException exception) {
				}
			}
		}		
		return valueString;
	}
	
	/**
	 * Returns whether or not details should be shown in the
	 * label of the given variable.
	 * @param variable the variable
	 * @return whether or not details should be shown in the label
	 *  of the given variable
	 */
	public boolean isShowLabelDetails(IJavaValue value) {
		boolean showDetails= false;
		String details= JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(IJDIPreferencesConstants.PREF_SHOW_DETAILS);
		if (details != null) {
			if (details.equals(IJDIPreferencesConstants.INLINE_ALL)) {
				showDetails= true;
			} else if (details.equals(IJDIPreferencesConstants.INLINE_FORMATTERS)){
				try {
					IJavaType javaType = value.getJavaType();
					JavaDetailFormattersManager manager= JavaDetailFormattersManager.getDefault();
					DetailFormatter formatter = manager.getAssociatedDetailFormatter(javaType);
					showDetails= formatter != null && formatter.isEnabled();
				} catch (DebugException e) {
				}
			}
		}
		return showDetails;
	}
	
	/**
	 * Returns the detail value for the given variable or <code>null</code>
	 * if none can be computed.
	 * @param variable the variable to compute the detail for
	 * @return the detail value for the variable
	 */
	private String getVariableDetail(IJavaValue value) {
		final String[] detail= new String[1];
		final Object lock= new Object();
		computeDetail(value, new IValueDetailListener() {
		    /* (non-Javadoc)
		     * @see org.eclipse.debug.ui.IValueDetailListener#detailComputed(org.eclipse.debug.core.model.IValue, java.lang.String)
		     */
		    public void detailComputed(IValue computedValue, String result) {
		        synchronized (lock) {
		            detail[0]= result;
		            lock.notifyAll();
		        }
		    }
		});
		synchronized (lock) {
		    if (detail[0] == null) {
		        try {
		            lock.wait(5000);
		        } catch (InterruptedException e1) {
		            // Fall through
		        }
		    }
		}
		return detail[0];
	}

	protected String getExpressionText(IExpression expression) throws DebugException {
		boolean showTypes= isShowVariableTypeNames();
		StringBuffer buff= new StringBuffer();
		IJavaValue javaValue= (IJavaValue) expression.getValue();
		if (javaValue != null) {
			String typeName=null;
			try {
				typeName= javaValue.getReferenceTypeName();
			} catch (DebugException exception) {
				// ObjectCollectedException is an expected exception which will
				// occur if the inspected object has been garbage collected.
				if (exception.getStatus().getException() instanceof ObjectCollectedException) {
					return DebugUIMessages.JDIModelPresentation__garbage_collected_object__6; 
				}
				throw exception;
			}
			if (showTypes ) {
				typeName= getQualifiedName(typeName);
				if (typeName.length() > 0) {
					buff.append(typeName);
					buff.append(' ');
				}
			}
		}
		// Edit the snippet to make it easily viewable in one line
		StringBuffer snippetBuffer = new StringBuffer();
		String snippet = expression.getExpressionText().trim();
		snippetBuffer.append('"');
		if (snippet.length() > 30){
			snippetBuffer.append(snippet.substring(0, 15));
			snippetBuffer.append(SnippetMessages.getString("SnippetEditor.ellipsis")); //$NON-NLS-1$
			snippetBuffer.append(snippet.substring(snippet.length() - 15));
		} else {
			snippetBuffer.append(snippet);
		}
		snippetBuffer.append('"');
		snippet = snippetBuffer.toString().replaceAll("[\n\r\t]+", " ");  //$NON-NLS-1$//$NON-NLS-2$
		buff.append(snippet);
		
		if (javaValue != null) {
			String valueString= getValueText(javaValue);
			if (valueString.length() > 0) {
				buff.append("= "); //$NON-NLS-1$
				buff.append(valueString);
			}
		}
		return buff.toString();
	}
	
	protected String getWatchExpressionText(IWatchExpression expression) throws DebugException {
		return getExpressionText(expression) + (expression.isEnabled() ? "" : DebugUIMessages.JDIModelPresentation_116); //$NON-NLS-1$ 
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
				int byteVal;
				try {
					byteVal= Integer.parseInt(value.getValueString());
				} catch (NumberFormatException e) {
					return null;
				}
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
		long longValue;
		char sigValue = sig.charAt(0);
		try {
			if (sigValue == 'C') {
				longValue = value.getValueString().charAt(0);
			} else {
				longValue= Long.parseLong(value.getValueString());
			}
		} catch (NumberFormatException e) {
			return null;
		}
		switch (sigValue) {
			case 'B' :
				buff.append("0x"); //$NON-NLS-1$
				// keep only the relevant bits for byte
				longValue &= 0xFF;
				buff.append(Long.toHexString(longValue));
				break;
			case 'I' :
				buff.append("0x"); //$NON-NLS-1$
				// keep only the relevant bits for integer
				longValue &= 0xFFFFFFFFl;
				buff.append(Long.toHexString(longValue));
				break;			
			case 'S' :
				buff.append("0x"); //$NON-NLS-1$
				// keep only the relevant bits for short
				longValue = longValue & 0xFFFF;
				buff.append(Long.toHexString(longValue));
				break;
			case 'J' :
				buff.append("0x"); //$NON-NLS-1$
				buff.append(Long.toHexString(longValue));
				break;
			case 'C' :
				buff.append("\\u"); //$NON-NLS-1$
				String hexString= Long.toHexString(longValue);
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

	@SuppressWarnings("deprecation")
	protected String getBreakpointText(IBreakpoint breakpoint) {
	    try {
	    	String label = null;
			if (breakpoint instanceof IJavaExceptionBreakpoint) {
				label = getExceptionBreakpointText((IJavaExceptionBreakpoint)breakpoint);
			} else if (breakpoint instanceof IJavaWatchpoint) {
				label = getWatchpointText((IJavaWatchpoint)breakpoint);
			} else if (breakpoint instanceof IJavaMethodBreakpoint) {
				label = getMethodBreakpointText((IJavaMethodBreakpoint)breakpoint);
			} else if (breakpoint instanceof IJavaPatternBreakpoint) {
				label = getJavaPatternBreakpointText((IJavaPatternBreakpoint)breakpoint);
			} else if (breakpoint instanceof IJavaTargetPatternBreakpoint) {
				label = getJavaTargetPatternBreakpointText((IJavaTargetPatternBreakpoint)breakpoint);
			} else if (breakpoint instanceof IJavaStratumLineBreakpoint) {
				label = getJavaStratumLineBreakpointText((IJavaStratumLineBreakpoint)breakpoint);
			} else if (breakpoint instanceof IJavaLineBreakpoint) {
				label = getLineBreakpointText((IJavaLineBreakpoint)breakpoint);
			} else if (breakpoint instanceof IJavaClassPrepareBreakpoint) {
				label = getClassPrepareBreakpointText((IJavaClassPrepareBreakpoint)breakpoint);
			} else {
				// Should never get here
				return ""; //$NON-NLS-1$
			}
			String suffix = breakpoint.getMarker().getAttribute(BREAKPOINT_LABEL_SUFFIX, null);
			if (suffix == null) {
				return label;
			}
			StringBuffer buffer = new StringBuffer(label);
			buffer.append(suffix);
			return buffer.toString();
	    } catch (CoreException e) {
	    	// if the breakpoint has been deleted, don't log exception
	    	IMarker marker = breakpoint.getMarker();
			if (marker == null || !marker.exists()) {
	    		return DebugUIMessages.JDIModelPresentation_6; 
	    	}
	        JDIDebugUIPlugin.log(e);
	        return DebugUIMessages.JDIModelPresentation_4; 
	    }
	}

	/**
	 * @param breakpoint
	 * @return
	 */
	private String getJavaStratumLineBreakpointText(IJavaStratumLineBreakpoint breakpoint) throws CoreException {
		IMember member= BreakpointUtils.getMember(breakpoint);
		String sourceName = breakpoint.getSourceName();
		if (sourceName == null) {
		    sourceName = ""; //$NON-NLS-1$
		    IMarker marker = breakpoint.getMarker();
		    if (marker != null) {
		        IResource resource = marker.getResource();
		        if (resource.getType() == IResource.FILE) {
		            sourceName = resource.getName();
		        }
		    }
		}
		StringBuffer label= new StringBuffer(sourceName);
		appendLineNumber(breakpoint, label);
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint,label);
		appendThreadFilter(breakpoint, label);
					
		if (member != null) {
			label.append(" - "); //$NON-NLS-1$
			label.append(getJavaLabelProvider().getText(member));
		}
		
		return label.toString();
	}

	protected String getExceptionBreakpointText(IJavaExceptionBreakpoint breakpoint) throws CoreException {
		StringBuffer buffer = new StringBuffer();
		String typeName = breakpoint.getTypeName();
		buffer.append(getQualifiedName(typeName));
		appendHitCount(breakpoint, buffer);
		appendSuspendPolicy(breakpoint, buffer);
		//TODO remove the cast once the API freeze has thawed
		if(((JavaExceptionBreakpoint)breakpoint).isSuspendOnSubclasses()) {
			buffer.append(DebugUIMessages.JDIModelPresentation_117);
		}
		appendThreadFilter(breakpoint, buffer);
		if (breakpoint.getExclusionFilters().length > 0 || breakpoint.getInclusionFilters().length > 0) {
			buffer.append(DebugUIMessages.JDIModelPresentation___scoped__1); 
		}
		appendInstanceFilter(breakpoint, buffer);
		String state= null;
		boolean c= breakpoint.isCaught();
		boolean u= breakpoint.isUncaught();
		if (c && u) {
			state= DebugUIMessages.JDIModelPresentation_caught_and_uncaught_60; 
		} else if (c) {
			state= DebugUIMessages.JDIModelPresentation_caught_61; 
		} else if (u) {
			state= DebugUIMessages.JDIModelPresentation_uncaught_62; 
		}
		String label= null;
		if (state == null) {
			label= buffer.toString();
		} else {
			String format= DebugUIMessages.JDIModelPresentation__1____0__63; 
			label= NLS.bind(format, new Object[] {state, buffer});
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
		
		if (member != null) {
			label.append(" - "); //$NON-NLS-1$
			label.append(getJavaLabelProvider().getText(member));
		}
		
		return label.toString();
	}
	
	protected String getClassPrepareBreakpointText(IJavaClassPrepareBreakpoint breakpoint) throws CoreException {
		String typeName= breakpoint.getTypeName();
		StringBuffer label = new StringBuffer();
		label.append(getQualifiedName(typeName));
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint, label);
		return label.toString();
	}	
	
	protected StringBuffer appendLineNumber(IJavaLineBreakpoint breakpoint, StringBuffer label) throws CoreException {
		int lineNumber= breakpoint.getLineNumber();
		if (lineNumber > 0) {
			label.append(" ["); //$NON-NLS-1$
			label.append(DebugUIMessages.JDIModelPresentation_line__65); 
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
			label.append(DebugUIMessages.JDIModelPresentation_hit_count__67); 
			label.append(' ');
			label.append(hitCount);
			label.append(']');
		}
		return label;
	}
	
	protected String getJavaPatternBreakpointText(@SuppressWarnings("deprecation") IJavaPatternBreakpoint breakpoint) throws CoreException {
		IResource resource= breakpoint.getMarker().getResource();
		IMember member= BreakpointUtils.getMember(breakpoint);
		StringBuffer label= new StringBuffer(resource.getName());
		appendLineNumber(breakpoint, label);
		appendHitCount(breakpoint, label);
		appendSuspendPolicy(breakpoint,label);
		appendThreadFilter(breakpoint, label);
					
		if (member != null) {
			label.append(" - "); //$NON-NLS-1$
			label.append(getJavaLabelProvider().getText(member));
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
			label.append(getJavaLabelProvider().getText(member));
		}
		
		return label.toString();
	}
		
	protected String getWatchpointText(IJavaWatchpoint watchpoint) throws CoreException {
		
		String typeName= watchpoint.getTypeName();
		IMember member= BreakpointUtils.getMember(watchpoint);
		StringBuffer label= new StringBuffer();
		label.append(getQualifiedName(typeName));
		appendHitCount(watchpoint, label);
		appendSuspendPolicy(watchpoint,label);
		appendThreadFilter(watchpoint, label);
		

		boolean access= watchpoint.isAccess();
		boolean modification= watchpoint.isModification();
		if (access && modification) {
			label.append(DebugUIMessages.JDIModelPresentation_access_and_modification_70); 
		} else if (access) {
			label.append(DebugUIMessages.JDIModelPresentation_access_71); 
		} else if (modification) {
			label.append(DebugUIMessages.JDIModelPresentation_modification_72); 
		}
		
		label.append(" - "); //$NON-NLS-1$
		if (member != null) {
			label.append(getJavaLabelProvider().getText(member));
		} else {
			label.append(watchpoint.getFieldName());
		}

		return label.toString();	
	}	

	protected String getMethodBreakpointText(IJavaMethodBreakpoint methodBreakpoint) throws CoreException {
		
		String typeName= methodBreakpoint.getTypeName();
		IMember member= BreakpointUtils.getMember(methodBreakpoint);
		StringBuffer label= new StringBuffer();
		label.append(getQualifiedName(typeName));
		appendHitCount(methodBreakpoint, label);
		appendSuspendPolicy(methodBreakpoint,label);
		appendThreadFilter(methodBreakpoint, label);
		

		boolean entry = methodBreakpoint.isEntry();
		boolean exit = methodBreakpoint.isExit();
		if (entry && exit) {
			label.append(DebugUIMessages.JDIModelPresentation_entry_and_exit); 
		} else if (entry) {
			label.append(DebugUIMessages.JDIModelPresentation_entry); 
		} else if (exit) {
			label.append(DebugUIMessages.JDIModelPresentation_exit); 
		}
		appendConditional(methodBreakpoint, label);
		
		if (member != null) {
			label.append(" - "); //$NON-NLS-1$
			label.append(getJavaLabelProvider().getText(member));
		} else {
			String methodSig= methodBreakpoint.getMethodSignature();
			String methodName= methodBreakpoint.getMethodName();
			if (methodSig != null) {
				label.append(" - "); //$NON-NLS-1$
				label.append(Signature.toString(methodSig, methodName, null, false, false));
			} else if (methodName != null) {
				label.append(" - "); //$NON-NLS-1$
				label.append(methodName);
			}
		}

		return label.toString();	
	}	

	protected String getStackFrameText(IStackFrame stackFrame) throws DebugException {
		IJavaStackFrame frame= (IJavaStackFrame) stackFrame.getAdapter(IJavaStackFrame.class);
		if (frame != null) {
			StringBuffer label= new StringBuffer();
			
			String dec= DebugUIMessages.JDIModelPresentation_unknown_declaring_type__4; 
			try {
				dec= frame.getDeclaringTypeName();
			} catch (DebugException exception) {
			}
			if (frame.isObsolete()) {
				label.append(DebugUIMessages.JDIModelPresentation__obsolete_method_in__1); 
				label.append(dec);
				label.append('>');
				return label.toString();
			}
			
			boolean javaStratum= true;
			try {
				javaStratum = frame.getReferenceType().getDefaultStratum().equals("Java"); //$NON-NLS-1$
			} catch (DebugException e) {
			}
			
			if (javaStratum) {
				// receiver name
				String rec= DebugUIMessages.JDIModelPresentation_unknown_receiving_type__5; 
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
					label.append(DebugUIMessages.JDIModelPresentation_unknown_method_name__6); 
				}
				try {
					List<String> args= frame.getArgumentTypeNames();
					if (args.isEmpty()) {
						label.append("()"); //$NON-NLS-1$
					} else {
						label.append('(');
						Iterator<String> iter= args.iterator();
						while (iter.hasNext()) {
							label.append(getQualifiedName(iter.next()));
							if (iter.hasNext()) {
								label.append(", "); //$NON-NLS-1$
							} else if (frame.isVarArgs()) {
								label.replace(label.length() - 2, label.length(), "..."); //$NON-NLS-1$
							}
						}
						label.append(')');
					}
				} catch (DebugException exception) {
					label.append(DebugUIMessages.JDIModelPresentation__unknown_arguements___7); 
				}
			} else {
				if (isShowQualifiedNames()) {
					label.append(frame.getSourcePath());
				} else {
					label.append(frame.getSourceName());
				}
			}

			try {
				int lineNumber= frame.getLineNumber();
				label.append(' ');
				label.append(DebugUIMessages.JDIModelPresentation_line__76); 
				label.append(' ');
				if (lineNumber >= 0) {
					label.append(lineNumber);
				} else {
					label.append(DebugUIMessages.JDIModelPresentation_not_available); 
					if (frame.isNative()) {
						label.append(' ');
						label.append(DebugUIMessages.JDIModelPresentation_native_method); 
					}
				}
			} catch (DebugException exception) {
				label.append(DebugUIMessages.JDIModelPresentation__unknown_line_number__8); 
			}
			
			if (!frame.wereLocalsAvailable()) {
				label.append(' ');
				label.append(DebugUIMessages.JDIModelPresentation_local_variables_unavailable); 
			}
			
			return label.toString();

		}
		return null;
	}
	
	protected String getQualifiedName(String qualifiedName) {
		if (!isShowQualifiedNames()) {
			return removeQualifierFromGenericName(qualifiedName);
		}
		return qualifiedName;
	}
	
	/**
	 * Return the simple generic name from a qualified generic name
	 */
	public String removeQualifierFromGenericName(String qualifiedName) {
		if (qualifiedName.endsWith("...")) { //$NON-NLS-1$
			// handle variable argument name
			return removeQualifierFromGenericName(qualifiedName.substring(0, qualifiedName.length() - 3)) + "..."; //$NON-NLS-1$
		}
		if (qualifiedName.endsWith("[]")) { //$NON-NLS-1$
			// handle array type
			return removeQualifierFromGenericName(qualifiedName.substring(0, qualifiedName.length() - 2)) + "[]"; //$NON-NLS-1$
		}
		// check if the type has parameters
		int parameterStart= qualifiedName.indexOf('<');
		if (parameterStart == -1) {
			return getSimpleName(qualifiedName);
		}
		// get the list of the parameters and generates their simple name
		List<String> parameters= getNameList(qualifiedName.substring(parameterStart + 1, qualifiedName.length() - 1));
		StringBuffer name= new StringBuffer(getSimpleName(qualifiedName.substring(0, parameterStart)));
		name.append('<');
		Iterator<String> iterator= parameters.iterator();
		if (iterator.hasNext()) {
			name.append(removeQualifierFromGenericName(iterator.next()));
			while (iterator.hasNext()) {
				name.append(',').append(removeQualifierFromGenericName(iterator.next()));
			}
		}
		name.append('>');
		return name.toString();
	}
	
	/**
	 * Return the simple name from a qualified name (non-generic)
	 */
	private String getSimpleName(String qualifiedName) {
		int index = qualifiedName.lastIndexOf('.');
		if (index >= 0) {
			return qualifiedName.substring(index + 1);
		}
		return qualifiedName;
	}

	/**
	 * Decompose a comma separated list of generic names (String) to a list of generic names (List)
	 */
	private List<String> getNameList(String listName) {
		List<String> names= new ArrayList<String>();
		StringTokenizer tokenizer= new StringTokenizer(listName, ",<>", true); //$NON-NLS-1$
		int enclosingLevel= 0;
		int startPos= 0;
		int currentPos= 0;
		while (tokenizer.hasMoreTokens()) {
			String token= tokenizer.nextToken();
			switch (token.charAt(0)) {
				case ',':
					if (enclosingLevel == 0) {
						names.add(listName.substring(startPos, currentPos));
						startPos= currentPos + 1;
					}
					break;
				case '<':
					enclosingLevel++;
					break;
				case '>':
					enclosingLevel--;
					break;
			}
			currentPos += token.length();
		}
		names.add(listName.substring(startPos));
		return names;
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
		return NLS.bind(string, args);
	}
	
	interface IValueDetailProvider {
		public void computeDetail(IValue value, IJavaThread thread, IValueDetailListener listener) throws DebugException;
	}
	
	protected void appendSuspendPolicy(IJavaBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		if (breakpoint.getSuspendPolicy() == IJavaBreakpoint.SUSPEND_VM) {
			buffer.append(' ');
			buffer.append(DebugUIMessages.JDIModelPresentation_Suspend_VM); 
		}
	}
	
	protected void appendThreadFilter(IJavaBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		if (breakpoint.getThreadFilters().length != 0) {
			buffer.append(' ');
			buffer.append(DebugUIMessages.JDIModelPresentation_thread_filtered); 
		}
	}
	
	protected void appendConditional(IJavaLineBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		if (breakpoint.isConditionEnabled() && breakpoint.getCondition() != null) {
			buffer.append(' ');
			buffer.append(DebugUIMessages.JDIModelPresentation__conditional__2); 
		}
	}
	
	protected void appendInstanceFilter(IJavaBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		IJavaObject[] instances = breakpoint.getInstanceFilters();
		for (int i = 0; i < instances.length; i++) {
			String instanceText= instances[i].getValueString();
			if (instanceText != null) {
				buffer.append(' ');
				buffer.append(NLS.bind(DebugUIMessages.JDIModelPresentation_instance_1, new String[] {instanceText})); 
			}				
		}
	}
	
	protected static ImageDescriptorRegistry getJavaElementImageRegistry() {
		if (fgJavaElementImageRegistry == null) {
			fgJavaElementImageRegistry = JavaPlugin.getImageDescriptorRegistry();		
		}
		return fgJavaElementImageRegistry;
	}

	protected static org.eclipse.jdt.internal.debug.ui.ImageDescriptorRegistry getDebugImageRegistry() {
		if (fgDebugImageRegistry == null) {
			fgDebugImageRegistry = JDIDebugUIPlugin.getImageDescriptorRegistry();		
		}
		return fgDebugImageRegistry;
	}

	protected JavaElementLabelProvider getJavaLabelProvider() {
		if (fJavaLabelProvider == null) {
			fJavaLabelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);		
		}
		return fJavaLabelProvider;
	}
	
	/**
	 * Returns whether the given field variable has the same name as any variables
	 */
	protected boolean isDuplicateName(IJavaFieldVariable variable) {
		IJavaReferenceType javaType= variable.getReceivingType();
		try {
			String[] names = javaType.getAllFieldNames();
			boolean found= false;
			for (int i = 0; i < names.length; i++) {
				if (variable.getName().equals(names[i])) {
					if (found) {
						return true;
					}
					found= true;
				}
			}
			return false;
		} catch (DebugException e) {
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (element instanceof JavaContendedMonitor && ((JavaContendedMonitor)element).getMonitor().isInDeadlock()) {
			return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(IJDIPreferencesConstants.PREF_THREAD_MONITOR_IN_DEADLOCK_COLOR);
		}
		if (element instanceof JavaOwnedMonitor && ((JavaOwnedMonitor)element).getMonitor().isInDeadlock()) {
			return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(IJDIPreferencesConstants.PREF_THREAD_MONITOR_IN_DEADLOCK_COLOR);
		}
		if (element instanceof JavaWaitingThread && ((JavaWaitingThread)element).getThread().isInDeadlock()) {
			return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(IJDIPreferencesConstants.PREF_THREAD_MONITOR_IN_DEADLOCK_COLOR);
		}
		if (element instanceof JavaOwningThread && ((JavaOwningThread)element).getThread().isInDeadlock()) {
			return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(IJDIPreferencesConstants.PREF_THREAD_MONITOR_IN_DEADLOCK_COLOR);
		}
		if (element instanceof IJavaThread && ThreadMonitorManager.getDefault().isInDeadlock((IJavaThread)element)) {
			return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(IJDIPreferencesConstants.PREF_THREAD_MONITOR_IN_DEADLOCK_COLOR);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		return null;
	}
	
	private ImageDescriptor getImageDescriptor(String key) {
		return JavaDebugImages.getImageDescriptor(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentationExtension#requiresUIThread(java.lang.Object)
	 */
	public synchronized boolean requiresUIThread(Object element) {
		return !isInitialized();
	}
}
