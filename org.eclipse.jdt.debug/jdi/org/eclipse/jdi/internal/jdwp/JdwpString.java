/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.jdwp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

/**
 * This class implements the corresponding Java Debug Wire Protocol (JDWP) ID
 * declared by the JDWP specification.
 * 
 */
public class JdwpString {
	/**
	 * Reads String from Jdwp stream. Read a UTF where length has 4 bytes, and
	 * not just 2. This code was based on the OTI Retysin source for readUTF.
	 */
	public static String read(DataInputStream in) throws IOException {
		int utfSize = in.readInt();
		byte utfBytes[] = new byte[utfSize];
		in.readFully(utfBytes);
		/* Guess at buffer size */
		StringBuffer strBuffer = new StringBuffer(utfSize / 3 * 2);
		for (int i = 0; i < utfSize;) {
			int a = utfBytes[i] & 0xFF;
			if ((a >> 4) < 12) {
				strBuffer.append((char) a);
				i++;
			} else {
				int b = utfBytes[i + 1] & 0xFF;
				if ((a >> 4) < 14) {
					if ((b & 0xBF) == 0) {
						throw new UTFDataFormatException(
								JDWPMessages.JdwpString_Second_byte_input_does_not_match_UTF_Specification_1);
					}
					strBuffer.append((char) (((a & 0x1F) << 6) | (b & 0x3F)));
					i += 2;
				} else {
					int c = utfBytes[i + 2] & 0xFF;
					if ((a & 0xEF) > 0) {
						if (((b & 0xBF) == 0) || ((c & 0xBF) == 0)) {
							throw new UTFDataFormatException(
									JDWPMessages.JdwpString_Second_or_third_byte_input_does_not_mach_UTF_Specification_2);
						}
						strBuffer.append((char) (((a & 0x0F) << 12)
								| ((b & 0x3F) << 6) | (c & 0x3F)));
						i += 3;
					} else {
						throw new UTFDataFormatException(
								JDWPMessages.JdwpString_Input_does_not_match_UTF_Specification_3);
					}
				}
			}
		}
		return strBuffer.toString();
	}

	/**
	 * Writes String to Jdwp stream. Write a UTF where length has 4 bytes, and
	 * not just 2. This code was based on OTI Retsin source for writeUTF.
	 */
	public static void write(String str, DataOutputStream out)
			throws IOException {
		if (str == null)
			throw new NullPointerException(
					JDWPMessages.JdwpString_str_is_null_4);
		int utfCount = 0;
		for (int i = 0; i < str.length(); i++) {
			int charValue = str.charAt(i);
			if (charValue > 0 && charValue <= 127)
				utfCount += 1;
			else if (charValue <= 2047)
				utfCount += 2;
			else
				utfCount += 3;
		}
		byte utfBytes[] = new byte[utfCount];
		int utfIndex = 0;
		for (int i = 0; i < str.length(); i++) {
			int charValue = str.charAt(i);
			if (charValue > 0 && charValue <= 127)
				utfBytes[utfIndex++] = (byte) charValue;
			else if (charValue <= 2047) {
				utfBytes[utfIndex++] = (byte) (0xc0 | (0x1f & (charValue >> 6)));
				utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & charValue));
			} else {
				utfBytes[utfIndex++] = (byte) (0xe0 | (0x0f & (charValue >> 12)));
				utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & (charValue >> 6)));
				utfBytes[utfIndex++] = (byte) (0x80 | (0x3f & charValue));
			}
		}
		out.writeInt(utfCount);
		if (utfCount > 0)
			out.write(utfBytes);
	}
}
