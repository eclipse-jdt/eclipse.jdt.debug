package org.eclipse.jdt.internal.debug.core.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaElementMapper;
import org.eclipse.jdt.core.refactoring.RenameTypeArguments;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

public class WatchpointTypeRenameChange extends WatchpointTypeChange {

	private RefactoringProcessor fProcessor;
	private RenameTypeArguments fArguments;

	public WatchpointTypeRenameChange(IJavaWatchpoint watchpoint, IType destType, IType originalType, RefactoringProcessor processor, RenameTypeArguments arguments) throws CoreException {
		super(watchpoint, destType, originalType);
		fProcessor = processor;
		fArguments = arguments;
	}
	
	public Change perform(IProgressMonitor pm) throws CoreException {
		IField originalField = getOriginalType().getField(getFieldName());
		IField destinationField = null;
		
		if (fArguments.getUpdateSimilarDeclarations()) {
			IJavaElementMapper elementMapper = (IJavaElementMapper) fProcessor.getAdapter(IJavaElementMapper.class);
			destinationField = (IField) elementMapper.getRefactoredJavaElement(originalField);
		}
		if (destinationField == null) {
			destinationField = getDestinationType().getField(getFieldName());
		}
		
		Map map = new HashMap();
		BreakpointUtils.addJavaBreakpointAttributes(map, destinationField);
		IResource resource = BreakpointUtils.getBreakpointResource(destinationField);
		IJavaWatchpoint breakpoint = JDIDebugModel.createWatchpoint(
				resource,
				getDestinationType().getFullyQualifiedName(),
				destinationField.getElementName(),
				getLineNumber(),
				getCharStart(),
				getCharEnd(),
				0,
				true,
				map);
		apply(breakpoint);
		getOriginalBreakpoint().delete();
		return new DeleteBreakpointChange(breakpoint);
	}

}
