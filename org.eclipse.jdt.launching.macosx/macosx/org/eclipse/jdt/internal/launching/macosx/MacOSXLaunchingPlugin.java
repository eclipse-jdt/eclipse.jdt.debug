/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */package org.eclipse.jdt.internal.launching.macosx;import org.eclipse.core.runtime.IPluginDescriptor;import org.eclipse.core.runtime.Plugin;
public class MacOSXLaunchingPlugin extends Plugin {	
	private static MacOSXLaunchingPlugin fgPlugin;	
	/**	 * Constructor for MacOSXLaunchingPlugin	 */	public MacOSXLaunchingPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgPlugin= this;	}
		public static MacOSXLaunchingPlugin getDefault() {		return fgPlugin;	}		/**	 * Convenience method which returns the unique identifier of this plugin.	 */	public static String getUniqueIdentifier() {		if (getDefault() == null) {			// If the default instance is not yet initialized,			// return a static identifier. This identifier must			// match the plugin id defined in plugin.xml			return "org.eclipse.jdt.launching.macosx"; //$NON-NLS-1$		}		return getDefault().getDescriptor().getUniqueIdentifier();	}}
