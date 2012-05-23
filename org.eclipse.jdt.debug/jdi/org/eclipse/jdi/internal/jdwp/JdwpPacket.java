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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * This class implements the corresponding Java Debug Wire Protocol (JDWP)
 * packet declared by the JDWP specification.
 * 
 */
public abstract class JdwpPacket {
	/** General JDWP constants. */
	public static final byte FLAG_REPLY_PACKET = (byte) 0x80;
	protected static final int MIN_PACKET_LENGTH = 11;

	/** Map with Strings for flag bits. */
	private static String[] fgFlagStrings = null;

	/** Header fields. */
	protected int fId = 0;
	protected byte fFlags = 0;
	protected byte[] fDataBuf = null;

	/**
	 * Set Id.
	 */
	/* package */void setId(int id) {
		fId = id;
	}

	/**
	 * @return Returns Id.
	 */
	public int getId() {
		return fId;
	}

	/**
	 * Set Flags.
	 */
	/* package */void setFlags(byte flags) {
		fFlags = flags;
	}

	/**
	 * @return Returns Flags.
	 */
	public byte getFlags() {
		return fFlags;
	}

	/**
	 * @return Returns total length of packet.
	 */
	public int getLength() {
		return MIN_PACKET_LENGTH + getDataLength();
	}

	/**
	 * @return Returns length of data in packet.
	 */
	public int getDataLength() {
		return fDataBuf == null ? 0 : fDataBuf.length;
	}

	/**
	 * @return Returns data of packet.
	 */
	public byte[] data() {
		return fDataBuf;
	}

	/**
	 * @return Returns DataInputStream with reply data, or an empty stream if
	 *         there is none.
	 */
	public DataInputStream dataInStream() {
		if (fDataBuf != null) {
			return new DataInputStream(new ByteArrayInputStream(fDataBuf));
		}

		return new DataInputStream(new ByteArrayInputStream(new byte[0]));
	}

	/**
	 * Assigns data to packet.
	 */
	public void setData(byte[] data) {
		fDataBuf = data;
	}

	/**
	 * Reads header fields that are specific for a type of packet.
	 */
	protected abstract int readSpecificHeaderFields(byte[] bytes, int index)
			throws IOException;

	/**
	 * Writes header fields that are specific for a type of packet.
	 */
	protected abstract int writeSpecificHeaderFields(byte[] bytes, int index)
			throws IOException;

	/**
	 * Constructs a JdwpPacket from a byte[].
	 */
	public static JdwpPacket build(byte[] bytes) throws IOException {
		// length (int)
		int a = (bytes[0] & 0xff) << 24;
		int b = (bytes[1] & 0xff) << 16;
		int c = (bytes[2] & 0xff) << 8;
		int d = (bytes[3] & 0xff) << 0;
		int packetLength = a + b + c + d;

		// id (int)
		a = (bytes[4] & 0xff) << 24;
		b = (bytes[5] & 0xff) << 16;
		c = (bytes[6] & 0xff) << 8;
		d = (bytes[7] & 0xff) << 0;
		int id = a + b + c + d;

		// flags (byte)
		byte flags = bytes[8];

		// Determine type: command or reply.
		JdwpPacket packet;
		if ((flags & FLAG_REPLY_PACKET) != 0)
			packet = new JdwpReplyPacket();
		else
			packet = new JdwpCommandPacket();

		// Assign generic header fields.
		packet.setId(id);
		packet.setFlags(flags);

		// Read specific header fields and data.
		int index = 9;
		index += packet.readSpecificHeaderFields(bytes, 9);
		if (packetLength - MIN_PACKET_LENGTH > 0) {
			packet.fDataBuf = new byte[packetLength - MIN_PACKET_LENGTH];
			System.arraycopy(bytes, index, packet.fDataBuf, 0,
					packet.fDataBuf.length);
		}

		return packet;
	}

	public byte[] getPacketAsBytes() throws IOException {
		int len = getLength();
		byte[] bytes = new byte[len];

		// convert len to bytes
		bytes[0] = (byte) (len >>> 24);
		bytes[1] = (byte) (len >>> 16);
		bytes[2] = (byte) (len >>> 8);
		bytes[3] = (byte) (len >>> 0);

		// convert id to bytes
		int id = getId();
		bytes[4] = (byte) (id >>> 24);
		bytes[5] = (byte) (id >>> 16);
		bytes[6] = (byte) (id >>> 8);
		bytes[7] = (byte) (id >>> 0);

		// flags
		bytes[8] = getFlags();

		// convert specific header fields
		int index = 9;
		index += writeSpecificHeaderFields(bytes, index);

		if (index < len && fDataBuf != null) {
			// copy data
			System.arraycopy(fDataBuf, 0, bytes, index, fDataBuf.length);
		}
		return bytes;
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fgFlagStrings != null) {
			return;
		}

		Field[] fields = JdwpPacket.class.getDeclaredFields();
		fgFlagStrings = new String[8];

		for (Field field : fields) {
			if ((field.getModifiers() & Modifier.PUBLIC) == 0
					|| (field.getModifiers() & Modifier.STATIC) == 0
					|| (field.getModifiers() & Modifier.FINAL) == 0) {
				continue;
			}

			String name = field.getName();
			if (!name.startsWith("FLAG_")) {//$NON-NLS-1$
				continue;
			}

			name = name.substring(5);

			try {
				byte value = field.getByte(null);

				for (int j = 0; j < fgFlagStrings.length; j++) {
					if ((1 << j & value) != 0) {
						fgFlagStrings[j] = name;
						break;
					}
				}
			} catch (IllegalAccessException e) {
				// Will not occur for own class.
			} catch (IllegalArgumentException e) {
				// Should not occur.
				// We should take care that all public static final constants
				// in this class are bytes.
			}
		}
	}

	/**
	 * @return Returns a mapping with string representations of flags.
	 */
	public static String[] getFlagMap() {
		getConstantMaps();
		return fgFlagStrings;
	}
}
