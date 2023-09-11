/*******************************************************************************
 * Copyright (c) 2016 Google, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Google, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.connectors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;

public class MockLaunch implements ILaunch {
	private final ConcurrentLinkedDeque<IProcess> processes = new ConcurrentLinkedDeque<>();
	private final ConcurrentLinkedDeque<IDebugTarget> targets = new ConcurrentLinkedDeque<>();
	private ISourceLocator sourceLocator;
	private final Map<String, String> attributes = new HashMap<>();

	@Override
	public boolean canTerminate() {
		for (IProcess p : processes) {
			if (p.canTerminate()) {
				return true;
			}
		}
		for (IDebugTarget t : targets) {
			if (t.canTerminate()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isTerminated() {
		for (IProcess p : processes) {
			if (!p.isTerminated()) {
				return false;
			}
		}
		for (IDebugTarget t : targets) {
			if (!t.isTerminated()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void terminate() throws DebugException {
		for (Iterator<IProcess> iter = processes.iterator(); iter.hasNext(); iter.remove()) {
			IProcess p = iter.next();
			if (p.canTerminate()) {
				p.terminate();
			}
		}
		for (Iterator<IDebugTarget> iter = targets.iterator(); iter.hasNext(); iter.remove()) {
			IDebugTarget t = iter.next();
			if (t.canTerminate()) {
				t.terminate();
			}
		}
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public Object[] getChildren() {
		return new Object[0];
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return null;
	}

	@Override
	public IProcess[] getProcesses() {
		return processes.toArray(new IProcess[processes.size()]);
	}

	@Override
	public IDebugTarget[] getDebugTargets() {
		return targets.toArray(new IDebugTarget[targets.size()]);
	}

	@Override
	public void addDebugTarget(IDebugTarget target) {
		targets.add(target);
	}

	@Override
	public void removeDebugTarget(IDebugTarget target) {
		targets.remove(target);
	}

	@Override
	public void addProcess(IProcess process) {
		processes.add(process);
	}

	@Override
	public void removeProcess(IProcess process) {
		processes.remove(process);
	}

	@Override
	public ISourceLocator getSourceLocator() {
		return sourceLocator;
	}

	@Override
	public void setSourceLocator(ISourceLocator locator) {
		sourceLocator = locator;
	}

	@Override
	public String getLaunchMode() {
		return ILaunchManager.RUN_MODE;
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration() {
		return null;
	}

	@Override
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	@Override
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public boolean hasChildren() {
		return getChildren().length > 0;
	}
}