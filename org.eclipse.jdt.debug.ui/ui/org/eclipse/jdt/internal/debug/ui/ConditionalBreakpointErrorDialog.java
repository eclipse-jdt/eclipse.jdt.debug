
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConditionalBreakpointErrorDialog extends ErrorDialog {
	
	private Text fTextArea;
	private String fMessage;
	private IJavaLineBreakpoint fBreakpoint;

	public ConditionalBreakpointErrorDialog(Shell parentShell, String dialogTitle, String message, IStatus status, IJavaLineBreakpoint breakpoint) {
		super(parentShell, dialogTitle, message, status, IStatus.ERROR);
		fMessage= message;
		fBreakpoint= breakpoint;
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea= (Composite) super.createDialogArea(parent);
		String condition= "";
		try {
			condition= fBreakpoint.getCondition();
		} catch(CoreException e) {
		}
		fTextArea= createEditArea(parent, condition, "Edit the condition");
		
		return dialogArea;
	}
	
	private Text createEditArea(Composite parent, String startingText, String labelText) {
		Composite editArea = new Composite(parent, SWT.NONE);
		GridData data = new GridData(SWT.NONE);
		data.horizontalSpan = GridData.FILL_HORIZONTAL;
		data.widthHint= 400;
		data.horizontalAlignment = GridData.CENTER;
		editArea.setLayoutData(data);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		editArea.setLayout(layout);
				
		Label label= new Label(editArea, SWT.NONE);
		label.setText(labelText);
		GridData labelData= new GridData(SWT.NONE);
		labelData.horizontalAlignment= GridData.BEGINNING;
		labelData.horizontalSpan= 1;
		label.setLayoutData(labelData);
		
		Text text= new Text(editArea, SWT.SINGLE | SWT.BORDER);
		text.setText(startingText);		
		GridData textData= new GridData(SWT.NONE);
		textData.widthHint= 300;
		textData.horizontalSpan= 1;
		textData.horizontalAlignment= GridData.BEGINNING;
		text.setLayoutData(textData);
		
		return text;
	}

	protected void buttonPressed(int id) {
		if (id == IDialogConstants.OK_ID) {  // was the Ok button pressed?
			try {
				fBreakpoint.setCondition(fTextArea.getText());
			} catch (CoreException exception) {
			}
		}
		super.buttonPressed(id);
	}

}
