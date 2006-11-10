/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.model.elements.VariableLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

/**
 * @since 3.2
 *
 */
public class JavaVariableLabelProvider extends VariableLabelProvider {
	
	public static JDIModelPresentation fLabelProvider = new JDIModelPresentation();
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getValueText(org.eclipse.debug.core.model.IVariable, org.eclipse.debug.core.model.IValue)
	 */
	protected String getValueText(IVariable variable, IValue value, IPresentationContext context) throws CoreException {
		if (value instanceof IJavaValue) {
			return escapeSpecialChars(fLabelProvider.getFormattedValueText((IJavaValue) value));
		}
		return super.getValueText(variable, value, context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getValueTypeName(org.eclipse.debug.core.model.IVariable, org.eclipse.debug.core.model.IValue)
	 */
	protected String getValueTypeName(IVariable variable, IValue value, IPresentationContext context) throws CoreException {
		String typeName= DebugUIMessages.JDIModelPresentation_unknown_type__2;
		try {
			typeName = value.getReferenceTypeName();
			if (!isShowQualfiiedNames(context)) {
				return fLabelProvider.removeQualifierFromGenericName(typeName);
			}
		} catch (DebugException e) {}
		return typeName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getVariableTypeName(org.eclipse.debug.core.model.IVariable)
	 */
	protected String getVariableTypeName(IVariable variable, IPresentationContext context) throws CoreException {
		String typeName= DebugUIMessages.JDIModelPresentation_unknown_type__2;
		try {
			typeName = variable.getReferenceTypeName();
			if (!isShowQualfiiedNames(context)) {
				return fLabelProvider.removeQualifierFromGenericName(typeName);
			}
		} catch (DebugException e) {}
		return typeName;		
	}

	private boolean isShowQualfiiedNames(IPresentationContext context) {
		return JDIDebugUIPlugin.getDefault().getPluginPreferences().getBoolean(context.getId() + "." + IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES);  //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getColumnText(org.eclipse.debug.core.model.IVariable, org.eclipse.debug.core.model.IValue, java.lang.String, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected String getColumnText(IVariable variable, IValue value, IPresentationContext context, String columnId) throws CoreException {
		if (JavaVariableColumnPresentation.COLUMN_INSTANCE_ID.equals(columnId)) {
			if (value instanceof JDIObjectValue) {
				long uniqueId = ((JDIObjectValue)value).getUniqueId();
				StringBuffer buffer = new StringBuffer();
				buffer.append(uniqueId);
				return buffer.toString();
			} else {
				return ""; //$NON-NLS-1$
			}
		}
		return super.getColumnText(variable, value, context, columnId);
	}
	
	

}
