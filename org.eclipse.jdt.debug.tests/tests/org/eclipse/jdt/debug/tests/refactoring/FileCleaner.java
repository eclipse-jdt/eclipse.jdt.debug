/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class FileCleaner extends AbstractDebugTest{

	public FileCleaner(String name) {
		super(name);
	}

	public void cleanTestFiles() throws Exception
	{
		//ensure proper packages
		//cleanup new Package
		IPackageFragmentRoot root = getPackageFragmentRoot(getJavaProject(), "src");
		IPackageFragment fragment = root.getPackageFragment("renamedPackage");
		if(fragment.exists())
			fragment.delete(true, new NullProgressMonitor());	
		
		fragment = root.getPackageFragment("a.b.c");
		if(!fragment.exists())
			root.createPackageFragment("a.b.c", true, new NullProgressMonitor());
		
		//cleanup Movee
		IFile target = getJavaProject().getProject().getFile("src/a/b/Movee.java");
		if(target.exists())
			target.delete(false, false, null);		
		target = getJavaProject().getProject().getFile("src/a/b/c/Movee.java");//move up a dir
		if(target.exists())
			target.delete(false, false, null);
		//get original source & replace old result
		IFile source = getJavaProject().getProject().getFile("src/a/MoveeSource");//no .java - it's a bin
		source.copy(target.getFullPath(), false, null );
		
		//cleanup moveeRecipient
		target = getJavaProject().getProject().getFile("src/a/b/MoveeRecipient.java");//move up a dir
		if(target.exists())
			target.delete(false, false, null);
		//get original source & replace old result
		source = getJavaProject().getProject().getFile("src/a/MoveeRecipientSource");//no .java - it's a bin
		source.copy(target.getFullPath(), false, null );	
		
		//cleanup renamedType
		target = getJavaProject().getProject().getFile("src/a/b/c/RenamedType.java");//move up a dir
		if(target.exists())
			target.delete(false, false, null);
		//cleanup renamedType
		target = getJavaProject().getProject().getFile("src/a/b/c/RenamedCompilationUnit.java");//move up a dir
		if(target.exists())
			target.delete(false, false, null);		
		
		//cleanup child
		target = getJavaProject().getProject().getFile("src/a/b/MoveeChild.java");//move up a dir
		if(target.exists())
			target.delete(false, false, null);		
		target = getJavaProject().getProject().getFile("src/a/b/c/MoveeChild.java");//move up a dir
		if(target.exists())
			target.delete(false, false, null);
		//get original source & replace old result
		source = getJavaProject().getProject().getFile("src/a/MoveeChildSource");//no .java - it's a bin
		source.copy(target.getFullPath(), false, null );
	}
	
}
