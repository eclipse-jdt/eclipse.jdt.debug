package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import org.eclipse.jdi.internal.connect.*;
import org.eclipse.jdi.internal.request.*;
import org.eclipse.jdi.internal.event.*;
import org.eclipse.jdi.internal.jdwp.*;
import org.eclipse.jdi.internal.spy.*;
import java.util.*;
import java.util.List;
import java.io.*;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class MethodImpl extends TypeComponentImpl implements Method, Locatable {
	/** InvokeOptions Constants. */
	public static final int INVOKE_SINGLE_THREADED_JDWP = 0x01;
	public static final int INVOKE_NONVIRTUAL_JDWP = 0x02;
	
	/** Map with Strings for flag bits. */
	private static Vector fInvokeOptionsVector = null;
	
	/** MethodTypeID that corresponds to this reference. */
	private JdwpMethodID fMethodID;
	
	/** The following are the stored results of JDWP calls. */
	private Vector fVariables = null;
	private long fLowestValidCodeIndex = -1;
	private long fHighestValidCodeIndex = -1;
	private HashMap fCodeIndexToLine = null;
	private Vector fLineToCodeIndexes = null;
	private Vector fAllLineLocations = null;
	private int fArgumentSlotsCount = -1;
	private Vector fArguments = null;
	private Vector fArgumentTypes = null;
	private Vector fArgumentTypeNames = null;
	private Vector fArgumentTypeSignatures = null;
	private byte[] fByteCodes = null;
	
	/**
	 * Creates new MethodImpl.
	 */
	public MethodImpl(VirtualMachineImpl vmImpl, ReferenceTypeImpl declaringType, JdwpMethodID methodID, String name, String signature, int modifierBits) {
		super("Method", vmImpl, declaringType, name, signature, modifierBits);
		fMethodID = methodID;
	}

	/**
	 * Flushes all stored Jdwp results.
	 */
	public void flushStoredJdwpResults() {
		fVariables = null;
		fLowestValidCodeIndex = -1;
		fHighestValidCodeIndex = -1;
		fCodeIndexToLine = null;
		fLineToCodeIndexes = null;
		fAllLineLocations = null;
		fArgumentSlotsCount = -1;
		fArguments = null;
		fArgumentTypes = null;
		fArgumentTypeNames = null;
		fArgumentTypeSignatures = null;
		fByteCodes = null;
	}
	
	/** 
	 * @return Returns methodID of method.
	 */
	public JdwpMethodID getMethodID() {
		return fMethodID;
	}
	
	/** 
	 * @return Returns map of location to line number.
	 */
	public HashMap codeIndexToLine() throws AbsentInformationException {
		getLineTable();
		return fCodeIndexToLine;
		
	}
	
	/** 
	 * @return Returns map of line number to locations.
	 */
	public Vector lineToCodeIndexes(int line) throws AbsentInformationException {
		getLineTable();
		if (fLineToCodeIndexes.size() <= line)
			return null;
		
		return (Vector)fLineToCodeIndexes.get(line);
	}
	
	/** 
	 * Gets line table from VM.
	 */
	private void getLineTable() throws AbsentInformationException {
		if (isObsolete()) {
			return;
		}
		if (fCodeIndexToLine != null) {
			if (fCodeIndexToLine.isEmpty())
				throw new AbsentInformationException("Got empty line number table for this method.");
			else
				return;
		}

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithReferenceType(this, outData);
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.M_LINE_TABLE, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.ABSENT_INFORMATION:
					throw new AbsentInformationException("No line number information available.");
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
			fLowestValidCodeIndex = readLong("lowest index", replyData);
			fHighestValidCodeIndex = readLong("highest index", replyData);
			int nrOfElements = readInt("elements", replyData);
			fCodeIndexToLine = new HashMap();
			fLineToCodeIndexes = new Vector();
			if (nrOfElements == 0)
				throw new AbsentInformationException("Got empty line number table for this method.");
			for (int i = 0; i < nrOfElements; i++) {
				long lineCodeIndex = readLong("code index", replyData);
				Long lineCodeIndexLong = new Long(lineCodeIndex);
				int lineNr = readInt("line nr", replyData);
				Integer lineNrInt = new Integer(lineNr);
				
				// Add entry to code-index to line mapping.
				fCodeIndexToLine.put(lineCodeIndexLong, lineNrInt);
				
				// Add entry to line to code-index mapping.
				if (fLineToCodeIndexes.size() <= lineNr)
					fLineToCodeIndexes.setSize(lineNr + 1);
				if (fLineToCodeIndexes.get(lineNr) == null)
					fLineToCodeIndexes.set(lineNr, new Vector());
				Vector lineNrEntry = (Vector)fLineToCodeIndexes.get(lineNr);
				lineNrEntry.add(lineCodeIndexLong);
			}
		} catch (IOException e) {
			fCodeIndexToLine = null;
			fLineToCodeIndexes = null;
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}	
	
	/** 
	 * @return Returns the line number that corresponds to the given lineCodeIndex.
	 */
	public int findLineNr(long lineCodeIndex) throws AbsentInformationException {
		if (isObsolete()) {
			return -1;
		}
		getLineTable();
		if (lineCodeIndex > fHighestValidCodeIndex)
			throw new InvalidCodeIndexException ("Invalid code index of a location given.");

		Long lineCodeIndexObj;
		Integer lineNrObj;
		// Search for the line where this code index is located.
		do {
			lineCodeIndexObj = new Long(lineCodeIndex);
			lineNrObj = (Integer)codeIndexToLine().get(lineCodeIndexObj);
		} while (lineNrObj == null && --lineCodeIndex >= fLowestValidCodeIndex);
		if (lineNrObj == null)
			throw new InvalidCodeIndexException ("Invalid code index of a location given.");
		return lineNrObj.intValue();
	}

	/** 
	 * @return Returns the beginning Location objects for each executable source line in this method.
	 */
	public List allLineLocations() throws AbsentInformationException {
		if (fAllLineLocations != null)
			return fAllLineLocations;
			
		Iterator locations = codeIndexToLine().keySet().iterator();
		Vector result = new Vector();
		while (locations.hasNext()) {
			Long lindeCodeIndex = (Long)locations.next();
			result.add(new LocationImpl(virtualMachineImpl(), this, lindeCodeIndex.longValue()));
		}
		fAllLineLocations = result;
		return fAllLineLocations;
	}
	
	/** 
	 * @return Returns a list containing each LocalVariable that is declared as an argument of this method.
	 */
	public List arguments() throws AbsentInformationException {
		if (fArguments != null)
			return fArguments;
		
		Vector result = new Vector();	
		Iterator iter = variables().iterator();
		while (iter.hasNext()) {
			LocalVariableImpl var = (LocalVariableImpl)iter.next();
			if (var.isArgument())
				result.add(var);
		}
		fArguments = result;
		return fArguments;
	}
	
	/** 
	 * @return Returns a text representation of all declared argument types of this method. 
	 */
	public List argumentTypeNames() {
		if (fArgumentTypeNames != null)
			return fArgumentTypeNames;
		
		// Get typenames from method signatures.
		Vector result = new Vector();
		Iterator iter = argumentTypeSignatures().iterator();
		while (iter.hasNext()) {
			String name = TypeImpl.signatureToName((String)iter.next());
			result.add(name);
		}
		
		fArgumentTypeNames = result;
		return fArgumentTypeNames;
	}


	/** 
	 * @return Returns a signatures of all declared argument types of this method. 
	 */
	private List argumentTypeSignatures() {
		if (fArgumentTypeSignatures != null)
			return fArgumentTypeSignatures;
		
		Vector result = new Vector();
		
		int index = 1;	// Start position is just after the starting brace.
		int endIndex = signature().lastIndexOf(')') - 1;	// End position is just before ending brace.
		
		while (index <= endIndex) {
			int typeLen = TypeImpl.signatureTypeStringLength(signature(), index);
			result.add(signature().substring(index, index + typeLen));
			index += typeLen;
		}
		fArgumentTypeSignatures = result;
		return fArgumentTypeSignatures;
	}

	/** 
	 * @return Returns the list containing the type of each argument. 
	 */
	public List argumentTypes() throws ClassNotLoadedException {
		if (fArgumentTypes != null)
			return fArgumentTypes;

		Vector result = new Vector();
		Iterator iter = argumentTypeSignatures().iterator();
		while (iter.hasNext()) {
			String argumentTypeSignature = (String)iter.next();
			result.add(TypeImpl.create(virtualMachineImpl(), argumentTypeSignature, declaringType().classLoader()));
		}
		fArgumentTypes = result;
		return fArgumentTypes;
	}

	/** 
	 * @return Returns an array containing the bytecodes for this method. 
	 */
	public byte bytecodes()[] {
		if (fByteCodes != null)
			return fByteCodes;

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithReferenceType(this, outData);
			
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.M_BYTECODES, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
			int length = readInt("length", replyData);
			fByteCodes = readByteArray(length, "bytecodes", replyData);
			return fByteCodes;
		} catch (IOException e) {
			fByteCodes = null;
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
		return fMethodID.hashCode();
	}
	
	/**
	 * @return Returns true if two mirrors refer to the same entity in the target VM.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		return object != null
			&& object.getClass().equals(this.getClass())
			&& fMethodID.equals(((MethodImpl)object).fMethodID)
			&& referenceTypeImpl().equals(((MethodImpl)object).referenceTypeImpl());
	}

	/**
	 * @return Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Object object) {
		if (object == null || !object.getClass().equals(this.getClass()))
			throw new ClassCastException("Can't compare method to given object.");
		
		// See if declaring types are the same, if not return comparison between declaring types.
		Method type2 = (Method)object;
		if (!declaringType().equals(type2.declaringType()))
			return declaringType().compareTo(type2.declaringType());
		
		// Return comparison of position within declaring type.
		int index1 = declaringType().methods().indexOf(this);
		int index2 = type2.declaringType().methods().indexOf(type2);
		if (index1 < index2)
			return -1;
		else if (index1 > index2)
			return 1;
		else return 0;
	}
	
	/** 
	 * @return Returns true if method is abstract.
	 */
	public boolean isAbstract() {
		return (fModifierBits & MODIFIER_ACC_ABSTRACT) != 0;
	}
	
	/** 
	 * @return Returns true if method is constructor.
	 */
	public boolean isConstructor() {
		return name().equals("<init>");
	}
	
	/** 
	 * @return Returns true if method is native.
	 */
	public boolean isNative() {
		return (fModifierBits & MODIFIER_ACC_NATIVE) != 0;
	}
		
	/** 
	 * @return Returns true if method is a static initializer.
	 */
	public boolean isStaticInitializer() {
		return name().equals("<clinit>");
	}
	
	/** 
	 * @return Returns true if method is synchronized.
	 */
	public boolean isSynchronized() {
		return (fModifierBits & MODIFIER_ACC_SYNCHRONIZED) != 0;
	}
	
	/**
	 * @return Returns a Location for the given code index.
	 */
	public Location locationOfCodeIndex(long index) {
		try {
			Integer lineNrInt = (Integer)codeIndexToLine().get(new Long(index));
			if (lineNrInt == null)
				throw new InvalidCodeIndexException();
		} catch (AbsentInformationException e ) {
		}
		return new LocationImpl(virtualMachineImpl(), this, index);
	}
	
	/**
	 * @return Returns a list containing each Location that maps to the given line. 
	 */
	public List locationsOfLine(int line) throws AbsentInformationException, InvalidLineNumberException {
		Vector indexes = lineToCodeIndexes(line);
		if (indexes == null)
			throw new InvalidLineNumberException("No executable code at line " + line + ".");
		
		Iterator codeIndexes = indexes.iterator();
		Vector locations = new Vector();
		while (codeIndexes.hasNext()) {
			long codeIndex = ((Long)codeIndexes.next()).longValue();
			locations.add(new LocationImpl(virtualMachineImpl(), this, codeIndex));
		}
		return locations;
	}
	
	/**
	 * @return Returns the return type of the this Method. 
	 */
	public Type returnType() throws ClassNotLoadedException {
		int startIndex = signature().lastIndexOf(')') + 1;	// Signature position is just after ending brace.
		return TypeImpl.create(virtualMachineImpl(), signature().substring(startIndex), declaringType().classLoader());
	}

	/**
	 * @return Returns a text representation of the declared return type of this method.
	 */
	public String returnTypeName() {
		int startIndex = signature().lastIndexOf(')') + 1;	// Signature position is just after ending brace.
		return TypeImpl.signatureToName(signature().substring(startIndex));
	}	
	
	/**
	 * @return Returns a list containing each LocalVariable declared in this method.
	 */
	public List variables() throws AbsentInformationException {
		if (fVariables != null)
			return fVariables;

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithReferenceType(this, outData);
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.M_VARIABLE_TABLE, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.ABSENT_INFORMATION:
					throw new AbsentInformationException("No local variable information available.");
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
			fArgumentSlotsCount = readInt("arg count", replyData);
			int nrOfElements = readInt("elements", replyData);
			fVariables = new Vector(nrOfElements);
			for (int i = 0; i < nrOfElements; i++) {
				long codeIndex = readLong("code index", replyData);
				String name = readString("name", replyData);
				String signature = readString("signature", replyData);
				int length = readInt("length", replyData);
				int slot = readInt("slot", replyData);
				boolean isArgument = slot < fArgumentSlotsCount;

				// Note that for static methods, the first variable will be the this reference.
				if (isStatic() || i > 0) {
					LocalVariableImpl localVar = new LocalVariableImpl(virtualMachineImpl(), this, codeIndex, name, signature, length, slot, isArgument);
					fVariables.add(localVar);
				}
			}
			return fVariables;
		} catch (IOException e) {
			fArgumentSlotsCount = -1;
			fVariables = null;
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * @return Returns a list containing each LocalVariable of a given name in this method.
	 */
	public List variablesByName(String name) throws AbsentInformationException {
		Iterator iter = variables().iterator();
		Vector result = new Vector();
		while (iter.hasNext()) {
			LocalVariableImpl var = (LocalVariableImpl)iter.next();
			if (var.name().equals(name))
				result.add(var);
		}
		return result;
	}

	/**
	 * @return Returns the Location of this mirror, if there is executable Java language code associated with it.
	 */
	public Location location() {
		// First retrieve line code table.
   		try {
			getLineTable();
		} catch (AbsentInformationException e) {
			return null;
		}

		// Return location with Lowest Valid Code Index.
		return new LocationImpl(virtualMachineImpl(), this, fLowestValidCodeIndex);
	}
	
	/**
	 * @return Returns modifier bits of method.
	 */
	public int modifierBits() {
		return fModifierBits;
	}
	
	/**
	 * Writes JDWP representation.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		fMethodID.write(out);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("method", fMethodID.value());
	}
	
	/**
	 * Writes JDWP representation, including ReferenceType.
	 */
	public void writeWithReferenceType(MirrorImpl target, DataOutputStream out) throws IOException {
		referenceTypeImpl().write(target, out);
		write(target, out);
	}

	/**
	 * Writes JDWP representation, including ReferenceType with Tag.
	 */
	public void writeWithReferenceTypeWithTag(MirrorImpl target, DataOutputStream out) throws IOException {
		referenceTypeImpl().writeWithTag(target, out);
		write(target, out);
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static MethodImpl readWithReferenceTypeWithTag(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
	  	// See Location.
		ReferenceTypeImpl referenceType = ReferenceTypeImpl.readWithTypeTag(target, in);
		if (referenceType == null)
			return null;

		JdwpMethodID ID = new JdwpMethodID(vmImpl);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("method", ID.value());

		ID.read(in);
		if (ID.isNull())
			return null;
			
		// The method must be part of a known reference type.
		MethodImpl method = referenceType.findMethod(ID);
		if (method == null)
			throw new InternalError("Got MethodID of ReferenceType that is not a member of the ReferenceType.");
		return method;
	}
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static MethodImpl readWithNameSignatureModifiers(ReferenceTypeImpl target, ReferenceTypeImpl referenceType, DataInputStream in)  throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpMethodID ID = new JdwpMethodID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("method", ID.value());

		if (ID.isNull())
			return null;
		String name = target.readString("name", in);
		String signature = target.readString("signature", in);
		int modifierBits = target.readInt("modifiers", AccessibleImpl.modifierVector(), in);

		MethodImpl mirror = new MethodImpl(vmImpl, referenceType, ID, name, signature, modifierBits);
		return mirror;
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fInvokeOptionsVector != null)
			return;
		
		java.lang.reflect.Field[] fields = MethodImpl.class.getDeclaredFields();
		fInvokeOptionsVector = new Vector();
		fInvokeOptionsVector.setSize(32); // Int

		for (int i = 0; i < fields.length; i++) {
			java.lang.reflect.Field field = fields[i];
			if ((field.getModifiers() & java.lang.reflect.Modifier.PUBLIC) == 0 || (field.getModifiers() & java.lang.reflect.Modifier.STATIC) == 0 || (field.getModifiers() & java.lang.reflect.Modifier.FINAL) == 0)
				continue;
				
			try {
				String name = field.getName();
				int value = field.getInt(null);

				if (name.startsWith("INVOKE_")) {
					//fInvokeOptionsMap.put(intValue, name);
					for (int j = 0; j < fInvokeOptionsVector.size(); j++) {
						if ((1 << j & value) != 0) {
							fInvokeOptionsVector.set(j, name);
							break;
						}
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
	 public static Vector invokeOptionsVector() {
	 	getConstantMaps();
	 	return fInvokeOptionsVector;
	 }
	/**
	 * @see Method#isObsolete()
	 * 
	 * The JDK 1.4.0 specification states that obsolete methods
	 * are given an ID of zero. It also states that when a method
	 * is redefined, the new method gets the ID of the old method.
	 * Thus, the JDWP query for isObsolete will never return true
	 * for a non-zero method ID. The query is therefore not needed.
	 */
	public boolean isObsolete() {
		return (fMethodID.value() == 0);
	}

	/*
	 * @see Method#allLineLocations(String, String)
	 */
	public List allLineLocations(String stratum, String sourceName) throws AbsentInformationException {
		return new ArrayList(0);
	}

	/*
	 * @see Method#locationsOfLine(String, String, int)
	 */
	public List locationsOfLine(String stratum, String sourceName, int lineNumber) throws AbsentInformationException {
		return new ArrayList(0);
	}

}
