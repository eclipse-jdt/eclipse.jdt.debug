package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The preference page that is used to present the properties of a breakpoint as 
 * preferences.  A JavaBreakpointPreferenceStore is used to interface between this
 * page and the breakpoint.
 * @see JavaBreakpointPropertiesDialog
 * @see JavaBreakpointPreferenceStore
 */
public class JavaBreakpointPreferencePage extends FieldEditorPreferencePage {

	class BreakpointIntegerFieldEditor extends IntegerFieldEditor {
		public BreakpointIntegerFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
			setErrorMessage(ActionMessages.getString("BreakpointHitCountAction.Value_must_be_positive_integer"));//$NON-NLS-1$
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
		/**
 		 * Clears the error message from the message line if the error
 		 * message is the error message from this field editor.
		 */
		protected void clearErrorMessage() {
			if (getPreferencePage() != null) {
				String message= getPreferencePage().getErrorMessage();
				if (message != null) {
					if(getErrorMessage().equals(message)) {
						super.clearErrorMessage();
					}
					
				} else {
					super.clearErrorMessage();
				}
			}
		}
	}

	class BreakpointStringFieldEditor extends StringFieldEditor {
		public BreakpointStringFieldEditor(String name,	String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		/**
		 * @see StringFieldEditor#checkState()
		 */
		protected boolean checkState() {
			Text control= getTextControl();
			if (!control.isEnabled()) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}
		
		protected void doStore() {
			Text text = getTextControl();
			if (text.isEnabled()) {
				super.doStore();
			}
		}

		/**
		 * @see FieldEditor#refreshValidState()
		 */
		protected void refreshValidState() {
			super.refreshValidState();
		}
		
		/**
 		 * Clears the error message from the message line if the error
 		 * message is the error message from this field editor.
		 */
		protected void clearErrorMessage() {
			if (getPreferencePage() != null) {
				String message= getPreferencePage().getErrorMessage();
				if (message != null) {
					if(getErrorMessage().equals(message)) {
						super.clearErrorMessage();
					}
					
				} else {
					super.clearErrorMessage();
				}
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
	
	private Text fConditionTextControl;
	private BooleanFieldEditor fConditionEnabler;
	private BreakpointStringFieldEditor fCondition;

	private IJavaBreakpoint fBreakpoint;
	protected static final String VM_SUSPEND_POLICY = "VM"; //$NON-NLS-1$
	protected static final String THREAD_SUSPEND_POLICY = "THREAD"; //$NON-NLS-1$

	protected JavaElementLabelProvider fJavaLabelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
	
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
				boolean enabled = fHitCountEnabler.getBooleanValue();
				fHitCountTextControl.setEnabled(enabled);
				fHitCount.refreshValidState();
				if (fHitCount.isValid() && fCondition != null) {
					fCondition.refreshValidState();
				}
				checkState();
			}
		});
		if (fConditionEnabler == null) {
			return;
		}
		fConditionEnabler.setPropertyChangeListener(new IPropertyChangeListener() {
			/**
			 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				boolean enabled = fConditionEnabler.getBooleanValue();
				fConditionTextControl.setEnabled(enabled);
				fCondition.refreshValidState();
				if (fCondition.isValid() && fHitCount != null) {
					fHitCount.refreshValidState();
				}
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
			createTypeSpecificLabelFieldEditors(breakpoint);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
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
				JDIDebugUIPlugin.log(ce);
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
			JDIDebugUIPlugin.log(ce);
		}
	}

	protected void createTypeSpecificLabelFieldEditors(IJavaBreakpoint breakpoint) throws CoreException {
		if (breakpoint instanceof IJavaLineBreakpoint) {
			IMember member= BreakpointUtils.getMember((IJavaLineBreakpoint)breakpoint);
			if (member == null) {
				return;
			}
			String label= ActionMessages.getString("JavaBreakpointPreferencePage.Member");//$NON-NLS-1$
			String memberName= fJavaLabelProvider.getText(member);
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				label= ActionMessages.getString("JavaBreakpointPreferencePage.Method");//$NON-NLS-1$
			} else if (breakpoint instanceof IJavaWatchpoint) {
				label= ActionMessages.getString("JavaBreakpointPreferencePage.Field");//$NON-NLS-1$
			}
			addField(createLabelEditor(getFieldEditorParent(), label, memberName)); 
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
			setTitle(ActionMessages.getString("JavaBreakpointPreferencePage.Java_Exception_Breakpoint_Properties_8")); //$NON-NLS-1$
			store.setValue(
				JavaBreakpointPreferenceStore.UNCAUGHT, jeBreakpoint.isUncaught());
			store.setValue(JavaBreakpointPreferenceStore.CAUGHT, jeBreakpoint.isCaught());
			addField(createUncaughtEditor(getFieldEditorParent()));
			addField(createCaughtEditor(getFieldEditorParent()));
			addField(createFilterEditor(getFieldEditorParent()));
		} else if (breakpoint instanceof IJavaLineBreakpoint) {
			IJavaLineBreakpoint lineBreakpoint= (IJavaLineBreakpoint)breakpoint;
			if (lineBreakpoint.supportsCondition()) {
				createConditionEditor(getFieldEditorParent());
				String condition= lineBreakpoint.getCondition();
				if (condition == null) {
					condition = ""; //$NON-NLS-1$
				}
				store.setValue(JavaBreakpointPreferenceStore.CONDITION, condition);
				if (lineBreakpoint.isConditionEnabled()) {
					store.setValue(JavaBreakpointPreferenceStore.CONDITION_ENABLED, true);
				} else {
					store.setValue(JavaBreakpointPreferenceStore.CONDITION_ENABLED, false);				
				}
			}
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IJavaMethodBreakpoint jmBreakpoint = (IJavaMethodBreakpoint) breakpoint;
				setTitle(ActionMessages.getString("JavaBreakpointPreferencePage.Java_Method_Breakpoint_Properties_10")); //$NON-NLS-1$
				store.setValue(
					JavaBreakpointPreferenceStore.METHOD_ENTRY, jmBreakpoint.isEntry());
				store.setValue(
					JavaBreakpointPreferenceStore.METHOD_EXIT, jmBreakpoint.isExit());
				addField(createMethodEntryEditor(getFieldEditorParent()));
				addField(createMethodExitEditor(getFieldEditorParent()));
			} else if (breakpoint instanceof IJavaWatchpoint) {
				IJavaWatchpoint jWatchpoint = (IJavaWatchpoint) breakpoint;
				setTitle(ActionMessages.getString("JavaBreakpointPreferencePage.Java_Watchpoint_Properties_12")); //$NON-NLS-1$
				store.setValue(JavaBreakpointPreferenceStore.ACCESS, jWatchpoint.isAccess());
				store.setValue(
					JavaBreakpointPreferenceStore.MODIFICATION, jWatchpoint.isModification());
				addField(createAccessEditor(getFieldEditorParent()));
				addField(createModificationEditor(getFieldEditorParent()));
			} else if (breakpoint instanceof IJavaPatternBreakpoint) {
				setTitle(ActionMessages.getString("JavaBreakpointPreferencePage.Java_Pattern_Breakpoint_Properties_14")); //$NON-NLS-1$
			} else {
			 	setTitle(ActionMessages.getString("JavaBreakpointPreferencePage.Java_Line_Breakpoint_Properties_16")); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createContents(parent);
		setControl(getFieldEditorParent());
		WorkbenchHelp.setHelp(
			parent,
			IJavaDebugHelpContextIds.JAVA_BREAKPOINT_PREFERENCE_PAGE );
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
			JDIDebugUIPlugin.log(ce);
		}
		addField(fHitCount);
	}

	protected void createConditionEditor(Composite parent) {
		fConditionEnabler= new BooleanFieldEditor(JavaBreakpointPreferenceStore.CONDITION_ENABLED, ActionMessages.getString("JavaBreakpointPreferencePage.Enable_condition_1"), parent); //$NON-NLS-1$
		addField(fConditionEnabler);

		fCondition =
			new BreakpointStringFieldEditor(JavaBreakpointPreferenceStore.CONDITION, ActionMessages.getString("JavaBreakpointPreferencePage.Condition_2"), parent); //$NON-NLS-1$
		fConditionTextControl= fCondition.getTextControl(parent);
		try {
			fConditionTextControl.setEnabled(((IJavaLineBreakpoint)getBreakpoint()).isConditionEnabled());
		} catch (CoreException ce) {
		}
		fCondition.setEmptyStringAllowed(false);
		fCondition.setErrorMessage(ActionMessages.getString("JavaBreakpointPreferencePage.Invalid_condition")); //$NON-NLS-1$
		addField(fCondition);
		
	}
	
	protected FieldEditor createLabelEditor(
		Composite parent,
		String title,
		String value) {
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
	
	/**
 	 * The preference page implementation of this <code>IPreferencePage</code>
	 * (and <code>IPropertyChangeListener</code>) method intercepts <code>IS_VALID</code> 
     * events but passes other events on to its superclass.
     */
	public void propertyChange(PropertyChangeEvent event) {

		if (event.getProperty().equals(FieldEditor.IS_VALID)) {
			boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
			// If the new value is true then we must check all field editors.
			// If it is false, then the page is invalid in any case.
			if (newValue) {
				if (fHitCount != null && event.getSource() != fHitCount) {
					fHitCount.refreshValidState();
				} 
				if (fCondition != null && event.getSource() != fCondition) {
					fCondition.refreshValidState();
				}
				checkState();
			} else {
				super.propertyChange(event);
			}

		} else {
			super.propertyChange(event);
		}
}


}