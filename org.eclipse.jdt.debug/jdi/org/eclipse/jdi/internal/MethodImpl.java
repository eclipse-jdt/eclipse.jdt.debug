/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tim Tromey - update method signature syntax (bug 31507)
 *******************************************************************************/
package org.eclipse.jdi.internal;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpMethodID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
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
	private List<LocalVariable> fVariables = null;
	private long fLowestValidCodeIndex = -1;
	private long fHighestValidCodeIndex = -1;
	private Map<Long, Integer> fCodeIndexToLine = null;
	private Map<Integer, List<Long>> fLineToCodeIndexes = null;
	private Map<String, Map<String, List<Location>>> fStratumAllLineLocations = null;
	private int fArgumentSlotsCount = -1;
	private List<LocalVariable> fArguments = null;
	private List<Type> fArgumentTypes = null;
	private List<String> fArgumentTypeNames = null;
	private List<String> fArgumentTypeSignatures = null;
	private byte[] fByteCodes = null;
	private long[] fCodeIndexTable;
	private int[] fJavaStratumLineNumberTable;

	private String fReturnTypeName = null;

	/**
	 * Creates new MethodImpl.
	 */
	public MethodImpl(VirtualMachineImpl vmImpl,
			ReferenceTypeImpl declaringType, JdwpMethodID methodID,
			String name, String signature, String genericSignature,
			int modifierBits) {
		super(
				"Method", vmImpl, declaringType, name, signature, genericSignature, modifierBits); //$NON-NLS-1$
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
		fStratumAllLineLocations = null;
		fCodeIndexTable = null;
		fJavaStratumLineNumberTable = null;
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
	protected Map<Long, Integer> javaStratumCodeIndexToLine()
			throws AbsentInformationException {
		if (isAbstract()) {
			return Collections.EMPTY_MAP;
		}
		getLineTable();
		return fCodeIndexToLine;
	}

	/**
	 * @return Returns map of line number to locations.
	 */
	protected List<Long> javaStratumLineToCodeIndexes(int line) throws AbsentInformationException {
		if (isAbstract() || isNative()) {
			return null;
		}
		getLineTable();

		return fLineToCodeIndexes.get(new Integer(line));
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
				throw new AbsentInformationException(
						JDIMessages.MethodImpl_Got_empty_line_number_table_for_this_method_1);
			}
			return;
		}

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithReferenceType(this, outData);

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.M_LINE_TABLE, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.ABSENT_INFORMATION:
				throw new AbsentInformationException(
						JDIMessages.MethodImpl_No_line_number_information_available_2);
			case JdwpReplyPacket.NATIVE_METHOD:
				throw new AbsentInformationException(
						JDIMessages.MethodImpl_No_line_number_information_available_2);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			fLowestValidCodeIndex = readLong("lowest index", replyData); //$NON-NLS-1$
			fHighestValidCodeIndex = readLong("highest index", replyData); //$NON-NLS-1$
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			fCodeIndexToLine = new HashMap<Long, Integer>();
			fLineToCodeIndexes = new HashMap<Integer, List<Long>>();
			if (nrOfElements == 0) {
				throw new AbsentInformationException(
						JDIMessages.MethodImpl_Got_empty_line_number_table_for_this_method_3);
			}
			fCodeIndexTable = new long[nrOfElements];
			fJavaStratumLineNumberTable = new int[nrOfElements];
			for (int i = 0; i < nrOfElements; i++) {
				long lineCodeIndex = readLong("code index", replyData); //$NON-NLS-1$
				Long lineCodeIndexLong = new Long(lineCodeIndex);
				int lineNr = readInt("line nr", replyData); //$NON-NLS-1$
				Integer lineNrInt = new Integer(lineNr);

				// Add entry to code-index to line mapping.
				fCodeIndexToLine.put(lineCodeIndexLong, lineNrInt);

				fCodeIndexTable[i] = lineCodeIndex;
				fJavaStratumLineNumberTable[i] = lineNr;

				List<Long> lineNrEntry = fLineToCodeIndexes.get(lineNrInt);
				if (lineNrEntry == null) {
					lineNrEntry = new ArrayList<Long>();
					fLineToCodeIndexes.put(lineNrInt, lineNrEntry);
				}
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
	 * @return Returns the line number that corresponds to the given
	 *         lineCodeIndex.
	 */
	protected int javaStratumLineNumber(long lineCodeIndex)
			throws AbsentInformationException {
		if (isAbstract() || isNative() || isObsolete()) {
			return -1;
		}
		getLineTable();
		if (lineCodeIndex > fHighestValidCodeIndex) {
			throw new AbsentInformationException(JDIMessages.MethodImpl_Invalid_code_index_of_a_location_given_4);
		}

		Long lineCodeIndexObj;
		Integer lineNrObj;
		long index = lineCodeIndex;
		// Search for the line where this code index is located.
		do {
			lineCodeIndexObj = new Long(index);
			lineNrObj = javaStratumCodeIndexToLine().get(lineCodeIndexObj);
		} while (lineNrObj == null && --index >= fLowestValidCodeIndex);
		if (lineNrObj == null) {
			if (lineCodeIndex >= fLowestValidCodeIndex) {
				index = lineCodeIndex;
				do {
					lineCodeIndexObj = new Long(index);
					lineNrObj = javaStratumCodeIndexToLine().get(lineCodeIndexObj);
				} while (lineNrObj == null && ++index <= fHighestValidCodeIndex);
				if (lineNrObj != null) {
					return lineNrObj.intValue();
				}
			}
			throw new AbsentInformationException(JDIMessages.MethodImpl_Invalid_code_index_of_a_location_given_4);
		}
		return lineNrObj.intValue();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#allLineLocations()
	 */
	public List<Location> allLineLocations() throws AbsentInformationException {
		return allLineLocations(virtualMachine().getDefaultStratum(), null);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#arguments()
	 */
	public List<LocalVariable> arguments() throws AbsentInformationException {
		if (isNative() || isAbstract()) {
			throw new AbsentInformationException(JDIMessages.MethodImpl_No_local_variable_information_available_9);
		}
		if (fArguments != null) {
			return fArguments;
		}

		List<LocalVariable> result = new ArrayList<LocalVariable>();
		Iterator<LocalVariable> iter = variables().iterator();
		while (iter.hasNext()) {
			LocalVariable var = iter.next();
			if (var.isArgument())
				result.add(var);
		}
		fArguments = result;
		return fArguments;
	}

	/**
	 * @return Returns a text representation of all declared argument types of
	 *         this method.
	 */
	public List<String> argumentTypeNames() {
		if (fArgumentTypeNames != null) {
			return fArgumentTypeNames;
		}
		List<String> argumentTypeSignatures = argumentTypeSignatures();
		List<String> result = new ArrayList<String>();
		for (Iterator<String> iter = argumentTypeSignatures.iterator(); iter.hasNext();) {
			result.add(TypeImpl.signatureToName(iter.next()));
		}

		fArgumentTypeNames = result;
		return fArgumentTypeNames;
	}

	/**
	 * @return Returns a signatures of all declared argument types of this
	 *         method.
	 */
	private List<String> argumentTypeSignatures() {
		if (fArgumentTypeSignatures != null) {
			return fArgumentTypeSignatures;
		}

		fArgumentTypeSignatures = GenericSignature.getParameterTypes(signature());
		return fArgumentTypeSignatures;
	}

	/**
	 * @return Returns the list containing the type of each argument.
	 */
	public List<Type> argumentTypes() throws ClassNotLoadedException {
		if (fArgumentTypes != null) {
			return fArgumentTypes;
		}
		List<Type> result = new ArrayList<Type>();
		Iterator<String> iter = argumentTypeSignatures().iterator();
		ClassLoaderReference classLoaderRef = declaringType().classLoader();
		VirtualMachineImpl vm = virtualMachineImpl();
		while (iter.hasNext()) {
			String argumentTypeSignature = iter.next();
			result.add(TypeImpl.create(vm, argumentTypeSignature, classLoaderRef));
		}
		fArgumentTypes = result;
		return fArgumentTypes;
	}

	/**
	 * @return Returns an array containing the bytecodes for this method.
	 */
	public byte[] bytecodes() {
		if (fByteCodes != null) {
			return fByteCodes;
		}

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithReferenceType(this, outData);

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.M_BYTECODES, outBytes);
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
	@Override
	public int hashCode() {
		return fMethodID.hashCode();
	}

	/**
	 * @return Returns true if two mirrors refer to the same entity in the
	 *         target VM.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		return object != null
				&& object.getClass().equals(this.getClass())
				&& fMethodID.equals(((MethodImpl) object).fMethodID)
				&& referenceTypeImpl().equals(
						((MethodImpl) object).referenceTypeImpl());
	}

	/**
	 * @return Returns a negative integer, zero, or a positive integer as this
	 *         {@link Method} is less than, equal to, or greater than the specified
	 *         {@link Method}.
	 */
	public int compareTo(Method method) {
		if (method == null || !method.getClass().equals(this.getClass()))
			throw new ClassCastException(
					JDIMessages.MethodImpl_Can__t_compare_method_to_given_object_6);

		// See if declaring types are the same, if not return comparison between
		// declaring types.
		Method type2 = method;
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

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#isAbstract()
	 */
	public boolean isAbstract() {
		return (fModifierBits & MODIFIER_ACC_ABSTRACT) != 0;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#isConstructor()
	 */
	public boolean isConstructor() {
		return name().equals("<init>"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#isNative()
	 */
	public boolean isNative() {
		return (fModifierBits & MODIFIER_ACC_NATIVE) != 0;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#isStaticInitializer()
	 */
	public boolean isStaticInitializer() {
		return name().equals("<clinit>"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#isSynchronized()
	 */
	public boolean isSynchronized() {
		return (fModifierBits & MODIFIER_ACC_SYNCHRONIZED) != 0;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#locationOfCodeIndex(long)
	 */
	public Location locationOfCodeIndex(long index) {
		if (isAbstract() || isNative()) {
			return null;
		}
		try {
			Integer lineNrInt = javaStratumCodeIndexToLine().get(new Long(index));
			if (lineNrInt == null) {
				throw new AbsentInformationException(MessageFormat.format(JDIMessages.MethodImpl_No_valid_location_at_the_specified_code_index__0__2, new Object[] { Long.toString(index) }));
			}
		} catch (AbsentInformationException e) {
		}
		return new LocationImpl(virtualMachineImpl(), this, index);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#locationsOfLine(int)
	 */
	public List<Location> locationsOfLine(int line) throws AbsentInformationException {
		return locationsOfLine(virtualMachine().getDefaultStratum(), null, line);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#returnType()
	 */
	public Type returnType() throws ClassNotLoadedException {
		int startIndex = signature().lastIndexOf(')') + 1; // Signature position
															// is just after
															// ending brace.
		return TypeImpl.create(virtualMachineImpl(),
				signature().substring(startIndex), declaringType()
						.classLoader());
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#returnTypeName()
	 */
	public String returnTypeName() {
		if (fReturnTypeName != null) {
			return fReturnTypeName;
		}
		int startIndex = signature().lastIndexOf(')') + 1; // Signature position
															// is just after
															// ending brace.
		fReturnTypeName = TypeImpl.signatureToName(signature().substring(
				startIndex));
		return fReturnTypeName;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#variables()
	 */
	public List<LocalVariable> variables() throws AbsentInformationException {
		if (isNative() || isAbstract()) {
			throw new AbsentInformationException(JDIMessages.MethodImpl_No_local_variable_information_available_9);
		}
		if (fVariables != null) {
			return fVariables;
		}
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithReferenceType(this, outData);

			boolean withGenericSignature = virtualMachineImpl()
					.isJdwpVersionGreaterOrEqual(1, 5);
			int jdwpCommand = withGenericSignature ? JdwpCommandPacket.M_VARIABLE_TABLE_WITH_GENERIC
					: JdwpCommandPacket.M_VARIABLE_TABLE;
			JdwpReplyPacket replyPacket = requestVM(jdwpCommand, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.ABSENT_INFORMATION:
				return inferArguments();
			}

			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			fArgumentSlotsCount = readInt("arg count", replyData); //$NON-NLS-1$
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			List<LocalVariable> variables = new ArrayList<LocalVariable>(nrOfElements);
			for (int i = 0; i < nrOfElements; i++) {
				long codeIndex = readLong("code index", replyData); //$NON-NLS-1$
				String name = readString("name", replyData); //$NON-NLS-1$
				String signature = readString("signature", replyData); //$NON-NLS-1$
				String genericSignature = null;
				if (withGenericSignature) {
					genericSignature = readString("generic signature", replyData); //$NON-NLS-1$
					if ("".equals(genericSignature)) { //$NON-NLS-1$
						genericSignature = null;
					}
				}
				int length = readInt("length", replyData); //$NON-NLS-1$
				int slot = readInt("slot", replyData); //$NON-NLS-1$
				boolean isArgument = slot < fArgumentSlotsCount;

				// Note that for instance methods, the first slot contains the
				// this reference.
				if (isStatic() || slot > 0) {
					LocalVariableImpl localVar = new LocalVariableImpl(
							virtualMachineImpl(), this, codeIndex, name,
							signature, genericSignature, length, slot,
							isArgument);
					variables.add(localVar);
				}
			}
			fVariables = variables;
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
	 * @throws AbsentInformationException
	 */
	private List<LocalVariable> inferArguments() throws AbsentInformationException {
		// infer arguments, if possible

		// try to generate the right generic signature for each argument
		String genericSignature = genericSignature();
		String[] signatures = argumentTypeSignatures().toArray(new String[0]);
		String[] genericSignatures;
		if (genericSignature == null) {
			genericSignatures = new String[signatures.length];
		} else {
			genericSignatures = GenericSignature.getParameterTypes(genericSignature).toArray(new String[0]);
			for (int i = 0; i < genericSignatures.length; i++) {
				if (genericSignatures[i].equals(signatures[i])) {
					genericSignatures[i] = null;
				}
			}
		}

		int slot = 0;
		if (!isStatic()) {
			slot++;
		}
		if (signatures.length > 0) {
			fArgumentSlotsCount = signatures.length;
			fVariables = new ArrayList<LocalVariable>(fArgumentSlotsCount);
			for (int i = 0; i < signatures.length; i++) {
				String name = "arg" + i; //$NON-NLS-1$
				LocalVariableImpl localVar = new LocalVariableImpl(virtualMachineImpl(), this, 0, name, signatures[i], genericSignatures[i], -1, slot, true);
				fVariables.add(localVar);
				slot++;
			}
			return fVariables;
		}
		throw new AbsentInformationException(
				JDIMessages.MethodImpl_No_local_variable_information_available_9);

	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#variablesByName(java.lang.String)
	 */
	public List<LocalVariable> variablesByName(String name) throws AbsentInformationException {
		Iterator<LocalVariable> iter = variables().iterator();
		List<LocalVariable> result = new ArrayList<LocalVariable>();
		while (iter.hasNext()) {
			LocalVariable var = iter.next();
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
			return new LocationImpl(virtualMachineImpl(), this, -1);
		}

		// Return location with Lowest Valid Code Index.
		return new LocationImpl(virtualMachineImpl(), this,
				fLowestValidCodeIndex);
	}

	/**
	 * Writes JDWP representation.
	 */
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		fMethodID.write(out);
		if (target.fVerboseWriter != null) {
			target.fVerboseWriter.println("method", fMethodID.value()); //$NON-NLS-1$
		}
	}

	/**
	 * Writes JDWP representation, including ReferenceType.
	 */
	protected void writeWithReferenceType(MirrorImpl target,
			DataOutputStream out) throws IOException {
		referenceTypeImpl().write(target, out);
		write(target, out);
	}

	/**
	 * Writes JDWP representation, including ReferenceType with Tag.
	 */
	protected void writeWithReferenceTypeWithTag(MirrorImpl target,
			DataOutputStream out) throws IOException {
		referenceTypeImpl().writeWithTag(target, out);
		write(target, out);
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	protected static MethodImpl readWithReferenceTypeWithTag(MirrorImpl target,
			DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		// See Location.
		ReferenceTypeImpl referenceType = ReferenceTypeImpl.readWithTypeTag(
				target, in);
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
		Method method = referenceType.findMethod(ID);
		if (method == null) {
			throw new InternalError(
					JDIMessages.MethodImpl_Got_MethodID_of_ReferenceType_that_is_not_a_member_of_the_ReferenceType_10);
		}
		return (MethodImpl) method;
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	protected static MethodImpl readWithNameSignatureModifiers(
			ReferenceTypeImpl target, ReferenceTypeImpl referenceType,
			boolean withGenericSignature, DataInputStream in)
			throws IOException {
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
		String genericSignature = null;
		if (withGenericSignature) {
			genericSignature = target.readString("generic signature", in); //$NON-NLS-1$
			if ("".equals(genericSignature)) { //$NON-NLS-1$
				genericSignature = null;
			}
		}
		int modifierBits = target.readInt(
				"modifiers", AccessibleImpl.getModifierStrings(), in); //$NON-NLS-1$

		MethodImpl mirror = new MethodImpl(vmImpl, referenceType, ID, name,
				signature, genericSignature, modifierBits);
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

		for (Field field : fields) {
			if ((field.getModifiers() & Modifier.PUBLIC) == 0
					|| (field.getModifiers() & java.lang.reflect.Modifier.STATIC) == 0
					|| (field.getModifiers() & Modifier.FINAL) == 0) {
				continue;
			}

			try {
				String name = field.getName();

				if (name.startsWith("INVOKE_")) { //$NON-NLS-1$
					int value = field.getInt(null);
					for (int j = 0; j < fgInvokeOptions.length; j++) {
						if ((1 << j & value) != 0) {
							fgInvokeOptions[j] = name;
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
	 *      The JDK 1.4.0 specification states that obsolete methods are given
	 *      an ID of zero. It also states that when a method is redefined, the
	 *      new method gets the ID of the old method. Thus, the JDWP query for
	 *      isObsolete on JDK 1.4 will never return true for a non-zero method
	 *      ID. The query is therefore not needed
	 */
	public boolean isObsolete() {
		if (virtualMachineImpl().isJdwpVersionGreaterOrEqual(1, 4)) {
			return fMethodID.value() == 0;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#allLineLocations(java.lang.String, java.lang.String)
	 */
	public List<Location> allLineLocations(String stratum, String sourceName)	throws AbsentInformationException {
		if (isAbstract() || isNative()) {
			return Collections.EMPTY_LIST;
		}
		if (stratum == null) { // if stratum not defined use the default stratum for the declaring type
			stratum = declaringType().defaultStratum();
		}
		List<Location> allLineLocations = null;
		Map<String, List<Location>> sourceNameAllLineLocations = null;
		if (fStratumAllLineLocations == null) { // the stratum map doesn't
												// exist, create it
			fStratumAllLineLocations = new HashMap<String, Map<String, List<Location>>>();
		} else {
			// get the source name map
			sourceNameAllLineLocations = fStratumAllLineLocations.get(stratum);
		}
		if (sourceNameAllLineLocations == null) { // the source name map doesn't
													// exist, create it
			sourceNameAllLineLocations = new HashMap<String, List<Location>>();
			fStratumAllLineLocations.put(stratum, sourceNameAllLineLocations);
		} else {
			// get the line locations
			allLineLocations = sourceNameAllLineLocations.get(sourceName);
		}
		if (allLineLocations == null) { // the line locations are not know,
										// compute and store them
			getLineTable();
			allLineLocations = referenceTypeImpl().allLineLocations(stratum, sourceName, this, fCodeIndexTable, fJavaStratumLineNumberTable);
			sourceNameAllLineLocations.put(sourceName, allLineLocations);
		}
		return allLineLocations;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#locationsOfLine(java.lang.String, java.lang.String, int)
	 */
	public List<Location> locationsOfLine(String stratum, String sourceName,
			int lineNumber) throws AbsentInformationException {
		if (isAbstract() || isNative()) {
			return Collections.EMPTY_LIST;
		}
		return referenceTypeImpl().locationsOfLine(stratum, sourceName, lineNumber, this);
	}

	/**
	 * Return a list which contains a location for the each disjoint range of
	 * code indices that have bean assigned to the given lines (by the compiler
	 * or/and the VM). Return an empty list if there is not executable code at
	 * the specified lines.
	 */
	protected List<Location> javaStratumLocationsOfLines(List<Integer> javaLines)	throws AbsentInformationException {
		Set<Long> tmpLocations = new TreeSet<Long>();
		for (Iterator<Integer> iter = javaLines.iterator(); iter.hasNext();) {
			Integer key = iter.next();
			List<Long> indexes = javaStratumLineToCodeIndexes(key.intValue());
			if (indexes != null) {
				tmpLocations.addAll(indexes);
			}
		}
		List<Location> locations = new ArrayList<Location>();
		for (Iterator<Long> iter = tmpLocations.iterator(); iter.hasNext();) {
			long index = iter.next().longValue();
			int position = Arrays.binarySearch(fCodeIndexTable, index);
			if (position == 0 || !tmpLocations.contains(new Long(fCodeIndexTable[position - 1]))) {
				locations.add(new LocationImpl(virtualMachineImpl(), this, index));
			}
		}
		return locations;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#isBridge()
	 */
	public boolean isBridge() {
		return (fModifierBits & MODIFIER_ACC_BRIDGE) != 0;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.Method#isVarArgs()
	 */
	public boolean isVarArgs() {
		// TODO: remove this test when j9 solve its problem
		// it returns invalid 1.5 flags for 1.4 classes.
		// see bug 53870
		return !virtualMachine().name().equals("j9") && (fModifierBits & MODIFIER_ACC_VARARGS) != 0; //$NON-NLS-1$
	}
}
