/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.snippeteditor;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Support class for launching a snippet evaluation.
 * <p>
 * CAUTION: This class gets compiled with target=1.7, see scripts/buildExtraJAR.xml.
 */
public class ScrapbookMain {

	public static void main(String[] args) {

		URL[] urls= getClasspath(args);
		if (urls == null) {
			return;
		}

		while (true) {
			try {
				evalLoop(urls);
			} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException e) {
				return;
			}
		}

	}

	static void evalLoop(URL[] urls) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
		try (URLClassLoader cl = new URLClassLoader(urls, null)) {
			Class<?> clazz = cl.loadClass(JavaSnippetEditor.SCRAPBOOK_MAIN1_TYPE);
			Method method = clazz.getDeclaredMethod(JavaSnippetEditor.SCRAPBOOK_MAIN1_METHOD, new Class[] { Class.class });
			method.invoke(null, new Object[] {ScrapbookMain.class});
		}
	}

	/**
	 * The magic "no-op" method, where {@link org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher#createMagicBreakpoint(String)} sets a
	 * breakpoint.
	 * <p>
	 */
	public static void nop() {
		try {
			Thread.sleep(100);
		} catch(InterruptedException e) {
		}
	}


	static URL[] getClasspath(String[] urlStrings) {

		//The URL Strings MUST be properly encoded
		// using URLEncoder...see ScrapbookLauncher.getEncodedURL(File)
		URL[] urls= new URL[urlStrings.length + 1];

		for (int i = 0; i < urlStrings.length; i++) {
			try {
				urls[i + 1] = new URL(URLDecoder.decode(urlStrings[i], StandardCharsets.UTF_8.name()));
			} catch (MalformedURLException | UnsupportedEncodingException e) {
				return null;
			}
		}

		ProtectionDomain pd = ScrapbookMain.class.getProtectionDomain();
		if (pd == null) {
			return null;
		}
		CodeSource cs = pd.getCodeSource();
		if (cs == null) {
			return null;
		}
		urls[0] = cs.getLocation();

		return urls;
	}
}
