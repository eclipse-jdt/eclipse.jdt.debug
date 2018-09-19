/*******************************************************************************
 * Copyright (c) 2017 salesforce.com.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
