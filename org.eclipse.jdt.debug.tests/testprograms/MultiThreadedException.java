import java.util.Vector;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

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
