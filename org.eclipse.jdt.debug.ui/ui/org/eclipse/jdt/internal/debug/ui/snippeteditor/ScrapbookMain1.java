package org.eclipse.jdt.internal.debug.ui.snippeteditor;/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */import java.lang.reflect.InvocationTargetException;import java.lang.reflect.Method;public class ScrapbookMain1 {	public static void eval(Class clazz) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {		Method method=clazz.getDeclaredMethod("nop", new Class[0]); //$NON-NLS-1$		method.invoke(null, new Object[0]);	}}