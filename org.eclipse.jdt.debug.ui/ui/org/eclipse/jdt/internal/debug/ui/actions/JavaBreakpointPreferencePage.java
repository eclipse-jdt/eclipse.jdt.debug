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
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.JavaBreakpointPropertiesDialog.JavaBreakpointPreferenceStore;
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

public class JavaBreakpointPreferencePage extends FieldEditorPreferencePage {

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
			//gd.widthHint = convertWidthInCharsToPixels(80);
			fValueLabel.setLayoutData(gd);
		}

		public int getNumberOfControls() {
			return 1;
		}

		protected void doLoad() {
		}
		protected void doLoadDefault() {
		}
		protected void doStore() {
		}
	}

	private Text fHitCountTextControl;
	private BooleanFieldEditor fHitCountEnabler;
	private IJavaBreakpoint fBreakpoint;
	protected static final String VM_SUSPEND_POLICY = "VM";
	protected static final String THREAD_SUSPEND_POLICY = "THREAD";

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
				Boolean enabled = (Boolean) event.getNewValue();
				fHitCountTextControl.setEnabled(enabled.booleanValue());
			}
		});
	}

	/**
	 * @see FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		IJavaBreakpoint breakpoint = getBreakpoint();
		String type = "";
		try {
			type = BreakpointUtils.getType(breakpoint).getFullyQualifiedName();
		} catch (CoreException ce) {
			JDIDebugUIPlugin.logError(ce);
		}
		addField(createLabelEditor(getFieldEditorParent(), "Type: ", type));
		if (breakpoint instanceof ILineBreakpoint) {
			ILineBreakpoint lBreakpoint = (ILineBreakpoint) breakpoint;
			StringBuffer lineNumber = new StringBuffer(4);
			try {
				int lNumber = lBreakpoint.getLineNumber();
				if (lNumber > 0) {
					lineNumber.append(lNumber);
				}
			} catch (CoreException ce) {
				JDIDebugUIPlugin.logError(ce);
			}
			if (lineNumber.length() > 0) {
				addField(
					createLabelEditor(
						getFieldEditorParent(),
						"Line Number: ",
						lineNumber.toString()));
			}
		}
		IPreferenceStore store =getPreferenceStore();
		StringBuffer title = new StringBuffer("Properties For ");

		try {
			store.setValue(JavaBreakpointPreferenceStore.ENABLED, breakpoint.isEnabled());
			int hitCount = breakpoint.getHitCount();
			if (hitCount > 0) {
				store.setValue(JavaBreakpointPreferenceStore.HIT_COUNT, hitCount);
				store.setValue(JavaBreakpointPreferenceStore.HIT_COUNT_ENABLED, true);
			} else {
				store.setValue(JavaBreakpointPreferenceStore.HIT_COUNT_ENABLED, false);
			}

			store.setValue(
				JavaBreakpointPreferenceStore.PERSISTED,
				breakpoint.isPersisted());
			String policy = "";
			if (breakpoint.getSuspendPolicy() == IJavaBreakpoint.SUSPEND_THREAD) {
				policy = THREAD_SUSPEND_POLICY;
			} else {
				policy = VM_SUSPEND_POLICY;
			}
			store.setValue(JavaBreakpointPreferenceStore.SUSPEND_POLICY, policy);
			addField(createEnabledEditor(getFieldEditorParent()));
			addField(createPersistedEditor(getFieldEditorParent()));
			createHitCountEditor(getFieldEditorParent());
			addField(createSuspendPolicyEditor(getFieldEditorParent()));

			if (breakpoint instanceof IJavaExceptionBreakpoint) {
				IJavaExceptionBreakpoint jeBreakpoint = (IJavaExceptionBreakpoint) breakpoint;
				this.setTitle("Java Exception Breakpoint Properties");
				store.setValue(
					JavaBreakpointPreferenceStore.UNCAUGHT,
					jeBreakpoint.isUncaught());
				store.setValue(JavaBreakpointPreferenceStore.CAUGHT, jeBreakpoint.isCaught());
				addField(createUncaughtEditor(getFieldEditorParent()));
				addField(createCaughtEditor(getFieldEditorParent()));
				title.append("Java Exception Breakpoint");
			} else if (breakpoint instanceof IJavaLineBreakpoint) {
				if (breakpoint instanceof IJavaMethodBreakpoint) {
					IJavaMethodBreakpoint jmBreakpoint = (IJavaMethodBreakpoint) breakpoint;
					this.setTitle("Java Method Breakpoint Properties");
					store.setValue(
						JavaBreakpointPreferenceStore.METHOD_ENTRY,
						jmBreakpoint.isEntry());
					store.setValue(
						JavaBreakpointPreferenceStore.METHOD_EXIT,
						jmBreakpoint.isExit());
					addField(createMethodEntryEditor(getFieldEditorParent()));
					addField(createMethodExitEditor(getFieldEditorParent()));
					title.append("Java Method Breakpoint");
				} else if (breakpoint instanceof IJavaWatchpoint) {
					IJavaWatchpoint jWatchpoint = (IJavaWatchpoint) breakpoint;
					this.setTitle("Java Watchpoint Properties");
					store.setValue(JavaBreakpointPreferenceStore.ACCESS, jWatchpoint.isAccess());
					store.setValue(
						JavaBreakpointPreferenceStore.MODIFICATION,
						jWatchpoint.isModification());
					addField(createAccessEditor(getFieldEditorParent()));
					addField(createModificationEditor(getFieldEditorParent()));
					title.append("Java Watchpoint");
				} else if (breakpoint instanceof IJavaPatternBreakpoint) {
					this.setTitle("Java Pattern Breakpoint Properties");
					title.append("Java Pattern Breakpoint");
				} else {
					this.setTitle("Java Line Breakpoint Properties");
					title.append("Java Line Breakpoint");
				}
			}
		} catch (CoreException ce) {
			JDIDebugUIPlugin.logError(ce);
		}
		//JavaBreakpointPropertiesDialog.this.setTitle(title.toString());

	}

	public void createControl(Composite parent) {
		super.createContents(parent);
		setControl(getFieldEditorParent());
	}

	protected FieldEditor createAccessEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.ACCESS, "Access", parent);
		return bfe;
	}
	
	protected void createHitCountEditor(Composite parent) {

		fHitCountEnabler =
			new BooleanFieldEditor(
				JavaBreakpointPreferenceStore.HIT_COUNT_ENABLED,
				"Enable Hit Count",
				parent);
		addField(fHitCountEnabler);

		IntegerFieldEditor ife =
			new IntegerFieldEditor(
				JavaBreakpointPreferenceStore.HIT_COUNT,
				"Hit Count:",
				parent);
		ife.setValidRange(1, Integer.MAX_VALUE);
		fHitCountTextControl = ife.getTextControl(parent);
		try {
			fHitCountTextControl.setEnabled(getBreakpoint().getHitCount() > 0);
		} catch (CoreException ce) {
		}
		addField(ife);
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
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.CAUGHT, "Caught", parent);
		return bfe;
	}

	protected FieldEditor createEnabledEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.ENABLED, "Enabled",parent);
		return bfe;
	}

	protected FieldEditor createLineNumberEditor(Composite parent) {	
		IntegerFieldEditor ife= new IntegerFieldEditor("LineNumber", "Line Number:",parent);
		ife.setValidRange(0, Integer.MAX_VALUE);
		return ife;
	}

	protected FieldEditor createMethodEntryEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.METHOD_ENTRY, "Entry", parent);
		return bfe;
	}

	protected FieldEditor createMethodExitEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.METHOD_EXIT, "Exit", parent);
		return bfe;
	}

	protected FieldEditor createModificationEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.MODIFICATION, "Modification", parent);
		return bfe;
	}

	protected FieldEditor createPersistedEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.PERSISTED, "Persisted",parent);
		return bfe;
	}

	protected FieldEditor createSuspendPolicyEditor(Composite parent) {	
		RadioGroupFieldEditor editor= new RadioGroupFieldEditor(
 			JavaBreakpointPreferenceStore.SUSPEND_POLICY, "Suspend Policy", 1,
 			new String[][] {
 				{"Suspend Thread", THREAD_SUSPEND_POLICY},
 				{"Suspend VM", VM_SUSPEND_POLICY}
 			},
           parent);	

		return editor;
	}

	protected FieldEditor createUncaughtEditor(Composite parent) {	
		BooleanFieldEditor bfe= new BooleanFieldEditor(JavaBreakpointPreferenceStore.UNCAUGHT, "Uncaught", parent);
		return bfe;
	}
}