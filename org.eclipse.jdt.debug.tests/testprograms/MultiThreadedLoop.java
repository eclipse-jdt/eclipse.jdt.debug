/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

public class MultiThreadedLoop {

	public static void main(String[] args) {
		MultiThreadedLoop mtl = new MultiThreadedLoop();
		Thread.currentThread().setName("1stThread");
		mtl.go();
	}
	
	protected void go() {
		Thread secondThread = new Thread(new Runnable() {
			public void run() {
				loop();
			}
		});
		secondThread.setName("2ndThread");
		secondThread.start();
		
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
		}
		
		loop();
	}
	
	private void loop() {
		int i = 0;
		while (i < 20) {
			System.out.println("Thread: " + Thread.currentThread().getName() + " loop #" + i++); 
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}
