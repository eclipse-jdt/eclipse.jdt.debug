/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
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
import java.util.concurrent.CountDownLatch;
public class EvalAnonymousClassVariableTests {
  public Runnable m1() {
    return new Runnable() {
      private int innerClassField;
      public void run() {
        innerClassField++; // << breakpoint goes here
        System.out.println(innerClassField);
      }
    };
  }

	public static void main(String[] args) {
		Runnable r = new EvalAnonymousClassVariableTests().m1();
		r.run();
		System.out.println("Tests ...");
		final CountDownLatch latch = new CountDownLatch(2);
		new Thread() {
			public void run() {
				latch.countDown();
			}
		}.start();
	}
}
