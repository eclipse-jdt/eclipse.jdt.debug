package org.eclipse.jdt.internal.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved. 
 */
 
import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.ReferenceType;

/**
 * The interface of an object in a debug target.
 */
public class JDIInterfaceType extends JDIType implements IJavaInterfaceType {
	
	/**
	 * Cosntructs a new interface type on the given target referencing
	 * the specified interface type.
	 */
	public JDIInterfaceType(JDIDebugTarget target, InterfaceType type) {
		super(target, type);
	}

	/**
	 * @see IJavaInterfaceType#getField(String)
	 */
	public IJavaFieldVariable getField(String name) throws DebugException {
		try {
			Field field = ((InterfaceType)getUnderlyingType()).fieldByName(name);
			if (field != null) {
				return new JDIFieldVariable(getDebugTarget(), field, null);
			}			
		} catch (RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.exception_while_retrieving_field"), new String[] {e.toString(), name}), e); //$NON-NLS-1$
		}
		// it is possible to return null		
		return null;
	}
	
	/**
	 * @see IJavaClassType#getClassObject()
	 */
	public IJavaClassObject getClassObject() throws DebugException {
		try {
			ReferenceType type= (ReferenceType)getUnderlyingType();
			return (IJavaClassObject)JDIValue.createValue(getDebugTarget(), type.classObject());
		} catch (RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIClassType.exception_while_retrieving_class_object"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		// execution will not fall through to here,
		// as #requestFailed will throw an exception
		return null;
	}	

}

