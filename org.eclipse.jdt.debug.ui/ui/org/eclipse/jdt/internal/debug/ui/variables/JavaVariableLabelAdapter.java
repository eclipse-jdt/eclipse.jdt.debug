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
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @since 3.2
 *
 */
public class JavaVariableLabelAdapter extends VariableLabelAdapter {
	
	public static JDIModelPresentation fLabelProvider = new JDIModelPresentation();
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getValueText(org.eclipse.debug.core.model.IVariable, org.eclipse.debug.core.model.IValue)
	 */
	protected String getValueText(IVariable variable, IValue value, IPresentationContext context) throws CoreException {
		return fLabelProvider.getFormattedValueText((IJavaValue) value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getValueTypeName(org.eclipse.debug.core.model.IVariable, org.eclipse.debug.core.model.IValue)
	 */
	protected String getValueTypeName(IVariable variable, IValue value, IPresentationContext context) throws CoreException {
		if (!isShowQualfiiedNames(context)) {
			return fLabelProvider.removeQualifierFromGenericName(value.getReferenceTypeName());
		}
		return super.getValueTypeName(variable, value, context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getVariableTypeName(org.eclipse.debug.core.model.IVariable)
	 */
	protected String getVariableTypeName(IVariable variable, IPresentationContext context) throws CoreException {
		if (!isShowQualfiiedNames(context)) {
			return fLabelProvider.removeQualifierFromGenericName(variable.getReferenceTypeName());
		}
		return super.getVariableTypeName(variable, context);
	}

	private boolean isShowQualfiiedNames(IPresentationContext context) {
		IWorkbenchPart part = context.getPart();
		if (part != null) {
			return JDIDebugUIPlugin.getDefault().getPluginPreferences().getBoolean(part.getSite().getId() + "." + IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES);  //$NON-NLS-1$
		}
		return false;
	}
	

}
