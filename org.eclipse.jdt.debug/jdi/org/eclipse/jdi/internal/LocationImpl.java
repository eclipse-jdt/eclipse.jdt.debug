package org.eclipse.jdi.internal;/*
 * JDI class Implementation
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */



import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import org.eclipse.jdi.internal.connect.*;
import org.eclipse.jdi.internal.request.*;
import org.eclipse.jdi.internal.event.*;
import org.eclipse.jdi.internal.jdwp.*;
import org.eclipse.jdi.internal.spy.*;
import java.util.*;
import java.io.*;

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
	/** Index of location within the method, not: this value must be treated as UNSIGNED! */
	long fIndex;
	
	/**
	 * Creates new instance.
	 */
	public LocationImpl(VirtualMachineImpl vmImpl, MethodImpl method, long index) {
		super("Location", vmImpl);
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
			throw new ClassCastException("Can't compare location to given object.");
			
		// See if methods are the same, if not return comparison between methods.
		LocationImpl location2 = (LocationImpl)object;
		if (!method().equals(location2.method()))
			return method().compareTo(location2.method());

		// Return comparison between code-indexes.		
		// Code indexes must be treated as unsigned. This matters if you have to compare them.
		if (codeIndex() < 0 || location2.codeIndex() < 0)
			throw new InternalError("Code indexes are assumed to be always positive.");

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
			return "sourcename: " + sourceName() + ", line: " + lineNumber();
		} catch (Exception e) {
			return fDescription;
		}
	}

	/**
	 * Writes JDWP representation.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		fMethod.writeWithReferenceTypeWithTag(target, out);
		target.writeLong(fIndex, "index", out);
	}
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static LocationImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		// Notice that Locations are not stored or cached because they don't 'remember' any information.
		MethodImpl method = MethodImpl.readWithReferenceTypeWithTag(target, in);
		long index = target.readLong("index", in);
		if (method == null)
			return null;
		return new LocationImpl(vmImpl, method, index);
	}
}
