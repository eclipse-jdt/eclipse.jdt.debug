
package org.eclipse.jdt.internal.debug.ui;

import java.text.MessageFormat;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.IJavaConditionalBreakpointListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class JavaConditionalBreakpointListener implements IJavaConditionalBreakpointListener {

	ILabelProvider fLabelProvider= new DelegatingModelPresentation();

	/**
	 * @see IJavaConditionalBreakpointListener#breakpointHasRuntimeException(IJavaLineBreakpoint, Throwable)
	 */
	public void breakpointHasRuntimeException(final IJavaLineBreakpoint breakpoint, final Throwable exception) {
		if (!(exception instanceof CoreException)) {
			return;
		}
		final CoreException coreException= (CoreException)exception;
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
				IStatus status;
				Throwable wrappedException= coreException.getStatus().getException();
				if (wrappedException instanceof InvocationException) {
					InvocationException ie= (InvocationException) wrappedException;
					ObjectReference ref= ie.exception();		
					status= new Status(IStatus.ERROR,JDIDebugUIPlugin.getPluginId(), IStatus.ERROR, ref.referenceType().name(), null);
				} else {
					status= coreException.getStatus();
				}
				ConditionalBreakpointErrorDialog dialog= new ConditionalBreakpointErrorDialog(shell, "Conditional breakpoint evaluation failed",
					MessageFormat.format("An exception occurred while evaluating the condition for breakpoint: {0} ", new String[] {breakpointText}), status, breakpoint);
				dialog.open();
			}
		});
	}

	/**
	 * @see IJavaConditionalBreakpointListener#breakpointHasCompilationErrors(IJavaLineBreakpoint, Message[])
	 */
	public void breakpointHasCompilationErrors(final IJavaLineBreakpoint breakpoint, final Message[] errors) {
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
				StringBuffer message= new StringBuffer();
				Message error;
				for (int i=0, numErrors= errors.length; i < numErrors; i++) {
					error= errors[i];
					message.append(error.getMessage());
					message.append("\n ");
				}
				IStatus status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getPluginId(), IStatus.ERROR, message.toString(), null);
				ConditionalBreakpointErrorDialog dialog= new ConditionalBreakpointErrorDialog(shell, "Conditional breakpoint compilation failed",
					MessageFormat.format("Errors detected compiling the condition for breakpoint {0}", new String[] {breakpointText}), status, breakpoint);
				dialog.open();
			}
		});
	}

}
