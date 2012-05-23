/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class JavaDetailFormattersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	static public final String DETAIL_FORMATTER_IS_ENABLED= "1"; //$NON-NLS-1$
	static public final String DETAIL_FORMATTER_IS_DISABLED= "0"; //$NON-NLS-1$
	
	private CheckboxTableViewer fFormatterListViewer;
	private Button fAddFormatterButton;
	private Button fRemoveFormatterButton;
	private Button fEditFormatterButton;
	private JDISourceViewer fCodeViewer;
	private Label fTableLabel;
	
	private FormatterListViewerContentProvider fFormatViewerContentProvider;
    private Button fInlineFormattersButton;
    private Button fInlineAllButton;
	
	public JavaDetailFormattersPreferencePage() {
		super();
		setTitle(DebugUIMessages.JavaDetailFormattersPreferencePage_0); 
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setDescription(DebugUIMessages.JavaDetailFormattersPreferencePage_Override_default___toString_____for_Variables_and_Expressions_view_details__1); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_DETAIL_FORMATTER_PREFERENCE_PAGE);
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
        
		createDetailFormatsPreferences(container);	
        createLabelPreferences(container);
        
        Dialog.applyDialogFont(container);
        return container;
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
    
    private void createLabelPreferences(Composite parent) {
        Group group= new Group(parent, SWT.NONE);
        GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan= 2;
        group.setLayoutData(gridData);
        group.setLayout(new GridLayout());
        group.setText(DebugUIMessages.JavaDetailFormattersPreferencePage_1); 
        
        String preference= getPreferenceStore().getString(IJDIPreferencesConstants.PREF_SHOW_DETAILS);
        
        // Create the 3 detail option radio buttons
        fInlineFormattersButton = new Button(group, SWT.RADIO);
        fInlineFormattersButton.setText(DebugUIMessages.JavaDetailFormattersPreferencePage_2); 
        fInlineFormattersButton.setSelection(preference.equals(IJDIPreferencesConstants.INLINE_FORMATTERS));
        
        fInlineAllButton = new Button(group, SWT.RADIO);
        fInlineAllButton.setText(DebugUIMessages.JavaDetailFormattersPreferencePage_3); 
        fInlineAllButton.setSelection(preference.equals(IJDIPreferencesConstants.INLINE_ALL));
        
        Button detailPane = new Button(group, SWT.RADIO);
        detailPane.setText(DebugUIMessages.JavaDetailFormattersPreferencePage_4); 
        detailPane.setSelection(preference.equals(IJDIPreferencesConstants.DETAIL_PANE));
    }
	
	/**
	 * Create a group to contain the detail formatters related widgetry
	 */
	private Control createDetailFormatsPreferences(Composite parent) {
        Font font= parent.getFont();
		
		//table label
		fTableLabel= new Label(parent, SWT.NONE);
		fTableLabel.setText(DebugUIMessages.JavaDetailFormattersPreferencePage__Types_with_detail_formatters__2); 
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fTableLabel.setLayoutData(gd);
		fTableLabel.setFont(font);

		fFormatterListViewer= CheckboxTableViewer.newCheckList(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = (Table)fFormatterListViewer.getControl();
		gd = new GridData(GridData.FILL_BOTH);
		//gd.heightHint= convertHeightInCharsToPixels(5);
		gd.widthHint= convertWidthInCharsToPixels(10);
		table.setLayoutData(gd);
		table.setFont(font);
		fFormatViewerContentProvider= new FormatterListViewerContentProvider(fFormatterListViewer);
		fFormatterListViewer.setContentProvider(fFormatViewerContentProvider);
		fFormatterListViewer.setLabelProvider(new LabelProvider() {
			@Override
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
				if (!event.getSelection().isEmpty()) {
					editType();
				}
			}
		}); 
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					removeTypes();
				}
			}
		});	
		fFormatterListViewer.setInput(this);

		createDetailFormatsButtons(parent);

		Label label = new Label(parent, SWT.NONE);
		label.setText(DebugUIMessages.JavaDetailFormattersPreferencePage_Detail_formatter_code_snippet_defined_for_selected_type__3); 
		label.setFont(font);
        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan= 2;
        label.setLayoutData(gd);
		createSourceViewer(parent);
		
		fFormatViewerContentProvider.refreshViewer();
		return parent;
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
		fAddFormatterButton.setText(DebugUIMessages.JavaDetailFormattersPreferencePage_Add__Formatter____5); 
		fAddFormatterButton.setToolTipText(DebugUIMessages.JavaDetailFormattersPreferencePage_Allow_you_to_create_a_new_detail_formatter_6); 
		fAddFormatterButton.setLayoutData(gd);
		fAddFormatterButton.setFont(font);
		setButtonLayoutData(fAddFormatterButton);
		fAddFormatterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addType();
			}
		});
		
		// Edit button
		fEditFormatterButton = new Button(buttonContainer, SWT.PUSH);
		fEditFormatterButton.setText(DebugUIMessages.JavaDetailFormattersPreferencePage__Edit____9); 
		fEditFormatterButton.setToolTipText(DebugUIMessages.JavaDetailFormattersPreferencePage_Edit_the_selected_detail_formatter_10); 
		fEditFormatterButton.setFont(font);
		setButtonLayoutData(fEditFormatterButton);
		fEditFormatterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				editType();
			}
		});
		fEditFormatterButton.setEnabled(false);
		
		// Remove button
		fRemoveFormatterButton = new Button(buttonContainer, SWT.PUSH);
		fRemoveFormatterButton.setText(DebugUIMessages.JavaDetailFormattersPreferencePage__Remove_7); 
		fRemoveFormatterButton.setToolTipText(DebugUIMessages.JavaDetailFormattersPreferencePage_Remove_all_selected_detail_formatters_8); 
		fRemoveFormatterButton.setFont(font);
		setButtonLayoutData(fRemoveFormatterButton);
		fRemoveFormatterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				removeTypes();
			}
		});
		fRemoveFormatterButton.setEnabled(false);
	}
	
	public void createSourceViewer(Composite container) {
		fCodeViewer= new JDISourceViewer(container,  null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT_TO_RIGHT);

		JavaTextTools tools= JDIDebugUIPlugin.getDefault().getJavaTextTools();
		IDocument document= new Document();
		tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
		fCodeViewer.configure(new DisplayViewerConfiguration());
		fCodeViewer.setEditable(false);
		fCodeViewer.setDocument(document);
		
		Control control= fCodeViewer.getControl();
		GridData gd= new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan= 2;
		//gd.heightHint= convertHeightInCharsToPixels(5);
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
	
	@Override
	public boolean performOk() {
		if (fFormatViewerContentProvider != null) {
			fFormatViewerContentProvider.saveDetailFormatters();
	
	        String value= IJDIPreferencesConstants.DETAIL_PANE;
	        if (fInlineAllButton.getSelection()) {
	            value= IJDIPreferencesConstants.INLINE_ALL;
	        } else if (fInlineFormattersButton.getSelection()) {
	            value= IJDIPreferencesConstants.INLINE_FORMATTERS;
	        }
	        JDIDebugUIPlugin.getDefault().getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_SHOW_DETAILS, value);
			fCodeViewer.dispose();
		}
		return true;
	}
	
	class FormatterListViewerContentProvider implements IStructuredContentProvider {
		
		private Set<DetailFormatter> fDetailFormattersSet;
		
		private List<String> fDefinedTypes;
		
		private CheckboxTableViewer fViewer;
		
		/**
		 * FormatterListViewerContentProvider constructor.
		 */
		public FormatterListViewerContentProvider(CheckboxTableViewer viewer) {
			fViewer= viewer;
			// load the current formatters
			String[] detailFormattersList= JavaDebugOptionsManager.parseList(JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(IJDIPreferencesConstants.PREF_DETAIL_FORMATTERS_LIST));
			fDetailFormattersSet= new TreeSet<DetailFormatter>();
			fDefinedTypes= new ArrayList<String>(detailFormattersList.length / 3);
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
			for (Iterator<DetailFormatter> iter= fDetailFormattersSet.iterator(); iter.hasNext();) {
				DetailFormatter detailFormatter= iter.next();
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
			for (Iterator<DetailFormatter> iter= fDetailFormattersSet.iterator(); iter.hasNext();) {
				DetailFormatter detailFormatter= iter.next();
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
		
		public List<String> getDefinedTypes() {
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performCancel()
	 */
	@Override
	public boolean performCancel() {
		if (fCodeViewer != null) {
			fCodeViewer.dispose();
		}
		return super.performCancel();
	}	
}
