package org.eclipse.jdt.internal.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Type;

/**
 * Proxy to a stack frame on the target.
 */

public class JDIStackFrame extends JDIDebugElement implements IJavaStackFrame {

	/**
	 * Underlying JDI stack frame.
	 */
	private StackFrame fStackFrame;
	/**
	 * The last (previous) underlying stack frame.
	 */
	private StackFrame fLastStackFrame;
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
	 * Whether this stack frame has been declared out of synch
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
	public JDIStackFrame(JDIThread thread, StackFrame stackFrame) {
		super((JDIDebugTarget)thread.getDebugTarget());
		setUnderlyingStackFrame(stackFrame);
		setThread(thread);
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
			if (isObsolete()) {
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
				return exists() && !bottomFrame && !aboveObsoleteFrame && getThread().canStepReturn();
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
	protected Method getUnderlyingMethod() throws DebugException {
		if (fMethod == null) {
			try {
				fMethod= getUnderlyingStackFrame().location().method();
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
			
			// throw exception if native method, so varaiable view will upate
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
				try {
					allFields= method.declaringType().allFields();
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
							fVariables.add(new JDIFieldVariable((JDIDebugTarget)getDebugTarget(), field, null));
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
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
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
				Location location= getUnderlyingStackFrame().location();
				if (location != null) {
					return location.lineNumber();
				}
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_line_number"), new String[] {e.toString()}), e); //$NON-NLS-1$
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
			ObjectReference thisObject= getUnderlyingThisObject();
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
						// 'this' still exists
						index= 1;
					}
				}
			}
		}

		List locals= null;
		try {
			locals= getUnderlyingStackFrame().visibleVariables();
		} catch (AbsentInformationException e) {
			locals= new ArrayList(0);
		} catch (NativeMethodException e) {
			locals= new ArrayList(0);
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
			boolean jdkSupport= getVM().canPopFrames();
			try {
				j9Support= (thread.getUnderlyingThread() instanceof org.eclipse.jdi.hcr.ThreadReference) &&
						((org.eclipse.jdi.hcr.VirtualMachine) ((JDIDebugTarget) getDebugTarget()).getVM()).canDoReturn();
			} catch (UnsupportedOperationException uoe) {
				j9Support= false;
			}
			
			if (jdkSupport || j9Support) {
				// Also ensure that this frame and no frames above this
				// frame are native. Unable to pop native stack frames.
				List frames= thread.computeStackFrames();
				if (jdkSupport) {
					// JDK VMs are currently unable to pop the bottom
					// stack frame.
					if (frames.get(frames.size() - 1) == this) {
						return false;
					}
				}
				Iterator iter= frames.iterator();
				JDIStackFrame frame= null;
				while (iter.hasNext()) {
					frame= (JDIStackFrame) iter.next();
					if (frame.isNative()) {
						return false;
					}
					if (frame.equals(this)) {
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
	public IVariable findVariable(String varName) throws DebugException {
		IVariable[] variables = getVariables();
		JDIThisVariable thisVariable= null;
		for (int i = 0; i < variables.length; i++) {
			IVariable var= (IVariable) variables[i];
			if (var.getName().equals(varName)) {
				return var;
			}
			if (var instanceof JDIThisVariable) {
				// save for later - check for instance and static vars
				thisVariable= (JDIThisVariable)var;
			}
		}

		if (thisVariable != null) {
			Iterator thisChildren = ((JDIValue)thisVariable.getValue()).getVariables0().iterator();
			while (thisChildren.hasNext()) {
				IVariable var= (IVariable) thisChildren.next();
				if (var.getName().equals(varName)) {
					return var;
				}
			}
		}

		return null;

	}

	/**
	 * Retrieves visible varialbes in this stack frame
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
		if (fThisObject == null && !isStatic()) {
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
		if (fDeclaringTypeName == null) {
			try {
				Method underlyingMethod= getUnderlyingMethod();
				if (underlyingMethod.isObsolete()) {
					fDeclaringTypeName=  JDIDebugModelMessages.getString("JDIStackFrame.<unknown_declaring_type>_1"); //$NON-NLS-1$
				} else {
					fDeclaringTypeName= getUnderlyingMethod().declaringType().name();
				}
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_declaring_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
				// execution will not reach this line, as 
				// #targetRequestFailed will throw an exception			
				return null;
			}
		}
		return getUnderlyingMethod().declaringType().name();
	}
	
	/**
	 * @see IJavaStackFrame#getReceivingTypeName()
	 */
	public String getReceivingTypeName() throws DebugException {
		if (fReceivingTypeName == null) {
			try {
				if (getUnderlyingMethod().isObsolete()) {
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
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_receiving_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
				// execution will not reach this line, as 
				// #targetRequestFailed will throw an exception			
				return null;
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
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_method_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as 
			// #targetRequestFailed will throw an exception			
			return null;
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
		if (fSourceName == null) {
			try {
				Location l = getUnderlyingMethod().location();
				if (l != null) {
					fSourceName = l.sourceName();
				}
			} catch (AbsentInformationException e) {
				fSourceName = ""; //$NON-NLS-1$
			} catch (NativeMethodException e) {
				fSourceName = ""; //$NON-NLS-1$
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_source_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
		}
		if (fSourceName == null || fSourceName.length() == 0) {
			return null;
		} else {
			return fSourceName;
		}
	}
	
	protected boolean isTopStackFrame() throws DebugException {
		IStackFrame tos = getThread().getTopStackFrame();
		return tos != null && tos.equals(this);
	}
	
	public void setOutOfSynch(boolean outOfSynch) {
		fIsOutOfSynch= outOfSynch;
		fireChangeEvent(DebugEvent.STATE);
	}
	
	/**
	 * @see IJavaStackFrame#isOutOfSynch()
	 */
	public boolean isOutOfSynch() throws DebugException {
		if (fIsOutOfSynch) {
			return true;
		}
		if (((JDIDebugTarget)getDebugTarget()).isOutOfSynch(getUnderlyingMethod().declaringType().name())) {
			fIsOutOfSynch= true;
			return true;
		}
		return false;
	}
	
	/**
	 * @see IJavaStackFrame#isObsolete()
	 */
	public boolean isObsolete() throws DebugException {
		if (!((JDIDebugTarget)getDebugTarget()).hasHCROccurred()) {
			// If no hot code replace has occurred, this frame
			// cannot be obsolete.
			return false;
		}
		try {
			return getUnderlyingMethod().isObsolete();
		} catch (RuntimeException re) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.Exception_occurred_determining_if_stack_frame_is_obsolete_1"), new String[] {re.toString()}), re); //$NON-NLS-1$
			// execution will not reach this line, as 
			// #targetRequestFailed will throw an exception			
			return true;
		}
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
	protected StackFrame getUnderlyingStackFrame() throws DebugException {
		if (fStackFrame == null) {
			// In the case of preserved stack frames, the underlying stack frame
			// could have been set via a call to JDIThread#updateStackFrames
			((JDIThread)getThread()).computeStackFrames();
			if (fStackFrame == null) {
				requestFailed(JDIDebugModelMessages.getString("JDIStackFrame.Thread_not_suspended,_stack_frame_unavailable._3"), null); //$NON-NLS-1$
			}
		}
		return fStackFrame;
	}
	
	/**
	 * The underlying stack frame that existed before the current underlying
	 * stack frame.  Used only so that equality can be checked on stack frame
	 * after the new one has been set.
	 */
	protected StackFrame getLastUnderlyingStackFrame() {
		return fLastStackFrame;
	}
	
	/**
	 * Sets the underlying JDI StackFrame. Called by a thread
	 * when incrementally updating after a step has completed.
	 * 
	 * @param frame The underlying stack frame
	 */
	protected void setUnderlyingStackFrame(StackFrame frame) {
		if (frame != null) {
			fLastStackFrame = frame;
		} else {
			fLastStackFrame = fStackFrame;
		}
		fStackFrame = frame;
		fRefreshVariables = true;
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
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
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
	protected void clearCachedData() {
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
}