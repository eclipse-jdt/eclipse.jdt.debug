package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.MirrorImpl;

public class RequestID {
	/** Null request ID, returned by Virtual Machine in events that were not requested. */		
	private static final int NULL_REQUEST_ID = 0;
	public static final RequestID nullID = new RequestID(NULL_REQUEST_ID);

	/** Integer representation of request ID.*/
	private int fRequestID;
	
	/**
	 * Creates new request ID.
	 */
	private RequestID(int ID) {
		fRequestID = ID;
	}
	
	/**
	 * @return Returns whether the request ID is a NULL ID, which means that there is no corresponding request.
	 */
	public boolean isNull() {
		return fRequestID == NULL_REQUEST_ID;
	}
	
	/**
	 * @return Returns true if two RequestIDs are the same.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		return object != null && object.getClass().equals(this.getClass()) && fRequestID == ((RequestID)object).fRequestID;
	}
	
	/**
	 * @return Returns a has code for this object.
	 * @see java.lang.Object#hashCode
	 */
	public int hashCode() {
		return fRequestID;
	}
	
	/**
	 * @return Returns string representation.
	 */
	public String toString() {
		return new Long(fRequestID).toString();
	}

	/**
	 * Writes IDto stream.
	 */
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeInt(fRequestID, "request ID", out); //$NON-NLS-1$
	}
	
	/**
	 * @return Returns a new request ID read from stream.
	 */
	public static RequestID read(MirrorImpl target, DataInputStream in) throws IOException {
		int result = target.readInt("request ID", in); //$NON-NLS-1$
		return new RequestID(result);
	}
}