package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
/**
 * Model object that represents a single entry in a filter table.
 */
public class Filter {

	private String fName;
	private boolean fChecked;

	public Filter(String name, boolean checked) {
		setName(name);
		setChecked(checked);
	}

	public String getName() {
		return fName;
	}

	public void setName(String name) {
		fName = name;
	}

	public boolean isChecked() {
		return fChecked;
	}

	public void setChecked(boolean checked) {
		fChecked = checked;
	}

	public boolean equals(Object o) {
		if (o instanceof Filter) {
			Filter other = (Filter) o;
			if (getName().equals(other.getName())) {
				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		return getName().hashCode();
	}
}