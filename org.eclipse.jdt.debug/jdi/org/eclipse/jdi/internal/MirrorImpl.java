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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpPacket;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdi.internal.jdwp.JdwpString;
import org.eclipse.jdt.internal.debug.core.JDIDebugOptions;

import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.Mirror;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VMMismatchException;
import com.sun.jdi.VMOutOfMemoryException;
import com.sun.jdi.VirtualMachine;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class MirrorImpl implements Mirror {

	/** Description of Mirror object. */
	protected String fDescription;
	/** Virtual Machine of Mirror object. */
	private VirtualMachineImpl fVirtualMachineImpl;
	/**
	 * VerboseWriter where verbose info is written to, null if no verbose must
	 * be given.
	 */
	protected VerboseWriter fVerboseWriter = null;
	/**
	 * True if a Jdwp request has been sent to the VM and the response is not
	 * yet (fully) processed.
	 */
	private boolean fPendingJdwpRequest = false;

	/**
	 * Constructor only to be used by Virtual Machine objects: stores
	 * description of Mirror object and Virtual Machine.
	 */
	public MirrorImpl(String description) {
		fDescription = description;
		fVirtualMachineImpl = (VirtualMachineImpl) this;
		PrintWriter writer = ((VirtualMachineManagerImpl) org.eclipse.jdi.Bootstrap
				.virtualMachineManager()).verbosePrintWriter();
		if (writer != null)
			fVerboseWriter = new VerboseWriter(writer);
	}

	/**
	 * Constructor stores description of Mirror object and its Virtual Machine.
	 */
	public MirrorImpl(String description, VirtualMachineImpl virtualMachineImpl) {
		fVirtualMachineImpl = virtualMachineImpl;
		fDescription = description;
		PrintWriter writer = ((VirtualMachineManagerImpl) org.eclipse.jdi.Bootstrap
				.virtualMachineManager()).verbosePrintWriter();
		if (writer != null)
			fVerboseWriter = new VerboseWriter(writer);
	}

	/**
	 * @return Returns description of Mirror object.
	 */
	@Override
	public String toString() {
		return fDescription;
	}

	/**
	 * @return Returns Virtual Machine of Mirror object.
	 */
	public VirtualMachine virtualMachine() {
		return fVirtualMachineImpl;
	}

	/**
	 * @return Returns Virtual Machine implementation of Mirror object.
	 */
	public VirtualMachineImpl virtualMachineImpl() {
		return fVirtualMachineImpl;
	}

	/**
	 * Processing before each Jdwp event.
	 */
	public void initJdwpEventSet(JdwpCommandPacket commandPacket) {
		if (fVerboseWriter != null) {
			fVerboseWriter.println("Received event set"); //$NON-NLS-1$
			fVerboseWriter.println("length", commandPacket.getLength()); //$NON-NLS-1$
			fVerboseWriter.println("id", commandPacket.getId()); //$NON-NLS-1$
			fVerboseWriter.println(
					"flags", commandPacket.getFlags(), JdwpPacket.getFlagMap()); //$NON-NLS-1$
			fVerboseWriter.println(
					"command set", (byte) (commandPacket.getCommand() >>> 8)); //$NON-NLS-1$
			fVerboseWriter
					.println("command", (byte) commandPacket.getCommand()); //$NON-NLS-1$
		}
	}

	/**
	 * Processing after each Jdwp Event.
	 */
	public void handledJdwpEventSet() {
		if (fVerboseWriter != null) {
			fVerboseWriter.println();
			fVerboseWriter.flush();
		}
	}

	/**
	 * Processing before each Jdwp request. Note that this includes building the
	 * request message and parsing the response.
	 */
	public void initJdwpRequest() {
		if (fVerboseWriter != null) {
			fVerboseWriter.gotoPosition(6);
		}
	}

	/**
	 * Writes command packet header if verbose is on.
	 */
	public void writeVerboseCommandPacketHeader(JdwpCommandPacket commandPacket) {
		if (fVerboseWriter != null) {
			int command = commandPacket.getCommand();
			int currentPosition = fVerboseWriter.position();
			fVerboseWriter.gotoPosition(0);
			fVerboseWriter.print("Sending command ("); //$NON-NLS-1$
			fVerboseWriter.printValue(command, JdwpCommandPacket.commandMap());
			fVerboseWriter.println(")"); //$NON-NLS-1$
			fVerboseWriter.println("length", commandPacket.getLength()); //$NON-NLS-1$
			fVerboseWriter.println("id", commandPacket.getId()); //$NON-NLS-1$
			fVerboseWriter.println(
					"flags", commandPacket.getFlags(), JdwpPacket.getFlagMap()); //$NON-NLS-1$
			fVerboseWriter.println("command set", (byte) (command >>> 8)); //$NON-NLS-1$
			fVerboseWriter.println("command", (byte) command); //$NON-NLS-1$
			fVerboseWriter.gotoPosition(currentPosition);
		}
	}

	/**
	 * Processing after each Jdwp Request.
	 */
	public void handledJdwpRequest() {
		if (fVerboseWriter != null && fPendingJdwpRequest) {
			fVerboseWriter.println();
			fVerboseWriter.flush();
		}
		fPendingJdwpRequest = false;
	}

	/**
	 * Performs a VM request.
	 * 
	 * @return Returns reply data.
	 */
	public JdwpReplyPacket requestVM(int command, byte[] outData) {
		JdwpCommandPacket commandPacket = new JdwpCommandPacket(command);
		commandPacket.setData(outData);
		long sent = System.currentTimeMillis();
		fVirtualMachineImpl.packetSendManager().sendPacket(commandPacket);
		fPendingJdwpRequest = true;
		writeVerboseCommandPacketHeader(commandPacket);

		JdwpReplyPacket reply = fVirtualMachineImpl.packetReceiveManager()
				.getReply(commandPacket);
		long recieved = System.currentTimeMillis();
		if (JDIDebugOptions.DEBUG_JDI_REQUEST_TIMES) {
			StringBuffer buf = new StringBuffer();
			buf.append(JDIDebugOptions.FORMAT.format(new Date(sent)));
			buf.append(" JDI Request: "); //$NON-NLS-1$
			buf.append(commandPacket.toString());
			buf.append("\n\tResponse Time: "); //$NON-NLS-1$
			buf.append(recieved - sent);
			buf.append("ms"); //$NON-NLS-1$
			buf.append(" length: "); //$NON-NLS-1$
			buf.append(reply.getLength());
			JDIDebugOptions.trace(buf.toString());
		}
		if (fVerboseWriter != null) {
			fVerboseWriter.println();
			fVerboseWriter.println("Received reply"); //$NON-NLS-1$
			fVerboseWriter.println("length", reply.getLength()); //$NON-NLS-1$
			fVerboseWriter.println("id", reply.getId()); //$NON-NLS-1$
			fVerboseWriter.println(
					"flags", reply.getFlags(), JdwpPacket.getFlagMap()); //$NON-NLS-1$
			fVerboseWriter
					.println(
							"error code", reply.errorCode(), JdwpReplyPacket.errorMap()); //$NON-NLS-1$
		}

		return reply;
	}

	/**
	 * Performs a VM request.
	 * 
	 * @return Returns reply data.
	 */
	public JdwpReplyPacket requestVM(int command, ByteArrayOutputStream outData) {
		return requestVM(command, outData.toByteArray());
	}

	/**
	 * Performs a VM request for a specified object.
	 * 
	 * @return Returns reply data.
	 */
	public JdwpReplyPacket requestVM(int command, ObjectReferenceImpl object) {
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		DataOutputStream dataOutStream = new DataOutputStream(byteOutStream);
		try {
			object.write(this, dataOutStream);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		}
		return requestVM(command, byteOutStream);
	}

	/**
	 * Performs a VM request for a specified object.
	 * 
	 * @return Returns reply data.
	 */
	public JdwpReplyPacket requestVM(int command, ReferenceTypeImpl refType) {
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		DataOutputStream dataOutStream = new DataOutputStream(byteOutStream);
		try {
			refType.write(this, dataOutStream);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		}
		return requestVM(command, byteOutStream);
	}

	/**
	 * Performs a VM request.
	 * 
	 * @return Returns reply data.
	 */
	public JdwpReplyPacket requestVM(int command) {
		return requestVM(command, (byte[]) null);
	}

	/**
	 * Performs default error handling.
	 */
	public void defaultReplyErrorHandler(int error) {
		switch (error) {
		case JdwpReplyPacket.NONE:
			break;
		case JdwpReplyPacket.INVALID_OBJECT:
			throw new ObjectCollectedException();
		case JdwpReplyPacket.INVALID_CLASS:
			throw new ClassNotPreparedException();
		case JdwpReplyPacket.CLASS_NOT_PREPARED:
			throw new ClassNotPreparedException();
		case JdwpReplyPacket.OUT_OF_MEMORY:
			throw new VMOutOfMemoryException();
		case JdwpReplyPacket.ILLEGAL_ARGUMENT:
			throw new IllegalArgumentException();
		case JdwpReplyPacket.NATIVE_METHOD:
			throw new NativeMethodException();
		case JdwpReplyPacket.INVALID_FRAMEID:
			throw new InvalidStackFrameException();
		case JdwpReplyPacket.NOT_IMPLEMENTED:
			throw new UnsupportedOperationException();
		case JdwpReplyPacket.HCR_OPERATION_REFUSED:
			throw new org.eclipse.jdi.hcr.OperationRefusedException();
		case JdwpReplyPacket.VM_DEAD:
			throw new VMDisconnectedException();
		default:
			throw new InternalException(
					JDIMessages.MirrorImpl_Got_error_code_in_reply___1 + error,
					error);
		}
	}

	/**
	 * Performs default handling of IOException in creating or interpreting a
	 * Jdwp packet.
	 */
	public void defaultIOExceptionHandler(Exception e) {
		throw new InternalException(JDIMessages.MirrorImpl_Got_invalid_data___2
				+ e);
	}

	/**
	 * Waits for a specified command packet from the VM.
	 * 
	 * @return Returns Command Packet from VM.
	 */
	public final JdwpCommandPacket getCommandVM(int command, long timeout)
			throws InterruptedException {
		return fVirtualMachineImpl.packetReceiveManager().getCommand(command,
				timeout);
	}

	/**
	 * @exception VMMismatchException
	 *                is thrown if the Mirror argument and this mirror do not
	 *                belong to the same VirtualMachine.
	 */
	public void checkVM(Mirror mirror) throws VMMismatchException {
		if (((MirrorImpl) mirror).virtualMachineImpl() != this
				.virtualMachineImpl())
			throw new VMMismatchException();
	}

	/**
	 * Disconnects VM.
	 */
	public void disconnectVM() {
		fVirtualMachineImpl.setDisconnected(true);
		fVirtualMachineImpl.packetSendManager().disconnectVM();
		fVirtualMachineImpl.packetReceiveManager().disconnectVM();
		((VirtualMachineManagerImpl) Bootstrap.virtualMachineManager())
				.removeConnectedVM(fVirtualMachineImpl);
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public byte readByte(String description, DataInputStream in)
			throws IOException {
		byte result = in.readByte();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public short readShort(String description, DataInputStream in)
			throws IOException {
		short result = in.readShort();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public int readInt(String description, DataInputStream in)
			throws IOException {
		int result = in.readInt();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public long readLong(String description, DataInputStream in)
			throws IOException {
		long result = in.readLong();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public byte readByte(String description, Map<Integer, String> valueToString,
			DataInputStream in) throws IOException {
		byte result = in.readByte();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, valueToString);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public short readShort(String description, Map<Integer, String> valueToString,
			DataInputStream in) throws IOException {
		short result = in.readShort();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, valueToString);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public int readInt(String description, Map<Integer, String> valueToString, DataInputStream in)
			throws IOException {
		int result = in.readInt();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, valueToString);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public String readString(String description, DataInputStream in)
			throws IOException {
		String result = JdwpString.read(in);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public boolean readBoolean(String description, DataInputStream in)
			throws IOException {
		boolean result = in.readBoolean();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public char readChar(String description, DataInputStream in)
			throws IOException {
		char result = in.readChar();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public double readDouble(String description, DataInputStream in)
			throws IOException {
		double result = in.readDouble();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public float readFloat(String description, DataInputStream in)
			throws IOException {
		float result = in.readFloat();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public byte[] readByteArray(int length, String description,
			DataInputStream in) throws IOException {
		byte[] result = new byte[length];
		in.readFully(result);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeByte(byte value, String description, DataOutputStream out)
			throws IOException {
		out.writeByte(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeShort(short value, String description, DataOutputStream out)
			throws IOException {
		out.writeShort(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeInt(int value, String description, DataOutputStream out)
			throws IOException {
		out.writeInt(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeLong(long value, String description, DataOutputStream out)
			throws IOException {
		out.writeLong(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeByte(byte value, String description, Map<Integer, String> valueToString,
			DataOutputStream out) throws IOException {
		out.writeByte(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, valueToString);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeShort(short value, String description, Map<Integer, String> valueToString,
			DataOutputStream out) throws IOException {
		out.writeShort(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, valueToString);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeInt(int value, String description, Map<Integer, String> valueToString,
			DataOutputStream out) throws IOException {
		out.writeInt(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, valueToString);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeString(String value, String description,
			DataOutputStream out) throws IOException {
		JdwpString.write(value, out);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeBoolean(boolean value, String description,
			DataOutputStream out) throws IOException {
		out.writeBoolean(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeChar(char value, String description, DataOutputStream out)
			throws IOException {
		out.writeChar(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeDouble(double value, String description,
			DataOutputStream out) throws IOException {
		out.writeDouble(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeFloat(float value, String description, DataOutputStream out)
			throws IOException {
		out.writeFloat(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeShort(short value, String description, String[] bitNames,
			DataOutputStream out) throws IOException {
		out.writeShort(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, bitNames);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeInt(int value, String description, String[] bitNames,
			DataOutputStream out) throws IOException {
		out.writeInt(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, bitNames);
		}
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public byte readByte(String description, String[] bitNames,
			DataInputStream in) throws IOException {
		byte result = in.readByte();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, bitNames);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public short readShort(String description, String[] bitNames,
			DataInputStream in) throws IOException {
		short result = in.readShort();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, bitNames);
		}
		return result;
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * 
	 * @return Returns value that has been read.
	 */
	public int readInt(String description, String[] bitNames, DataInputStream in)
			throws IOException {
		int result = in.readInt();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, bitNames);
		}
		return result;
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeByte(byte value, String description, String[] bitNames,
			DataOutputStream out) throws IOException {
		out.writeByte(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, bitNames);
		}
	}

	/**
	 * @return Returns VerboseWriter where verbose info is written to, null if
	 *         no verbose must be given.
	 */
	public VerboseWriter verboseWriter() {
		return fVerboseWriter;
	}
}
