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
public class StackFrameImpl extends MirrorImpl implements StackFrame, Locatable {
	/** FrameID that corresponds to this reference. */
	private JdwpFrameID fFrameID;
	/** Thread under which this frame's method is running. */
	private ThreadReferenceImpl fThread;
	/** Location of the current instruction in the frame. */
	private LocationImpl fLocation;
	
	/**
	 * Creates new StackFrameImpl.
	 */
	public StackFrameImpl(VirtualMachineImpl vmImpl, JdwpFrameID ID, ThreadReferenceImpl thread, LocationImpl location) {
		super("StackFrame", vmImpl);
		fFrameID = ID;
		fThread = thread;
		fLocation = location;
	}

	/**
	 * @return Returns the Value of a LocalVariable in this frame. 
	 */
	public Value getValue(LocalVariable variable) throws IllegalArgumentException, InvalidStackFrameException, VMMismatchException {
		ArrayList list = new ArrayList(1);
		list.add(variable);
		return (ValueImpl)getValues(list).get(variable);
	}
	
	/**
	 * @return Returns the values of multiple local variables in this frame.
	 */
	public Map getValues(List variables) throws IllegalArgumentException, InvalidStackFrameException, VMMismatchException {
		// Note that this information should not be cached.
		HashMap map = new HashMap();

		/*
		 * If 'this' is requested, we have to use a special JDWP request.
		 * Therefore, we remember the positions in the list of requests for 'this'.
		 */
		int sizeAll = variables.size();
		int sizeThis = 0;
		boolean[] isThisValue = new boolean[sizeAll];
		for (int i = 0; i < sizeAll; i++) {
			LocalVariableImpl var = (LocalVariableImpl)variables.get(i);
			isThisValue[i] = var.isThis();
			if (isThisValue[i])
				sizeThis++;
		}
		int sizeNotThis = sizeAll - sizeThis;
		
		if (sizeThis > 0) {
			Value thisValue = thisObject();
			for (int i = 0; i < sizeAll; i++) {
				if (isThisValue[i])
					map.put(variables.get(i), thisValue);
			}
		}
		
		// If only 'this' was requested, we're finished.
		if (sizeNotThis == 0)
			return map;
			
		// Request values for local variables other than 'this'.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithThread(this, outData);
			writeInt(sizeNotThis, "size", outData);
			for (int i = 0; i < sizeNotThis; i++) {
				LocalVariableImpl var = (LocalVariableImpl)variables.get(i);
				checkVM(var);
				writeInt(var.slot(), "slot", outData);
				writeByte(var.tag(), "tag", JdwpID.tagMap(), outData);
			}
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.SF_GET_VALUES, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
		
			DataInputStream replyData = replyPacket.dataInStream();
			int nrOfElements = readInt("elements", replyData);
			if (nrOfElements != sizeNotThis) 
				throw new InternalError("Retrieved a different number of values from the VM than requested.");
			
			for (int i = 0, j = 0; i < sizeAll; i++) {
				if (!isThisValue[i])
					map.put(variables.get(j++), ValueImpl.readWithTag(this, replyData));
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
	 * @return Returns the Location of the current instruction in the frame.
	 */
	public Location location() {
		return fLocation;
	}
	
	/**
	 * Sets the Value of a LocalVariable in this frame. 
	 */
	public void setValue(LocalVariable var, Value value) throws InvalidTypeException, ClassNotLoadedException {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			((ThreadReferenceImpl)thread()).write(this, outData);
			write(this, outData);
			writeInt(1, "size", outData);	// We only set one field
			checkVM(var);
			writeInt(((LocalVariableImpl)var).slot(), "slot", outData);
			if (value != null) {
				checkVM(value);
				((ValueImpl)value).writeWithTag(this, outData);
			} else {
				ValueImpl.writeNullWithTag(this, outData);
			}
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.SF_SET_VALUES, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.TYPE_MISMATCH:
					throw new InvalidTypeException();
				case JdwpReplyPacket.INVALID_CLASS:
					throw new ClassNotLoadedException(var.typeName());
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the value of 'this' for the current frame.
	 */
	public ObjectReference thisObject() throws InvalidStackFrameException {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeWithThread(this, outData);
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.SF_THIS_OBJECT, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
			ObjectReference result = ObjectReferenceImpl.readObjectRefWithTag(this, replyData);
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the thread under which this frame's method is running.
	 */
	public com.sun.jdi.ThreadReference thread() {
		return fThread;
	}
	
	/**
	 * @return Returns a LocalVariable that matches the given name and is visible at the current frame location.
	 */
	public LocalVariable visibleVariableByName(String name) throws AbsentInformationException {
		Iterator iter = visibleVariables().iterator();
		while (iter.hasNext()) {
			LocalVariableImpl var = (LocalVariableImpl)iter.next();
			if (var.name().equals(name))
				return var;
		}
		
		return null;
	}
	
	/**
	 * @return Returns the values of multiple local variables in this frame. 
	 */
	public List visibleVariables() throws AbsentInformationException {
		Iterator iter = fLocation.method().variables().iterator();
		Vector visibleVars = new Vector();
		while (iter.hasNext()) {
			LocalVariableImpl var = (LocalVariableImpl)iter.next();
			// Only return local variables other than the this pointer.
			if (var.isVisible(this) && !var.isThis())
				visibleVars.add(var);
		}
		return visibleVars;
	}
	
	/** 
	 * @return Returns the hash code value.
	 */
	public int hashCode() {
		return fThread.hashCode() + fFrameID.hashCode();
	}
	
	/**
	 * @return Returns true if two mirrors refer to the same entity in the target VM.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		return object != null && object.getClass().equals(this.getClass()) && fThread.equals(((StackFrameImpl)object).fThread) && fFrameID.equals(((StackFrameImpl)object).fFrameID);
	}
	
	/**
	 * Writes JDWP representation.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		fFrameID.write(out);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("stackFrame", fFrameID.value());
	}
	
	/**
	 * Writes JDWP representation.
	 */
	public void writeWithThread(MirrorImpl target, DataOutputStream out) throws IOException {
		fThread.write(target, out);
		write(target, out);
	}
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static StackFrameImpl readWithLocation(MirrorImpl target, ThreadReferenceImpl thread, DataInputStream in)  throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpFrameID ID = new JdwpFrameID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("stackFrame", ID.value());

		if (ID.isNull())
			return null;
		LocationImpl location = LocationImpl.read(target, in);
		if (location == null)
			return null;

		return new StackFrameImpl(vmImpl, ID, thread, location);
	}
}
