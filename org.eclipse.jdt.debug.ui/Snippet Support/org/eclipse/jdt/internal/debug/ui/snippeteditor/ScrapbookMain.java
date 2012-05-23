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
package org.eclipse.jdt.internal.debug.ui.snippeteditor;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Support class for launching a snippet evaluation
 */
public class ScrapbookMain {
	
	public static void main(String[] args) {

		URL[] urls= getClasspath(args);
		if (urls == null) return;
		
		while (true) {
			try {
				evalLoop(urls);
			} catch (ClassNotFoundException e) {
				return;
			} catch (NoSuchMethodException e) {
				return;
			} catch (InvocationTargetException e) {
				return;
			} catch (IllegalAccessException e) {
				return;
			}
		}
	
	}
	
	static void evalLoop(URL[] urls) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		ClassLoader cl= new URLClassLoader(urls, null);
		Class<?> clazz= cl.loadClass("org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain1"); //$NON-NLS-1$
		Method method= clazz.getDeclaredMethod("eval", new Class[] {Class.class}); //$NON-NLS-1$
		method.invoke(null, new Object[] {ScrapbookMain.class});
	}
	
	public static void nop() {
		try {
			Thread.sleep(100);
		} catch(InterruptedException e) {
		}
	}
	
	
	static URL[] getClasspath(String[] urlStrings) {
		
		//The URL Strings MUST be properly encoded
		//using URLEncoder...see ScrapbookLauncher for details
		URL[] urls= new URL[urlStrings.length + 1];
		
		for (int i = 0; i < urlStrings.length; i++) {
			try {
				urls[i + 1] = new URL(URLDecoder.decode(urlStrings[i]));
			} catch (MalformedURLException e) {
				return null;
			}
		}

		ProtectionDomain pd = ScrapbookMain.class.getProtectionDomain();
		if (pd == null) return null;
		CodeSource cs = pd.getCodeSource();
		if (cs == null) return null;
		urls[0] = cs.getLocation();

		return urls;
	}
}
