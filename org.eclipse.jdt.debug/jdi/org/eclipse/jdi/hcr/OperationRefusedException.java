package org.eclipse.jdi.hcr;/*
 * JDI Interface Specification class. 
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
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