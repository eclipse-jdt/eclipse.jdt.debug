/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.jdi.tests;

import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;

/**
 * An event listener that handles all kinds of event coming from the VM.
 */

public interface EventListener {
	/**
	 * Handles an access watchpoint event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean accessWatchpoint(AccessWatchpointEvent event);
	/**
	 * Handles a breakpoint event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean breakpoint(BreakpointEvent event);
	/**
	 * Handles a class prepare event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean classPrepare(ClassPrepareEvent event);
	/**
	 * Handles a class unload event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean classUnload(ClassUnloadEvent event);
	/**
	 * Handles an exception event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean exception(ExceptionEvent event);
	/**
	 * Handles a method entry event
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean methodEntry(MethodEntryEvent event);
	/**
	 * Handles a method exit event
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean methodExit(MethodExitEvent event);
	/**
	 * Handles a modification watchpoint event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean modificationWatchpoint(ModificationWatchpointEvent event);
	/**
	 * Handles a step event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean step(StepEvent event);
	/**
	 * Handles a thread death event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean threadDeath(ThreadDeathEvent event);
	/**
	 * Handles a thread start event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean threadStart(ThreadStartEvent event);
	/**
	 * Handles a vm death event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean vmDeath(VMDeathEvent event);
	/**
	 * Handles a vm disconnect event.
	 * Returns whether the VM should be resumed if it was interrupted.
	 * @param event
	 * @return whether the VM should be resumed if it was interrupted.
	 */
	public boolean vmDisconnect(VMDisconnectEvent event);
}
