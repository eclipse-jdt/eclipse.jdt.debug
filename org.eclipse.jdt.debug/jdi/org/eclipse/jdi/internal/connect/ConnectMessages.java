package org.eclipse.jdi.internal.connect;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ConnectMessages {

	private static final String BUNDLE_NAME = "org.eclipse.jdi.internal.connect.ConnectMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private ConnectMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}