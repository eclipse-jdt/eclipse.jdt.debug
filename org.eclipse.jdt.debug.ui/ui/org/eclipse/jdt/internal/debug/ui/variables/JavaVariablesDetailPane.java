/*******************************************************************************
 *  Copyright (c) 2019 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.views.variables.details.DefaultDetailPane;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.ExpressionInformationControlCreator;
import org.eclipse.jdt.internal.debug.ui.JDIContentAssistPreference;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.propertypages.PropertyPageMessages;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Java Variable detail pane.
 *
 * @since 3.10
 */
public class JavaVariablesDetailPane extends DefaultDetailPane {

	/**
	 * Identifier for this Java Variable detail pane editor
	 */
	public static final String JAVA_VARIABLE_DETAIL_PANE_VARIABLES = JDIDebugUIPlugin.getUniqueIdentifier() + ".JAVA_VARIABLE_DETAIL_PANE_VARIABLES"; //$NON-NLS-1$
	public static final String NAME = PropertyPageMessages.JavaVariableDetailsPane_name;
	public static final String DESCRIPTION = PropertyPageMessages.JavaVariableDetailsPane_description;

	private FocusListener focusListener;
	private Composite fDetailPaneContainer;
	private Combo fExpressionHistory;
	private IDialogSettings fExpressionHistoryDialogSettings;
	private static final int MAX_HISTORY_SIZE = 20;
	private static final String DS_SECTION_EXPRESSION_HISTORY = "expressionHistory"; //$NON-NLS-1$
	private static final String DS_KEY_HISTORY_ENTRY_COUNT = "expressionHistoryEntryCount"; //$NON-NLS-1$
	private static final String DS_KEY_HISTORY_ENTRY_PREFIX = "expressionHistoryEntry_"; //$NON-NLS-1$
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\r\n|\r|\n"); //$NON-NLS-1$ ;

	private IJavaVariable fVariable;
	private IValue fValue;
	private boolean textModified = false;

	public JavaVariablesDetailPane() {
		fExpressionHistoryDialogSettings = DialogSettings.getOrCreateSection(JDIDebugUIPlugin.getDefault().getDialogSettings(), DS_SECTION_EXPRESSION_HISTORY);
	}

	@Override
	public Control createControl(Composite parent) {
		if (!isInView()) {
			Control c = super.createControl(parent);
			c.setBackground(ExpressionInformationControlCreator.getSystemBackgroundColor());
			return c;
		}
		Composite container = parent;
		if (fExpressionHistoryDialogSettings != null) {
			container = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
			fDetailPaneContainer = container;

			fExpressionHistory = SWTFactory.createCombo(container, SWT.DROP_DOWN | SWT.READ_ONLY, 1, null);
			fExpressionHistory.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int historyIndex = fExpressionHistory.getSelectionIndex() - 1;
					if (historyIndex >= 0 && getSourceViewer() != null) {
						getSourceViewer().getDocument().set(getExpressionHistory()[historyIndex]);
						textModified = true;
					}
				}
			});
			GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);

			fExpressionHistory.setLayoutData(data);
			fExpressionHistory.setEnabled(false);
		}

		Control newControl = super.createControl(container);
		SourceViewer viewer = getSourceViewer();
		// Light bulb for content assist hint
		ControlDecoration decoration = new ControlDecoration(viewer.getControl(), SWT.TOP | SWT.LEFT);
		decoration.setShowOnlyOnFocus(true);
		FieldDecoration dec = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		decoration.setImage(dec.getImage());
		decoration.setDescriptionText(JDIContentAssistPreference.getContentAssistDescription());

		focusListener = new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				updateExpressionHistories();
				fValue = null;
			}
			@Override
			public void focusGained(FocusEvent e) {
				fValue = null;
				try {
					if (fVariable != null) {
						fValue = fVariable.getValue();
					}
				} catch (DebugException ex) {
				}
			}
		};
		viewer.getTextWidget().addFocusListener(focusListener);
		viewer.getTextWidget().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				textModified = true;
			}
		});
		return newControl;
	}

	/**
	 * Initializes the Expression history drop-down with values.
	 */
	private void initializeExpressionHistoryDropDown() {
		fExpressionHistory.setItems(getExpressionHistoryLabels());
		String userHint = PropertyPageMessages.JavaVariableDetailsPane_choosePreviousExpression;
		fExpressionHistory.add(userHint, 0);
		fExpressionHistory.setText(userHint);
	}

	/**
	 * Returns the Expression history labels for the current variale.
	 *
	 * @return an array of strings containing the Expression history labels
	 */
	private String[] getExpressionHistoryLabels() {
		String[] expressions = getExpressionHistory();
		String[] labels = new String[expressions.length];
		for (int i = 0; i < expressions.length; i++) {
			labels[i] = NEWLINE_PATTERN.matcher(expressions[i]).replaceAll(" "); //$NON-NLS-1$
		}
		return labels;
	}

	/**
	 * Returns the Expression history entries for the current variable.
	 *
	 * @return an array of strings containing the history of Expressions
	 */
	private String[] getExpressionHistory() {
		// Get global history
		return readExpressionHistory(fExpressionHistoryDialogSettings);
	}

	/**
	 * Updates the local and global Expression histories.
	 */
	private void updateExpressionHistories() {
		String newItem = getSourceViewer().getDocument().get();
		if (newItem.length() == 0 || fValue == null) {
			return;
		}

		String oldValue = fValue.toString();
		if (oldValue.charAt(0) == '"' && oldValue.charAt(oldValue.length() - 1) == '"') {
			oldValue = oldValue.substring(1, oldValue.length() - 1);
		}
		if (!textModified || newItem.equals(oldValue)) {
			return;
		}
		textModified = false;
		// Update global history
		String[] globalItems = readExpressionHistory(fExpressionHistoryDialogSettings);
		if (globalItems.length > 0 && newItem.equals(globalItems[0])) {
			return;
		}

		if (globalItems.length == 0) {
			globalItems = new String[1];
		} else {
			String[] tempItems = new String[globalItems.length + 1];
			System.arraycopy(globalItems, 0, tempItems, 1, globalItems.length);
			globalItems = tempItems;
		}
		globalItems[0] = newItem;
		storeExpressionHistory(globalItems, fExpressionHistoryDialogSettings);
	}

	/**
	 * Reads the Expression history from the given dialog settings.
	 *
	 * @param dialogSettings
	 *            the dialog settings
	 * @return the Expression history
	 */
	private static String[] readExpressionHistory(IDialogSettings dialogSettings) {
		int count = 0;
		try {
			count = dialogSettings.getInt(DS_KEY_HISTORY_ENTRY_COUNT);
		} catch (NumberFormatException ex) {
			// No history yet
		}
		count = Math.min(count, MAX_HISTORY_SIZE);
		String[] expressions = new String[count];
		for (int i = 0; i < count; i++) {
			expressions[i] = dialogSettings.get(DS_KEY_HISTORY_ENTRY_PREFIX + i);
		}
		return expressions;
	}

	/**
	 * Writes the given Expressions into the given dialog settings.
	 *
	 * @param expressions
	 *            an array of strings containing the Expressions
	 * @param dialogSettings
	 *            the dialog settings
	 */
	private static void storeExpressionHistory(String[] expressions, IDialogSettings dialogSettings) {
		List<String> uniqueExpressions = new ArrayList<>(new LinkedHashSet<>(Arrays.asList(expressions)));
		final int length = Math.min(uniqueExpressions.size(), MAX_HISTORY_SIZE);
		int count = 0;
		for (String expression : uniqueExpressions) {
			dialogSettings.put(DS_KEY_HISTORY_ENTRY_PREFIX + count, expression);
			count++;
			if (count >= length) {
				break;
			}
		}
		dialogSettings.put(DS_KEY_HISTORY_ENTRY_COUNT, count);
	}


	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getID() {
		return JAVA_VARIABLE_DETAIL_PANE_VARIABLES;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void display(IStructuredSelection selection) {
		if (fExpressionHistory != null && selection != null && selection.getFirstElement() instanceof IJavaVariable) {
			IJavaVariable variable = (IJavaVariable) (selection.getFirstElement());
			if (fVariable == null || !fVariable.equals(variable)) {
				fVariable = variable;
				fExpressionHistory.setEnabled(true);
				initializeExpressionHistoryDropDown();
			}
		}
		super.display(selection);
	}

	/**
	 * Clears the Java variable detail viewer, removes all text.
	 */
	@Override
	protected void clearSourceViewer(){
		fVariable = null;
		if (fExpressionHistory != null) {
			fExpressionHistory.setEnabled(false);
		}
		super.clearSourceViewer();
	}

	@Override
	public void dispose() {
		if (fExpressionHistory != null) {
			fExpressionHistory.dispose();
		}
		if (fDetailPaneContainer != null) {
			fDetailPaneContainer.dispose();
		}
		if (focusListener != null && getSourceViewer() != null && getSourceViewer().getTextWidget() != null) {
			getSourceViewer().getTextWidget().removeFocusListener(focusListener);
		}
		super.dispose();
	}
}

