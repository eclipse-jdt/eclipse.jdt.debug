package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointConditionCompletionProcessor;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	
	class BreakpointConditionFieldEditor extends FieldEditor {
		
		private SourceViewer fViewer;
		
		private BreakpointConditionCompletionProcessor fCompletionProcessor;
		
		private boolean fIsValid;
		
		private String fOldValue;
		
		private String fErrorMessage;
		
		private Composite fParent;
		
		public BreakpointConditionFieldEditor(String name,	String labelText, Composite parent) {
			super(name, labelText, parent);
			setDefaults();
			
			fErrorMessage= ActionMessages.getString("JavaBreakpointPreferencePage.Invalid_condition"); //$NON-NLS-1$
			fOldValue= ""; //$NON-NLS-1$
		}

		protected void doStore() {
			getPreferenceStore().setValue(getPreferenceName(), fViewer.getDocument().get());
		}

		/**
		 * @see FieldEditor#refreshValidState()
		 */
		protected void refreshValidState() {
			// the value is valid if the field is not editable, or if the value is not empty
			if (!fViewer.isEditable()) {
				clearErrorMessage();
				fIsValid= true;
			} else {
				String text= fViewer.getDocument().get();
				fIsValid= text != null && text.trim().length() > 0;
				if (!fIsValid) {
					showErrorMessage(fErrorMessage);
				} else {
					clearErrorMessage();
				}
			}
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
					if(fErrorMessage.equals(message)) {
						super.clearErrorMessage();
					}
					
				} else {
					super.clearErrorMessage();
				}
			}
		}
		/**
		 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
		 */
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			fParent= parent;
			getLabelControl(parent).setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
			
			// the source viewer
			fViewer= new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			fViewer.setInput(parent);
		
			JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
			IDocument document= new Document();
			IDocumentPartitioner partitioner= tools.createDocumentPartitioner();
			document.setDocumentPartitioner(partitioner);
			partitioner.connect(document);		
			fViewer.configure(new DisplayViewerConfiguration() {
				public IContentAssistProcessor getContentAssistantProcessor() {
						return getCompletionProcessor();
				}
			});
			fViewer.setEditable(true);
			fViewer.setDocument(document);
		
			Font font= JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
			fViewer.getTextWidget().setFont(font);
			
			Control control= fViewer.getControl();
			GridData gd = new GridData();
			gd.horizontalSpan = numColumns - 1;
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			control.setLayoutData(gd);
		
			// listener for activate the code assist
			fViewer.getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {
				public void verifyKey(VerifyEvent event) {
					//do code assist for CTRL-SPACE
					if (event.stateMask == SWT.CTRL && event.keyCode == 0) {
						if (event.character == 0x20) {
							fViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
							event.doit= false;
						}
					}
				}
			});

			// listener for check the value
			fViewer.getTextWidget().addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					valueChanged();
				}
			});
			
		}

		/**
		 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
		 */
		protected void adjustForNumColumns(int numColumns) {
			GridData gd = (GridData)fViewer.getControl().getLayoutData();
			gd.horizontalSpan = numColumns - 1;
			// We only grab excess space if we have to
			// If another field editor has more columns then
			// we assume it is setting the width.
			gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
		}

		/**
		 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
		 */
		protected void doLoad() {
			fViewer.getDocument().set(getPreferenceStore().getString(getPreferenceName()));
			valueChanged();
		}

		/**
		 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
		 */
		protected void doLoadDefault() {
			fViewer.getDocument().set(getPreferenceStore().getDefaultString(getPreferenceName()));
			valueChanged();
		}

		/**
		 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
		 */
		public int getNumberOfControls() {
			return 0;
		}
		
		/**
		 * Return the completion processor associated with this viewer.		 * @return BreakPointConditionCompletionProcessor		 */
		private BreakpointConditionCompletionProcessor getCompletionProcessor() {
			if (fCompletionProcessor == null) {
				fCompletionProcessor= new BreakpointConditionCompletionProcessor(null);
			}
			return fCompletionProcessor;
		}
		
		/**
		 * Set the defaults value of this fields which can't be set in doFillIntoGrid().		 */
		public void setDefaults() {
			// we can only do code assist if there is an associated type
			IType type = null;
			try {
				type= BreakpointUtils.getType(fBreakpoint);
			} catch (CoreException e) {
			}
			if (type != null) {
				try {
					getCompletionProcessor().setType(type);			
					String source= null;
					source= type.getSource();
					int lineNumber= fBreakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1);
					int position= -1;
					if (source != null && lineNumber != -1) {
						try {
							position= new Document(source).getLineOffset(lineNumber);
						} catch (BadLocationException e) {
						}
					}
					getCompletionProcessor().setPosition(position);
				} catch (CoreException e) {
				}
			}
			
			GridData gd= (GridData)fViewer.getControl().getLayoutData();
			gd.heightHint= convertHeightInCharsToPixels(10);
			gd.widthHint= convertWidthInCharsToPixels(40);			
		}

		/**
		 * @see org.eclipse.jface.preference.FieldEditor#setEnabled(boolean, org.eclipse.swt.widgets.Composite)
		 */
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled, fParent);
			fViewer.setEditable(enabled);
			valueChanged();
		}

		/**
		 * @see org.eclipse.jface.preference.FieldEditor#isValid()
		 */
		public boolean isValid() {
			return fIsValid;
		}
		
		public void valueChanged() {
			boolean oldState= fIsValid;
			refreshValidState();
			if (fIsValid != oldState)
				fireStateChanged(IS_VALID, oldState, fIsValid);
				
			String newValue = fViewer.getDocument().get();
			if (!newValue.equals(fOldValue)) {
				fireValueChanged(VALUE, fOldValue, newValue);
				fOldValue = newValue;
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
	
	private BooleanFieldEditor fConditionEnabler;
	private BreakpointConditionFieldEditor fCondition;

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
				fCondition.setEnabled(enabled);
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
		
		try {
			IJavaObject[] instances = breakpoint.getInstanceFilters();
			if (instances.length > 0) {
				addField(createInstanceFilterViewer(getFieldEditorParent()));
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
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
	
	protected FieldEditor createInstanceFilterViewer(Composite parent) {
		return new InstanceFilterViewer(parent, getBreakpoint());
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
		IType type = null;
		try {
			type = BreakpointUtils.getType(fBreakpoint);
		} catch (CoreException e) {
		}
		String label = null;
		if (type == null) {
			label = ActionMessages.getString("JavaBreakpointPreferencePage.Enable_Condition_(code_assist_not_available)_1"); //$NON-NLS-1$
		} else {
			label = ActionMessages.getString("JavaBreakpointPreferencePage.Enable_condition_1"); //$NON-NLS-1$
		}
		fConditionEnabler= new BooleanFieldEditor(JavaBreakpointPreferenceStore.CONDITION_ENABLED, label, parent);
		addField(fConditionEnabler);

		fCondition =
			new BreakpointConditionFieldEditor(JavaBreakpointPreferenceStore.CONDITION, ActionMessages.getString("JavaBreakpointPreferencePage.Condition_2"), parent); //$NON-NLS-1$
		try {
			fCondition.setEnabled(((IJavaLineBreakpoint)getBreakpoint()).isConditionEnabled());
		} catch (CoreException ce) {
		}
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