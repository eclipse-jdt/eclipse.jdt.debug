/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
public class BreakpointsLocationBug344984 {
    private final String fWorkingValues; // Breakpoint here 
    BreakpointsLocationBug344984() {
        fWorkingValues= null;
        System.out.println(fWorkingValues);
    }
}