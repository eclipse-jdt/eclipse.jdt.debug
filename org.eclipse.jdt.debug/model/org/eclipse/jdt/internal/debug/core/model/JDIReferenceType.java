/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

/**
 * References a class, interface, or array type.
 */
public abstract class JDIReferenceType extends JDIType implements IJavaReferenceType {
	
	// field names declared in this type
	private String[] fDeclaredFields = null;
	// field names declared in this type, super types, implemented interaces and superinterfaces
	private String[] fAllFields = null;

	/**
	 * Constructs a new reference type in the given target.
	 * 
	 * @param target associated vm
	 * @param type reference type
	 */
	public JDIReferenceType(JDIDebugTarget target, Type type) {
		super(target, type);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getAvailableStrata()
	 */
	public String[] getAvailableStrata() {
		List strata = getReferenceType().availableStrata();
		return (String[])strata.toArray(new String[strata.size()]);
	}

	/**
	 * Returns the underlying reference type.
	 * 
	 * @return the underlying reference type
	 */
	protected ReferenceType getReferenceType() {
		return (ReferenceType)getUnderlyingType();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getDefaultStratum()
	 */
	public String getDefaultStratum() throws DebugException {
        try {
            return getReferenceType().defaultStratum();
        } catch (RuntimeException e) {
            targetRequestFailed(JDIDebugModelMessages.JDIReferenceType_1, e);
        }
        // exectution will not reach here
        return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getField(java.lang.String)
	 */
	public IJavaFieldVariable getField(String name) throws DebugException {
		try {
			ReferenceType type = (ReferenceType)getUnderlyingType();
			Field field = type.fieldByName(name);
			if (field != null && field.isStatic()) {
				return new JDIFieldVariable(getJavaDebugTarget(), field, type);
			}			
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIClassType_exception_while_retrieving_field, new String[] {e.toString(), name}), e); 
		}
		// it is possible to return null		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getClassObject()
	 */
	public IJavaClassObject getClassObject() throws DebugException {
		try {
			ReferenceType type= (ReferenceType)getUnderlyingType();
			return (IJavaClassObject)JDIValue.createValue(getJavaDebugTarget(), type.classObject());
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIClassType_exception_while_retrieving_class_object, new String[] {e.toString()}), e); 
		}
		// execution will not fall through to here,
		// as #requestFailed will throw an exception
		return null;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getAllFieldNames()
	 */
	public String[] getAllFieldNames() throws DebugException {
		if (fAllFields == null) {
			try {
				List fields = ((ReferenceType)getUnderlyingType()).allFields();
				fAllFields = new String[fields.size()];
				Iterator iterator = fields.iterator();
				int i = 0;
				while (iterator.hasNext()) {
					Field field = (Field)iterator.next();
					fAllFields[i] = field.name();
					i++;
				}
			} catch (RuntimeException e) {
				targetRequestFailed(JDIDebugModelMessages.JDIReferenceType_2, e); 
			}			
		}			
		return fAllFields;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getDeclaredFieldNames()
	 */
	public String[] getDeclaredFieldNames() throws DebugException {
		if (fDeclaredFields == null) {
			try {
				List fields = ((ReferenceType)getUnderlyingType()).fields();
				fDeclaredFields = new String[fields.size()];
				Iterator iterator = fields.iterator();
				int i = 0;
				while (iterator.hasNext()) {
					Field field = (Field)iterator.next();
					fDeclaredFields[i] = field.name();
					i++;
				}
			} catch (RuntimeException e) {
				targetRequestFailed(JDIDebugModelMessages.JDIReferenceType_3, e); 
			}			
		}
		return fDeclaredFields;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getSourcePaths(java.lang.String)
	 */
	public String[] getSourcePaths(String stratum) throws DebugException {
		try {
			List sourcePaths= getReferenceType().sourcePaths(stratum);
			return (String[]) sourcePaths.toArray(new String[sourcePaths.size()]);
		} catch (AbsentInformationException e) {
		} catch (RuntimeException e) {
			requestFailed(JDIDebugModelMessages.JDIReferenceType_4, e, DebugException.TARGET_REQUEST_FAILED);
		}
		return null;
	}

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getSourceName()
	 */
	public String getSourceName() throws DebugException {
		try {
			return getReferenceType().sourceName();
		} catch (AbsentInformationException e) {
		} catch (RuntimeException e) {
			requestFailed(JDIDebugModelMessages.JDIReferenceType_4, e, DebugException.TARGET_REQUEST_FAILED);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getSourceNames(java.lang.String)
	 */
	public String[] getSourceNames(String stratum) throws DebugException {
		try {
			List sourceNames= getReferenceType().sourceNames(stratum);
			return (String[]) sourceNames.toArray(new String[sourceNames.size()]);
		} catch (AbsentInformationException e) {
		} catch (RuntimeException e) {
			requestFailed(JDIDebugModelMessages.JDIReferenceType_4, e, DebugException.TARGET_REQUEST_FAILED);
		}
		return null;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getClassLoaderObject()
     */
    public IJavaObject getClassLoaderObject() throws DebugException {
        try {
            ReferenceType type= (ReferenceType)getUnderlyingType();
            ClassLoaderReference classLoader = type.classLoader();
            if (classLoader != null) {
            	return (IJavaObject)JDIValue.createValue(getJavaDebugTarget(), classLoader);
            }
        } catch (RuntimeException e) {
            targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIReferenceType_0, new String[] {e.toString()}), e); 
        }
        return null;
    }
    

	static public String getGenericName(ReferenceType type) throws DebugException {
		if (type instanceof ArrayType) {
			try {
				Type componentType;
					componentType= ((ArrayType)type).componentType();
				if (componentType instanceof ReferenceType) {
					return getGenericName((ReferenceType)componentType) + "[]"; //$NON-NLS-1$
				}
				return type.name();
			} catch (ClassNotLoadedException e) {
				// we cannot create the generic name using the component type,
				// just try to create one with the infos
			}
		}
		String signature= type.signature();
		StringBuffer res= new StringBuffer(getTypeName(signature));
		String genericSignature= type.genericSignature();
		if (genericSignature != null) {
			String[] typeParameters= Signature.getTypeParameters(genericSignature);
			if (typeParameters.length > 0) {
				res.append('<').append(Signature.getTypeVariable(typeParameters[0]));
				for (int i= 1; i < typeParameters.length; i++) {
					res.append(',').append(Signature.getTypeVariable(typeParameters[i]));
				}
				res.append('>');
			}
		}
		return res.toString();
	}
	
	/**
	 * Return the name from the given signature.
	 * Keep the '$' characters.
	 */
	static public String getTypeName(String genericTypeSignature) {
		int arrayDimension= 0;
		while (genericTypeSignature.charAt(arrayDimension) == '[') {
			arrayDimension++;
		}
		int parameterStart= genericTypeSignature.indexOf('<');
		StringBuffer name= new StringBuffer();
		if (parameterStart < 0) {
			name.append(genericTypeSignature.substring(arrayDimension + 1, genericTypeSignature.length() - 1).replace('/', '.'));
		} else {
			name.append(genericTypeSignature.substring(arrayDimension + 1, parameterStart).replace('/', '.'));
			name.append(Signature.toString(genericTypeSignature).substring(parameterStart - 1 - arrayDimension).replace('/', '.'));
		}
		for (int i= 0; i < arrayDimension; i++) {
			name.append("[]"); //$NON-NLS-1$
		}
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getGenericSignature()
	 */
	public String getGenericSignature() throws DebugException {
		return getReferenceType().genericSignature();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getInstances(long)
	 */
	public IJavaObject[] getInstances(long max) throws DebugException {
		try {
			List list = getReferenceType().instances(max);
			IJavaObject[] instances = new IJavaObject[list.size()];
			for (int i = 0; i < instances.length; i++) {
				instances[i] = (IJavaObject) JDIValue.createValue(getJavaDebugTarget(), (Value) list.get(i));
			}
			return instances;
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIReferenceType_5, e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaReferenceType#getInstanceCount()
	 */
	public long getInstanceCount() throws DebugException {
		JDIDebugTarget target = getJavaDebugTarget();
		if (target.supportsInstanceRetrieval()) {
			ArrayList list = new ArrayList(2);
			list.add(getUnderlyingType());
			VirtualMachine vm = getVM();
			try {
				long[] counts = vm.instanceCounts(list);
				return counts[0];
			} catch (RuntimeException e) {
				targetRequestFailed(JDIDebugModelMessages.JDIReferenceType_5, e);
			}
		}
		return -1;
	}
}
