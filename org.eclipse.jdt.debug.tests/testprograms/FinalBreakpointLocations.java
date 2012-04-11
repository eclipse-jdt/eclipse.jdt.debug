/*******************************************************************************
 *  Copyright (c) 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

public class FinalBreakpointLocations {
	public int bar = 0;
	public final int foo = 0;
    public final FinalBreakpointLocations ft1 = new FinalBreakpointLocations() {
    	public void method() {
    		System.out.println("ft1"); //bp here
    	};
		public void method2() {
    		final FinalBreakpointLocations ftinner = new FinalBreakpointLocations() {
    	    	public void method() {
    	    		System.out.println("ftinner"); //bp here
    	    	};
    	    }; 
    	}
    }; 
    
    public final FinalBreakpointLocations ft2;
    public FinalBreakpointLocations() {
    	ft2 = new FinalBreakpointLocations() {
    		
    	};
    }
}