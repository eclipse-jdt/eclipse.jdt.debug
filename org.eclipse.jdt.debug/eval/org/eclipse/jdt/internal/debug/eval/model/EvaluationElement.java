package org.eclipse.jdt.internal.debug.eval.model;

import org.eclipse.debug.core.model.IDebugElement;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
/**
 * A proxy to an underlying java debug model element
 * used for evaluation.
 */
public abstract class EvaluationElement {
	
	/**
	 * Returns the underlying Java debug model element
	 */
	protected abstract Object getUnderlyingModelObject();

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof EvaluationElement) {
			return getUnderlyingModelObject().equals(((EvaluationElement)o).getUnderlyingModelObject());
		}
		return false;
	}
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getUnderlyingModelObject().hashCode();
	}	
}

