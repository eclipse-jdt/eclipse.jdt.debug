/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.debug.jdi.tests.program;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Class loader that loads its own copy of {@link UnloadedClass}, so that the
 * class can be unloaded.
 */
public class UnloadedClassLoader extends URLClassLoader {

	/** A class loader of which the copy of UnloadedClass stays loaded. */
	static UnloadedClassLoader fKeptLoader;

	/** A class loader of which the copy of UnloadedClass is unloaded later. */
	static UnloadedClassLoader fLoaderToUnload;

	private UnloadedClassLoader() throws ClassNotFoundException {
		super(new URL[] { UnloadedClassLoader.class.getProtectionDomain().getCodeSource().getLocation() }, null);
		// Initializing the class also prepares it
		Class.forName("org.eclipse.debug.jdi.tests.program.UnloadedClass", true, this);
	}

	/**
	 * Trigger a class unload event for the front-end: loads a copy of
	 * UnloadedClass and waits until its class loader is collected. If asked,
	 * another copy is loaded first (once), with a class loader that stays
	 * alive.
	 */
	static void triggerClassUnloadEvent(boolean keepSameNameLoaded) {
		try {
			if (keepSameNameLoaded && fKeptLoader == null) {
				fKeptLoader = new UnloadedClassLoader();
			}
			waitUntilCollected(new WeakReference<>(new UnloadedClassLoader()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads a copy of UnloadedClass that stays loaded until
	 * {@link #triggerClassUnloadEventForLoadedClass()} is called.
	 */
	static void loadClassToUnload() {
		try {
			fLoaderToUnload = new UnloadedClassLoader();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Trigger a class unload event for the front-end, for the class that was
	 * loaded by {@link #loadClassToUnload()}.
	 */
	static void triggerClassUnloadEventForLoadedClass() {
		try {
			WeakReference<ClassLoader> loader = new WeakReference<>(fLoaderToUnload);
			fLoaderToUnload = null;
			waitUntilCollected(loader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void waitUntilCollected(WeakReference<ClassLoader> loader) throws InterruptedException {
		for (int i = 0; i < 50 && loader.get() != null; i++) {
			System.gc();
			Thread.sleep(100);
		}
	}
}
