package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import java.util.ArrayList;
 
/**
 * Common functionality for variables that support value modification
 */
public abstract class JDIModificationVariable extends JDIVariable {
	
	private final static ArrayList fgValidSignatures = new ArrayList (9);
	{
		fgValidSignatures.add("B");  // byte
 		fgValidSignatures.add("C");  // char
		fgValidSignatures.add("D");  // double
		fgValidSignatures.add("F");  // float
		fgValidSignatures.add("I");  // int
		fgValidSignatures.add("J");  // long
		fgValidSignatures.add("S");  // short
		fgValidSignatures.add("Z");  // boolean
		fgValidSignatures.add(jdiStringSignature); // String
	};

	public JDIModificationVariable(JDIDebugElement parent) {
		super(parent);
	}
	
	public boolean supportsValueModification() {
		try {
			Value currentValue= getCurrentValue();
			if (currentValue != null) {
				String signature = currentValue.type().signature();
				return fgValidSignatures.contains(signature);
			} else {
				String signature = getSignature();
				return fgValidSignatures.contains(signature);
			}
		} catch (DebugException e) {
			internalError(e);
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			internalError(e);
		}
		return false;
	}
	
	protected Value generateVMValue(String expression) throws DebugException {
	
		String signature= null;
		Value cValue= getCurrentValue();
		VirtualMachine vm= getVirtualMachine();
		if (cValue == null) {
			//String with null value
			signature = jdiStringSignature;
		} else {
			signature= cValue.type().signature();
		}
		if (signature.length() > 1 && !signature.equals(jdiStringSignature)) {
			return null;
		}
		Value vmValue= null;
		try {
			switch (signature.charAt(0)) {
				case 'Z' :
					String flse= Boolean.FALSE.toString();
					String tre= Boolean.TRUE.toString();
					if (expression.equals(tre) || expression.equals(flse)) {
						boolean booleanValue= Boolean.valueOf(expression).booleanValue();
						vmValue= vm.mirrorOf(booleanValue);
					}
					break;
				case 'B' :
					byte byteValue= Byte.valueOf(expression).byteValue();
					vmValue= vm.mirrorOf(byteValue);
					break;
				case 'C' :
					if (expression.length() == 1) {
						char charValue= expression.charAt(0);
						vmValue= vm.mirrorOf(charValue);
					} else if (expression.length() == 2) {
							char charValue;
							if (!(expression.charAt(0) == '\\')) {
								return null;
							}
							switch (expression.charAt(1)) {
								case 'b':
									charValue= '\b';
									break;
								case 'f':
									charValue= '\f';
									break;
								case 'n':
									charValue= '\n';
									break;
								case 'r':
									charValue= '\r';
									break;
								case 't':
									charValue= '\t';
									break;
								case '\'':
									charValue= '\'';
									break;
								case '\"':
									charValue= '\"';
									break;
								case '\\':
									charValue= '\\';
									break;
								default :
									return null;
							}
						vmValue= vm.mirrorOf(charValue);
					}
					break;
				case 'S' :
					short shortValue= Short.valueOf(expression).shortValue();
					vmValue= vm.mirrorOf(shortValue);
					break;
				case 'I' :
					int intValue= Integer.valueOf(expression).intValue();
					vmValue= vm.mirrorOf(intValue);
					break;
				case 'J' :
					long longValue= Long.valueOf(expression).longValue();
					vmValue= vm.mirrorOf(longValue);
					break;
				case 'F' :
					float floatValue= Float.valueOf(expression).floatValue();
					vmValue= vm.mirrorOf(floatValue);
					break;
				case 'D' :
					double doubleValue= Double.valueOf(expression).doubleValue();
					vmValue= vm.mirrorOf(doubleValue);
					break;
				case 'L' :
					vmValue= vm.mirrorOf(expression);
					break;

			}
		} catch (NumberFormatException nfe) {
			return null;
		}
		return vmValue;
	}
	
	/**
	 * @see IValueModification
	 */
	public boolean verifyValue(String expression) {
		try {
			Value vmValue= generateVMValue(expression);
			return vmValue != null;
		} catch (DebugException e) {
			internalError(e);
			return false;
		}
	}
	
	/**
	 * @see IValueModification
	 */
	public final void setValue(String expression) throws DebugException {
	 	Value value= generateVMValue(expression);

		if (value == null) {
			requestFailed(ERROR_SET_VALUE, null);
		} 

		setValue(value);
		fireValueChanged();
	}

	/**
	 * Set my value to the given value
	 */
	protected abstract void setValue(Value value) throws DebugException;

	/**
	 * Returns the virtual machine to which this variable belongs.
	 */
	protected abstract VirtualMachine getVirtualMachine();
	
	/**
	 * The value of this variable has changed.
	 * Fire an event that will update the root variable and
	 * children.
	 */
	protected void fireValueChanged() {
		IDebugElement parent= getParent();
		IDebugElement varRoot= this;
		while (!(parent instanceof JDIStackFrame) && parent != null) {
			varRoot= parent;
			if (parent instanceof IValue) {
				parent= ((IValue)parent).getVariable();
			} else {
				parent= parent.getParent();
			}
		}
		((JDIDebugElement)varRoot).fireChangeEvent();
	}
}