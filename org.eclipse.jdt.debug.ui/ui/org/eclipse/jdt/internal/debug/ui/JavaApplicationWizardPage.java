package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaApplicationLauncherDelegate;
import org.eclipse.jdt.internal.ui.util.StringMatcher;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.DialogPageContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The main page in a <code>JavaApplicationWizard</code>. Presents the
 * user with a list of launchable elements - from which the user must choose.  
 */
public class JavaApplicationWizardPage extends WizardPage {

	/**
	 * Viewer for the elements to launch
	 */
	protected TableViewer fElementsList;

	/**
	 * A text field to perform pattern matching
	 */
	protected Text fPatternText;

	/**
	 * The filtered array
	 */
	protected Object[] fFilteredElements;

	/**
	 * The selection from which to determine the elements to launch
	 */
	protected Object[] fElements;

	protected String fMode;

	protected JavaApplicationLauncherDelegate fLauncher;

	/**
	 * A content provider for the elements list
	 */
	class ElementsContentProvider implements IStructuredContentProvider {

		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return fElements;
		}
	}

	class PatternFilter extends ViewerFilter {
		protected StringMatcher fMatcher= null;

		/**
		 * @see ViewerFilter#select(Viewer, Object, Object)
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (fMatcher == null) {
				return true;
			}
			ILabelProvider lp= (ILabelProvider) fElementsList.getLabelProvider();
			return fMatcher.match(lp.getText(element));
		}

		public void setPattern(String pattern) {
			fMatcher= new StringMatcher(pattern + "*", true, false); //$NON-NLS-1$
		}

		/**
		 * Cache the filtered elements so we can single-select.
		 *
		 * @see ViewerFilter#filter(Viewer, Object, Object[])
		 */
		public Object[] filter(Viewer viewer, Object parent, Object[] input) {
			fFilteredElements= super.filter(viewer, parent, input);
			return fFilteredElements;
		}

	}

	class SimpleSorter extends ViewerSorter {
		/**
		 * @see ViewerSorter#isSorterProperty(Object, Object)
		 */
		public boolean isSorterProperty(Object element, Object property) {
			return true;
		}
	}

	/**
	 * Constructs a <code>JavaApplicationWizardPage</code> with the given launcher and pre-computed children
	 */
	public JavaApplicationWizardPage(Object[] elements, JavaApplicationLauncherDelegate launcher, String mode) {
		super(DebugUIMessages.getString("JavaApplicationWizardPage.Select_Target_2")); //$NON-NLS-1$
		setImageDescriptor(JavaDebugImages.DESC_WIZBAN_JAVA_LAUNCH);
		fElements= elements;
		fMode= mode;
		fLauncher= launcher;
	}

	/**
	 * Creates the contents of the page - two lists
	 * and a check box for setting the default launcher.
	 */
	public void createControl(Composite ancestor) {
		Composite root= new Composite(ancestor, SWT.NONE);
		GridLayout l= new GridLayout();
		l.numColumns= 1;
		l.makeColumnsEqualWidth= true;
		root.setLayout(l);

		createElementsGroup(root);

		//determine description
		if (fMode.equals(ILaunchManager.DEBUG_MODE)) {
			setDescription(DebugUIMessages.getString("JavaApplicationWizardPage.Debug..._3")); //$NON-NLS-1$
		} else {
			setDescription(DebugUIMessages.getString("JavaApplicationWizardPage.Run..._4")); //$NON-NLS-1$
		}

		setPageComplete(false);
		setTitle(DebugUIMessages.getString("JavaApplicationWizardPage.Select_Target_5")); //$NON-NLS-1$
		setControl(root);
		WorkbenchHelp.setHelp(root, new DialogPageContextComputer(this, IHelpContextIds.JAVA_APPLICATION_WIZARD_PAGE));				
	}

	public void createElementsGroup(Composite root) {
		Label elementsLabel= new Label(root, SWT.NONE);
		elementsLabel.setText(DebugUIMessages.getString("JavaApplicationWizardPage.&Enter_a_pattern_to_select_a_range_of_elements__6")); //$NON-NLS-1$

		fPatternText= new Text(root, SWT.BORDER);
		fPatternText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		fElementsList= new TableViewer(root, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER) {
			protected void handleDoubleSelect(SelectionEvent event) {
				updateSelection(getSelection());
				if (getWizard().performFinish()) {
					((WizardDialog) getWizard().getContainer()).close();
				}
			}
		};

		Table list= fElementsList.getTable();

		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gd.heightHint= 200;
		gd.grabExcessVerticalSpace= true;
		list.setLayoutData(gd);

		fElementsList.setContentProvider(new ElementsContentProvider());
		int flags= JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_POST_QUALIFIED | 
			JavaElementLabelProvider.SHOW_ROOT ;
			
		fElementsList.setLabelProvider(new JavaElementLabelProvider(flags));
		fElementsList.setSorter(new SimpleSorter());

		final PatternFilter filter= new PatternFilter();
		fElementsList.addFilter(filter);
		fPatternText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				filter.setPattern(((Text) (e.widget)).getText());
				fElementsList.refresh();
				if (fFilteredElements.length >= 1) {
					fElementsList.setSelection(new StructuredSelection(fFilteredElements[0]), true);
					setMessage(DebugUIMessages.getString("JavaApplicationWizardPage._Select_element(s)_to_launch._7"));					 //$NON-NLS-1$
					setPageComplete(true);
					return;
				} else {
					setMessage(DebugUIMessages.getString("JavaApplicationWizardPage.No_elements_available._8")); //$NON-NLS-1$
					setPageComplete(false);
				}
			}
		});

		fElementsList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				if (e.getSelection().isEmpty()) {
					setPageComplete(false);
				} else if (e.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection ss= (IStructuredSelection) e.getSelection();
					if (!ss.isEmpty()) {
						setPageComplete(true);
					}
				}
			}
		});

		fElementsList.setInput(fLauncher);
		initializeSettings();
	}

	/**
	 * Returns the selected elements to launch or <code>null</code> if
	 * no elements are selected.
	 */
	protected Object[] getElements() {
		ISelection s= fElementsList.getSelection();
		if (s.isEmpty()) {
			return null;
		}

		if (s instanceof IStructuredSelection) {
			return ((IStructuredSelection) s).toArray();
		}

		return null;
	}

	/**
	 * Convenience method to set the message line
	 */
	public void setMessage(String message) {
		super.setErrorMessage(null);
		super.setMessage(message);
	}

	/**
	 * Convenience method to set the error line
	 */
	public void setErrorMessage(String message) {
		super.setMessage(null);
		super.setErrorMessage(message);
	}

	/**
	 * Initialize the settings:<ul>
	 * <li>If there is only one element, select it
	 * <li>Put the cursor in the pattern text area
	 * </ul>
	 */
	protected void initializeSettings() {
		Runnable runnable= new Runnable() {
			public void run() {
				if (getControl().isDisposed()) {
					return;
				}
				if (fElements.length >= 1) {
					fElementsList.setSelection(new StructuredSelection(fElements[0]), true);
					setMessage(DebugUIMessages.getString("JavaApplicationWizardPage._Select_element(s)_to_launch._9")); //$NON-NLS-1$
					setPageComplete(true);				
				} else {										
					// no elements to select
					setErrorMessage(DebugUIMessages.getString("JavaApplicationWizardPage.No_elements_available._10")); //$NON-NLS-1$
					setPageComplete(false);	
								
				}
				fPatternText.setFocus();		
			}
		};

		Display.getCurrent().asyncExec(runnable);
	}
}