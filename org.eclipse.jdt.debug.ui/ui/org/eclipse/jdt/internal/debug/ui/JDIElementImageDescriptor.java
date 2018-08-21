/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Point;

public class JDIElementImageDescriptor extends CompositeImageDescriptor {

	private ImageDescriptor fBaseImage;
	private int fFlags;
	private Point fSize;
	public JDIElementImageDescriptor(ImageDescriptor baseImage, int flags, Point size) {
		fBaseImage = baseImage;
		Assert.isNotNull(fBaseImage);
		fFlags = flags;
		Assert.isTrue(fFlags >= 0);
		fSize = size;
		Assert.isNotNull(fSize);
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(createCachedImageDataProvider(fBaseImage), 0, 0);
		drawRightBottom();
	}

	private void drawRightBottom() {
		Point size = getSize();
		Point pos = new Point(size.x, size.y);

		if ((fFlags & JDIImageDescriptor.LOGICAL_STRUCTURE) != 0) {
			addRightBottomImage(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_OVR_SHOW_LOGICAL_STRUCTURE), pos);
		}
	}

	private void addRightBottomImage(ImageDescriptor desc, Point pos) {
		CachedImageDataProvider provider = createCachedImageDataProvider(desc);
		int x = pos.x - provider.getWidth();
		int y = pos.y - provider.getHeight();
		if (x >= 0 && y >= 0) {
			drawImage(provider, x, y);
			pos.x = x;
		}
	}

	@Override
	protected Point getSize() {
		if (fSize == null) {
			CachedImageDataProvider provider = createCachedImageDataProvider(fBaseImage);
			fSize = new Point(provider.getWidth(), provider.getHeight());
		}
		return fSize;
	}

	/**
	 * @see Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof JDIElementImageDescriptor)) {
			return false;
		}
		JDIElementImageDescriptor other = (JDIElementImageDescriptor) object;
		return (fBaseImage.equals(other.fBaseImage) && fFlags == other.fFlags);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return fBaseImage.hashCode() | fFlags;
	}

}
