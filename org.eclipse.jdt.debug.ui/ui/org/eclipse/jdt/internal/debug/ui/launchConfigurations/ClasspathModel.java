/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.launchConfigurations;

import java.util.Iterator;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

public class ClasspathModel extends AbstractClasspathEntry {
	
	public static final int DEFAULT_BOOTSTRAP= 0;
	public static final int BOOTSTRAP= 1;
	public static final int DEFAULT_USER= 2;
	public static final int USER= 3;
	
	private ClasspathGroup bootstrapEntries;
	private ClasspathGroup userEntries;
	private ClasspathGroup defaultBootstrapEntries;
	private ClasspathGroup defaultUserEntries;
	
	public Object addEntry(Object entry) {
		if (entry instanceof ClasspathGroup) {
			if (!childEntries.contains(entry)) {
				childEntries.add(entry);
				return entry;
			}
			return null;
		} else {
			ClasspathEntry newEntry= createEntry((IRuntimeClasspathEntry)entry, null);
			Iterator entries= childEntries.iterator();
			while (entries.hasNext()) {
				Object element = entries.next();
				if (element instanceof ClasspathGroup) {
					if(((ClasspathGroup)element).contains(newEntry)) {
						return null;
					}
				} else if (element.equals(newEntry)) {
					return null;
				}
			}
			childEntries.add(newEntry);
			return newEntry;
		}
	}
	
	public Object addEntry(int entryType, IRuntimeClasspathEntry entry) {
		IClasspathEntry entryParent= null;
		switch (entryType) {
			case BOOTSTRAP :
				entryParent= getBootstrapEntry();
				break;
			case DEFAULT_BOOTSTRAP :
				if (defaultBootstrapEntries == null) {
					String name= "Default Bootstrap Entries";
					defaultBootstrapEntries= createGlobalEntry(new IRuntimeClasspathEntry[0], (ClasspathGroup)getBootstrapEntry(), name, true, false);
				}
				((ClasspathGroup)getBootstrapEntry()).addEntry(defaultBootstrapEntries);
				entryParent= defaultBootstrapEntries;
				break;
			case USER :
				entryParent= getUserEntry();
				break;
			case DEFAULT_USER :
				if (defaultUserEntries == null) {
					String name= "Default User Entries";
					defaultUserEntries= createGlobalEntry(new IRuntimeClasspathEntry[0], (ClasspathGroup)getUserEntry(), name, true, false);
				}
				((ClasspathGroup)getUserEntry()).addEntry(defaultUserEntries);
				entryParent= defaultUserEntries;
				break;
			default :
				break;
		}
			
		ClasspathEntry newEntry= createEntry(entry, entryParent);
		Iterator entries= childEntries.iterator();
		while (entries.hasNext()) {
			Object element = entries.next();
			if (element instanceof ClasspathGroup) {
				if(((ClasspathGroup)element).contains(newEntry)) {
					return null;
				}
			} else if (element.equals(newEntry)) {
				return null;
			}
		}
		if (entryParent != null) {
			((ClasspathGroup)entryParent).addEntry(newEntry);
		} else {
			childEntries.add(newEntry);
		}
		return newEntry;		
	}
	
	public IClasspathEntry[] getEntries(int entryType) {
		IClasspathEntry[] classpathEntries= null;
		switch (entryType) {
			case BOOTSTRAP :
				if (bootstrapEntries != null) {
					classpathEntries= bootstrapEntries.getEntries();
				}
				break;
			case USER :
				if (userEntries != null) {
					classpathEntries= userEntries.getEntries();
				}
				break;
			default :
				return null;
		}
		return classpathEntries;
	}
	
	public void remove(Object entry) {
		childEntries.remove(entry);
	}
	
	public ClasspathEntry createEntry(IRuntimeClasspathEntry entry, IClasspathEntry entryParent) {
		if (entryParent == null) {
			entryParent= this;
		} 
		return new ClasspathEntry(entry, entryParent);
	}

	public void removeAll() {
		if (bootstrapEntries != null) {
			bootstrapEntries.removeAll();
		} 
		if (userEntries != null) {
			userEntries.removeAll();
		}
	}
	
	public void removeAll(Object[] entries) {
		
		for (int i = 0; i < entries.length; i++) {
			Object object = entries[i];
			if (object instanceof ClasspathEntry) {
				IClasspathEntry entryParent= ((ClasspathEntry)object).getParent();
				if (entryParent instanceof ClasspathGroup) {
					((ClasspathGroup)entryParent).removeEntry((ClasspathEntry) object);
				} else {
					remove(object);
				}
			} else {
				remove(object);
			}
		}
	}
	
	public void setBootstrapEntries(IRuntimeClasspathEntry[] entries) {
		if (bootstrapEntries == null) {
			getBootstrapEntry();
		} 
		bootstrapEntries.removeAll();
		for (int i = 0; i < entries.length; i++) {
			bootstrapEntries.addEntry(new ClasspathEntry(entries[i], bootstrapEntries));
		}
	}

	private ClasspathGroup createGlobalEntry(IRuntimeClasspathEntry[] entries, ClasspathGroup entryParent, String name, boolean canBeRemoved, boolean addEntry) {
		
		ClasspathGroup global= new ClasspathGroup(name, entryParent, canBeRemoved);
		
		for (int i = 0; i < entries.length; i++) {
			global.addEntry(new ClasspathEntry(entries[i], global));
		}
		
		if (addEntry) {
			addEntry(global);
		}
		return global;
	}

	public void setUserEntries(IRuntimeClasspathEntry[] entries) {
		if (userEntries == null) {
			getUserEntry();
		} 
		userEntries.removeAll();
		for (int i = 0; i < entries.length; i++) {
			userEntries.addEntry(new ClasspathEntry(entries[i], userEntries));
		}
	}
	
//	public IClasspathEntry[] getAllUserEntries() {
//		List allUserEntries= new ArrayList(childEntries.size());
//		Iterator itr= childEntries.iterator();
//		while (itr.hasNext()) {
//			IClasspathEntry element = (IClasspathEntry) itr.next();
//			if (element instanceof GlobalClasspathEntries) {
//				continue;
//			}
//			allUserEntries.add(element);
//		}
//		return (IClasspathEntry[])allUserEntries.toArray(new IClasspathEntry[allUserEntries.size()]);
//	}

	/**
	 * @return
	 */
//	public Object[] getRemovedGlobalEntries() {
//		if (userEntries == null) {
//			String name= "User";
//			return new Object[] {createGlobalEntry(new IRuntimeClasspathEntry[0], name, true, false)};
//		}
//		return new Object[] {};
//	}

	public IClasspathEntry getBootstrapEntry() {
		if (bootstrapEntries == null) {
			String name= "Bootstrap Entries";
			bootstrapEntries= createGlobalEntry(new IRuntimeClasspathEntry[0], null, name, false, true);
		}
		return bootstrapEntries;
	}
	
	public IClasspathEntry getUserEntry() {
		if (userEntries == null) {
			String name= "User Entries";
			userEntries= createGlobalEntry(new IRuntimeClasspathEntry[0], null, name, false, true);
		}
		return userEntries;
	}

	public void checkConsistancy() {
		if (defaultBootstrapEntries != null && !defaultBootstrapEntries.hasEntries()) {
			bootstrapEntries.removeEntry(defaultBootstrapEntries);
			defaultBootstrapEntries= null;
		}
		if (defaultUserEntries!= null && !defaultUserEntries.hasEntries()) {
			userEntries.removeEntry(defaultUserEntries);
			defaultUserEntries= null;
			
		}		
	}
}