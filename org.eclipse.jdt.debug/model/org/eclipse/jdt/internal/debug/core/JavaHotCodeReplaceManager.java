package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The hot code replace manager listens for changes to
 * class files and notifies running debug targets of the changes.
 * <p>
 * Currently, replacing .jar files has no effect on running targets.
 */
public class JavaHotCodeReplaceManager implements IResourceChangeListener {

	// Resource String keys
	private static final String PREFIX= "jdi_hcr.";
	private static final String NO_OUTPUT_LOC= PREFIX + "error.no_output_loc";
	private static final String DELTAS_CHANGED= PREFIX + "error.deltas_changed";

	/**
	 * Singleton 
	 */
	private static JavaHotCodeReplaceManager fgInstance= null;

	/**
	 * The class file extension
	 */
	private static final String CLASS_FILE_EXTENSION= "class";

	/**
	 * Visitor for resource deltas.
	 */
	protected ChangedClassFilesVisitor fVisitor = new ChangedClassFilesVisitor();
	
	/**
	 * Creates a new HCR manager
	 */
	public JavaHotCodeReplaceManager() {
		fgInstance= this;
	}

	/**
	 * Returns the singleton HCR manager
	 */
	public static JavaHotCodeReplaceManager getDefault() {
		return fgInstance;
	}

	/**
	 * Registers this HCR manager as a resource change listener. This method
	 * is called by the JDI debug model plugin on startup.
	 */
	public void startup() {
		getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Deregisters this HCR manager as a resource change listener. This method
	 * is called by the JDI debug model plugin on shutdown.
	 */
	public void shutdown() {
		getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * Returns the workspace.
	 */
	protected IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	/**
	 * Returns the launch manager.
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}


	/**
	 * @see IResourceChangeListener
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		List hotSwapTargets= getHotSwapTargets();
		if (hotSwapTargets != null) {
			List resources= getChangedClassFiles(event.getDelta());
			if (resources == null || resources.isEmpty()) {
				return; // no changed class files.
			}
			List typeNames = getQualifiedNames(resources);		
			notify(hotSwapTargets, typeNames);
		}
		
	}

	/**
	 * Returns the currently registered debug targets that support
	 * hot code replace, or <code>null</code> if none.
	 */
	protected List getHotSwapTargets() {
		List hotSwapTargets = null;
		DebugPlugin plugin= DebugPlugin.getDefault();
		IDebugTarget[] allTargets= plugin.getLaunchManager().getDebugTargets();
		for (int i= 0; i < allTargets.length; i++) {
			IDebugTarget target= allTargets[i];
			if (target instanceof JDIDebugTarget) {
				JDIDebugTarget javaTarget= (JDIDebugTarget) target;
				if (javaTarget.supportsHotCodeReplace()) {
					if (hotSwapTargets == null) {
						hotSwapTargets = new ArrayList(2);
					}
					hotSwapTargets.add(target);
				}
			}
		}
		return hotSwapTargets;
	}

	/**
	 * Notifies the targets of the changed types.
	 */
	protected void notify(List targets, List typeNames) {
		String[] qNames = (String[]) typeNames.toArray(new String[typeNames.size()]);
		Iterator itr= targets.iterator();
		while (itr.hasNext()) {
			JDIDebugTarget target= (JDIDebugTarget) itr.next();
			try {
				target.typesHaveChanged(qNames);
				attemptDropToFrame(target, typeNames);
			} catch (DebugException de) {
				//target update failed
				DebugJavaUtils.logError(de);
			}
		}
	}

	/**
	 * Looks for the deepest effected stack frame in the stack
	 * and forces a drop to frame.  Does this for all of the active
	 * stack frames in the target.
	 */
	protected void attemptDropToFrame(IDebugTarget target, List replacedClassNames) throws DebugException {
		IDebugElement[] threads= target.getChildren();
		for (int i = 0; i < threads.length; i++) {
			IThread thread= (IThread) threads[i];
			if (thread.isSuspended()) {
				IDebugElement[] frames= thread.getChildren();
				IJavaStackFrame dropFrame= null;
				for (int j= frames.length - 1; j >= 0; j--) {
					IJavaStackFrame f= (IJavaStackFrame) frames[j];
					if (replacedClassNames.contains(f.getDeclaringTypeName())) {
						dropFrame = f;
						break;
					}
				}
				if (null != dropFrame) {
					if (dropFrame.supportsDropToFrame()) {
						dropFrame.dropToFrame();
					}
				}
			}
		}
	}

	/**
	 * Returns a collection of <code>String</code>s representing
	 * the qualified type names of the given resources. The qualified
	 * names are returned dot separated.
	 * <p>
	 * This method takes into account the output directory of 
	 * Java projects.
	 */
	protected List getQualifiedNames(List resources) {
		List qualifiedNames= new ArrayList(resources.size());
		Iterator itr= resources.iterator();
		IProject project = null;
		IPath outputPath = null;
		IJavaProject javaProject = null;
		while (itr.hasNext()) {
			IResource resource= (IResource) itr.next();
			if (project == null || !resource.getProject().equals(project)) {
				project= resource.getProject();
				javaProject= JavaCore.create(project);
				try {
					outputPath= javaProject.getOutputLocation();
				} catch (JavaModelException e) {
					DebugJavaUtils.logError(e);
					project = null;
					continue;
				}
			}
			IPath resourcePath= resource.getFullPath();
			int count= resourcePath.matchingFirstSegments(outputPath);
			resourcePath= resourcePath.removeFirstSegments(count);
			String pathString= resourcePath.toString();
			pathString= translateResourceName(pathString);
			qualifiedNames.add(pathString);
		}
		return qualifiedNames;
	}

	/**
	 * Returns the changed class files in the delta or <code>null</code> if none.
	 */
	protected List getChangedClassFiles(IResourceDelta delta) {
		if (delta == null) {
			return null;
		}
		fVisitor.reset();
		try {
			delta.accept(fVisitor);
		} catch (CoreException e) {
			DebugJavaUtils.logError(e);
			return null; // quiet failure
		}

		return fVisitor.getChangedClassFiles();
	}

	protected String translateResourceName(String resourceName) {
		// get rid of ".class"
		resourceName= resourceName.substring(0, resourceName.length() - 6);
		// switch to dot separated
		return resourceName.replace(IPath.SEPARATOR, '.');
	}

	/**
	 * A visitor which collects changed class files.
	 */
	class ChangedClassFilesVisitor implements IResourceDeltaVisitor {
		/**
		 * The collection of changed class files.
		 */
		protected List fFiles= null;

		/**
		 * Answers whether children should be visited.
		 * <p>
		 * If the associated resource is a class file which 
		 * has been changed, record it.
		 */
		public boolean visit(IResourceDelta delta) {
			if (0 == (delta.getKind() & IResourceDelta.CHANGED))
				return false;
			IResource resource= delta.getResource();
			if (resource != null) {
				switch (resource.getType()) {
					case IResource.FILE :
						if (0 == (delta.getFlags() & IResourceDelta.CONTENT))
							return false;
						if (CLASS_FILE_EXTENSION.equals(resource.getFullPath().getFileExtension()))
							fFiles.add(resource);
						return false;
					default :
						return true;
				}
			}
			return true;
		}

		/**
		 * Resets the file collection to empty
		 */
		public void reset() {
			fFiles = new ArrayList();
		}
		
		/**
		 * Answers a collection of changed class files or <code>null</code>
		 */
		public List getChangedClassFiles() {
			return fFiles;
		}
	}

}

