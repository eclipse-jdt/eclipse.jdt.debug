/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.ui.help.WorkbenchHelp;

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
		setDescription(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.Override_default___toString()___for_Variables_and_Expressions_view_details._1")); //$NON-NLS-1$
	}

	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		WorkbenchHelp.setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_DETAIL_FORMATTER_PREFERENCE_PAGE);
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
		Font font = parent.getFont();
		initializeDialogUnits(parent);
		
		// top level container
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);
		container.setFont(font);
		
		//table label
		fTableLabel= new Label(container, SWT.NONE);
		fTableLabel.setText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.&Types_with_detail_formatters__2")); //$NON-NLS-1$
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fTableLabel.setLayoutData(gd);
		fTableLabel.setFont(font);

		fFormatterListViewer= CheckboxTableViewer.newCheckList(container, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = (Table)fFormatterListViewer.getControl();
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(10);
		gd.widthHint= convertWidthInCharsToPixels(10);
		table.setLayoutData(gd);
		table.setFont(font);
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
		fFormatterListViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editType();
			}
		}); 
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					removeTypes();
				}
			}
		});	
		fFormatterListViewer.setInput(this);

		createDetailFormatsButtons(container);

		Label label = new Label(container, SWT.NONE);
		label.setText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.Detail_formatter_code_snippet_defined_for_selected_type__3")); //$NON-NLS-1$
		label.setFont(font);
		createSourceViewer(container);
		
		fFormatViewerContentProvider.refreshViewer();
		return container;
	}
	

	private void createDetailFormatsButtons(Composite container) {
		Font font = container.getFont();
		
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
		fAddFormatterButton.setLayoutData(gd);
		fAddFormatterButton.setFont(font);
		setButtonLayoutData(fAddFormatterButton);
		fAddFormatterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addType();
			}
		});
		
		// Remove button
		fRemoveFormatterButton = new Button(buttonContainer, SWT.PUSH);
		fRemoveFormatterButton.setText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.&Remove_7")); //$NON-NLS-1$
		fRemoveFormatterButton.setToolTipText(DebugUIMessages.getString("JavaDetailFormattersPreferencePage.Remove_all_selected_detail_formatters_8")); //$NON-NLS-1$
		fRemoveFormatterButton.setFont(font);
		setButtonLayoutData(fRemoveFormatterButton);
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
		fEditFormatterButton.setFont(font);
		setButtonLayoutData(fEditFormatterButton);
		fEditFormatterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				editType();
			}
		});
		fEditFormatterButton.setEnabled(false);
		
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
	
		fCodeViewer.getTextWidget().setFont(JFaceResources.getTextFont());
		
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
		if (new DetailFormatterDialog(getShell(), detailFormat, fFormatViewerContentProvider.getDefinedTypes(), false).open() == Window.OK) {
			fFormatViewerContentProvider.addDetailFormatter(detailFormat);
		}
	}
	
	public void removeTypes() {
		Object[] all = fFormatViewerContentProvider.getElements(null);
		IStructuredSelection selection= (IStructuredSelection)fFormatterListViewer.getSelection();
		Object first = selection.getFirstElement();
		int index = -1;
		for (int i = 0; i < all.length; i++) {
			Object object = all[i];
			if (object.equals(first)) {
				index = i;
				break;
			}
		}
		fFormatViewerContentProvider.removeDetailFormatters(selection.toArray());
		all = fFormatViewerContentProvider.getElements(null);
		if (index > all.length - 1) {
			index = all.length - 1;
		}
		if (index >= 0) {
			fFormatterListViewer.setSelection(new StructuredSelection(all[index]));
		}
	}
	
	public void editType() {
		IStructuredSelection selection= (IStructuredSelection)fFormatterListViewer.getSelection();
		if (new DetailFormatterDialog(getShell(), (DetailFormatter)(selection).getFirstElement(), null, true, true).open() == Window.OK) {
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
		
		private Set fDetailFormattersSet;
		
		private List fDefinedTypes;
		
		private CheckboxTableViewer fViewer;
		
		/**
		 * FormatterListViewerContentProvider constructor.
		 */
		public FormatterListViewerContentProvider(CheckboxTableViewer viewer) {
			fViewer= viewer;
			// load the current formatters
			String[] detailFormattersList= JavaDebugOptionsManager.parseList(JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST));
			fDetailFormattersSet= new TreeSet();
			fDefinedTypes= new ArrayList(detailFormattersList.length / 3);
			for (int i= 0, length= detailFormattersList.length; i < length;) {
				String typeName= detailFormattersList[i++];
				String snippet= detailFormattersList[i++].replace('\u0000', ',');
				boolean enabled= ! DETAIL_FORMATTER_IS_DISABLED.equals(detailFormattersList[i++]);
				DetailFormatter detailFormatter= new DetailFormatter(typeName, snippet, enabled);
				fDetailFormattersSet.add(detailFormatter);
				fDefinedTypes.add(typeName);
			}
		}
		
		/**
		 * Save the detail formatter list.
		 */
		public void saveDetailFormatters() {
			String[] values= new String[fDetailFormattersSet.size() * 3];
			int i= 0;
			for (Iterator iter= fDetailFormattersSet.iterator(); iter.hasNext();) {
				DetailFormatter detailFormatter= (DetailFormatter) iter.next();
				values[i++]= detailFormatter.getTypeName();
				values[i++]= detailFormatter.getSnippet().replace(',','\u0000');
				values[i++]= detailFormatter.isEnabled() ? DETAIL_FORMATTER_IS_ENABLED : DETAIL_FORMATTER_IS_DISABLED;
			}
			String pref = JavaDebugOptionsManager.serializeList(values);
			getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST, pref);
			
		}
		
		/**
		 * Add a detail formatter.
		 */
		public void addDetailFormatter(DetailFormatter detailFormatter) {
			fDetailFormattersSet.add(detailFormatter);
			fDefinedTypes.add(detailFormatter.getTypeName());
			fViewer.refresh();
			refreshViewer();
			IStructuredSelection selection= new StructuredSelection(detailFormatter);
			fViewer.setSelection(selection);
			updatePage(selection);
		}
		
		/**
		 * Remove a detailFormatter
		 */
		public void removeDetailFormatter(DetailFormatter detailFormatter) {
			fDetailFormattersSet.remove(detailFormatter);
			fDefinedTypes.remove(detailFormatter.getTypeName());
			fViewer.refresh();
			IStructuredSelection selection= new StructuredSelection();
			fViewer.setSelection(selection);
			updatePage(selection);
		}
		
		/**
		 * Remove detailFormatters
		 */
		public void removeDetailFormatters(Object[] detailFormatters) {
			for (int i= 0, length= detailFormatters.length; i < length; i++) {
				fDetailFormattersSet.remove(detailFormatters[i]);
				fDefinedTypes.remove(((DetailFormatter)detailFormatters[i]).getTypeName());
			}
			fViewer.refresh();
			IStructuredSelection selection= new StructuredSelection();
			fViewer.setSelection(selection);
			updatePage(selection);
		}
		
		/**
		 * Refresh the formatter list viewer. 
		 */
		private void refreshViewer() {
			DetailFormatter[] checkedElementsTmp= new DetailFormatter[fDetailFormattersSet.size()];
			int i= 0;
			for (Iterator iter= fDetailFormattersSet.iterator(); iter.hasNext();) {
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
			return fDetailFormattersSet.toArray();
		}
		
		public List getDefinedTypes() {
			return fDefinedTypes;
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
