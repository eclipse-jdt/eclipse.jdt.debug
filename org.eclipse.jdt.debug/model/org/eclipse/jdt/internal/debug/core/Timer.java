package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

/**
 * A timer notifies listeners when a specific amount
 * of time has passed.
 */
public class Timer {
	
	protected ITimeoutListener fListener;
	protected int fTimeout;
	protected boolean fAlive = true;
	protected boolean fStarted = false;
	
	/**
	 * For efficiency, we use one thread instead of creating new
	 * threads for each request.
	 */
	private Thread fThread;
	
	/**
	 * Constructs a new timer
	 */
	public Timer() {
		fTimeout = Integer.MAX_VALUE;
		Runnable r = new Runnable() {
			public void run() {
				while (fAlive) {
					boolean interrupted = false;
					try {
						Thread.sleep(fTimeout);
					} catch (InterruptedException e) {
						interrupted = true;
					}
					if (!interrupted) {
						if (fListener != null) {
							fStarted = false;
							fTimeout = Integer.MAX_VALUE;
							fListener.timeout();
						}
					}
				}
			}
		};
		fThread = new Thread(r, "Step Timer");
		fThread.setDaemon(true);
		fThread.start();
	}

	/**
	 * Starts this timer, and notifies the given listener when
	 * the time has passed. A call to <code>stop</code>, before the
	 * time expires, will cancel the the timer and timeout callback.
	 * This method can only be called if this timer is idle (i.e.
	 * stopped, or expired).
	 */
	public void start(ITimeoutListener listener, int ms) {
		if (fStarted) {
			throw new IllegalStateException();
		}
		fListener = listener;
		fTimeout = ms;
		fStarted = true;
		fThread.interrupt();
	}
	
	/**
	 * Stops this timer
	 */
	public void stop() {
		fTimeout = Integer.MAX_VALUE;
		fStarted = false;
		fThread.interrupt();
	}
	
	/**
	 * Disposes this timer
	 */
	public void dispose() {
		fAlive = false;
		fThread.interrupt();
		fThread = null;
	}
	
}