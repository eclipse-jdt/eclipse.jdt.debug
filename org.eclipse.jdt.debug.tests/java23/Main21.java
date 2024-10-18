/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation -- initial API and implementation
 *******************************************************************************/
public class Main21 {
	public static void main(String[] args) throws InterruptedException {
		try {
			Thread.startVirtualThread(() -> {
				int p = 21;
				System.out.println("From Virtual Thread");
			}).join();
		} catch (Exception e) {

		}
	}
}