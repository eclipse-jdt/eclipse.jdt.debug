package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Adds a method breakpoint on a single selected element of type IMethod 
 */
public class ManageMethodBreakpointAction extends ManageBreakpointAction {
	
	public ManageMethodBreakpointAction() {
		super();
		fAddText= ActionMessages.getString("ManageMethodBreakpointAction.&Add_Method_Breakpoint_1"); //$NON-NLS-1$
		fAddDescription= ActionMessages.getString("ManageMethodBreakpointAction.Add_a_method_breakpoint_2"); //$NON-NLS-1$
		fRemoveText= ActionMessages.getString("ManageMethodBreakpointAction.Remove_&Method_Breakpoint_4"); //$NON-NLS-1$
		fRemoveDescription= ActionMessages.getString("ManageMethodBreakpointAction.Remove_a_method_breakpoint_5"); //$NON-NLS-1$
	}
		
	protected void update() {
		IMethod method= (IMethod)getElement();
		if (method != null && method.isBinary()) { // only add to class files
			getAction().setEnabled(true);
			setBreakpoint(getBreakpoint(method));
			boolean doesNotExist= getBreakpoint() == null;
			getAction().setText(doesNotExist ? fAddText : fRemoveText);
			getAction().setDescription(doesNotExist ? fAddDescription : fRemoveDescription);
		} else {
			getAction().setEnabled(false);
		}
	}
	
	protected IJavaBreakpoint getBreakpoint(IMember method) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IMember container = null;
				try {
					container= BreakpointUtils.getMember((IJavaMethodBreakpoint) breakpoint);
				} catch (CoreException e) {
					return null;
				}
				if (method.equals(container)) {
					return (IJavaBreakpoint)breakpoint;
				}
			}
		}
		return null;
	}
	
	protected IMember getElement(ISelection s) {
		if (s instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) s;
			if (ss.size() == 1) {					
				Object o=  ss.getFirstElement();
				if (o instanceof IMethod)
					return (IMethod) o;
			}
		}
		return null;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (getBreakpoint() == null) {
			// add breakpoint
			try {
				IMethod method = (IMethod)getElement();
				int start = -1;
				int end = -1;
				ISourceRange range = method.getNameRange();
				if (range != null) {
					start = range.getOffset();
					end = start + range.getLength();
				}
				Map attributes = new HashMap(10);
				BreakpointUtils.addJavaBreakpointAttributes(attributes, method);
				String methodName = method.getElementName();
				if (((IMethod)method).isConstructor()) {
					methodName = "<init>"; //$NON-NLS-1$
				}
				setBreakpoint(JDIDebugModel.createMethodBreakpoint(BreakpointUtils.getBreakpointResource(method), 
					method.getDeclaringType().getFullyQualifiedName(), methodName, method.getSignature(), true, false, false, -1, start, end, 0, true, attributes));
			} catch (CoreException x) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageMethodBreakpointAction.Problems_creating_breakpoint_7"), x.getMessage()); //$NON-NLS-1$
			}
		} else {
			// remove breakpoint
			try {
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				breakpointManager.removeBreakpoint(getBreakpoint(), true);
			} catch (CoreException x) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageMethodBreakpointAction.Problems_removing_breakpoint_8"), x.getMessage()); //$NON-NLS-1$
			}
		}
		update();
	}
	
	protected String resolveMethodSignature(IMethod method) {
		try {
			IType type= method.getDeclaringType();
			String signature= method.getSignature();
			String[] pTypes= Signature.getParameterTypes(signature);
			String returnType= Signature.getReturnType(signature);
			String[] resolvedTypes= new String[pTypes.length];
			for (int i = 0; i < pTypes.length; i++) {
				String unresolvedTypeName= pTypes[i];
				unresolvedTypeName= unresolvedTypeName.substring(1, unresolvedTypeName.length() - 2);
				String[][] resolvedTypeNames= type.resolveType(unresolvedTypeName);
				if (resolvedTypeNames == null || resolvedTypeNames.length > 1) {
					//cannot be uniquely resolved
					return null;
				}
				StringBuffer buf= new StringBuffer();
				for (int j= 0; j < resolvedTypeNames[0].length; j++) {
					buf.append(resolvedTypeNames[0][j]);
				}
				resolvedTypes[i]= buf.toString();
			}
			
			
			/*String[][] resolvedTypeNames= type.resolveType(returnType);
			if (resolvedTypeNames == null || resolvedTypeNames.length > 1) {
					//cannot be uniquely resolved
					return null;
			}
			StringBuffer buf= new StringBuffer();
			for (int j= 0; j < resolvedTypeNames[0].length; j++) {
				buf.append(resolvedTypeNames[0][j]);
			}
			returnType= buf.toString();*/
			return Signature.createMethodSignature(resolvedTypes, returnType);	
		} catch (JavaModelException jme) {
			JDIDebugUIPlugin.logError(jme);
			return null;
		} 
		
	}
}