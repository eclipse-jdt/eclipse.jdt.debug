package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import com.sun.jdi.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.*;
import java.util.*;

/**
 * Proxy to a stack frame on the target.
 */

public class JDIStackFrame extends JDIDebugElement implements IJavaStackFrame {

	// Resource String keys
	private static final String PREFIX= "jdi_stack_frame.";
	private static final String ERROR= PREFIX + "error.";
	private static final String ERROR_GET_NAME= ERROR + "get_name";
	private static final String ERROR_DROP_NOT_SUPPORTED= ERROR + "drop_not_supported";
	private static final String ERROR_GET_ARGUMENTS= ERROR + "get_arguments";
	private static final String ERROR_GET_DECLARING_TYPE= ERROR + "get_declaring_type";
	private static final String ERROR_GET_RECEIVING_TYPE= ERROR + "get_receiving_type";
	private static final String ERROR_GET_LINE_NUMBER= ERROR + "get_line_number";
	private static final String ERROR_GET_SIGNATURE= ERROR + "get_signature";
	private static final String ERROR_GET_METHOD= ERROR + "get_method";
	private static final String ERROR_GET_SOURCE_NAME= ERROR + "get_source_name";

	/**
	 * Underlying stack frame
	 */
	protected StackFrame fStackFrame;
	
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
		super(thread);
		fStackFrame= stackFrame;
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
	public IStackFrame getStackFrame() {
		return this;
	}
	
	/**
	 * @see IDebugElement
	 */
	public IThread getThread() {
		return (IThread)fParent;
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
			return isTopStackFrame() && getThread().canStepInto();
		} catch (DebugException e) {
			return false;
		}
	}

	/**
	 * @see IStep.
	 */
	public boolean canStepOver() {
		return getThread().canStepOver();
	}

	/**
	 * @see IStep.
	 */
	public boolean canStepReturn() {
		try {
			List frames = ((JDIThread)getThread()).getChildren0();
			if (frames != null && !frames.isEmpty()) {
				Object bottomFrame = frames.get(frames.size() - 1);
				return !this.equals(bottomFrame) && getThread().canStepReturn();
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
				targetRequestFailed(ERROR_GET_METHOD, e);
			}
		}
		return fMethod;
	}

	/**
	 * @see IDebugElement
	 */
	protected List getChildren0() throws DebugException {
		if (fChildren == null) {
			Method method= getUnderlyingMethod();
			fChildren= new ArrayList();
			// #isStatic() does not claim to throw any exceptions - so it is not try/catch coded
			if (method.isStatic()) {
				// add statics
				List allFields= null;
				try {
					allFields= method.declaringType().allFields();
				} catch (VMDisconnectedException e) {
					return Collections.EMPTY_LIST;
				} catch (RuntimeException e) {
					targetRequestFailed(ERROR_GET_CHILDREN, e);
				}
				if (allFields != null) {
					Iterator fields= allFields.iterator();
					while (fields.hasNext()) {
						Field field= (Field) fields.next();
						if (field.isStatic()) {
							fChildren.add(new JDIFieldVariable(this, field));
						}
					}
					Collections.sort(fChildren, new Comparator() {
						public int compare(Object a, Object b) {
							return sortStaticChildren(a, b);
						}
					});
				}
			} else {
				// add "this"
				ObjectReference t= null;
				try {
					t= fStackFrame.thisObject();
				} catch (VMDisconnectedException e) {
					return Collections.EMPTY_LIST;
				} catch (RuntimeException e) {
					targetRequestFailed(ERROR_GET_CHILDREN, e);
				}
				if (t != null) {
					fChildren.add(new JDIThisVariable(this, t));
				}
			}
			// add locals
			Iterator variables= getUnderlyingVisibleVariables().iterator();
			while (variables.hasNext()) {
				LocalVariable var= (LocalVariable) variables.next();
				fChildren.add(new JDILocalVariable(this, var));
			}
		} else if (fRefreshVariables) {
			updateVariables();
		}
		fRefreshVariables = false;
		return fChildren;
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
			internalError(de);
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
			targetRequestFailed(ERROR_GET_ARGUMENTS, e);
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
				targetRequestFailed(ERROR_GET_LINE_NUMBER, e);
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
		getThread().stepInto();
	}

	/**
	 * @see IStep
	 */
	public void stepOver() throws DebugException {
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
		if (isTopStackFrame()) {
			getThread().stepReturn();
		} else {
			List frames = ((JDIThread)getThread()).getChildren0();
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
		if (fChildren == null) {
			return;
		}

		Method method= getUnderlyingMethod();
		int index= 0;
		if (method.isStatic()) {
			// update statics
			while (index < fChildren.size() && fChildren.get(index) instanceof JDIFieldVariable) {
				index++;
			}
		} else {
			// update "this"
			ObjectReference thisObject= null;
			try {
				thisObject= fStackFrame.thisObject();
			} catch (VMDisconnectedException e) {
				return;
			} catch (RuntimeException e) {
				targetRequestFailed(ERROR_GET_CHILDREN, e);
			}
			JDIThisVariable oldThisObject= null;
			if (!fChildren.isEmpty() && fChildren.get(0) instanceof JDIThisVariable) {
				oldThisObject= (JDIThisVariable) fChildren.get(0);
			}
			if (thisObject == null && oldThisObject != null) {
				// removal of 'this'
				fChildren.remove(0);
				index= 0;
			} else {
				if (oldThisObject == null && thisObject != null) {
					// creation of 'this'
					oldThisObject= new JDIThisVariable(this, thisObject);
					fChildren.add(0, oldThisObject);
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
			targetRequestFailed(ERROR_GET_CHILDREN, e);
		}
		int localIndex= -1;
		while (index < fChildren.size()) {
			JDILocalVariable local= (JDILocalVariable) fChildren.get(index);
			localIndex= locals.indexOf(local.getLocal());
			if (localIndex >= 0) {
				// update variable with new underling JDI LocalVariable
				local.setLocal((LocalVariable) locals.get(localIndex));
				locals.remove(localIndex);
				index++;
			} else {
				// remove variable
				fChildren.remove(index);
			}
		}

		// add any new locals
		Iterator newOnes= locals.iterator();
		while (newOnes.hasNext()) {
			JDILocalVariable local= new JDILocalVariable(this, (LocalVariable) newOnes.next());
			fChildren.add(local);
		}
	}

	/**
	 * @see IDropToFrame
	 */
	public boolean supportsDropToFrame() {
		try {
			JDIThread thread= (JDIThread) getThread();
			return !thread.isTerminated()
				&& thread.isSuspended()
				&& thread.getUnderlyingThread() instanceof org.eclipse.jdi.hcr.ThreadReference
				&& ((org.eclipse.jdi.hcr.VirtualMachine) ((JDIDebugTarget) getDebugTarget()).getVM()).canDoReturn(); 
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
			notSupported(ERROR_DROP_NOT_SUPPORTED);
		}
	}

	/**
	 * @see IVariableLookup
	 */
	public IVariable findVariable(String varName) throws DebugException {
		List list= getChildren0();
		Iterator variables = list.iterator();
		JDIThisVariable thisVariable= null;
		while (variables.hasNext()) {
			IVariable var= (IVariable) variables.next();
			if (var.getName().equals(varName)) {
				return var;
			}
			if (var instanceof JDIThisVariable) {
				// save for later - check for instance and static vars
				thisVariable= (JDIThisVariable)var;
			}
		}

		if (thisVariable != null) {
			variables= ((JDIValue)thisVariable.getValue()).getChildren0().iterator();
			while (variables.hasNext()) {
				IVariable var= (IVariable) variables.next();
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
			targetRequestFailed(ERROR_GET_CHILDREN, e);
		}

		return variables;
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
	protected Object getAdpater(Class adapter) {
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
		if (((JDIThread)getThread()).fInEvaluation) {
			requestFailed(JDIThread.IN_EVALUATION, null);
		}
		if (!evaluationContext.getProject().hasBuildState()) {
			requestFailed(JDIThread.NO_BUILT_STATE, null);
		}
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
			targetRequestFailed(ERROR_GET_SIGNATURE, e);
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
			targetRequestFailed(ERROR_GET_DECLARING_TYPE, e);
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
			targetRequestFailed(ERROR_GET_RECEIVING_TYPE, e);
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
			targetRequestFailed(ERROR_GET_NAME, e);
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
			targetRequestFailed(ERROR_GET_SOURCE_NAME, e);
		}
		return null;
	}
	
	protected boolean isTopStackFrame() throws DebugException {
		IStackFrame tos = getThread().getTopStackFrame();
		return tos != null && tos.equals(this);
	}
}


