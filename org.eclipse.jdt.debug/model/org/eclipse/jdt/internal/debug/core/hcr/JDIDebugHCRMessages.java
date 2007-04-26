/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.hcr;

import org.eclipse.osgi.util.NLS;

public class JDIDebugHCRMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.core.hcr.JDIDebugHCRMessages";//$NON-NLS-1$
	public static String JavaHotCodeReplaceManager_Drop_to_frame_not_supported;
	public static String JavaHotCodeReplaceManager_does_not_support_hcr;
	public static String JavaHotCodeReplaceManager_exception_replacing_types;
	public static String JavaHotCodeReplaceManager_hcr_failed;
	public static String JavaHotCodeReplaceManager_hcr_ignored;
	public static String JavaHotCodeReplaceManager_hcr_unsupported_redefinition;
	public static String JavaHotCodeReplaceManager_hcr_unsupported_operation;
	public static String JavaHotCodeReplaceManager_hcr_bad_bytes;
	public static String JavaHotCodeReplaceManager_hcr_verify_error;
	public static String JavaHotCodeReplaceManager_hcr_unsupported_class_version;
	public static String JavaHotCodeReplaceManager_hcr_class_format_error;
	public static String JavaHotCodeReplaceManager_hcr_class_circularity_error;
	public static String JavaHotCodeReplaceManager_Hot_code_replace_failed___VM_disconnected__1;
	public static String JavaHotCodeReplaceManager_Hot_code_replace_failed___VM_disconnected__2;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, JDIDebugHCRMessages.class);
	}
}
