package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ActionMessages {

	private static final String BUNDLE_NAME =
		"org.eclipse.jdt.internal.debug.ui.actions.ActionMessages";//$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(BUNDLE_NAME);

	private ActionMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}