
package org.eclipse.jdt.internal.debug.ui;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.IJavaConditionalBreakpointListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

public class JavaConditionalBreakpointListener implements IJavaConditionalBreakpointListener {

	ILabelProvider fLabelProvider= new DelegatingModelPresentation();

	/**
	 * @see IJavaConditionalBreakpointListener#breakpointHasRuntimeException(IJavaLineBreakpoint, Throwable)
	 */
	public void breakpointHasRuntimeException(final IJavaLineBreakpoint breakpoint, final DebugException exception) {
		IStatus status;
		Throwable wrappedException= exception.getStatus().getException();
		if (wrappedException instanceof InvocationException) {
			InvocationException ie= (InvocationException) wrappedException;
			ObjectReference ref= ie.exception();		
			status= new Status(IStatus.ERROR,JDIDebugUIPlugin.getPluginId(), IStatus.ERROR, ref.referenceType().name(), null);
		} else {
			status= exception.getStatus();
		}
		openConditionErrorDialog(breakpoint, "Conditional breakpoint evaluation failed", "An exception occurred while evaluating the condition for breakpoint: {0} ", status);
	}

	/**
	 * @see IJavaConditionalBreakpointListener#breakpointHasCompilationErrors(IJavaLineBreakpoint, Message[])
	 */
	public void breakpointHasCompilationErrors(final IJavaLineBreakpoint breakpoint, final Message[] errors) {
		StringBuffer message= new StringBuffer();
		Message error;
		for (int i=0, numErrors= errors.length; i < numErrors; i++) {
			error= errors[i];
			message.append(error.getMessage());
			message.append("\n ");
		}
		IStatus status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getPluginId(), IStatus.ERROR, message.toString(), null);
		openConditionErrorDialog(breakpoint, "Conditional breakpoint compilation failed", "Errors detected compiling the condition for breakpoint {0}", status);
	}
	
	private void openConditionErrorDialog(final IJavaLineBreakpoint breakpoint, final String title, final String errorMessage, final IStatus status) {
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		if (display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			public void run() {
				if (display.isDisposed()) {
					return;
				}
				Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
				String breakpointText= fLabelProvider.getText(breakpoint);
				ConditionalBreakpointErrorDialog dialog= new ConditionalBreakpointErrorDialog(shell, title,
					MessageFormat.format(errorMessage, new String[] {breakpointText}), status, breakpoint);
				dialog.open();
			}
		});
	}

}
