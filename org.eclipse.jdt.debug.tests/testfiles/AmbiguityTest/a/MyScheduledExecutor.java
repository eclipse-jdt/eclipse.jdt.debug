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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
public class MyScheduledExecutor {
    public void executeTask() {
    	Wor worker = new Wor();
    }
    class Worker {
        public void doWork(Wor w) {
        	System.out.println("Expected_Result");
        }
    }
    class Wor {
    	
    }
}
