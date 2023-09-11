/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.Assert;

/**
 * Helper class for accessing classes and members which cannot be accessed using standard Java
 * access control like private or package visible elements.
 *
 * @since 3.7
 */
public class Accessor {

	/** The class to access. */
	private final Class<?> fClass;
	/** The instance to access. */
	private Object fInstance;

	/**
	 * Creates an accessor for the given <code>instance</code> and <code>class</code>. Only
	 * non-inherited members that particular <code>class</code> can be accessed.
	 *
	 * @param instance
	 *            the instance
	 * @param clazz
	 *            the class
	 */
	public Accessor(Object instance, Class<?> clazz) {
		org.eclipse.core.runtime.Assert.isNotNull(instance);
		Assert.isNotNull(clazz);
		fInstance = instance;
		fClass = clazz;
	}

	/**
	 * Creates an accessor for the given <code>instance</code> and <code>class</code>. Only
	 * non-inherited members that particular <code>class</code> can be accessed.
	 *
	 * @param instance
	 *            the instance
	 * @param className
	 *            the name of the class
	 * @param classLoader
	 *            the class loader to use i.e. <code>getClass().getClassLoader()</code>
	 */
	public Accessor(Object instance, String className, ClassLoader classLoader) throws ClassNotFoundException {
		Assert.isNotNull(instance);
		Assert.isNotNull(className);
		Assert.isNotNull(classLoader);
		fInstance = instance;

		fClass = Class.forName(className, true, classLoader);
	}

	/**
	 * Creates an accessor for the given class.
	 * <p>
	 * In order to get the type information from the given arguments they must all be instanceof
	 * Object. Use {@link #Accessor(String, ClassLoader, Class[], Object[])} if this is not the
	 * case.
	 * </p>
	 *
	 * @param className
	 *            the name of the class
	 * @param classLoader
	 *            the class loader to use i.e. <code>getClass().getClassLoader()</code>
	 * @param constructorArgs
	 *            the constructor arguments which must all be instance of Object
	 */
	public Accessor(String className, ClassLoader classLoader, Object[] constructorArgs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		this(className, classLoader, getTypes(constructorArgs), constructorArgs);
	}

	/**
	 * Creates an accessor for the given class.
	 *
	 * @param className
	 *            the name of the class
	 * @param classLoader
	 *            the class loader to use i.e. <code>getClass().getClassLoader()</code>
	 * @param constructorTypes
	 *            the types of the constructor arguments
	 * @param constructorArgs
	 *            the constructor arguments
	 */
	public Accessor(String className, ClassLoader classLoader, Class<?>[] constructorTypes, Object[] constructorArgs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		fClass = Class.forName(className, true, classLoader);

		Constructor<?> constructor = fClass.getDeclaredConstructor(constructorTypes);

		Assert.isNotNull(constructor);
		constructor.setAccessible(true);
		fInstance = constructor.newInstance(constructorArgs);
	}

	/**
	 * Creates an accessor for the given class.
	 * <p>
	 * This constructor is used to access static stuff.
	 * </p>
	 *
	 * @param className
	 *            the name of the class
	 * @param classLoader
	 *            the class loader to use i.e. <code>getClass().getClassLoader()</code>
	 */
	public Accessor(String className, ClassLoader classLoader) throws ClassNotFoundException {
		fClass = Class.forName(className, true, classLoader);
	}

	/**
	 * Creates an accessor for the given class.
	 * <p>
	 * This constructor is used to access static stuff.
	 * </p>
	 *
	 * @param clazz
	 *            the class
	 */
	public Accessor(Class<?> clazz) {
		Assert.isNotNull(clazz);
		fClass = clazz;
	}

	/**
	 * Invokes the method with the given method name and arguments.
	 * <p>
	 * In order to get the type information from the given arguments all those arguments must be instance of Object. Use
	 * {@link #invoke(String, Class[], Object[])} if this is not the case.
	 * </p>
	 *
	 * @param methodName
	 *            the method name
	 * @param arguments
	 *            the method arguments which must all be instance of Object
	 * @return the method return value
	 */
	public Object invoke(String methodName, Object[] arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return invoke(methodName, getTypes(arguments), arguments);
	}

	/**
	 * Invokes the method with the given method name and arguments.
	 *
	 * @param methodName
	 *            the method name
	 * @param types
	 *            the argument types
	 * @param arguments
	 *            the method arguments
	 */
	public Object invoke(String methodName, Class<?>[] types, Object[] arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Method method = null;
		method = fClass.getDeclaredMethod(methodName, types);

		Assert.isNotNull(method);
		method.setAccessible(true);
		return method.invoke(fInstance, arguments);
	}

	/**
	 * Assigns the given value to the field with the given name.
	 *
	 * @param fieldName
	 *            the field name
	 * @param value
	 *            the value to assign to the field
	 */
	public void set(String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = getField(fieldName);
		field.set(fInstance, value);
	}

	/**
	 * Assigns the given value to the field with the given name.
	 *
	 * @param fieldName
	 *            the field name
	 * @param value
	 *            the value to assign to the field
	 */
	public void set(String fieldName, boolean value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = getField(fieldName);
		field.setBoolean(fInstance, value);
	}

	/**
	 * Assigns the given value to the field with the given name.
	 *
	 * @param fieldName
	 *            the field name
	 * @param value
	 *            the value to assign to the field
	 */
	public void set(String fieldName, int value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = getField(fieldName);
		field.setInt(fInstance, value);
	}

	/**
	 * Returns the value of the field with the given name.
	 *
	 * @param fieldName
	 *            the field name
	 * @return the value of the field
	 */
	public Object get(String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = getField(fieldName);
		return field.get(fInstance);
	}

	/**
	 * Returns the value of the field with the given name.
	 *
	 * @param fieldName
	 *            the field name
	 * @return the value of the field
	 */
	public boolean getBoolean(String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = getField(fieldName);
		return field.getBoolean(fInstance);
	}

	/**
	 * Returns the value of the field with the given name.
	 *
	 * @param fieldName
	 *            the field name
	 * @return the value of the field
	 */
	public int getInt(String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = getField(fieldName);
		return field.getInt(fInstance);
	}

	public Field getField(String fieldName) throws NoSuchFieldException, SecurityException {
		Field field = fClass.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field;
	}

	private static Class<?>[] getTypes(Object[] objects) {
		if (objects == null) {
			return null;
		}

		int length = objects.length;
		Class<?>[] classes = new Class[length];
		for (int i = 0; i < length; i++) {
			Assert.isNotNull(objects[i]);
			classes[i] = objects[i].getClass();
		}
		return classes;
	}

}
