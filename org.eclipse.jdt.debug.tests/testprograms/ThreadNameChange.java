/*******************************************************************************
 * Copyright (c) 2017 Andrey Loskutov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class ThreadNameChange {

	public static void main(String[] args) throws Exception {
		final TransferQueue queue = new LinkedTransferQueue();

		Thread t = new Thread(new Runnable() {
			public void run() {
				Thread thread = Thread.currentThread();
				try {
					queue.transfer("");

					thread.setName("2");

					// give debugger time to check the name
					queue.transfer("");
				} catch (InterruptedException e) {
				}
			}
		}, "1");
		t.start();

		// wait for thread to start, debugger should see "1" at this breakpoint
		queue.take();  // <-- bp 1
		System.out.println("thread name: " + t.getName());

		// second breakpoint, debugger should see "2" as thread name
		queue.take(); // <-- bp 2
		System.out.println("thread name: " + t.getName());
	}
}
