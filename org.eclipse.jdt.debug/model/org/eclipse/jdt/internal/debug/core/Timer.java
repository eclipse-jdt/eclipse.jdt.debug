package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jdt.debug.core.JDIDebugModel;

/**
 * A timer notifies listeners when a specific amount
 * of time has passed.
 * 
 * @see ITimeoutListener
 */
public class Timer {
	
	private ITimeoutListener fListener;
	private int fTimeout;
	private boolean fAlive = true;
	private boolean fStarted = false;
	
	/**
	 * The single thread used for each request.
	 */
	private Thread fThread;
	
	/**
	 * Constructs a new timer
	 */
	public Timer() {
		setTimeout(Integer.MAX_VALUE);
		Runnable r = new Runnable() {
			public void run() {
				while (isAlive()) {
					boolean interrupted = false;
					try {
						Thread.sleep(getTimeout());
					} catch (InterruptedException e) {
						interrupted = true;
					}
					if (!interrupted) {
						if (getListener() != null) {
							setStarted(false);
							setTimeout(Integer.MAX_VALUE);
							getListener().timeout();
						}
					}
				}
			}
		};
		setThread(new Thread(r, JDIDebugModel.getPluginIdentifier() + JDIDebugModelMessages.getString("Timer.label"))); //$NON-NLS-1$
		getThread().setDaemon(true);
		getThread().start();
	}

	/**
	 * Starts this timer, and notifies the given listener when
	 * the time has passed. A call to <code>stop</code>, before the
	 * time expires, will cancel the the timer and timeout callback.
	 * This method can only be called if this timer is idle (i.e.
	 * stopped, or expired).
	 * 
	 * @param listener The timer listener
	 * @param ms The number of milliseconds for this timer
	 */
	public void start(ITimeoutListener listener, int ms) {
		if (isStarted()) {
			throw new IllegalStateException(JDIDebugModelMessages.getString("Timer.exception_already_started")); //$NON-NLS-1$
		}
		setListener(listener);
		setTimeout(ms);
		setStarted(true);
		getThread().interrupt();
	}
	
	/**
	 * Stops this timer
	 */
	public void stop() {
		setTimeout(Integer.MAX_VALUE);
		setStarted(false);
		getThread().interrupt();
	}
	
	/**
	 * Disposes this timer
	 */
	public void dispose() {
		setAlive(false);
		getThread().interrupt();
		setThread(null);
	}
	
	protected boolean isAlive() {
		return fAlive;
	}

	protected void setAlive(boolean alive) {
		fAlive = alive;
	}

	protected ITimeoutListener getListener() {
		return fListener;
	}

	protected void setListener(ITimeoutListener listener) {
		fListener = listener;
	}

	protected boolean isStarted() {
		return fStarted;
	}

	protected void setStarted(boolean started) {
		fStarted = started;
	}

	protected Thread getThread() {
		return fThread;
	}

	protected void setThread(Thread thread) {
		fThread = thread;
	}

	protected int getTimeout() {
		return fTimeout;
	}

	protected void setTimeout(int timeout) {
		fTimeout = timeout;
	}
}