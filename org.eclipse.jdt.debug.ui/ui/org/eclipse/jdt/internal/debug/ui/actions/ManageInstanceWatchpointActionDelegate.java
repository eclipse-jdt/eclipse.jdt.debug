package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;

public class ManageInstanceWatchpointActionDelegate extends ManageWatchpointActionDelegate {
	
	private IJavaFieldVariable fField;
	
	protected IJavaBreakpoint createBreakpoint(IResource resource, String typeName, String fieldName, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
		if (fField != null) {
			IJavaBreakpoint bp = super.createBreakpoint(resource, typeName, fieldName, lineNumber, charStart, charEnd, hitCount, register, attributes);
			bp.addInstanceFilter((IJavaObject)fField.getValue());
			return bp;
		}
		return null;
	}
	
	/**
	 * Cache the field variable for later use.
	 */
	protected IField getField(IJavaFieldVariable variable) {
		fField= variable;
		return super.getField(variable);
	}

}
