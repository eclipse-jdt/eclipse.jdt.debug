package org.eclipse.jdi.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
public class ThreadGroupReferenceImpl extends ObjectReferenceImpl implements ThreadGroupReference {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.THREAD_GROUP_TAG;
	
	/**
	 * Creates new ThreadGroupReferenceImpl.
	 */
	public ThreadGroupReferenceImpl(VirtualMachineImpl vmImpl, JdwpThreadGroupID threadGroupID) {
		super("ThreadGroupReference", vmImpl, threadGroupID);
	}

	/**
	 * @returns Value tag.
	 */
	public byte getTag() {
		return tag;
	}
	
	/**
	 * @return Returns the name of this thread group.
	 */
	public String name() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.TGR_NAME, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			return readString("name", replyData);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * @return Returns the parent of this thread group., or null if there isn't.
	 */
	public ThreadGroupReference parent() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.TGR_PARENT, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			return ThreadGroupReferenceImpl.read(this, replyData);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}
		
	/**
	 * Resumes all threads in this thread group (including subgroups).
	 */
	public void resume() {
		Iterator iter = allThreads().iterator();
		while (iter.hasNext()) {
			ThreadReferenceImpl thr = (ThreadReferenceImpl)iter.next();
			thr.resume();
		}
	}
	
	/**
	 * Suspends all threads in this thread group (including subgroups).
	 */
	public void suspend()  {
		Iterator iter = allThreads().iterator();
		while (iter.hasNext()) {
			ThreadReferenceImpl thr = (ThreadReferenceImpl)iter.next();
			thr.suspend();
		}
	}
	
	/** 
	 * Inner class used to return children info.
	 */
	private class ChildrenInfo {
		List childThreads;
		List childThreadGroups;
	}
		
	/**
	 * @return Returns a List containing each ThreadReference in this thread group. 
	 */
	public ChildrenInfo childrenInfo() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.TGR_CHILDREN, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			ChildrenInfo result = new ChildrenInfo();
			int nrThreads = readInt("nr threads", replyData);
			result.childThreads = new ArrayList(nrThreads);
			for (int i = 0; i < nrThreads; i++)
				result.childThreads.add(ThreadReferenceImpl.read(this, replyData));
			int nrThreadGroups = readInt("nr thread groups", replyData);
			result.childThreadGroups = new ArrayList(nrThreadGroups);
			for (int i = 0; i < nrThreadGroups; i++)
				result.childThreads.add(ThreadGroupReferenceImpl.read(this, replyData));
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns a List containing each ThreadGroupReference in this thread group. 
	 */
	public List threadGroups() {
		return childrenInfo().childThreadGroups;
	}
	
	/**
	 * @return Returns a List containing each ThreadReference in this thread group. 
	 */
	public List threads() {
		return childrenInfo().childThreads;
	}
		
	/**
	 * @return Returns a List containing each ThreadGroupReference in this thread group and all of
	 * its subgroups.
	 */
	private List allThreads() {
		ChildrenInfo info = childrenInfo();
		List result = info.childThreads;
		Iterator iter = info.childThreadGroups.iterator();
		while (iter.hasNext()) {
			ThreadGroupReferenceImpl tg = (ThreadGroupReferenceImpl)iter.next();
			result.addAll(tg.allThreads());
		}
		return result;
	}
	
	/**
	 * @return Returns description of Mirror object.
	 */
	public String toString() {
		try {
			return name();
		} catch (Exception e) {
			return fDescription;
		}
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ThreadGroupReferenceImpl read(MirrorImpl target, DataInputStream in)  throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpThreadGroupID ID = new JdwpThreadGroupID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("threadGroupReference", ID.value());

		if (ID.isNull())
			return null;
			
		ThreadGroupReferenceImpl mirror = new ThreadGroupReferenceImpl(vmImpl, ID);
		return mirror;
	}
}
