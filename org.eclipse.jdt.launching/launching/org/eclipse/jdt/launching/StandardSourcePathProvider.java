/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.DefaultProjectClasspathEntry;
import org.eclipse.jdt.internal.launching.VariableClasspathEntry;

/**
 * Default implementation of source lookup path computation and resolution.
 * <p>
 * This class may be sub-classed.
 * </p>
 * @since 2.0
 */
public class StandardSourcePathProvider extends StandardClasspathProvider {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathProvider#computeUnresolvedClasspath(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
		boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, true);
		IRuntimeClasspathEntry[] entries = null;
		if (useDefault) {
			// the default source lookup path is the same as the classpath
			entries = super.computeUnresolvedClasspath(configuration);
		} else {
			// recover persisted source path
			entries = recoverRuntimePath(configuration, IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH);
		}
		return entries;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathProvider#resolveClasspath(org.eclipse.jdt.launching.IRuntimeClasspathEntry[], org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
		List<IRuntimeClasspathEntry> all = new UniqueList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			switch (entries[i].getType()) {
				case IRuntimeClasspathEntry.PROJECT:
					// a project resolves to itself for source lookup (rather than the class file output locations)
					all.add(entries[i]);
					break;
				case IRuntimeClasspathEntry.OTHER:
					IRuntimeClasspathEntry2 entry = (IRuntimeClasspathEntry2)entries[i];
					String typeId = entry.getTypeId();
					IRuntimeClasspathEntry[] res = null;
					if (typeId.equals(DefaultProjectClasspathEntry.TYPE_ID)) {
						// add the resolved children of the project
						IRuntimeClasspathEntry[] children = entry.getRuntimeClasspathEntries(configuration);
						res = JavaRuntime.resolveSourceLookupPath(children, configuration);
					} else if (typeId.equals(VariableClasspathEntry.TYPE_ID)) {
						// add the archive itself - we currently do not allow a source attachment
						res = JavaRuntime.resolveRuntimeClasspathEntry(entry, configuration);
					} else {
                        res = JavaRuntime.resolveRuntimeClasspathEntry(entry, configuration);
                    }
					if (res != null) {
						for (int j = 0; j < res.length; j++) {
							all.add(res[j]);
                            addManifestReferences(res[j], all);
						}
					}
					break;
				default:
					IRuntimeClasspathEntry[] resolved =JavaRuntime.resolveRuntimeClasspathEntry(entries[i], configuration);
					for (int j = 0; j < resolved.length; j++) {
						all.add(resolved[j]);
                        addManifestReferences(resolved[j], all);
					}
					break;
			}
		}
		return all.toArray(new IRuntimeClasspathEntry[all.size()]);
	}

    /**
     * If the given entry is an archive, adds any archives referenced by the associated manifest.
     * 
     * @param entry runtime classpath entry
     * @param all list to add references to
     */
    protected void addManifestReferences(IRuntimeClasspathEntry entry, List<IRuntimeClasspathEntry> all) {
        if (entry.getType() == IRuntimeClasspathEntry.ARCHIVE) {
            String location = entry.getLocation();
            if (location != null) {
                JarFile jar = null;
                try {
                    jar = new JarFile(location, false);
                    Manifest manifest = jar.getManifest();
                    if (manifest != null) {
                        Attributes mainAttributes = manifest.getMainAttributes();
                        if (mainAttributes != null) {
                            String value = mainAttributes.getValue(Attributes.Name.CLASS_PATH);
                            if (value != null) {
                                String[] entries = value.split("\\s+"); //$NON-NLS-1$
                                IPath base = new Path(location);
                                base = base.removeLastSegments(1);
                                for (int i = 0; i < entries.length; i++) {
                                    IPath path = base.append(entries[i]);
                                    if (path.toFile().exists()) {
                                        IRuntimeClasspathEntry ref = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
                                        if (!all.contains(ref)) {
                                            all.add(ref);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                } finally {
                    if (jar != null) {
                        try {
                            jar.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
    }

    /*
     * An ArrayList that acts like a set -i.e. does not allow duplicate items.
     * hack for bug 112774
     */
    class UniqueList extends ArrayList<IRuntimeClasspathEntry> {
        private static final long serialVersionUID = -7402160651027036270L;
        HashSet<IRuntimeClasspathEntry> set;
        
        public UniqueList(int length) {
            super(length);
            set = new HashSet<IRuntimeClasspathEntry>(length);
        }

        @Override
		public void add(int index, IRuntimeClasspathEntry element) {
            if (set.add(element))
                super.add(index, element);
        }

        @Override
		public boolean add(IRuntimeClasspathEntry o) {
            if (set.add(o))
                return super.add(o);
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends IRuntimeClasspathEntry> c) {
        	if (set.addAll(c))
                return super.addAll(c);
            return false;
        }

        @Override
		public boolean addAll(int index, Collection<? extends IRuntimeClasspathEntry> c) {
            if (set.addAll(c))
                return super.addAll(index, c);
            return false;
        }

        @Override
		public void clear() {
            set.clear();
            super.clear();
        }

        @Override
		public boolean contains(Object elem) {
            return set.contains(elem);
        }

        @Override
		public void ensureCapacity(int minCapacity) {
            super.ensureCapacity(minCapacity);
        }

        @Override
		public IRuntimeClasspathEntry remove(int index) {
            IRuntimeClasspathEntry object = super.remove(index);
            set.remove(object);
            return object;
        }

        @Override
		protected void removeRange(int fromIndex, int toIndex) {
            for (int index = fromIndex; index<=toIndex; index++)
                remove(index);
        }

        @Override
		public IRuntimeClasspathEntry set(int index, IRuntimeClasspathEntry element) {
            set.remove(element);
            if (set.add(element))
                return super.set(index, element);
            return null; //should not happen.
        }        
    }

}
