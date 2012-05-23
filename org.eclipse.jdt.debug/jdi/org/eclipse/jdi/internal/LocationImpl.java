/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.osgi.util.NLS;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class LocationImpl extends MirrorImpl implements Location {
	/** Line nr used if line numbers are not available. */
	public static final int LINE_NR_NOT_AVAILABLE = -1;

	/** Method that holds the location. */
	MethodImpl fMethod;
	/**
	 * Index of location within the method, note: this value must be treated as
	 * UNSIGNED!
	 */
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
	@Override
	public int hashCode() {
		return fMethod.hashCode() + (int) fIndex;
	}

	/**
	 * @return Returns true if two mirrors refer to the same entity in the
	 *         target VM.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (object != null && object.getClass().equals(this.getClass())) {
			LocationImpl loc = (LocationImpl) object;
			return fMethod.equals(loc.fMethod) && fIndex == loc.fIndex;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Locatable locatable) {
		if (locatable == null || !locatable.getClass().equals(this.getClass()))
			throw new ClassCastException(
					JDIMessages.LocationImpl_Can__t_compare_location_to_given_object_1);

		// See if methods are the same, if not return comparison between
		// methods.
		LocationImpl location2 = (LocationImpl) locatable;
		if (!method().equals(location2.method()))
			return method().compareTo(location2.method());

		// Return comparison between code-indexes.
		// Code indexes must be treated as unsigned. This matters if you have to
		// compare them.
		if (codeIndex() < 0 || location2.codeIndex() < 0)
			throw new InternalError(
					JDIMessages.LocationImpl_Code_indexes_are_assumed_to_be_always_positive_2);

		if (codeIndex() < location2.codeIndex())
			return -1;
		else if (codeIndex() > location2.codeIndex())
			return 1;
		else
			return 0;
	}

	/**
	 * @return Returns an int specifying the line in the source, return -1 if
	 *         the information is not available.
	 */
	public int lineNumber() {
		return lineNumber(virtualMachine().getDefaultStratum());
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
		return sourceName(virtualMachine().getDefaultStratum());
	}

	/**
	 * @return Returns description of Mirror object.
	 */
	@Override
	public String toString() {
		try {
			return NLS.bind(JDIMessages.LocationImpl_sourcename___0___line___1__3,
							new String[] { sourceName(),
									Integer.toString(lineNumber()) });
		} catch (Exception e) {
			return fDescription;
		}
	}

	/**
	 * Writes JDWP representation.
	 */
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		fMethod.writeWithReferenceTypeWithTag(target, out);
		target.writeLong(fIndex, "index", out); //$NON-NLS-1$
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static LocationImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		// Notice that Locations are not stored or cached because they don't
		// 'remember' any information.
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
		return fMethod.referenceTypeImpl().lineNumber(fIndex, fMethod, stratum);
	}

	/**
	 * @see Location#sourceName(String)
	 */
	public String sourceName(String stratum) throws AbsentInformationException {
		return fMethod.referenceTypeImpl().sourceName(fIndex, fMethod, stratum);
	}

	/**
	 * @see Location#sourcePath(String)
	 */
	public String sourcePath(String stratum) throws AbsentInformationException {
		return fMethod.referenceTypeImpl().sourcePath(fIndex, fMethod, stratum);
	}

	/**
	 * @see Location#sourcePath()
	 */
	public String sourcePath() throws AbsentInformationException {
		return sourcePath(virtualMachine().getDefaultStratum());
	}

}
