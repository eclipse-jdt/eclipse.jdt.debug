/*******************************************************************************
 * Copyright (c) 2011-2016 Igor Fedorenko
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.sourcelookup.advanced;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;

public final class JDIHelpers implements IJDIHelpers {

	// must match ClassfileTransformer.STRATA_ID
	public static final String STRATA_ID = "jdt"; //$NON-NLS-1$

	JDIHelpers() {
	}

	// jdt debug boilerplate and other ideas were originally "borrowed" from
	// org.eclipse.pde.internal.launching.sourcelookup.PDESourceLookupQuery.run()

	@Override
	public File getClassesLocation(Object element) throws DebugException {
		IJavaReferenceType declaringType = null;
		if (element instanceof IJavaStackFrame) {
			IJavaStackFrame stackFrame = (IJavaStackFrame) element;
			declaringType = stackFrame.getReferenceType();
		} else if (element instanceof IJavaObject) {
			IJavaType javaType = ((IJavaObject) element).getJavaType();
			if (javaType instanceof IJavaReferenceType) {
				declaringType = (IJavaReferenceType) javaType;
			}
		} else if (element instanceof IJavaReferenceType) {
			declaringType = (IJavaReferenceType) element;
		} else if (element instanceof IJavaVariable) {
			IJavaVariable javaVariable = (IJavaVariable) element;
			IJavaType javaType = ((IJavaValue) javaVariable.getValue()).getJavaType();
			if (javaType instanceof IJavaReferenceType) {
				declaringType = (IJavaReferenceType) javaType;
			}
		}

		if (declaringType != null) {
			String[] locations = declaringType.getSourceNames(STRATA_ID);

			if (locations == null || locations.length < 2) {
				return null;
			}

			String spec = locations[1].trim();
			try {
				if (spec.startsWith("file:")) { //$NON-NLS-1$
					if (!spec.startsWith("file:/")) { //$NON-NLS-1$
						// opaque -> URI is not hierarchical https://github.com/eclipse-jdt/eclipse.jdt.debug/issues/330
						spec = spec.replace("file:", "file:/"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					URI uri = URI.create(spec);
					return Path.of(uri).normalize().toFile();
				}
			} catch (Exception e) {
				IStatus status = Status.error("Unable to resolve class location from: '" + spec + "'", e);  //$NON-NLS-1$//$NON-NLS-2$
				LaunchingPlugin.log(status);
			}
		}

		return null;
	}

	@Override
	public String getSourcePath(Object element) throws DebugException {
		IJavaReferenceType declaringType = null;
		if (element instanceof IJavaStackFrame) {
			IJavaStackFrame stackFrame = (IJavaStackFrame) element;
			// under JSR 45 source path from the stack frame is more precise than anything derived from the type
			String sourcePath = stackFrame.getSourcePath(STRATA_ID);
			if (sourcePath != null) {
				return sourcePath;
			}

			declaringType = stackFrame.getReferenceType();
		} else if (element instanceof IJavaObject) {
			IJavaType javaType = ((IJavaObject) element).getJavaType();
			if (javaType instanceof IJavaReferenceType) {
				declaringType = (IJavaReferenceType) javaType;
			}
		} else if (element instanceof IJavaReferenceType) {
			declaringType = (IJavaReferenceType) element;
		} else if (element instanceof IJavaVariable) {
			IJavaType javaType = ((IJavaVariable) element).getJavaType();
			if (javaType instanceof IJavaReferenceType) {
				declaringType = (IJavaReferenceType) javaType;
			}
		}

		if (declaringType != null) {
			String[] sourcePaths = declaringType.getSourcePaths(STRATA_ID);

			if (sourcePaths != null && sourcePaths.length > 0 && sourcePaths[0] != null) {
				return sourcePaths[0];
			}

			return generateSourceName(declaringType.getName());
		}

		return null;
	}

	private static final IStackFrame[] EMPTY_STACK = new IStackFrame[0];

	private IStackFrame[] getStackFrames(Object element) throws DebugException {
		if (element instanceof IStackFrame) {
			IStackFrame[] frames = ((IStackFrame) element).getThread().getStackFrames();
			for (int i = 0; i < frames.length - 1; i++) {
				if (frames[i] == element) {
					return Arrays.copyOfRange(frames, i + 1, frames.length);
				}
			}
		}
		return EMPTY_STACK;
	}

	@Override
	public Iterable<File> getStackFramesClassesLocations(Object element) throws DebugException {
		IStackFrame[] stack = getStackFrames(element);

		return new Iterable<>() {
			@Override
			public Iterator<File> iterator() {
				return Arrays.stream(stack) //
						.map(this::getClassesLocation) //
						.filter(frameLocation -> frameLocation != null) //
						.iterator();
			}

			private File getClassesLocation(IStackFrame frame) {
				// TODO consider ignoring DebugException for all IJDIHeloper methods
				try {
					return JDIHelpers.this.getClassesLocation(frame);
				}
				catch (DebugException e) {
					return null;
				}
			}
		};
	}

	// copy&paste from org.eclipse.pde.internal.launching.sourcelookup.PDESourceLookupQuery.generateSourceName(String)
	private static String generateSourceName(String qualifiedTypeName) {
		int index = qualifiedTypeName.indexOf('$');
		if (index >= 0) {
			qualifiedTypeName = qualifiedTypeName.substring(0, index);
		}
		return qualifiedTypeName.replace('.', File.separatorChar) + ".java"; //$NON-NLS-1$
	}
}
