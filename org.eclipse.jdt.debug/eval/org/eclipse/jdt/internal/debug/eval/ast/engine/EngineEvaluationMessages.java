package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class EngineEvaluationMessages {

	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.eval.ast.engine.EngineEvaluationMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private EngineEvaluationMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}