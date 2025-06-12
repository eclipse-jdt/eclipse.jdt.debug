/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaLogicalStructure;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaLogicalStructures;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * The preference page for creating/modifying logical structures
 */
public class JavaLogicalStructuresPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, ISelectionChangedListener, Listener {

	public class LogicalStructuresListViewerLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
		public String getColumnText(Object element, int columnIndex) {
            JavaLogicalStructure logicalStructure= (JavaLogicalStructure) element;
            StringBuilder buffer= new StringBuilder();
            if (columnIndex == 0) {
                String qualifiedName= logicalStructure.getQualifiedTypeName();
                int index= qualifiedName.lastIndexOf('.') + 1;
                String simpleName= qualifiedName.substring(index);
                buffer.append(simpleName);
                if (index > 0) {
                    buffer.append(" (").append(logicalStructure.getQualifiedTypeName()).append(')'); //$NON-NLS-1$
                }
            }
            else if (columnIndex == 1) {
                buffer.append(logicalStructure.getDescription());
                String pluginId= logicalStructure.getContributingPluginId();
                if (pluginId != null) {
					buffer.append(NLS.bind(DebugUIMessages.JavaLogicalStructuresPreferencePage_8, pluginId));
                }
            }
            return buffer.toString();
        }

        @Override
		public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    public class LogicalStructuresListViewerContentProvider implements IStructuredContentProvider {

        private final List<JavaLogicalStructure> fLogicalStructures;

        LogicalStructuresListViewerContentProvider() {
			fLogicalStructures = new ArrayList<>();
			JavaLogicalStructure[] logicalStructures= JavaLogicalStructures.getJavaLogicalStructures();
			for (int i= 0; i < logicalStructures.length; i++) {
				add(logicalStructures[i]);
			}
		}

        @Override
		public Object[] getElements(Object inputElement) {
            return fLogicalStructures.toArray();
        }

        @Override
		public void dispose() {
        }

        @Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

		/**
		 * Add the given logical structure to the content provider.
		 * @param logicalStructure the new structure
		 */
		public void add(JavaLogicalStructure logicalStructure) {
			for (int i= 0, length= fLogicalStructures.size(); i < length; i++) {
				if (!greaterThan(logicalStructure, fLogicalStructures.get(i))) {
					fLogicalStructures.add(i, logicalStructure);
					return;
				}
			}
			fLogicalStructures.add(logicalStructure);
		}

		/**
		 * Compare two logical structures, return <code>true</code> if the first one is 'greater' than
		 * the second one.
		 * @param logicalStructure1 a structure
		 * @param logicalStructure2 a structure
		 * @return if the first structure is 'greater' than the second
		 */
		private boolean greaterThan(JavaLogicalStructure logicalStructure1, JavaLogicalStructure logicalStructure2) {
			int res= logicalStructure1.getQualifiedTypeName().compareToIgnoreCase(logicalStructure2.getQualifiedTypeName());
			if (res != 0) {
				return res > 0;
			}
			res= logicalStructure1.getDescription().compareToIgnoreCase(logicalStructure2.getDescription());
			if (res != 0) {
				return res > 0;
			}
			return logicalStructure1.hashCode() > logicalStructure2.hashCode();
		}

		/**
		 * Remove the given logical structures from the content provider.
		 * @param list the list
		 */
		public void remove(List<JavaLogicalStructure> list) {
			fLogicalStructures.removeAll(list);
		}

		/**
		 * Refresh (reorder) the given logical structure.
		 * @param logicalStructure the logical structure
		 */
		public void refresh(JavaLogicalStructure logicalStructure) {
			fLogicalStructures.remove(logicalStructure);
			add(logicalStructure);
		}

		public void saveUserDefinedJavaLogicalStructures() {
			List<JavaLogicalStructure> logicalStructures = new ArrayList<>();
			for (Iterator<JavaLogicalStructure> iter = fLogicalStructures.iterator(); iter.hasNext();) {
				JavaLogicalStructure logicalStructure= iter.next();
				if (!logicalStructure.isContributed()) {
					logicalStructures.add(logicalStructure);
				}
			}
			JavaLogicalStructures.setUserDefinedJavaLogicalStructures(logicalStructures.toArray(new JavaLogicalStructure[logicalStructures.size()]));
		}

    }

	private TableViewer fLogicalStructuresViewer;
	private Button fAddLogicalStructureButton;
	private Button fEditLogicalStructureButton;
	private Button fRemoveLogicalStructureButton;
    private LogicalStructuresListViewerContentProvider fLogicalStructuresContentProvider;

    protected static String[] fTableColumnProperties= {
        "type", //$NON-NLS-1$
        "showAs", //$NON-NLS-1$
    };
    protected String[] fTableColumnHeaders= {
        DebugUIMessages.JavaLogicalStructuresPreferencePage_9,
        DebugUIMessages.JavaLogicalStructuresPreferencePage_10,
    };
    protected ColumnLayoutData[] fTableColumnLayouts= {
        new ColumnWeightData(70),
        new ColumnWeightData(30),
    };
    private JDISourceViewer fCodeViewer;

	/**
	 * Constructor
	 */
	public JavaLogicalStructuresPreferencePage() {
		super(DebugUIMessages.JavaLogicalStructuresPreferencePage_0);
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
        setDescription(DebugUIMessages.JavaLogicalStructuresPreferencePage_11);
	}

	@Override
	public void applyData(Object data) {
		if (data instanceof String) {
			Object[] logicalStructures = fLogicalStructuresContentProvider.getElements(null);
			for (int i = 0, length = logicalStructures.length; i < length; i++) {
				JavaLogicalStructure javaLogicalStructure = ((JavaLogicalStructure) logicalStructures[i]);
				if (((String) data).compareToIgnoreCase(javaLogicalStructure.getId() + javaLogicalStructure.getDescription()
						+ javaLogicalStructure.hashCode()) == 0) {
					fLogicalStructuresViewer.setSelection(new StructuredSelection(logicalStructures[i]));
					return;
				}
			}
		} else {
			super.applyData(data);
		}
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_LOGICAL_STRUCTURES_PAGE);
	}

	@Override
	protected Control createContents(Composite parent) {
        Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, GridData.FILL_BOTH, 0, 2);
        createTable(comp);
		createTableButtons(comp);
        createSourceViewer(comp);
        noDefaultAndApplyButton();
		return comp;
	}

    /**
     * Creates the source viewer
     * @param parent the parent to add the viewer to
     */
    public void createSourceViewer(Composite parent) {
    	SWTFactory.createWrapLabel(parent, DebugUIMessages.JavaLogicalStructuresPreferencePage_12, 2, 300);

        fCodeViewer = new JDISourceViewer(parent,  null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT_TO_RIGHT);

        JavaTextTools tools = JDIDebugUIPlugin.getDefault().getJavaTextTools();
        IDocument document= new Document();
        tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
        fCodeViewer.configure(new DisplayViewerConfiguration());
        fCodeViewer.setEditable(false);
        fCodeViewer.setDocument(document);

        Control control = fCodeViewer.getControl();
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 2;
        gd.heightHint = convertHeightInCharsToPixels(10);
        control.setLayoutData(gd);
    }

    /**
     * Creates the button group for the table of logical structures
     * @param container the parent container to add the buttons to
     */
    private void createTableButtons(Composite container) {
        // button container
		Composite buttonContainer = SWTFactory.createComposite(container, container.getFont(), 1, 1, GridData.VERTICAL_ALIGN_BEGINNING, 1, 0);
       // add button
		fAddLogicalStructureButton = SWTFactory.createPushButton(buttonContainer, DebugUIMessages.JavaLogicalStructuresPreferencePage_2, DebugUIMessages.JavaLogicalStructuresPreferencePage_3, null);
        fAddLogicalStructureButton.addListener(SWT.Selection, this);
       // edit button
		fEditLogicalStructureButton = SWTFactory.createPushButton(buttonContainer, DebugUIMessages.JavaLogicalStructuresPreferencePage_4, DebugUIMessages.JavaLogicalStructuresPreferencePage_5, null);
        fEditLogicalStructureButton.addListener(SWT.Selection, this);
       // remove button
		fRemoveLogicalStructureButton = SWTFactory.createPushButton(buttonContainer, DebugUIMessages.JavaLogicalStructuresPreferencePage_6, DebugUIMessages.JavaLogicalStructuresPreferencePage_7, null);
        fRemoveLogicalStructureButton.addListener(SWT.Selection, this);
        // initialize the buttons state
		selectionChanged((IStructuredSelection)fLogicalStructuresViewer.getSelection());
    }

    /**
     * @param parent the parent widget
     */
    private void createTable(Composite parent) {
    	SWTFactory.createWrapLabel(parent, DebugUIMessages.JavaLogicalStructuresPreferencePage_1, 2, 300);

        // logical structures list
        fLogicalStructuresViewer = new TableViewer(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        Table table = (Table)fLogicalStructuresViewer.getControl();
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint= convertHeightInCharsToPixels(10);
        gd.widthHint= convertWidthInCharsToPixels(10);
        table.setLayoutData(gd);
        table.setFont(parent.getFont());
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // create table columns
        fLogicalStructuresViewer.setColumnProperties(fTableColumnProperties);
        for (int i = 0; i < fTableColumnHeaders.length; i++) {
            tableLayout.addColumnData(fTableColumnLayouts[i]);
            TableColumn column = new TableColumn(table, SWT.NONE, i);
            column.setResizable(fTableColumnLayouts[i].resizable);
            column.setText(fTableColumnHeaders[i]);
        }

        fLogicalStructuresContentProvider= new LogicalStructuresListViewerContentProvider();
        fLogicalStructuresViewer.setContentProvider(fLogicalStructuresContentProvider);
        fLogicalStructuresViewer.setLabelProvider(new LogicalStructuresListViewerLabelProvider());
        fLogicalStructuresViewer.addSelectionChangedListener(this);
        fLogicalStructuresViewer.setInput(this);
        fLogicalStructuresViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
			public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection= ((IStructuredSelection) fLogicalStructuresViewer.getSelection());
                if (selection.size() == 1 && !((JavaLogicalStructure) selection.getFirstElement()).isContributed()) {
                    editLogicalStructure();
                }
            }
        });
        table.addKeyListener(new KeyAdapter() {
            @Override
			public void keyPressed(KeyEvent event) {
                if (event.character == SWT.DEL && event.stateMask == 0 && fRemoveLogicalStructureButton.isEnabled()) {
                	removeLogicalStrutures();
                }
            }
        });
        fLogicalStructuresViewer.setComparator(new ViewerComparator() {
            @Override
			public int compare(Viewer iViewer, Object e1, Object e2) {
                if (e1 == null) {
                    return -1;
                } else if (e2 == null) {
                    return 1;
                } else {
                    String type1= ((JavaLogicalStructure)e1).getQualifiedTypeName();
                    int index= type1.lastIndexOf('.') + 1;
                    if (index > 0) {
                        type1= type1.substring(index);
                    }
                    String type2= ((JavaLogicalStructure)e2).getQualifiedTypeName();
                    index= type2.lastIndexOf('.') + 1;
                    if (index > 0) {
                        type2= type2.substring(index);
                    }
                    return type1.compareToIgnoreCase(type2);
                }
            }
        });
    }

	@Override
	public void init(IWorkbench workbench) {}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection= event.getSelection();
		if (selection instanceof IStructuredSelection) {
			selectionChanged((IStructuredSelection)selection);
		}
	}

	/**
	 * Modify the state of the button from the selection.
	 * @param structuredSelection the selection that changed
	 */
	private void selectionChanged(IStructuredSelection structuredSelection) {
		int size= structuredSelection.size();
		if (size == 0) {
			fEditLogicalStructureButton.setEnabled(false);
			fRemoveLogicalStructureButton.setEnabled(false);
			refreshCodeViewer(null);
		} else {
			JavaLogicalStructure structure = (JavaLogicalStructure)structuredSelection.getFirstElement();
            fEditLogicalStructureButton.setEnabled(size == 1 && !structure.isContributed());
			boolean removeEnabled= true;
			for (Iterator<JavaLogicalStructure> iter= structuredSelection.iterator(); iter.hasNext();) {
				if (iter.next().isContributed()) {
					removeEnabled= false;
				}
			}
			fRemoveLogicalStructureButton.setEnabled(removeEnabled);
			refreshCodeViewer(structure);
		}
	}

	/**
	 * Refreshes the code viewer after an edit has taken place
	 * @param structure the logical structure that was modified
	 */
	private void refreshCodeViewer(JavaLogicalStructure structure){
		StringBuilder buffer= new StringBuilder();
	    if (structure != null){
	    	String snippet= structure.getValue();
	    	if (snippet != null) {
	    		buffer.append(snippet);
	    	} else {
	    		String[][] variables = structure.getVariables();
	    		for (int i = 0; i < variables.length; i++) {
	    			buffer.append(variables[i][0]);
	    			buffer.append(" = "); //$NON-NLS-1$
	    			buffer.append(variables[i][1]);
	    			if (buffer.charAt(buffer.length() - 1) != '\n') {
	    				buffer.append('\n');
	    			}
	    		}
	    	}
	    }
	    if (fCodeViewer != null) {
	    	fCodeViewer.getDocument().set(buffer.toString());
	    }
	}

	@Override
	public void handleEvent(Event event) {
		Widget source= event.widget;
		if (source == fAddLogicalStructureButton) {
			addLogicalStructure();
		} else if (source == fEditLogicalStructureButton) {
			editLogicalStructure();
		} else if (source == fRemoveLogicalStructureButton) {
			removeLogicalStrutures();
		}
	}

	/**
	 * Performs the add button operation
	 */
	protected void addLogicalStructure() {
		JavaLogicalStructure logicalStructure= new JavaLogicalStructure("", true, "", "", new String[0][]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (new EditLogicalStructureDialog(getShell(), logicalStructure).open() == Window.OK) {
			fLogicalStructuresContentProvider.add(logicalStructure);
			fLogicalStructuresViewer.refresh();
			fLogicalStructuresViewer.setSelection(new StructuredSelection(logicalStructure));
		}
	}

	/**
	 * Performs the edit button operation
	 */
	protected void editLogicalStructure() {
		IStructuredSelection structuredSelection= (IStructuredSelection) fLogicalStructuresViewer.getSelection();
		if (structuredSelection.size() == 1) {
			JavaLogicalStructure logicalStructure= (JavaLogicalStructure)structuredSelection.getFirstElement();
			new EditLogicalStructureDialog(getShell(), logicalStructure).open();
			fLogicalStructuresContentProvider.refresh(logicalStructure);
			fLogicalStructuresViewer.refresh();
			refreshCodeViewer(logicalStructure);
		}
	}

	/**
	 * Performs the remove button operation
	 */
	protected void removeLogicalStrutures() {
		IStructuredSelection selection= (IStructuredSelection)fLogicalStructuresViewer.getSelection();
		if (selection.size() > 0) {
			List<JavaLogicalStructure> selectedElements= selection.toList();
			Object[] elements= fLogicalStructuresContentProvider.getElements(null);
			Object newSelectedElement= null;
			for (int i= 0; i < elements.length; i++) {
				if (!selectedElements.contains(elements[i])) {
					newSelectedElement= elements[i];
				} else {
					break;
				}
			}
			fLogicalStructuresContentProvider.remove(((IStructuredSelection) fLogicalStructuresViewer.getSelection()).toList());
			fLogicalStructuresViewer.refresh();
			if (newSelectedElement == null) {
				Object[] newElements= fLogicalStructuresContentProvider.getElements(null);
				if (newElements.length > 0) {
					fLogicalStructuresViewer.setSelection(new StructuredSelection(newElements[0]));
				}
			} else {
				fLogicalStructuresViewer.setSelection(new StructuredSelection(newSelectedElement));
			}
		}

	}

	@Override
	public boolean performOk() {
		if (fCodeViewer != null) {
			fLogicalStructuresContentProvider.saveUserDefinedJavaLogicalStructures();
			fCodeViewer.dispose();
		}
		return super.performOk();
	}

	@Override
	public boolean performCancel() {
		if (fCodeViewer != null) {
			fCodeViewer.dispose();
		}
		return super.performCancel();
	}
}
