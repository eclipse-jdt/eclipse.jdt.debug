package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A preference store that presents the state of the properties
 * of a Java breakpoint. Default settings are not supported.
 */
public class JavaBreakpointPreferenceStore implements IPreferenceStore {
	
	protected final static String ENABLED= "ENABLED"; //$NON-NLS-1$
	protected final static String HIT_COUNT= "HIT_COUNT"; //$NON-NLS-1$
	protected final static String HIT_COUNT_ENABLED= "HIT_COUNT_ENABLED"; //$NON-NLS-1$
	protected final static String SUSPEND_POLICY= "SUSPEND_POLICY"; //$NON-NLS-1$
	protected final static String METHOD_EXIT= "METHOD_EXIT"; //$NON-NLS-1$
	protected final static String METHOD_ENTRY= "METHOD_ENTRY"; //$NON-NLS-1$
	protected final static String CAUGHT= "CAUGHT"; //$NON-NLS-1$
	protected final static String UNCAUGHT= "UNCAUGHT"; //$NON-NLS-1$
	protected final static String ACCESS= "ACCESS"; //$NON-NLS-1$
	protected final static String MODIFICATION= "MODIFICATION"; //$NON-NLS-1$
	protected final static String THREAD_FILTER= "THREAD_FILTER"; //$NON-NLS-1$
	protected final static String EXCEPTION_FILTER= "EXCEPTION_FILTER"; //$NON-NLS-1$
	protected final static String CONDITION= "CONDITION"; //$NON-NLS-1$
	protected final static String CONDITION_ENABLED= "CONDITION_ENABLED"; //$NON-NLS-1$
	protected final static String CONDITION_SUSPEND_ON_TRUE= "CONDITION_SUSPEND_ON_TRUE"; //$NON-NLS-1$
	protected final static String CONDITION_SUSPEND_ON_CHANGES= "CONDITION_SUSPEND_ON_CHANGES"; //$NON-NLS-1$
	protected final static String INSTANCE_FILTER= "INSTANCE_FILTER"; //$NON-NLS-1$

	protected HashMap fProperties;
	private boolean fIsDirty= false; 
	private ListenerList fListeners;
	
	protected JavaBreakpointPreferenceStore() {
		fProperties= new HashMap(9);
		fListeners= new ListenerList();
	}
	/**
	 * @see IPreferenceStore#addPropertyChangeListener(IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.add(listener);
	}

	/**
	 * @see IPreferenceStore#contains(String)
	 */
	public boolean contains(String name) {
		return fProperties.containsKey(name);
	}

	/**
	 * @see IPreferenceStore#firePropertyChangeEvent(String, Object, Object)
	 */
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		Object[] listeners = fListeners.getListeners();
		// Do we need to fire an event.
		if (listeners.length > 0 && (oldValue == null || !oldValue.equals(newValue))) {
			PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
			for (int i = 0; i < listeners.length; ++i) {
				IPropertyChangeListener l = (IPropertyChangeListener) listeners[i];
				l.propertyChange(pe);
			}
		}
	}

	/**
	 * @see IPreferenceStore#getBoolean(String)
	 */
	public boolean getBoolean(String name) {
		Object b= fProperties.get(name);
		if (b instanceof Boolean) {
			return ((Boolean)b).booleanValue();
		}
		return false;
	}

	/**
	 * @see IPreferenceStore#getDefaultBoolean(String)
	 */
	public boolean getDefaultBoolean(String name) {
		return false;
	}

	/**
	 * @see IPreferenceStore#getDefaultDouble(String)
	 */
	public double getDefaultDouble(String name) {
		return 0;
	}

	/**
	 * @see IPreferenceStore#getDefaultFloat(String)
	 */
	public float getDefaultFloat(String name) {
		return 0;
	}

	/**
	 * @see IPreferenceStore#getDefaultInt(String)
	 */
	public int getDefaultInt(String name) {
		return 0;
	}

	/**
	 * @see IPreferenceStore#getDefaultLong(String)
	 */
	public long getDefaultLong(String name) {
		return 0;
	}

	/**
	 * @see IPreferenceStore#getDefaultString(String)
	 */
	public String getDefaultString(String name) {
		return null;
	}

	/**
	 * @see IPreferenceStore#getDouble(String)
	 */
	public double getDouble(String name) {
		return 0;
	}

	/**
	 * @see IPreferenceStore#getFloat(String)
	 */
	public float getFloat(String name) {
		return 0;
	}

	/**
	 * @see IPreferenceStore#getInt(String)
	 */
	public int getInt(String name) {
		Object i= fProperties.get(name);
		if (i instanceof Integer) {
			return ((Integer)i).intValue();
		}
		return 1;
	}

	/**
	 * @see IPreferenceStore#getLong(String)
	 */
	public long getLong(String name) {
		return 0;
	}

	/**
	 * @see IPreferenceStore#getString(String)
	 */
	public String getString(String name) {
		Object str= fProperties.get(name);
		if (str instanceof String) {
			return (String)str;
		}
		return null;
	}

	/**
	 * @see IPreferenceStore#isDefault(String)
	 */
	public boolean isDefault(String name) {
		return false;
	}

	/**
	 * @see IPreferenceStore#needsSaving()
	 */
	public boolean needsSaving() {
		return fIsDirty;
	}

	/**
	 * @see IPreferenceStore#putValue(String, String)
	 */
	public void putValue(String name, String newValue) {
		Object oldValue= fProperties.get(name);
		if (oldValue == null || !oldValue.equals(newValue)) {
			fProperties.put(name, newValue);
			setDirty(true);
		}
	}

	/**
	 * @see IPreferenceStore#removePropertyChangeListener(IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * @see IPreferenceStore#setDefault(String, boolean)
	 */
	public void setDefault(String name, boolean value) {
	}

	/**
	 * @see IPreferenceStore#setDefault(String, double)
	 */
	public void setDefault(String name, double value) {
	}

	/**
	 * @see IPreferenceStore#setDefault(String, float)
	 */
	public void setDefault(String name, float value) {
	}

	/**
	 * @see IPreferenceStore#setDefault(String, int)
	 */
	public void setDefault(String name, int value) {
	}

	/**
	 * @see IPreferenceStore#setDefault(String, long)
	 */
	public void setDefault(String name, long value) {
	}

	/**
	 * @see IPreferenceStore#setDefault(String, String)
	 */
	public void setDefault(String name, String defaultObject) {
	}

	/**
	 * @see IPreferenceStore#setToDefault(String)
	 */
	public void setToDefault(String name) {
	}

	/**
	 * @see IPreferenceStore#setValue(String, boolean)
	 */
	public void setValue(String name, boolean newValue) {
		boolean oldValue= getBoolean(name);
		if (oldValue != newValue) {
			fProperties.put(name,new Boolean(newValue));
			setDirty(true);
			firePropertyChangeEvent(name, new Boolean(oldValue), new Boolean(newValue));
		}
	}

	/**
	 * @see IPreferenceStore#setValue(String, double)
	 */
	public void setValue(String name, double value) {
		//breakpoints do not currently have any double properties
	}

	/**
	 * @see IPreferenceStore#setValue(String, float)
	 */
	public void setValue(String name, float value) {
		//breakpoints do not currently have any float properties
	}

	/**
	 * @see IPreferenceStore#setValue(String, int)
	 */
	public void setValue(String name, int newValue) {
		int oldValue= getInt(name);
		if (oldValue != newValue) {
			fProperties.put(name,new Integer(newValue));
			setDirty(true);
			firePropertyChangeEvent(name, new Integer(oldValue), new Integer(newValue));
		}
	}

	/**
	 * @see IPreferenceStore#setValue(String, long)
	 */
	public void setValue(String name, long value) {
		//breakpoints do not currently have any long properties
	}

	/**
	 * @see IPreferenceStore#setValue(String, String)
	 */
	public void setValue(String name, String newValue) {
		Object oldValue= fProperties.get(name);
		if (oldValue == null || !oldValue.equals(newValue)) {
			fProperties.put(name, newValue);
			setDirty(true);
			firePropertyChangeEvent(name, oldValue, newValue);
		}
	}

	protected void setDirty(boolean isDirty) {
		fIsDirty = isDirty;
	}
}