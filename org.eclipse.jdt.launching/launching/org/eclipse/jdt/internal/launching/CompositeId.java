/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */package org.eclipse.jdt.internal.launching;import java.util.ArrayList;

/**
 * Utility class for id's made of multiple Strings
 */
public class CompositeId {
	private String[] fParts;
	
	public CompositeId(String[] parts) {
		fParts= parts;
	}
	
	public static CompositeId fromString(String idString) {
		ArrayList parts= new ArrayList();
		int commaIndex= idString.indexOf(',');
		while (commaIndex > 0) {
			int length= Integer.valueOf(idString.substring(0, commaIndex)).intValue();
			String part= idString.substring(commaIndex+1, commaIndex+1+length);
			parts.add(part);
			idString= idString.substring(commaIndex+1+length);
			commaIndex= idString.indexOf(',');
		}
		String[] result= (String[])parts.toArray(new String[parts.size()]);
		return new CompositeId(result);
	}
	
	public String toString() {
		StringBuffer buf= new StringBuffer();
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
