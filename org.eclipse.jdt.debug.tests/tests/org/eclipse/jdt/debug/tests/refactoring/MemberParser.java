/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;

/**
 * Contains methods to find an IMember within a given path subdivided by the '$' character.
 * Syntax:
 * Type$InnerType$MethodNameAndSignature$AnonymousTypeDeclarationNumber$FieldName
 * eg:<code>
 * public class Foo{
 * 		class Inner
 * 		{
 * 			public void aMethod()
 * 			{
 * 				Object anon = new Object(){
 * 					int anIntField;
 * 					String anonTypeMethod() {return "an Example";}
 * 				}
 * 			}
 * 		}
 * }</code>
 * Syntax to get anIntField would be: Foo$Inner$aMethod()V$1$anIntField
 * Syntax to get the anonymous toString would be: Foo$Inner$aMethod()V$1$anonTypeMethod()QString
 * In the case of local types, the listed syntax should be Count and then Name, like: CountName
 * eg:<code>1MyType</code>
 */
public class MemberParser{

	private static ArrayList<String> createTypeList(String typeQualifiedName) {
		String newname = typeQualifiedName;
		newname = newname.replace('$','.');//ensure proper format was used.
		String parsed[] = newname.split("\\."); //$NON-NLS-1$
		//make list of types to find
		ArrayList<String> typeList = new ArrayList<>();
		for (int splitNum = 0; splitNum < parsed.length; splitNum++) {
			typeList.add(parsed[splitNum]);
		}
		return typeList;
	}
	/**
	 * @param fragments the scope of which you wish to return compilation units
	 * @return a handle to all compilation units contained by the given fragments
	 */
	private static ICompilationUnit[] getAllCompilationUnits(IPackageFragment[] fragments) throws JavaModelException {
		if(fragments == null)
			return null;
		final Set<ICompilationUnit> results = new HashSet<>();
		for (int fragmentNum = 0; fragmentNum < fragments.length; fragmentNum++) {
			if(fragments[fragmentNum].containsJavaResources()){
				ICompilationUnit cunits[] = fragments[fragmentNum].getCompilationUnits();
				for (int cunitNum = 0; cunitNum < cunits.length; cunitNum++) {
					results.add(cunits[cunitNum]);
				}
			}
		}
		if(results.isEmpty())
			return null;
		return results.toArray(new ICompilationUnit[results.size()]);
	}

	/**
	 * @param projects the scope of which you wish to return compilation units
	 * @return a handle to all compilation units contained by the given projects
	 */
	private static ICompilationUnit[] getAllCompilationUnits(IProject[] projects)  throws JavaModelException{
		return getAllCompilationUnits(getAllPackageFragments(projects));
	}

	private static ICompilationUnit[] getAllCompilationUnits(String packageName, IProject[] projects)throws JavaModelException {
		return getAllCompilationUnits(getAllPackageFragments(packageName, projects));
	}

	/**
	 * @return an array of all declared methods for the given types
	 */
	private static IMethod[] getAllMethods(IType[] types) throws JavaModelException{
		if(types==null)
			return null;

		final Set<IMethod> results = new HashSet<>();
		for (int typeNum = 0; typeNum < types.length; typeNum++) {
			IMethod[] methods = types[typeNum].getMethods();
			for (int methodNum = 0; methodNum < methods.length; methodNum++) {
				results.add(methods[methodNum]);
			}
		}
		if(results.isEmpty())
			return null;
		return results.toArray(new SourceMethod[results.size()]);
	}

	/**
	 * @param projects the scope of the return
	 * @return all package fragments in the scope
	 */
	private static IPackageFragment[] getAllPackageFragments(IProject[] projects) throws JavaModelException {
		final Set<IPackageFragment> results = new HashSet<>();
		for (int projectNum = 0; projectNum < projects.length; projectNum++) {
			IJavaProject javaProj = JavaCore.create(projects[projectNum]);
			if(javaProj!= null && javaProj.exists() && javaProj.hasChildren()){
				IPackageFragment fragments[] = javaProj.getPackageFragments();
				for (int fragmentNum = 0; fragmentNum < fragments.length; fragmentNum++) {
					results.add(fragments[fragmentNum]);
				}
			}
		}
		if(results.isEmpty())
			return null;
		return results.toArray(new IPackageFragment[results.size()]);
	}
	/**
	 * @return all projects in the workspace
	 */
	private static IProject[] getAllProjects(){
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}

	/**
	 * @param cunits the scope of the search
	 * @return all types within the scope
	 */
	private static IType[] getAllTypes(ICompilationUnit[] cunits) throws JavaModelException {
		if(cunits == null)
			return null;

		final Set<IType> results = new HashSet<>();
		for (int cunitNum = 0; cunitNum < cunits.length; cunitNum++) {
			IType types[] = cunits[cunitNum].getTypes(); //get all topLevel types
			for (int typeNum = 0; typeNum < types.length; typeNum++) {
				results.add(types[typeNum]);
			}
		}
		if(results.isEmpty())
			return null;
		return results.toArray(new IType[results.size()]);
	}

	/**
	 * @param methods the scope of the search
	 * @return an array of all types declared within the given methods.
	 */
	private static IType[] getAllTypes(IMethod[] methods) throws JavaModelException {
		if(methods==null)
			return null;
		final Set<IJavaElement> results = new HashSet<>();
		for (int methodNum = 0; methodNum < methods.length; methodNum++) {
			IJavaElement[] children = methods[methodNum].getChildren();
			for (int childNum = 0; childNum < children.length; childNum++) {
				if(children[childNum] instanceof IType)
					results.add(children[childNum]);
			}
		}
		if(results.isEmpty())
			return null;
		return results.toArray(new SourceType[results.size()]);
	}

	/**Will search within the given type and all of it's children - including methods
	 * and anonymous types for other types.
	 * @param types the scope of the search
	 * @return all types within the given scope
	 */
	public static IType[] getAllTypes(IType[] types) throws JavaModelException{
		if(types == null)
			return null;
		IType[] newtypes = types;
		final Set<IType> results = new HashSet<>();
		//get all the obvious type declarations
		for (int mainTypeNum = 0; mainTypeNum < newtypes.length; mainTypeNum++) {
			IType declaredTypes[] = newtypes[mainTypeNum].getTypes();
			for (int declaredTypeNum = 0; declaredTypeNum < declaredTypes.length; declaredTypeNum++) {
				results.add(declaredTypes[declaredTypeNum]);
			}
			//get all the type's method's type declarations
			newtypes = getAllTypes(getAllMethods(newtypes));
			for (int methodTypes = 0; methodTypes < newtypes.length; methodTypes++) {
				results.add(newtypes[methodTypes]);
			}
		}
		if(results.isEmpty())
			return null;
		//else
		return results.toArray(new SourceType[results.size()]);//possibly change to new IType
	}


	/**
	 * Returns the Java project with the given name.
	 *
	 * @param name project name
	 * @return the Java project with the given name
	 */
	static protected IJavaProject getJavaProject() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject();
		return JavaCore.create(project);
	}

	/**
	 * @param packageName name of the package
	 * @param projects where to search
	 * @return the 1st instance of the given packageName
	 */
	private static IPackageFragment[] getAllPackageFragments(String packageName, IProject[] projects) throws JavaModelException{
		final Set<IPackageFragment> results = new HashSet<>();
		for (int projectNum = 0; projectNum < projects.length; projectNum++) {
			IJavaProject javaProj = JavaCore.create(projects[projectNum]);
			if(javaProj!= null && javaProj.exists() && javaProj.hasChildren()){
				IPackageFragment fragments[] = javaProj.getPackageFragments();
				for (int fragmentNum = 0; fragmentNum < fragments.length; fragmentNum++) {
					if(fragments[fragmentNum].getElementName().equalsIgnoreCase(packageName))
						results.add(fragments[fragmentNum]);
				}
			}
		}
		if(results.isEmpty())
			return null;
		//else
		return results.toArray(new IPackageFragment[results.size()]);
	}

	private static IProject getProject(String projectName){
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	private static IType getType(ArrayList<String> typeList, ICompilationUnit[] cunits) throws JavaModelException{
		IType[] types = getAllTypes(cunits);
		//if 1st letter is a number, it's anonymous
		boolean targetIsAnonymous = Character.isDigit(typeList.get(0).toString().charAt(0));
		boolean targetFound=false;
		char separator = '.';
		while (true) {//search all types for desired target - will return internally.
			//check all the types we have
			int typeNum=0;
			while(typeNum < types.length) {//search current list of types
				if(targetIsAnonymous){//must ensure format is same for both.
					String nameOfCurrentType = types[typeNum].getTypeQualifiedName(separator);
					nameOfCurrentType = nameOfCurrentType.substring(nameOfCurrentType.lastIndexOf(separator)+1);
					targetFound = nameOfCurrentType.equalsIgnoreCase(typeList.get(0).toString());
				}else{
					targetFound = types[typeNum].getElementName().equalsIgnoreCase(typeList.get(0).toString());
				}
				if(targetFound){//yay!
					typeList.remove(0);
					if(typeList.isEmpty()){
						return types[typeNum];//we're at our destination
					}
					//else, get all this type's subtypes
					types = getAllTypes(new IType[]{types[typeNum]});//get next level
//					check format of this new type
					targetIsAnonymous = Character.isDigit(typeList.get(0).toString().charAt(0));
					typeNum = 0;//start again.
				}
				else
					typeNum++;//check the next type
			}

			//else, it is not in the top-level types - check in methods
			types = getAllTypes(getAllMethods(types));
			if(types==null)
				return null;//couldn't find it.
		}//end while
	}

	/**
	 * Will search the workspace and return the requested type. The more information given,
	 * the faster the search
	 * @param typeName the name of the type, with or without qualifiers - it cannot be null
	 * 		e.g. "aType.innerType.1.typeInAnonymousType" or even just "typeInAnonymousType"
	 * 		or "innerType.1.typeInAnonymousType".
	 * @param packageName the elemental name of the package containing the given type - may be null
	 * @param projectName the elemental name of the project containing the given type - may be null
	 * @return the IType handle to the requested type
	 */
	public static IType getType(String typeName, String packageName, String projectName) throws JavaModelException{
		if(typeName == null)
			return null;
		//make list of types to find, in order
		ArrayList<String> typeList = createTypeList(typeName);
		//get the proper project(s)
		IProject[] projects=null;
		if(projectName!=null && projectName.length()>0){
			projects = new IProject[] {getProject(projectName)};
		}
		else{
			projects = getAllProjects();
		}

		//get the Comp.units for those projects
		ICompilationUnit cunits[] = null;
		if(packageName!=null && packageName.length()>0){
			cunits = getAllCompilationUnits(packageName, projects);
		}
		else{
			cunits = getAllCompilationUnits(projects);
		}

		return getType(typeList, cunits);
	}


	/**
	 * @param cu the CompilationUnit containing the toplevel Type
	 * @param target - the IMember target, listed in full Syntax, as noted in MemberParser
	 * eg: EnclosingType$InnerType
	 * @return the Lowest level inner type specified in input
	 */
	public IMember getDeepest(ICompilationUnit cu, String target)
	{
		for(int i=0;i<target.length();i++)
		{
			if(target.charAt(i)=='$')
			{//EnclosingType$InnerType$MoreInner
				String tail = target.substring(i+1);
				IType enclosure = cu.getType(target.substring(0, i));
				if(enclosure.exists())
					return getDeepest(enclosure,tail);
			}
		}
		//has no inner type
		return cu.getType(target);

	}

	/**
	 * Helper method for getLowestType (ICompilationUnit cu, String input)
	 * @param top name of enclosing Type
	 * @param tail the typename, possibly including inner type,
	 * separated by $.
	 * eg: EnclosingType$InnerType
	 * @return the designated type, or null if type not found.
	 */
	protected IMember getDeepest(IMember top, String tail) {
		String newtail = tail;
		if(newtail==null || newtail.length()==0 )
			return top;

		if(!top.exists())
			return null;

		//check if there are more nested elements
		String head=null;
		for(int i=0;i<newtail.length();i++)
		{
			if(newtail.charAt(i)=='$')//nested Item?
			{//Enclosing$Inner$MoreInner
				head = newtail.substring(0,i);
				newtail = newtail.substring(i+1);
				break;//found next item
			}
		}
		if(head==null)//we are at last item to parse
		{//swap Members
			head = newtail;
			newtail = null;
		}

		if(top instanceof IType)
			return getNextFromType(top, head, newtail);
		else
			if(top instanceof IMethod)
				return getNextFromMethod(top, head, newtail);
			else
				if(top instanceof IField)
					return getNextFromField(top, head, newtail);
		//else there is a problem!
		return getDeepest(top,newtail);
	}

	/**
	 * @param head the string to parse for a name
	 * @return the name in the type, given in the format "Occurance#Type"
	 * e.g. head = "1Type";
	 */
	protected String getLocalTypeName(String head) {
		for(int i=0;i<head.length();i++)
		{
			if(!Character.isDigit(head.charAt(i)))
			{
				return head.substring(i);
			}

		}
		return IInternalDebugCoreConstants.EMPTY_STRING;//entire thing is a number
	}

	/**
	 * @param head the string to parse for an occurrence
	 * @return the name in the type, given in the format "Occurance#Type"
	 * e.g. head = "1Type";
	 */
	protected int getLocalTypeOccurrence(String head) {
		for(int i=0;i<head.length();i++)
		{
			if(!Character.isDigit(head.charAt(i)))
				return Integer.parseInt(head.substring(0, i));
		}
		return Integer.parseInt(head);//entire thing is a number
	}

	/**
	 * @param head name of method w/ signature at the end
	 * @return simply the name of the given method, using format:
	 * methodNameSignature.
	 * e.g.  head = "someMethod()V"
	 */
	protected String getName(String head) {
		for(int i=0;i<head.length();i++)
		{
			if(head.charAt(i)=='(')//nested Item?
				return head.substring(0,i);
		}
		return null;
	}

	/**
	 * @param top the field in which to search
	 * @param head the next member to find
	 * @param tail the remaining members to find
	 * @return the next member down contained by the given Field
	 */
	protected IMember getNextFromField(IMember top, String head, String tail) {
		IField current = (IField)top;

		IType type = current.getType(getLocalTypeName(head),getLocalTypeOccurrence(head));
		if(type.exists())
			return getDeepest(type,tail);
		//else
		return null;//something failed.
	}

	/**
	 * @param top the member in which to search
	 * @param head the next member to find
	 * @param tail the remaining members to find
	 * @return the next member down contained by the given Method
	 */
	protected IMember getNextFromMethod(IMember top, String head, String tail) {
		//must be a local or anonymous type
		IMethod current = (IMethod)top;

		//is next part a Type?
		IType type = current.getType(getLocalTypeName(head), getLocalTypeOccurrence(head));
		if(type.exists())
			return getDeepest(type,tail);
		//else
		return null;
	}

	/**
	 * @param top the member in which to search
	 * @param head the next member to find
	 * @param tail the remaining members to find
	 * @return the next member down contained by the given Type
	 */
	protected IMember getNextFromType(IMember top, String head, String tail) {
		IType current = (IType)top;

		//is next part a Type?
		IMember next = current.getType(head);
		if(next.exists())
			return getDeepest(next,tail);
		//else, is next part a Field?
		next = current.getField(head);
		if(next.exists())
			return getDeepest(next,tail);
		//else, is next part a Method?
		next = current.getMethod(getName(head),getSignature(head));
		if(next.exists())
			return getDeepest(next,tail);
		//else
		return null;//something failed.
	}

	/**
	 * @param head name of method w/ signature at the end
	 * @return simply the ParameterTypeSignature, using format:
	 * methodNameSignature.
	 * e.g.  head = "someMethod()V"
	 */
	protected String[] getSignature(String head) {
		for(int i=0;i<head.length();i++)
		{
			if(head.charAt(i)=='(')//nested Item?
				return Signature.getParameterTypes(head.substring(i));
		}
		return null;
	}
}
