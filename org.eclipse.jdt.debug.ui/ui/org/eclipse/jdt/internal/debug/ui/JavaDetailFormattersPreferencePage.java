package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class JavaDetailFormattersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	static public final String DETAIL_FORMATTER_IS_ENABLED= "1"; //$NON-NLS-1$
	static public final String DETAIL_FORMATTER_IS_DISABLED= "0"; //$NON-NLS-1$
	
	private CheckboxTableViewer fFormatterListViewer;
	private Button fAddFormatterButton;
	private Button fRemoveFormatterButton;
	private Button fEditFormatterButton;
	private SourceViewer fCodeViewer;
	private Label fTableLabel;
	
	private FormatterListViewerContentProvider fFormatViewerContentProvider;
	
	public JavaDetailFormattersPreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setDescription(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.Management_of_detail_formatters._3")); //$NON-NLS-1$
	}

	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
//		WorkbenchHelp.setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_STEP_FILTER_PREFERENCE_PAGE);
		return createDetailFormatsPreferences(parent);	
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Create a group to contain the detail formatters related widgetry
	 */
	private Control createDetailFormatsPreferences(Composite parent) {
		// top level container
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);
		
		//table label
		fTableLabel= new Label(container, SWT.NONE);
		fTableLabel.setText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.Defined_&types__4")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fTableLabel.setLayoutData(gd);

		fFormatterListViewer= CheckboxTableViewer.newCheckList(container, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = (Table)fFormatterListViewer.getControl();
		gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);
		fFormatViewerContentProvider= new FormatterListViewerContentProvider(fFormatterListViewer);
		fFormatterListViewer.setContentProvider(fFormatViewerContentProvider);
		fFormatterListViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof DetailFormatter) {
					return ((DetailFormatter)element).getTypeName();
				}
				return null;
			}
		});
		fFormatterListViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				((DetailFormatter)event.getElement()).setEnabled(event.getChecked());
			}
		});
		fFormatterListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updatePage((IStructuredSelection)event.getSelection());
			}
		});
		fFormatterListViewer.setInput(this);

		createDetailFormatsButtons(container);

		createSourceViewer(container);
		
		fFormatViewerContentProvider.refreshViewer();
		
		return container;
	}
	

	private void createDetailFormatsButtons(Composite container) {
		// button container
		Composite buttonContainer = new Composite(container, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonContainer.setLayout(buttonLayout);
		
		// Add type button
		fAddFormatterButton = new Button(buttonContainer, SWT.PUSH);
		fAddFormatterButton.setText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.Add_&Formatter..._5")); //$NON-NLS-1$
		fAddFormatterButton.setToolTipText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.Allow_you_to_create_a_new_detail_formatter_6")); //$NON-NLS-1$
		gd = getButtonGridData(fAddFormatterButton);
		fAddFormatterButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(fAddFormatterButton);
		fAddFormatterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addType();
			}
		});
		
		// Remove button
		fRemoveFormatterButton = new Button(buttonContainer, SWT.PUSH);
		fRemoveFormatterButton.setText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.&Remove_7")); //$NON-NLS-1$
		fRemoveFormatterButton.setToolTipText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.Remove_all_selected_detail_formatters_8")); //$NON-NLS-1$
		gd = getButtonGridData(fRemoveFormatterButton);
		fRemoveFormatterButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(fRemoveFormatterButton);
		fRemoveFormatterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				removeTypes();
			}
		});
		fRemoveFormatterButton.setEnabled(false);
		
		// Edit button
		fEditFormatterButton = new Button(buttonContainer, SWT.PUSH);
		fEditFormatterButton.setText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.&Edit..._9")); //$NON-NLS-1$
		fEditFormatterButton.setToolTipText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.Edit_the_selected_detail_formatter_10")); //$NON-NLS-1$
		gd = getButtonGridData(fEditFormatterButton);
		fEditFormatterButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(fEditFormatterButton);
		fEditFormatterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				editType();
			}
		});
		fEditFormatterButton.setEnabled(false);
		
	}

	private GridData getButtonGridData(Button button) {
		GridData gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		GC gc = new GC(button);
		gc.setFont(button.getFont());
		FontMetrics fontMetrics= gc.getFontMetrics();
		gc.dispose();
		int widthHint= Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
		gd.widthHint= Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		
		gd.heightHint= Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_HEIGHT);
		return gd;
	}
	
	public void createSourceViewer(Composite container) {
		fCodeViewer= new SourceViewer(container,  null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
		IDocument document= new Document();
		IDocumentPartitioner partitioner= tools.createDocumentPartitioner();
		document.setDocumentPartitioner(partitioner);
		partitioner.connect(document);		
		fCodeViewer.configure(new JavaSourceViewerConfiguration(tools, null));
		fCodeViewer.setEditable(false);
		fCodeViewer.setDocument(document);
		fCodeViewer.getTextWidget().setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	
		Font font= JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
		fCodeViewer.getTextWidget().setFont(font);
		
		Control control= fCodeViewer.getControl();
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan= 2;
		gd.heightHint= convertHeightInCharsToPixels(10);
		control.setLayoutData(gd);
	}

	private void updatePage(IStructuredSelection selection) {
		fRemoveFormatterButton.setEnabled(!selection.isEmpty());
		fEditFormatterButton.setEnabled(selection.size() == 1);
		updateFormatViewer(selection);
	}

	private void updateFormatViewer(IStructuredSelection selection) {
		if (selection.size() == 1) {
			fCodeViewer.getDocument().set(((DetailFormatter)selection.getFirstElement()).getSnippet());
		} else {
			fCodeViewer.getDocument().set(""); //$NON-NLS-1$
		}
	}

	public void addType() {
		DetailFormatter detailFormat= new DetailFormatter("", "", true); //$NON-NLS-1$ //$NON-NLS-2$
		if (new DetailFormatterDialog(getShell(), detailFormat, false).open() == Window.OK) {
			fFormatViewerContentProvider.addDetailFormat(detailFormat);
		}
	}
	
	public void removeTypes() {
		fFormatViewerContentProvider.removeDetailFormat((DetailFormatter)((IStructuredSelection)fFormatterListViewer.getSelection()).getFirstElement());
	}
	
	public void editType() {
		IStructuredSelection selection= (IStructuredSelection)fFormatterListViewer.getSelection();
		if (new DetailFormatterDialog(getShell(), (DetailFormatter)(selection).getFirstElement(), true).open() == Window.OK) {
			fFormatterListViewer.refresh();
			fFormatViewerContentProvider.refreshViewer();
			updatePage(selection);
		}
	}
	
	public boolean performOk() {
		fFormatViewerContentProvider.saveDetailFormatters();
		JDIDebugUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	class FormatterListViewerContentProvider implements IStructuredContentProvider {
		
		private List fDetailFormattersList;
		
		private CheckboxTableViewer fViewer;
		
		/**
		 * FormatterListViewerContentProvider constructor.
		 */
		public FormatterListViewerContentProvider(CheckboxTableViewer viewer) {
			fViewer= viewer;
			// load the current formatters
			String[] detailFormattersList= JavaDebugOptionsManager.parseList(JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST));
			fDetailFormattersList= new ArrayList(detailFormattersList.length / 3);
			for (int i= 0, length= detailFormattersList.length; i < length;) {
				String typeName= detailFormattersList[i++];
				String snippet= detailFormattersList[i++].replace('\u0000', ',');
				boolean enabled= ! DETAIL_FORMATTER_IS_DISABLED.equals(detailFormattersList[i++]);
				DetailFormatter detailFormatter= new DetailFormatter(typeName, snippet, enabled);
				fDetailFormattersList.add(detailFormatter);
			}
		}
		
		/**
		 * Save the detail formatter list.		 */
		public void saveDetailFormatters() {
			String[] values= new String[fDetailFormattersList.size() * 3];
			int i= 0;
			for (Iterator iter= fDetailFormattersList.iterator(); iter.hasNext();) {
				DetailFormatter detailFormatter= (DetailFormatter) iter.next();
				values[i++]= detailFormatter.getTypeName();
				values[i++]= detailFormatter.getSnippet().replace(',','\u0000');
				values[i++]= detailFormatter.isEnabled() ? DETAIL_FORMATTER_IS_ENABLED : DETAIL_FORMATTER_IS_DISABLED;
			}
			String pref = JavaDebugOptionsManager.serializeList(values);
			getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST, pref);
			
		}
		
		/**
		 * Add a detail formatter.		 */
		public void addDetailFormat(DetailFormatter detailFormatter) {
			fDetailFormattersList.add(detailFormatter);
			fViewer.refresh();
			refreshViewer();
			IStructuredSelection selection= new StructuredSelection(detailFormatter);
			fViewer.setSelection(selection);
			updatePage(selection);
		}
		
		/**
		 * Remove a detailFormatter		 */
		public void removeDetailFormat(DetailFormatter detailFormatter) {
			fDetailFormattersList.remove(detailFormatter);
			fViewer.refresh();
			IStructuredSelection selection= new StructuredSelection();
			fViewer.setSelection(selection);
			updatePage(selection);
		}
		
		/**
		 * Refresh the formatter list viewer. 		 */
		private void refreshViewer() {
			DetailFormatter[] checkedElementsTmp= new DetailFormatter[fDetailFormattersList.size()];
			int i= 0;
			for (Iterator iter= fDetailFormattersList.iterator(); iter.hasNext();) {
				DetailFormatter detailFormatter= (DetailFormatter) iter.next();
				if (detailFormatter.isEnabled()) {
					checkedElementsTmp[i++]= detailFormatter;
				}
			}
			DetailFormatter[] checkedElements= new DetailFormatter[i];
			System.arraycopy(checkedElementsTmp, 0, checkedElements, 0, i);
			fViewer.setAllChecked(false);
			fViewer.setCheckedElements(checkedElements);
		}
		
		/**
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return fDetailFormattersList.toArray();
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

}
