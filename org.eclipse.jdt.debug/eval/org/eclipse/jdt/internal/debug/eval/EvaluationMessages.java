package org.eclipse.jdt.internal.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class EvaluationMessages {

	private static final String BUNDLE_NAME =
		"org.eclipse.jdt.internal.debug.eval.EvaluationMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(BUNDLE_NAME);

	private EvaluationMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}