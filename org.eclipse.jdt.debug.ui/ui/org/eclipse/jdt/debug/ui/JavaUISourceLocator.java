package org.eclipse.jdt.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.SourceLookupBlock;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;
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

/**
 * A source locator for a Java project that prompts the user
 * for source when source cannot be found on the project's
 * build path.
 * <p>
 * This class is intended to be instantiated. This class is not
 * intended to be subclassed.
 * </p>
 */

public class JavaUISourceLocator implements ISourceLocator {

	/**
	 * The project being debugged.
	 */
	private IJavaProject fJavaProject; 
	
	/**
	 * Underlying source locator.
	 */
	private JavaSourceLocator fSourceLocator;
	
	/**
	 * Whether the user should be prompted for source.
	 * Initially true, until the user checks the 'do not
	 * ask again' box.
	 */
	private boolean fAllowedToAsk;
	
	/**
	 * Constructs a source locator that searches for source
	 * in the given Java project, and all of its required projects,
	 * as specified by its build path.
	 * 
	 * @param project Java project
	 * @exception CoreException if unable to read the project's
	 * 	 build path
	 */
	public JavaUISourceLocator(IJavaProject project) throws CoreException {
		fJavaProject= project;
		fSourceLocator= new JavaSourceLocator(project);
		fAllowedToAsk= true;
	}

	/**
	 * @see ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement(IStackFrame stackFrame) {
		Object res= fSourceLocator.getSourceElement(stackFrame);
		if (res == null && fAllowedToAsk) {
			IJavaStackFrame frame= (IJavaStackFrame)stackFrame.getAdapter(IJavaStackFrame.class);
			if (frame != null) {
				try {
					showDebugSourcePage(frame.getDeclaringTypeName());
					res= fSourceLocator.getSourceElement(stackFrame);
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e); 											
				}
			}
		}
		return res;
	}
	
	/**
	 * Prompts to locate the source of the given type.
	 * 
	 * @param typeName the name of the type for which source
	 *  could not be located
	 */
	private void showDebugSourcePage(String typeName) {
		SourceLookupDialog dialog= new SourceLookupDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), fJavaProject, typeName);
		dialog.open();
		fAllowedToAsk= !dialog.isNotAskAgain();
	}
	
	/**
	 * Dialog that prompts for source.
	 */
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
			fAskAgainCheckBox= new Button(composite, SWT.CHECK + SWT.WRAP);
			data= new GridData();
			data.widthHint= convertWidthInCharsToPixels(fAskAgainCheckBox, 70);
			fAskAgainCheckBox.setLayoutData(data);
			fAskAgainCheckBox.setText(LauncherMessages.getString("JavaUISourceLocator.askagain.message")); //$NON-NLS-1$
			
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

