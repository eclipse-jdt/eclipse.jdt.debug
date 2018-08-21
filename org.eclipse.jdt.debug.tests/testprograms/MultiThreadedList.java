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
import java.util.ArrayList;
import java.util.List;


public class MultiThreadedList {

	int i = 0;
	List list = new ArrayList();

	public static void main(String[] args) {
		MultiThreadedList mtl = new MultiThreadedList();
		Thread.currentThread().setName("1stThread");
		mtl.go();
	}
	
	protected void go() {
		Thread secondThread = new Thread(new Runnable() {
			public void run() {
				listLoop();
			}
		});
		secondThread.setName("2ndThread");
		secondThread.start();
		
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
		}
		
		listLoop();
	}
	
	private void listLoop() {
		while (i < 20) {
			list.add(new Integer(i++));
			System.out.println("Size = " + list.size());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}
