package org.eclipse.debug.jdi.tests.program;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * Interface type for target VM tests.
 * This interface is intended to be loaded by the target VM. 
 *
 * WARNING, WARNING:
 * Tests in org.eclipse.debug.jdi.tests assume the content of this interface.
 * So if this interface or one of the types in this
 * package is changed, the corresponding tests must also be changed.
 */

import java.io.OutputStream;

public interface Printable extends Cloneable {
	int CONSTANT = 1;
public void print(OutputStream out);
}
