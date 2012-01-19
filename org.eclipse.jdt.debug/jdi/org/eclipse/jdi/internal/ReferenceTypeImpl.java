/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Yavor Boyadzhiev <yavor.vasilev.boyadzhiev@sap.com> - Bug 162399  
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
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InternalException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public abstract class ReferenceTypeImpl extends TypeImpl implements
		ReferenceType, org.eclipse.jdi.hcr.ReferenceType {

	/** ClassStatus Constants. */
	public static final int JDWP_CLASS_STATUS_VERIFIED = 1;
	public static final int JDWP_CLASS_STATUS_PREPARED = 2;
	public static final int JDWP_CLASS_STATUS_INITIALIZED = 4;
	public static final int JDWP_CLASS_STATUS_ERROR = 8;

	/** Mapping of command codes to strings. */
	private static String[] fgClassStatusStrings = null;

	/**
	 * Represent the data about one file info contained in one stratum in the
	 * SMAP.
	 */
	protected static class FileInfo {

		/**
		 * The id.
		 */
		protected int fFileId;

		/**
		 * The name of the source file.
		 */
		protected String fFileName;

		/**
		 * The path of the source file.
		 */
		protected String fAbsoluteFileName;

		/**
		 * Map line number in the input source file -> list of [start line in
		 * the output source file, range in the output source file]. (Integer ->
		 * List of int[2]).
		 */
		private HashMap<Integer, List<int[]>> fLineInfo;

		/**
		 * FileInfo constructor.
		 * 
		 * @param fileId
		 *            the id.
		 * @param fileName
		 *            the name of the source file.
		 * @param absoluteFileName
		 *            the path of the source file (can be <code>null</code>).
		 */
		public FileInfo(int fileId, String fileName, String absoluteFileName) {
			fFileId = fileId;
			fFileName = fileName;
			fAbsoluteFileName = absoluteFileName;
			fLineInfo = new HashMap<Integer, List<int[]>>();
		}

		/**
		 * Add information about the mapping of one line. Associate a line in
		 * the input source file to a snippet of code in the output source file.
		 * 
		 * @param inputLine
		 *            the line number in the input source file.
		 * @param outputStartLine
		 *            the number of the first line of the corresponding snippet
		 *            in the output source file.
		 * @param outputLineRange
		 *            the size of the corresponding snippet in the output source
		 *            file.
		 */
		public void addLineInfo(int inputLine, int outputStartLine,
				int outputLineRange) {
			Integer key = new Integer(inputLine);
			List<int[]> outputLines = fLineInfo.get(key);
			if (outputLines == null) {
				outputLines = new ArrayList<int[]>();
				fLineInfo.put(key, outputLines);
			}
			outputLines.add(new int[] { outputStartLine, outputLineRange });
		}

		/**
		 * Return a list of line information about the code in the output source
		 * file associated to the given line in the input source file.
		 * 
		 * @param lineNumber
		 *            the line number in the input source file.
		 * @return a List of int[2].
		 */
		public List<Integer> getOutputLinesForLine(int lineNumber) {
			List<Integer> list = new ArrayList<Integer>();
			List<int[]> outputLines = fLineInfo.get(new Integer(lineNumber));
			if (outputLines != null) {
				for (Iterator<int[]> iter = outputLines.iterator(); iter.hasNext();) {
					int[] info = iter.next();
					int outputLineNumber = info[0];
					int length = info[1];
					if (length == 0) {
						length = length + 1;
					}
					for (int i = 0; i < length; i++) {
						list.add(new Integer(outputLineNumber++));
					}
				}
			}
			return list;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object object) {
			if (!(object instanceof FileInfo)) {
				return false;
			}
			return fFileId == ((FileInfo) object).fFileId;
		}

	}

	/**
	 * Represent the information contained in the SMAP about one stratum.
	 */
	protected static class Stratum {

		/**
		 * The id of this stratum.
		 */
		private String fId;

		/**
		 * The file info data associated to this stratum.
		 */
		private List<FileInfo> fFileInfos;

		/**
		 * Id of the primary file for this stratum.
		 */
		private int fPrimaryFileId;

		/**
		 * Map line number in the output source file -> list of line numbers in
		 * the input source file. (Integer -> List of Integer)
		 */
		private HashMap<Integer, List<int[]>> fOutputLineToInputLine;

		/**
		 * Stratum constructor.
		 * 
		 * @param id
		 *            The id of this stratum.
		 */
		public Stratum(String id) {
			fId = id;
			fFileInfos = new ArrayList<FileInfo>();
			fOutputLineToInputLine = new HashMap<Integer, List<int[]>>();
			fPrimaryFileId = -1;
		}

		/**
		 * Add a file info to this stratum.
		 * 
		 * @param fileId
		 *            the id.
		 * @param fileName
		 *            the name of the source file.
		 */
		public void addFileInfo(int fileId, String fileName)
				throws AbsentInformationException {
			addFileInfo(fileId, fileName, null);
		}

		/**
		 * Add a file info to this stratum.
		 * 
		 * @param fileId
		 *            the id.
		 * @param fileName
		 *            the name of the source file.
		 * @param absoluteFileName
		 *            the path of the source file.
		 */
		public void addFileInfo(int fileId, String fileName,
				String absoluteFileName) throws AbsentInformationException {
			if (fPrimaryFileId == -1) {
				fPrimaryFileId = fileId;
			}
			FileInfo fileInfo = new FileInfo(fileId, fileName, absoluteFileName);
			if (fFileInfos.contains(fileInfo)) {
				throw new AbsentInformationException(NLS.bind(
						JDIMessages.ReferenceTypeImpl_28, new String[] {
								Integer.toString(fileId), fId }));
			}
			fFileInfos.add(fileInfo);
		}

		/**
		 * Add line mapping information.
		 * 
		 * @param inputStartLine
		 *            number of the first line in the input source file.
		 * @param lineFileId
		 *            id of the input source file.
		 * @param repeatCount
		 *            number of iterations.
		 * @param outputStartLine
		 *            number of the first line in the output source file.
		 * @param outputLineIncrement
		 *            number of line to increment at each iteration.
		 * @throws AbsentInformationException
		 */
		public void addLineInfo(int inputStartLine, int lineFileId,
				int repeatCount, int outputStartLine, int outputLineIncrement)
				throws AbsentInformationException {
			FileInfo fileInfo = null;
			// get the FileInfo object
			for (Iterator<FileInfo> iter = fFileInfos.iterator(); iter.hasNext();) {
				FileInfo element = iter.next();
				if (element.fFileId == lineFileId) {
					fileInfo = element;
				}
			}
			if (fileInfo == null) {
				throw new AbsentInformationException(NLS.bind(
						JDIMessages.ReferenceTypeImpl_29,
						new String[] { Integer.toString(lineFileId) }));
			}
			// add the data to the different hash maps.
			for (int i = 0; i < repeatCount; i++, inputStartLine++) {
				fileInfo.addLineInfo(inputStartLine, outputStartLine,
						outputLineIncrement);
				if (outputLineIncrement == 0) {
					// see bug 40022
					addLineInfoToMap(inputStartLine, lineFileId,
							outputStartLine);
				} else {
					for (int j = 0; j < outputLineIncrement; j++, outputStartLine++) {
						addLineInfoToMap(inputStartLine, lineFileId,
								outputStartLine);
					}
				}
			}
		}

		/**
		 * Add the data to the map.
		 */
		private void addLineInfoToMap(int inputStartLine, int lineFileId,
				int outputStartLine) {
			Integer key = new Integer(outputStartLine);
			List<int[]> inputLines = fOutputLineToInputLine.get(key);
			if (inputLines == null) {
				inputLines = new ArrayList<int[]>();
				fOutputLineToInputLine.put(key, inputLines);
			}
			inputLines.add(new int[] { lineFileId, inputStartLine });
		}

		/**
		 * Return the FileInfo object for the specified source name. Return
		 * <code>null</code> if the specified name is the source name of no file
		 * info.
		 * 
		 * @param sourceName
		 *            the source name to search.
		 */
		public FileInfo getFileInfo(String sourceName) {
			for (Iterator<FileInfo> iter = fFileInfos.iterator(); iter.hasNext();) {
				FileInfo fileInfo = iter.next();
				if (fileInfo.fFileName.equals(sourceName)) {
					return fileInfo;
				}
			}
			return null;
		}

		/**
		 * @param outputLineNumber
		 * @return
		 */
		public List<int[]> getInputLineInfos(int outputLineNumber) {
			return fOutputLineToInputLine.get(new Integer(
					outputLineNumber));
		}

	}

	/** ReferenceTypeID that corresponds to this reference. */
	private JdwpReferenceTypeID fReferenceTypeID;

	/** The following are the stored results of JDWP calls. */
	protected List<InterfaceType> fInterfaces = null;
	private List<Method> fMethods = null;
	private Hashtable<JdwpMethodID, Method> fMethodTable = null;
	private List<Field> fFields = null;
	private List<Method> fAllMethods = null;
	private List<Method> fVisibleMethods = null;
	private List<Field> fAllFields = null;
	private List<Field> fVisibleFields = null;
	private List<InterfaceType> fAllInterfaces = null;
	private Map<String, Map<String, List<Location>>> fStratumAllLineLocations = null;
	private String fSourceName = null;
	private int fModifierBits = -1;
	private ClassLoaderReferenceImpl fClassLoader = null;
	private ClassObjectReferenceImpl fClassObject = null;

	private String fGenericSignature; // 1.5 addition
	private boolean fGenericSignatureKnown; // 1.5 addition

	private boolean fGotClassFileVersion = false; // HCR addition.
	private int fClassFileVersion; // HCR addition.
	private boolean fIsHCREligible; // HCR addition.
	private boolean fIsVersionKnown; // HCR addition.

	private boolean fSourceDebugExtensionAvailable = true; // JSR-045 addition

	/**
	 * The default stratum id.
	 */
	private String fDefaultStratumId; // JSR-045 addition

	/**
	 * A map of the defined strata. Map stratum id -> Stratum object. (String ->
	 * Stratum).
	 */
	private Map<String, Stratum> fStrata; // JSR-045 addition

	/**
	 * The source map string returned by the VM.
	 */
	private String fSmap; // JSR-045 addition

	/**
	 * Creates new instance.
	 */
	protected ReferenceTypeImpl(String description, VirtualMachineImpl vmImpl,
			JdwpReferenceTypeID referenceTypeID) {
		super(description, vmImpl);
		fReferenceTypeID = referenceTypeID;
	}

	/**
	 * Creates new instance.
	 */
	protected ReferenceTypeImpl(String description, VirtualMachineImpl vmImpl,
			JdwpReferenceTypeID referenceTypeID, String signature,
			String genericSignature) {
		super(description, vmImpl);
		fReferenceTypeID = referenceTypeID;
		setSignature(signature);
		setGenericSignature(genericSignature);
	}

	/**
	 * @return Returns type tag.
	 */
	public abstract byte typeTag();

	/**
	 * Flushes all stored Jdwp results.
	 */
	public void flushStoredJdwpResults() {
		// Flush Methods.
		if (fMethods != null) {
			for (Method method : fMethods) {
				((MethodImpl)method).flushStoredJdwpResults();
			}
			fMethods = null;
			fMethodTable = null;
		}

		// Flush Fields.
		if (fFields != null) {
			for (Field field : fFields) {
				((FieldImpl)field).flushStoredJdwpResults();
			}
			fFields = null;
		}

		fInterfaces = null;
		fAllMethods = null;
		fVisibleMethods = null;
		fAllFields = null;
		fVisibleFields = null;
		fAllInterfaces = null;
		fStratumAllLineLocations = null;
		fSourceName = null;
		fModifierBits = -1;
		fClassLoader = null;
		fClassObject = null;
		fGotClassFileVersion = false;
		// java 1.5
		fGenericSignature = null;
		fGenericSignatureKnown = false;

		// JSR-045
		fSourceDebugExtensionAvailable = true;
		fDefaultStratumId = null;
		fStrata = null;
		fSmap = null;

		// The following cached results are stored higher up in the class
		// hierarchy.
		fSignature = null;
		fSourceName = null;
	}

	/**
	 * @return Returns the interfaces declared as implemented by this class.
	 *         Interfaces indirectly implemented (extended by the implemented
	 *         interface or implemented by a superclass) are not included.
	 */
	public List<InterfaceType> allInterfaces() {
		if (fAllInterfaces != null) {
			return fAllInterfaces;
		}

		/*
		 * Recursion: The interfaces that it directly implements; All interfaces
		 * that are implemented by its interfaces; If it is a class, all
		 * interfaces that are implemented by its superclass.
		 */
		// The interfaces are maintained in a set, to avoid duplicates.
		// The interfaces of its own (own interfaces() command) are first
		// inserted.
		HashSet<InterfaceType> allInterfacesSet = new HashSet<InterfaceType>(interfaces());

		// All interfaces of the interfaces it implements.
		Iterator<InterfaceType> interfaces = interfaces().iterator();
		InterfaceType inter;
		while (interfaces.hasNext()) {
			inter = interfaces.next();
			allInterfacesSet.addAll(((InterfaceTypeImpl)inter).allInterfaces());
		}

		// If it is a class, all interfaces of it's superclass.
		if (this instanceof ClassType) {
			ClassType superclass = ((ClassType) this).superclass();
			if (superclass != null) {
				allInterfacesSet.addAll(superclass.allInterfaces());
			}
		}

		fAllInterfaces = new ArrayList<InterfaceType>(allInterfacesSet);
		return fAllInterfaces;
	}

	/**
	 * @return Returns JDWP Reference ID.
	 */
	public JdwpReferenceTypeID getRefTypeID() {
		return fReferenceTypeID;
	}

	/**
	 * @return Returns modifier bits.
	 */
	@Override
	public int modifiers() {
		if (fModifierBits != -1)
			return fModifierBits;

		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_MODIFIERS, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fModifierBits = readInt(
					"modifiers", AccessibleImpl.getModifierStrings(), replyData); //$NON-NLS-1$
			return fModifierBits;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Add methods to a set of methods if they are not overridden, add new
	 * names+signature combinations to set of names+signature combinations.
	 */
	private void addVisibleMethods(List<Method> inheritedMethods, Set<String> nameAndSignatures, List<Method> resultMethods) {
		Iterator<Method> iter = inheritedMethods.iterator();
		Method inheritedMethod;
		while (iter.hasNext()) {
			inheritedMethod = iter.next();
			if (!nameAndSignatures.contains(inheritedMethod.name()
					+ inheritedMethod.signature())) {
				resultMethods.add(inheritedMethod);
			}
		}
	}

	/**
	 * @return Returns a list containing each visible and unambiguous Method in
	 *         this type.
	 */
	public List<Method> visibleMethods() {
		if (fVisibleMethods != null)
			return fVisibleMethods;

		/*
		 * Recursion: The methods of its own (own methods() command); All
		 * methods of the interfaces it implements; If it is a class, all
		 * methods of it's superclass.
		 */
		// The name+signature combinations of methods are maintained in a set,
		// to avoid including methods that have been overridden.
		Set<String> namesAndSignatures = new HashSet<String>();
		List<Method> visibleMethods = new ArrayList<Method>();

		// The methods of its own (own methods() command).
		for (Iterator<Method> iter = methods().iterator(); iter.hasNext();) {
			MethodImpl method = (MethodImpl) iter.next();
			namesAndSignatures.add(method.name() + method.signature());
			visibleMethods.add(method);
		}

		// All methods of the interfaces it implements.
		Iterator<InterfaceType> interfaces = interfaces().iterator();
		InterfaceType inter;
		while (interfaces.hasNext()) {
			inter = interfaces.next();
			addVisibleMethods(inter.visibleMethods(), namesAndSignatures,
					visibleMethods);
		}

		// If it is a class, all methods of it's superclass.
		if (this instanceof ClassType) {
			ClassType superclass = ((ClassType) this).superclass();
			if (superclass != null)
				addVisibleMethods(superclass.visibleMethods(),
						namesAndSignatures, visibleMethods);
		}

		fVisibleMethods = visibleMethods;
		return fVisibleMethods;
	}

	/**
	 * @return Returns a list containing each Method declared in this type, and
	 *         its super-classes, implemented interfaces, and/or
	 *         super-interfaces.
	 */
	public List<Method> allMethods() {
		if (fAllMethods != null)
			return fAllMethods;

		/*
		 * Recursion: The methods of its own (own methods() command); All
		 * methods of the interfaces it implements; If it is a class, all
		 * methods of it's superclass.
		 */
		// The name+signature combinations of methods are maintained in a set.
		HashSet<Method> resultSet = new HashSet<Method>();

		// The methods of its own (own methods() command).
		resultSet.addAll(methods());

		// All methods of the interfaces it implements.
		Iterator<InterfaceType> interfaces = interfaces().iterator();
		InterfaceType inter;
		while (interfaces.hasNext()) {
			inter = interfaces.next();
			resultSet.addAll(inter.allMethods());
		}

		// If it is a class, all methods of it's superclass.
		if (this instanceof ClassType) {
			ClassType superclass = ((ClassType) this).superclass();
			if (superclass != null)
				resultSet.addAll(superclass.allMethods());
		}

		fAllMethods = new ArrayList<Method>(resultSet);
		return fAllMethods;
	}

	/**
	 * @return Returns the interfaces declared as implemented by this class.
	 *         Interfaces indirectly implemented (extended by the implemented
	 *         interface or implemented by a superclass) are not included.
	 */
	public List<InterfaceType> interfaces() {
		if (fInterfaces != null) {
			return fInterfaces;
		}

		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_INTERFACES, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.NOT_FOUND:
				// Workaround for problem in J2ME WTK (wireless toolkit)
				// @see Bug 12966
				return Collections.EMPTY_LIST;
			default:
				defaultReplyErrorHandler(replyPacket.errorCode());
			}
			DataInputStream replyData = replyPacket.dataInStream();
			List<InterfaceType> elements = new ArrayList<InterfaceType>();
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
	 * Add fields to a set of fields if they are not overridden, add new field
	 * names to set of field names.
	 */
	private void addVisibleFields(List<Field> newFields, Set<String> names, List<Field> resultFields) {
		Iterator<Field> iter = newFields.iterator();
		FieldImpl field;
		while (iter.hasNext()) {
			field = (FieldImpl) iter.next();
			String name = field.name();
			if (!names.contains(name)) {
				resultFields.add(field);
				names.add(name);
			}
		}
	}

	/**
	 * @return Returns a list containing each visible and unambiguous Field in
	 *         this type.
	 */
	public List<Field> visibleFields() {
		if (fVisibleFields != null)
			return fVisibleFields;

		/*
		 * Recursion: The fields of its own (own fields() command); All fields
		 * of the interfaces it implements; If it is a class, all fields of it's
		 * superclass.
		 */
		// The names of fields are maintained in a set, to avoid including
		// fields that have been overridden.
		HashSet<String> fieldNames = new HashSet<String>();

		// The fields of its own (own fields() command).
		List<Field> visibleFields = new ArrayList<Field>();
		addVisibleFields(fields(), fieldNames, visibleFields);

		// All fields of the interfaces it implements.
		Iterator<InterfaceType> interfaces = interfaces().iterator();
		InterfaceType inter;
		while (interfaces.hasNext()) {
			inter = interfaces.next();
			addVisibleFields(inter.visibleFields(), fieldNames, visibleFields);
		}

		// If it is a class, all fields of it's superclass.
		if (this instanceof ClassType) {
			ClassType superclass = ((ClassType) this).superclass();
			if (superclass != null)
				addVisibleFields(superclass.visibleFields(), fieldNames,
						visibleFields);
		}

		fVisibleFields = visibleFields;
		return fVisibleFields;
	}

	/**
	 * @return Returns a list containing each Field declared in this type, and
	 *         its super-classes, implemented interfaces, and/or
	 *         super-interfaces.
	 */
	public List<Field> allFields() {
		if (fAllFields != null)
			return fAllFields;

		/*
		 * Recursion: The fields of its own (own fields() command); All fields
		 * of the interfaces it implements; If it is a class, all fields of it's
		 * superclass.
		 */
		// The names of fields are maintained in a set, to avoid including
		// fields that have been inherited double.
		HashSet<Field> resultSet = new HashSet<Field>();

		// The fields of its own (own fields() command).
		resultSet.addAll(fields());

		// All fields of the interfaces it implements.
		Iterator<InterfaceType> interfaces = interfaces().iterator();
		InterfaceType inter;
		while (interfaces.hasNext()) {
			inter = interfaces.next();
			resultSet.addAll(inter.allFields());
		}

		// If it is a class, all fields of it's superclass.
		if (this instanceof ClassType) {
			ClassType superclass = ((ClassType) this).superclass();
			if (superclass != null)
				resultSet.addAll(superclass.allFields());
		}

		fAllFields = new ArrayList<Field>(resultSet);
		return fAllFields;
	}

	/**
	 * @return Returns the class loader object which loaded the class
	 *         corresponding to this type.
	 */
	public ClassLoaderReference classLoader() {
		if (fClassLoader != null)
			return fClassLoader;

		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_CLASS_LOADER, this);
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
	 * @return Returns the class object that corresponds to this type in the
	 *         target VM.
	 */
	public ClassObjectReference classObject() {
		if (fClassObject != null)
			return fClassObject;

		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_CLASS_OBJECT, this);
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
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_STATUS, this);
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
		Iterator<Field> iter = visibleFields().iterator();
		while (iter.hasNext()) {
			FieldImpl field = (FieldImpl) iter.next();
			if (field.name().equals(name))
				return field;
		}
		return null;
	}

	/**
	 * @return Returns a list containing each Field declared in this type.
	 */
	public List<Field> fields() {
		if (fFields != null) {
			return fFields;
		}

		// Note: Fields are returned in the order they occur in the class file,
		// therefore their
		// order in this list can be used for comparisons.
		initJdwpRequest();
		try {
			boolean withGenericSignature = virtualMachineImpl()
					.isJdwpVersionGreaterOrEqual(1, 5);
			int jdwpCommand = withGenericSignature ? JdwpCommandPacket.RT_FIELDS_WITH_GENERIC
					: JdwpCommandPacket.RT_FIELDS;
			JdwpReplyPacket replyPacket = requestVM(jdwpCommand, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			List<Field> elements = new ArrayList<Field>();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			for (int i = 0; i < nrOfElements; i++) {
				FieldImpl elt = FieldImpl.readWithNameSignatureModifiers(this,
						this, withGenericSignature, replyData);
				if (elt == null) {
					continue;
				}
				elements.add(elt);
			}
			fFields = elements;
			return fFields;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns FieldImpl of a field in the reference specified by a
	 *         given fieldID, or null if not found.
	 */
	public FieldImpl findField(JdwpFieldID fieldID) {
		Iterator<Field> iter = fields().iterator();
		while (iter.hasNext()) {
			FieldImpl field = (FieldImpl) iter.next();
			if (field.getFieldID().equals(fieldID))
				return field;
		}
		return null;
	}

	/**
	 * @return Returns MethodImpl of a method in the reference specified by a
	 *         given methodID, or null if not found.
	 */
	public Method findMethod(JdwpMethodID methodID) {
		if (methodID.value() == 0) {
			return new MethodImpl(virtualMachineImpl(), this, methodID,
					JDIMessages.ReferenceTypeImpl_Obsolete_method_1,
					"", null, -1); //$NON-NLS-1$ 
		}
		if (fMethodTable == null) {
			fMethodTable = new Hashtable<JdwpMethodID, Method>();
			Iterator<Method> iter = methods().iterator();
			while (iter.hasNext()) {
				MethodImpl method = (MethodImpl) iter.next();
				fMethodTable.put(method.getMethodID(), method);
			}
		}
		return fMethodTable.get(methodID);
	}

	/**
	 * @return Returns the Value of a given static Field in this type.
	 */
	public Value getValue(Field field) {
		ArrayList<Field> list = new ArrayList<Field>(1);
		list.add(field);
		return getValues(list).get(field);
	}

	/**
	 * @return a Map of the requested static Field objects with their Value.
	 */
	public Map<Field, Value> getValues(List<? extends Field> fields) {
		// if the field list is empty, nothing to do
		if (fields.isEmpty()) {
			return new HashMap<Field, Value>();
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
				FieldImpl field = (FieldImpl) fields.get(i);
				checkVM(field);
				field.getFieldID().write(outData);
			}

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_GET_VALUES, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			HashMap<Field, Value> map = new HashMap<Field, Value>();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			if (nrOfElements != fieldsSize)
				throw new InternalError(
						JDIMessages.ReferenceTypeImpl_Retrieved_a_different_number_of_values_from_the_VM_than_requested_3);

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
	@Override
	public int hashCode() {
		return fReferenceTypeID.hashCode();
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
				&& fReferenceTypeID
						.equals(((ReferenceTypeImpl) object).fReferenceTypeID)
				&& virtualMachine().equals(
						((MirrorImpl) object).virtualMachine());
	}

	/**
	 * @return Returns a negative integer, zero, or a positive integer as this
	 *         {@link ReferenceType} is less than, equal to, or greater than the specified
	 *         {@link ReferenceType}.
	 */
	public int compareTo(ReferenceType type) {
		if (type == null || !type.getClass().equals(this.getClass()))
			throw new ClassCastException(JDIMessages.ReferenceTypeImpl_Can__t_compare_reference_type_to_given_object_4);
		return name().compareTo(type.name());
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

	/* (non-Javadoc)
	 * @see com.sun.jdi.ReferenceType#locationsOfLine(int)
	 */
	public List<Location> locationsOfLine(int line) throws AbsentInformationException {
		return locationsOfLine(virtualMachine().getDefaultStratum(), null, line);
	}

	/**
	 * @return Returns a list containing each Method declared directly in this
	 *         type.
	 */
	public List<Method> methods() {
		// Note that ArrayReference overwrites this method by returning an empty
		// list.
		if (fMethods != null)
			return fMethods;

		// Note: Methods are returned in the order they occur in the class file,
		// therefore their
		// order in this list can be used for comparisons.
		initJdwpRequest();
		try {
			boolean withGenericSignature = virtualMachineImpl()
					.isJdwpVersionGreaterOrEqual(1, 5);
			int jdwpCommand = withGenericSignature ? JdwpCommandPacket.RT_METHODS_WITH_GENERIC
					: JdwpCommandPacket.RT_METHODS;
			JdwpReplyPacket replyPacket = requestVM(jdwpCommand, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			List<Method> elements = new ArrayList<Method>();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			for (int i = 0; i < nrOfElements; i++) {
				MethodImpl elt = MethodImpl.readWithNameSignatureModifiers(
						this, this, withGenericSignature, replyData);
				if (elt == null) {
					continue;
				}
				elements.add(elt);
			}
			fMethods = elements;
			return fMethods;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns a List containing each visible Method that has the given
	 *         name.
	 */
	public List<Method> methodsByName(String name) {
		List<Method> elements = new ArrayList<Method>();
		Iterator<Method> iter = visibleMethods().iterator();
		while (iter.hasNext()) {
			Method method = iter.next();
			if (method.name().equals(name)) {
				elements.add(method);
			}
		}
		return elements;
	}

	/**
	 * @return Returns a List containing each visible Method that has the given
	 *         name and signature.
	 */
	public List<Method> methodsByName(String name, String signature) {
		List<Method> elements = new ArrayList<Method>();
		Iterator<Method> iter = visibleMethods().iterator();
		while (iter.hasNext()) {
			MethodImpl method = (MethodImpl) iter.next();
			if (method.name().equals(name) && method.signature().equals(signature)) {
				elements.add(method);
			}
		}
		return elements;
	}

	/**
	 * @return Returns the fully qualified name of this type.
	 */
	@Override
	public String name() {
		// Make sure that we know the signature, from which the name is derived.
		if (fName == null) {
			setName(signatureToName(signature()));
		}
		return fName;
	}

	/**
	 * @return Returns the JNI-style signature for this type.
	 */
	@Override
	public String signature() {
		if (fSignature != null) {
			return fSignature;
		}
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_SIGNATURE, this);
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
	 * @return Returns a List containing each ReferenceType declared within this
	 *         type.
	 */
	public List<ReferenceType> nestedTypes() {
		// Note that the VM gives an empty reply on RT_NESTED_TYPES, therefore
		// we search for the
		// nested types in the loaded types.
		List<ReferenceType> result = new ArrayList<ReferenceType>();
		Iterator<ReferenceType> itr = virtualMachineImpl().allRefTypes();
		while (itr.hasNext()) {
			try {
				ReferenceTypeImpl refType = (ReferenceTypeImpl) itr.next();
				String refName = refType.name();
				if (refName.length() > name().length()
						&& refName.startsWith(name())
						&& refName.charAt(name().length()) == '$') {
					result.add(refType);
				}
			} catch (ClassNotPreparedException e) {
				continue;
			}
		}
		return result;
	}

	/**
	 * @return Returns an identifying name for the source corresponding to the
	 *         declaration of this type.
	 */
	public String sourceName() throws AbsentInformationException {
		// sourceNames list in never empty, an AbsentInformationException is
		// thrown
		// if the source name is not known.
		return sourceNames(virtualMachine().getDefaultStratum())
				.get(0);
	}

	/**
	 * @return Returns the CRC-32 of the given reference type, undefined if
	 *         unknown.
	 */
	public int getClassFileVersion() {
		virtualMachineImpl().checkHCRSupported();
		if (fGotClassFileVersion)
			return fClassFileVersion;

		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.HCR_GET_CLASS_VERSION, this);
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
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		fReferenceTypeID.write(out);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println(
					"referenceType", fReferenceTypeID.value()); //$NON-NLS-1$
	}

	/**
	 * Writes representation of null referenceType.
	 */
	public static void writeNull(MirrorImpl target, DataOutputStream out)
			throws IOException {
		// create null id
		JdwpReferenceTypeID ID = new JdwpReferenceTypeID(
				target.virtualMachineImpl());
		ID.write(out);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("referenceType", ID.value()); //$NON-NLS-1$
	}

	/**
	 * Writes JDWP representation.
	 */
	public void writeWithTag(MirrorImpl target, DataOutputStream out)
			throws IOException {
		target.writeByte(typeTag(), "type tag", JdwpID.typeTagMap(), out); //$NON-NLS-1$
		write(target, out);
	}

	/**
	 * @return Reads JDWP representation and returns new or cached instance.
	 */
	public static ReferenceTypeImpl readWithTypeTag(MirrorImpl target,
			DataInputStream in) throws IOException {
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
		throw new InternalException(
				JDIMessages.ReferenceTypeImpl_Invalid_ReferenceTypeID_tag_encountered___8
						+ typeTag);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ReferenceType#allLineLocations()
	 */
	public List<Location> allLineLocations() throws AbsentInformationException {
		return allLineLocations(virtualMachine().getDefaultStratum(), null);
	}

	/**
	 * @return Reads JDWP representation and returns new or cached instance.
	 */
	public static ReferenceTypeImpl readWithTypeTagAndSignature(
			MirrorImpl target, boolean withGenericSignature, DataInputStream in)
			throws IOException {
		byte typeTag = target.readByte("type tag", JdwpID.typeTagMap(), in); //$NON-NLS-1$
		switch (typeTag) {
		case 0:
			return null;
		case ArrayTypeImpl.typeTag:
			return ArrayTypeImpl.readWithSignature(target,
					withGenericSignature, in);
		case ClassTypeImpl.typeTag:
			return ClassTypeImpl.readWithSignature(target,
					withGenericSignature, in);
		case InterfaceTypeImpl.typeTag:
			return InterfaceTypeImpl.readWithSignature(target,
					withGenericSignature, in);
		}
		throw new InternalException(
				JDIMessages.ReferenceTypeImpl_Invalid_ReferenceTypeID_tag_encountered___8
						+ typeTag);
	}

	/**
	 * @return Returns new instance based on signature and classLoader.
	 * @throws ClassNotLoadedException
	 *             when the ReferenceType has not been loaded by the specified
	 *             class loader.
	 */
	public static TypeImpl create(VirtualMachineImpl vmImpl, String signature,
			ClassLoaderReference classLoader) throws ClassNotLoadedException {
		ReferenceTypeImpl refTypeBootstrap = null;
		List<ReferenceType> classes = vmImpl.classesBySignature(signature);
		ReferenceTypeImpl type;
		Iterator<ReferenceType> iter = classes.iterator();
		while (iter.hasNext()) {
			// First pass. Look for a class loaded by the given class loader
			type = (ReferenceTypeImpl) iter.next();
			if (type.classLoader() == null) { // bootstrap classloader
				if (classLoader == null) {
					return type;
				}
				refTypeBootstrap = type;
			}
			if (classLoader != null && classLoader.equals(type.classLoader())) {
				return type;
			}
		}
		// If no ReferenceType is found with the specified classloader, but
		// there is one with the
		// bootstrap classloader, the latter is returned.
		if (refTypeBootstrap != null) {
			return refTypeBootstrap;
		}

		List<ReferenceType> visibleTypes;
		iter = classes.iterator();
		while (iter.hasNext()) {
			// Second pass. Look for a class that is visible to
			// the given class loader
			type = (ReferenceTypeImpl) iter.next();
			visibleTypes = classLoader.visibleClasses();
			Iterator<ReferenceType> visibleIter = visibleTypes.iterator();
			while (visibleIter.hasNext()) {
				if (type.equals(visibleIter.next())) {
					return type;
				}
			}
		}

		throw new ClassNotLoadedException(classSignatureToName(signature),
				JDIMessages.ReferenceTypeImpl_Type_has_not_been_loaded_10);
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fgClassStatusStrings != null) {
			return;
		}

		java.lang.reflect.Field[] fields = ReferenceTypeImpl.class
				.getDeclaredFields();
		fgClassStatusStrings = new String[32];

		for (java.lang.reflect.Field field : fields) {
			if ((field.getModifiers() & Modifier.PUBLIC) == 0
					|| (field.getModifiers() & Modifier.STATIC) == 0
					|| (field.getModifiers() & Modifier.FINAL) == 0) {
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
						fgClassStatusStrings[j] = name;
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
	@Override
	public Value createNullValue() {
		return null;
	}

	/**
	 * @see ReferenceType#sourceNames(String)
	 */
	public List<String> sourceNames(String stratumId) throws AbsentInformationException {
		List<String> list = new ArrayList<String>();
		Stratum stratum = getStratum(stratumId);
		if (stratum != null) {
			// return the source names defined for this stratum in the SMAP.
			List<FileInfo> fileInfos = stratum.fFileInfos;
			if (fileInfos.isEmpty()) {
				throw new AbsentInformationException(
						JDIMessages.ReferenceTypeImpl_30);
			}
			for (Iterator<FileInfo> iter = stratum.fFileInfos.iterator(); iter.hasNext();) {
				list.add(iter.next().fFileName);
			}
			return list;
		}
		// Java stratum
		if (fSourceName == null) {
			getSourceName();
		}
		list.add(fSourceName);
		return list;
	}

	/**
	 * @see ReferenceType#sourcePaths(String)
	 */
	public List<String> sourcePaths(String stratumId) throws AbsentInformationException {
		List<String> list = new ArrayList<String>();
		Stratum stratum = getStratum(stratumId);
		if (stratum != null) {
			// return the source paths defined for this stratum in the SMAP.
			for (Iterator<FileInfo> iter = stratum.fFileInfos.iterator(); iter.hasNext();) {
				FileInfo fileInfo = iter.next();
				String path = fileInfo.fAbsoluteFileName;
				if (path == null) {
					path = getPath(fileInfo.fFileName);
				}
				list.add(path);
			}
			return list;
		}
		// Java stratum
		if (fSourceName == null) {
			getSourceName();
		}
		list.add(getPath(fSourceName));
		return list;
	}

	/**
	 * @see ReferenceType#sourceDebugExtension()
	 */
	public String sourceDebugExtension() throws AbsentInformationException {
		if (isSourceDebugExtensionAvailable()) {
			return fSmap;
		}
		if (!virtualMachine().canGetSourceDebugExtension()) {
			throw new UnsupportedOperationException();
		}
		throw new AbsentInformationException();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ReferenceType#allLineLocations(java.lang.String, java.lang.String)
	 */
	public List<Location> allLineLocations(String stratum, String sourceName) throws AbsentInformationException {
		Iterator<Method> allMethods = methods().iterator();
		if (stratum == null) { // if stratum not defined use the default stratum
			stratum = defaultStratum();
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
			allLineLocations = new ArrayList<Location>();
			while (allMethods.hasNext()) {
				MethodImpl method = (MethodImpl) allMethods.next();
				if (method.isAbstract() || method.isNative()) {
					continue;
				}
				allLineLocations.addAll(method.allLineLocations(stratum, sourceName));
			}
			sourceNameAllLineLocations.put(sourceName, allLineLocations);
		}
		return allLineLocations;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ReferenceType#locationsOfLine(java.lang.String, java.lang.String, int)
	 */
	public List<Location> locationsOfLine(String stratum, String sourceName, int lineNumber) throws AbsentInformationException {
		Iterator<Method> allMethods = methods().iterator();
		List<Location> locations = new ArrayList<Location>();
		boolean hasLineInformation = false;
		AbsentInformationException exception = null;
		while (allMethods.hasNext()) {
			MethodImpl method = (MethodImpl) allMethods.next();
			if (method.isAbstract() || method.isNative()) {
				continue;
			}
			// one line in the input source can be translate in multiple lines
			// in different
			// methods in the output source. We need all these locations.
			try {
				locations.addAll(locationsOfLine(stratum, sourceName, lineNumber, method));
				hasLineInformation = true;
			} catch (AbsentInformationException e) {
				exception = e;
			}
		}
		if (!hasLineInformation && exception != null) {
			throw exception;
		}
		return locations;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ReferenceType#availableStrata()
	 */
	public List<String> availableStrata() {
		List<String> list = new ArrayList<String>();
		// The strata defined in the SMAP.
		if (isSourceDebugExtensionAvailable()) {
			list.addAll(fStrata.keySet());
		}
		// plus the Java stratum
		list.add(VirtualMachineImpl.JAVA_STRATUM_NAME);
		return list;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ReferenceType#defaultStratum()
	 */
	public String defaultStratum() {
		if (isSourceDebugExtensionAvailable()) {
			return fDefaultStratumId;
		}
		// if not defined, return Java.
		return VirtualMachineImpl.JAVA_STRATUM_NAME;
	}

	/**
	 * Generate a source path from the given source name. The returned string is
	 * the package name of this type converted to a platform dependent path
	 * followed by the given source name. For example, on a Unix platform, the
	 * type org.my.TestJsp with the source name test.jsp would return
	 * "org/my/test.jsp".
	 */
	private String getPath(String sourceName) {
		String name = name();
		int lastDotOffset = name.lastIndexOf('.');
		if (lastDotOffset == -1) {
			return sourceName;
		}
		char fileSeparator = System.getProperty("file.separator").charAt(0); //$NON-NLS-1$
		return name.substring(0, lastDotOffset).replace('.', fileSeparator)
				+ fileSeparator + sourceName;
	}

	/**
	 * Return the stratum object for this stratum Id. If the the specified
	 * stratum id is not defined for this reference type, return the stratum
	 * object for the default stratum. If the specified stratum id (or the
	 * default stratum id, if the specified stratum id is not defined) is
	 * <code>Java</code>, return <code>null</code>.
	 */
	private Stratum getStratum(String stratumId) {
		if (!VirtualMachineImpl.JAVA_STRATUM_NAME.equals(stratumId)
				&& isSourceDebugExtensionAvailable()) {
			if (stratumId == null || !fStrata.keySet().contains(stratumId)) {
				stratumId = fDefaultStratumId;
			}
			if (!VirtualMachineImpl.JAVA_STRATUM_NAME.equals(stratumId)) {
				return fStrata.get(stratumId);
			}
		}
		return null;
	}

	/**
	 * Get the source debug extension from the VM.
	 * 
	 * @throws AbsentInformationException
	 */
	private void getSourceDebugExtension() throws AbsentInformationException {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_SOURCE_DEBUG_EXTENSION, this);
			if (replyPacket.errorCode() == JdwpReplyPacket.ABSENT_INFORMATION) {
				throw new AbsentInformationException(
						JDIMessages.ReferenceTypeImpl_31);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fSmap = readString(JDIMessages.ReferenceTypeImpl_32, replyData);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
		// TODO: remove the workaround when the J9SC20030415 bug is fixed (see
		// bug 96485 of the vendor bug system).
		// Workaround to a J9SC bug. It returns an empty string instead of a
		// ABSENT_INFORMATION
		// error if the source debug extension is not available.
		if ("".equals(fSmap)) { //$NON-NLS-1$
			throw new AbsentInformationException(
					JDIMessages.ReferenceTypeImpl_31);
		}
		// parse the source map.
		fStrata = new HashMap<String, Stratum>();
		SourceDebugExtensionParser.parse(fSmap, this);
	}

	/**
	 * Get the name of the Java source file from the VM.
	 * 
	 * @throws AbsentInformationException
	 */
	private void getSourceName() throws AbsentInformationException {
		if (fSourceName != null || isSourceDebugExtensionAvailable()) {
			return;
		}
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_SOURCE_FILE, this);
			if (replyPacket.errorCode() == JdwpReplyPacket.ABSENT_INFORMATION) {
				throw new AbsentInformationException(
						JDIMessages.ReferenceTypeImpl_Source_name_is_not_known_7);
			}

			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			fSourceName = readString("source name", replyData); //$NON-NLS-1$
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Check in the source debug extension is available. To call before doing
	 * operations which need data from the SMAP. Return <code>false</code> if
	 * the source debug extension is not available for any reason.
	 * <code>true</code> indicates that the source debug extension is available
	 * and the information has been parsed and stored in the maps and lists.
	 */
	private synchronized boolean isSourceDebugExtensionAvailable() {
		if (!fSourceDebugExtensionAvailable) {
			return false;
		}
		if (!virtualMachine().canGetSourceDebugExtension()) {
			fSourceDebugExtensionAvailable = false;
			return false;
		}
		if (fSmap == null) {
			try {
				getSourceDebugExtension();
			} catch (AbsentInformationException e) {
				fSourceDebugExtensionAvailable = false;
				return false;
			}
		}
		return true;
	}

	/**
	 * Set the output file name, i.e. the .java file used to generate the
	 * bytecode.
	 */
	protected void setOutputFileName(String outputFileName) {
		fSourceName = outputFileName;
	}

	/**
	 * Set the default stratum. This stratum will be used for the method on
	 * strata related data, but with no stratum parameter.
	 */
	protected void setDefaultStratumId(String defaultStratumId) {
		fDefaultStratumId = defaultStratumId;
	}

	/**
	 * Add a new stratum to this type.
	 */
	protected void addStratum(Stratum stratum) {
		fStrata.put(stratum.fId, stratum);
	}

	/**
	 * Return the name of the input source file of which the given code index is
	 * part of the translation, for this stratum. If the code at the given index
	 * is not a part of the translation of the given stratum code, return the
	 * name of the primary input source file.
	 * 
	 * @param codeIndex
	 *            the index of the code.
	 * @param method
	 *            the method where is the code.
	 * @param stratumId
	 */
	protected String sourceName(long codeIndex, MethodImpl method,
			String stratumId) throws AbsentInformationException {
		Stratum stratum = getStratum(stratumId);
		if (stratum != null) {
			FileInfo fileInfo = fileInfo(codeIndex, method, stratum);
			if (fileInfo != null) {
				return fileInfo.fFileName;
			}
		}
		// Java stratum
		if (fSourceName == null) {
			getSourceName();
		}
		return fSourceName;
	}

	/**
	 * Return the FileInfo object of the input source file of which the given
	 * code index is part of the translation, for this stratum. If the code at
	 * the given index is not a part of the translation of the given stratum
	 * code, return the FileInfo of the primary input source file.
	 * 
	 * @param codeIndex
	 *            the index of the code.
	 * @param method
	 *            the method where is the code.
	 * @param stratum
	 */
	private FileInfo fileInfo(long codeIndex, MethodImpl method, Stratum stratum) {
		int fileId = stratum.fPrimaryFileId;
		if (stratum.fFileInfos.size() > 1) {
			List<int[]> lineInfos = null;
			try {
				lineInfos = lineInfos(codeIndex, method, stratum);
			} catch (AbsentInformationException e) {
				// nothing to do, use the primary file id.
			}
			if (lineInfos != null) {
				fileId = lineInfos.get(0)[0];
			}
		}
		for (Iterator<FileInfo> iter = stratum.fFileInfos.iterator(); iter.hasNext();) {
			FileInfo fileInfo = iter.next();
			if (fileInfo.fFileId == fileId) {
				return fileInfo;
			}
		}
		// should never return null
		return null;
	}

	/**
	 * Return the list of line number in the input files of the stratum
	 * associated with the code at the given address.
	 * 
	 * @param codeIndex
	 *            the index of the code.
	 * @param method
	 *            the method where is the code.
	 * @param stratum
	 * @return List of int[2]: [fileId, inputLineNumber]
	 */
	private List<int[]> lineInfos(long codeIndex, MethodImpl method, Stratum stratum) throws AbsentInformationException {
		int outputLineNumber = -1;
		try {
			outputLineNumber = method.javaStratumLineNumber(codeIndex);
		} catch (NativeMethodException e) { // Occurs in SUN VM.
			return null;
		}
		if (outputLineNumber != -1) {
			return stratum.getInputLineInfos(outputLineNumber);
		}
		return null;
	}

	/**
	 * Return the path of the input source file of which the given code index is
	 * part of the translation, for this stratum. If the code at the given index
	 * is not a part of the translation of the given stratum code, return the
	 * path of the primary input source file.
	 * 
	 * @param codeIndex
	 *            the index of the code.
	 * @param method
	 *            the method where is the code.
	 * @param stratumId
	 */
	protected String sourcePath(long codeIndex, MethodImpl method,
			String stratumId) throws AbsentInformationException {
		Stratum stratum = getStratum(stratumId);
		if (stratum != null) {
			FileInfo fileInfo = fileInfo(codeIndex, method, stratum);
			if (fileInfo != null) {
				String path = fileInfo.fAbsoluteFileName;
				if (path == null) {
					return getPath(fileInfo.fFileName);
				}
				return path;
			}
		}
		// Java stratum
		if (fSourceName == null) {
			getSourceName();
		}
		return getPath(fSourceName);
	}

	/**
	 * Return the number of the line of which the given code index is part of
	 * the translation, for this stratum.
	 * 
	 * @param codeIndex
	 *            the index of the code.
	 * @param method
	 *            the method where is the code.
	 * @param stratumId
	 */
	protected int lineNumber(long codeIndex, MethodImpl method, String stratumId) {
		Stratum stratum = getStratum(stratumId);
		try {
			if (stratum != null) {
				List<int[]> lineInfos = lineInfos(codeIndex, method, stratum);
				if (lineInfos != null) {
					return lineInfos.get(0)[1];
				}
				return LocationImpl.LINE_NR_NOT_AVAILABLE;
			}
			// Java stratum
			try {
				return method.javaStratumLineNumber(codeIndex);
			} catch (NativeMethodException e) { // Occurs in SUN VM.
				return LocationImpl.LINE_NR_NOT_AVAILABLE;
			}
		} catch (AbsentInformationException e) {
			return LocationImpl.LINE_NR_NOT_AVAILABLE;
		}
	}

	/**
	 * Return the location which are part of the translation of the given line,
	 * in the given stratum in the source file with the given source name. If
	 * sourceName is <code>null</code>, return the locations for all source file
	 * in the given stratum. The returned location are in the given method.
	 * 
	 * @param stratumId
	 *            the stratum id.
	 * @param sourceName
	 *            the name of the source file.
	 * @param lineNumber
	 *            the number of the line.
	 * @param method
	 * @throws AbsentInformationException
	 *             if the specified sourceName is not valid.
	 */
	public List<Location> locationsOfLine(String stratumId, String sourceName, int lineNumber, MethodImpl method)	throws AbsentInformationException {
		Stratum stratum = getStratum(stratumId);
		List<Integer> javaLines = new ArrayList<Integer>();
		if (stratum != null) {
			boolean found = false;
			for (Iterator<FileInfo> iter = stratum.fFileInfos.iterator(); iter.hasNext() && !found;) {
				FileInfo fileInfo = iter.next();
				if (sourceName == null || (found = sourceName.equals(fileInfo.fFileName))) {
					javaLines.addAll(fileInfo.getOutputLinesForLine(lineNumber));
				}
			}
			if (sourceName != null && !found) {
				throw new AbsentInformationException(JDIMessages.ReferenceTypeImpl_34);
			}
		} else { // Java stratum
			javaLines.add(new Integer(lineNumber));
		}
		return method.javaStratumLocationsOfLines(javaLines);
	}

	/**
	 * Return the locations of all lines in the given source file of the given
	 * stratum which are included in the given method. If sourceName is
	 * <code>null</code>, return the locations for all source file in the given
	 * stratum.
	 * 
	 * @param stratumId
	 *            the stratum id
	 * @param sourceName
	 *            the name of the source file
	 * @param method
	 * @param codeIndexTable
	 *            the list of code indexes for the method, as get from the
	 *            VM/JDWP
	 * @param javaStratumLineNumberTable
	 *            the list of line numbers in the java stratum for the method,
	 *            as get from the VM/JDWP
	 * @return
	 */
	public List<Location> allLineLocations(String stratumId, String sourceName,
			MethodImpl method, long[] codeIndexTable,
			int[] javaStratumLineNumberTable) throws AbsentInformationException {
		Stratum stratum = getStratum(stratumId);
		if (stratum != null) {
			int[][] lineInfoTable = new int[codeIndexTable.length][];
			if (sourceName == null) {
				int lastIndex = 0;
				for (int i = 0, length = javaStratumLineNumberTable.length; i < length; i++) {
					// for each executable line in the java source, get the
					// associated lines in the stratum source
					List<int[]> lineInfos = stratum.getInputLineInfos(javaStratumLineNumberTable[i]);
					if (lineInfos != null) {
						int[] lineInfo = lineInfos.get(0);
						if (!lineInfo.equals(lineInfoTable[lastIndex])) {
							lineInfoTable[i] = lineInfo;
							lastIndex = i;
						}
					}
				}
			} else { // sourceName != null
				FileInfo fileInfo = stratum.getFileInfo(sourceName);
				if (fileInfo == null) {
					throw new AbsentInformationException(JDIMessages.ReferenceTypeImpl_34);
				}
				int fileId = fileInfo.fFileId;
				int lastIndex = 0;
				for (int i = 0, length = javaStratumLineNumberTable.length; i < length; i++) {
					List<int[]> lineInfos = stratum.getInputLineInfos(javaStratumLineNumberTable[i]);
					if (lineInfos != null) {
						for (Iterator<int[]> iter = lineInfos.iterator(); iter.hasNext();) {
							int[] lineInfo = iter.next();
							if (lineInfo[0] == fileId) {
								if (!lineInfo.equals(lineInfoTable[lastIndex])) {
									lineInfoTable[i] = lineInfo;
									lastIndex = i;
								}
								break;
							}
						}
					}
				}
			}
			List<Location> locations = new ArrayList<Location>();
			for (int i = 0, length = lineInfoTable.length; i < length; i++) {
				if (lineInfoTable[i] != null) {
					locations.add(new LocationImpl(virtualMachineImpl(), method, codeIndexTable[i]));
				}
			}
			return locations;
		}
		// Java stratum
		List<Location> result = new ArrayList<Location>();
		for (long element : codeIndexTable) {
			result.add(new LocationImpl(virtualMachineImpl(), method, element));
		}
		return result;
	}

	/*
	 * @since 3.0
	 * 
	 * @since java 1.5
	 */
	public String genericSignature() {
		if (fGenericSignatureKnown) {
			return fGenericSignature;
		}
		if (virtualMachineImpl().isJdwpVersionGreaterOrEqual(1, 5)) {
			initJdwpRequest();
			try {
				JdwpReplyPacket replyPacket = requestVM(
						JdwpCommandPacket.RT_SIGNATURE_WITH_GENERIC, this);
				defaultReplyErrorHandler(replyPacket.errorCode());
				DataInputStream replyData = replyPacket.dataInStream();
				setSignature(readString("signature", replyData)); //$NON-NLS-1$
				fGenericSignature = readString("generic signature", replyData); //$NON-NLS-1$
				if (fGenericSignature.length() == 0) {
					fGenericSignature = null;
				}
				fGenericSignatureKnown = true;
			} catch (IOException e) {
				defaultIOExceptionHandler(e);
				return null;
			} finally {
				handledJdwpRequest();
			}
		} else {
			fGenericSignatureKnown = true;
		}
		return fGenericSignature;
	}

	/**
	 * if genericSignature is <code>null</code>, the generic signature is set to
	 * not-known (genericSignature() will ask the VM for the generic signature)
	 * if genericSignature is an empty String, the generic signature is set to
	 * no-generic-signature (genericSignature() will return null) if
	 * genericSignature is an non-empty String, the generic signature is set to
	 * the specified value (genericSignature() will return the specified value)
	 * 
	 * @since 3.0
	 */
	public void setGenericSignature(String genericSignature) {
		if (genericSignature == null) {
			fGenericSignature = null;
			fGenericSignatureKnown = false;
		} else {
			if (genericSignature.length() == 0) {
				fGenericSignature = null;
			} else {
				fGenericSignature = genericSignature;
			}
			fGenericSignatureKnown = true;
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ReferenceType#instances(long)
	 */
	public List<ObjectReference> instances(long maxInstances) {
		try {
			int max = (int) maxInstances;
			if (maxInstances >= Integer.MAX_VALUE) {
				max = Integer.MAX_VALUE;
			}
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			writeInt(max, "max instances", outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_INSTANCES, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_OBJECT:
			case JdwpReplyPacket.INVALID_CLASS:
				throw new ObjectCollectedException(
						JDIMessages.class_or_object_not_known);
			case JdwpReplyPacket.NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.ReferenceTypeImpl_27);
			case JdwpReplyPacket.ILLEGAL_ARGUMENT:
				throw new IllegalArgumentException(
						JDIMessages.ReferenceTypeImpl_26);
			case JdwpReplyPacket.VM_DEAD:
				throw new VMDisconnectedException(JDIMessages.vm_dead);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			int elements = readInt("element count", replyData); //$NON-NLS-1$
			if (max > 0 && elements > max) {
				elements = max;
			}
			ArrayList<ObjectReference> list = new ArrayList<ObjectReference>();
			for (int i = 0; i < elements; i++) {
				list.add((ObjectReference) ValueImpl.readWithTag(this, replyData));
			}
			return list;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @see com.sun.jdi.ReferenceType#majorVersion()
	 * @since 3.3
	 */
	public int majorVersion() {
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			getRefTypeID().write(outData);

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_CLASS_VERSION, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_CLASS:
			case JdwpReplyPacket.INVALID_OBJECT:
				throw new ObjectCollectedException(
						JDIMessages.class_or_object_not_known);
			case JdwpReplyPacket.ABSENT_INFORMATION:
				return 0;
			case JdwpReplyPacket.NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.ReferenceTypeImpl_no_class_version_support24);
			case JdwpReplyPacket.VM_DEAD:
				throw new VMDisconnectedException(JDIMessages.vm_dead);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			return readInt("major version", replyData); //$NON-NLS-1$
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @see com.sun.jdi.ReferenceType#minorVersion()
	 * @since 3.3
	 */
	public int minorVersion() {
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			getRefTypeID().write(outData);

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_CLASS_VERSION, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_CLASS:
			case JdwpReplyPacket.INVALID_OBJECT:
				throw new ObjectCollectedException(
						JDIMessages.class_or_object_not_known);
			case JdwpReplyPacket.ABSENT_INFORMATION:
				return 0;
			case JdwpReplyPacket.NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.ReferenceTypeImpl_no_class_version_support24);
			case JdwpReplyPacket.VM_DEAD:
				throw new VMDisconnectedException(JDIMessages.vm_dead);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			readInt("major version", replyData); //$NON-NLS-1$
			return readInt("minor version", replyData); //$NON-NLS-1$
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @see com.sun.jdi.ReferenceType#constantPoolCount()
	 * @since 3.3
	 */
	public int constantPoolCount() {
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			this.getRefTypeID().write(outData);

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_CONSTANT_POOL, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_CLASS:
			case JdwpReplyPacket.INVALID_OBJECT:
				throw new ObjectCollectedException(
						JDIMessages.class_or_object_not_known);
			case JdwpReplyPacket.ABSENT_INFORMATION:
				return 0;
			case JdwpReplyPacket.NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.ReferenceTypeImpl_no_constant_pool_support);
			case JdwpReplyPacket.VM_DEAD:
				throw new VMDisconnectedException(JDIMessages.vm_dead);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			return readInt("pool count", replyData); //$NON-NLS-1$
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @see com.sun.jdi.ReferenceType#constantPool()
	 * @since 3.3
	 */
	public byte[] constantPool() {
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			this.getRefTypeID().write(outData);

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.RT_CONSTANT_POOL, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_CLASS:
			case JdwpReplyPacket.INVALID_OBJECT:
				throw new ObjectCollectedException(
						JDIMessages.class_or_object_not_known);
			case JdwpReplyPacket.ABSENT_INFORMATION:
				return new byte[0];
			case JdwpReplyPacket.NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.ReferenceTypeImpl_no_constant_pool_support);
			case JdwpReplyPacket.VM_DEAD:
				throw new VMDisconnectedException(JDIMessages.vm_dead);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			readInt("pool count", replyData); //$NON-NLS-1$
			int bytes = readInt("byte count", replyData); //$NON-NLS-1$
			byte[] array = new byte[bytes];
			for (int i = 0; i < bytes; i++) {
				array[i] = readByte("byte read", replyData); //$NON-NLS-1$
			}
			return array;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
}
