package org.eclipse.jdt.launching.sourcelookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;


/**
 * Locates source for a Java debug session by searching
 * a configurable set of source locations.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 *
 * @see org.eclipse.debug.core.model.ISourceLocator
 */
public class JavaSourceLocator implements ISourceLocator {

	/**
	 * A collection of the source locations to search
	 */
	private IJavaSourceLocation[] fLocations;

	/**
	 * Constructs a new JavaSourceLocator that searches the
	 * specified set of source locations for source elements.
	 * 
	 * @param locations the source locations to search for
	 *  source, in the order they should be searched
	 */
	public JavaSourceLocator(IJavaSourceLocation[] locations) {
		setSourceLocations(locations);
	}
	
	/**
	 * Sets the locations that will be searched, in the order
	 * to be searched.
	 * 
	 * @param locations the locations that will be searched, in the order
	 *  to be searched
	 */
	public void setSourceLocations(IJavaSourceLocation[] locations) {
		fLocations = locations;
	}
	
	/**
	 * Returns the locations that this source locator is currently
	 * searching, in the order that they are searched.
	 * 
	 * @return the locations that this source locator is currently
	 * searching, in the order that they are searched
	 */
	public IJavaSourceLocation[] getSourceLocations() {
		return fLocations;
	}
			
	/**
	 *@see ISourceLocator#getSourceElement
	 */
	public Object getSourceElement(IStackFrame stackFrame) {
		if (stackFrame instanceof IJavaStackFrame) {
			IJavaStackFrame frame = (IJavaStackFrame)stackFrame;
			String name = null;
			try {
				String sourceName = frame.getSourceName();
				if (sourceName == null) {
					// no debug attributes, guess at source name
					name = frame.getDeclaringTypeName();
				} else {
					// build source name from debug attributes using
					// the source file name and the package of the declaring
					// type
					String declName= frame.getDeclaringTypeName();
					int index = declName.lastIndexOf('.');
					if (index >= 0) {
						name = declName.substring(0, index + 1);
					} else {
						name = ""; //$NON-NLS-1$
					}
					index = sourceName.lastIndexOf('.');
					if (index >= 0) {
						name += sourceName.substring(0, index) ;
					}					
				}
				IJavaSourceLocation[] locations = getSourceLocations();
				for (int i = 0; i < locations.length; i++) {
					Object sourceElement = locations[i].findSourceElement(name);
					if (sourceElement != null) {
						return sourceElement;
					}
				}
			} catch (CoreException e) {
				LaunchingPlugin.log(e);
			}
		}
		return null;
	}
}