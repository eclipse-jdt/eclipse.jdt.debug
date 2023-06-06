/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.filtertable.Filter;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.ButtonLabel;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.DialogLabels;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.FilterStorage;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.FilterTableConfig;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class ExceptionFilterEditor {

	protected static final String DEFAULT_PACKAGE = "(default package)"; //$NON-NLS-1$
	private IJavaExceptionBreakpoint fBreakpoint;

	private JavaFilterTable fJavaFilterTable;

	public ExceptionFilterEditor(Composite parent, JavaExceptionBreakpointAdvancedPage page) {
		fBreakpoint = (IJavaExceptionBreakpoint) page.getBreakpoint();
		// top level container
		Composite outer = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		outer.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		outer.setLayoutData(gd);
		outer.setFont(parent.getFont());
		// filter table
		Label label= new Label(outer, SWT.NONE);
		label.setText(PropertyPageMessages.ExceptionFilterEditor_5);
		label.setFont(parent.getFont());
		gd= new GridData();
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);

		TableLayout tableLayout = new TableLayout();
		ColumnLayoutData[] columnLayoutData = new ColumnLayoutData[1];
		columnLayoutData[0] = new ColumnWeightData(100);
		tableLayout.addColumnData(columnLayoutData[0]);

		createFilterTable(outer);
	}

	private void createFilterTable(Composite outer) {
		fJavaFilterTable = new JavaFilterTable(new FilterStorage() {

			@Override
			public void setStoredFilters(IPreferenceStore store, Filter[] filters) {
				List<String> inclusionFilters = new ArrayList<>(filters.length);
				List<String> exclusionFilters = new ArrayList<>(filters.length);
				for (Filter filter : filters) {
					String name = filter.getName();
					if (name.equals(DEFAULT_PACKAGE)) {
						name = ""; //$NON-NLS-1$
					}
					if (filter.isChecked()) {
						inclusionFilters.add(name);
					} else {
						exclusionFilters.add(name);
					}
				}
				try {
					fBreakpoint.setInclusionFilters(inclusionFilters.toArray(new String[inclusionFilters.size()]));
					fBreakpoint.setExclusionFilters(exclusionFilters.toArray(new String[exclusionFilters.size()]));
				} catch (CoreException ce) {
					JDIDebugUIPlugin.log(ce);
				}
			}

			@Override
			public Filter[] getStoredFilters(boolean defaults) {
				if (defaults) {
					return new Filter[0];
				}
				try {
					String[] iFilters = fBreakpoint.getInclusionFilters();
					String[] eFilters = fBreakpoint.getExclusionFilters();
					List<Filter> storedFilters = new ArrayList<>(iFilters.length + eFilters.length);
					for (String filter : iFilters) {
						storedFilters.add(toFilter(filter, true));
					}
					for (String filter : eFilters) {
						storedFilters.add(toFilter(filter, false));
					}
					return storedFilters.toArray(Filter[]::new);
				} catch (CoreException ce) {
					JDIDebugUIPlugin.log(ce);
					return new Filter[0];
				}

			}
		}, new FilterTableConfig()
				.setAddFilter(new ButtonLabel(PropertyPageMessages.ExceptionFilterEditor_6, PropertyPageMessages.ExceptionFilterEditor_7))
				.setAddType(new ButtonLabel(PropertyPageMessages.ExceptionFilterEditor_8, PropertyPageMessages.ExceptionFilterEditor_9))
				.setAddPackage(new ButtonLabel(PropertyPageMessages.ExceptionFilterEditor_10, PropertyPageMessages.ExceptionFilterEditor_11))
				.setRemove(new ButtonLabel(PropertyPageMessages.ExceptionFilterEditor_12, PropertyPageMessages.ExceptionFilterEditor_13))
				.setAddPackageDialog(new DialogLabels(PropertyPageMessages.ExceptionFilterEditor_15, PropertyPageMessages.ExceptionFilterEditor_18))
				.setAddTypeDialog(new DialogLabels(PropertyPageMessages.ExceptionFilterEditor_19, PropertyPageMessages.ExceptionFilterEditor_22))
				.setErrorAddTypeDialog(new DialogLabels(PropertyPageMessages.ExceptionFilterEditor_19, PropertyPageMessages.ExceptionFilterEditor_20)));
		fJavaFilterTable.createTable(outer);
	}

	private Filter toFilter(String stringFilter, boolean include) {
		if (stringFilter.length() == 0) {
			return new Filter(DEFAULT_PACKAGE, include);
		}
		return new Filter(stringFilter, include);
	}

	protected void doStore() {
		fJavaFilterTable.performOk(null);
	}

}
