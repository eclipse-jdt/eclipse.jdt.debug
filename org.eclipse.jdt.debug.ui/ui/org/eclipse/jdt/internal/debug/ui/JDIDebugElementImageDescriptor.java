package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.ISharedImages;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;

/**
 * A JDIDebugElementImageDescriptor consists of a main icon and several adornments. The adornments
 * are computed according to Java element's modifiers (e.g. visibility, static, final, ...). 
 */
public class JDIDebugElementImageDescriptor extends CompositeImageDescriptor {
	
	/** Flag to render the is out of synch adornment */
	public final static int IS_OUT_OF_SYNCH= 		0x001;
	/** Flag to render the may be out of synch adornment */
	public final static int MAY_BE_OUT_OF_SYNCH= 		0x002;
	
	private ImageDescriptor fBaseImage;
	private int fFlags;
	private Point fSize;
	
	/**
	 * Create a new JavaElementImageDescriptor.
	 * 
	 * @param baseImage an image descriptor used as the base image
	 * @param flags flags indicating which adornments are to be rendered
	 * 
	 */
	public JDIDebugElementImageDescriptor(ImageDescriptor baseImage, int flags) {
		fBaseImage= baseImage;
		Assert.isNotNull(fBaseImage);
		fFlags= flags;
		Assert.isTrue(fFlags >= 0);
		ImageData data= baseImage.getImageData();
		fSize= new Point(data.width, data.height);
		Assert.isNotNull(fSize);
	}
	
	/**
	 * @see CompositeImageDescriptor#getSize()
	 */
	protected Point getSize() {
		return fSize;
	}
	
	/**
	 * @see Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (!JDIDebugElementImageDescriptor.class.equals(object.getClass()))
			return false;
			
		JDIDebugElementImageDescriptor other= (JDIDebugElementImageDescriptor)object;
		return (fBaseImage.equals(other.fBaseImage) && fFlags == other.fFlags && fSize.equals(other.fSize));
	}
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return fBaseImage.hashCode() | fFlags | fSize.hashCode();
	}
	
	/**
	 * @see CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	protected void drawCompositeImage(int width, int height) {
		ImageData bg;
		if ((bg= fBaseImage.getImageData()) == null)
			bg= DEFAULT_IMAGE_DATA;
			
		drawImage(bg, 0, 0);
		drawTopRight();
	}	
	
	/**
	 * Add any overlays to the top right of the image
	 */
	private void drawTopRight() {
		int x= getSize().x;
		int y= 0;
		ImageData data= null;
		if ((fFlags & IS_OUT_OF_SYNCH) != 0) {
			data= JavaDebugImages.DESC_OVR_IS_OUT_OF_SYNCH.getImageData();
			x -= data.width;
			drawImage(data, x, y);
		} else if ((fFlags & MAY_BE_OUT_OF_SYNCH) != 0) {
			data= JavaDebugImages.DESC_OVR_MAY_BE_OUT_OF_SYNCH.getImageData();
			x -= data.width;
			drawImage(data, x, y);
		}
	}
}