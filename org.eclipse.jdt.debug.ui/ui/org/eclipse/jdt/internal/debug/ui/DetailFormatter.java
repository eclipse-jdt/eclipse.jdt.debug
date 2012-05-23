/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


/**
 * Information about a detail formatter.
 */
public class DetailFormatter implements Comparable<Object> {
	
	private boolean fEnabled;
	
	private String fTypeName;
	
	private String fSnippet;
	
	public DetailFormatter(String typeName, String snippet, boolean enabled) {
		fTypeName= typeName;
		fSnippet= snippet;
		fEnabled= enabled;
	}
	
	/**
	 * Indicate if this pretty should be used or not.
	 * @return boolean
	 */
	public boolean isEnabled() {
		return fEnabled;
	}

	/**
	 * Returns the code snippet.
	 * @return String
	 */
	public String getSnippet() {
		return fSnippet;
	}

	/**
	 * Returns the type name.
	 * @return String
	 */
	public String getTypeName() {
		return fTypeName;
	}

	/**
	 * Sets the enabled flag.
	 * @param enabled the new value of the flag
	 */
	public void setEnabled(boolean enabled) {
		fEnabled= enabled;
	}

	/**
	 * Sets the code snippet.
	 * @param snippet the snippet to set
	 */
	public void setSnippet(String snippet) {
		fSnippet= snippet;
	}

	/**
	 * Sets the type name.
	 * @param typeName the type name to set
	 */
	public void setTypeName(String typeName) {
		fTypeName= typeName;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object another) {
		DetailFormatter detailFormatter= (DetailFormatter)another;
		if (fTypeName == null) {
			if (detailFormatter.fTypeName == null) {
				return 0;
			}
			return detailFormatter.fTypeName.compareTo(fTypeName);
		}
		return fTypeName.compareTo(detailFormatter.fTypeName);
	}

}
