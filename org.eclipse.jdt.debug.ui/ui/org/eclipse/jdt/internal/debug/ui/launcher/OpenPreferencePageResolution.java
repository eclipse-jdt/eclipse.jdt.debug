/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.progress.UIJob;

/**
 * Marker resolution to open a preference page
 * 
 * @since 3.5
 */
public class OpenPreferencePageResolution implements IMarkerResolution2 {

	private String pageid = null;
	private String label = null;
	private String description = null;
	private String[] additional = null;
	
	/**
	 * Constructor
	 * 
	 * @param pageid the id of the page to show
	 * @param additional the page ids of additional pages to show as well
	 * @param label the label to show for the resolution
	 * @param description the description to show for the resolution
	 */
	public OpenPreferencePageResolution(String pageid, String[] additional, String label, String description) {
		this.pageid = pageid;
		this.additional = additional;
		this.label = label;
		this.description = description;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
	 */
	public String getDescription() {
		return this.description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution2#getImage()
	 */
	public Image getImage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	public String getLabel() {
		return this.label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
	 */
	public void run(IMarker marker) {
		UIJob job = new UIJob(""){ //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				SWTFactory.showPreferencePage(
							OpenPreferencePageResolution.this.pageid, 
							OpenPreferencePageResolution.this.additional);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

}
