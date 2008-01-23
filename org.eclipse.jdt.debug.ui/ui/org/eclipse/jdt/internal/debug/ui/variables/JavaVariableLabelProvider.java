/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.model.elements.VariableLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jface.viewers.TreePath;

/**
 * Base implementation of a label provider for Java variables
 * @since 3.2
 */
public class JavaVariableLabelProvider extends VariableLabelProvider implements IPropertyChangeListener {
	
	public static JDIModelPresentation fLabelProvider = new JDIModelPresentation();
	/**
	 * Map of view id to qualified name setting
	 */
	private Map fQualifiedNameSettings = new HashMap();
	private boolean fQualifiedNames = false;
	
	public JavaVariableLabelProvider() {
		JDIDebugUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getValueText(org.eclipse.debug.core.model.IVariable, org.eclipse.debug.core.model.IValue)
	 */
	protected String getValueText(IVariable variable, IValue value, IPresentationContext context) throws CoreException {
		if (value instanceof IJavaValue) {
			return fLabelProvider.getFormattedValueText((IJavaValue) value);
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
			if (!fQualifiedNames) {
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
			if (!fQualifiedNames) {
				return fLabelProvider.removeQualifierFromGenericName(typeName);
			}
		} catch (DebugException e) {}
		return typeName;		
	}

	/**
	 * Returns if the the specified presentation context is showing qualified names or not
	 * @param context
	 * @return true if the presentation context is showing qualified names, false otherwise
	 */
	private Boolean isShowQualfiiedNames(IPresentationContext context) {
		Boolean qualified = (Boolean) fQualifiedNameSettings.get(context.getId());
		if (qualified == null) {
			qualified = Boolean.valueOf(JDIDebugUIPlugin.getDefault().getPluginPreferences().getBoolean(context.getId() + '.' + IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES));
			fQualifiedNameSettings.put(context.getId(), qualified);
		}
		return qualified;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getColumnText(org.eclipse.debug.core.model.IVariable, org.eclipse.debug.core.model.IValue, java.lang.String, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected String getColumnText(IVariable variable, IValue value, IPresentationContext context, String columnId) throws CoreException {
		if (JavaVariableColumnPresentation.COLUMN_INSTANCE_ID.equals(columnId)) {
			if (value instanceof JDIObjectValue) {
				long uniqueId = ((JDIObjectValue)value).getUniqueId();
				if (uniqueId >= 0) {
					StringBuffer buffer = new StringBuffer();
					buffer.append(uniqueId);
					return buffer.toString();
				}
			}
			return ""; //$NON-NLS-1$
		}
		return super.getColumnText(variable, value, context, columnId);
	}

	/**
	 * Sets qualified name setting before building label
	 */
	protected void retrieveLabel(ILabelUpdate update) throws CoreException {
		Boolean showQ = isShowQualfiiedNames(update.getPresentationContext());
		fQualifiedNames = showQ.booleanValue();
		fLabelProvider.setAttribute(JDIModelPresentation.DISPLAY_QUALIFIED_NAMES, showQ);
		super.retrieveLabel(update);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences$IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().endsWith(IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES)) {
			fQualifiedNameSettings.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableLabelProvider#getLabel(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	protected String getLabel(TreePath elementPath, IPresentationContext context, String columnId) throws CoreException {
		if (columnId == null) {
			// when no columns, handle special escaping ourselves
			IDebugModelPresentation presentation = getModelPresentation(context, JDIDebugModel.getPluginIdentifier());
			if (presentation != null) {
				return presentation.getText(elementPath.getLastSegment());
			}
		}
		return super.getLabel(elementPath, context, columnId);
	}
	
	
}
