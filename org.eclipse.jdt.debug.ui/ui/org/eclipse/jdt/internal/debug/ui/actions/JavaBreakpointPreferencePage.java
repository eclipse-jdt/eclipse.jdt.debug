package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The preference page that is used to present the properties of a breakpoint as 
 * preferences.  A JavaBreakpointPreferenceStore is used to interface between this
 * page and the breakpoint.
 * @see JavaBreakpointPropertiesDialog
 * @see JavaBrekapointPropertyStore
 */
public class JavaBreakpointPreferencePage extends FieldEditorPreferencePage {

	class BreakpointIntegerFieldEditor extends IntegerFieldEditor {
		public BreakpointIntegerFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		/**
		 * @see IntegerFieldEditor#checkState()
		 */
		protected boolean checkState() {
			Text control= getTextControl();
			if (!control.isEnabled()) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}
		
		/**
		 * Overrode here to be package visible.
		 */
		protected void refreshValidState() {
			super.refreshValidState();
		}
		
		/**
		 * Only store if the text control is enabled
		 * @see FieldEditor#doStore()
		 */
		protected void doStore() {
			Text text = getTextControl();
			if (text.isEnabled()) {
				super.doStore();
			}
		}
}
	class LabelFieldEditor extends FieldEditor {

		private Label fTitleLabel;
		private Label fValueLabel;
		private Composite fBasicComposite;
		private String fValue;
		private String fTitle;

		public LabelFieldEditor(Composite parent, String title, String value) {
			fValue = value;
			fTitle = title;
			this.createControl(parent);
		}

		protected void adjustForNumColumns(int numColumns) {
			((GridData) fBasicComposite.getLayoutData()).horizontalSpan = numColumns;
		}

		protected void doFillIntoGrid(Composite parent, int numColumns) {
			fBasicComposite = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.numColumns = 2;
			fBasicComposite.setLayout(layout);
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			fBasicComposite.setLayoutData(data);

			fTitleLabel = new Label(fBasicComposite, SWT.NONE);
			fTitleLabel.setText(fTitle);
			GridData gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			fTitleLabel.setLayoutData(gd);

			fValueLabel = new Label(fBasicComposite, SWT.WRAP);
			fValueLabel.setText(fValue);
			gd = new GridData();
			fValueLabel.setLayoutData(gd);
		}

		public int getNumberOfControls() {
			return 1;
		}

		/**
		 * The label field editor is only used to present a text label
		 * on a preference page.
		 */
		protected void doLoad() {
		}
		protected void doLoadDefault() {
		}
		protected void doStore() {
		}
	}

	private Text fHitCountTextControl;
	private BooleanFieldEditor fHitCountEnabler;
	private BreakpointIntegerFieldEditor fHitCount;
	private IJavaBreakpoint fBreakpoint;
	protected static final String VM_SUSPEND_POLICY = "VM"; //$NON-NLS-1$
	protected static final String THREAD_SUSPEND_POLICY = "THREAD"; //$NON-NLS-1$

	protected JavaBreakpointPreferencePage(IJavaBreakpoint breakpoint) {
		super(GRID);
		setBreakpoint(breakpoint);
	}

	/**
	 * Initializes all field editors.
	 */
	protected void initialize() {
		super.initialize();
		fHitCountEnabler.setPropertyChangeListener(new IPropertyChangeListener() {
			/**
			 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				boolean enabled = ((Boolean) event.getNewValue()).booleanValue();
				fHitCountTextControl.setEnabled(enabled);
				fHitCount.refreshValidState();
				checkState();
			}
		});
	}

	/**
	 * @see FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		IJavaBreakpoint breakpoint = getBreakpoint();
		try {
			String typeName = breakpoint.getTypeName();
			if (typeName != null) {
				addField(createLabelEditor(getFieldEditorParent(), ActionMessages.getString("JavaBreakpointPreferencePage.Type___4"), typeName)); //$NON-NLS-1$
			}
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce.getStatus());
		}

		if (breakpoint instanceof ILineBreakpoint) {
			ILineBreakpoint lBreakpoint = (ILineBreakpoint) breakpoint;
			StringBuffer lineNumber = new StringBuffer(4);
			try {
				int lNumber = lBreakpoint.getLineNumber();
				if (lNumber > 0) {
					lineNumber.append(lNumber);
				}
			} catch (CoreException ce) {
				JDIDebugUIPlugin.log(ce.getStatus());
			}
			if (lineNumber.length() > 0) {
				addField(
					createLabelEditor(
						getFieldEditorParent(),
						ActionMessages.getString("JavaBreakpointPreferencePage.Line_Number___5"), //$NON-NLS-1$
						lineNumber.toString()));
			}
		}
		IPreferenceStore store= getPreferenceStore();
		
		try {
			store.setValue(JavaBreakpointPreferenceStore.ENABLED, breakpoint.isEnabled());
			int hitCount = breakpoint.getHitCount();
			if (hitCount > 0) {
				store.setValue(JavaBreakpointPreferenceStore.HIT_COUNT, hitCount);
				store.setValue(JavaBreakpointPreferenceStore.HIT_COUNT_ENABLED, true);
			} else {
				store.setValue(JavaBreakpointPreferenceStore.HIT_COUNT_ENABLED, false);
			}
			
			String policy = ""; //$NON-NLS-1$
			if (breakpoint.getSuspendPolicy() == IJavaBreakpoint.SUSPEND_THREAD) {
				policy = THREAD_SUSPEND_POLICY;
			} else {
				policy = VM_SUSPEND_POLICY;
			}
			store.setValue(JavaBreakpointPreferenceStore.SUSPEND_POLICY, policy);
			addField(createEnabledEditor(getFieldEditorParent()));
			createHitCountEditor(getFieldEditorParent());
			addField(createSuspendPolicyEditor(getFieldEditorParent()));
			createTypeSpecificFieldEditors();
			addField(createThreadFilterViewer(getFieldEditorParent()));
		} catch (CoreException ce) {
			JDIDebugUIPlugin.logError(ce);
		}
	}
	
	protected FieldEditor createThreadFilterViewer(Composite parent) {
		return new ThreadFilterViewer(parent, getBreakpoint());
	}
	
	protected FieldEditor createFilterEditor(Composite parent) {
		return new ExceptionBreakpointFilterEditor(parent, (IJavaExceptionBreakpoint)getBreakpoint());
	}

	protected void createTypeSpecificFieldEditors() throws CoreException {
		IJavaBreakpoint breakpoint= getBreakpoint();
		IPreferenceStore store= getPreferenceStore();
		if (breakpoint instanceof IJavaExceptionBreakpoint) {
			IJavaExceptionBreakpoint jeBreakpoint = (IJavaExceptionBreakpoint) breakpoint;
			this.setTitle(ActionMessages.getString("JavaBreakpointPreferencePage.Java_Exception_Breakpoint_Properties_8")); //$NON-NLS-1$
			store.setValue(
				JavaBreakpointPreferenceStore.UNCAUGHT, jeBreakpoint.isUncaught());
			store.setValue(JavaBreakpointPreferenceStore.CAUGHT, jeBreakpoint.isCaught());
			addField(createUncaughtEditor(getFieldEditorParent()));
			addField(createCaughtEditor(getFieldEditorParent()));
			addField(createFilterEditor(getFieldEditorParent()));
		} else if (breakpoint instanceof IJavaLineBreakpoint) {
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IJavaMethodBreakpoint jmBreakpoint = (IJavaMethodBreakpoint) breakpoint;
				this.setTitle(ActionMessages.getString("JavaBreakpointPreferencePage.Java_Method_Breakpoint_Properties_10")); //$NON-NLS-1$
				store.setValue(
					JavaBreakpointPreferenceStore.METHOD_ENTRY, jmBreakpoint.isEntry());
				store.setValue(
					JavaBreakpointPreferenceStore.METHOD_EXIT, jmBreakpoint.isExit());
				addField(createMethodEntryEditor(getFieldEditorParent()));
				addField(createMethodExitEditor(getFieldEditorParent()));
			} else if (breakpoint instanceof IJavaWatchpoint) {
				IJavaWatchpoint jWatchpoint = (IJavaWatchpoint) breakpoint;
				this.setTitle(ActionMessages.getString("JavaBreakpointPreferencePage.Java_Watchpoint_Properties_12")); //$NON-NLS-1$
				store.setValue(JavaBreakpointPreferenceStore.ACCESS, jWatchpoint.isAccess());
				store.setValue(
					JavaBreakpointPreferenceStore.MODIFICATION, jWatchpoint.isModification());
				addField(createAccessEditor(getFieldEditorParent()));
				addField(createModificationEditor(getFieldEditorParent()));
			} else if (breakpoint instanceof IJavaPatternBreakpoint) {
				this.setTitle(ActionMessages.getString("JavaBreakpointPreferencePage.Java_Pattern_Breakpoint_Properties_14")); //$NON-NLS-1$
			} else {
				this.setTitle(ActionMessages.getString("JavaBreakpointPreferencePage.Java_Line_Breakpoint_Properties_16")); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createContents(parent);
		setControl(getFieldEditorParent());
	}

	protected FieldEditor createAccessEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.ACCESS, ActionMessages.getString("JavaBreakpointPreferencePage.Access_18"), parent); //$NON-NLS-1$
		return bfe;
	}
	
	protected void createHitCountEditor(Composite parent) {
		fHitCountEnabler =
			new BooleanFieldEditor(
				JavaBreakpointPreferenceStore.HIT_COUNT_ENABLED,
				ActionMessages.getString("JavaBreakpointPreferencePage.Enable_&Hit_Count_19"), //$NON-NLS-1$
				parent);
		addField(fHitCountEnabler);

		fHitCount =
			new BreakpointIntegerFieldEditor(
				JavaBreakpointPreferenceStore.HIT_COUNT,
				ActionMessages.getString("JavaBreakpointPreferencePage.H&it_Count__20"), //$NON-NLS-1$
				parent);
		fHitCount.setValidRange(1, Integer.MAX_VALUE);
		fHitCountTextControl = fHitCount.getTextControl(parent);
		try {
			fHitCountTextControl.setEnabled(getBreakpoint().getHitCount() > 0);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce.getStatus());
		}
		addField(fHitCount);
	}

	protected FieldEditor createLabelEditor(Composite parent, String title, String value) {
		return new LabelFieldEditor(parent, title, value);
	}
	
	protected IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint(IJavaBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
	
	protected FieldEditor createCaughtEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.CAUGHT, ActionMessages.getString("JavaBreakpointPreferencePage.&Caught_21"), parent); //$NON-NLS-1$
		return bfe;
	}

	protected FieldEditor createEnabledEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.ENABLED, ActionMessages.getString("JavaBreakpointPreferencePage.&Enabled_22"),parent); //$NON-NLS-1$
		return bfe;
	}

	protected FieldEditor createLineNumberEditor(Composite parent) {	
		IntegerFieldEditor ife= new IntegerFieldEditor(ActionMessages.getString("JavaBreakpointPreferencePage.LineNumber_23"), ActionMessages.getString("JavaBreakpointPreferencePage.Line_Number__24"),parent); //$NON-NLS-1$ //$NON-NLS-2$
		ife.setValidRange(0, Integer.MAX_VALUE);
		return ife;
	}

	protected FieldEditor createMethodEntryEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.METHOD_ENTRY, ActionMessages.getString("JavaBreakpointPreferencePage.E&ntry_25"), parent); //$NON-NLS-1$
		return bfe;
	}

	protected FieldEditor createMethodExitEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.METHOD_EXIT, ActionMessages.getString("JavaBreakpointPreferencePage.E&xit_26"), parent); //$NON-NLS-1$
		return bfe;
	}

	protected FieldEditor createModificationEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.MODIFICATION, ActionMessages.getString("JavaBreakpointPreferencePage.&Modification_27"), parent); //$NON-NLS-1$
		return bfe;
	}


	protected FieldEditor createSuspendPolicyEditor(Composite parent) {	
		RadioGroupFieldEditor editor= new RadioGroupFieldEditor(
 			JavaBreakpointPreferenceStore.SUSPEND_POLICY, ActionMessages.getString("JavaBreakpointPreferencePage.Suspend_Policy_29"), 1, //$NON-NLS-1$
 			new String[][] {
 				{ActionMessages.getString("JavaBreakpointPreferencePage.Suspend_&Thread_30"), THREAD_SUSPEND_POLICY}, //$NON-NLS-1$
 				{ActionMessages.getString("JavaBreakpointPreferencePage.Suspend_&VM_31"), VM_SUSPEND_POLICY} //$NON-NLS-1$
 			},
           parent);	

		return editor;
	}

	protected FieldEditor createUncaughtEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.UNCAUGHT, ActionMessages.getString("JavaBreakpointPreferencePage.&Uncaught_32"), parent); //$NON-NLS-1$
		return bfe;
	}
}