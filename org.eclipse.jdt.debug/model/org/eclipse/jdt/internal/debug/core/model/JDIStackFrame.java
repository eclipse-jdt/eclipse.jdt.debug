/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Type;
import com.sun.jdi.VirtualMachine;

/**
 * Proxy to a stack frame on the target.
 */

public class JDIStackFrame extends JDIDebugElement implements IJavaStackFrame {

	/**
	 * This frame's depth in the call stack
	 */
	private int fDepth;
	
	/**
	 * Underlying JDI stack frame.
	 */
	private StackFrame fStackFrame;
	/**
	 * The last (previous) underlying method.
	 */
	private Method fLastMethod;
	/**
	 * Containing thread.
	 */
	private JDIThread fThread;
	/**
	 * Visible variables.
	 */
	private List fVariables;
	/**
	 * The method this stack frame is associated with.
	 * Cached lazily on first access.
	 */
	private Method fMethod= null;
	/**
	 * The underlying Object associated with this stack frame.  
	 * Cached lazily on first access.
	 */
	private ObjectReference fThisObject;
	/**
	 * The name of the type in which the method for this stack frame was
	 * declared (implemented).  Cached lazily on first access.
	 */
	private String fDeclaringTypeName;
	/**
	 * The name of the type of the object that received the method call associated
	 * with this stack frame.  Cached lazily on first access.
	 */
	private String fReceivingTypeName;
	/**
	 * Whether the variables need refreshing
	 */
	private boolean fRefreshVariables= true;
	/**
	 * Whether this stack frame has been marked as out of synch. If set
	 * to <code>true</code> this stack frame will stop dynamically
	 * calculating its out of synch state.
	 */
	private boolean fIsOutOfSynch= false;
	/**
	 * The source name debug attribute. Cached lazily on first access.
	 */
	private String fSourceName;
	
	/**
	 * Whether local variable information was available
	 */
	private boolean fLocalsAvailable = true;

	/**
	 * Creates a new stack frame in the given thread.
	 * 
	 * @param thread The parent JDI thread
	 * @param stackFrame The underlying stack frame
	 */
	public JDIStackFrame(JDIThread thread, int depth) {
		super((JDIDebugTarget)thread.getDebugTarget());
		setDepth(depth);
		setThread(thread);
	}
	
	/**
	 * Sets this frame's depth in the call stack.
	 * 
	 * @param depth index in the call stack
	 */
	protected void setDepth(int depth) {
		fDepth = depth;
		clearCachedData();
		fRefreshVariables = true;
	}
	
	/**
	 * Returns this fame's depth in the call stack.
	 * 
	 * @return this frame's depth in the call stack
	 */
	protected int getDepth() {
		return fDepth;
	}
	
	/**
	 * @see IStackFrame#getThread()
	 */
	public IThread getThread() {
		return fThread;
	}

	/**
	 * @see ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return getThread().canResume();
	}

	/**
	 * @see ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return getThread().canSuspend();
	}

	/**
	 * @see IStep#canStepInto()
	 */
	public boolean canStepInto() {
		try {
			return exists() && isTopStackFrame() && !isObsolete() && getThread().canStepInto();
		} catch (DebugException e) {
			logError(e);
			return false;
		}
	}

	/**
	 * @see IStep#canStepOver()
	 */
	public boolean canStepOver() {
		try {
			return exists() && !isObsolete() && getThread().canStepOver();
		} catch (DebugException e) {
			logError(e);
			return false;
		}
	}

	/**
	 * @see IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		try {
			if (!exists() || isObsolete() || !getThread().canStepReturn()) {
				return false;
			}
			List frames = ((JDIThread)getThread()).computeStackFrames();
			if (frames != null && !frames.isEmpty()) {
				boolean bottomFrame = this.equals(frames.get(frames.size() - 1));
				boolean aboveObsoleteFrame= false;
				if (!bottomFrame) {
					int index= frames.indexOf(this);
					if (index < frames.size() -1 && ((JDIStackFrame)frames.get(index + 1)).isObsolete()) {
						aboveObsoleteFrame= true;
					}
				}
				return !bottomFrame && !aboveObsoleteFrame;
			}
		} catch (DebugException e) {
			logError(e);
		}
		return false;
	}

	/**
	 * Returns the underlying method associated with this stack frame,
	 * retreiving the method is necessary.
	 */
	public Method getUnderlyingMethod() throws DebugException {
		if (fStackFrame == null || fMethod == null) {
			try {
				fMethod= getUnderlyingStackFrame().location().method();
				fLastMethod = fMethod;
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_method"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
		}
		return fMethod;
	}

	/**
	 * @see IStackFrame#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		List list = getVariables0();
		return (IVariable[])list.toArray(new IVariable[list.size()]);
	}
	
	protected synchronized List getVariables0() throws DebugException {
		if (fVariables == null) {
			
			// throw exception if native method, so variable view will update
			// with information message
			if (isNative()) {
				requestFailed(JDIDebugModelMessages.getString("JDIStackFrame.Variable_information_unavailable_for_native_methods"), null); //$NON-NLS-1$
			}
			
			Method method= getUnderlyingMethod();
			fVariables= new ArrayList();
			// #isStatic() does not claim to throw any exceptions - so it is not try/catch coded
			if (method.isStatic()) {
				// add statics
				List allFields= null;
				ReferenceType declaringType = method.declaringType();
				try {
					allFields= declaringType.allFields();
				} catch (RuntimeException e) {
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_fields"),new String[] {e.toString()}), e); //$NON-NLS-1$
					// execution will not reach this line, as 
					// #targetRequestFailed will throw an exception					
					return Collections.EMPTY_LIST;
				}
				if (allFields != null) {
					Iterator fields= allFields.iterator();
					while (fields.hasNext()) {
						Field field= (Field) fields.next();
						if (field.isStatic()) {
							fVariables.add(new JDIFieldVariable((JDIDebugTarget)getDebugTarget(), field, declaringType));
						}
					}
					Collections.sort(fVariables, new Comparator() {
						public int compare(Object a, Object b) {
							JDIFieldVariable v1= (JDIFieldVariable)a;
							JDIFieldVariable v2= (JDIFieldVariable)b;
							try {
								return v1.getName().compareToIgnoreCase(v2.getName());
							} catch (DebugException de) {
								logError(de);
								return -1;
							}
						}
					});
				}
			} else {
				// add "this"
				ObjectReference t= getUnderlyingThisObject();
				if (t != null) {
					fVariables.add(new JDIThisVariable((JDIDebugTarget)getDebugTarget(), t));
				}
			}
			// add locals
			Iterator variables= getUnderlyingVisibleVariables().iterator();
			while (variables.hasNext()) {
				LocalVariable var= (LocalVariable) variables.next();
				fVariables.add(new JDILocalVariable(this, var));
			}
		} else if (fRefreshVariables) {
			updateVariables();
		}
		fRefreshVariables = false;
		return fVariables;
	}

	/**
	 * @see IStackFrame#getName()
	 */
	public String getName() throws DebugException {
		return getMethodName();
	}
	
	/**
	 * @see IJavaStackFrame#getArgumentTypeNames()
	 */
	public List getArgumentTypeNames() throws DebugException {
		try {
			return getUnderlyingMethod().argumentTypeNames();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_argument_type_names"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will never reach this line, as
			// #targetRequestFailed will throw an exception			
			return null;
		}
	}

	/**
	 * @see IStackFrame#getLineNumber()
	 */
	public int getLineNumber() throws DebugException {
		if (isSuspended()) {
			try {
				return getUnderlyingStackFrame().location().lineNumber();
			} catch (RuntimeException e) {
				if (getThread().isSuspended()) {
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_line_number"), new String[] {e.toString()}), e); //$NON-NLS-1$
				}
			}
		}
		return -1;
	}

	/**
	 * @see IStep#isStepping()
	 */
	public boolean isStepping() {
		return getThread().isStepping();
	}

	/**
	 * @see ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return getThread().isSuspended();
	}

	/**
	 * @see ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		getThread().resume();
	}

	/**
	 * @see IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		if (!canStepInto()) {
			return;
		}
		getThread().stepInto();
	}

	/**
	 * @see IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		if (!canStepOver()) {
			return;
		}
		if (isTopStackFrame()) {
			getThread().stepOver();
		} else {
			((JDIThread)getThread()).stepToFrame(this);
		}
	}

	/**
	 * @see IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		if (!canStepReturn()) {
			return;
		}
		if (isTopStackFrame()) {
			getThread().stepReturn();
		} else {
			List frames = ((JDIThread)getThread()).computeStackFrames();
			int index = frames.indexOf(this);
			if (index >= 0 && index < frames.size() - 1) {
				IStackFrame nextFrame = (IStackFrame)frames.get(index + 1);
				((JDIThread)getThread()).stepToFrame(nextFrame);
			}
		}
	}

	/**
	 * @see ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		getThread().suspend();
	}

	/**
	 * Incrementally updates this stack frames variables.
	 * 
	 * @see JDIDebugElement#targetRequestFailed(String, RuntimeException)
	 */
	protected void updateVariables() throws DebugException {
		if (fVariables == null) {
			return;
		}

		Method method= getUnderlyingMethod();
		int index= 0;
		if (!method.isStatic()) {
			// update "this"
			ObjectReference thisObject;
			try {
				thisObject= getUnderlyingThisObject();
			} catch (DebugException exception) {
				if (!getThread().isSuspended()) {
					thisObject= null;
				} else {
					throw exception;
				}
			}
			JDIThisVariable oldThisObject= null;
			if (!fVariables.isEmpty() && fVariables.get(0) instanceof JDIThisVariable) {
				oldThisObject= (JDIThisVariable) fVariables.get(0);
			}
			if (thisObject == null && oldThisObject != null) {
				// removal of 'this'
				fVariables.remove(0);
				index= 0;
			} else {
				if (oldThisObject == null && thisObject != null) {
					// creation of 'this'
					oldThisObject= new JDIThisVariable((JDIDebugTarget)getDebugTarget(),thisObject);
					fVariables.add(0, oldThisObject);
					index= 1;
				} else {
					if (oldThisObject != null) {
						// 'this' still exists, replace with new 'this' if a different receiver
						if (!oldThisObject.retrieveValue().equals(thisObject)) {
							fVariables.remove(0);
							fVariables.add(0, new JDIThisVariable((JDIDebugTarget)getDebugTarget(),thisObject));
						}
						index= 1;
					}
				}
			}
		}

		List locals= null;
		try {
			locals= getUnderlyingStackFrame().visibleVariables();
		} catch (AbsentInformationException e) {
			locals= Collections.EMPTY_LIST;
		} catch (NativeMethodException e) {
			locals= Collections.EMPTY_LIST;
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_visible_variables"),new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as 
			// #targetRequestFailed will throw an exception			
			return;
		}
		int localIndex= -1;
		while (index < fVariables.size()) {
			Object var= fVariables.get(index);
			if (var instanceof JDILocalVariable) {
				JDILocalVariable local= (JDILocalVariable) fVariables.get(index);
				localIndex= locals.indexOf(local.getLocal());
				if (localIndex >= 0) {
					// update variable with new underling JDI LocalVariable
					local.setLocal((LocalVariable) locals.get(localIndex));
					locals.remove(localIndex);
					index++;
				} else {
					// remove variable
					fVariables.remove(index);
				}
			} else {
				//field variable of a static frame
				index++;
			}
		}

		// add any new locals
		Iterator newOnes= locals.iterator();
		while (newOnes.hasNext()) {
			JDILocalVariable local= new JDILocalVariable(this, (LocalVariable) newOnes.next());
			fVariables.add(local);
		}
	}

	/**
	 * @see IJavaStackFrame#supportsDropToFrame()
	 */
	public boolean supportsDropToFrame() {
		//FIXME 1GH3XDA: ITPDUI:ALL - Drop to frame hangs if after invoke
		JDIThread thread= (JDIThread) getThread();
		JDIDebugTarget target= (JDIDebugTarget)thread.getDebugTarget();
		try {
			if (!target.isAvailable() || !thread.isSuspended() || thread.isTerminated()) {
				return false;
			} 
			boolean j9Support= false;
			boolean jdkSupport= target.canPopFrames();
			VirtualMachine vm = getVM();
			if (vm == null) {
				return false;
			}
			try {
				j9Support= (thread.getUnderlyingThread() instanceof org.eclipse.jdi.hcr.ThreadReference) &&
						((org.eclipse.jdi.hcr.VirtualMachine)vm).canDoReturn();
			} catch (UnsupportedOperationException uoe) {
				j9Support= false;
			}
			
			if (jdkSupport || j9Support) {
				// Also ensure that this frame and no frames above this
				// frame are native. Unable to pop native stack frames.
				List frames= thread.computeStackFrames();
				if (jdkSupport) {
					// JDK 1.4 VMs are currently unable to pop the bottom
					// stack frame.
					if (frames.get(frames.size() - 1) == this) {
						return false;
					}
				}
				int index = 0;
				JDIStackFrame frame= null;
				while (index < frames.size()) {
					frame= (JDIStackFrame) frames.get(index);
					index++;
					if (frame.isNative()) {
						return false;
					}
					if (frame.equals(this)) {
						if (jdkSupport) {
							// JDK 1.4 VMs are currently unable to pop the
							// frame directly above a native frame
							if (index < frames.size() && ((JDIStackFrame)frames.get(index)).isNative()) {
								return false;
							}
						}
						return true;
					}
				}
			}
			return false;
		} catch (DebugException e) {
			logError(e);
		} catch (UnsupportedOperationException e) {
			// drop to frame not supported - this is an expected
			// exception for VMs that do not support drop to frame
			return false;
		} catch (RuntimeException e) {
			internalError(e);
		}
		return false;
	}

	/**
	 * @see IJavaStackFrame#dropToFrame()
	 */
	public void dropToFrame() throws DebugException {
		if (supportsDropToFrame()) {
			((JDIThread) getThread()).dropToFrame(this);
		} else {
			notSupported(JDIDebugModelMessages.getString("JDIStackFrame.Drop_to_frame_not_supported")); //$NON-NLS-1$
		}
	}

	public void popFrame() throws DebugException {
		if (supportsDropToFrame()) {
			((JDIThread) getThread()).popFrame(this);
		} else {
			notSupported(JDIDebugModelMessages.getString("JDIStackFrame.pop_frame_not_supported")); //$NON-NLS-1$
		}
	}

	/**
	 * @see IJavaStackFrame#findVariable(String)
	 */
	public IJavaVariable findVariable(String varName) throws DebugException {
		if (isNative()) {
			return null;
		}
		IVariable[] variables = getVariables();
		IJavaVariable thisVariable= null;
		for (int i = 0; i < variables.length; i++) {
			IJavaVariable var = (IJavaVariable) variables[i];
			if (var.getName().equals(varName)) {
				return var;
			}
			if (var instanceof JDIThisVariable) {
				// save for later - check for instance and static vars
				thisVariable= var;
			}
		}

		if (thisVariable != null) {
			IVariable[] thisChildren = thisVariable.getValue().getVariables();
			for (int i = 0; i < thisChildren.length; i++) {
				IJavaVariable var= (IJavaVariable) thisChildren[i];
				if (var.getName().equals(varName)) {
					return var;
				}
			}
		}

		return null;

	}

	/**
	 * Retrieves visible variables in this stack frame
	 * handling any exceptions. Returns an empty list if there are no
	 * variables.
	 * 
	 * @see JDIDebugElement#targetRequestFailed(String, RuntimeException)
	 */
	protected List getUnderlyingVisibleVariables() throws DebugException {
		List variables= Collections.EMPTY_LIST;
		try {
			variables= getUnderlyingStackFrame().visibleVariables();
		} catch (AbsentInformationException e) {
			setLocalsAvailable(false);
		} catch (NativeMethodException e) {
			setLocalsAvailable(false);
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_visible_variables_2"),new String[] {e.toString()}), e); //$NON-NLS-1$
		}

		return variables;
	}

	/**
	 * Retrieves 'this' from the underlying stack frame.
	 * Returns <code>null</code> for static stack frames.
	 * 
	 * @see JDIDebugElement#targetRequestFailed(String, RuntimeException)
	 */
	protected ObjectReference getUnderlyingThisObject() throws DebugException {
		if (fStackFrame == null || fThisObject == null && !isStatic()) {
			try {
				fThisObject = getUnderlyingStackFrame().thisObject();
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_this"),new String[] {e.toString()}), e); //$NON-NLS-1$
				// execution will not reach this line, as 
				// #targetRequestFailed will throw an exception			
				return null;
			}
		}
		return fThisObject;
	}
	
	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaStackFrame.class || adapter == IJavaModifiers.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}
	
	/**
	 * @see IJavaStackFrame#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			return getUnderlyingMethod().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_method_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as 
			// #targetRequestFailed will throw an exception			
			return null;
		}
	}
	
	/**
	 * @see IJavaStackFrame#getDeclaringTypeName()
	 */
	public String getDeclaringTypeName() throws DebugException {
		if (fStackFrame == null || fDeclaringTypeName == null) {
			try {
				if (isObsolete()) {
					fDeclaringTypeName=  JDIDebugModelMessages.getString("JDIStackFrame.<unknown_declaring_type>_1"); //$NON-NLS-1$
				} else {
					fDeclaringTypeName= getUnderlyingMethod().declaringType().name();
				}
			} catch (RuntimeException e) {
				if (getThread().isSuspended()) {
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_declaring_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
				}
				return JDIDebugModelMessages.getString("JDIStackFrame.<unknown_declaring_type>_1"); //$NON-NLS-1$
			}
		}
		return fDeclaringTypeName;
	}
	
	/**
	 * @see IJavaStackFrame#getReceivingTypeName()
	 */
	public String getReceivingTypeName() throws DebugException {
		if (fStackFrame == null || fReceivingTypeName == null) {
			try {
				if (isObsolete()) {
					fReceivingTypeName=JDIDebugModelMessages.getString("JDIStackFrame.<unknown_receiving_type>_2"); //$NON-NLS-1$
				} else {
					ObjectReference thisObject = getUnderlyingThisObject();
					if (thisObject == null) {
						fReceivingTypeName = getDeclaringTypeName();
					} else {
						fReceivingTypeName = thisObject.referenceType().name();
					}
				}
			} catch (RuntimeException e) {
				if (getThread().isSuspended()) {
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_receiving_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
				}
				return JDIDebugModelMessages.getString("JDIStackFrame.<unknown_receiving_type>_2"); //$NON-NLS-1$
			}
		}
		return fReceivingTypeName;
	}
	
	/**
	 * @see IJavaStackFrame#getMethodName()
	 */
	public String getMethodName() throws DebugException {
		try {
			return getUnderlyingMethod().name();	
		} catch (RuntimeException e) {
			if (getThread().isSuspended()) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_method_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
			return JDIDebugModelMessages.getString("JDIStackFrame.<unknown_method>_1"); //$NON-NLS-1$
		}
	}
	
	/**
	 * @see IJavaStackFrame#isNative()
	 */
	public boolean isNative() throws DebugException {
		return getUnderlyingMethod().isNative();
	}
	
	/**
	 * @see IJavaStackFrame#isConstructor()
	 */
	public boolean isConstructor() throws DebugException {
		return getUnderlyingMethod().isConstructor();
	}
	
	/**
	 * @see IJavaStackFrame#isStaticInitializer()
	 */
	public boolean isStaticInitializer() throws DebugException {
		return getUnderlyingMethod().isStaticInitializer();
	}
	
	/**
	 * @see IJavaModifiers#isFinal()
	 */
	public boolean isFinal() throws DebugException {
		return getUnderlyingMethod().isFinal();
	}
	
	/**
	 * @see IJavaStackFrame#isSynchronized()
	 */
	public boolean isSynchronized() throws DebugException {
		return getUnderlyingMethod().isSynchronized();
	}
	
	/**
	 * @see IJavaModifiers#isSynthetic()
	 */
	public boolean isSynthetic() throws DebugException {
		return getUnderlyingMethod().isSynthetic();
	}
	
	/**
	 * @see IJavaModifiers#isPublic()
	 */
	public boolean isPublic() throws DebugException {
		return getUnderlyingMethod().isPublic();
	}
	
	/**
	 * @see IJavaModifiers#isPrivate()
	 */
	public boolean isPrivate() throws DebugException {
		return getUnderlyingMethod().isPrivate();
	}
	
	/**
	 * @see IJavaModifiers#isProtected()
	 */
	public boolean isProtected() throws DebugException {
		return getUnderlyingMethod().isProtected();
	}
	
	/**
	 * @see IJavaModifiers#isPackagePrivate()
	 */
	public boolean isPackagePrivate() throws DebugException {
		return getUnderlyingMethod().isPackagePrivate();
	}
	
	/**
	 * @see IJavaModifiers#isStatic()
	 */
	public boolean isStatic() throws DebugException {
		return getUnderlyingMethod().isStatic();
	}
		
	/**
	 * @see IJavaStackFrame#getSourceName()
	 */
	public String getSourceName() throws DebugException {
		if (fStackFrame == null || fSourceName == null) {
			try {
				fSourceName = getUnderlyingStackFrame().location().sourceName();
			} catch (AbsentInformationException e) {
				fSourceName = null;
			} catch (NativeMethodException e) {
				fSourceName = null;
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_source_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
		}
		return fSourceName;
	}
	
	protected boolean isTopStackFrame() throws DebugException {
		IStackFrame tos = getThread().getTopStackFrame();
		return tos != null && tos.equals(this);
	}
	
	/**
	 * Sets this stack frame to be out of synch.
	 * Note that passing <code>true</code> to this method
	 * marks this stack frame as out of synch permanently (statically).
	 */
	public void setOutOfSynch(boolean outOfSynch) {
		fIsOutOfSynch= outOfSynch;
	}
	
	/**
	 * @see IJavaStackFrame#isOutOfSynch()
	 */
	public boolean isOutOfSynch() throws DebugException {
		if (fIsOutOfSynch) {
			return true;
		}
		// if this frame's thread is not suspended, the out-of-synch info cannot
		// change until it suspends again
		if (getThread().isSuspended()) {
			JDIDebugTarget target= (JDIDebugTarget)getDebugTarget();
			if (target.hasHCROccurred() && target.isOutOfSynch(getUnderlyingMethod().declaringType().name())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @see IJavaStackFrame#isObsolete()
	 */
	public boolean isObsolete() throws DebugException {
		if (!JDIDebugPlugin.isJdiVersionGreaterThanOrEqual(new int[] {1,4}) || !((JDIDebugTarget)getDebugTarget()).hasHCROccurred()) {
			// If no hot code replace has occurred, this frame
			// cannot be obsolete.
			return false;
		}
		// if this frame's thread is not suspended, the obsolete status cannot
		// change until it suspends again
		if (getThread().isSuspended()) {
			try {
				return getUnderlyingMethod().isObsolete();
			} catch (RuntimeException re) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.Exception_occurred_determining_if_stack_frame_is_obsolete_1"), new String[] {re.toString()}), re); //$NON-NLS-1$
				// execution will not reach this line, as 
				// #targetRequestFailed will throw an exception			
				return true;
			}
		}
		return false;
	}
	
	protected boolean exists() throws DebugException {
		return ((JDIThread)getThread()).computeStackFrames().indexOf(this) != -1;
	}
	
	/**
	 * @see ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		boolean exists= false;
		try {
			exists= exists();
		} catch (DebugException e) {
			logError(e);
		}
		return exists && getThread().canTerminate() || getDebugTarget().canTerminate();
	}

	/**
	 * @see ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return getThread().isTerminated();
	}

	/**
	 * @see ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if (getThread().canTerminate()) {
			getThread().terminate();
		} else {
			getDebugTarget().terminate();
		}
	}
	
	/**
	 * Returns this stack frame's underlying JDI frame.
	 * 
	 * @exception DebugException if this stack frame does
	 *  not currently have an underlying frame (is in an
	 *  interim state where this frame's thead has been
	 *  resumed, and is not yet suspended).
	 */
	protected synchronized StackFrame getUnderlyingStackFrame() throws DebugException {
		if (fStackFrame == null) {
			int depth= getDepth();
			if (depth == -1) {
				// Depth is set to -1 when the thread clears its handles
				// to this object. See Bug 47198.
				throw new DebugException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IStatus.ERROR, JDIDebugModelMessages.getString("JDIStackFrame.25"), null)); //$NON-NLS-1$
			}
			setUnderlyingStackFrame(((JDIThread)getThread()).getUnderlyingFrame(depth));
		}
		return fStackFrame;
	}
	
	/**
	 * Sets the underlying JDI StackFrame. Called by a thread
	 * when incrementally updating after a step has completed.
	 * 
	 * @param frame The underlying stack frame
	 */
	protected synchronized void setUnderlyingStackFrame(StackFrame frame) {
		fStackFrame = frame;
	}
	
	/**
	 * The underlying method that existed before the current underlying
	 * method.  Used only so that equality can be checked on stack frame
	 * after the new one has been set.
	 */
	protected Method getLastMethod() {
		return fLastMethod;
	}

	protected void setThread(JDIThread thread) {
		fThread = thread;
	}

	protected void setVariables(List variables) {
		fVariables = variables;
	}
	
	/**
	 * @see IJavaStackFrame#getLocalVariables()
	 */
	public IJavaVariable[] getLocalVariables() throws DebugException {
		List list = getUnderlyingVisibleVariables();
		IJavaVariable[] locals = new IJavaVariable[list.size()];
		for (int i = 0; i < list.size(); i++) {
			locals[i] = new JDILocalVariable(this, (LocalVariable)list.get(i));
		}
		return locals;
	}

	/**
	 * @see IJavaStackFrame#getThis()
	 */
	public IJavaObject getThis() throws DebugException {
		IJavaObject receiver = null;
		if (!isStatic()) {
			ObjectReference thisObject = getUnderlyingThisObject();
			if (thisObject != null) {
				receiver = (IJavaObject)JDIValue.createValue((JDIDebugTarget)getDebugTarget(), thisObject);
			}
		}
		return receiver;
	}

	/**
	 * Java stack frames do not support registers
	 * 
	 * @see IStackFrame#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() {
		return new IRegisterGroup[0];
	}

	/**
	 * @see IJavaStackFrame#getDeclaringType()
	 */
	public IJavaClassType getDeclaringType() throws DebugException {
		Method method = getUnderlyingMethod();
		try {
			Type type = method.declaringType();
			return (IJavaClassType)JDIType.createType((JDIDebugTarget)getDebugTarget(), type);
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retreiving_declaring_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Expression level stepping not supported.
	 * 
	 * @see IStackFrame#getCharEnd()
	 */
	public int getCharEnd() {
		return -1;
	}

	/**
	 * Expression level stepping not supported.
	 * 
	 * @see IStackFrame#getCharStart()
	 */
	public int getCharStart() {
		return -1;
	}

	/**
	 * Clears the cached data of this stack frame.
	 * The underlying stack frame has changed in such a way
	 * that the cached data may not be valid.
	 */
	private void clearCachedData() {
		fMethod= null;
		fThisObject= null;
		fDeclaringTypeName= null;
		fReceivingTypeName= null;	
		fSourceName= null;
	}
	
	/**
	 * @see IJavaStackFrame#wereLocalsAvailable()
	 */
	public boolean wereLocalsAvailable() {
		return fLocalsAvailable;
	}
	
	/**
	 * Sets whether locals were available. If the setting is
	 * not the same as the current value, a change event is
	 * fired such that a UI client can update.
	 * 
	 * @param available whether local variable information is
	 * 	available for this stack frame.
	 */
	private void setLocalsAvailable(boolean available) {
		if (available != fLocalsAvailable) {
			fLocalsAvailable = available;
			fireChangeEvent(DebugEvent.STATE);
		}
	}	
	/**
	 * @see IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() {
		return false;
	}

	/**
	 * @see IStackFrame#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return getVariables0().size() > 0;
	}

	/**
	 * @see org.eclipse.debug.core.model.IFilteredStep#canStepWithFilters()
	 */
	public boolean canStepWithFilters() {
		if (canStepInto()) {
			String[] filters = getJavaDebugTarget().getStepFilters();
			return filters != null && filters.length > 0;
		}
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.IFilteredStep#stepWithFilters()
	 */
	public void stepWithFilters() throws DebugException {
		((IJavaThread)getThread()).stepWithFilters();
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaStackFrame#getSourcePath(java.lang.String)
	 */
	public String getSourcePath(String stratum) throws DebugException {
		try {
			return getUnderlyingStackFrame().location().sourcePath(stratum);
		} catch (AbsentInformationException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_source_path"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IJavaStackFrame#getSourcePath()
	 */
	public String getSourcePath() throws DebugException {
		try {
			return getUnderlyingStackFrame().location().sourcePath();
		} catch (AbsentInformationException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_source_path"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return null;
	}	

	/*
	 * @see org.eclipse.jdt.debug.core.IJavaStackFrame#getLineNumber(java.lang.String)
	 */
	public int getLineNumber(String stratum) throws DebugException {
		if (isSuspended()) {
			try {
				return getUnderlyingStackFrame().location().lineNumber(stratum);
			} catch (RuntimeException e) {
				if (getThread().isSuspended()) {
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_line_number"), new String[] {e.toString()}), e); //$NON-NLS-1$
				}
			}
		}
		return -1;
	}

	/*
	 * @see org.eclipse.jdt.debug.core.IJavaStackFrame#getSourceName(java.lang.String)
	 */
	public String getSourceName(String stratum) throws DebugException {
		try {
			return getUnderlyingStackFrame().location().sourceName(stratum);
		} catch (AbsentInformationException e) {
		} catch (NativeMethodException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_source_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return null;
	}

}
