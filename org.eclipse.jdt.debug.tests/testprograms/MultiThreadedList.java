import java.util.ArrayList;
import java.util.List;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

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
