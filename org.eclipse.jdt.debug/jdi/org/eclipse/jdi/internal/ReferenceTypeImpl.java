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
package org.eclipse.jdi.internal;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpFieldID;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpMethodID;
import org.eclipse.jdi.internal.jdwp.JdwpReferenceTypeID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidLineNumberException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class ReferenceTypeImpl extends TypeImpl implements ReferenceType, org.eclipse.jdi.hcr.ReferenceType {

	/** ClassStatus Constants. */
	public static final int JDWP_CLASS_STATUS_VERIFIED = 1;
	public static final int JDWP_CLASS_STATUS_PREPARED = 2;
	public static final int JDWP_CLASS_STATUS_INITIALIZED = 4;
	public static final int JDWP_CLASS_STATUS_ERROR = 8;

	/** Mapping of command codes to strings. */
	private static String[] fgClassStatusStrings = null;
	
	/** ReferenceTypeID that corresponds to this reference. */
	private JdwpReferenceTypeID fReferenceTypeID;

	/** The following are the stored results of JDWP calls. */
	protected List fInterfaces = null;
	private List fMethods = null;
	private Hashtable fMethodTable= null;
	private List fFields = null;
	private List fAllMethods = null;
	private List fVisibleMethods = null;
	private List fAllFields = null;
	private List fVisibleFields = null;
	private List fAllInterfaces = null;
	private List fAllLineLocations = null;
	private String fSourcename = null;
	private int fModifierBits = -1;
	private ClassLoaderReferenceImpl fClassLoader = null;
	private ClassObjectReferenceImpl fClassObject = null;

	private boolean fGotClassFileVersion = false;	// HCR addition.
	private int fClassFileVersion;	// HCR addition.
	private boolean fIsHCREligible;	// HCR addition.
	private boolean fIsVersionKnown;	// HCR addition.
	
	/**
	 * Creates new instance.
	 */
	protected ReferenceTypeImpl(String description, VirtualMachineImpl vmImpl, JdwpReferenceTypeID referenceTypeID) {
		super(description, vmImpl);
		fReferenceTypeID = referenceTypeID;
	}
	
	/**
	 * Creates new instance.
	 */
	protected ReferenceTypeImpl(String description, VirtualMachineImpl vmImpl, JdwpReferenceTypeID referenceTypeID, String signature) {
		super(description, vmImpl);
		fReferenceTypeID = referenceTypeID;
		setSignature(signature);
	}
	
	/**
	 * @return Returns type tag.
	 */
	public abstract byte typeTag();
	
	/**
	 * Flushes all stored Jdwp results.
	 */
	public void flushStoredJdwpResults() {
		Iterator iter;
	
		// Flush Methods.
		if (fMethods != null) {
			iter = fMethods.iterator();
			while (iter.hasNext()) {
				MethodImpl method = (MethodImpl)iter.next();
				method.flushStoredJdwpResults();
			}
			fMethods = null;
			fMethodTable= null;
		}

		// Flush Fields.
		if (fFields != null) {
			iter = fFields.iterator();
			while (iter.hasNext()) {
				FieldImpl field = (FieldImpl)iter.next();
				field.flushStoredJdwpResults();
			}
			fFields = null;
		}

		fInterfaces = null;
		fAllMethods = null;
		fVisibleMethods = null;
		fAllFields = null;
		fVisibleFields = null;
		fAllInterfaces = null;
		fAllLineLocations = null;
		fSourcename = null;
		fModifierBits = -1;
		fClassLoader = null;
		fClassObject = null;
		fGotClassFileVersion = false;
		
		// The following cached results are stored higher up in the class hierarchy.
		fSignature = null;
		fSourcename = null;
	}
	
	/** 
	 * @return Returns the interfaces declared as implemented by this class. Interfaces indirectly implemented (extended by the implemented interface or implemented by a superclass) are not included.
	 */
	public List allInterfaces() {
		if (fAllInterfaces != null) {
			return fAllInterfaces;
		}
	
		/* Recursion:
		 * The interfaces that it directly implements;
		 * All interfaces that are implemented by its interfaces;
		 * If it is a class, all interfaces that are implemented by its superclass.
		 */
		// The interfaces are maintained in a set, to avoid duplicates.
		// The interfaces of its own (own interfaces() command) are first inserted.
		HashSet allInterfacesSet = new HashSet(interfaces());
		
		// All interfaces of the interfaces it implements.
		Iterator interfaces = interfaces().iterator();
		InterfaceTypeImpl inter;
		while (interfaces.hasNext()) {
			inter = (InterfaceTypeImpl)interfaces.next();
			allInterfacesSet.addAll(inter.allInterfaces());
		}
		
		// If it is a class, all interfaces of it's superclass.
		if (this instanceof ClassType) {
			ClassType superclass = ((ClassType)this).superclass();
			if (superclass != null) {
				allInterfacesSet.addAll(superclass.allInterfaces());
			}
		}
				
		fAllInterfaces = new ArrayList(allInterfacesSet);
		return fAllInterfaces;
	}
	
	/**
	 * @return Returns Jdwp Reference ID.
	 */
	public JdwpReferenceTypeID getRefTypeID() {
		return fReferenceTypeID;
	}
	
	/**
	 * @return Returns modifier bits.
	 */
	public int modifiers() {
		if (fModifierBits != -1)
			return fModifierBits;
			
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.RT_MODIFIERS, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fModifierBits = readInt("modifiers", AccessibleImpl.getModifierStrings(), replyData); //$NON-NLS-1$
			return fModifierBits;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/** 
	 * Add methods to a set of methods if they are not overriden, add new names+signature combinations to set of names+signature combinations.
	 */
	private void addVisibleMethods(List inheritedMethods, Set nameAndSignatures, List resultMethods) {
		Iterator iter = inheritedMethods.iterator();
		MethodImpl inheritedMethod;
		while (iter.hasNext()) {
			inheritedMethod = (MethodImpl)iter.next();
			if (!nameAndSignatures.contains(inheritedMethod.name() + inheritedMethod.signature())) {
				resultMethods.add(inheritedMethod);
			}
		}
	}
	
	/**
	 * @return Returns a list containing each unhidden and unambiguous Method in this type.
	 */
	public List visibleMethods() {
		if (fVisibleMethods != null)
			return fVisibleMethods;

		/* Recursion:
		 * The methods of its own (own methods() command);
		 * All methods of the interfaces it implements;
		 * If it is a class, all methods of it's superclass.
		 */
		// The name+signature combinations of methods are maintained in a set, to avoid including methods that have been overriden.
		Set namesAndSignatures = new HashSet();
		List visibleMethods= new ArrayList();
		
		// The methods of its own (own methods() command).
		for (Iterator iter= methods().iterator(); iter.hasNext();) {
			MethodImpl method= (MethodImpl) iter.next();
			namesAndSignatures.add(method.name() + method.signature());
			visibleMethods.add(method);
		}

		// All methods of the interfaces it implements.
		Iterator interfaces = interfaces().iterator();
		InterfaceTypeImpl inter;
		while (interfaces.hasNext()) {
			inter = (InterfaceTypeImpl)interfaces.next();
			addVisibleMethods(inter.visibleMethods(), namesAndSignatures, visibleMethods);
		}
		
		// If it is a class, all methods of it's superclass.
		if (this instanceof ClassType) {
			ClassType superclass = ((ClassType)this).superclass();
			if (superclass != null)
				addVisibleMethods(superclass.visibleMethods(), namesAndSignatures, visibleMethods);
		}
		
		fVisibleMethods= visibleMethods;
		return fVisibleMethods;
	}

	/**
	 * @return Returns a list containing each Method declared in this type, and its superclasses, implemented interfaces, and/or superinterfaces.
	 */
	public List allMethods() {
		if (fAllMethods != null)
			return fAllMethods;

		/* Recursion:
		 * The methods of its own (own methods() command);
		 * All methods of the interfaces it implements;
		 * If it is a class, all methods of it's superclass.
		 */
		// The name+signature combinations of methods are maintained in a set.
		HashSet resultSet = new HashSet();
		
		// The methods of its own (own methods() command).
		resultSet.addAll(methods());
		
		// All methods of the interfaces it implements.
		Iterator interfaces = interfaces().iterator();
		InterfaceTypeImpl inter;
		while (interfaces.hasNext()) {
			inter = (InterfaceTypeImpl)interfaces.next();
			resultSet.addAll(inter.allMethods());
		}
		
		// If it is a class, all methods of it's superclass.
		if (this instanceof ClassType) {
			ClassType superclass = ((ClassType)this).superclass();
			if (superclass != null)
				resultSet.addAll(superclass.allMethods());
		}
		
		fAllMethods  = new ArrayList(resultSet);
		return fAllMethods;
	}

	/** 
	 * @return Returns the interfaces declared as implemented by this class. Interfaces indirectly implemented (extended by the implemented interface or implemented by a superclass) are not included.
	 */
	public List interfaces() {
		if (fInterfaces != null) {
			return fInterfaces;
		}
			
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.RT_INTERFACES, this);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.NOT_FOUND:
					// Workaround for problem in J2ME WTK (wireless toolkit)
					// @see Bug 12966
					return Collections.EMPTY_LIST;
				default:
					defaultReplyErrorHandler(replyPacket.errorCode());
			}
			DataInputStream replyData = replyPacket.dataInStream();
			List elements = new ArrayList();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			for (int i = 0; i < nrOfElements; i++) {
				InterfaceTypeImpl ref = InterfaceTypeImpl.read(this, replyData);
				if (ref == null) {
					continue;
				}
				elements.add(ref);
			}
			fInterfaces = elements;
			return elements;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
			
	/** 
	 * Add fields to a set of fields if they are not overriden, add new fieldnames to set of fieldnames.
	 */
	private void addVisibleFields(List newFields, Set names, List resultFields) {
		Iterator iter = newFields.iterator();
		FieldImpl field;
		while (iter.hasNext()) {
			field = (FieldImpl)iter.next();
			String name = field.name();
			if (!names.contains(name)) {
				resultFields.add(field);
				names.add(name);
			}
		}
	}
	
	/**
	 * @return Returns a list containing each unhidden and unambiguous Field in this type.
	 */
	public List visibleFields() {
		if (fVisibleFields != null)
			return fVisibleFields;

		/* Recursion:
		 * The fields of its own (own fields() command);
		 * All fields of the interfaces it implements;
		 * If it is a class, all fields of it's superclass.
		 */
		// The names of fields are maintained in a set, to avoid including fields that have been overriden.
		HashSet fieldNames = new HashSet();
		
		// The fields of its own (own fields() command).
		List visibleFields = new ArrayList();
		addVisibleFields(fields(), fieldNames, visibleFields);
		
		// All fields of the interfaces it implements.
		Iterator interfaces = interfaces().iterator();
		InterfaceTypeImpl inter;
		while (interfaces.hasNext()) {
			inter = (InterfaceTypeImpl)interfaces.next();
			addVisibleFields(inter.visibleFields(), fieldNames, visibleFields);
		}
		
		// If it is a class, all fields of it's superclass.
		if (this instanceof ClassType) {
			ClassType superclass = ((ClassType)this).superclass();
			if (superclass != null)
				addVisibleFields(superclass.visibleFields(), fieldNames, visibleFields);
		}
				
		fVisibleFields = visibleFields;
		return fVisibleFields;
	}

	/** 
	 * @return Returns a list containing each Field declared in this type, and its superclasses, implemented interfaces, and/or superinterfaces.
	 */
	public List allFields() {
		if (fAllFields != null)
			return fAllFields;

		/* Recursion:
		 * The fields of its own (own fields() command);
		 * All fields of the interfaces it implements;
		 * If it is a class, all fields of it's superclass.
		 */
		// The names of fields are maintained in a set, to avoid including fields that have been inherited double.
		HashSet resultSet = new HashSet();
		
		// The fields of its own (own fields() command).
		resultSet.addAll(fields());
		
		// All fields of the interfaces it implements.
		Iterator interfaces = interfaces().iterator();
		InterfaceTypeImpl inter;
		while (interfaces.hasNext()) {
			inter = (InterfaceTypeImpl)interfaces.next();
			resultSet.addAll(inter.allFields());
		}
		
		// If it is a class, all fields of it's superclass.
		if (this instanceof ClassType) {
			ClassType superclass = ((ClassType)this).superclass();
			if (superclass != null)
				resultSet.addAll(superclass.allFields());
		}
				
		fAllFields = new ArrayList(resultSet);
		return fAllFields;
	}
	
	/** 
	 * @return Returns the classloader object which loaded the class corresponding to this type.
	 */
	public ClassLoaderReference classLoader() {
		if (fClassLoader != null)
			return fClassLoader;

		initJdwpRequest();
		try {
	   		JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.RT_CLASS_LOADER, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fClassLoader = ClassLoaderReferenceImpl.read(this, replyData);
			return fClassLoader;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
	   		return null;
		} finally {
			handledJdwpRequest();
		}
	}
		
	/** 
	 * @return Returns the class object that corresponds to this type in the target VM. 
	 */
	public ClassObjectReference classObject() {
		if (fClassObject != null)
			return fClassObject;

		initJdwpRequest();
		try {
	   		JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.RT_CLASS_OBJECT, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fClassObject = ClassObjectReferenceImpl.read(this, replyData);
			return fClassObject;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
	   		return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/** 
	 * @return Returns status of class/interface.
	 */
	protected int status() { 
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.RT_STATUS, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			int status = readInt("status", classStatusStrings(), replyData); //$NON-NLS-1$
			return status;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/** 
	 * @return Returns true if initialization failed for this class.
	 */
	public boolean failedToInitialize() {
		return (status() & JDWP_CLASS_STATUS_ERROR) != 0;
	}

	/** 
	 * @return Returns true if this type has been initialized.
	 */
	public boolean isInitialized() {
		return (status() & JDWP_CLASS_STATUS_INITIALIZED) != 0;
	}

	/** 
	 * @return Returns true if this type has been prepared.
	 */
	public boolean isPrepared() {
		return (status() & JDWP_CLASS_STATUS_PREPARED) != 0;
	}

	/** 
	 * @return Returns true if this type has been verified.
	 */
	public boolean isVerified() {
		return (status() & JDWP_CLASS_STATUS_VERIFIED) != 0;
	}

	/** 
	 * @return Returns the visible Field with the given non-ambiguous name. 
	 */
	public Field fieldByName(String name) {
		Iterator iter = visibleFields().iterator();
		while (iter.hasNext()) {
			FieldImpl field = (FieldImpl)iter.next();
			if (field.name().equals(name))
				return field;
		}
		return null;
	}
	
	/** 
	 * @return Returns a list containing each Field declared in this type. 
	 */
	public List fields() {
		if (fFields != null) {
			return fFields;
		}
		
		// Note: Fields are returned in the order they occur in the class file, therefore their
		// order in this list can be used for comparisons.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.RT_FIELDS, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			List elements = new ArrayList();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			for (int i = 0; i < nrOfElements; i++) {
				FieldImpl elt = FieldImpl.readWithNameSignatureModifiers(this, this, replyData);
				if (elt == null) {
					continue;
				}
				elements.add(elt);
			}
			fFields = elements;
			return elements;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/** 
	 * @return Returns FieldImpl of a field in the reference specified by a given fieldID, or null if not found.
	 */
	public FieldImpl findField(JdwpFieldID fieldID) {
		Iterator iter = fields().iterator();
		while(iter.hasNext()) {
			FieldImpl field = (FieldImpl)iter.next();
			if (field.getFieldID().equals(fieldID))
				return field;
		}
		return null;
	}
	
	/** 
	 * @return Returns MethodImpl of a method in the reference specified by a given methodID, or null if not found.
	 */
	public MethodImpl findMethod(JdwpMethodID methodID) {
		if (methodID.value() == 0) {
			return new MethodImpl(virtualMachineImpl(), this, methodID, JDIMessages.getString("ReferenceTypeImpl.Obsolete_method_1"), "", -1); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (fMethodTable == null) {
			fMethodTable= new Hashtable();
			Iterator iter = methods().iterator();
			while(iter.hasNext()) {
				MethodImpl method = (MethodImpl)iter.next();
				fMethodTable.put(method.getMethodID(), method);
			}
		}
		return (MethodImpl)fMethodTable.get(methodID);
	}
	
	/** 
	 * @return Returns the Value of a given static Field in this type. 
	 */
	public Value getValue(Field field) {
		ArrayList list = new ArrayList(1);
		list.add(field);
		return (ValueImpl)getValues(list).get(field);
	}
		
	/** 
	 * @return a Map of the requested static Field objects with their Value.
	 */
	public Map getValues(List fields) {
		// if the field list is empty, nothing to do
		if (fields.isEmpty()) {
			return new HashMap();
		}
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			int fieldsSize = fields.size();
			write(this, outData);
			writeInt(fieldsSize, "size", outData); //$NON-NLS-1$
			for (int i = 0; i < fieldsSize; i++) {
				FieldImpl field = (FieldImpl)fields.get(i);
				checkVM(field);
				field.getFieldID().write(outData);
			}
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.RT_GET_VALUES, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
			HashMap map = new HashMap();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			if (nrOfElements != fieldsSize) 
				throw new InternalError(JDIMessages.getString("ReferenceTypeImpl.Retrieved_a_different_number_of_values_from_the_VM_than_requested_3")); //$NON-NLS-1$
				
			for (int i = 0; i < nrOfElements; i++) {
				map.put(fields.get(i), ValueImpl.readWithTag(this, replyData));
			}
			return map;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/** 
	 * @return Returns the hash code value.
	 */
	public int hashCode() {
		return fReferenceTypeID.hashCode();
	}
	
	/**
	 * @return Returns true if two mirrors refer to the same entity in the target VM.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		return object != null
			&& object.getClass().equals(this.getClass())
			&& fReferenceTypeID.equals(((ReferenceTypeImpl)object).fReferenceTypeID)
			&& virtualMachine().equals(((MirrorImpl)object).virtualMachine());
	}
	
	/**
	 * @return Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Object object) {
		if (object == null || !object.getClass().equals(this.getClass()))
			throw new ClassCastException(JDIMessages.getString("ReferenceTypeImpl.Can__t_compare_reference_type_to_given_object_4")); //$NON-NLS-1$
		return name().compareTo(((ReferenceType)object).name());
	}
	
	/** 
	 * @return Returns true if the type was declared abstract.
	 */
	public boolean isAbstract() {
		return (modifiers() & MODIFIER_ACC_ABSTRACT) != 0;
	}
	
	/** 
	 * @return Returns true if the type was declared final.
	 */
	public boolean isFinal() {
		return (modifiers() & MODIFIER_ACC_FINAL) != 0;
	}
	
	/** 
	 * @return Returns true if the type was declared static.
	 */
	public boolean isStatic() {
		return (modifiers() & MODIFIER_ACC_STATIC) != 0;
	}
	
	/**
	 * @return Returns a List filled with all Location objects that map to the given line number. 
	 */
	public List locationsOfLine(int line) throws AbsentInformationException {
		Iterator allMethods = methods().iterator();
		AbsentInformationException absentInformationException = null;
		while (allMethods.hasNext()) {
			MethodImpl method = (MethodImpl)allMethods.next();
			if (method.isAbstract() || method.isNative()) {
					continue;
			}
			try {	
				return method.locationsOfLine(line);
			} catch (InvalidLineNumberException e) {
				continue;
			} catch (AbsentInformationException e) {
				absentInformationException = e;
			}
		}
		if (absentInformationException != null) {
			throw absentInformationException;
		}
		throw new InvalidLineNumberException(JDIMessages.getString("ReferenceTypeImpl.No_executable_code_at_line__5") + line  + JDIMessages.getString("ReferenceTypeImpl._6")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * @return Returns a list containing each Method declared directly in this type.
	 */
	public List methods() {
		// Note that ArrayReference overwrites this method by returning an empty list.
		if (fMethods != null)
			return fMethods;
		
		// Note: Methods are returned in the order they occur in the class file, therefore their
		// order in this list can be used for comparisons.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.RT_METHODS, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			List elements = new ArrayList();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			for (int i = 0; i < nrOfElements; i++) {
				MethodImpl elt = MethodImpl.readWithNameSignatureModifiers(this, this, replyData);
				if (elt == null) {
					continue;
				}
				elements.add(elt);
			}
			fMethods = elements;
			return elements;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * @return Returns a List containing each visible Method that has the given name.
	 */
	public List methodsByName(String name) {
		List elements = new ArrayList();
		Iterator iter = visibleMethods().iterator();
		while (iter.hasNext()) {
			MethodImpl method = (MethodImpl)iter.next();
			if (method.name().equals(name)){
				elements.add(method);
			}
		}
		return elements;
	}

	/**
	 * @return Returns a List containing each visible Method that has the given name and signature.
	 */
	public List methodsByName(String name, String signature) {
		List elements = new ArrayList();
		Iterator iter = visibleMethods().iterator();
		while (iter.hasNext()) {
			MethodImpl method = (MethodImpl)iter.next();
			if (method.name().equals(name) && method.signature().equals(signature)) {
				elements.add(method);
			}
		}
		return elements;
	}

	/** 
	 * @return Returns the fully qualified name of this type.
	 */
	public String name() {
		// Make sure that we know the signature, from which the name is derived.
		if (fName == null)
			setName(signatureToName(signature()));
		
		return fName;
	}
	
	/** 
	 * @return Returns the JNI-style signature for this type. 
	 */
	public String signature() {
		if (fSignature != null)
			return fSignature;

		initJdwpRequest();
		try {
	   		JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.RT_SIGNATURE, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			setSignature(readString("signature", replyData)); //$NON-NLS-1$
			return fSignature;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
	   		return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/** 
	 * @return Returns a List containing each ReferenceType declared within this type. 
	 */
	public List nestedTypes() {
		// Note that the VM gives an empty reply on RT_NESTED_TYPES, therefore we search for the
		// nested types in the loaded types.
		List result = new ArrayList();
		Iterator itr = virtualMachineImpl().allRefTypes();
		while (itr.hasNext()) {
			try {
				ReferenceTypeImpl refType = (ReferenceTypeImpl)itr.next();
				String refName = refType.name();
				if (refName.length() > name().length() && refName.startsWith(name()) && refName.charAt(name().length()) == '$') {
					result.add(refType);
				}
			} catch (ClassNotPreparedException e) {
				continue;
			}
		}
		return result;
	}

	/** 
	 * @return Returns an identifing name for the source corresponding to the declaration of this type.
	 */
	public String sourceName() throws AbsentInformationException {
		if (fSourcename != null) {
			return fSourcename;
		}

		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.RT_SOURCE_FILE, this);
			if (replyPacket.errorCode() == JdwpReplyPacket.ABSENT_INFORMATION)
				throw new AbsentInformationException(JDIMessages.getString("ReferenceTypeImpl.Source_name_is_not_known_7")); //$NON-NLS-1$
			else
				defaultReplyErrorHandler(replyPacket.errorCode());
		
			DataInputStream replyData = replyPacket.dataInStream();
			fSourcename = readString("source name", replyData); //$NON-NLS-1$
			return fSourcename;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the CRC-32 of the given reference type, undefined if unknown.
	 */
	public int getClassFileVersion() {
		virtualMachineImpl().checkHCRSupported();
		if (fGotClassFileVersion)
			return fClassFileVersion;
		
		initJdwpRequest();
		try {
	   		JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.HCR_GET_CLASS_VERSION, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fIsHCREligible = readBoolean("HCR eligible", replyData); //$NON-NLS-1$
			fIsVersionKnown = readBoolean("version known", replyData); //$NON-NLS-1$
			fClassFileVersion = readInt("class file version", replyData); //$NON-NLS-1$
			fGotClassFileVersion = true;
			return fClassFileVersion;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
	   		return 0;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * @return Returns whether the CRC-32 of the given reference type is known.
	 */
	public boolean isVersionKnown() {
		getClassFileVersion();
		return fIsVersionKnown;
	}
	
	/**
	 * @return Returns whether the reference type is HCR-eligible.
	 */
	public boolean isHCREligible() {
		getClassFileVersion();
		return fIsHCREligible;
	}
	
	/**
	 * Writes JDWP representation.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		fReferenceTypeID.write(out);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("referenceType", fReferenceTypeID.value()); //$NON-NLS-1$
	}
	
	/**
	 * Writes representation of null referenceType.
	 */
	public static void writeNull(MirrorImpl target, DataOutputStream out) throws IOException {
		// create null id
		JdwpReferenceTypeID ID = new JdwpReferenceTypeID(target.virtualMachineImpl());
		ID.write(out);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("referenceType", ID.value()); //$NON-NLS-1$
	}
	
	/**
	 * Writes JDWP representation.
	 */
	public void writeWithTag(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeByte(typeTag(), "type tag", JdwpID.typeTagMap(), out); //$NON-NLS-1$
		write(target, out);
	}
	
	/**
	 * @return Reads JDWP representation and returns new or cached instance.
	 */
	public static ReferenceTypeImpl readWithTypeTag(MirrorImpl target, DataInputStream in) throws IOException {
		byte typeTag = target.readByte("type tag", JdwpID.typeTagMap(), in); //$NON-NLS-1$
		switch (typeTag) {
	   		case 0:
				return null;
			case ArrayTypeImpl.typeTag:
				return ArrayTypeImpl.read(target, in);
			case ClassTypeImpl.typeTag:
				return ClassTypeImpl.read(target, in);
			case InterfaceTypeImpl.typeTag:
				return InterfaceTypeImpl.read(target, in);
		}
		throw new InternalException(JDIMessages.getString("ReferenceTypeImpl.Invalid_ReferenceTypeID_tag_encountered___8") + typeTag); //$NON-NLS-1$
	}
	
	/**
	 * @return Returns the Location objects for each executable source line in this reference type.
	 */
	public List allLineLocations() throws AbsentInformationException {
		if (fAllLineLocations != null) {
			return fAllLineLocations;
		}

		Iterator allMethods = methods().iterator();
		List locations = new ArrayList();
		while (allMethods.hasNext()) {
			MethodImpl method = (MethodImpl)allMethods.next();
			if (method.isAbstract() || method.isNative()) {
				continue;
			}
			locations.addAll(method.allLineLocations());
		}
		fAllLineLocations = locations;
		return fAllLineLocations;
	}
	
	/**
	 * @return Reads JDWP representation and returns new or cached instance.
	 */
	public static ReferenceTypeImpl readWithTypeTagAndSignature(MirrorImpl target, DataInputStream in) throws IOException {
		byte typeTag = target.readByte("type tag", JdwpID.typeTagMap(), in); //$NON-NLS-1$
		switch (typeTag) {
	   		case 0:
				return null;
			case ArrayTypeImpl.typeTag:
				return ArrayTypeImpl.readWithSignature(target, in);
			case ClassTypeImpl.typeTag:
				return ClassTypeImpl.readWithSignature(target, in);
			case InterfaceTypeImpl.typeTag:
				return InterfaceTypeImpl.readWithSignature(target, in);
		}
		throw new InternalException(JDIMessages.getString("ReferenceTypeImpl.Invalid_ReferenceTypeID_tag_encountered___8") + typeTag); //$NON-NLS-1$
	}
		
	/**
	 * @return Returns new instance based on signature and classLoader.
	 * @throws ClassNotLoadedException when the ReferenceType has not been loaded by the specified class loader.
	 */
	public static TypeImpl create(VirtualMachineImpl vmImpl, String signature, ClassLoaderReference classLoader) throws ClassNotLoadedException {
		ReferenceTypeImpl refTypeBootstrap = null;
		List classes= vmImpl.classesBySignature(signature);
		ReferenceTypeImpl type;
		Iterator iter= classes.iterator();		
		while (iter.hasNext()) {
			// First pass. Look for a class loaded by the given class loader
			type = (ReferenceTypeImpl)iter.next();
			if (type.classLoader() == null) {	// bootstrap classloader
				if (classLoader == null) {
					return type;
				} else {
					refTypeBootstrap = type;
				}
			}
			if (classLoader != null && classLoader.equals(type.classLoader())) {
				return type;
			}
		}
		// If no ReferenceType is found with the specified classloader, but there is one with the
		// bootstrap classloader, the latter is returned.
		if (refTypeBootstrap != null) {
			return refTypeBootstrap;
		}
		
		List visibleTypes;
		iter= classes.iterator();
		while (iter.hasNext()) {
			// Second pass. Look for a class that is visible to
			// the given class loader
			type = (ReferenceTypeImpl)iter.next();
			visibleTypes= classLoader.visibleClasses();
			Iterator visibleIter= visibleTypes.iterator();
			while (visibleIter.hasNext()) {
				if (type.equals(visibleIter.next())) {
					return type;
				}
			}
		}

		throw new ClassNotLoadedException(TypeImpl.classSignatureToName(signature), JDIMessages.getString("ReferenceTypeImpl.Type_has_not_been_loaded_10")); //$NON-NLS-1$
	}
	
	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fgClassStatusStrings != null) {
			return;
		}
		
		java.lang.reflect.Field[] fields = ReferenceTypeImpl.class.getDeclaredFields();
		fgClassStatusStrings = new String[32];
		
		for (int i = 0; i < fields.length; i++) {
			java.lang.reflect.Field field = fields[i];
			if ((field.getModifiers() & Modifier.PUBLIC) == 0 || (field.getModifiers() & Modifier.STATIC) == 0 || (field.getModifiers() & Modifier.FINAL) == 0) {
				continue;
			}
				
			String name = field.getName();
			if (!name.startsWith("JDWP_CLASS_STATUS_")) { //$NON-NLS-1$
				continue;
			}
				
			name = name.substring(18);
			
			try {
				int value = field.getInt(null);
				
				for (int j = 0; j < fgClassStatusStrings.length; j++) {
					if ((1 << j & value) != 0) {
						fgClassStatusStrings[j]= name;
						break;
					}
				}
			} catch (IllegalAccessException e) {
				// Will not occur for own class.
			} catch (IllegalArgumentException e) {
				// Should not occur.
				// We should take care that all public static final constants
				// in this class are numbers that are convertible to int.
			}
		}
	}
	
	/**
	 * @return Returns a map with string representations of tags.
	 */
	 public static String[] classStatusStrings() {
	 	getConstantMaps();
	 	return fgClassStatusStrings;
	 }
	 
	/**
	 * @see TypeImpl#createNullValue()
	 */
	public Value createNullValue() {
		return null;
	}

	/**
	 * @see ReferenceType#sourceNames(String)
	 */
	public List sourceNames(String arg0) throws AbsentInformationException {
		return null;
	}

	/**
	 * @see ReferenceType#sourcePaths(String)
	 */
	public List sourcePaths(String arg0) throws AbsentInformationException {
		return null;
	}

	/**
	 * @see ReferenceType#sourceDebugExtension()
	 */
	public String sourceDebugExtension() throws AbsentInformationException {
		return null;
	}

	/**
	 * @see ReferenceType#allLineLocations(String, String)
	 */
	public List allLineLocations(String arg0, String arg1)
		throws AbsentInformationException {
		return null;
	}

	/**
	 * @see ReferenceType#locationsOfLine(String, String, int)
	 */
	public List locationsOfLine(String arg0, String arg1, int arg2)
		throws AbsentInformationException {
		return null;
	}

	/**
	 * @see ReferenceType#availableStrata()
	 */
	public List availableStrata() {
		return null;
	}

	/**
	 * @see ReferenceType#defaultStratum()
	 */
	public String defaultStratum() {
		return null;
	}
}
