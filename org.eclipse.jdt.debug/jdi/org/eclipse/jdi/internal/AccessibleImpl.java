package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Vector;

import com.sun.jdi.Accessible;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class AccessibleImpl extends MirrorImpl implements Accessible {
	/** Modifier bit flag: Is synthetic. */
	public static final int MODIFIER_SYNTHETIC = 0xf0000000;
	/** Modifier bit flag: Is public; may be accessed from outside its package. */
	public static final int MODIFIER_ACC_PUBLIC = 0x0001;
	/** Modifier bit flag: Is private; usable only within the defining class. */
	public static final int MODIFIER_ACC_PRIVATE = 0x0002;
	/** Modifier bit flag: Is protected; may be accessed within subclasses. */
	public static final int MODIFIER_ACC_PROTECTED = 0x0004;
	/** Modifier bit flag: Is static. */
	public static final int MODIFIER_ACC_STATIC = 0x0008;
	/** Modifier bit flag: Is final; no overriding is allowed. */
	public static final int MODIFIER_ACC_FINAL = 0x0010;
	/** Modifier bit flag: Is synchronized; wrap use in monitor lock. */
	public static final int MODIFIER_ACC_SYNCHRONIZED = 0x0020;
	/** Modifier bit flag: Is volitile; cannot be reached. */
	public static final int MODIFIER_ACC_VOLITILE = 0x0040;
	/** Modifier bit flag: Is transient; not written or read by a persistent object manager. */
	public static final int MODIFIER_ACC_TRANSIENT = 0x0080;
	/** Modifier bit flag: Is native; implemented in a language other than Java. */
	public static final int MODIFIER_ACC_NATIVE = 0x0100;
	/** Modifier bit flag: Is abstract; no implementation is provided. */
	public static final int MODIFIER_ACC_ABSTRACT = 0x0400;
	
	/** Mapping of command codes to strings. */
	private static Vector fModifierVector = null;
	
	/**
	 * Creates new instance.
	 */
	public AccessibleImpl(String description, VirtualMachineImpl vmImpl) {
		super(description, vmImpl);
	}

	/** 
	 * @return Returns true if object is package private.
	 */
	public boolean isPackagePrivate() {
		return !(isPrivate() || isPublic() || isProtected());
	}
		
	/** 
	 * @return Returns true if object is private.
	 */
	public boolean isPrivate() {
		return (modifiers() & MODIFIER_ACC_PRIVATE) != 0;
	}
	
	/** 
	 * @return Returns true if object is pubic.
	 */
	public boolean isPublic() {
		return (modifiers() & MODIFIER_ACC_PUBLIC) != 0;
	}
	
	/** 
	 * @return Returns true if object is protected.
	 */
	public boolean isProtected() {
		return (modifiers() & MODIFIER_ACC_PROTECTED) != 0;
	}
	
	/**
	 * @return Returns modifier bits.
	 */
	public abstract int modifiers();

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fModifierVector != null)
			return;
		
		java.lang.reflect.Field[] fields = AccessibleImpl.class.getDeclaredFields();
		fModifierVector = new Vector();
		fModifierVector.setSize(32);	// Integer
		
		for (int i = 0; i < fields.length; i++) {
			java.lang.reflect.Field field = fields[i];
			if ((field.getModifiers() & java.lang.reflect.Modifier.PUBLIC) == 0 || (field.getModifiers() & java.lang.reflect.Modifier.STATIC) == 0 || (field.getModifiers() & java.lang.reflect.Modifier.FINAL) == 0)
				continue;
				
			String name = field.getName();
			if (!name.startsWith("MODIFIER_")) //$NON-NLS-1$
				continue;
				
			name = name.substring(9);
			
			try {
				int value = field.getInt(null);
				
				for (int j = 0; j < fModifierVector.size(); j++) {
					if ((1 << j & value) != 0) {
						fModifierVector.set(j, name);
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
	 public static Vector modifierVector() {
	 	getConstantMaps();
	 	return fModifierVector;
	 }
}
