package org.eclipse.jdt.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.util.Hashtable;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.internal.debug.eval.LocalEvaluationEngine;

/**
 * The evaluation manager provides factory methods for
 * creating evaluation engines, and maintains a cache
 * of active evaluation engines per VM.
 * <p>
 * Clients are not intended subclass or instantiate this
 * class.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IEvaluationResult
 * @see IEvaluationListener
 * @since 2.0
 */
public class EvaluationManager {
		
	/**
	 * The singleton evaluation manager.
	 */
	private static EvaluationManager fgManager;
	
	/**
	 * Cache of active evaluation engines by VM.
	 * A mapping of <code>IJavaDebugTarget</code>
	 * to <code>IEvaluationEngine</code>.
	 */
	private Hashtable fEngines;
	
	/**
	 * Debug event handler to monitor VMs
	 * for termination.
	 */
	private VMMonitor fMonitor;
	
	/**
	 * Constructs a new evaluation manager.
	 */
	private EvaluationManager() {
		fgManager = this;
	}
	
	/**
	 * Returns the default (single) evaluation manager.
	 * 
	 * @return evaluation manager
	 */
	private static EvaluationManager getDefault() {
		if (fgManager == null) {
			fgManager = new EvaluationManager();
		}
		return fgManager;
	}
		
	/**
	 * Returns any existing evaluation engine for the
	 * specified debug target, or <code>null<code> if
	 * none currently exist.
	 * 
	 * @param vm the vm for which an engine is requested
	 * @return evaluation engine, or <code>null</code>
	 */
	public static IEvaluationEngine getEvaluationEngine(IJavaDebugTarget vm) {
		return getDefault().getEvaluationEngine0(vm);
	}
	
	/**
	 * Returns any existing evaluation engine for the
	 * specified debug target, or <code>null<code> if
	 * none currently exist.
	 * 
	 * @param vm the vm for which an engine is requested
	 * @return evaluation engine, or <code>null</code>
	 */
	private IEvaluationEngine getEvaluationEngine0(IJavaDebugTarget vm) {
		if (fEngines == null) {
			return null;
		} else {
			return (IEvaluationEngine)fEngines.get(vm);
		}
	}
		
	/**
	 * Adds the given engine to the cache of active engines.
	 * If this is the first engine created, this manager is
	 * registered as a debug event handler such that cleanup
	 * can be performed when VMs terminate.
	 * 
	 * @param engine the engine to add to the cache
	 */
	private void addEvaluationEngine(IEvaluationEngine engine) {
		if (fEngines == null) {
			fEngines = new Hashtable();
			fMonitor = new VMMonitor();
			DebugPlugin.getDefault().addDebugEventListener(fMonitor);
		}
		fEngines.put(engine.getDebugTarget(), engine);
	}
	
	/**
	 * Removes the given engine from the cache of active engines.
	 * If the cache becomes empty, it is re-set to <code>null</code>
	 * and this managers VM monitor is deregistered as a debug event
	 * handler.
	 * 
	 * @param engine the engine to dispose.
	 */
	private void disposeEvaluationEngine(IEvaluationEngine engine) {
		if (fEngines != null) {
			fEngines.remove(engine.getDebugTarget());
			if (fEngines.isEmpty()) {
				fEngines = null;
				DebugPlugin.getDefault().removeDebugEventListener(fMonitor);
				fMonitor = null;
			}
		}
		engine.dispose();
	}
	
	/**
	 * A debug event handler to monitor the termination
	 * of VMs. When a VM terminates, its evaluation engine
	 * is disposed.
	 */
	class VMMonitor implements IDebugEventListener {
		/**
		 * Disposes any evaluation engines for targets that
		 * terminate.
		 * 
		 * @see IDebugEventListener#handleDebugEvent(DebugEvent)
		 */
		public void handleDebugEvent(DebugEvent event) {
			if (event.getSource() instanceof IJavaDebugTarget && event.getKind() == DebugEvent.TERMINATE) {
				IEvaluationEngine engine = EvaluationManager.this.getEvaluationEngine((IJavaDebugTarget)event.getSource());
				if (engine != null) {
					disposeEvaluationEngine(engine);
				}
			}
		}

	}
	
	/**
	 * Creates and retuns a new evaluation engine that
	 * performs evaluations for local Java applications
	 * by deploying class files.
	 * 
	 * @param project the java project in which snippets
	 *  are to be compiled
	 * @param vm the java debug target in which snippets
	 *  are to be evaluated
	 * @param directory the directory where support class files
	 *  are deployed to assist in the evaluation. The directory
	 *  must exist.
	 */
	public static IClassFileEvaluationEngine newClassFileEvaluationEngine(IJavaProject project, IJavaDebugTarget vm, File directory) {
		IClassFileEvaluationEngine engine = new LocalEvaluationEngine(project, vm, directory);
		getDefault().addEvaluationEngine(engine);
		return engine;
	}
	 

}

