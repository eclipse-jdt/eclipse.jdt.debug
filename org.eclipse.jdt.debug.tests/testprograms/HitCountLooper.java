/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

public class HitCountLooper {
	public static void main(String[] args) {
		int i = 0;
		while (i < 20) {
			System.out.println("Main Looping " + i);
			i++;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}