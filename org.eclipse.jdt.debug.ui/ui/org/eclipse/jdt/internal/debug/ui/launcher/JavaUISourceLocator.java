package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.ProjectSourceLocator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class JavaUISourceLocator implements ISourceLocator {

	private IJavaProject fJavaProject; 
	private ProjectSourceLocator fProjectSourceLocator;
	private boolean fAllowedToAsk;
	
	public JavaUISourceLocator(IJavaProject project) {
		fJavaProject= project;
		fProjectSourceLocator= new ProjectSourceLocator(project);
		fAllowedToAsk= true;
	}

	/**
	 * @see ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement(IStackFrame stackFrame) {
		Object res= fProjectSourceLocator.getSourceElement(stackFrame);
		if (res == null && fAllowedToAsk) {
			IJavaStackFrame frame= (IJavaStackFrame)stackFrame.getAdapter(IJavaStackFrame.class);
			if (frame != null) {
				try {
					showDebugSourcePage(frame.getDeclaringTypeName());
					res= fProjectSourceLocator.getSourceElement(stackFrame);
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e); 											
				}
			}
		}
		return res;
	}
	
	private void showDebugSourcePage(String typeName) {
		SourceLookupDialog dialog= new SourceLookupDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), fJavaProject, typeName);
		dialog.open();
		fAllowedToAsk= !dialog.isNotAskAgain();
	}
	
	private static class SourceLookupDialog extends Dialog {
		
		private SourceLookupBlock fSourceLookupBlock;
		private String fTypeName;
		private boolean fNotAskAgain;
		private Button fAskAgainCheckBox;
		
		public SourceLookupDialog(Shell shell, IJavaProject project, String typeName) {
			super(shell);
			fSourceLookupBlock= new SourceLookupBlock(project);
			fTypeName= typeName;
			fNotAskAgain= false;
			fAskAgainCheckBox= null;
		}
		
		public boolean isNotAskAgain() {
			return fNotAskAgain;
		}
				
				
		protected Control createDialogArea(Composite parent) {
			getShell().setText(LauncherMessages.getString("JavaUISourceLocator.selectprojects.title")); //$NON-NLS-1$
			
			Composite composite= (Composite) super.createDialogArea(parent);
			composite.setLayout(new GridLayout());
			
			Label message= new Label(composite, SWT.LEFT + SWT.WRAP);
			message.setText(LauncherMessages.getFormattedString("JavaUISourceLocator.selectprojects.message", fTypeName)); //$NON-NLS-1$
			GridData data= new GridData();
			data.widthHint= convertWidthInCharsToPixels(message, 70);
			message.setLayoutData(data);

			Control inner= fSourceLookupBlock.createControl(composite);
			inner.setLayoutData(new GridData(GridData.FILL_BOTH));
			fAskAgainCheckBox= new Button(composite, SWT.CHECK);
			fAskAgainCheckBox.setText(LauncherMessages.getString("JavaUISourceLocator.askagain.message")); //$NON-NLS-1$
			Label askmessage= new Label(composite, SWT.LEFT + SWT.WRAP);
			askmessage.setText(LauncherMessages.getString("JavaUISourceLocator.askagain.description")); //$NON-NLS-1$
			data= new GridData();
			data.widthHint= convertWidthInCharsToPixels(askmessage, 70);
			askmessage.setLayoutData(data);

			return composite;
		}
		
		/**
		 * @see Dialog#convertWidthInCharsToPixels(FontMetrics, int)
		 */
		protected int convertWidthInCharsToPixels(Control control, int chars) {
			GC gc = new GC(control);
			gc.setFont(control.getFont());
			FontMetrics fontMetrics= gc.getFontMetrics();
			gc.dispose();
			return Dialog.convertWidthInCharsToPixels(fontMetrics, chars);
		}	

		protected void okPressed() {
			try {
				if (fAskAgainCheckBox != null) {
					fNotAskAgain= fAskAgainCheckBox.getSelection();
				}
				fSourceLookupBlock.applyChanges();
			} catch (JavaModelException e) {
				JDIDebugUIPlugin.log(e);
			}
			super.okPressed();
		}
	}
}

