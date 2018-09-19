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
package org.eclipse.debug.tests.targets;



public class HitCountLooper {
	public static void main(String[] args) {
		int i = 0;
		while (i < 20) {
			System.out.println("Main Looping " + i);
			i++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		(new HitCountLooper()).loop();
	}

	public void loop() {
		Runnable r= new Runnable() {
			public void run() {
				while (true) {
					int i = 0;
					System.out.println("Thread Looping " + i);
					i++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		new Thread(r).start();
		int i = 0;
		while (true) {
			System.out.println("Instance Looping " + i);
			i++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
}
