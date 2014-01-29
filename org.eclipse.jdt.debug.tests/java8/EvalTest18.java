/*******************************************************************************
 * Copyright (c) 2014 Jesper Steen Møller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *******************************************************************************/

import java.util.Arrays;
import java.util.List;

public class EvalTest18 {
	public static void main(String[] args) {
		List<String> strings = Arrays.asList("One", "Two", "Three");
		System.out.println("Count of strings in stream from array =" + strings.stream().count());
	}
}