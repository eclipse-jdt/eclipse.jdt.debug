/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval;


import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;

public class JavaEvaluationEngineManager implements IDebugEventSetListener {

	/**
	 * A mapping of maps that associates combinations of debug
	 * targets and projects to evaluation engines.
	 * 
	 * The outer map associates debug
	 * targets with a map. The inner maps map projects to
	 * evaluation engines.
	 * 
	 */
	HashMap fTargetMap= new HashMap();

	public JavaEvaluationEngineManager() {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/**
	 * @see IDebugEventSetListener#handleDebugEvent(DebugEvent)
	 * 
	 * Removes debug targets from the engine map when they terminate,
	 * and dispose of engines.
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		DebugEvent event;
		for (int i= 0, numEvents= events.length; i < numEvents; i++) {
			event= events[i];
			if (event.getKind() == DebugEvent.TERMINATE && event.getSource() instanceof IJavaDebugTarget) {
				HashMap map = (HashMap)fTargetMap.remove(event.getSource());
				if (map != null) {
					Iterator iter = map.values().iterator();
					while (iter.hasNext()) {
						((IAstEvaluationEngine)iter.next()).dispose();
					}
					map.clear();
				}
			}
		}
	}
	
	/**
	 * Returns an evaluation engine for the given project and debug target.
	 * If an engine already exists for this project and target combination,
	 * that same engine is returned. Otherwise, a new engine is created.
	 */
	public IAstEvaluationEngine getEvaluationEngine(IJavaProject project, IJavaDebugTarget target) {
		IAstEvaluationEngine engine= null;
		HashMap map= (HashMap)fTargetMap.get(target);
		if (map == null) {
			map= new HashMap();
			fTargetMap.put(target, map);
		}
		engine= (IAstEvaluationEngine)map.get(project);
		if (engine == null) {
			engine= EvaluationManager.newAstEvaluationEngine(project, target);
			map.put(project, engine);
		}
		return engine;
	}
	
	/**
	 * Disposes this evaluation engine manager.
	 * When disposed, the manager disposes all engines
	 * it is currently managing.
	 * 
	 * After this evaluation engine manager has been disposed, it
	 * must not be reused.
	 */
	public void dispose() {
		HashMap engines;
		Iterator iter= fTargetMap.values().iterator();
		while (iter.hasNext()) {
			engines= ((HashMap)iter.next());
			Iterator engineIter= engines.values().iterator();
			while (engineIter.hasNext()) {
				IAstEvaluationEngine engine = (IAstEvaluationEngine)engineIter.next();
				engine.dispose();
			}
			engines.clear();
		}
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

}
