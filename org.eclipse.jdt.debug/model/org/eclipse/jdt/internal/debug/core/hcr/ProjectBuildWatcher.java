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
	
	public static String BUILDER_ID= "org.eclipse.jdt.debug.hcrbuilder"; //$NON-NLS-1$
	
	long fLastBuildTime= new Date().getTime();
	long fCurrentBuildTime= new Date().getTime();
	
	public ProjectBuildWatcher() {
	}
	
	protected IProject[] build(int kind, Map arguments, IProgressMonitor monitor) {
		fLastBuildTime= fCurrentBuildTime;
		fCurrentBuildTime= new Date().getTime();
		return null;
	}
	
	public long getLastBuildTime() {
		return fLastBuildTime;
	}
	
	protected void startupOnInitialize() {
		JavaHotCodeReplaceManager.getDefault().registerBuilder(this, getProject());
	}
}
