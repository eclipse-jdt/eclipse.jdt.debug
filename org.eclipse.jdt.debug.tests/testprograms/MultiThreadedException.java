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
import java.util.Vector;


public class MultiThreadedException {

	public static void main(String[] args) {
		MultiThreadedException mte = new MultiThreadedException();
		mte.go();
	}
	
	private void go() {
		Thread.currentThread().setName("1stThread");
		
		Thread secondThread = new Thread(new Runnable() {
			public void run() {
				generateNPE();
			}
		});
		secondThread.setName("2ndThread");
		secondThread.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		
		generateNPE();
	}
	
	void generateNPE() {
		Vector vector = null;
		if (1 > 2) {
			vector = new Vector();
		}
		vector.add("item");
	}
}
