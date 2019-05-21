/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Samrat Dhillon samrat.dhillon@gmail.com - Bug 384458 - debug shows value of variable in another scope
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdi.internal.ReferenceTypeImpl;
import org.eclipse.jdi.internal.ValueImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDILambdaVariable;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIReturnValueVariable;
import org.eclipse.jdt.internal.debug.core.model.MethodResult.ResultType;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
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
	 * This frame's depth in the call stack (0 == bottom of stack). A new frame
	 * is indicated by -2. An invalid frame is indicated by -1.
	 */
	private int fDepth = -2;

	/**
	 * Underlying JDI stack frame.
	 */
	private StackFrame fStackFrame;

	/**
	 * Containing thread.
	 */
	private JDIThread fThread;
	/**
	 * Visible variables.
	 */
	private List<IJavaVariable> fVariables;

	/**
	 * The underlying Object associated with this stack frame. Cached lazily on
	 * first access.
	 */
	private ObjectReference fThisObject;

	/**
	 * The name of the type of the object that received the method call
	 * associated with this stack frame. Cached lazily on first access.
	 */
	private String fReceivingTypeName;
	/**
	 * Whether the variables need refreshing
	 */
	private boolean fRefreshVariables = true;
	/**
	 * Whether this stack frame has been marked as out of synch. If set to
	 * <code>true</code> this stack frame will stop dynamically calculating its
	 * out of synch state.
	 */
	private boolean fIsOutOfSynch = false;

	/**
	 * Whether local variable information was available
	 */
	private boolean fLocalsAvailable = true;

	/**
	 * Location of this stack frame
	 */
	private Location fLocation;

	/**
	 * Whether the current stack frame is the top of the stack
	 */
	private boolean fIsTop;

	@SuppressWarnings("restriction")
	private static final String SYNTHETIC_OUTER_LOCAL_PREFIX = new String(org.eclipse.jdt.internal.compiler.lookup.TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX);

	/**
	 * Creates a new stack frame in the given thread.
	 *
	 * @param thread
	 *            The parent JDI thread
	 * @param frame
	 *            underlying frame
	 * @param depth
	 *            depth on the stack (0 is bottom)
	 */
	public JDIStackFrame(JDIThread thread, StackFrame frame, int depth) {
		super((JDIDebugTarget) thread.getDebugTarget());
		setThread(thread);
		bind(frame, depth);
	}

	/**
	 * Binds this frame to the given underlying frame on the target VM or
	 * returns a new frame representing the given frame. A frame can only be
	 * re-bound to an underlying frame if it refers to the same depth on the
	 * stack in the same method.
	 *
	 * @param frame
	 *            underlying frame, or <code>null</code>
	 * @param depth
	 *            depth in the call stack, or -1 to indicate the frame should
	 *            become invalid
	 * @param return a frame to refer to the given frame or <code>null</code>
	 */
	protected JDIStackFrame bind(StackFrame frame, int depth) {
		synchronized (fThread) {
			if (fDepth == -2) {
				// first initialization
				fStackFrame = frame;
				fDepth = depth;
				fLocation = frame.location();
				return this;
			} else if (depth == -1) {
				// mark as invalid
				fDepth = -1;
				fStackFrame = null;
				fIsTop = false;
				return null;
			} else if (fDepth == depth) {
				Location location = frame.location();
				Method method = location.method();
				if (method.equals(fLocation.method())) {
					try {
						if (method.declaringType().defaultStratum()
								.equals("Java") || //$NON-NLS-1$
								equals(getSourceName(location),
										getSourceName(fLocation))) {
							// TODO: what about receiving type being the same?
							fStackFrame = frame;
							fLocation = location;
							clearCachedData();
							return this;
						}
					} catch (DebugException e) {
					}
				}
			}
			// invalidate this frame
			bind(null, -1);
			// return a new frame
			return new JDIStackFrame(fThread, frame, depth);
		}

	}

	/**
	 * @see IStackFrame#getThread()
	 */
	@Override
	public IThread getThread() {
		return fThread;
	}

	/**
	 * @see ISuspendResume#canResume()
	 */
	@Override
	public boolean canResume() {
		return getThread().canResume();
	}

	/**
	 * @see ISuspendResume#canSuspend()
	 */
	@Override
	public boolean canSuspend() {
		return getThread().canSuspend();
	}

	/**
	 * @see IStep#canStepInto()
	 */
	@Override
	public boolean canStepInto() {
		try {
			return exists() && isTopStackFrame() && !isObsolete()
					&& getThread().canStepInto();
		} catch (DebugException e) {
			logError(e);
			return false;
		}
	}

	/**
	 * @see IStep#canStepOver()
	 */
	@Override
	public boolean canStepOver() {
		return exists() && !isObsolete() && getThread().canStepOver();
	}

	/**
	 * @see IStep#canStepReturn()
	 */
	@Override
	public boolean canStepReturn() {
		try {
			if (!exists() || isObsolete() || !getThread().canStepReturn()) {
				return false;
			}
			List<IJavaStackFrame> frames = ((JDIThread) getThread())
					.computeStackFrames();
			if (frames != null && !frames.isEmpty()) {
				boolean bottomFrame = this
						.equals(frames.get(frames.size() - 1));
				boolean aboveObsoleteFrame = false;
				if (!bottomFrame) {
					int index = frames.indexOf(this);
					if (index < frames.size() - 1
							&& ((JDIStackFrame) frames.get(index + 1))
									.isObsolete()) {
						aboveObsoleteFrame = true;
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
	 * retrieving the method is necessary.
	 */
	public Method getUnderlyingMethod() {
		synchronized (fThread) {
			return fLocation.method();
		}
	}

	/**
	 * @see IStackFrame#getVariables()
	 */
	@Override
	public IVariable[] getVariables() throws DebugException {
		List<IJavaVariable> list = getVariables0();
		return list.toArray(new IVariable[list.size()]);
	}

	protected List<IJavaVariable> getVariables0() throws DebugException {
		synchronized (fThread) {
			if (fVariables == null) {

				// throw exception if native method, so variable view will
				// update
				// with information message
				if (isNative()) {
					requestFailed(
							JDIDebugModelMessages.JDIStackFrame_Variable_information_unavailable_for_native_methods,
							null);
				}

				Method method = getUnderlyingMethod();
				fVariables = new ArrayList<>();
				// #isStatic() does not claim to throw any exceptions - so it is
				// not try/catch coded
				if (method.isStatic()) {
					// add statics
					List<Field> allFields = null;
					ReferenceType declaringType = method.declaringType();
					try {
						allFields = declaringType.allFields();
					} catch (RuntimeException e) {
						targetRequestFailed(
								MessageFormat.format(
										JDIDebugModelMessages.JDIStackFrame_exception_retrieving_fields,
										e.toString()), e);
						// execution will not reach this line, as
						// #targetRequestFailed will throw an exception
						return Collections.EMPTY_LIST;
					}
					if (allFields != null) {
						Iterator<Field> fields = allFields.iterator();
						while (fields.hasNext()) {
							Field field = fields.next();
							if (field.isStatic()) {
								fVariables.add(new JDIFieldVariable(
										(JDIDebugTarget) getDebugTarget(),
										field, declaringType));
							}
						}
						Collections.sort(fVariables,
								new Comparator<IJavaVariable>() {
									@Override
									public int compare(IJavaVariable a, IJavaVariable b) {
										JDIFieldVariable v1 = (JDIFieldVariable) a;
										JDIFieldVariable v2 = (JDIFieldVariable) b;
										try {
											return v1.getName()
													.compareToIgnoreCase(
															v2.getName());
										} catch (DebugException de) {
											logError(de);
											return -1;
										}
									}
								});
					}
				} else {
					// add "this"
					ObjectReference t = getUnderlyingThisObject();
					if (t != null) {
						fVariables.add(new JDIThisVariable(
								(JDIDebugTarget) getDebugTarget(), t));
					}
				}
				if (LambdaUtils.isLambdaFrame(this)) {
					List<IJavaStackFrame> frames = fThread.computeStackFrames();
					int previousIndex = frames.indexOf(this) + 1;
					if (previousIndex > 0 && previousIndex < frames.size()) {
						IJavaStackFrame previousFrame = frames.get(previousIndex);
						ObjectReference underlyingThisObject = ((JDIStackFrame) previousFrame).getUnderlyingThisObject();
						IJavaValue closureValue = JDIValue.createValue((JDIDebugTarget) getDebugTarget(), underlyingThisObject);
						setLambdaVariableNames(closureValue, underlyingThisObject);
						fVariables.add(new JDILambdaVariable(closureValue));
					}
				}
				addStepReturnValue(fVariables);
				// add locals
				Iterator<LocalVariable> variables = getUnderlyingVisibleVariables()
						.iterator();
				while (variables.hasNext()) {
					LocalVariable var = variables.next();
					fVariables.add(new JDILocalVariable(this, var));
				}
			} else if (fRefreshVariables) {
				updateVariables();
			}
			fRefreshVariables = false;
			return fVariables;
		}
	}

	private void setLambdaVariableNames(IJavaValue value, ObjectReference underlyingThisObject) {
		try {
			IType type = JavaDebugUtils.resolveType(value.getJavaType());
			if (type == null) {
				return;
			}
			ASTParser parser = ASTParser.newParser(AST.JLS11);
			parser.setResolveBindings(true);
			parser.setSource(type.getTypeRoot());
			CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			cu.accept(new LambdaASTVisitor(false, underlyingThisObject, getUnderlyingMethod()));
		} catch (CoreException e) {
			logError(e);
		}
	}

	private final static class LambdaASTVisitor extends ASTVisitor {
		private final ObjectReference underlyingThisObject;
		private Method underlyingMethod;

		private LambdaASTVisitor(boolean visitDocTags, ObjectReference underlyingThisObject, Method underlyingMethod) {
			super(visitDocTags);
			this.underlyingThisObject = underlyingThisObject;
			this.underlyingMethod = underlyingMethod;
		}

		@Override
		public boolean visit(LambdaExpression lambdaExpression) {
			IMethodBinding binding = lambdaExpression.resolveMethodBinding();
			if (binding == null) {
				return true;
			}
			IVariableBinding[] synVars = binding.getSyntheticOuterLocals();
			if (synVars == null || synVars.length == 0) {// name cannot be updated if Synthetic Outer Locals are not available
				return true;
			}
			List<Field> allFields = underlyingThisObject.referenceType().fields();
			ListIterator<Field> listIterator = allFields.listIterator();
			int i = 0;
			if (underlyingMethod.isStatic()) {
				if (synVars.length == allFields.size()) {
					while (listIterator.hasNext()) {
						FieldImpl field = (FieldImpl) listIterator.next();
						String newName = synVars[i].getName();
						FieldImpl newField = createRenamedCopy(field, newName);
						listIterator.set(newField);
						i++;
					}
				}
			} else {
				if (synVars.length + 1 == allFields.size()) {
					while (listIterator.hasNext()) {
						FieldImpl field = (FieldImpl) listIterator.next();
						String newName = field.name();
						if (i == 0) {
							newName = "this"; //$NON-NLS-1$
						} else {
							newName = synVars[i - 1].getName();
						}
						FieldImpl newField = createRenamedCopy(field, newName);
						listIterator.set(newField);
						i++;
					}
				}
			}
			return true;
		}

		private FieldImpl createRenamedCopy(FieldImpl field, String newName) {
			return new FieldImpl((VirtualMachineImpl) field.virtualMachine(), (ReferenceTypeImpl) field.declaringType(), field.getFieldID(), newName, field.signature(), field.genericSignature(), field.modifiers());
		}
	}

	/**
	 * If there is a return value from a "step return" that belongs to this frame, insert it as first element
	 *
	 * @param variables
	 */
	private void addStepReturnValue(List<IJavaVariable> variables) {
		if (fIsTop) {
			MethodResult methodResult = fThread.getMethodResult();
			if (methodResult != null) {
				if (methodResult.fResultType == ResultType.returned) {
					if (fDepth + 1 != methodResult.fTargetFrameCount) {
						// can happen e.g., because of checkPackageAccess/System.getSecurityManager()
						return;
					}
					String name = MessageFormat.format(JDIDebugModelMessages.JDIStackFrame_ReturnValue, methodResult.fMethod.name());
					variables.add(0, new JDIReturnValueVariable(name, JDIValue.createValue(getJavaDebugTarget(), methodResult.fValue), true));
				} else if (methodResult.fResultType == ResultType.returning) {
					String name = MessageFormat.format(JDIDebugModelMessages.JDIStackFrame_ReturningValue, methodResult.fMethod.name());
					variables.add(0, new JDIReturnValueVariable(name, JDIValue.createValue(getJavaDebugTarget(), methodResult.fValue), true));
				} else if (methodResult.fResultType == ResultType.threw) {
					if (fDepth + 1 > methodResult.fTargetFrameCount) {
						// don't know if this really can happen, but other jvm suprises were not expected either
						return;
					}
					String name = MessageFormat.format(JDIDebugModelMessages.JDIStackFrame_ExceptionThrown, methodResult.fMethod.name());
					variables.add(0, new JDIReturnValueVariable(name, JDIValue.createValue(getJavaDebugTarget(), methodResult.fValue), true));
				} else if (methodResult.fResultType == ResultType.throwing) {
					String name = MessageFormat.format(JDIDebugModelMessages.JDIStackFrame_ThrowingException, methodResult.fMethod.name());
					variables.add(0, new JDIReturnValueVariable(name, JDIValue.createValue(getJavaDebugTarget(), methodResult.fValue), true));
				} else if (methodResult.fResultType == ResultType.step_timeout) {
					String msg = JDIDebugModelMessages.JDIStackFrame_NotObservedBecauseOfTimeout;
					variables.add(0, new JDIReturnValueVariable(JDIDebugModelMessages.JDIStackFrame_NoMethodReturnValue, new JDIPlaceholderValue(getJavaDebugTarget(), msg), false));
				}
			} else if(JDIThread.showStepResultIsEnabled()) {
				variables.add(0, new JDIReturnValueVariable(JDIDebugModelMessages.JDIStackFrame_NoMethodReturnValue, new JDIPlaceholderValue(getJavaDebugTarget(), ""), false)); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @see IStackFrame#getName()
	 */
	@Override
	public String getName() throws DebugException {
		return getMethodName();
	}

	/**
	 * @see IJavaStackFrame#getArgumentTypeNames()
	 */
	@Override
	public List<String> getArgumentTypeNames() throws DebugException {
		try {
			Method underlyingMethod = getUnderlyingMethod();
			String genericSignature = underlyingMethod.genericSignature();
			if (genericSignature == null) {
				// no generic signature
				return underlyingMethod.argumentTypeNames();
			}
			// generic signature
			String[] parameterTypes = Signature
					.getParameterTypes(genericSignature);
			List<String> argumentTypeNames = new ArrayList<>();
			for (String parameterType : parameterTypes) {
				argumentTypeNames.add(Signature.toString(parameterType)
						.replace('/', '.'));
			}
			return argumentTypeNames;
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIStackFrame_exception_retrieving_argument_type_names,
							e.toString()), e);
			// execution will never reach this line, as
			// #targetRequestFailed will throw an exception
			return null;
		}
	}

	/**
	 * @see IStackFrame#getLineNumber()
	 */
	@Override
	public int getLineNumber() throws DebugException {
		synchronized (fThread) {
			try {
				return fLocation.lineNumber();
			} catch (RuntimeException e) {
				if (getThread().isSuspended()) {
					targetRequestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIStackFrame_exception_retrieving_line_number,
									e.toString()), e);
				}
			}
		}
		return -1;
	}

	/**
	 * @see IStep#isStepping()
	 */
	@Override
	public boolean isStepping() {
		return getThread().isStepping();
	}

	/**
	 * @see ISuspendResume#isSuspended()
	 */
	@Override
	public boolean isSuspended() {
		return getThread().isSuspended();
	}

	/**
	 * @see ISuspendResume#resume()
	 */
	@Override
	public void resume() throws DebugException {
		getThread().resume();
	}

	/**
	 * @see IStep#stepInto()
	 */
	@Override
	public void stepInto() throws DebugException {
		if (!canStepInto()) {
			return;
		}
		getThread().stepInto();
	}

	/**
	 * @see IStep#stepOver()
	 */
	@Override
	public void stepOver() throws DebugException {
		if (!canStepOver()) {
			return;
		}
		if (isTopStackFrame()) {
			getThread().stepOver();
		} else {
			((JDIThread) getThread()).stepToFrame(this);
		}
	}

	/**
	 * @see IStep#stepReturn()
	 */
	@Override
	public void stepReturn() throws DebugException {
		if (!canStepReturn()) {
			return;
		}
		if (isTopStackFrame()) {
			getThread().stepReturn();
		} else {
			List<IJavaStackFrame> frames = ((JDIThread) getThread())
					.computeStackFrames();
			int index = frames.indexOf(this);
			if (index >= 0 && index < frames.size() - 1) {
				IStackFrame nextFrame = frames.get(index + 1);
				((JDIThread) getThread()).stepToFrame(nextFrame);
			}
		}
	}

	/**
	 * @see ISuspendResume#suspend()
	 */
	@Override
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

		// remove old return value first, so the "this" updating logic below works
		if (!fVariables.isEmpty() && fVariables.get(0) instanceof JDIReturnValueVariable) {
			fVariables.remove(0);
		}

		Method method = getUnderlyingMethod();
		int index = 0;
		if (!method.isStatic()) {
			// update "this"
			ObjectReference thisObject;
			thisObject = getUnderlyingThisObject();
			JDIThisVariable oldThisObject = null;
			if (!fVariables.isEmpty()
					&& fVariables.get(0) instanceof JDIThisVariable) {
				oldThisObject = (JDIThisVariable) fVariables.get(0);
			}
			if (thisObject == null && oldThisObject != null) {
				// removal of 'this'
				fVariables.remove(0);
				index = 0;
			} else {
				if (oldThisObject == null && thisObject != null) {
					// creation of 'this'
					oldThisObject = new JDIThisVariable(
							(JDIDebugTarget) getDebugTarget(), thisObject);
					fVariables.add(0, oldThisObject);
					index = 1;
				} else {
					if (oldThisObject != null) {
						// 'this' still exists, replace with new 'this' if a
						// different receiver
						if (!oldThisObject.retrieveValue().equals(thisObject)) {
							fVariables.remove(0);
							fVariables.add(0, new JDIThisVariable(
									(JDIDebugTarget) getDebugTarget(),
									thisObject));
						}
						index = 1;
					}
				}
			}
		}

		List<LocalVariable> locals = null;
		try {
			locals = getUnderlyingStackFrame().visibleVariables();
		} catch (AbsentInformationException e) {
			locals = Collections.EMPTY_LIST;
		} catch (NativeMethodException e) {
			locals = Collections.EMPTY_LIST;
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIStackFrame_exception_retrieving_visible_variables,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will throw an exception
			return;
		}
		int localIndex = -1;
		while (index < fVariables.size()) {
			Object var = fVariables.get(index);
			if (var instanceof JDILocalVariable) {
				JDILocalVariable local = (JDILocalVariable) fVariables
						.get(index);
				localIndex = locals.indexOf(local.getLocal());
				if (localIndex >= 0) {
					// update variable with new underling JDI LocalVariable
					local.setLocal(locals.get(localIndex));
					locals.remove(localIndex);
					index++;
				} else {
					// remove variable
					fVariables.remove(index);
				}
			} else {
				// field variable of a static frame
				index++;
			}
		}

		// add any new locals
		Iterator<LocalVariable> newOnes = locals.iterator();
		while (newOnes.hasNext()) {
			JDILocalVariable local = new JDILocalVariable(this, newOnes.next());
			fVariables.add(local);
		}

		addStepReturnValue(fVariables);
	}

	/**
	 * @see org.eclipse.debug.core.model.IDropToFrame#canDropToFrame()
	 */
	@Override
	public boolean canDropToFrame() {
		return supportsDropToFrame();
	}

	/**
	 * @see IJavaStackFrame#supportsDropToFrame()
	 */
	@Override
	public boolean supportsDropToFrame() {
		JDIThread thread = (JDIThread) getThread();
		JDIDebugTarget target = (JDIDebugTarget) thread.getDebugTarget();
		try {
			if (!target.isAvailable() || !thread.isSuspended()
					|| thread.isTerminated() || thread.isInvokingMethod()) {
				return false;
			}
			boolean j9Support = false;
			boolean jdkSupport = target.canPopFrames();
			VirtualMachine vm = getVM();
			if (vm == null) {
				return false;
			}
			try {
				j9Support = (thread.getUnderlyingThread() instanceof org.eclipse.jdi.hcr.ThreadReference)
						&& ((org.eclipse.jdi.hcr.VirtualMachine) vm)
								.canDoReturn();
			} catch (UnsupportedOperationException uoe) {
				j9Support = false;
			}

			if (jdkSupport || j9Support) {
				// Also ensure that this frame and no frames above this
				// frame are native. Unable to pop native stack frames.
				List<IJavaStackFrame> frames = thread.computeStackFrames();
				if (jdkSupport) {
					// JDK 1.4 VMs are currently unable to pop the bottom
					// stack frame.
					if ((frames.size() > 0)
							&& frames.get(frames.size() - 1) == this) {
						return false;
					}
				}
				int index = 0;
				JDIStackFrame frame = null;
				while (index < frames.size()) {
					frame = (JDIStackFrame) frames.get(index);
					index++;
					if (frame.isNative()) {
						return false;
					}
					if (frame.equals(this)) {
						if (jdkSupport) {
							// JDK 1.4 VMs are currently unable to pop the
							// frame directly above a native frame
							if (index < frames.size()
									&& ((JDIStackFrame) frames.get(index))
											.isNative()) {
								return false;
							}
						}
						return true;
					}
				}
			}
			return false;
		} catch (DebugException e) {
			if (e.getStatus().getException() instanceof IncompatibleThreadStateException
					|| e.getStatus().getCode() == IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
				// if the thread has since resumed, drop is not supported
				return false;
			}
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
	@Override
	public void dropToFrame() throws DebugException {
		if (supportsDropToFrame()) {
			((JDIThread) getThread()).dropToFrame(this);
		} else {
			notSupported(JDIDebugModelMessages.JDIStackFrame_Drop_to_frame_not_supported);
		}
	}

	public void popFrame() throws DebugException {
		if (supportsDropToFrame()) {
			((JDIThread) getThread()).popFrame(this);
		} else {
			notSupported(JDIDebugModelMessages.JDIStackFrame_pop_frame_not_supported);
		}
	}

	/**
	 * @see IJavaStackFrame#findVariable(String)
	 */
	@Override
	public IJavaVariable findVariable(String varName) throws DebugException {
		if (isNative()) {
			return null;
		}
		IVariable[] variables = getVariables();
		List<IJavaVariable> possibleMatches = new ArrayList<>();
		IJavaVariable thisVariable = null;
		for (IVariable variable : variables) {
			IJavaVariable var = (IJavaVariable) variable;
			if (var.getName().equals(varName)) {
				possibleMatches.add(var);
			}
			if (var instanceof JDIThisVariable) {
				// save for later - check for instance and static variables
				thisVariable = var;
			}
			if (var instanceof JDILambdaVariable) {
				// Check if we have match in synthetic fields generated
				// by compiler for the captured variables (they start with "val$")
				JDILambdaVariable lambda = (JDILambdaVariable) var;
				JDIObjectValue ov = (JDIObjectValue) lambda.getValue();
				IVariable[] lvars = ov.getVariables();
				for (IVariable lv : lvars) {
					String name = lv.getName();
					if (name.startsWith(SYNTHETIC_OUTER_LOCAL_PREFIX) && (SYNTHETIC_OUTER_LOCAL_PREFIX + varName).equals(name)) {
						possibleMatches.add((IJavaVariable) lv);
					}
				}
			}
		}
		for(IJavaVariable variable: possibleMatches){
			// Local Variable has more preference than Field Variable
			if(variable instanceof JDILocalVariable){
				return variable;
			}
		}
		if(possibleMatches.size() > 0) {
			return possibleMatches.get(0);
		}

		if (thisVariable != null) {
			IVariable[] thisChildren = thisVariable.getValue().getVariables();
			for (IVariable element : thisChildren) {
				IJavaVariable var = (IJavaVariable) element;
				if (var.getName().equals(varName)) {
					return var;
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves visible variables in this stack frame handling any exceptions.
	 * Returns an empty list if there are no variables.
	 *
	 * @see JDIDebugElement#targetRequestFailed(String, RuntimeException)
	 */
	protected List<LocalVariable> getUnderlyingVisibleVariables() throws DebugException {
		synchronized (fThread) {
			List<LocalVariable> variables = Collections.EMPTY_LIST;
			try {
				variables = getUnderlyingStackFrame().visibleVariables();
			} catch (AbsentInformationException e) {
				setLocalsAvailable(false);
			} catch (NativeMethodException e) {
				setLocalsAvailable(false);
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIStackFrame_exception_retrieving_visible_variables_2,
								e.toString()), e);
			}
			return variables;
		}
	}

	/**
	 * Retrieves 'this' from the underlying stack frame. Returns
	 * <code>null</code> for static stack frames.
	 *
	 * @see JDIDebugElement#targetRequestFailed(String, RuntimeException)
	 */
	protected ObjectReference getUnderlyingThisObject() throws DebugException {
		synchronized (fThread) {
			if ((fStackFrame == null || fThisObject == null) && !isStatic()) {
				try {
					fThisObject = getUnderlyingStackFrame().thisObject();
				} catch (RuntimeException e) {
					targetRequestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIStackFrame_exception_retrieving_this,
									e.toString()), e);
					// execution will not reach this line, as
					// #targetRequestFailed will throw an exception
					return null;
				}
			}
			return fThisObject;
		}
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IJavaStackFrame.class || adapter == IJavaModifiers.class) {
			return (T) this;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * @see IJavaStackFrame#getSignature()
	 */
	@Override
	public String getSignature() throws DebugException {
		try {
			return getUnderlyingMethod().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIStackFrame_exception_retrieving_method_signature,
							e.toString()), e);
			// execution will not reach this line, as
			// #targetRequestFailed will throw an exception
			return null;
		}
	}

	/**
	 * @see IJavaStackFrame#getDeclaringTypeName()
	 */
	@Override
	public String getDeclaringTypeName() throws DebugException {
		synchronized (fThread) {
			try {
				if (isObsolete()) {
					return JDIDebugModelMessages.JDIStackFrame__unknown_declaring_type__1;
				}
				return JDIReferenceType.getGenericName(getUnderlyingMethod()
						.declaringType());
			} catch (RuntimeException e) {
				if (getThread().isSuspended()) {
					targetRequestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIStackFrame_exception_retrieving_declaring_type,
									e.toString()), e);
				}
				return JDIDebugModelMessages.JDIStackFrame__unknown_declaring_type__1;
			}
		}
	}

	/**
	 * @see IJavaStackFrame#getReceivingTypeName()
	 */
	@Override
	public String getReceivingTypeName() throws DebugException {
		if (fStackFrame == null || fReceivingTypeName == null) {
			try {
				if (isObsolete()) {
					fReceivingTypeName = JDIDebugModelMessages.JDIStackFrame__unknown_receiving_type__2;
				} else {
					ObjectReference thisObject = getUnderlyingThisObject();
					if (thisObject == null) {
						fReceivingTypeName = getDeclaringTypeName();
					} else {
						fReceivingTypeName = JDIReferenceType
								.getGenericName(thisObject.referenceType());
					}
				}
			} catch (RuntimeException e) {
				if (getThread().isSuspended()) {
					targetRequestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIStackFrame_exception_retrieving_receiving_type,
									e.toString()), e);
				}
				return JDIDebugModelMessages.JDIStackFrame__unknown_receiving_type__2;
			}
		}
		return fReceivingTypeName;
	}

	/**
	 * @see IJavaStackFrame#getMethodName()
	 */
	@Override
	public String getMethodName() throws DebugException {
		try {
			return getUnderlyingMethod().name();
		} catch (RuntimeException e) {
			if (getThread().isSuspended()) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIStackFrame_exception_retrieving_method_name,
								e.toString()), e);
			}
			return JDIDebugModelMessages.JDIStackFrame__unknown_method__1;
		}
	}

	/**
	 * @see IJavaStackFrame#isNative()
	 */
	@Override
	public boolean isNative() throws DebugException {
		return getUnderlyingMethod().isNative();
	}

	/**
	 * @see IJavaStackFrame#isConstructor()
	 */
	@Override
	public boolean isConstructor() throws DebugException {
		return getUnderlyingMethod().isConstructor();
	}

	/**
	 * @see IJavaStackFrame#isStaticInitializer()
	 */
	@Override
	public boolean isStaticInitializer() throws DebugException {
		return getUnderlyingMethod().isStaticInitializer();
	}

	/**
	 * @see IJavaModifiers#isFinal()
	 */
	@Override
	public boolean isFinal() throws DebugException {
		return getUnderlyingMethod().isFinal();
	}

	/**
	 * @see IJavaStackFrame#isSynchronized()
	 */
	@Override
	public boolean isSynchronized() throws DebugException {
		return getUnderlyingMethod().isSynchronized();
	}

	/**
	 * @see IJavaModifiers#isSynthetic()
	 */
	@Override
	public boolean isSynthetic() throws DebugException {
		return getUnderlyingMethod().isSynthetic();
	}

	/**
	 * @see IJavaModifiers#isPublic()
	 */
	@Override
	public boolean isPublic() throws DebugException {
		return getUnderlyingMethod().isPublic();
	}

	/**
	 * @see IJavaModifiers#isPrivate()
	 */
	@Override
	public boolean isPrivate() throws DebugException {
		return getUnderlyingMethod().isPrivate();
	}

	/**
	 * @see IJavaModifiers#isProtected()
	 */
	@Override
	public boolean isProtected() throws DebugException {
		return getUnderlyingMethod().isProtected();
	}

	/**
	 * @see IJavaModifiers#isPackagePrivate()
	 */
	@Override
	public boolean isPackagePrivate() throws DebugException {
		return getUnderlyingMethod().isPackagePrivate();
	}

	/**
	 * @see IJavaModifiers#isStatic()
	 */
	@Override
	public boolean isStatic() throws DebugException {
		return getUnderlyingMethod().isStatic();
	}

	/**
	 * @see IJavaStackFrame#getSourceName()
	 */
	@Override
	public String getSourceName() throws DebugException {
		synchronized (fThread) {
			return getSourceName(fLocation);
		}
	}

	/**
	 * Returns the source from the default stratum of the given location or
	 * <code>null</code> if not available (missing attribute).
	 */
	private String getSourceName(Location location) throws DebugException {
		try {
			return location.sourceName();
		} catch (AbsentInformationException e) {
			return null;
		} catch (NativeMethodException e) {
			return null;
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIStackFrame_exception_retrieving_source_name,
							e.toString()), e);
		}
		return null;
	}

	private boolean equals(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		return o1.equals(o2);
	}

	protected boolean isTopStackFrame() throws DebugException {
		IStackFrame tos = getThread().getTopStackFrame();
		return tos != null && tos.equals(this);
	}

	/**
	 * Sets this stack frame to be out of synch. Note that passing
	 * <code>true</code> to this method marks this stack frame as out of synch
	 * permanently (statically).
	 */
	public void setOutOfSynch(boolean outOfSynch) {
		fIsOutOfSynch = outOfSynch;
	}

	/**
	 * @see IJavaStackFrame#isOutOfSynch()
	 */
	@Override
	public boolean isOutOfSynch() throws DebugException {
		if (fIsOutOfSynch) {
			return true;
		}
		// if this frame's thread is not suspended, the out-of-synch info cannot
		// change until it suspends again
		if (getThread().isSuspended()) {
			JDIDebugTarget target = (JDIDebugTarget) getDebugTarget();
			if (target.hasHCROccurred()
					&& target.isOutOfSynch(getUnderlyingMethod()
							.declaringType().name())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see IJavaStackFrame#isObsolete()
	 */
	@Override
	public boolean isObsolete() {
		if (!JDIDebugPlugin.isJdiVersionGreaterThanOrEqual(new int[] { 1, 4 })
				|| !((JDIDebugTarget) getDebugTarget()).hasHCROccurred()) {
			// If no hot code replace has occurred, this frame
			// cannot be obsolete.
			return false;
		}
		// if this frame's thread is not suspended, the obsolete status cannot
		// change until it suspends again
		synchronized (fThread) {
			if (getThread().isSuspended()) {
				return getUnderlyingMethod().isObsolete();
			}
			return false;
		}
	}

	protected boolean exists() {
		synchronized (fThread) {
			return fDepth != -1;
		}
	}

	/**
	 * @see ITerminate#canTerminate()
	 */
	@Override
	public boolean canTerminate() {
		return exists() && getThread().canTerminate()
				|| getDebugTarget().canTerminate();
	}

	/**
	 * @see ITerminate#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		return getThread().isTerminated();
	}

	/**
	 * @see ITerminate#terminate()
	 */
	@Override
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
	 * @exception DebugException
	 *                if this stack frame does not currently have an underlying
	 *                frame (is in an interim state where this frame's thread
	 *                has been resumed, and is not yet suspended).
	 */
	protected StackFrame getUnderlyingStackFrame() throws DebugException {
		synchronized (fThread) {
			if (fStackFrame == null) {
				if (fDepth == -1) {
					throw new DebugException(new Status(IStatus.ERROR,
							JDIDebugPlugin.getUniqueIdentifier(),
							IJavaStackFrame.ERR_INVALID_STACK_FRAME,
							JDIDebugModelMessages.JDIStackFrame_25, null));
				}
				if (fThread.isSuspended()) {
					// re-index stack frames - See Bug 47198
					fThread.computeStackFrames();
					if (fDepth == -1) {
						// If depth is -1, then this is an invalid frame
						throw new DebugException(new Status(IStatus.ERROR,
								JDIDebugPlugin.getUniqueIdentifier(),
								IJavaStackFrame.ERR_INVALID_STACK_FRAME,
								JDIDebugModelMessages.JDIStackFrame_25, null));
					}
				} else {
					throw new DebugException(new Status(IStatus.ERROR,
							JDIDebugPlugin.getUniqueIdentifier(),
							IJavaThread.ERR_THREAD_NOT_SUSPENDED,
							JDIDebugModelMessages.JDIStackFrame_25, null));
				}
			}
			return fStackFrame;
		}
	}

	/**
	 * Sets the underlying JDI StackFrame. Called by a thread when incrementally
	 * updating after a step has completed.
	 *
	 * @param frame
	 *            The underlying stack frame
	 */
	protected void setUnderlyingStackFrame(StackFrame frame) {
		synchronized (fThread) {
			fStackFrame = frame;
			if (frame == null) {
				fRefreshVariables = true;
			}
		}
	}

	protected void setThread(JDIThread thread) {
		fThread = thread;
	}

	protected void setVariables(List<IJavaVariable> variables) {
		fVariables = variables;
	}

	/**
	 * @see IJavaStackFrame#getLocalVariables()
	 */
	@Override
	public IJavaVariable[] getLocalVariables() throws DebugException {
		List<LocalVariable> list = getUnderlyingVisibleVariables();
		IJavaVariable[] locals = new IJavaVariable[list.size()];
		for (int i = 0; i < list.size(); i++) {
			locals[i] = new JDILocalVariable(this, list.get(i));
		}
		return locals;
	}

	/**
	 * @see IJavaStackFrame#getThis()
	 */
	@Override
	public IJavaObject getThis() throws DebugException {
		IJavaObject receiver = null;
		if (!isStatic() && !isNative()) {
			ObjectReference thisObject = getUnderlyingThisObject();
			if (thisObject != null) {
				receiver = (IJavaObject) JDIValue.createValue(
						(JDIDebugTarget) getDebugTarget(), thisObject);
			}
		}
		return receiver;
	}

	/**
	 * Java stack frames do not support registers
	 *
	 * @see IStackFrame#getRegisterGroups()
	 */
	@Override
	public IRegisterGroup[] getRegisterGroups() {
		return new IRegisterGroup[0];
	}

	/**
	 * @see IJavaStackFrame#getDeclaringType()
	 */
	@Override
	public IJavaClassType getDeclaringType() throws DebugException {
		Method method = getUnderlyingMethod();
		try {
			Type type = method.declaringType();
			if (type instanceof ClassType) {
				return (IJavaClassType) JDIType.createType(
						(JDIDebugTarget) getDebugTarget(), type);
			}
			targetRequestFailed(JDIDebugModelMessages.JDIStackFrame_0, null);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIStackFrame_exception_retreiving_declaring_type,
							e.toString()), e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaStackFrame#getReferenceType()
	 */
	@Override
	public IJavaReferenceType getReferenceType() throws DebugException {
		Method method = getUnderlyingMethod();
		try {
			Type type = method.declaringType();
			return (IJavaReferenceType) JDIType.createType(
					(JDIDebugTarget) getDebugTarget(), type);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIStackFrame_exception_retreiving_declaring_type,
							e.toString()), e);
		}
		return null;
	}

	/**
	 * Expression level stepping not supported.
	 *
	 * @see IStackFrame#getCharEnd()
	 */
	@Override
	public int getCharEnd() {
		return -1;
	}

	/**
	 * Expression level stepping not supported.
	 *
	 * @see IStackFrame#getCharStart()
	 */
	@Override
	public int getCharStart() {
		return -1;
	}

	/**
	 * Clears the cached data of this stack frame. The underlying stack frame
	 * has changed in such a way that the cached data may not be valid.
	 */
	private void clearCachedData() {
		fThisObject = null;
		fReceivingTypeName = null;
	}

	/**
	 * @see IJavaStackFrame#wereLocalsAvailable()
	 */
	@Override
	public boolean wereLocalsAvailable() {
		return fLocalsAvailable;
	}

	/**
	 * Sets whether locals were available. If the setting is not the same as the
	 * current value, a change event is fired such that a UI client can update.
	 *
	 * @param available
	 *            whether local variable information is available for this stack
	 *            frame.
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
	@Override
	public boolean hasRegisterGroups() {
		return false;
	}

	/**
	 * @see IStackFrame#hasVariables()
	 */
	@Override
	public boolean hasVariables() throws DebugException {
		return getVariables0().size() > 0;
	}

	/**
	 * @see org.eclipse.debug.core.model.IFilteredStep#canStepWithFilters()
	 */
	@Override
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
	@Override
	public void stepWithFilters() throws DebugException {
		((IJavaThread) getThread()).stepWithFilters();
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaStackFrame#getSourcePath(java.lang.String)
	 */
	@Override
	public String getSourcePath(String stratum) throws DebugException {
		synchronized (fThread) {
			try {
				return fLocation.sourcePath(stratum);
			} catch (AbsentInformationException e) {
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIStackFrame_exception_retrieving_source_path,
								e.toString()), e);
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaStackFrame#getSourcePath()
	 */
	@Override
	public String getSourcePath() throws DebugException {
		synchronized (fThread) {
			try {
				return fLocation.sourcePath();
			} catch (AbsentInformationException e) {
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIStackFrame_exception_retrieving_source_path,
								e.toString()), e);
			}
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaStackFrame#getLineNumber(java.lang.String
	 * )
	 */
	@Override
	public int getLineNumber(String stratum) throws DebugException {
		synchronized (fThread) {
			try {
				return fLocation.lineNumber(stratum);
			} catch (RuntimeException e) {
				if (getThread().isSuspended()) {
					targetRequestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIStackFrame_exception_retrieving_line_number,
									e.toString()), e);
				}
			}
		}
		return -1;
	}

	/*
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaStackFrame#getSourceName(java.lang.String
	 * )
	 */
	@Override
	public String getSourceName(String stratum) throws DebugException {
		synchronized (fThread) {
			try {
				return fLocation.sourceName(stratum);
			} catch (AbsentInformationException e) {
			} catch (NativeMethodException e) {
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIStackFrame_exception_retrieving_source_name,
								e.toString()), e);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaStackFrame#isVarargs()
	 */
	@Override
	public boolean isVarArgs() throws DebugException {
		return getUnderlyingMethod().isVarArgs();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaStackFrame#canForceReturn()
	 */
	@Override
	public boolean canForceReturn() {
		if (getJavaDebugTarget().supportsForceReturn() && isSuspended()) {
			try {
				if (!isNative()) {
					if (isTopStackFrame()) {
						return true;
					}
					List<IJavaStackFrame> frames = fThread.computeStackFrames();
					int index = frames.indexOf(this);
					if (index > 0) {
						JDIStackFrame prev = (JDIStackFrame) frames
								.get(index - 1);
						return prev.canDropToFrame();
					}
				}
			} catch (DebugException e) {
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaStackFrame#forceReturn(org.eclipse.jdt
	 * .debug.core.IJavaValue)
	 */
	@Override
	public void forceReturn(IJavaValue value) throws DebugException {
		if (isTopStackFrame()) {
			fThread.forceReturn(value);
		} else {
			// first check assignment compatible
			Method method = getUnderlyingMethod();
			try {
				ValueImpl.checkValue(((JDIValue) value).getUnderlyingValue(),
						method.returnType(),
						(VirtualMachineImpl) method.virtualMachine());
			} catch (InvalidTypeException e) {
				targetRequestFailed(JDIDebugModelMessages.JDIStackFrame_26, e);
			} catch (ClassNotLoadedException e) {
				targetRequestFailed(JDIDebugModelMessages.JDIThread_48, e);
			}
			List<IJavaStackFrame> frames = fThread.computeStackFrames();
			int index = frames.indexOf(this);
			if (index > 0) {
				JDIStackFrame prev = (JDIStackFrame) frames.get(index - 1);
				fThread.popFrame(prev);
				fThread.forceReturn(value);
			}
		}
	}

	public void setIsTop(boolean isTop) {
		this.fIsTop = isTop;
	}
}
