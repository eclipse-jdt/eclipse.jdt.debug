package org.eclipse.jdt.debug.testplugin;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

import junit.extensions.TestSetup;
import junit.framework.Test;

public class JavaTestSetup extends TestSetup {
	
	/**
	 * @deprecated
	 * Not needed anymore. No added value
	 */
	public JavaTestSetup(Test test) {
		super(test);
	}	
	
	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}	
}