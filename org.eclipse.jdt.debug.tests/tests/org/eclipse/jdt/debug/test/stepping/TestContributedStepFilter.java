/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.test.stepping;

import org.eclipse.debug.core.model.IStepFilter;

import com.sun.jdi.Method;

/**
 * Contributed step filter
 */
public class TestContributedStepFilter implements IStepFilter {

	/**
	 * 
	 */
	public TestContributedStepFilter() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStepFilter#isFiltered(java.lang.Object)
	 */
	@Override
	public boolean isFiltered(Object object) {
		if (object instanceof Method) {
			Method method = (Method) object;
			return "StepFilterTwo".equals(method.declaringType().name()) && "contributed".equals(method.name());
		}
		return false;
	}

}
