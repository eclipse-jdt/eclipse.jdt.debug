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


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ReferenceType;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class LocationImpl extends MirrorImpl implements Location {
	/** Line nr used if line numbers are not available. */
	public static final int LINE_NR_NOT_AVAILABLE = -1;
	
	/** Method that holds the location. */
	MethodImpl fMethod;
	/** Index of location within the method, note: this value must be treated as UNSIGNED! */
	long fIndex;
	
	/**
	 * Creates new instance.
	 */
	public LocationImpl(VirtualMachineImpl vmImpl, MethodImpl method, long index) {
		super("Location", vmImpl); //$NON-NLS-1$
		fMethod = method;
		fIndex = index;
	}
	
	/**
	 * @return Returns the code position within this location's method.
	 */
	public long codeIndex() {
		return fIndex;
	}
	
	/**
	 * @return Returns the type to which this Location belongs. 
	 */
	public ReferenceType declaringType() {
		return fMethod.declaringType();
	}
	
	/** 
	 * @return Returns the hash code value.
	 */
	public int hashCode() {
		return fMethod.hashCode() + (int)fIndex;
	}
	
	/**
	 * @return Returns true if two mirrors refer to the same entity in the target VM.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (object != null && object.getClass().equals(this.getClass())) {
			LocationImpl loc = (LocationImpl)object;
			return fMethod.equals(loc.fMethod) && fIndex == loc.fIndex;
		}
		return false;
	}
	
	/**
	 * @return Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Object object) {
		if (object == null || !object.getClass().equals(this.getClass()))
			throw new ClassCastException(JDIMessages.getString("LocationImpl.Can__t_compare_location_to_given_object_1")); //$NON-NLS-1$
			
		// See if methods are the same, if not return comparison between methods.
		LocationImpl location2 = (LocationImpl)object;
		if (!method().equals(location2.method()))
			return method().compareTo(location2.method());

		// Return comparison between code-indexes.		
		// Code indexes must be treated as unsigned. This matters if you have to compare them.
		if (codeIndex() < 0 || location2.codeIndex() < 0)
			throw new InternalError(JDIMessages.getString("LocationImpl.Code_indexes_are_assumed_to_be_always_positive_2")); //$NON-NLS-1$

		if (codeIndex() < location2.codeIndex())
			return -1;
		else if (codeIndex() > location2.codeIndex())
			return 1;
		else return 0;
	}
	
	/** 
	 * @return Returns an int specifying the line in the source, return -1 if the information is not available.
	 */
	public int lineNumber() {
		try {
			return fMethod.findLineNr(fIndex);
		} catch (NativeMethodException e) {	// Occurs in SUN VM.
			return LINE_NR_NOT_AVAILABLE;
		} catch (AbsentInformationException e) {
			return LINE_NR_NOT_AVAILABLE;
		}
	}
	
	/** 
	 * @return Returns the Method if this location is in a method.
	 */
	public Method method() {
		return fMethod;
	}
	
	/** 
	 * @return a string specifying the source.
	 */
	public String sourceName() throws AbsentInformationException {
		return fMethod.declaringType().sourceName();
	}
	
	/**
	 * @return Returns description of Mirror object.
	 */
	public String toString() {
		try {
			return MessageFormat.format(JDIMessages.getString("LocationImpl.sourcename__{0},_line__{1}_3"), new String[]{sourceName(), Integer.toString(lineNumber())}); //$NON-NLS-1$
		} catch (Exception e) {
			return fDescription;
		}
	}

	/**
	 * Writes JDWP representation.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		fMethod.writeWithReferenceTypeWithTag(target, out);
		target.writeLong(fIndex, "index", out); //$NON-NLS-1$
	}
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static LocationImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		// Notice that Locations are not stored or cached because they don't 'remember' any information.
		MethodImpl method = MethodImpl.readWithReferenceTypeWithTag(target, in);
		long index = target.readLong("index", in); //$NON-NLS-1$
		if (method == null) {
			return null;
		}
		return new LocationImpl(vmImpl, method, index);
	}
	
	/**
	 * @see Location#lineNumber(String)
	 */
	public int lineNumber(String stratum) {
		return 0;
	}

	/**
	 * @see Location#sourceName(String)
	 */
	public String sourceName(String stratum) throws AbsentInformationException {
		return null;
	}

	/**
	 * @see Location#sourcePath(String)
	 */
	public String sourcePath(String stratum) throws AbsentInformationException {
		return null;
	}

	/**
	 * @see Location#sourcePath()
	 */
	public String sourcePath() throws AbsentInformationException {
		return null;
	}

}
