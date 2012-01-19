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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdi.internal.jdwp.JdwpThreadGroupID;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ThreadGroupReferenceImpl extends ObjectReferenceImpl implements
		ThreadGroupReference {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.THREAD_GROUP_TAG;
	/**
	 * The cached name of this thread group. This value is safe to cache because
	 * there is no API for changing the name of a ThreadGroup.
	 */
	private String fName;
	/**
	 * The cached parent of this thread group. Once set, this value cannot be
	 * changed
	 */
	private ThreadGroupReference fParent = fgUnsetParent;
	private static ThreadGroupReferenceImpl fgUnsetParent = new ThreadGroupReferenceImpl(
			null, null);

	/**
	 * Creates new ThreadGroupReferenceImpl.
	 */
	public ThreadGroupReferenceImpl(VirtualMachineImpl vmImpl,
			JdwpThreadGroupID threadGroupID) {
		super("ThreadGroupReference", vmImpl, threadGroupID); //$NON-NLS-1$
	}

	/**
	 * @returns Value tag.
	 */
	@Override
	public byte getTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadGroupReference#name()
	 */
	public String name() {
		if (fName != null) {
			return fName;
		}
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.TGR_NAME,
					this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fName = readString("name", replyData); //$NON-NLS-1$
			return fName;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadGroupReference#parent()
	 */
	public ThreadGroupReference parent() {
		if (fParent != fgUnsetParent) {
			return fParent;
		}
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TGR_PARENT, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fParent = ThreadGroupReferenceImpl.read(this, replyData);
			return fParent;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadGroupReference#resume()
	 */
	public void resume() {
		Iterator<ThreadReference> iter = allThreads().iterator();
		while (iter.hasNext()) {
			ThreadReference thr = iter.next();
			thr.resume();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadGroupReference#suspend()
	 */
	public void suspend() {
		Iterator<ThreadReference> iter = allThreads().iterator();
		while (iter.hasNext()) {
			ThreadReference thr = iter.next();
			thr.suspend();
		}
	}

	/**
	 * Inner class used to return children info.
	 */
	private class ChildrenInfo {
		List<ThreadReference> childThreads;
		List<ThreadGroupReference> childThreadGroups;
	}

	/**
	 * @return Returns a List containing each ThreadReference in this thread
	 *         group.
	 */
	public ChildrenInfo childrenInfo() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TGR_CHILDREN, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			ChildrenInfo result = new ChildrenInfo();
			int nrThreads = readInt("nr threads", replyData); //$NON-NLS-1$
			result.childThreads = new ArrayList<ThreadReference>(nrThreads);
			for (int i = 0; i < nrThreads; i++)
				result.childThreads.add(ThreadReferenceImpl.read(this,
						replyData));
			int nrThreadGroups = readInt("nr thread groups", replyData); //$NON-NLS-1$
			result.childThreadGroups = new ArrayList<ThreadGroupReference>(nrThreadGroups);
			for (int i = 0; i < nrThreadGroups; i++)
				result.childThreadGroups.add(ThreadGroupReferenceImpl.read(
						this, replyData));
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadGroupReference#threadGroups()
	 */
	public List<ThreadGroupReference> threadGroups() {
		return childrenInfo().childThreadGroups;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadGroupReference#threads()
	 */
	public List<ThreadReference> threads() {
		return childrenInfo().childThreads;
	}

	/**
	 * @return Returns a List containing each ThreadGroupReference in this
	 *         thread group and all of its subgroups.
	 */
	private List<ThreadReference> allThreads() {
		ChildrenInfo info = childrenInfo();
		List<ThreadReference> result = info.childThreads;
		Iterator<ThreadGroupReference> iter = info.childThreadGroups.iterator();
		while (iter.hasNext()) {
			ThreadGroupReferenceImpl tg = (ThreadGroupReferenceImpl) iter.next();
			result.addAll(tg.allThreads());
		}
		return result;
	}

	/**
	 * @return Returns description of Mirror object.
	 */
	@Override
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
	public static ThreadGroupReferenceImpl read(MirrorImpl target,
			DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpThreadGroupID ID = new JdwpThreadGroupID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("threadGroupReference", ID.value()); //$NON-NLS-1$

		if (ID.isNull())
			return null;

		ThreadGroupReferenceImpl mirror = (ThreadGroupReferenceImpl) vmImpl
				.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new ThreadGroupReferenceImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		return mirror;
	}
}
