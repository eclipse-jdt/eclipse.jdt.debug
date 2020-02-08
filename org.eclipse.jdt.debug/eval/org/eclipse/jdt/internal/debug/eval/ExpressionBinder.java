package org.eclipse.jdt.internal.debug.eval;

import org.eclipse.jdt.core.dom.IVariableBinding;

public interface ExpressionBinder {
	// Record and register the binding specified
	void bind(IVariableBinding bind, String asVariableName);
}
