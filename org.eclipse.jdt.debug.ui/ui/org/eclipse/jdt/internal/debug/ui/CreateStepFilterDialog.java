package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CreateStepFilterDialog extends StatusDialog {

	private static final String DEFAULT_NEW_FILTER_TEXT = ""; //$NON-NLS-1$
	
	private Text text;
	private Filter filter;
	private Button okButton;
	private boolean filterValid;
	private boolean okClicked;
	private Filter[] existingFilters;

	private CreateStepFilterDialog(Shell parent, Filter filter, Filter[] existingFilters) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.filter = filter;
		this.existingFilters = existingFilters;
		
		setTitle(DebugUIMessages.getString("CreateStepFilterDialog.2")); //$NON-NLS-1$
		setStatusLineAboveButtons(false);
		
	}
	
	static Filter showCreateStepFilterDialog(Shell parent, Filter[] existingFilters) {
		CreateStepFilterDialog createStepFilterDialog = new CreateStepFilterDialog(parent, new Filter(DEFAULT_NEW_FILTER_TEXT, true), existingFilters);
		createStepFilterDialog.create();
		createStepFilterDialog.open();
		
		return createStepFilterDialog.filter;		
	}
	
	boolean isValidFilter() {
		return filterValid;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		okButton= createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);			
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite)super.createDialogArea(parent);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 15;
		gridLayout.marginWidth = 15;
		container.setLayout(gridLayout);

		int textStyles = SWT.SINGLE | SWT.LEFT;
		Label label= new Label(container, textStyles);
		label.setText(DebugUIMessages.getString("CreateStepFilterDialog.3")); //$NON-NLS-1$

		// create & configure Text widget for editor
		// Fix for bug 1766.  Border behavior on for text fields varies per platform.
		// On Motif, you always get a border, on other platforms,
		// you don't.  Specifying a border on Motif results in the characters
		// getting pushed down so that only there very tops are visible.  Thus,
		// we have to specify different style constants for the different platforms.
		if (!SWT.getPlatform().equals("motif")) {  //$NON-NLS-1$
			textStyles |= SWT.BORDER;
		}
		
		text = new Text(container, textStyles);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);		
		gridData.horizontalSpan=1;
		gridData.widthHint = 300;
		text.setLayoutData(gridData);
		
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateChange();
				if (!filterValid) 
					updateStatus(new StatusInfo(IStatus.ERROR, DebugUIMessages.getString("CreateStepFilterDialog.4"))); //$NON-NLS-1$
				else if (isDuplicateFilter(text.getText().trim())) {
					updateStatus(new StatusInfo(IStatus.WARNING, DebugUIMessages.getString("CreateStepFilterDialog.5"))); //$NON-NLS-1$
					return;
				} else 
					updateStatus(new StatusInfo());		
			}
		});
	
		return container;
	}
	
	private void validateChange() {
		String trimmedValue = text.getText().trim();

		if (trimmedValue.length()>0 && validateInput(trimmedValue)) {
			okButton.setEnabled(true);
			filter.setName(text.getText());
			filterValid = true;
		} else {
			okButton.setEnabled(false);
			filter.setName(DEFAULT_NEW_FILTER_TEXT);
			filterValid = false;
		}
	}
	
	/**
	 * @param trimmedValue
	 * @return
	 */
	private boolean isDuplicateFilter(String trimmedValue) {
		for (int i=0; i<existingFilters.length; i++)
			if(existingFilters[i].getName().equals(trimmedValue))
				return true;
		return false;
	}
	/**
	 * A valid step filter is simply one that is a valid Java identifier.
	 * and, as defined in the JDI spec, the regular expressions used for
	 * step filtering must be limited to exact matches or patterns that
	 * begin with '*' or end with '*'. Beyond this, a string cannot be validated
	 * as corresponding to an existing type or package (and this is probably not
	 * even desirable).  
	 */
	private boolean validateInput(String trimmedValue) {
		char firstChar= trimmedValue.charAt(0);
		if (!Character.isJavaIdentifierStart(firstChar)) {
			if (!(firstChar == '*')) {
				return false;
			}
		}
		int length= trimmedValue.length();
		for (int i= 1; i < length; i++) {
			char c= trimmedValue.charAt(i);
			if (!Character.isJavaIdentifierPart(c)) {
				if (c == '.' && i != (length - 1)) {
					continue;
				}
				if (c == '*' && i == (length - 1)) {
					continue;
				}
				return false;
			}
		}
		return true;
	}	


	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		if (!okClicked) {
			filterValid = false;
			filter = null;
		}
		return super.close();
	}

	protected void okPressed() {
		okClicked = true;
		super.okPressed();
	}

}
