/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Point;

/**
 * A JDIImageDescriptor consists of a main icon and several adornments. The adornments
 * are computed according to flags set on creation of the descriptor.
 */
public class JDIImageDescriptor extends CompositeImageDescriptor {

	/** Flag to render the is out of synch adornment */
	public final static int IS_OUT_OF_SYNCH= 			0x0001;
	/** Flag to render the may be out of synch adornment */
	public final static int MAY_BE_OUT_OF_SYNCH= 		0x0002;
	/** Flag to render the installed breakpoint adornment */
	public final static int INSTALLED= 					0x0004;
	/** Flag to render the entry method breakpoint adornment */
	public final static int ENTRY=				 		0x0008;
	/** Flag to render the exit method breakpoint adornment */
	public final static int EXIT=				 		0x0010;
	/** Flag to render the enabled breakpoint adornment */
	public final static int ENABLED=						0x0020;
	/** Flag to render the conditional breakpoint adornment */
	public final static int CONDITIONAL=					0x0040;
	/** Flag to render the caught breakpoint adornment */
	public final static int CAUGHT=						0x0080;
	/** Flag to render the uncaught breakpoint adornment */
	public final static int UNCAUGHT=					0x0100;
	/** Flag to render the scoped breakpoint adornment */
	public final static int SCOPED=						0x0200;

	/** Flag to render the owning a monitor thread adornment */
	public final static int OWNS_MONITOR=				0x0400;
	/** Flag to render the owned monitor adornment */
	public final static int OWNED_MONITOR=				0x0800;
	/** Flag to render the in contention monitor adornment */
	public final static int CONTENTED_MONITOR=			0x1000;
	/** Flag to render the in contention for monitor thread adornment */
	public final static int IN_CONTENTION_FOR_MONITOR=	0x2000;
	/** Flag to render the in deadlock adornment */
	public final static int IN_DEADLOCK= 				0x8000;

	/** Flag to render the synchronized stack frame adornment */
	public final static int SYNCHRONIZED=				0x4000;

	/** Flag to render the trigger point adornment */
	public final static int TRIGGER_POINT = 0x10000;

	/** Flag to render disabled due to trigger point adornment */
	public final static int TRIGGER_SUPPRESSED = 0x20000;

	public final static int LOGICAL_STRUCTURE = 0x400000;

	private ImageDescriptor fBaseImage;
	private int fFlags;
	private Point fSize;

	/**
	 * Create a new JDIImageDescriptor.
	 *
	 * @param baseImage an image descriptor used as the base image
	 * @param flags flags indicating which adornments are to be rendered
	 */
	public JDIImageDescriptor(ImageDescriptor baseImage, int flags) {
		setBaseImage(baseImage);
		setFlags(flags);
	}

	/**
	 * @see CompositeImageDescriptor#getSize()
	 */
	@Override
	protected Point getSize() {
		if (fSize == null) {
			CachedImageDataProvider provider = createCachedImageDataProvider(getBaseImage());
			setSize(new Point(provider.getWidth(), provider.getHeight()));
		}
		return fSize;
	}

	/**
	 * @see Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof JDIImageDescriptor)){
			return false;
		}

		JDIImageDescriptor other= (JDIImageDescriptor)object;
		return (getBaseImage().equals(other.getBaseImage()) && getFlags() == other.getFlags());
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getBaseImage().hashCode() | getFlags();
	}

	/**
	 * @see CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(createCachedImageDataProvider(getBaseImage()), 0, 0);
		drawOverlays();
	}

	private ImageDescriptor getImageDescriptor(String imageDescriptorKey) {
		return JavaDebugImages.getImageDescriptor(imageDescriptorKey);
	}
	/**
	 * Add any overlays to the image as specified in the flags.
	 */
	protected void drawOverlays() {
		int flags = getFlags();
		int x = 0;
		int y = 0;
		CachedImageDataProvider provider;
		if ((flags & IS_OUT_OF_SYNCH) != 0) {
			x = getSize().x;
			y = 0;
			provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_OUT_OF_SYNCH));
			x -= provider.getWidth();
			drawImage(provider, x, y);
		} else if ((flags & MAY_BE_OUT_OF_SYNCH) != 0) {
			x = getSize().x;
			y = 0;
			provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_MAY_BE_OUT_OF_SYNCH));
			x -= provider.getWidth();
			drawImage(provider, x, y);
		} else if ((flags & SYNCHRONIZED) != 0) {
			x = getSize().x;
			y = 0;
			provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_SYNCHRONIZED));
			x -= provider.getWidth();
			drawImage(provider, x, y);
		} else {
			if ((flags & IN_DEADLOCK) != 0) {
				x = 0;
				y = 0;
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_IN_DEADLOCK));
				drawImage(provider, x, y);
			}
			if ((flags & TRIGGER_POINT) != 0) {
				x = getSize().x;
				y = getSize().y;
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_IN_TRIGGER_POINT));
				x -= provider.getWidth();
				y -= provider.getHeight();
				drawImage(provider, x, y);
			} else if ((flags & TRIGGER_SUPPRESSED) != 0) {
				x = getSize().x;
				y = getSize().y;
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_TRIGGER_SUPPRESSED));
				x -= provider.getWidth();
				y -= provider.getHeight();
				drawImage(provider, x, y);
			}
			if ((flags & OWNED_MONITOR) != 0) {
				x = getSize().x;
				y = getSize().y;
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_OWNED));
				x -= provider.getWidth();
				y -= provider.getHeight();
				drawImage(provider, x, y);
			} else if ((flags & CONTENTED_MONITOR) != 0) {
				x = getSize().x;
				y = getSize().y;
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_IN_CONTENTION));
				x -= provider.getWidth();
				y -= provider.getHeight();
				drawImage(provider, x, y);
			} else if ((flags & OWNS_MONITOR) != 0) {
				x = getSize().x;
				y = 0;
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_OWNS_MONITOR));
				x -= provider.getWidth();
				drawImage(provider, x, y);
			} else if ((flags & IN_CONTENTION_FOR_MONITOR) != 0) {
				x = getSize().x;
				y = 0;
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_IN_CONTENTION_FOR_MONITOR));
				x -= provider.getWidth();
				drawImage(provider, x, y);
			} else {
				drawBreakpointOverlays();
			}
		}
	}

	protected void drawBreakpointOverlays() {
		int flags= getFlags();
		int x= 0;
		int y= 0;
		CachedImageDataProvider provider;
		if ((flags & INSTALLED) != 0) {
			x= 0;
			y= getSize().y;
			if ((flags & ENABLED) !=0) {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_BREAKPOINT_INSTALLED));
			} else {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_BREAKPOINT_INSTALLED_DISABLED));
			}

			y -= provider.getHeight();
			drawImage(provider, x, y);
		}
		if ((flags & CAUGHT) != 0) {
			if ((flags & ENABLED) !=0) {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_CAUGHT_BREAKPOINT));
			} else {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_CAUGHT_BREAKPOINT_DISABLED));
			}
			x= 0;
			y= 0;
			drawImage(provider, x, y);
		}
		if ((flags & UNCAUGHT) != 0) {
			if ((flags & ENABLED) !=0) {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_UNCAUGHT_BREAKPOINT));
			} else {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_UNCAUGHT_BREAKPOINT_DISABLED));
			}
			x = provider.getWidth();
			y = provider.getHeight();
			drawImage(provider, x, y);
		}
		if ((flags & SCOPED) != 0) {
			if ((flags & ENABLED) !=0) {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_SCOPED_BREAKPOINT));
			} else {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_SCOPED_BREAKPOINT_DISABLED));
			}
			x= 0;
			y= getSize().y;
			y -= provider.getHeight();
			drawImage(provider, x, y);
		}
		if ((flags & CONDITIONAL) != 0) {
			if ((flags & ENABLED) !=0) {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_CONDITIONAL_BREAKPOINT));
			} else {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_CONDITIONAL_BREAKPOINT_DISABLED));
			}
			x= 0;
			y= 0;
			drawImage(provider, x, y);
		}
		if ((flags & ENTRY) != 0) {
			x= getSize().x;
			y= 0;
			if ((flags & ENABLED) !=0) {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_METHOD_BREAKPOINT_ENTRY));
			} else {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_METHOD_BREAKPOINT_ENTRY_DISABLED));
			}
			x -= provider.getWidth();
			x = x - 2;
			drawImage(provider, x, y);
		}
		if ((flags & EXIT)  != 0){
			x= getSize().x;
			y= getSize().y;
			if ((flags & ENABLED) != 0) {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_METHOD_BREAKPOINT_EXIT));
			} else {
				provider = createCachedImageDataProvider(getImageDescriptor(JavaDebugImages.IMG_OVR_METHOD_BREAKPOINT_EXIT_DISABLED));
			}
			x -= provider.getWidth();
			x = x - 2;
			y -= provider.getHeight();
			drawImage(provider, x, y);
		}
	}
	protected ImageDescriptor getBaseImage() {
		return fBaseImage;
	}

	protected void setBaseImage(ImageDescriptor baseImage) {
		fBaseImage = baseImage;
	}

	protected int getFlags() {
		return fFlags;
	}

	protected void setFlags(int flags) {
		fFlags = flags;
	}

	protected void setSize(Point size) {
		fSize = size;
	}
}
