/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.lang.Error;

/**
 * A dummy test exception class
 * Inner class exception to use to avoid NPE or other exceptions being mis-handled in other contexts
 * -- see bug 164703
 */
public class TestException extends Error {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
