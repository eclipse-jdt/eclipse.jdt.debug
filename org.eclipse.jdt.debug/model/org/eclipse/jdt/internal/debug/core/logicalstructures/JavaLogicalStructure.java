/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;

import com.ibm.icu.text.MessageFormat;

public class JavaLogicalStructure implements ILogicalStructureType {

	private static IStatusHandler fgStackFrameProvider;

	/**
	 * Fully qualified type name.
	 */
	private String fType;
	/**
	 * Indicate if this java logical structure should be used on object instance
	 * of subtype of the specified type.
	 */
	private boolean fSubtypes;
	/**
	 * Code snippet to evaluate to create the logical value.
	 */
	private String fValue;
	/**
	 * Description of the logical structure.
	 */
	private String fDescription;
	/**
	 * Name and associated code snippet of the variables of the logical value.
	 */
	private String[][] fVariables;
	/**
	 * The plugin identifier of the plugin which contributed this logical
	 * structure or <code>null</code> if this structure was defined by the user.
	 */
	private String fContributingPluginId = null;

	/**
	 * Performs the evaluations.
	 */
	private class EvaluationBlock implements IEvaluationListener {

		private IJavaObject fEvaluationValue;
		private IJavaReferenceType fEvaluationType;
		private IJavaThread fThread;
		private IAstEvaluationEngine fEvaluationEngine;
		private IEvaluationResult fResult;

		/**
		 * Constructor
		 * 
		 * @param value
		 * @param type
		 * @param thread
		 * @param evaluationEngine
		 */
		public EvaluationBlock(IJavaObject value, IJavaReferenceType type,
				IJavaThread thread, IAstEvaluationEngine evaluationEngine) {
			fEvaluationValue = value;
			fEvaluationType = type;
			fThread = thread;
			fEvaluationEngine = evaluationEngine;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.debug.eval.IEvaluationListener#evaluationComplete
		 * (org.eclipse.jdt.debug.eval.IEvaluationResult)
		 */
		public void evaluationComplete(IEvaluationResult result) {
			synchronized (this) {
				fResult = result;
				this.notify();
			}
		}

		/**
		 * Evaluates the specified snippet and returns the
		 * <code>IJavaValue</code> from the evaluation
		 * 
		 * @param snippet
		 *            the snippet to evaluate
		 * @return the <code>IJavaValue</code> from the evaluation
		 * @throws DebugException
		 */
		public IJavaValue evaluate(String snippet) throws DebugException {
			ICompiledExpression compiledExpression = fEvaluationEngine
					.getCompiledExpression(snippet, fEvaluationType);
			if (compiledExpression.hasErrors()) {
				String[] errorMessages = compiledExpression.getErrorMessages();
				log(errorMessages);
				return new JavaStructureErrorValue(errorMessages,
						fEvaluationValue);
			}
			fResult = null;
			fEvaluationEngine.evaluateExpression(compiledExpression,
					fEvaluationValue, fThread, this,
					DebugEvent.EVALUATION_IMPLICIT, false);
			synchronized (this) {
				if (fResult == null) {
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			if (fResult == null) {
				return new JavaStructureErrorValue(
						LogicalStructuresMessages.JavaLogicalStructure_1,
						fEvaluationValue);
			}
			if (fResult.hasErrors()) {
				DebugException exception = fResult.getException();
				String message;
				if (exception != null) {
					if (exception.getStatus().getException() instanceof UnsupportedOperationException) {
						message = LogicalStructuresMessages.JavaLogicalStructure_0;
					} else if (exception.getStatus().getCode() == IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
						// throw this exception so the content provider can
						// handle if (cancel the update)
						throw exception;
					} else {
						message = MessageFormat.format(LogicalStructuresMessages.JavaLogicalStructure_2, exception.getMessage());
					}
				} else {
					message = LogicalStructuresMessages.JavaLogicalStructure_3;
				}
				return new JavaStructureErrorValue(message, fEvaluationValue);
			}
			return fResult.getValue();
		}

		/**
		 * Logs the given error messages if this logical structure was
		 * contributed via extension.
		 */
		private void log(String[] messages) {
			if (isContributed()) {
				StringBuffer log = new StringBuffer();
				for (String message : messages) {
					log.append(message).append('\n');
				}
				JDIDebugPlugin.log(new Status(IStatus.ERROR, JDIDebugPlugin
						.getUniqueIdentifier(), IStatus.ERROR, log.toString(),
						null));
			}
		}
	}

	/**
	 * Constructor from parameters.
	 */
	public JavaLogicalStructure(String type, boolean subtypes, String value,
			String description, String[][] variables) {
		fType = type;
		fSubtypes = subtypes;
		fValue = value;
		fDescription = description;
		fVariables = variables;
	}

	/**
	 * Constructor from configuration element.
	 */
	public JavaLogicalStructure(IConfigurationElement configurationElement)
			throws CoreException {
		fType = configurationElement.getAttribute("type"); //$NON-NLS-1$
		if (fType == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.ERROR,
					LogicalStructuresMessages.JavaLogicalStructures_0, null));
		}
		fSubtypes = Boolean.valueOf(
				configurationElement.getAttribute("subtypes")).booleanValue(); //$NON-NLS-1$
		fValue = configurationElement.getAttribute("value"); //$NON-NLS-1$
		fDescription = configurationElement.getAttribute("description"); //$NON-NLS-1$
		if (fDescription == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.ERROR,
					LogicalStructuresMessages.JavaLogicalStructures_4, null));
		}
		IConfigurationElement[] variableElements = configurationElement
				.getChildren("variable"); //$NON-NLS-1$
		if (fValue == null && variableElements.length == 0) {
			throw new CoreException(new Status(IStatus.ERROR,
					JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.ERROR,
					LogicalStructuresMessages.JavaLogicalStructures_1, null));
		}
		fVariables = new String[variableElements.length][2];
		for (int j = 0; j < fVariables.length; j++) {
			String variableName = variableElements[j].getAttribute("name"); //$NON-NLS-1$
			if (variableName == null) {
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								JDIDebugPlugin.getUniqueIdentifier(),
								JDIDebugPlugin.ERROR,
								LogicalStructuresMessages.JavaLogicalStructures_2,
								null));
			}
			fVariables[j][0] = variableName;
			String variableValue = variableElements[j].getAttribute("value"); //$NON-NLS-1$
			if (variableValue == null) {
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								JDIDebugPlugin.getUniqueIdentifier(),
								JDIDebugPlugin.ERROR,
								LogicalStructuresMessages.JavaLogicalStructures_3,
								null));
			}
			fVariables[j][1] = variableValue;
		}
		fContributingPluginId = configurationElement.getContributor().getName();
	}

	/**
	 * @see org.eclipse.debug.core.model.ILogicalStructureTypeDelegate#providesLogicalStructure(IValue)
	 */
	public boolean providesLogicalStructure(IValue value) {
		if (!(value instanceof IJavaObject)) {
			return false;
		}
		return getType((IJavaObject) value) != null;
	}

	/**
	 * @see org.eclipse.debug.core.model.ILogicalStructureTypeDelegate#getLogicalStructure(IValue)
	 */
	public IValue getLogicalStructure(IValue value) throws CoreException {
		if (!(value instanceof IJavaObject)) {
			return value;
		}
		IJavaObject javaValue = (IJavaObject) value;
		try {
			IJavaReferenceType type = getType(javaValue);
			if (type == null) {
				return value;
			}
			IJavaStackFrame stackFrame = getStackFrame(javaValue);
			if (stackFrame == null) {
				return value;
			}

			// find the project the snippets will be compiled in.
			ISourceLocator locator = javaValue.getLaunch().getSourceLocator();
			Object sourceElement = null;
			if (locator instanceof ISourceLookupDirector) {
				String[] sourcePaths = type.getSourcePaths(null);
				if (sourcePaths != null && sourcePaths.length > 0) {
					sourceElement = ((ISourceLookupDirector) locator)
							.getSourceElement(sourcePaths[0]);
				}
				if (!(sourceElement instanceof IJavaElement)
						&& sourceElement instanceof IAdaptable) {
					sourceElement = ((IAdaptable) sourceElement)
							.getAdapter(IJavaElement.class);
				}
			}
			if (sourceElement == null) {
				sourceElement = locator.getSourceElement(stackFrame);
				if (!(sourceElement instanceof IJavaElement)
						&& sourceElement instanceof IAdaptable) {
					sourceElement = ((IAdaptable) sourceElement)
							.getAdapter(IJavaElement.class);
				}
			}
			IJavaProject project = null;
			if (sourceElement instanceof IJavaElement) {
				project = ((IJavaElement) sourceElement).getJavaProject();
			} else if (sourceElement instanceof IResource) {
				IJavaProject resourceProject = JavaCore
						.create(((IResource) sourceElement).getProject());
				if (resourceProject.exists()) {
					project = resourceProject;
				}
			}
			if (project == null) {
				return value;
			}

			IAstEvaluationEngine evaluationEngine = JDIDebugPlugin.getDefault()
					.getEvaluationEngine(project,
							(IJavaDebugTarget) stackFrame.getDebugTarget());

			EvaluationBlock evaluationBlock = new EvaluationBlock(javaValue,
					type, (IJavaThread) stackFrame.getThread(),
					evaluationEngine);
			if (fValue == null) {
				// evaluate each variable
				IJavaVariable[] variables = new IJavaVariable[fVariables.length];
				for (int i = 0; i < fVariables.length; i++) {
					variables[i] = new JDIPlaceholderVariable(fVariables[i][0],
							evaluationBlock.evaluate(fVariables[i][1]),
							javaValue);
				}
				return new LogicalObjectStructureValue(javaValue, variables);
			}
			// evaluate the logical value
			IJavaValue logicalValue = evaluationBlock.evaluate(fValue);
			if (logicalValue instanceof JDIValue) {
				((JDIValue) logicalValue).setLogicalParent(javaValue);
			}
			return logicalValue;

		} catch (CoreException e) {
			if (e.getStatus().getCode() == IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
				throw e;
			}
			JDIDebugPlugin.log(e);
		}
		return value;
	}

	/**
	 * Returns the <code>IJavaReferenceType</code> from the specified
	 * <code>IJavaObject</code>
	 * 
	 * @param value
	 * @return the <code>IJavaReferenceType</code> from the specified
	 *         <code>IJavaObject</code>
	 */
	private IJavaReferenceType getType(IJavaObject value) {
		try {
			IJavaType type = value.getJavaType();
			if (!(type instanceof IJavaClassType)) {
				return null;
			}
			IJavaClassType classType = (IJavaClassType) type;
			if (classType.getName().equals(fType)) {
				// found the type
				return classType;
			}
			if (!fSubtypes) {
				// if not checking the subtypes, stop here
				return null;
			}
			IJavaClassType superClass = classType.getSuperclass();
			while (superClass != null) {
				if (superClass.getName().equals(fType)) {
					// found the type, it's a super class
					return superClass;
				}
				superClass = superClass.getSuperclass();
			}
			IJavaInterfaceType[] superInterfaces = classType.getAllInterfaces();
			for (IJavaInterfaceType superInterface : superInterfaces) {
				if (superInterface.getName().equals(fType)) {
					// found the type, it's a super interface
					return superInterface;
				}
			}
		} catch (DebugException e) {
			JDIDebugPlugin.log(e);
			return null;
		}
		return null;
	}

	/**
	 * Return the current stack frame context, or a valid stack frame for the
	 * given value.
	 * 
	 * @param value
	 * @return the current stack frame context, or a valid stack frame for the
	 *         given value.
	 * @throws CoreException
	 */
	private IJavaStackFrame getStackFrame(IValue value) throws CoreException {
		IStatusHandler handler = getStackFrameProvider();
		if (handler != null) {
			IJavaStackFrame stackFrame = (IJavaStackFrame) handler
					.handleStatus(JDIDebugPlugin.STATUS_GET_EVALUATION_FRAME,
							value);
			if (stackFrame != null) {
				return stackFrame;
			}
		}
		IDebugTarget target = value.getDebugTarget();
		IJavaDebugTarget javaTarget = (IJavaDebugTarget) target
				.getAdapter(IJavaDebugTarget.class);
		if (javaTarget != null) {
			IThread[] threads = javaTarget.getThreads();
			for (IThread thread : threads) {
				if (thread.isSuspended()) {
					return (IJavaStackFrame) thread.getTopStackFrame();
				}
			}
		}
		return null;
	}

	/**
	 * Returns the singleton stackframe provider
	 * 
	 * @return the singleton stackframe provider
	 */
	private static IStatusHandler getStackFrameProvider() {
		if (fgStackFrameProvider == null) {
			fgStackFrameProvider = DebugPlugin.getDefault().getStatusHandler(
					JDIDebugPlugin.STATUS_GET_EVALUATION_FRAME);
		}
		return fgStackFrameProvider;
	}

	/**
	 * Returns if this logical structure should be used for subtypes too.
	 * 
	 * @return if this logical structure should be used for subtypes too.
	 */
	public boolean isSubtypes() {
		return fSubtypes;
	}

	/**
	 * Sets if this logical structure should be used for subtypes or not.
	 * 
	 * @param subtypes
	 */
	public void setSubtypes(boolean subtypes) {
		fSubtypes = subtypes;
	}

	/**
	 * Returns the name of the type this logical structure should be used for.
	 * 
	 * @return the name of the type this logical structure should be used for.
	 */
	public String getQualifiedTypeName() {
		return fType;
	}

	/**
	 * Sets the name of the type this logical structure should be used for.
	 * 
	 * @param type
	 */
	public void setType(String type) {
		fType = type;
	}

	/**
	 * Returns the code snippet to use to generate the logical structure.
	 * 
	 * @return the code snippet to use to generate the logical structure.
	 */
	public String getValue() {
		return fValue;
	}

	/**
	 * Sets the code snippet to use to generate the logical structure.
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		fValue = value;
	}

	/**
	 * Returns the variables of this logical structure.
	 * 
	 * @return the variables of this logical structure.
	 */
	public String[][] getVariables() {
		return fVariables;
	}

	/**
	 * Sets the variables of this logical structure.
	 * 
	 * @param variables
	 */
	public void setVariables(String[][] variables) {
		fVariables = variables;
	}

	/**
	 * Set the description of this logical structure.
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		fDescription = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.ILogicalStructureTypeDelegate2#getDescription
	 * (org.eclipse.debug.core.model.IValue)
	 */
	public String getDescription(IValue value) {
		return getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.ILogicalStructureType#getDescription()
	 */
	public String getDescription() {
		return fDescription;
	}

	/**
	 * Indicates if this logical structure was contributed by a plug-in or
	 * defined by a user.
	 * 
	 * @return if this logical structure is contributed
	 */
	public boolean isContributed() {
		return fContributingPluginId != null;
	}

	/**
	 * Returns the plugin identifier of the plugin which contributed this
	 * logical structure or <code>null</code> if this structure was defined by
	 * the user.
	 * 
	 * @return the plugin identifier of the plugin which contributed this
	 *         structure or <code>null</code>
	 */
	public String getContributingPluginId() {
		return fContributingPluginId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.ILogicalStructureType#getId()
	 */
	public String getId() {
		return JDIDebugPlugin.getUniqueIdentifier() + fType + fDescription;
	}
}
