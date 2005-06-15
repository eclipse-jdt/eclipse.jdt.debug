/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


/**
 * Abtract change to update a breakpoint when a IType is moved or renamed.
 */
public abstract class JavaBreakpointTypeChange extends Change {
	
	public static final int TYPE_RENAME= 1;
	public static final int TYPE_MOVE= 2;
	public static final int PROJECT_RENAME= 3;
	public static final int PACKAGE_RENAME= 4;
	public static final int PACKAGE_MOVE= 5;
	
	private IJavaBreakpoint fBreakpoint;
	private Object fChangedElement;
	private Object fArgument;
	private int fChangeType;
	private IType fDeclaringType;
	private boolean fIsEnable;
	private Map fAttributes;
	private int fHitCount;
	
	/**
	 * Create changes for each breakpoint which needs to be updated for this IType rename.
	 */
	public static Change createChangesForTypeRename(IType type, String newName) throws CoreException {
		return createChangesForTypeChange(type, newName, TYPE_RENAME);
	}
	
	/**
	 * Create changes for each breakpoint which needs to be updated for this IType move.
	 */
	public static Change createChangesForTypeMove(IType type, Object destination) throws CoreException {
		return createChangesForTypeChange(type, destination, TYPE_MOVE);
	}

	/**
	 * Create a change for each breakpoint which needs to be updated for this IJavaProject rename.
	 */
	public static Change createChangesForProjectRename(IJavaProject project, String newName) throws CoreException {
		List changes= new ArrayList();
		IBreakpoint[] breakpoints= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaBreakpoint) {
				IJavaBreakpoint javaBreakpoint= (IJavaBreakpoint) breakpoint;
				IType breakpointType= BreakpointUtils.getType(javaBreakpoint);
				if (breakpointType != null && project.equals(breakpointType.getJavaProject())) {
					changes.add(createChange(javaBreakpoint, null, newName, PROJECT_RENAME));
				}
			}
		}
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.JavaBreakpointTypeChange_0); //$NON-NLS-1$
	}
	
	/**
	 * Create a change for each breakpoint which needs to be updated for this IPackageFragment rename.
	 */
	public static Change createChangesForPackageRename(IPackageFragment packageFragment, String newName) throws CoreException {
		return createChangesForPackageChange(packageFragment, newName, PACKAGE_RENAME);
	}

	/**
	 * Create a change for each breakponit which needs to be updated for this IPackageFragment move.
	 */
	public static Change createChangesForPackageMove(IPackageFragment packageFragment, IPackageFragmentRoot destination) throws CoreException {
		return createChangesForPackageChange(packageFragment, destination, PACKAGE_MOVE);
	}
	
	/**
	 * Create changes for each breakpoint which need to be updated for this IType change.
	 */
	private static Change createChangesForTypeChange(IType changedType, Object argument, int changeType) throws CoreException {
		List changes= new ArrayList();

		IBreakpoint[] breakpoints= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
		String typeName= changedType.getFullyQualifiedName();
		for (int i= 0; i < breakpoints.length; i++) {
			// for each breakpoint
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaBreakpoint) {
				IJavaBreakpoint javaBreakpoint= (IJavaBreakpoint) breakpoint;
				IType breakpointType= BreakpointUtils.getType(javaBreakpoint);
				// check the name of the type where the breakpoint is installed
				if (breakpointType != null && javaBreakpoint.getTypeName().startsWith(typeName)) {
					// if it matcheds, check the type
					if (changedType.equals(breakpointType)) {
						changes.add(createChange(javaBreakpoint, changedType, argument, changeType));
					} else {
						// if it's not the type, check the inner types
						Change change= createChangesForOuterTypeChange(javaBreakpoint, changedType, changedType, argument, changeType);
						if (change != null) {
							changes.add(change);
						}
					}
				}
			}
		}
				
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.JavaBreakpointTypeChange_0); //$NON-NLS-1$
	}
	
	private static Change createChangesForOuterTypeChange(IJavaBreakpoint javaBreakpoint, IType type, IType changedType, Object argument, int changeType) throws CoreException {
		IType[] innerTypes= type.getTypes();
		String breakpointTypeName= javaBreakpoint.getTypeName();
		IType breakpointType= BreakpointUtils.getType(javaBreakpoint);
		for (int i= 0; i < innerTypes.length; i++) {
			IType innerType= innerTypes[i];
			// check the name of the type where the breakpoint is installed
			if (breakpointTypeName.startsWith(innerType.getFullyQualifiedName())) {
				// if it matcheds, check the type
				if (innerType.equals(breakpointType)) {
					return createChange(javaBreakpoint, changedType, argument, changeType);
				} 
				// if it's not the type, check the inner types
				return createChangesForOuterTypeChange(javaBreakpoint, innerType, changedType, argument, changeType);
			}
			
		}
		return null;
	}

	/**
	 * Create a change for each breakpoint which needs to be updated for this IPackageFragment change.
	 */
	private static Change createChangesForPackageChange(IPackageFragment packageFragment, Object argument, int changeType) throws CoreException {
		List changes= new ArrayList();
		IBreakpoint[] breakpoints= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaBreakpoint) {
				IJavaBreakpoint javaBreakpoint= (IJavaBreakpoint) breakpoint;
				IType breakpointType= BreakpointUtils.getType(javaBreakpoint);
				if (breakpointType != null && packageFragment.equals(breakpointType.getPackageFragment())) {
					changes.add(createChange(javaBreakpoint, packageFragment, argument, changeType));
				}
			}
		}
		return JDTDebugRefactoringUtil.createChangeFromList(changes, RefactoringMessages.JavaBreakpointTypeChange_0); //$NON-NLS-1$
	}
	
	/**
	 * Create a change according to type of the breakpoint.
	 */
	private static Change createChange(IJavaBreakpoint javaBreakpoint, Object changedElement, Object argument, int changeType) throws CoreException {
		if (javaBreakpoint instanceof IJavaClassPrepareBreakpoint) {
			return new JavaClassPrepareBreakpointTypeChange((IJavaClassPrepareBreakpoint) javaBreakpoint, changedElement, argument, changeType);
		} else if (javaBreakpoint instanceof IJavaExceptionBreakpoint) {
			return new JavaExceptionBreakpointTypeChange((IJavaExceptionBreakpoint) javaBreakpoint, changedElement, argument, changeType);
		} else if (javaBreakpoint instanceof IJavaMethodBreakpoint) {
			return new JavaMethodBreakpointTypeChange((IJavaMethodBreakpoint) javaBreakpoint, changedElement, argument, changeType);
		} else if (javaBreakpoint instanceof IJavaWatchpoint) {
			return new JavaWatchpointTypeChange((IJavaWatchpoint) javaBreakpoint, changedElement, argument, changeType);
		} else if (javaBreakpoint instanceof IJavaLineBreakpoint) {
			return new JavaLineBreakpointTypeChange((IJavaLineBreakpoint) javaBreakpoint, changedElement, argument, changeType);
		} else {
			return null;
		}
	}

	/**
	 * JavaBreakpointTypeChange constructor.
	 */
	protected JavaBreakpointTypeChange(IJavaBreakpoint breakpoint, Object changedElement, Object argument, int changeType) throws CoreException {
		fBreakpoint= breakpoint;
		fChangedElement= changedElement;
		fArgument= argument;
		fChangeType= changeType;
		fDeclaringType= BreakpointUtils.getType(breakpoint);
		fAttributes= breakpoint.getMarker().getAttributes();
		fIsEnable= breakpoint.isEnabled();
		fHitCount= breakpoint.getHitCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeValidationData(IProgressMonitor pm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		RefactoringStatus status= new RefactoringStatus();
		if (!fBreakpoint.isRegistered()) {
			status.addFatalError(getErrorMessageNoMoreExists());
		}
		return status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		switch (fChangeType) {
			case TYPE_RENAME:
				return performTypeRename();
			case TYPE_MOVE:
				return performTypeMove();
			case PROJECT_RENAME:
				return performProjectRename();
			case PACKAGE_RENAME:
				return performPackageRename();
			case PACKAGE_MOVE:
				return performPackageMove();
		}
		return null;
	}
	
	private Change performTypeRename() throws CoreException {
		// Get the new type and the new 'changed' type then call the code specific to this type
		// of breakpoint.
		IType changedType= getChangedType();
		String oldChangedTypeName= changedType.getFullyQualifiedName('.');
		String newChangedTypeName;
		IType parent= changedType.getDeclaringType();
		if (parent == null) {
			newChangedTypeName= changedType.getPackageFragment().getElementName() + '.' + getNewName();
		} else {
			newChangedTypeName= parent.getFullyQualifiedName('.') + '.' + getNewName();
		}
		
		IType newChangedType;
		IType newType;
		IJavaProject project= fDeclaringType.getJavaProject();
		if (changedType.equals(fDeclaringType)) {
			newType= project.findType(newChangedTypeName);
			newChangedType= newType;
		} else {
			String typeNameSuffix= fDeclaringType.getFullyQualifiedName('.').substring(oldChangedTypeName.length());
			String newTypeName= newChangedTypeName + typeNameSuffix;
			newType= project.findType(newTypeName);
			newChangedType= project.findType(newChangedTypeName);
		}
		
		/*return*/ performChange(newType, newChangedType, changedType.getElementName(), TYPE_RENAME);
		return new NullChange();
	}
	
	private Change performTypeMove() throws CoreException {
		// Get the new type and the new 'changed' type then call the code specific to this type
		// of breakpoint.
		IType changedType= getChangedType();
		Object destination= getDestination();
		String newChangedTypeName;
		IJavaProject project;
		if (destination instanceof IPackageFragment) {
			IPackageFragment packageDestination= (IPackageFragment) destination;
			project= packageDestination.getJavaProject();
			if (packageDestination.isDefaultPackage()) {
				newChangedTypeName= changedType.getElementName();
			} else {
				newChangedTypeName= ((IPackageFragment)destination).getElementName() + '.' + changedType.getElementName();
			}
		} else {
			IType type = (IType)destination;
			newChangedTypeName= (type).getFullyQualifiedName('.') + '.' + changedType.getElementName();
			project= type.getJavaProject();
		}
		
		IType newChangedType;
		IType newType;
		if (changedType == fDeclaringType) {
			newType= project.findType(newChangedTypeName);
			newChangedType= newType;
		} else {
			String oldChangedTypeName= changedType.getFullyQualifiedName('.');
			String typeNameSuffix= fDeclaringType.getFullyQualifiedName('.').substring(oldChangedTypeName.length());
			String newTypeName= newChangedTypeName + typeNameSuffix;
			newType= project.findType(newTypeName);
			newChangedType= project.findType(newChangedTypeName);
		}
		
		Object oldDestination= changedType.getDeclaringType();
		if (oldDestination == null) {
			oldDestination= changedType.getPackageFragment();
		}
		
		/*return*/ performChange(newType, newChangedType, oldDestination, TYPE_MOVE);
		return new NullChange();
	}
	
	private Change performProjectRename() throws CoreException {
		// Get the new type, then call the code specific to this type of breakpoint.
		IJavaProject project= JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(getNewName()));
		IType newType= project.findType(fDeclaringType.getFullyQualifiedName('.'));
		/*return*/ performChange(newType, null, fDeclaringType.getJavaProject().getElementName(), PROJECT_RENAME);
		return new NullChange();
	}
	
	private Change performPackageRename() throws CoreException {
		// Get the new type and the new package fragment, then call the code specific
		// to this type of breakpoint.
		IPackageFragment changedPackage= getChangePackage();
		IJavaProject project= fDeclaringType.getJavaProject();
		String newTypeName= getNewName() + fDeclaringType.getFullyQualifiedName('.').substring(changedPackage.getElementName().length());
		IType newType= project.findType(newTypeName);
		/*return*/ performChange(newType, newType.getPackageFragment(), changedPackage.getElementName(), PACKAGE_RENAME);
		return new NullChange();
	}
	
	private Change performPackageMove() throws CoreException {
		IPackageFragmentRoot destination= getPackageRootDestination();
		IPackageFragment changedPackage= getChangePackage();
		IJavaProject project= destination.getJavaProject();
		IType newType= project.findType(fDeclaringType.getFullyQualifiedName('.'));
		/*return*/ performChange(newType, newType.getPackageFragment(), changedPackage.getParent(), PROJECT_RENAME);
		return new NullChange();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
	 */
	public Object getModifiedElement() {
		return getBreakpoint();
	}

	/**
	 * Return the breakpoint modified in this change.
	 */
	public IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}
	
	/**
	 * Return the new name of the changed type for a IType, IJavaProject
	 * or IPackageFragment rename change.
	 */
	public String getNewName() {
		if (fChangeType == TYPE_RENAME || fChangeType == PROJECT_RENAME || fChangeType == PACKAGE_RENAME) {
			return (String)fArgument;
		}
		return null;
	}
	
	/**
	 * Return the destination for a IType move change.
	 */
	private Object getDestination() {
		if (fChangeType == TYPE_MOVE) {
			return fArgument;
		}
		return null;
	}
	
	/**
	 * Return the destination for a IPackageFragment move change.
	 */
	private IPackageFragmentRoot getPackageRootDestination() {
		if (fChangeType == PACKAGE_MOVE) {
			return (IPackageFragmentRoot)fArgument;
		}
		return null;
	}

	/**
	 * Return the original declaring type of the breakpoint.
	 */
	public IType getDeclaringType() {
		return fDeclaringType;
	}

	/**
	 * Return the type modified.
	 */
	public IType getChangedType() {
		if (fChangeType == TYPE_RENAME || fChangeType == TYPE_MOVE) {
			return (IType) fChangedElement;
		}
		return null;
	}

	/**
	 * Return the package modified.
	 */
	public IPackageFragment getChangePackage() {
		if (fChangeType == PACKAGE_RENAME || fChangeType == PACKAGE_MOVE) {
			return (IPackageFragment) fChangedElement;
		}
		return null;
	}
	
	/**
	 * Return the enable state of the breakpoint.
	 */
	public boolean getEnable() {
		return fIsEnable;
	}
	
	/**
	 * Return the attributes map of the breakpoint.
	 */
	public Map getAttributes() {
		return fAttributes;
	}
	
	/**
	 * Return the hit count of the breakpoint.
	 */
	public int getHitCount() {
		return fHitCount;
	}
	
	/**
	 * Return the message to use if the breakpoint no more exists (used in #isValid()).
	 */
	public abstract String getErrorMessageNoMoreExists();
	
	/**
	 * Perform the real modifications.
	 * @return the undo change.
	 */
	public abstract Change performChange(IType newType, Object undoChangedElement, Object undoArgument, int changeType) throws CoreException;

}
