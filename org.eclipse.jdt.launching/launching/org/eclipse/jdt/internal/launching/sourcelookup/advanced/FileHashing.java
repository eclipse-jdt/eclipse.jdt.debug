/*******************************************************************************
 * Copyright (c) 2011, 2022 Igor Fedorenko
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.sourcelookup.advanced;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helpers to compute file content digests. Provides long-lived hasher instance with bounded cache of most recently requested files, which is useful
 * to handle source lookup requests. Also provides factory of hasher instances with unbounded caches, which is useful to perform bulk workspace
 * indexing.
 */
public class FileHashing {

	public static interface Hasher {
		Object hash(File file);
	}

	// default hasher with bounded cache.
	// this is used when performing source lookup and number of unique files requested during the same debugging session is likely to be small.
	private static final HasherImpl HASHER = new HasherImpl(5000);

	/**
	 * Returns default long-lived Hasher instance with bounded hash cache.
	 */
	public static Hasher hasher() {
		return HASHER;
	}

	/**
	 * Returns new Hasher instance with unbounded hash cache, useful for bulk hashing of projects and their dependencies.
	 */
	public static Hasher newHasher() {
		return new HasherImpl(HASHER);
	}

	private static class CacheKey {
		public final Object file;

		private final long length;

		private final long lastModified;

		public CacheKey(Object fileKey, BasicFileAttributes attributes) {
			this.file = fileKey;
			this.length = attributes.size();
			this.lastModified = attributes.lastModifiedTime().toMillis();
		}

		@Override
		public int hashCode() {
			int hash = 17;
			hash = hash * 31 + file.hashCode();
			hash = hash * 31 + (int) length;
			hash = hash * 31 + (int) lastModified;
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof CacheKey)) {
				return false;
			}
			CacheKey other = (CacheKey) obj;
			return file.equals(other.file) && length == other.length && lastModified == other.lastModified;
		}
	}

	private static class HashCode {
		private final byte[] bytes;

		public HashCode(byte[] bytes) {
			this.bytes = bytes; // assumes this class "owns" the array from now on
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(bytes);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof HashCode)) {
				return false;
			}
			return Arrays.equals(bytes, ((HashCode) obj).bytes);
		}

		@Override
		public final String toString() {
			StringBuilder sb = new StringBuilder(2 * bytes.length);
			for (byte b : bytes) {
				sb.append(hexDigits[(b >> 4) & 0xf]).append(hexDigits[b & 0xf]);
			}
			return sb.toString();
		}

		private static final char[] hexDigits = "0123456789abcdef".toCharArray(); //$NON-NLS-1$
	}

	private static class HasherImpl implements Hasher {

		private final Map<CacheKey, HashCode> cache;

		@SuppressWarnings("serial")
		public HasherImpl(int cacheSize) {
			this.cache = new LinkedHashMap<>() {
				@Override
				protected boolean removeEldestEntry(Map.Entry<CacheKey, HashCode> eldest) {
					return size() > cacheSize;
				}
			};
		}

		public HasherImpl(HasherImpl initial) {
			this.cache = new LinkedHashMap<>(initial.cache);
		}

		@Override
		public HashCode hash(File file) {
			if (file == null) {
				return null;
			}
			try {
				BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
				if (!attributes.isRegularFile()) {
					return null;
				}
				Object key= file.getAbsoluteFile().toPath().toAbsolutePath().normalize();
				CacheKey cacheKey = new CacheKey(key, attributes);
				synchronized (cache) {
					HashCode hashCode = cache.get(cacheKey);
					if (hashCode != null) {
						return hashCode;
					}
				}
				// don't hold cache lock while hashing file
				HashCode hashCode = computeHash(file);
				synchronized (cache) {
					cache.put(cacheKey, hashCode);
				}
				return hashCode;
			}
			catch (IOException e) {
				return null; // file does not exist or can't be read
			}
		}

	}

	private static HashCode computeHash(File file) throws IOException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Unsupported JVM", e); //$NON-NLS-1$
		}
		byte[] buf = new byte[4096];
		try (InputStream is = new FileInputStream(file)) {
			int len;
			while ((len = is.read(buf)) > 0) {
				digest.update(buf, 0, len);
			}
		}
		return new HashCode(digest.digest());
	}

}
