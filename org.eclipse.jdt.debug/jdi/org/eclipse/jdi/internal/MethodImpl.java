package org.eclipse.jdi.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpMethodID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidCodeIndexException;
import com.sun.jdi.InvalidLineNumberException;
import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.Type;


/**
 * Implementation of com.sun.jdi.Method.
 */
public class MethodImpl extends TypeComponentImpl implements Method, Locatable {
	/** InvokeOptions Constants. */
	public static final int INVOKE_SINGLE_THREADED_JDWP = 0x01;
	public static final int INVOKE_NONVIRTUAL_JDWP = 0x02;
	
	/** Map with Strings for flag bits. */
	private static String[] fgInvokeOptions = null;
	
	/** MethodTypeID that corresponds to this reference. */
	private JdwpMethodID fMethodID;
	
	/** The following are the stored results of JDWP calls. */
	private List fVariables = null;
	private long fLowestValidCodeIndex = -1;
	private long fHighestValidCodeIndex = -1;
	private Map fCodeIndexToLine = null;
	private Map fLineToCodeIndexes = null;
	private List fAllLineLocations = null;
	private int fArgumentSlotsCount = -1;
	private List fArguments = null;
	private List fArgumentTypes = null;
	private List fArgumentTypeNames = null;
	private List fArgumentTypeSignatures = null;
	private byte[] fByteCodes = null;
	
	private String fReturnTypeName= null;
	
	/**
	 * Creates new MethodImpl.
	 */
	public MethodImpl(VirtualMachineImpl vmImpl, ReferenceTypeImpl declaringType, JdwpMethodID methodID, String name, String signature, int modifierBits) {
		super("Method", vmImpl, declaringType, name, signature, modifierBits); //$NON-NLS-1$
		fMethodID = methodID;
	}

	/**
	 * Flushes all stored Jdwp results.
	 */
	protected void flushStoredJdwpResults() {
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
	protected JdwpMethodID getMethodID() {
		return fMethodID;
	}
	
	/** 
	 * @return Returns map of location to line number.
	 */
	protected Map codeIndexToLine() throws AbsentInformationException {
		if (isAbstract()) {
			return Collections.EMPTY_MAP;
		}
		getLineTable();
		return fCodeIndexToLine;
	}
	
	/** 
	 * @return Returns map of line number to locations.
	 */
	protected List lineToCodeIndexes(int line) throws AbsentInformationException {
		if (isAbstract() || isNative()) {
			return null;
		}
		getLineTable();
		
		return (List)fLineToCodeIndexes.get(new Integer(line));
	}
	
	/** 
	 * Gets line table from VM.
	 */
	private void getLineTable() throws AbsentInformationException {
		if (isObsolete()) {
			return;
		}
		if (fCodeIndexToLine != null) {
			if (fCodeIndexToLine.isEmpty()) {
				throw new AbsentInformationException(JDIMessages.getString("MethodImpl.Got_empty_line_number_table_for_this_method_1")); //$NON-NLS-1$
			} else {
				return;
			}
		}

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithReferenceType(this, outData);
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.M_LINE_TABLE, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.ABSENT_INFORMATION:
					throw new AbsentInformationException(JDIMessages.getString("MethodImpl.No_line_number_information_available_2")); //$NON-NLS-1$
				case JdwpReplyPacket.NATIVE_METHOD:
					throw new AbsentInformationException(JDIMessages.getString("MethodImpl.No_line_number_information_available_2")); //$NON-NLS-1$
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
			fLowestValidCodeIndex = readLong("lowest index", replyData); //$NON-NLS-1$
			fHighestValidCodeIndex = readLong("highest index", replyData); //$NON-NLS-1$
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			fCodeIndexToLine = new HashMap();
			fLineToCodeIndexes = new HashMap();
			if (nrOfElements == 0) {
				throw new AbsentInformationException(JDIMessages.getString("MethodImpl.Got_empty_line_number_table_for_this_method_3")); //$NON-NLS-1$
			}
			for (int i = 0; i < nrOfElements; i++) {
				long lineCodeIndex = readLong("code index", replyData); //$NON-NLS-1$
				Long lineCodeIndexLong = new Long(lineCodeIndex);
				int lineNr = readInt("line nr", replyData); //$NON-NLS-1$
				Integer lineNrInt = new Integer(lineNr);
				
				// Add entry to code-index to line mapping.
				fCodeIndexToLine.put(lineCodeIndexLong, lineNrInt);
				
				if (fLineToCodeIndexes.get(lineNrInt) == null) {
					fLineToCodeIndexes.put(lineNrInt, new ArrayList());
				}
				List lineNrEntry = (List)fLineToCodeIndexes.get(lineNrInt);
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
	protected int findLineNr(long lineCodeIndex) throws AbsentInformationException {
		if (isAbstract() || isNative() || isObsolete()) {
			return -1;
		}
		getLineTable();
		if (lineCodeIndex > fHighestValidCodeIndex) {
			throw new InvalidCodeIndexException (JDIMessages.getString("MethodImpl.Invalid_code_index_of_a_location_given_4")); //$NON-NLS-1$
		}

		Long lineCodeIndexObj;
		Integer lineNrObj;
		long index= lineCodeIndex;
		// Search for the line where this code index is located.
		do {
			lineCodeIndexObj = new Long(index);
			lineNrObj = (Integer)codeIndexToLine().get(lineCodeIndexObj);
		} while (lineNrObj == null && --index >= fLowestValidCodeIndex);
		if (lineNrObj == null) {
			if (lineCodeIndex >= fLowestValidCodeIndex) {
				index= lineCodeIndex;
				do {
					lineCodeIndexObj = new Long(index);
					lineNrObj = (Integer)codeIndexToLine().get(lineCodeIndexObj);
				} while (lineNrObj == null && ++index <= fHighestValidCodeIndex);
				if (lineNrObj != null) {
					return lineNrObj.intValue();
				}
			}
			throw new InvalidCodeIndexException (JDIMessages.getString("MethodImpl.Invalid_code_index_of_a_location_given_4")); //$NON-NLS-1$
		}
		return lineNrObj.intValue();
	}

	
	/**
	 * @see com.sun.jdi.Method#allLineLocations()
	 */
	public List allLineLocations() throws AbsentInformationException {
		if (fAllLineLocations != null) {
			return fAllLineLocations;
		}
		
		if (isAbstract() || isNative()) {
			fAllLineLocations= Collections.EMPTY_LIST;
			return fAllLineLocations;
		}
			
		Iterator locations = codeIndexToLine().keySet().iterator();
		List result = new ArrayList();
		while (locations.hasNext()) {
			Long lineCodeIndex = (Long)locations.next();
			result.add(new LocationImpl(virtualMachineImpl(), this, lineCodeIndex.longValue()));
		}
		fAllLineLocations = result;
		return fAllLineLocations;
	}
	
	/**
	 * @see com.sun.jdi.Method#arguments()
	 */
	public List arguments() throws AbsentInformationException {
		if (isNative()) {
			throw new NativeMethodException(JDIMessages.getString("MethodImpl.Attempt_to_retrieve_argument_information_from_a_native_method_1")); //$NON-NLS-1$
		}
		if (fArguments != null) {
			return fArguments;
		}
		
		List result = new ArrayList();	
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
		if (fArgumentTypeNames != null) {
			return fArgumentTypeNames;
		}
		
		// Get typenames from method signatures.
		List result = new ArrayList();
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
		if (fArgumentTypeSignatures != null) {
			return fArgumentTypeSignatures;
		}
		
		List result = new ArrayList();
		
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
		if (fArgumentTypes != null) {
			return fArgumentTypes;
		}

		List result = new ArrayList();
		Iterator iter = argumentTypeSignatures().iterator();
		ClassLoaderReference classLoaderRef= declaringType().classLoader();
		VirtualMachineImpl vm= virtualMachineImpl();
		while (iter.hasNext()) {
			String argumentTypeSignature = (String)iter.next();
			result.add(TypeImpl.create(vm, argumentTypeSignature, classLoaderRef));
		}
		fArgumentTypes = result;
		return fArgumentTypes;
	}

	/** 
	 * @return Returns an array containing the bytecodes for this method. 
	 */
	public byte bytecodes()[] {
		if (fByteCodes != null) {
			return fByteCodes;
		}

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithReferenceType(this, outData);
			
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.M_BYTECODES, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
			int length = readInt("length", replyData); //$NON-NLS-1$
			fByteCodes = readByteArray(length, "bytecodes", replyData); //$NON-NLS-1$
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
			throw new ClassCastException(JDIMessages.getString("MethodImpl.Can__t_compare_method_to_given_object_6")); //$NON-NLS-1$
		
		// See if declaring types are the same, if not return comparison between declaring types.
		Method type2 = (Method)object;
		if (!declaringType().equals(type2.declaringType()))
			return declaringType().compareTo(type2.declaringType());
		
		// Return comparison of position within declaring type.
		int index1 = declaringType().methods().indexOf(this);
		int index2 = type2.declaringType().methods().indexOf(type2);
		if (index1 < index2) {
			return -1;
		} else if (index1 > index2) {
			return 1;
		} else {
			return 0;
		}
	}
	
	
	/**
	 * @see com.sun.jdi.Method#isAbstract()
	 */
	public boolean isAbstract() {
		return (fModifierBits & MODIFIER_ACC_ABSTRACT) != 0;
	}
	
	
	/**
	 * @see com.sun.jdi.Method#isConstructor()
	 */
	public boolean isConstructor() {
		return name().equals("<init>"); //$NON-NLS-1$
	}
	
	/**
	 * @see com.sun.jdi.Method#isNative()
	 */
	public boolean isNative() {
		return (fModifierBits & MODIFIER_ACC_NATIVE) != 0;
	}
		
	/**
	 * @see com.sun.jdi.Method#isStaticInitializer()
	 */
	public boolean isStaticInitializer() {
		return name().equals("<clinit>"); //$NON-NLS-1$
	}
	
	/**
	 * @see com.sun.jdi.Method#isSynchronized()
	 */
	public boolean isSynchronized() {
		return (fModifierBits & MODIFIER_ACC_SYNCHRONIZED) != 0;
	}
	
	/**
	 * @see com.sun.jdi.Method#locationOfCodeIndex(long)
	 */
	public Location locationOfCodeIndex(long index) {
		if (isAbstract() || isNative()) {
			return null;
		}
		try {
			Integer lineNrInt = (Integer)codeIndexToLine().get(new Long(index));
			if (lineNrInt == null) {
				throw new InvalidCodeIndexException(MessageFormat.format(JDIMessages.getString("MethodImpl.No_valid_location_at_the_specified_code_index_{0}_2"), new Object[]{Long.toString(index)})); //$NON-NLS-1$
			}
		} catch (AbsentInformationException e ) {
		}
		return new LocationImpl(virtualMachineImpl(), this, index);
	}
	
	/**
	 * @see com.sun.jdi.Method#locationsOfLine(int)
	 */
	public List locationsOfLine(int line) throws AbsentInformationException, InvalidLineNumberException {
		if (isAbstract() || isNative()) {
			return Collections.EMPTY_LIST;
		}
		List indexes = lineToCodeIndexes(line);
		if (indexes == null) {
			throw new InvalidLineNumberException(MessageFormat.format(JDIMessages.getString("MethodImpl.No_executable_code_at_line__7"), new String[]{Integer.toString(line)})); //$NON-NLS-1$
		}
		
		Iterator codeIndexes = indexes.iterator();
		List locations = new ArrayList();
		while (codeIndexes.hasNext()) {
			long codeIndex = ((Long)codeIndexes.next()).longValue();
			locations.add(new LocationImpl(virtualMachineImpl(), this, codeIndex));
		}
		return locations;
	}
	
	/**
	 * @see com.sun.jdi.Method#returnType()
	 */
	public Type returnType() throws ClassNotLoadedException {
		int startIndex = signature().lastIndexOf(')') + 1;	// Signature position is just after ending brace.
		return TypeImpl.create(virtualMachineImpl(), signature().substring(startIndex), declaringType().classLoader());
	}

	
	/**
	 * @see com.sun.jdi.Method#returnTypeName()
	 */
	public String returnTypeName() {
		if (fReturnTypeName != null) {
			return fReturnTypeName;
		}
		int startIndex = signature().lastIndexOf(')') + 1;	// Signature position is just after ending brace.
		fReturnTypeName= TypeImpl.signatureToName(signature().substring(startIndex));
		return fReturnTypeName;
	}	
	
	
	/**
	 * @see com.sun.jdi.Method#variables()
	 */
	public List variables() throws AbsentInformationException {
		if (isNative()) {
			throw new NativeMethodException(JDIMessages.getString("MethodImpl.Attempt_to_retrieve_variable_information_from_a_native_method_3")); //$NON-NLS-1$
		}
		
		if (fVariables != null) {
			return fVariables;
		}

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithReferenceType(this, outData);
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.M_VARIABLE_TABLE, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.ABSENT_INFORMATION:
					throw new AbsentInformationException(JDIMessages.getString("MethodImpl.No_local_variable_information_available_9")); //$NON-NLS-1$
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
			fArgumentSlotsCount = readInt("arg count", replyData); //$NON-NLS-1$
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			fVariables = new ArrayList(nrOfElements);
			for (int i = 0; i < nrOfElements; i++) {
				long codeIndex = readLong("code index", replyData); //$NON-NLS-1$
				String name = readString("name", replyData); //$NON-NLS-1$
				String signature = readString("signature", replyData); //$NON-NLS-1$
				int length = readInt("length", replyData); //$NON-NLS-1$
				int slot = readInt("slot", replyData); //$NON-NLS-1$
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
	 * @see com.sun.jdi.Method#variablesByName(String)
	 */
	public List variablesByName(String name) throws AbsentInformationException {
		Iterator iter = variables().iterator();
		List result = new ArrayList();
		while (iter.hasNext()) {
			LocalVariableImpl var = (LocalVariableImpl)iter.next();
			if (var.name().equals(name)) {
				result.add(var);
			}
		}
		return result;
	}

	/**
	 * @see com.sun.jdi.Locatable#location()
	 */
	public Location location() {
		if (isAbstract()) {
			return null;
		}
		if (isNative()) {
			return new LocationImpl(virtualMachineImpl(), this, -1);
		}
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
	 * Writes JDWP representation.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		fMethodID.write(out);
		if (target.fVerboseWriter != null) {
			target.fVerboseWriter.println("method", fMethodID.value()); //$NON-NLS-1$
		}
	}
	
	/**
	 * Writes JDWP representation, including ReferenceType.
	 */
	protected void writeWithReferenceType(MirrorImpl target, DataOutputStream out) throws IOException {
		referenceTypeImpl().write(target, out);
		write(target, out);
	}

	/**
	 * Writes JDWP representation, including ReferenceType with Tag.
	 */
	protected void writeWithReferenceTypeWithTag(MirrorImpl target, DataOutputStream out) throws IOException {
		referenceTypeImpl().writeWithTag(target, out);
		write(target, out);
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	protected static MethodImpl readWithReferenceTypeWithTag(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
	  	// See Location.
		ReferenceTypeImpl referenceType = ReferenceTypeImpl.readWithTypeTag(target, in);
		if (referenceType == null)
			return null;

		JdwpMethodID ID = new JdwpMethodID(vmImpl);
		if (target.fVerboseWriter != null) {
			target.fVerboseWriter.println("method", ID.value()); //$NON-NLS-1$
		}

		ID.read(in);
		if (ID.isNull()) {
			return null;
		}
			
		// The method must be part of a known reference type.
		MethodImpl method = referenceType.findMethod(ID);
		if (method == null) {
			throw new InternalError(JDIMessages.getString("MethodImpl.Got_MethodID_of_ReferenceType_that_is_not_a_member_of_the_ReferenceType_10")); //$NON-NLS-1$
		}
		return method;
	}
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	protected static MethodImpl readWithNameSignatureModifiers(ReferenceTypeImpl target, ReferenceTypeImpl referenceType, DataInputStream in)  throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpMethodID ID = new JdwpMethodID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null) {
			target.fVerboseWriter.println("method", ID.value()); //$NON-NLS-1$
		}

		if (ID.isNull()) {
			return null;
		}
		String name = target.readString("name", in); //$NON-NLS-1$
		String signature = target.readString("signature", in); //$NON-NLS-1$
		int modifierBits = target.readInt("modifiers", AccessibleImpl.getModifierStrings(), in); //$NON-NLS-1$

		MethodImpl mirror = new MethodImpl(vmImpl, referenceType, ID, name, signature, modifierBits);
		return mirror;
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fgInvokeOptions != null) {
			return;
		}
		
		Field[] fields = MethodImpl.class.getDeclaredFields();
		fgInvokeOptions = new String[32];

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if ((field.getModifiers() & Modifier.PUBLIC) == 0 || (field.getModifiers() & java.lang.reflect.Modifier.STATIC) == 0 || (field.getModifiers() & Modifier.FINAL) == 0) {
				continue;
			}
				
			try {
				String name = field.getName();

				if (name.startsWith("INVOKE_")) { //$NON-NLS-1$
					int value = field.getInt(null);
					for (int j = 0; j < fgInvokeOptions.length; j++) {
						if ((1 << j & value) != 0) {
							fgInvokeOptions[j]= name;
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
	 protected static String[] getInvokeOptions() {
	 	getConstantMaps();
	 	return fgInvokeOptions;
	 }
	/**
	 * @see Method#isObsolete()
	 * 
	 * The JDK 1.4.0 specification states that obsolete methods
	 * are given an ID of zero. It also states that when a method
	 * is redefined, the new method gets the ID of the old method.
	 * Thus, the JDWP query for isObsolete on JDK 1.4 will never return true
	 * for a non-zero method ID. The query is therefore not needed
	 */
	public boolean isObsolete() {
		if (virtualMachineImpl().isJdwpVersionGreaterOrEqual(1, 4)) {
			return fMethodID.value() == 0;
		}
		return false;
	}

	/**
	 * @see Method#allLineLocations(String, String)
	 */
	public List allLineLocations(String stratum, String sourceName) throws AbsentInformationException {
		return new ArrayList(0);
	}

	/**
	 * @see Method#locationsOfLine(String, String, int)
	 */
	public List locationsOfLine(String stratum, String sourceName, int lineNumber) throws AbsentInformationException {
		return new ArrayList(0);
	}
}
