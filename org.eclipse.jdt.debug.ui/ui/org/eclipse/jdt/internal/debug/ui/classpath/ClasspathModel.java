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

package org.eclipse.jdt.internal.debug.ui.classpath;

import java.util.Iterator;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

public class ClasspathModel extends AbstractClasspathEntry {
	
	public static final int BOOTSTRAP= 0;
	public static final int USER= 1;
	
	private ClasspathGroup bootstrapEntries;
	private ClasspathGroup userEntries;
	
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
			case USER :
				entryParent= getUserEntry();
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
	
	/**
	 * Returns the entries of the given type, or an empty
	 * collection if none.
	 * 
	 * @param entryType
	 * @return the entries of the given type, or an empty
	 * collection if none
	 */
	public IClasspathEntry[] getEntries(int entryType) {
		switch (entryType) {
			case BOOTSTRAP :
				if (bootstrapEntries != null) {
					return bootstrapEntries.getEntries();
				}
				break;
			case USER :
				if (userEntries != null) {
					return userEntries.getEntries();
				}
				break;
		}
		return new IClasspathEntry[0];
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

	private ClasspathGroup createGroupEntry(IRuntimeClasspathEntry[] entries, ClasspathGroup entryParent, String name, boolean canBeRemoved, boolean addEntry) {
		
		ClasspathGroup group= new ClasspathGroup(name, entryParent, canBeRemoved);
		
		for (int i = 0; i < entries.length; i++) {
			group.addEntry(new ClasspathEntry(entries[i], group));
		}
		
		if (addEntry) {
			addEntry(group);
		}
		return group;
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

	public IClasspathEntry getBootstrapEntry() {
		if (bootstrapEntries == null) {
			String name= ClasspathMessages.getString("ClasspathModel.0"); //$NON-NLS-1$
			bootstrapEntries= createGroupEntry(new IRuntimeClasspathEntry[0], null, name, false, true);
		}
		return bootstrapEntries;
	}
	
	public IClasspathEntry getUserEntry() {
		if (userEntries == null) {
			String name= ClasspathMessages.getString("ClasspathModel.1"); //$NON-NLS-1$
			userEntries= createGroupEntry(new IRuntimeClasspathEntry[0], null, name, false, true);
		}
		return userEntries;
	}
}