package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.*;

import com.sun.jdi.*;

/**
 * Proxy to a stack frame on the target.
 */

public class JDIStackFrame extends JDIDebugElement implements IJavaStackFrame {

	/**
	 * Underlying stack frame
	 */
	protected StackFrame fStackFrame;
	/**
	 * Containing thread
	 */
	protected JDIThread fThread;
	/**
	 * Visible variables
	 */
	protected List fVariables;
	
	/**
	 * The method this stack frame is associated with. Cached
	 * lazily on first access.
	 */
	protected Method fMethod= null;
	
	/**
	 * Whether the variables need refreshing
	 */
	protected boolean fRefreshVariables= true;

	/**
	 * Creates a new stack frame in the given thread.
	 */
	public JDIStackFrame(JDIThread thread, StackFrame stackFrame) {
		super((JDIDebugTarget)thread.getDebugTarget());
		fStackFrame= stackFrame;
		fThread= thread;
	}
	
	/**
	 * @see IDebugElement
	 */
	public int getElementType() {
		return STACK_FRAME;
	}	
	
	
	/**
	 * @see IDebugElement
	 */
	public IThread getThread() {
		return fThread;
	}

	/**
	 * @see ISuspendResume
	 */
	public boolean canResume() {
		return getThread().canResume();
	}

	/**
	 * @see ISuspendResume
	 */
	public boolean canSuspend() {
		return getThread().canSuspend();
	}

	/**
	 * @see IStep.
	 */
	public boolean canStepInto() {
		try {
			return exists() && isTopStackFrame() && getThread().canStepInto();
		} catch (DebugException e) {
			return false;
		}
	}

	/**
	 * @see IStep.
	 */
	public boolean canStepOver() {
		try {
			return exists() && getThread().canStepOver();
		} catch (DebugException e) {
			return false;
		}
	}

	/**
	 * @see IStep.
	 */
	public boolean canStepReturn() {
		try {
			List frames = ((JDIThread)getThread()).getStackFrames0();
			if (frames != null && !frames.isEmpty()) {
				Object bottomFrame = frames.get(frames.size() - 1);
				return exists() && !this.equals(bottomFrame) && getThread().canStepReturn();
			}
		} catch (DebugException e) {
		}
		return false;
	}

	/**
	 * Returns the underlying method associated with this stack frame.
	 */
	Method getUnderlyingMethod() throws DebugException {
		if (fMethod == null) {
			try {
				fMethod= fStackFrame.location().method();
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_method"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
		}
		return fMethod;
	}

	/**
	 * @see IStackFrame
	 */
	public IVariable[] getVariables() throws DebugException {
		List list = getVariables0();
		return (IVariable[])list.toArray(new IVariable[list.size()]);
	}
	
	protected synchronized List getVariables0() throws DebugException {
		if (fVariables == null) {
			Method method= getUnderlyingMethod();
			fVariables= new ArrayList();
			// #isStatic() does not claim to throw any exceptions - so it is not try/catch coded
			if (method.isStatic()) {
				// add statics
				List allFields= null;
				try {
					allFields= method.declaringType().allFields();
				} catch (VMDisconnectedException e) {
					return Collections.EMPTY_LIST;
				} catch (RuntimeException e) {
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_fields"),new String[] {e.toString()}), e); //$NON-NLS-1$
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
							return sortStaticChildren(a, b);
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
	 * Sorts the static variable children lexically
	 */
	protected int sortStaticChildren(Object a, Object b) {
		JDIFieldVariable v1= (JDIFieldVariable)a;
		JDIFieldVariable v2= (JDIFieldVariable)b;
		try {
			return v1.getName().compareToIgnoreCase(v2.getName());
		} catch (DebugException de) {
			logError(de);
			return -1;
		}
	}
	/**
	 * @see IDebugElement
	 */
	public String getName() throws DebugException {
		return getMethodName();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public List getArgumentTypeNames() throws DebugException {
		try {
			return getUnderlyingMethod().argumentTypeNames();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_argument_type_names"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see IStackFrame
	 */
	public int getLineNumber() throws DebugException {
		if (getThread().isSuspended()) {
			try {
				Location location= fStackFrame.location();
				if (location != null) {
					return location.lineNumber();
				}
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_line_number"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
		}
		return -1;
	}

	/**
	 * @see IStep
	 */
	public boolean isStepping() {
		return getThread().isStepping();
	}

	/**
	 * @see ISuspendResume
	 */
	public boolean isSuspended() {
		return getThread().isSuspended();
	}

	/**
	 * @see ISuspendResume
	 */
	public void resume() throws DebugException {
		getThread().resume();
	}

	/**
	 * @see IStep.
	 */
	public void stepInto() throws DebugException {
		if (!canStepInto()) {
			return;
		}
		getThread().stepInto();
	}

	/**
	 * @see IStep
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
	 * @see IStep
	 */
	public void stepReturn() throws DebugException {
		if (!canStepReturn()) {
			return;
		}
		if (isTopStackFrame()) {
			getThread().stepReturn();
		} else {
			List frames = ((JDIThread)getThread()).getStackFrames0();
			int index = frames.indexOf(this);
			if (index >= 0 && index < frames.size() - 1) {
				IStackFrame nextFrame = (IStackFrame)frames.get(index + 1);
				((JDIThread)getThread()).stepToFrame(nextFrame);
			}
		}
	}

	/**
	 * @see ISuspendResume
	 */
	public void suspend() throws DebugException {
		getThread().suspend();
	}

	/**
	 * Notes that variables will need to be updated on
	 * the next access.
	 */
	protected void invalidateVariables() {
		fRefreshVariables = true;
	}
	
	/**
	 * Update my variables incrementally.
	 */
	protected void updateVariables() throws DebugException {
		if (fVariables == null) {
			return;
		}

		Method method= getUnderlyingMethod();
		int index= 0;
		if (method.isStatic()) {
			// update statics
			while (index < fVariables.size() && fVariables.get(index) instanceof JDIFieldVariable) {
				index++;
			}
		} else {
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
			locals= fStackFrame.visibleVariables();
		} catch (AbsentInformationException e) {
			locals= new ArrayList(0);
		} catch (NativeMethodException e) {
			locals= new ArrayList(0);
		} catch (VMDisconnectedException e) {
			return;
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_visible_variables"),new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		int localIndex= -1;
		while (index < fVariables.size()) {
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
		}

		// add any new locals
		Iterator newOnes= locals.iterator();
		while (newOnes.hasNext()) {
			JDILocalVariable local= new JDILocalVariable(this, (LocalVariable) newOnes.next());
			fVariables.add(local);
		}
	}

	/**
	 * @see IDropToFrame
	 */
	public boolean supportsDropToFrame() {
		try {
			JDIThread thread= (JDIThread) getThread();
			boolean supported = !thread.isTerminated()
				&& thread.isSuspended()
				&& thread.getUnderlyingThread() instanceof org.eclipse.jdi.hcr.ThreadReference
				&& ((org.eclipse.jdi.hcr.VirtualMachine) ((JDIDebugTarget) getDebugTarget()).getVM()).canDoReturn();
			if (supported) {
				// Also ensure that this frame and no frames above this
				// frame are native. Unable to pop native stack frames.
				IStackFrame[] frames = thread.getStackFrames();
				for (int i = 0; i < frames.length; i++) {
					JDIStackFrame frame = (JDIStackFrame)frames[i];
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
		} catch (UnsupportedOperationException e) {
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			internalError(e);
		}
		return false;
	}

	/**
	 * @see IDropToFrame
	 */
	public void dropToFrame() throws DebugException {
		if (supportsDropToFrame()) {
			((JDIThread) getThread()).dropToFrame(this);
		} else {
			notSupported(JDIDebugModelMessages.getString("JDIStackFrame.Drop_to_frame_not_supported")); //$NON-NLS-1$
		}
	}

	/**
	 * @see IVariableLookup
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
	 * Helper method that retrieves visible varialbes in this stack frame
	 * handling any exceptions. Returns an empty list if there are no
	 * variables.
	 */
	List getUnderlyingVisibleVariables() throws DebugException {
		List variables= Collections.EMPTY_LIST;
		try {
			variables= fStackFrame.visibleVariables();
		} catch (AbsentInformationException e) {
		} catch (NativeMethodException e) {
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_visible_variables_2"),new String[] {e.toString()}), e); //$NON-NLS-1$
		}

		return variables;
	}

	/**
	 * Helper method that retrievs 'this' from the stack frame
	 */
	ObjectReference getUnderlyingThisObject() throws DebugException {
		try {
			return fStackFrame.thisObject();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_this"),new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return null;
	}
	/**
	 * Returns the underlying JDI StackFrame
	 */
	StackFrame getUnderlyingStackFrame() {
		return fStackFrame;
	}
	
	/**
	 * Sets the underlying JDI StackFrame. Called by a thread
	 * when incrementally updating after a step has completed.
	 */
	void setUnderlyingStackFrame(StackFrame frame) {
		fStackFrame = frame;
	}

	
	/**
	 * Returns the Java stack frame adapter for this stack frame
	 * 
	 * @see IAdaptable
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaStackFrame.class || adapter == IJavaModifiers.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Evaluates the snippet in the context of this stack frame
	 *
	 * @see IJavaStackFrame
	 */
	public void evaluate(String snippet, IJavaEvaluationListener listener, IJavaProject project) throws DebugException {
		IEvaluationContext underlyingContext = ((JDIDebugTarget)getDebugTarget()).getEvaluationContext(project);
		evaluate(snippet, listener, underlyingContext);
	}

	/**
	 * @see IJavaStackFrame
	 */
	public void evaluate(String snippet, IJavaEvaluationListener listener, IEvaluationContext evaluationContext) throws DebugException {
		((JDIThread)getThread()).verifyEvaluation(evaluationContext);
		StackFrameEvaluationContext context = new StackFrameEvaluationContext(this, evaluationContext);
		context.evaluate(snippet, listener);
	}	
	
	/**
	 * @see IJavaEvaluate
	 */
	public boolean canPerformEvaluation() {
		return ((IJavaThread)getThread()).canPerformEvaluation();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public String getSignature() throws DebugException {
		try {
			return getUnderlyingMethod().signature();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_method_signature"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public String getDeclaringTypeName() throws DebugException {
		try {
			return getUnderlyingMethod().declaringType().name();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_declaring_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public String getReceivingTypeName() throws DebugException {
		try {
			ObjectReference thisObject = fStackFrame.thisObject();
			if (thisObject == null) {
				return getDeclaringTypeName();
			} else {
				return thisObject.referenceType().name();
			}
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_receiving_type"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public String getMethodName() throws DebugException {
		try {
			return getUnderlyingMethod().name();	
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_method_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return getUnknownMessage();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isAbstract() throws DebugException {
		return getUnderlyingMethod().isAbstract();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isNative() throws DebugException {
		return getUnderlyingMethod().isNative();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isStaticInitializer() throws DebugException {
		return getUnderlyingMethod().isStaticInitializer();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isFinal() throws DebugException {
		return getUnderlyingMethod().isFinal();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isSynchronized() throws DebugException {
		return getUnderlyingMethod().isSynchronized();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isSynthetic() throws DebugException {
		return getUnderlyingMethod().isSynthetic();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isPublic() throws DebugException {
		return getUnderlyingMethod().isPublic();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isPrivate() throws DebugException {
		return getUnderlyingMethod().isPrivate();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isProtected() throws DebugException {
		return getUnderlyingMethod().isProtected();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isPackagePrivate() throws DebugException {
		return getUnderlyingMethod().isPackagePrivate();
	}
	
	/**
	 * @see IJavaStackFrame
	 */
	public boolean isStatic() throws DebugException {
		return getUnderlyingMethod().isStatic();
	}
		
	/**
	 * @see IJavaStackFrame
	 */
	public String getSourceName() throws DebugException {
		try {
			Location l = getUnderlyingMethod().location();
			if (l != null) {
				return l.sourceName();
			}
		} catch (AbsentInformationException e) {
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIStackFrame.exception_retrieving_source_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return null;
	}
	
	protected boolean isTopStackFrame() throws DebugException {
		IStackFrame tos = getThread().getTopStackFrame();
		return tos != null && tos.equals(this);
	}
	
	protected boolean exists() throws DebugException {
		return ((JDIThread)getThread()).getStackFrames0().indexOf(this) != -1;
	}
	
	/**
	 * @see ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return getThread().canTerminate() || getDebugTarget().canTerminate();
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
}