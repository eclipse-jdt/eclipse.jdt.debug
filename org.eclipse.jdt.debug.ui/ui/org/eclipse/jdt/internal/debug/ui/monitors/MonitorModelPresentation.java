package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
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

	public MonitorModelPresentation() {
		super();
	}
			
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
		}
	
		else if (item instanceof DeadLocksViewContentProvider.ContentMonitorWrapper) {
			return getMonitorText((IJavaObject)((DeadLocksViewContentProvider.ContentMonitorWrapper)item).fMonitor);
		}
		
		else if (item instanceof IJavaObject) {
			return getMonitorText((IJavaObject)item);
		}

		else if (item instanceof IJavaThread) {
			return getThreadText((IJavaThread)item);
		}			
		
		else if (item instanceof ThreadsViewContentProvider.MonitorWrapper) {
			return getMonitorWrapperText((ThreadsViewContentProvider.MonitorWrapper)item);
		}

		else if (item instanceof MonitorsViewContentProvider.ThreadWrapper) {
			return getThreadWrapperMonitorText((MonitorsViewContentProvider.ThreadWrapper)item);
		}

		else if (item instanceof ThreadsViewContentProvider.ThreadWrapper) {
			return getThreadWrapperThreadText((ThreadsViewContentProvider.ThreadWrapper)item);
		}						
		else {
			return "unsuported type";	
		}
	}

	/**
	 * Text for a ThreadWrapper in DeadLocksViewContentProvider
	 */
	protected String getThreadDeadLockText(DeadLocksViewContentProvider.ContentThreadWrapper thread){
		String res="";
		try{
			res += thread.fThread.getName();
		} catch(DebugException e){
		}
		
		if(thread.caughtInADeadLock){
			res += " (caught in the deadlock)";
		}
		return res;
	}

	/**
	 * Text for ThreadWrapper in DeadLocksViewContentProvider
	 */
	protected String getContentThreadWrapperText(DeadLocksViewContentProvider.ContentThreadWrapper ctw){
		String res="";
		try{
			res += ctw.fThread.getName();
		}
		catch (DebugException e) {
		}
		if(ctw.caughtInADeadLock){
			res += " (caught in a deadlock)";
		}
		return res;	
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
		String res="";
		res += monitor.monitor.toString();
		if(monitor.state==ThreadsViewContentProvider.MonitorWrapper.OWNED_MONITOR)
			res += " (owned)";
		else if(monitor.state==ThreadsViewContentProvider.MonitorWrapper.CONTENDED_MONITOR)
			res += " (contended)";
		return res;
	}

	/**
	 * Text for ThreadWrapper in ThreadsViewContentProvider
	 */
	protected String getThreadWrapperThreadText(ThreadsViewContentProvider.ThreadWrapper thread) {
		String res="";
		try{
			res += thread.thread.getName();
		} catch(Exception e){}
		
		if(thread.isCaughtInDeadlock)
			res += " (caught in a deadlock)";
		return res;
	}

	/**
	 * Text for ThreadWrapper in MonitorsViewContentProvider
	 */
	protected String getThreadWrapperMonitorText(MonitorsViewContentProvider.ThreadWrapper thread) {
		String res="";
		try{
			res += thread.thread.getName();
		} catch(Exception e){}
		
		if(thread.state==MonitorsViewContentProvider.ThreadWrapper.OWNING_THREAD)
			res += " (owning)";
		else if(thread.state==MonitorsViewContentProvider.ThreadWrapper.CONTENDING_THREAD)
			res += " (contending)";
		return res;
	}

	/**
	 * Text for threads
	 */	
	protected String getThreadText(IJavaThread thread){
		String res = "";
		try{
			res += thread.getName();
		} catch(Exception e){}
		return res;
	}

	/**
	 * Maps a Java element to an appropriate image.
	 * 
	 * @see IDebugModelPresentation#getImage(Object)
	 */
	public Image getImage(Object item) {
				
		if (item instanceof ThreadsViewContentProvider.ThreadWrapper) {
			return getThreadWrapperThreadImage((ThreadsViewContentProvider.ThreadWrapper)item);
		}						
		
		else if (item instanceof ThreadsViewContentProvider.MonitorWrapper) {
			return getMonitorWrapperThreadImage((ThreadsViewContentProvider.MonitorWrapper)item);
		}

		else if (item instanceof MonitorsViewContentProvider.ThreadWrapper) {
			return getThreadWrapperMonitorImage((MonitorsViewContentProvider.ThreadWrapper)item);
		}

		else if (item instanceof IJavaObject) {
			return getMonitorImage((IJavaObject)item);
		}
		
		else {
			return null;
		}
	}

	/**
	 * Image for a MonitorWrapper in ThreadsViewContentProvider
	 */
	private Image getMonitorWrapperThreadImage(ThreadsViewContentProvider.MonitorWrapper monitor){
		ImageDescriptor res=null;
		if(monitor.state==ThreadsViewContentProvider.MonitorWrapper.OWNED_MONITOR)
			res = JavaDebugImages.DESC_OBJ_MONITOR_OWNED;
		//else if(monitor.state==ThreadsViewContentProvider.MonitorWrapper.CONTENDED_MONITOR)
		else
			res = JavaDebugImages.DESC_OBJ_MONITOR_WAITED;
		return fDebugImageRegistry.get(res);
	}

	/**
	 * Image for a ThreadWrapper in ThreadsViewContentProvider
	 */
	private Image getThreadWrapperThreadImage(ThreadsViewContentProvider.ThreadWrapper thread){
		return fDebugImageRegistry.get(JavaDebugImages.DESC_OBJ_THREAD);
	}

	/**
	 * Image for ThreadWrapper in MonitorsViewContentProvider
	 */
	private Image getThreadWrapperMonitorImage(MonitorsViewContentProvider.ThreadWrapper thread){
		ImageDescriptor res=null;
		if(thread.state==MonitorsViewContentProvider.ThreadWrapper.OWNING_THREAD)
			res = JavaDebugImages.DESC_OBJ_THREAD_OWNING;
		//else if(monitor.state==ThreadsViewContentProvider.MonitorWrapper.CONTENDED_MONITOR)
		else
			res = JavaDebugImages.DESC_OBJ_THREAD_WAITING;
		return fDebugImageRegistry.get(res);
	}

	/**
	 * Image for monitors
	 */
	private Image getMonitorImage(IJavaObject monitor){
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
}
