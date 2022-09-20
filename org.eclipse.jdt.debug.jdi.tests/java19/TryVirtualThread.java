/*******************************************************************************
 * Copyright (c) 2022 Microsoft Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Microsoft Corporation - initial API and implementation
 *******************************************************************************/
import java.io.OutputStream;
import java.io.PrintWriter;

public class TryVirtualThread implements Runnable {
	/**
	 * A <code>Thread</code> object
	 */
	public static Thread fThread;

	/**
	 * A <code>Thread</code> object representing the main thread
	 */
	public static Thread fMainThread;
	
	public static Thread fVirtualThread;

	
	/**
	 * The instance of the <code>MainClass</code>
	 */
	public static TryVirtualThread fObject = new TryVirtualThread();

	/**
	 * An integer value
	 */
	public static int fInt = 0;
	
	/**
	 * A string initialized to 'hello world'
	 */
	public static String fString = "Hello World";
	
	/**
	 * The name of an event type
	 */
	public static String fEventType = "";
	
	/**
	 * Runs the test program
	 * @param args
	 */
	public static void main(java.lang.String[] args) {
		// Ensure at least one carrier thread is created.
		fVirtualThread = Thread.startVirtualThread(() -> {
			System.out.println("Start a test virtual thread.");
		});

		ThreadGroup group = new ThreadGroup("Test ThreadGroup");
		fThread = new Thread(group, fObject, "Test Thread");
		fThread.start();

		fMainThread = Thread.currentThread();

		// Prevent this thread from dying
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				printAndSignal();
				triggerEvent();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		} finally {
			System.out.println("Running finally block in MainClass.run()");
		}
	}
	
	/**
	 * Prints to System.out and throws an exception to indicate readiness
	 */
	synchronized public void printAndSignal() {
		print(System.out);
		// Signal readiness by throwing an exception
		try {
			throw new NegativeArraySizeException();
		} catch (NegativeArraySizeException exc) {
		}
	}

	public void print(OutputStream out) {
		String string = fInt++ +". " + fString;
		PrintWriter writer = new PrintWriter(out);
		writer.println(string);
		writer.flush();
	}
	
	/**
	 *	Trigger an event for the front-end.
	 */
	private void triggerEvent() {
		/* Ensure we do it only once */
		String eventType = fEventType;
		fEventType = "";

		/* Trigger event according to the field fEventType */
		if (eventType.isEmpty()) {
			return;
		} else if (eventType.equals("ThreadStartEvent")) {
			triggerThreadStartEvent();
		} else if (eventType.equals("ThreadDeathEvent")) {
			triggerThreadDeathEvent();
		} else {
			System.out.println("Unknown event type: " + eventType);
		}
	}
	
	/**
	 *	Trigger a thread end event for the front-end.
	 */
	private void triggerThreadDeathEvent() {
		Thread.startVirtualThread(() -> {
			System.out.println("Test VirtualThread Death Event");
		});
	}

	/**
	 *	Trigger a thread start event for the front-end.
	 */
	private void triggerThreadStartEvent() {
		Thread.startVirtualThread(() -> {
			System.out.println("Test VirtualThread Start Event");
		});
	}
}
