package org.eclipse.jdt.internal.debug.pattern;

import com.sun.jdi.VirtualMachine;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.debug.core.JDIDebugModel;

/**
 * Creates pattern debug targets.
 */
public class PatternDebugModel {
	
	/**
	 * The type identifier for a pattern breakpoint.
	 */
	public static final String PATTERN_BREAKPOINT = JDIDebugModel.getPluginIdentifier() + ".patternBreakpoint";
	
	/**
	 * The <code>PATTERN</code> field is used to match 
	 * type names to patterns given by breakpoints.
	 * This is used to install breakpoints.
	 */
	public static final String PATTERN= "pattern";
	
	/**
	 * Not to be instantiated.
	 */
	private PatternDebugModel() {}
	
	/**
	 * Creates a new PatternDebugTarget.
	 * @see org.eclipse.jdt.internal.debug.core.JDIDebugModel.newDebugTarget(VirtualMachine, String, IProcess, boolean, boolean)
	 */
	public static IDebugTarget newDebugTarget(VirtualMachine vm, String name, IProcess process, boolean allowTerminate, boolean allowDisconnect) {
		TargetCreator creator= new TargetCreator(vm, name, process, allowTerminate, allowDisconnect);
		try {
			ResourcesPlugin.getWorkspace().run(creator, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return creator.getTarget();
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceRunnable
	 */	
	static class TargetCreator implements IWorkspaceRunnable {
		IDebugTarget fTarget;
		VirtualMachine fVM;
		String fName;
		IProcess fProcess;
		boolean fAllowTerminate;
		boolean fAllowDisconnect;
		
		public TargetCreator(VirtualMachine vm, String name, IProcess process, boolean allowTerminate, boolean allowDisconnect) {
			fVM= vm;
			fName= name;
			fProcess= process;
			fAllowTerminate= allowTerminate;
			fAllowDisconnect= allowDisconnect;
		}
		
		public void run(IProgressMonitor m) {
			fTarget= new PatternDebugTarget(fVM, fName, fAllowTerminate, fAllowDisconnect, fProcess);
		}
		
		public IDebugTarget getTarget() {
			return fTarget;
		}
	}
}
