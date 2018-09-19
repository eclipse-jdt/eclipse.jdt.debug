/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.InstructionPointerAnnotation;
import org.eclipse.debug.internal.ui.InstructionPointerManager;
import org.eclipse.debug.internal.ui.viewers.model.InternalTreeModelViewer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests functionality of the InstructionPointerManager.
 * The tests are not currently part of the automated suite because they produce
 * transient failures that could not be tracked down.
 *
 * @since 3.3
 * @see InstructionPointerManager
 */
public class InstructionPointerManagerTests extends AbstractDebugTest {

	private Object fLock = new Object();
	private Annotation fAddedAnnotation = null;
	private Annotation fRemovedAnnotation = null;

	private MyPerspectiveListener fPerspectiveListener;
	private MyAnnotationListener fAnnotationListener;
	private IPartListener2 fPartListener;
	private Set<IAnnotationModel> fAnnotationModelsWithListeners = new HashSet<>();

	private static final String typeThreadStack = "org.eclipse.debug.tests.targets.ThreadStack";
	private static final String typeClassOne = "org.eclipse.debug.tests.targets.ClassOne";
	private static final String typeClassTwo = "org.eclipse.debug.tests.targets.ClassTwo";

	private IJavaDebugTarget target1;
	private IJavaDebugTarget target2;
	private IJavaThread thread1;
	private IJavaThread thread2;
	private IJavaThread thread3;
	private IJavaThread thread4;

	public InstructionPointerManagerTests(String name) {
		super(name);
	}

	public void testManagerWithEditorReuse() throws Exception{
		boolean restore = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_REUSE_EDITOR);
		DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_REUSE_EDITOR, true);

		try{
			addAndRemoveAnnotations(new int[]{1,2,1,2,1,0,1,1}, new int[]{1,1,1,1,1,0,1,1});
		} finally {
			System.out.println("Cleanup");
			if (target1 != null){
				terminateAndRemove(target1);
			}
			if (target2 != null){
				terminateAndRemove(target2);
			}
			Iterator<IAnnotationModel> annModels = fAnnotationModelsWithListeners.iterator();
			while (annModels.hasNext()) {
				IAnnotationModel currentModel = annModels.next();
				currentModel.removeAnnotationModelListener(getAnnotationListener());
			}
			removeAllBreakpoints();
			DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_REUSE_EDITOR, restore);
			Runnable cleanup = new Runnable() {
                @Override
				public void run() {
                    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    activeWorkbenchWindow.removePerspectiveListener(getPerspectiveListener());
                }
            };
            DebugUIPlugin.getStandardDisplay().asyncExec(cleanup);
		}
	}

	public void testManagerWithNoEditorReuse() throws Exception{
		boolean restore = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_REUSE_EDITOR);
		DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_REUSE_EDITOR, false);

		try{
			addAndRemoveAnnotations(new int[]{1,2,3,4,5,3,2,1}, new int[]{1,1,2,2,3,2,2,1});
		} finally {
			System.out.println("Cleanup");
			if (target1 != null){
				terminateAndRemove(target1);
			}
			if (target2 != null){
				terminateAndRemove(target2);
			}
			Iterator<IAnnotationModel> annModels = fAnnotationModelsWithListeners.iterator();
			while (annModels.hasNext()) {
				IAnnotationModel currentModel = annModels.next();
				currentModel.removeAnnotationModelListener(getAnnotationListener());
			}
			removeAllBreakpoints();
			DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_REUSE_EDITOR, restore);
			Runnable cleanup = new Runnable() {
                @Override
				public void run() {
                    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    activeWorkbenchWindow.removePerspectiveListener(getPerspectiveListener());
                }
            };
            DebugUIPlugin.getStandardDisplay().asyncExec(cleanup);
		}
	}

	/**
	 * Tests the ability of the manager to update it's set and mapping as
	 * annotations are added and removed.
	 *
	 * First, all editors are closed and the manager is checked to ensure there are 0 IPCs.
	 *
	 * <p>Next, annotations are created as follows:<br>
	 * (numbers in brackets correspond to index of expected IPC and Mapping counts checked)
	 * <pre>
	 * Target1	- Thread1	- IPC1	- ClassOne		- Editor1 (line 20)	[0]
	 *      	- Thread2	- IPC2	- ClassOne		- Editor1 (line 20)	[1]
	 * 						- IPC3	- ThreadStack	- Editor2 (line 28)	[2]
	 *
	 * Target2	- Thread3	- IPC4	- ThreadStack	- Editor2 (line 41) [3]
	 *      	- Thread4	- IPC5	- ClassTwo		- Editor3 (line 24) [4]
	 * </pre>
	 * </p>
	 * <p>They are then removed as follows:<br>
	 * (numbers in brackets correspond to index of expected IPC and Mapping counts checked)
	 * <ol>
	 * <li>Target2 is terminated [5]</li>
	 * <li>Thread1 is resumed [6]</li>
	 * <li>Editor2 is closed [7]</li>
	 * <li>All editors are closed, closing Editor1, No IPCs should exist</li>
	 * </ol>
	 * </p>
	 * @param expectedIPCCounts array of expected values for IPC count at each step as marked above, length must be 8
	 * @param expectedMappingCounts array of expected values for editor mapping count at each step as marked above, length must be 8
	 * @throws Exception
	 */
	private void addAndRemoveAnnotations(int[] expectedIPCCounts, int[] expectedMappingCounts) throws Exception{
		assertEquals("Incorrect number of expected counts", 8, expectedIPCCounts.length);
		assertEquals("Incorrect number of expected counts", 8, expectedMappingCounts.length);

		// Close all editors
	    Runnable closeAll = new Runnable() {
            @Override
			public void run() {
                IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                activeWorkbenchWindow.getActivePage().closeAllEditors(false);
                activeWorkbenchWindow.addPerspectiveListener(getPerspectiveListener());
            }
        };
        DebugUIPlugin.getStandardDisplay().syncExec(closeAll);
        assertEquals("Instruction pointer count was incorrect", 0, InstructionPointerManager.getDefault().getInstructionPointerCount());
        assertEquals("Editor mapping count was incorrect", 0, InstructionPointerManager.getDefault().getEditorMappingCount());

        // ADD ANNOTATIONS

        // Launch a target creating two threads, both suspend in ClassOne, one will automatically be selected
        IJavaLineBreakpoint breakpoint = createLineBreakpoint(20, typeClassOne);
        fAddedAnnotation = null;
        getPerspectiveListener().setTitle(typeClassOne);
        thread1 = launchAndSuspend(typeThreadStack);
        target1 = (IJavaDebugTarget)thread1.getDebugTarget();
        assertNotNull("Target was not launched.",target1);
        assertNotNull("Target was not launched.",thread1);
        waitForAnnotationToBeAdded();
        assertEquals("Instruction pointer count was incorrect", expectedIPCCounts[0], InstructionPointerManager.getDefault().getInstructionPointerCount());
        assertEquals("Editor mapping count was incorrect", expectedMappingCounts[0], InstructionPointerManager.getDefault().getEditorMappingCount());

        // Find and select the top stack frame of the other thread
        Runnable openParent = new Runnable() {
            @Override
			public void run() {
                IDebugView debugView = (IDebugView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.debug.ui.DebugView");
                Object[] newSegments = new Object[4];
            	newSegments[0] = target1.getLaunch();
            	newSegments[1] = target1;
            	try{
                	IThread[] threads = ((IJavaDebugTarget)newSegments[1]).getThreads();
                	for (int i = 0; i < threads.length; i++) {
						if (threads[i].isSuspended() && !threads[i].equals(thread1)){
							thread2 = (IJavaThread)threads[i];
							newSegments[2] = threads[i];
							newSegments[3] = threads[i].getTopStackFrame();
						}
					}
                	((InternalTreeModelViewer)debugView.getViewer()).setSelection(new TreeSelection(new TreePath(newSegments)), true, true);
            	} catch (DebugException e){
            		fail("Exception: " + e.getMessage());
            	}
            }
        };
        fAddedAnnotation = null;
        getPerspectiveListener().setTitle(typeClassOne);
        DebugUIPlugin.getStandardDisplay().syncExec(openParent);
        waitForAnnotationToBeAdded();
        assertNotNull("Thread not selected",thread2);
        assertEquals("Instruction pointer count was incorrect", expectedIPCCounts[1], InstructionPointerManager.getDefault().getInstructionPointerCount());
        assertEquals("Editor mapping count was incorrect", expectedMappingCounts[1], InstructionPointerManager.getDefault().getEditorMappingCount());

        // Select the same stack frame and make sure IPC count doesn't change
        Runnable selectSameStackFrame = new Runnable() {
            @Override
			public void run() {
                IDebugView debugView = (IDebugView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.debug.ui.DebugView");
                Object[] newSegments = new Object[4];
            	newSegments[0] = target1.getLaunch();
            	newSegments[1] = target1;
            	newSegments[2] = thread2;
            	try {
            		newSegments[3] = thread2.getTopStackFrame();
            	} catch (DebugException e) {
					fail("Exception: " + e.getMessage());
				}
                ((InternalTreeModelViewer)debugView.getViewer()).setSelection(new TreeSelection(new TreePath(newSegments)), true, true);
            }
        };
        fAddedAnnotation = null;
        getPerspectiveListener().setTitle(typeClassOne);
        DebugUIPlugin.getStandardDisplay().syncExec(selectSameStackFrame);
        waitForAnnotationToBeAdded();
        assertEquals("Instruction pointer count was incorrect", expectedIPCCounts[1], InstructionPointerManager.getDefault().getInstructionPointerCount());
        assertEquals("Editor mapping count was incorrect", expectedMappingCounts[1], InstructionPointerManager.getDefault().getEditorMappingCount());

        // Select the next stack frame in the same thread
        Runnable selectSecondStackFrame = new Runnable() {
            @Override
			public void run() {
                IDebugView debugView = (IDebugView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.debug.ui.DebugView");
                Object[] newSegments = new Object[4];
            	newSegments[0] = target1.getLaunch();
            	newSegments[1] = target1;
            	newSegments[2] = thread2;
            	try{
            		newSegments[3] = thread2.getStackFrames()[1];  // Select the next stack frame
            	} catch (DebugException e){
            		fail("Exception: " + e.getMessage());
            	}
            	((InternalTreeModelViewer)debugView.getViewer()).setSelection(new TreeSelection(new TreePath(newSegments)), true, true);
            }
        };
        fAddedAnnotation = null;
        getPerspectiveListener().setTitle(typeThreadStack);
        DebugUIPlugin.getStandardDisplay().syncExec(selectSecondStackFrame);
        waitForAnnotationToBeAdded();
        // Failure here, reuse, expected 1 but was 2, also with no reuse, expected 3 but was 2
        assertEquals("Instruction pointer count was incorrect", expectedIPCCounts[2], InstructionPointerManager.getDefault().getInstructionPointerCount());
        assertEquals("Editor mapping count was incorrect", expectedMappingCounts[2], InstructionPointerManager.getDefault().getEditorMappingCount());

        // Remove the breakpoint from before and create new ones, start a new target
        breakpoint.delete();
        createLineBreakpoint(41, typeThreadStack);
        createLineBreakpoint(24, typeClassTwo);
        target2 = (IJavaDebugTarget)launchAndSuspend(typeThreadStack).getDebugTarget();
        assertNotNull("Target was not launched", target2);
        assertEquals("Instruction pointer count was incorrect", expectedIPCCounts[2], InstructionPointerManager.getDefault().getInstructionPointerCount());
        assertEquals("Editor mapping count was incorrect", expectedMappingCounts[2], InstructionPointerManager.getDefault().getEditorMappingCount());

       // Select the stack frame from the new debug target displaying ThreadStack
       Runnable openOtherDebugTarget = new Runnable() {
            @Override
			public void run() {
                IDebugView debugView = (IDebugView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.debug.ui.DebugView");
                ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
                Object[] newSegments = new Object[4];
                for (int i = 0; i < launches.length; i++) {
					if (target2.equals(launches[i].getDebugTarget())){
						newSegments[0] = launches[i];
						newSegments[1] = target2;
						try{
							IThread[] threads = target2.getThreads();
							for (int j = 0; j < threads.length; j++) {
								if (threads[j].isSuspended()){
									if (typeThreadStack.equals(((IJavaStackFrame)threads[j].getTopStackFrame()).getDeclaringTypeName())){
										thread3 = (IJavaThread)threads[j];
										newSegments[2] = threads[j];
										newSegments[3] = threads[j].getTopStackFrame();
										break;
									}
								}
							}
						} catch (DebugException e){
							fail("Exception: " + e.getMessage());
						}
						break;
					}
				}
				((InternalTreeModelViewer)debugView.getViewer()).setSelection(new TreeSelection(new TreePath(newSegments)), true, true);
            }
       };
       fAddedAnnotation = null;
       getPerspectiveListener().setTitle(typeThreadStack);
       DebugUIPlugin.getStandardDisplay().syncExec(openOtherDebugTarget);
       assertNotNull("Thread was not selected",thread3);
       waitForAnnotationToBeAdded();
       assertEquals("Instruction pointer count was incorrect", expectedIPCCounts[3], InstructionPointerManager.getDefault().getInstructionPointerCount());
       assertEquals("Editor mapping count was incorrect", expectedMappingCounts[3], InstructionPointerManager.getDefault().getEditorMappingCount());

       // Select the other thread from the new target displaying ClassTwo
       Runnable openOtherThread = new Runnable() {
            @Override
			public void run() {
            	IDebugView debugView = (IDebugView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.debug.ui.DebugView");
                Object[] newSegments = new Object[4];
            	newSegments[0] = target2.getLaunch();
            	newSegments[1] = target2;
            	try{
                	IThread[] threads = target2.getThreads();
                	for (int i = 0; i < threads.length; i++) {
						if (threads[i].isSuspended() && !threads[i].equals(thread3)){
							thread4 = (IJavaThread)threads[i];
							newSegments[2] = threads[i];
							newSegments[3] = threads[i].getTopStackFrame();
						}
					}
                	((InternalTreeModelViewer)debugView.getViewer()).setSelection(new TreeSelection(new TreePath(newSegments)), true, true);
            	} catch (DebugException e){
            		fail("Exception: " + e.getMessage());
            	}
            }
       };
       fAddedAnnotation = null;
       getPerspectiveListener().setTitle(typeClassTwo);
       DebugUIPlugin.getStandardDisplay().syncExec(openOtherThread);
       assertNotNull("Thread was not selected",thread4);
       waitForAnnotationToBeAdded();
       assertEquals("Instruction pointer count was incorrect", expectedIPCCounts[4], InstructionPointerManager.getDefault().getInstructionPointerCount());
       assertEquals("Editor mapping count was incorrect", expectedMappingCounts[4], InstructionPointerManager.getDefault().getEditorMappingCount());

       // REMOVE ANNOTATIONS

       // Remove target2
       fRemovedAnnotation = null;
       target2.terminate();
       waitForAnnotationToBeRemoved();
       assertEquals("Instruction pointer count was incorrect", expectedIPCCounts[5], InstructionPointerManager.getDefault().getInstructionPointerCount());
       assertEquals("Editor mapping count was incorrect", expectedMappingCounts[5], InstructionPointerManager.getDefault().getEditorMappingCount());

       // TODO Selection of the other target does not occur automatically.  This functionality may change and will break this test.

       // Resume thread1
       fRemovedAnnotation = null;
       thread1.resume();
       waitForAnnotationToBeRemoved();
       assertEquals("Instruction pointer count was incorrect", expectedIPCCounts[6], InstructionPointerManager.getDefault().getInstructionPointerCount());
       assertEquals("Editor mapping count was incorrect", expectedMappingCounts[6], InstructionPointerManager.getDefault().getEditorMappingCount());

       // Close the editor displaying ThreadStack.java if it is open
	   Runnable closeEditor2 = new Runnable() {
           @Override
		public void run() {
               IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
               IEditorReference[] editors = activeWorkbenchWindow.getActivePage().getEditorReferences();
               for (int i = 0; i < editors.length; i++) {
            	   if (editors[i].getTitle().equals("ThreadStack.java")){
            		   activeWorkbenchWindow.getActivePage().closeEditors(new IEditorReference[]{editors[i]},false);
            		   fRemovedAnnotation = null;  // Clear the removed annotation so the test waits for the annotation to be removed
            		   break;
            	   }
               }
			}
	   };
	   // fRemovedAnnotation is used here to check if the editor has been found and closed successfully.  It is set to a annotation object, and will only be reset to null (causing the wait to occur) if the editor is closed.
       fRemovedAnnotation = new Annotation(true);
       DebugUIPlugin.getStandardDisplay().syncExec(closeEditor2);
       waitForAnnotationToBeRemoved();
       assertEquals("Instruction pointer count was incorrect", expectedIPCCounts[7], InstructionPointerManager.getDefault().getInstructionPointerCount());
       assertEquals("Editor mapping count was incorrect", expectedMappingCounts[7], InstructionPointerManager.getDefault().getEditorMappingCount());

       // Close all editors
	   Runnable closeAllEditors = new Runnable() {
           @Override
		public void run() {
               IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
               activeWorkbenchWindow.getActivePage().closeAllEditors(false);
			}
	   };
       fRemovedAnnotation = null;
       DebugUIPlugin.getStandardDisplay().syncExec(closeAllEditors);
       waitForAnnotationToBeRemoved();
       assertEquals("Instruction pointer count was incorrect", 0, InstructionPointerManager.getDefault().getInstructionPointerCount());
       assertEquals("Editor mapping count was incorrect", 0, InstructionPointerManager.getDefault().getEditorMappingCount());
	}

	protected MyPerspectiveListener getPerspectiveListener(){
		if (fPerspectiveListener == null){
			fPerspectiveListener = new MyPerspectiveListener();
			return fPerspectiveListener;
		}
		return fPerspectiveListener;
	}

	protected MyAnnotationListener getAnnotationListener(){
		if (fAnnotationListener == null){
			fAnnotationListener = new MyAnnotationListener();
			return fAnnotationListener;
		}
		return fAnnotationListener;
	}

	private IPartListener2 getPartListener(){
		if (fPartListener == null){
			fPartListener = new MyPartListener();
			return fPartListener;
		}
		return fPartListener;
	}

	private void waitForAnnotationToBeAdded() throws Exception{
		synchronized (fLock) {
		    if (fAddedAnnotation == null) {
		        fLock.wait(5000);
		    }
        }
		assertNotNull("Annotation was not added properly");

		// Synchronize with the UI thread so we know that the annotations have finished
		Runnable runner = new Runnable(){
			@Override
			public void run() {
				// Do nothing, just waiting for the UI thread to finish annotations
			}
		};
		DebugUIPlugin.getStandardDisplay().syncExec(runner);
	}

	private void waitForAnnotationToBeRemoved() throws Exception{
		synchronized (fLock) {
		    if (fRemovedAnnotation == null) {
		    	fLock.wait(5000);
		    }
        }
		assertNotNull("Annotation was not removed properly");

		// Synchronize with the UI thread so we know that the annotations have finished
		Runnable runner = new Runnable(){
			@Override
			public void run() {
				// Do nothing, just waiting for the UI thread to finish annotations
			}
		};
		DebugUIPlugin.getStandardDisplay().syncExec(runner);
	}

	class MyPerspectiveListener implements IPerspectiveListener2 {

		private String fTypeName = IInternalDebugCoreConstants.EMPTY_STRING;
		private String fTitle = IInternalDebugCoreConstants.EMPTY_STRING;

		@Override
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {}
		@Override
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
		 */
		@Override
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
            if (partRef.getTitle().equals(fTitle) && changeId == IWorkbenchPage.CHANGE_EDITOR_OPEN) {
            	IEditorPart editor = (IEditorPart) partRef.getPart(true);
            	if (editor instanceof ITextEditor){
        			IDocumentProvider docProvider = ((ITextEditor)editor).getDocumentProvider();
        			IEditorInput editorInput = editor.getEditorInput();
        	        // If there is no annotation model, there's nothing more to do
        	        IAnnotationModel annModel = docProvider.getAnnotationModel(editorInput);
        	        if (annModel == null) {
        	            fail("Could not get the annotation model");
        	        }
        	        annModel.addAnnotationModelListener(getAnnotationListener());
        	        fAnnotationModelsWithListeners.add(annModel);
        		} else {
        			fail("Editor was not a text editor");
        		}
            	partRef.getPage().addPartListener(getPartListener());
            }
            if (changeId == IWorkbenchPage.CHANGE_EDITOR_CLOSE) {
            	if (partRef.getPage().getEditorReferences().length == 0){
            		partRef.getPage().removePartListener(getPartListener());
            	}
            }
		}

		public void setTitle(String typeName){
			fTypeName = typeName;
			int index = typeName.lastIndexOf('.');
			if (index >= 0){
				fTitle = typeName.substring(index+1) + ".java";
			} else {
				fTitle = typeName + ".java";
			}
		}

		public String getTypeName(){
			return fTypeName;
		}
	}

	class MyPartListener implements IPartListener2{
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {}
		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {}
		@Override
		public void partHidden(IWorkbenchPartReference partRef) {}
		@Override
		public void partOpened(IWorkbenchPartReference partRef) {}
		@Override
		public void partVisible(IWorkbenchPartReference partRef) {}
		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {}
		@Override
		public void partClosed(IWorkbenchPartReference partRef) {}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
		 */
		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
			IEditorPart editor = (IEditorPart) partRef.getPart(true);
        	if (editor instanceof ITextEditor){
    			IDocumentProvider docProvider = ((ITextEditor)editor).getDocumentProvider();
    			IEditorInput editorInput = editor.getEditorInput();
    	        // If there is no annotation model, there's nothing more to do
    	        IAnnotationModel annModel = docProvider.getAnnotationModel(editorInput);
    	        if (annModel == null) {
    	            fail("Could not get the annotation model");
    	        }
    	        annModel.addAnnotationModelListener(getAnnotationListener());
    	        fAnnotationModelsWithListeners.add(annModel);
    		} else {
    			fail("Editor was not a text editor");
    		}
		}
	}

	class MyAnnotationListener implements IAnnotationModelListener, IAnnotationModelListenerExtension{

		@Override
		public void modelChanged(AnnotationModelEvent event) {
			Annotation[] annotations = event.getAddedAnnotations();
			for (int i = 0; i < annotations.length; i++) {
				if (annotations[i] instanceof InstructionPointerAnnotation){
					synchronized (fLock) {
						fAddedAnnotation = annotations[i];
						fLock.notifyAll();
						System.out.println("Annotation added to editor: " + fAddedAnnotation + " (" + this + ")" + event.getAnnotationModel());

					}
				}
			}
			annotations = event.getRemovedAnnotations();
			for (int i = 0; i < annotations.length; i++) {
				if (annotations[i] instanceof InstructionPointerAnnotation){
					synchronized (fLock) {
						fRemovedAnnotation = annotations[i];
						fLock.notifyAll();
					}
				}
			}
		}

		@Override
		public void modelChanged(IAnnotationModel model) {}
	}
}


