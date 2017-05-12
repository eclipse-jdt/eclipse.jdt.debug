/*******************************************************************************
 * Copyright (c) 2017 salesforce.com.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     salesforce.com - initial API and implementation
 *******************************************************************************/

public class BulkThreadCreationTest {

	public static void main(String[] args) throws Exception {
		int count = 1000;
		Thread[] threads = new Thread[count];
		for (int i = 0; i < count; i++) {
			threads[i] = new Thread("bulk-" + i);
			threads[i].start();
		}
		for (int i = 0; i < count; i++) {
			threads[i].join();
		}
		return;
	}

}
