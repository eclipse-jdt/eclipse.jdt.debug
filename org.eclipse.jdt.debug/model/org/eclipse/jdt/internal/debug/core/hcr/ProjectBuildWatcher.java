package org.eclipse.jdt.internal.debug.core.hcr;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import java.util.Date;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;

public class ProjectBuildWatcher extends IncrementalProjectBuilder {
	
	public ProjectBuildWatcher() {
	}
	
	protected IProject[] build(int kind, Map arguments, IProgressMonitor monitor) {
		return null;
	}
	
	protected void startupOnInitialize() {
	}
}
