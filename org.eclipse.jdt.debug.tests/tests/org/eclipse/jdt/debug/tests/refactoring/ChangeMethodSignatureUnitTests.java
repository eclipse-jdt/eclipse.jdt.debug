/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.refactoring;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

public class ChangeMethodSignatureUnitTests extends AbstractRefactoringDebugTest {

	public ChangeMethodSignatureUnitTests(String name) {
		super(name);
	}

	public void testPublicTypeMethodChange() throws Exception {
		try {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "Movee.java",
					type = "Movee",
					methodName = "testMethod1",
					methodSig = "()V";
			//create breakpoint to test
			createMethodBreakpoint(pack+"."+type, methodName, methodSig, true, false);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit, type, methodName, methodSig);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",methodBreakpoint.getMarker().exists());
			assertEquals("wrong method Signature", "(QObject;)V", methodBreakpoint.getMethodSignature());
			assertEquals("wrong type name", pack+"."+type, methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","changedMethod",methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testBreakPoint

	public void testInnerTypeMethodChange() throws Exception {
		try {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "Movee.java",
					type = "Movee$InnerType",
					methodName = "innerTypeMethod",
					methodSig = "()V";
			//create breakpoint to test
			createMethodBreakpoint(pack+"."+type, methodName, methodSig, true, false);
			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit, type, methodName, methodSig);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",methodBreakpoint.getMarker().exists());
			assertEquals("wrong method Signature", "(QObject;)V", methodBreakpoint.getMethodSignature());
			assertEquals("wrong type name", pack+"."+type, methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","changedMethod",methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testBreakPoint

	public void testNonPublicTypeMethodChange() throws Exception {
		try {
			String 	src = "src",
					pack = "a.b.c",
					cunit = "Movee.java",
					type = "NonPublicType",
					methodName = "nonPublicMethod",
					methodSig = "()V";
			//create breakpoint to test
			createMethodBreakpoint(pack,cunit,type,methodName,methodSig,true, false);

			//refactor
			Refactoring ref = setupRefactor(src, pack, cunit, type, methodName, methodSig);
			performRefactor(ref);
			//test breakpoints
			IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints();
			assertEquals("wrong number of breakpoints", 1, breakpoints.length);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoints[0];
			assertTrue("Breakpoint Marker has ceased existing",methodBreakpoint.getMarker().exists());
			assertEquals("wrong method Signature", "(QObject;)V", methodBreakpoint.getMethodSignature());
			assertEquals("wrong type name", pack+"."+type, methodBreakpoint.getTypeName());
			assertEquals("breakpoint attached to wrong method","changedMethod",methodBreakpoint.getMethodName());
		} catch (Exception e) {
			throw e;
		} finally {
			removeAllBreakpoints();
		}
	}//end testBreakPoint


//////////////////////////////////////////////////////////////////////////////////////
	private Refactoring setupRefactor(String root, String targetPackageName, String cuName, String typeName, String methodName, String methodSig) throws Exception {

		IJavaProject javaProject = get14Project();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, targetPackageName, cuName);
		IType type = getLowestType(cunit, typeName);

		IMethod method = type.getMethod(methodName, Signature.getParameterTypes(methodSig));

        ChangeSignatureProcessor processor = new ChangeSignatureProcessor(method);
        ProcessorBasedRefactoring ref= new ProcessorBasedRefactoring(processor);

		//configure the processor a little more here!
        processor.setNewMethodName("changedMethod");
        processor.setNewReturnTypeName("Object");
        processor.setVisibility(Modifier.PRIVATE);

		RefactoringStatus preconditionResult= ref.checkInitialConditions(new NullProgressMonitor());
		if(!preconditionResult.isOK())
		{
			System.out.println(preconditionResult.getMessageMatchingSeverity(preconditionResult.getSeverity()));
			return null;
		}
		return ref;
	}

	/**
	 *
	 * @param cu the CompilationUnit containing the toplevel Type
	 * @param input - the type, possibly including inner type,
	 * separated by $.
	 * eg: EnclosingType$InnerType
	 * @return the Lowest level inner type specified in input
	 */
	protected IType getLowestType(ICompilationUnit cu, String input)
	{
		for(int i=0;i<input.length();i++)
		{
			if(input.charAt(i)=='$')
			{//Enclosing$Inner$MoreInner
				String inner = input.substring(i+1);
				IType enclosingType = cu.getType(input.substring(0, i));
				return getInnermostType(enclosingType,inner);
			}
		}
		//has no inner type
		return cu.getType(input);

	}

	/**
	 * Helper method for getLowestType (ICompilationUnit cu, String input)
	 * @param enclosing name of enclosing Type
	 * @param name the typename, possibly including inner type,
	 * separated by $.
	 * eg: EnclosingType$InnerType
	 * @return
	 */
	private IType getInnermostType(IType enclosing, String name) {
		for(int i=0;i<name.length();i++)
		{
			if(name.charAt(i)=='$')
			{//Enclosing$Inner$MoreInner
				enclosing.getType(name.substring(0, i));
				return getInnermostType(enclosing,name.substring(i+1));
			}
		}
		//has no inner type
		return enclosing.getType(name);

	}
}
