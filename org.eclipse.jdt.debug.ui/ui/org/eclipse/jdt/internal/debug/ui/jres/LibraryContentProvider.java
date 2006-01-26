/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

public class LibraryContentProvider implements ITreeContentProvider {
	
	private Viewer fViewer;
	
	public class SubElement {
		
		public static final int JAVADOC_URL= 1;
		public static final int SOURCE_PATH= 2;
		
		private LibraryLocation fParent;
		private int fType;

		public SubElement(LibraryLocation parent, int type) {
			fParent= parent;
			fType= type;
		}
		
		public LibraryLocation getParent() {
			return fParent;
		}
		
		public int getType() {
			return fType;
		}
	}

	private HashMap fChildren= new HashMap();

	private LibraryLocation[] fLibraries= new LibraryLocation[0];

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fViewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return fLibraries;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof LibraryLocation) {
			LibraryLocation libraryLocation= (LibraryLocation) parentElement;
			Object[] children= (Object[])fChildren.get(libraryLocation);
			if (children == null) {
				children= new Object[] {new SubElement(libraryLocation, SubElement.SOURCE_PATH), new SubElement(libraryLocation, SubElement.JAVADOC_URL)};
				fChildren.put(libraryLocation, children);
			}
			return children;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof SubElement) {
			return ((SubElement)element).getParent();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return element instanceof LibraryLocation;
	}

	public void setLibraries(LibraryLocation[] libs) {
		fLibraries= libs;
		fViewer.refresh();
	}

	public LibraryLocation[] getLibraries() {
		return fLibraries;
	}

	/**
	 * Returns the list of libraries in the given selection. SubElements
	 * are replaced by their parent libraries.
	 */
	private Set getSelectedLibraries(IStructuredSelection selection) {
		Set libraries= new HashSet();
		for (Iterator iter= selection.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof LibraryLocation) {
				libraries.add(element);
			} else if (element instanceof SubElement) {
				libraries.add(((SubElement)element).getParent());
			}
		}
		return libraries;
	}

	/**
	 * Move the libraries of the given selection up.
	 */
	public void up(IStructuredSelection selection) {
		Set libraries= getSelectedLibraries(selection);
		for (int i= 0; i < fLibraries.length - 1; i++) {
			if (libraries.contains(fLibraries[i + 1])) {
				LibraryLocation temp= fLibraries[i];
				fLibraries[i]= fLibraries[i + 1];
				fLibraries[i + 1]= temp;
			}
		}
		fViewer.refresh();
		fViewer.setSelection(selection);
	}

	/**
	 * Move the libraries of the given selection down.
	 */
	public void down(IStructuredSelection selection) {
		Set libraries= getSelectedLibraries(selection);
		for (int i= fLibraries.length - 1; i > 0; i--) {
			if (libraries.contains(fLibraries[i - 1])) {
				LibraryLocation temp= fLibraries[i];
				fLibraries[i]= fLibraries[i - 1];
				fLibraries[i - 1]= temp;
			}
		}
		fViewer.refresh();
		fViewer.setSelection(selection);
	}

	/**
	 * Remove the libraries contained in the given selection.
	 */
	public void remove(IStructuredSelection selection) {
		Set libraries= getSelectedLibraries(selection);
		LibraryLocation[] newLibraries= new LibraryLocation[fLibraries.length - libraries.size()];
		int k= 0;
		for (int i= 0; i < fLibraries.length; i++) {
			if (!libraries.contains(fLibraries[i])) {
				newLibraries[k++]= fLibraries[i];
			}
		}
		fLibraries= newLibraries;
		fViewer.refresh();
	}

	/**
	 * Add the given libraries before the selection, or after the existing libraries
	 * if the selection is empty.
	 */
	public void add(LibraryLocation[] libs, IStructuredSelection selection) {
		LibraryLocation[] newLibraries= new LibraryLocation[fLibraries.length + libs.length];
		if (selection.isEmpty()) {
			System.arraycopy(fLibraries, 0, newLibraries, 0, fLibraries.length);
			System.arraycopy(libs, 0, newLibraries, fLibraries.length, libs.length);
		} else {
			Object element= selection.getFirstElement();
			LibraryLocation firstLib;
			if (element instanceof LibraryLocation) {
				firstLib= (LibraryLocation) element;
			} else {
				firstLib= ((SubElement) element).getParent();
			}
			int i= 0;
			while (i < fLibraries.length && fLibraries[i] != firstLib) {
				newLibraries[i]= fLibraries[i++];
			}
			System.arraycopy(libs, 0, newLibraries, i, libs.length);
			System.arraycopy(fLibraries, i, newLibraries, i + libs.length, fLibraries.length - i);
		}
		fLibraries= newLibraries;
		fViewer.refresh();
		fViewer.setSelection(new StructuredSelection(libs), true);
	}

	/**
	 * Set the given URL as the javadoc location for the libraries contained in
	 * the given selection.
	 */
	public void setJavadoc(URL javadocLocation, IStructuredSelection selection) {
		Set libraries= getSelectedLibraries(selection);
		LibraryLocation[] newLibraries= new LibraryLocation[fLibraries.length];
		Object[] newSelection= new Object[libraries.size()];
		int j= 0;
		for (int i= 0; i < fLibraries.length; i++) {
			LibraryLocation library= fLibraries[i];
			if (libraries.contains(library)) {
				LibraryLocation lib= new LibraryLocation(library.getSystemLibraryPath(), library.getSystemLibrarySourcePath(), library.getPackageRootPath(), javadocLocation);
				newSelection[j++]= getChildren(lib)[1];
				newLibraries[i]= lib;
			} else {
				newLibraries[i]= library;
			}
		}
		fLibraries= newLibraries;
		fViewer.refresh();
		fViewer.setSelection(new StructuredSelection(newSelection));
	}

	/**
	 * Set the given paths as the source info for the libraries contained in
	 * the given selection.
	 */
	public void setSourcePath(IPath sourceAttachmentPath, IPath sourceAttachmentRootPath, IStructuredSelection selection) {
		Set libraries= getSelectedLibraries(selection);
		LibraryLocation[] newLibraries= new LibraryLocation[fLibraries.length];
		Object[] newSelection= new Object[libraries.size()];
		int j= 0;
		for (int i= 0; i < fLibraries.length; i++) {
			LibraryLocation library= fLibraries[i];
			if (libraries.contains(library)) {
				if (sourceAttachmentPath == null) {
					sourceAttachmentPath = Path.EMPTY;
				}
				if (sourceAttachmentRootPath == null) {
					sourceAttachmentRootPath = Path.EMPTY;
				}
				LibraryLocation lib= new LibraryLocation(library.getSystemLibraryPath(), sourceAttachmentPath, sourceAttachmentRootPath, library.getJavadocLocation());
				newSelection[j++]= getChildren(lib)[1];
				newLibraries[i]= lib;
			} else {
				newLibraries[i]= library;
			}
		}
		fLibraries= newLibraries;
		fViewer.refresh();
		fViewer.setSelection(new StructuredSelection(newSelection));
	}
	
}