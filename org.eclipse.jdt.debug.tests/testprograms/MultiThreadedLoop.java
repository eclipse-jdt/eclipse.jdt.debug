/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

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
