package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * A JDIImageDescriptor consists of a main icon and several adornments. The adornments
 * are computed according to flags set on creation of the descriptor.
 */
public class JDIImageDescriptor extends CompositeImageDescriptor {
	
	/** Flag to render the is out of synch adornment */
	public final static int IS_OUT_OF_SYNCH= 		0x001;
	/** Flag to render the may be out of synch adornment */
	public final static int MAY_BE_OUT_OF_SYNCH= 	0x002;
	/** Flag to render the installed breakpoint adornment */
	public final static int INSTALLED= 				0x004;
	/** Flag to render the entry method breakpoint adornment */
	public final static int ENTRY=				 	0x008;
	/** Flag to render the exit method breakpoint adornment */
	public final static int EXIT=				 	0x010;
	/** Flag to render the enabled breakpoint adornment */
	public final static int ENABLED=				0x020;
	/** Flag to render the conditional breakpoint adornment */
	public final static int CONDITIONAL=				0x040;

	private ImageDescriptor fBaseImage;
	private int fFlags;
	private Point fSize;
	
	/**
	 * Create a new JDIImageDescriptor.
	 * 
	 * @param baseImage an image descriptor used as the base image
	 * @param flags flags indicating which adornments are to be rendered
	 * 
	 */
	public JDIImageDescriptor(ImageDescriptor baseImage, int flags) {
		setBaseImage(baseImage);
		setFlags(flags);
	}
	
	/**
	 * @see CompositeImageDescriptor#getSize()
	 */
	protected Point getSize() {
		if (fSize == null) {
			ImageData data= getBaseImage().getImageData();
			setSize(new Point(data.width, data.height));
		}
		return fSize;
	}
	
	/**
	 * @see Object#equals(java.lang.Object)
	 */
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
	public int hashCode() {
		return getBaseImage().hashCode() | getFlags();
	}
	
	/**
	 * @see CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	protected void drawCompositeImage(int width, int height) {
		ImageData bg= getBaseImage().getImageData();
		if (bg == null) {
			bg= DEFAULT_IMAGE_DATA;
		}
			
		drawImage(bg, 0, 0);
		drawOverlays();
	}	
	
	/**
	 * Add any overlays to the image as specified in the flags.
	 */
	private void drawOverlays() {
		
		ImageData data= null;
		int flags= getFlags();
		if ((flags & IS_OUT_OF_SYNCH) != 0) {
			int x= getSize().x;
			int y= 0;
			data= JavaDebugImages.DESC_OVR_IS_OUT_OF_SYNCH.getImageData();
			x -= data.width;
			drawImage(data, x, y);
		} else if ((flags & MAY_BE_OUT_OF_SYNCH) != 0) {
			int x= getSize().x;
			int y= 0;
			data= JavaDebugImages.DESC_OVR_MAY_BE_OUT_OF_SYNCH.getImageData();
			x -= data.width;
			drawImage(data, x, y);
		} else {
			if ((flags & INSTALLED) != 0) {
				int x= 0;
				int y= getSize().y;
				if ((flags & ENABLED) !=0) {
					data= JavaDebugImages.DESC_OBJS_BREAKPOINT_INSTALLED.getImageData();
				} else {
					data= JavaDebugImages.DESC_OBJS_BREAKPOINT_INSTALLED_DISABLED.getImageData();
				}
				
				y -= data.height;
				drawImage(data, x, y);
			}
			if ((flags & CONDITIONAL) != 0) {
				int x= getSize().x;
				int y= 0;
				if ((flags & ENABLED) !=0) {
					data= JavaDebugImages.DESC_OBJS_CONDITIONAL_BREAKPOINT.getImageData();
				} else {
					data= JavaDebugImages.DESC_OBJS_CONDITIONAL_BREAKPOINT_DISABLED.getImageData();
				}
				x -= data.width;
				drawImage(data, x, y);
			} else {
				if ((flags & ENTRY) != 0) {
					int x= getSize().x;
					int y= 0;
				
					if ((flags & ENABLED) !=0) {
						data= JavaDebugImages.DESC_OBJS_METHOD_BREAKPOINT_ENTRY.getImageData();
					} else {
						data= JavaDebugImages.DESC_OBJS_METHOD_BREAKPOINT_ENTRY_DISABLED.getImageData();
					}
					x -= data.width;
					drawImage(data, x, y);
				}
				if ((flags & EXIT)  != 0){
					int x= getSize().x;
					int y= getSize().y;
					if ((flags & ENABLED) != 0) {
						data= JavaDebugImages.DESC_OBJS_METHOD_BREAKPOINT_EXIT.getImageData();
					} else {
						data= JavaDebugImages.DESC_OBJS_METHOD_BREAKPOINT_EXIT_DISABLED.getImageData();
					}
					x -= data.width;
					y -= data.height;
					drawImage(data, x, y);
				}
			}	
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