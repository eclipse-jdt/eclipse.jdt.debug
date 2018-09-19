/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;

/**
 * Utility class for id's made of multiple Strings
 */
public class CompositeId {
	private String[] fParts;

	public CompositeId(String[] parts) {
		fParts= parts;
	}

	public static CompositeId fromString(String idString) {
		ArrayList<String> parts= new ArrayList<>();
		int commaIndex= idString.indexOf(',');
		while (commaIndex > 0) {
			int length= Integer.valueOf(idString.substring(0, commaIndex)).intValue();
			String part= idString.substring(commaIndex+1, commaIndex+1+length);
			parts.add(part);
			idString= idString.substring(commaIndex+1+length);
			commaIndex= idString.indexOf(',');
		}
		String[] result= parts.toArray(new String[parts.size()]);
		return new CompositeId(result);
	}

	@Override
	public String toString() {
		StringBuilder buf= new StringBuilder();
		for (int i= 0; i < fParts.length; i++) {
			buf.append(fParts[i].length());
			buf.append(',');
			buf.append(fParts[i]);
		}
		return buf.toString();
	}

	public String get(int index) {
		return fParts[index];
	}

	public int getPartCount() {
		return fParts.length;
	}
}
