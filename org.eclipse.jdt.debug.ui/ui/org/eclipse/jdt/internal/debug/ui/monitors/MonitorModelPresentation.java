package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIImageDescriptor;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;

/**
 * @see IDebugModelPresentation
 */
public class MonitorModelPresentation extends LabelProvider implements IDebugModelPresentation {

	protected ImageDescriptorRegistry fDebugImageRegistry= JDIDebugUIPlugin.getImageDescriptorRegistry();
			
	/**
	 * @see IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
	}
			
	/**
	 * @see IDebugModelPresentation#getText(Object)
	 */
	public String getText(Object item) {
			
		if (item instanceof DeadLocksViewContentProvider.ContentThreadWrapper) {
			return getThreadDeadLockText((DeadLocksViewContentProvider.ContentThreadWrapper)item);
		} else if (item instanceof DeadLocksViewContentProvider.ContentMonitorWrapper) {
			StringBuffer res= new StringBuffer();
			res.append(((DeadLocksViewContentProvider.ContentMonitorWrapper)item).fMonitor.toString());
			res.append(MonitorMessages.getString("MonitorModelPresentation._owned_by..._1")); //$NON-NLS-1$
			return res.toString();
		} else if (item instanceof IJavaObject) {
			return getMonitorText((IJavaObject)item);
		} else if (item instanceof IJavaThread) {
			return getThreadText((IJavaThread)item);
		} else if (item instanceof ThreadsViewContentProvider.MonitorWrapper) {
			return getMonitorWrapperText((ThreadsViewContentProvider.MonitorWrapper)item);
		} else if (item instanceof MonitorsViewContentProvider.ThreadWrapper) {
			return getThreadWrapperMonitorText((MonitorsViewContentProvider.ThreadWrapper)item);
		} else if (item instanceof ThreadsViewContentProvider.ThreadWrapper) {
			return getThreadWrapperThreadText((ThreadsViewContentProvider.ThreadWrapper)item);
		} else {
			return MonitorMessages.getString("MonitorModelPresentation.unsuported_type_1");	 //$NON-NLS-1$
		}
	}

	/**
	 * Text for a ThreadWrapper in DeadLocksViewContentProvider
	 */
	protected String getThreadDeadLockText(DeadLocksViewContentProvider.ContentThreadWrapper thread){
		StringBuffer res= new StringBuffer();
		try{
			res.append(thread.fThread.getName());
		} catch(DebugException e){
		}
		
		if(thread.caughtInADeadLock){
			res.append(MonitorMessages.getString("MonitorModelPresentation._(caught_in_the_deadlock)_2")); //$NON-NLS-1$
		} else {
			res.append(MonitorMessages.getString("MonitorModelPresentation._waiting_for..._2")); //$NON-NLS-1$
		}
		return res.toString();
	}

	/**
	 * Text for monitors
	 */
	protected String getMonitorText(IJavaObject monitor) {
		return monitor.toString();
	}

	/**
	 * Text for MonitorWrapper in ThreadsViewContentProvider
	 */
	protected String getMonitorWrapperText(ThreadsViewContentProvider.MonitorWrapper monitor) {
		StringBuffer res= new StringBuffer(monitor.monitor.toString());
		if(monitor.state==ThreadsViewContentProvider.MonitorWrapper.OWNED_MONITOR) {
			res.append(MonitorMessages.getString("MonitorModelPresentation._(owned)_4")); //$NON-NLS-1$
		} else if(monitor.state==ThreadsViewContentProvider.MonitorWrapper.CONTENDED_MONITOR) {
			res.append(MonitorMessages.getString("MonitorModelPresentation._(contended)_5")); //$NON-NLS-1$
		}
		return res.toString();
	}

	/**
	 * Text for ThreadWrapper in ThreadsViewContentProvider
	 */
	protected String getThreadWrapperThreadText(ThreadsViewContentProvider.ThreadWrapper thread) {
		StringBuffer res= new StringBuffer();
		try{
			res.append(thread.thread.getName());
		} catch(DebugException e){
		}
		
		if(thread.isCaughtInDeadlock) {
			res.append(MonitorMessages.getString("MonitorModelPresentation._(caught_in_a_deadlock)_6")); //$NON-NLS-1$
		}
		return res.toString();
	}

	/**
	 * Text for ThreadWrapper in MonitorsViewContentProvider
	 */
	protected String getThreadWrapperMonitorText(MonitorsViewContentProvider.ThreadWrapper thread) {
		StringBuffer res= new StringBuffer();
		try{
			res.append(thread.thread.getName());
		} catch(DebugException e){
		}
		
		if(thread.state == MonitorsViewContentProvider.ThreadWrapper.OWNING_THREAD) {
			res.append(MonitorMessages.getString("MonitorModelPresentation._(owning)_7")); //$NON-NLS-1$
		} else if(thread.state == MonitorsViewContentProvider.ThreadWrapper.IN_CONTENTION_FOR_MONITOR) {
			res.append(MonitorMessages.getString("MonitorModelPresentation._(contending)_8")); //$NON-NLS-1$
		}
		return res.toString();
	}

	/**
	 * Text for threads
	 */	
	protected String getThreadText(IJavaThread thread){
		StringBuffer res = new StringBuffer();
		try{
			res.append(thread.getName());
		} catch(DebugException e){
		}
		return res.toString();
	}

	/**
	 * Maps an element to an appropriate image.
	 * 
	 * @see IDebugModelPresentation#getImage(Object)
	 */
	public Image getImage(Object item) {
				
		if (item instanceof ThreadsViewContentProvider.ThreadWrapper) {
			return getThreadWrapperThreadImage(((ThreadsViewContentProvider.ThreadWrapper)item).thread);
		} else if (item instanceof ThreadsViewContentProvider.MonitorWrapper) {
			ThreadsViewContentProvider.MonitorWrapper monitorWrapper= (ThreadsViewContentProvider.MonitorWrapper)item;
			JDIImageDescriptor descriptor= null;
			int flags= computeMonitorAdornmentFlags(monitorWrapper);
			descriptor= new JDIImageDescriptor(JavaDebugImages.DESC_OBJ_MONITOR, flags);
			return fDebugImageRegistry.get(descriptor);
		} else if (item instanceof MonitorsViewContentProvider.ThreadWrapper) {
			MonitorsViewContentProvider.ThreadWrapper threadWrapper= (MonitorsViewContentProvider.ThreadWrapper)item;
			JDIImageDescriptor descriptor= null;
			int flags= computeThreadAdornmentFlags(threadWrapper);
			if (threadWrapper.thread.isSuspended()) {
				descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED), flags);
			} else {
				descriptor= new JDIImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING), flags);
			}
			return fDebugImageRegistry.get(descriptor);
		} else if (item instanceof IJavaObject) {
			return getMonitorImage();
		} else if (item instanceof DeadLocksViewContentProvider.ContentMonitorWrapper) {
			return getMonitorImage();
			
		} else if (item instanceof DeadLocksViewContentProvider.ContentThreadWrapper ) {
			return getThreadWrapperThreadImage(((DeadLocksViewContentProvider.ContentThreadWrapper)item).fThread);
		}
		
		return null;
	}

	/**
	 * Image for a ThreadWrapper in ThreadsViewContentProvider
	 */
	private Image getThreadWrapperThreadImage(IJavaThread thread){
		ImageDescriptor descriptor= null;
		if (thread.isSuspended()) {
			descriptor= DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED);
		} else {
			descriptor= DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
		}
		return fDebugImageRegistry.get(descriptor);
	}

	/**
	 * Image for monitors
	 */
	private Image getMonitorImage(){
		return fDebugImageRegistry.get(JavaDebugImages.DESC_OBJ_MONITOR);
	}

	/**
	 * @see IDebugModelPresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput(Object item) {
		return null;
	}

	/**
	 * @see IDebugModelPresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId(IEditorInput input, Object inputObject) {
		return null;
	}

	/**
	 * @see IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute(String id, Object value) {
	}
	
	/**
	 * Returns the adornment flags for the monitor.
	 * These flags are used to render appropriate overlay
	 * icons for the monitor.
	 */
	private int computeMonitorAdornmentFlags(ThreadsViewContentProvider.MonitorWrapper wrapper)  {
		int flags= 0;
		
		if (wrapper.state == ThreadsViewContentProvider.MonitorWrapper.CONTENDED_MONITOR) {
			flags |= JDIImageDescriptor.CONTENTED_MONITOR;
		}
		if (wrapper.state == ThreadsViewContentProvider.MonitorWrapper.OWNED_MONITOR) {
			flags |= JDIImageDescriptor.OWNED_MONITOR;
		}
		return flags;
	}
	
	/**
	 * Returns the adornment flags for the thread.
	 * These flags are used to render appropriate overlay
	 * icons for the thread.
	 */
	private int computeThreadAdornmentFlags(MonitorsViewContentProvider.ThreadWrapper wrapper)  {
		int flags= 0;
		
		if (wrapper.state == MonitorsViewContentProvider.ThreadWrapper.IN_CONTENTION_FOR_MONITOR) {
			flags |= JDIImageDescriptor.IN_CONTENTION_FOR_MONITOR;
		}
		if (wrapper.state == MonitorsViewContentProvider.ThreadWrapper.OWNING_THREAD) {
			flags |= JDIImageDescriptor.OWNS_MONITOR;
		}
		return flags;
	}
}
