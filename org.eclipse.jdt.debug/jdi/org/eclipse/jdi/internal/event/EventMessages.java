package org.eclipse.jdi.internal.event;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class EventMessages {

	private static final String BUNDLE_NAME = "org.eclipse.jdi.internal.event.EventMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private EventMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}