package org.eclipse.jdi.hcr;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Thrown to indicate that the target VM refused to perform an operation.
 */
public class OperationRefusedException extends RuntimeException {
	public OperationRefusedException() {
	}
	
	public OperationRefusedException(String s) {
		super(s);
	}
}