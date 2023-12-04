/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.model.elements.VariableLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

/**
 * Base implementation of a label provider for Java variables
 * @since 3.2
 */
public class JavaVariableLabelProvider extends VariableLabelProvider implements IPreferenceChangeListener {

	public static JDIModelPresentation fLabelProvider = new JDIModelPresentation();
	/**
	 * Map of view id to qualified name setting
	 */
	private final Map<String, Boolean> fQualifiedNameSettings = new HashMap<>();
	private boolean fQualifiedNames = false;

	/**
	 * Whether to use a thread rule for a label update job (serialize on thread)
	 */
	private int fSerializeMode = SERIALIZE_NONE;

	private static final int SERIALIZE_ALL = 0; // no toString()'s in line, so serialize labels
	private static final int SERIALIZE_NONE = 1; // all toString()'s in line, so don't serialize labels (evaluations will be serialized)
	private static final int SERIALIZE_SOME = 2; // some - only serialize those that don't have formatters (ones with formatters will be serialized by evaluation)

	public JavaVariableLabelProvider() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugUIPlugin.getUniqueIdentifier());
		if(prefs != null) {
			prefs.addPreferenceChangeListener(this);
			determineSerializationMode(prefs.get(IJDIPreferencesConstants.PREF_SHOW_DETAILS, IJDIPreferencesConstants.DETAIL_PANE));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getValueText(org.eclipse.debug.core.model.IVariable, org.eclipse.debug.core.model.IValue)
	 */
	@Override
	protected String getValueText(IVariable variable, IValue value, IPresentationContext context) throws CoreException {
		if (value instanceof IJavaValue) {
			return fLabelProvider.getFormattedValueText((IJavaValue) value);
		}
		return super.getValueText(variable, value, context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getValueTypeName(org.eclipse.debug.core.model.IVariable, org.eclipse.debug.core.model.IValue)
	 */
	@Override
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

	@Override
	protected FontData getFontData(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		var result = super.getFontData(elementPath, presentationContext, columnId);
		if (result == null) {
			return result;
		}
		var element = elementPath.getLastSegment();
		if (element instanceof IJavaVariable) {
			var variable = (IJavaVariable) element;
			var value = variable.getValue();
			if (value instanceof IJavaObject && ((IJavaObject) value).getLabel() != null) {
				return new FontData(result.getName(), result.getHeight(), result.getStyle() ^ SWT.BOLD);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getVariableTypeName(org.eclipse.debug.core.model.IVariable)
	 */
	@Override
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
	 * @return true if the presentation context is showing qualified names, false otherwise
	 */
	private Boolean isShowQualfiiedNames(IPresentationContext context) {
		Boolean qualified = fQualifiedNameSettings.get(context.getId());
		if (qualified == null) {
			qualified = Boolean.valueOf(Platform.getPreferencesService().getBoolean(
					JDIDebugUIPlugin.getUniqueIdentifier(),
					context.getId() + '.' + IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES,
					false,
					null));
			fQualifiedNameSettings.put(context.getId(), qualified);
		}
		return qualified;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.VariableLabelAdapter#getColumnText(org.eclipse.debug.core.model.IVariable, org.eclipse.debug.core.model.IValue, java.lang.String, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	@Override
	protected String getColumnText(IVariable variable, IValue value, IPresentationContext context, String columnId) throws CoreException {
		if (JavaVariableColumnPresentation.COLUMN_INSTANCE_ID.equals(columnId)) {
			if (value instanceof JDIObjectValue) {
				long uniqueId = ((JDIObjectValue)value).getUniqueId();
				if (uniqueId >= 0) {
					StringBuilder buffer = new StringBuilder();
					buffer.append(uniqueId);
					return buffer.toString();
				}
			}
			return ""; //$NON-NLS-1$
		}
		if (JavaVariableColumnPresentation.COLUMN_INSTANCE_COUNT.equals(columnId)) {
			if (value instanceof IJavaObject) {
				IJavaType jType = ((IJavaObject)value).getJavaType();
				if (jType == null && variable instanceof IJavaVariable) {
					jType = ((IJavaVariable)variable).getJavaType();
				}
				if (jType instanceof IJavaReferenceType) {
					if (!(jType instanceof IJavaInterfaceType)) {
						long count = ((IJavaReferenceType)jType).getInstanceCount();
						if (count == -1) {
							return DebugUIMessages.JavaVariableLabelProvider_0;
						}
						StringBuilder buffer = new StringBuilder();
						buffer.append(count);
						return buffer.toString();
					}
				}
			}
			return ""; //$NON-NLS-1$
		}
		if (JavaVariableColumnPresentation.COLUMN_LABEL.equals(columnId)) {
			if (value instanceof IJavaObject) {
				String label = ((IJavaObject) value).getLabel();
				if (label != null) {
					return label;
				}
			}
			return ""; //$NON-NLS-1$
		}
		return super.getColumnText(variable, value, context, columnId);
	}

	/**
	 * Sets qualified name setting before building label
	 */
	@Override
	protected void retrieveLabel(ILabelUpdate update) throws CoreException {
		Boolean showQ = isShowQualfiiedNames(update.getPresentationContext());
		fQualifiedNames = showQ.booleanValue();
		fLabelProvider.setAttribute(JDIModelPresentation.DISPLAY_QUALIFIED_NAMES, showQ);
		super.retrieveLabel(update);
	}

	/**
	 * Sets the serialization mode for label jobs based on the current preference setting.
	 *
	 * @param value preference value for PREF_SHOW_DETAILS
	 */
	private void determineSerializationMode(String value) {
		if (IJDIPreferencesConstants.INLINE_ALL.equals(value)) {
			fSerializeMode = SERIALIZE_NONE;
		} else if (IJDIPreferencesConstants.INLINE_FORMATTERS.equals(value)) {
			fSerializeMode = SERIALIZE_SOME;
		} else {
			fSerializeMode = SERIALIZE_ALL;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableLabelProvider#getLabel(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider#getRule(org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate)
	 */
	@Override
	protected ISchedulingRule getRule(ILabelUpdate update) {
		IJavaStackFrame frame = null;
		switch (fSerializeMode) {
		case SERIALIZE_NONE:
			return null;
		case SERIALIZE_ALL:
			Object input = update.getViewerInput();
			frame = (IJavaStackFrame) DebugPlugin.getAdapter(input, IJavaStackFrame.class);
			break;
		case SERIALIZE_SOME:
			Object element = update.getElement();
			if (element instanceof IJavaVariable) {
				try {
					IValue value = ((IJavaVariable)element).getValue();
					if (value instanceof IJavaValue) {
						if (!fLabelProvider.isShowLabelDetails((IJavaValue)value)) {
							input = update.getViewerInput();
							frame = (IJavaStackFrame) DebugPlugin.getAdapter(input, IJavaStackFrame.class);
						}
					}
				} catch (DebugException e) {

				}
			}
		}
		if (frame != null) {
			return ((JDIThread)frame.getThread()).getThreadRule();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
	 */
	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().endsWith(IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES)) {
			fQualifiedNameSettings.clear();
		} else if (event.getKey().equals(IJDIPreferencesConstants.PREF_SHOW_DETAILS)) {
			determineSerializationMode((String) event.getNewValue());
		}
	}
}