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
public class MirrorImpl implements Mirror {
	/** Description of Mirror object. */
	protected String fDescription;
	/** Virtual Machine of Mirror object. */
	private VirtualMachineImpl fVirtualMachineImpl;
	/** VerboseWriter where verbose info is written to, null if no verbose must be given. */
	protected VerboseWriter fVerboseWriter = null;
	/** True if a Jdwp request has been sent to the VM and the response is not yet (fully) processed. */
	private boolean fPendingJdwpRequest = false;

	/**
	 * Constructor only to be used by Virtual Machine objects: stores description of Mirror object and Virtual Machine.
	 */	
	public MirrorImpl(String description) {
		fDescription = description;
		fVirtualMachineImpl = (VirtualMachineImpl)this;
		PrintWriter writer = ((VirtualMachineManagerImpl)org.eclipse.jdi.Bootstrap.virtualMachineManager()).verbosePrintWriter();
		if (writer != null)
			fVerboseWriter = new VerboseWriter(writer);
	}

	/**
	 * Constructor stores description of Mirror object and its Virtual Machine.
	 */	
	public MirrorImpl(String description, VirtualMachineImpl virtualMachineImpl) {
		fVirtualMachineImpl = virtualMachineImpl;
		fDescription = description;
		PrintWriter writer = ((VirtualMachineManagerImpl)org.eclipse.jdi.Bootstrap.virtualMachineManager()).verbosePrintWriter();
		if (writer != null)
			fVerboseWriter = new VerboseWriter(writer);
	}
	
	/**
	 * @return Returns description of Mirror object.
	 */
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
			fVerboseWriter.println("Received event set");
			fVerboseWriter.println("length", commandPacket.getLength());
			fVerboseWriter.println("id", commandPacket.getId());
			fVerboseWriter.println("flags", commandPacket.getFlags(), JdwpPacket.flagVector());
			fVerboseWriter.println("command set", (byte)(commandPacket.getCommand() >>> 8));
			fVerboseWriter.println("command", (byte)commandPacket.getCommand());
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
	 * Processing before each Jdwp request.
	 * Note that this includes building the request message and parsing the response.
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
			fVerboseWriter.print("Sending command (");
			fVerboseWriter.printValue(command, JdwpCommandPacket.commandMap());
			fVerboseWriter.println(")");
			fVerboseWriter.println("length", commandPacket.getLength());
			fVerboseWriter.println("id", commandPacket.getId());
			fVerboseWriter.println("flags", commandPacket.getFlags(), JdwpPacket.flagVector());
			fVerboseWriter.println("command set", (byte)(command >>> 8));
			fVerboseWriter.println("command", (byte)command);
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
	 * @return Returns reply data.
	 */
	public JdwpReplyPacket requestVM(int command, byte[] outData) {
		JdwpCommandPacket commandPacket = new JdwpCommandPacket(command);
		commandPacket.setData(outData);
		fVirtualMachineImpl.packetSendManager().sendPacket(commandPacket);
		fPendingJdwpRequest = true;
		writeVerboseCommandPacketHeader(commandPacket);

		JdwpReplyPacket reply = fVirtualMachineImpl.packetReceiveManager().getReply(commandPacket);
		if (fVerboseWriter != null) {
			fVerboseWriter.println();
			fVerboseWriter.println("Received reply");
			fVerboseWriter.println("length", reply.getLength());
			fVerboseWriter.println("id", reply.getId());
			fVerboseWriter.println("flags", reply.getFlags(), JdwpPacket.flagVector());
			fVerboseWriter.println("error code", reply.errorCode(), JdwpReplyPacket.errorMap());
		}
		
		return reply;
	}
	
	/**
	 * Performs a VM request.
	 * @return Returns reply data.
	 */
	public JdwpReplyPacket requestVM(int command, ByteArrayOutputStream outData) {
			return requestVM(command, outData.toByteArray());
	}
	
	/**
	 * Performs a VM request for a specified object.
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
	 * @return Returns reply data.
	 */
	public JdwpReplyPacket requestVM(int command) {
		return requestVM(command, (byte[])null);
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
			default:
				throw new InternalException("Got error code in reply: " + error);
		}
	}

	/**
	 * Performs default handling of IOException in creating or interpreting a Jdwp packet.
	 */
	public void defaultIOExceptionHandler(Exception e) {
		throw new InternalException("Got invalid data: " + e);
	}
	
	/**
	 * Waits for a specified command packet from the VM.
	 * @return Returns Command Packet from VM.
	 */
	public final JdwpCommandPacket getCommandVM(int command, long timeout) throws InterruptedException {
		return fVirtualMachineImpl.packetReceiveManager().getCommand(command, timeout);
	}
	
	/**
	 * @exception VMMismatchException is thrown if the Mirror argument and this mirror do not belong to the same VirtualMachine.
	 */
	public void checkVM(Mirror mirror) throws VMMismatchException {
		if (((MirrorImpl)mirror).virtualMachineImpl() != this.virtualMachineImpl())
			throw new VMMismatchException();
	}
	
	/**
	 * Disconnects VM.
	 */
	public void disconnectVM() {
		fVirtualMachineImpl.packetSendManager().disconnectVM();
	}

	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public byte readByte(String description, DataInputStream in) throws IOException {
		byte result = in.readByte();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public short readShort(String description, DataInputStream in) throws IOException {
		short result = in.readShort();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public int readInt(String description, DataInputStream in) throws IOException {
		int result = in.readInt();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public long readLong(String description, DataInputStream in) throws IOException {
		long result = in.readLong();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public byte readByte(String description, Map valueToString, DataInputStream in) throws IOException {
		byte result = in.readByte();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, valueToString);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public short readShort(String description, Map valueToString, DataInputStream in) throws IOException {
		short result = in.readShort();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, valueToString);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public int readInt(String description, Map valueToString, DataInputStream in) throws IOException {
		int result = in.readInt();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, valueToString);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public byte readByte(String description, Vector bitNames, DataInputStream in) throws IOException {
		byte result = in.readByte();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, bitNames); 
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public short readShort(String description, Vector bitNames, DataInputStream in) throws IOException {
		short result = in.readShort();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, bitNames);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public int readInt(String description, Vector bitNames, DataInputStream in) throws IOException {
		int result = in.readInt();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result, bitNames);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public String readString(String description, DataInputStream in) throws IOException {
		String result = JdwpString.read(in);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public boolean readBoolean(String description, DataInputStream in) throws IOException {
		boolean result = in.readBoolean();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public char readChar(String description, DataInputStream in) throws IOException {
		char result = in.readChar();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public double readDouble(String description, DataInputStream in) throws IOException {
		double result = in.readDouble();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public float readFloat(String description, DataInputStream in) throws IOException {
		float result = in.readFloat();
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, result);
		}
		return result;
	}
	
	/**
	 * Reads Jdwp data and, if verbose is on, outputs verbose info.
	 * @return Returns value that has been read.
	 */
	public byte[] readByteArray(int length, String description, DataInputStream in) throws IOException {
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
	public void writeByte(byte value, String description, DataOutputStream out) throws IOException {
		out.writeByte(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeShort(short value, String description, DataOutputStream out) throws IOException {
		out.writeShort(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeInt(int value, String description, DataOutputStream out) throws IOException {
		out.writeInt(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeLong(long value, String description, DataOutputStream out) throws IOException {
		out.writeLong(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}

	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeByte(byte value, String description, Map valueToString, DataOutputStream out) throws IOException {
		out.writeByte(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, valueToString);
		}
	}
	
	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeShort(short value, String description, Map valueToString, DataOutputStream out) throws IOException {
		out.writeShort(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, valueToString);
		}
	}
	
	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeInt(int value, String description, Map valueToString, DataOutputStream out) throws IOException {
		out.writeInt(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, valueToString);
		}
	}
	
	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeByte(byte value, String description, Vector bitNames, DataOutputStream out) throws IOException {
		out.writeByte(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, bitNames);
		}
	}
	
	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeShort(short value, String description, Vector bitNames, DataOutputStream out) throws IOException {
		out.writeShort(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, bitNames);
		}
	}
	
	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeInt(int value, String description, Vector bitNames, DataOutputStream out) throws IOException {
		out.writeInt(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value, bitNames);
		}
	}
	
	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeString(String value, String description, DataOutputStream out) throws IOException {
		JdwpString.write(value, out);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}
	
	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeBoolean(boolean value, String description, DataOutputStream out) throws IOException {
		out.writeBoolean(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}
	
	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeChar(char value, String description, DataOutputStream out) throws IOException {
		out.writeChar(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}
	
	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeDouble(double value, String description, DataOutputStream out) throws IOException {
		out.writeDouble(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}
	
	/**
	 * Writes Jdwp data and, if verbose is on, outputs verbose info.
	 */
	public void writeFloat(float value, String description, DataOutputStream out) throws IOException {
		out.writeFloat(value);
		if (fVerboseWriter != null) {
			fVerboseWriter.println(description, value);
		}
	}
	
	/** 
	 * @return Returns VerboseWriter where verbose info is written to, null if no verbose must be given.
	 */
	public VerboseWriter verboseWriter() {
		return fVerboseWriter;
	}	
}
