/*******************************************************************************
 * Copyright (c) 2006, 2022 IBM Corporation and others.
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
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.environments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.launching.EEVMInstall;
import org.eclipse.jdt.internal.launching.EEVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstall3;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.CompatibleEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentAnalyzerDelegate;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
/**
 * Environment analyzer for standard execution environments.
 *
 * @since 3.3
 */
public class ExecutionEnvironmentAnalyzer implements IExecutionEnvironmentAnalyzerDelegate {

	// XXX: Note that this string is not yet standardized by OSGi, see http://wiki.osgi.org/wiki/Execution_Environment

	private static final String JavaSE_18 = "JavaSE-18"; //$NON-NLS-1$
	private static final String JavaSE_17 = "JavaSE-17"; //$NON-NLS-1$
	private static final String JavaSE_16 = "JavaSE-16"; //$NON-NLS-1$
	private static final String JavaSE_15 = "JavaSE-15"; //$NON-NLS-1$
	private static final String JavaSE_14 = "JavaSE-14"; //$NON-NLS-1$
	private static final String JavaSE_13 = "JavaSE-13"; //$NON-NLS-1$
	private static final String JavaSE_12 = "JavaSE-12"; //$NON-NLS-1$
	private static final String JavaSE_11 = "JavaSE-11"; //$NON-NLS-1$
	private static final String JavaSE_10_Plus = "JavaSE-10+"; //$NON-NLS-1$
	private static final String JavaSE_10 = "JavaSE-10"; //$NON-NLS-1$
	static final String JavaSE_9 = "JavaSE-9"; //$NON-NLS-1$
	private static final String JavaSE_1_8 = "JavaSE-1.8"; //$NON-NLS-1$

	private static final String JavaSE_1_7 = "JavaSE-1.7"; //$NON-NLS-1$
	private static final String JavaSE_1_6 = "JavaSE-1.6"; //$NON-NLS-1$
	private static final String J2SE_1_5 = "J2SE-1.5"; //$NON-NLS-1$
	private static final String J2SE_1_4 = "J2SE-1.4"; //$NON-NLS-1$
	private static final String J2SE_1_3 = "J2SE-1.3"; //$NON-NLS-1$
	private static final String J2SE_1_2 = "J2SE-1.2"; //$NON-NLS-1$
	private static final String JRE_1_1 = "JRE-1.1"; //$NON-NLS-1$

	private static final String CDC_FOUNDATION_1_1 = "CDC-1.1/Foundation-1.1"; //$NON-NLS-1$
	private static final String CDC_FOUNDATION_1_0 = "CDC-1.0/Foundation-1.0"; //$NON-NLS-1$

	private static final String OSGI_MINIMUM_1_0 = "OSGi/Minimum-1.0"; //$NON-NLS-1$
	private static final String OSGI_MINIMUM_1_1 = "OSGi/Minimum-1.1"; //$NON-NLS-1$
	private static final String OSGI_MINIMUM_1_2 = "OSGi/Minimum-1.2"; //$NON-NLS-1$

	private static final String JAVA_SPEC_VERSION = "java.specification.version"; //$NON-NLS-1$
	private static final String JAVA_SPEC_NAME = "java.specification.name"; //$NON-NLS-1$
	private static final String JAVA_VERSION = "java.version"; //$NON-NLS-1$

	private static final String[] VM_PROPERTIES = {JAVA_SPEC_NAME, JAVA_SPEC_VERSION, JAVA_VERSION};
	private static final String FOUNDATION = "foundation"; //$NON-NLS-1$
	private static final Map<String, String[]> mappings = new HashMap<>();

	static {
		// table where the key is the EE and the value is an array of EEs that it is a super-set of
		mappings.put(CDC_FOUNDATION_1_0, new String[] {OSGI_MINIMUM_1_0});
		mappings.put(CDC_FOUNDATION_1_1, new String[] {CDC_FOUNDATION_1_0, OSGI_MINIMUM_1_2});
		mappings.put(OSGI_MINIMUM_1_1, new String[] {OSGI_MINIMUM_1_0});
		mappings.put(OSGI_MINIMUM_1_2, new String[] {OSGI_MINIMUM_1_1});
		mappings.put(J2SE_1_2, new String[] {JRE_1_1});
		mappings.put(J2SE_1_3, new String[] {J2SE_1_2, CDC_FOUNDATION_1_0, OSGI_MINIMUM_1_0});
		mappings.put(J2SE_1_4, new String[] {J2SE_1_3, CDC_FOUNDATION_1_1, OSGI_MINIMUM_1_2});
		mappings.put(J2SE_1_5, new String[] {J2SE_1_4});
		mappings.put(JavaSE_1_6, new String[] {J2SE_1_5});
		mappings.put(JavaSE_1_7, new String[] {JavaSE_1_6});
		mappings.put(JavaSE_1_8, new String[] { JavaSE_1_7 });
		mappings.put(JavaSE_9, new String[] { JavaSE_1_8 });
		mappings.put(JavaSE_10, new String[] { JavaSE_9 });
		mappings.put(JavaSE_10_Plus, new String[] { JavaSE_18 });
		mappings.put(JavaSE_11, new String[] { JavaSE_10 });
		mappings.put(JavaSE_12, new String[] { JavaSE_11 });
		mappings.put(JavaSE_13, new String[] { JavaSE_12 });
		mappings.put(JavaSE_14, new String[] { JavaSE_13 });
		mappings.put(JavaSE_15, new String[] { JavaSE_14 });
		mappings.put(JavaSE_16, new String[] { JavaSE_15 });
		mappings.put(JavaSE_17, new String[] { JavaSE_16 });
		mappings.put(JavaSE_18, new String[] { JavaSE_17 });
	}
	@Override
	public CompatibleEnvironment[] analyze(IVMInstall vm, IProgressMonitor monitor) throws CoreException {
		ArrayList<CompatibleEnvironment> result = new ArrayList<>();
		if (!(vm instanceof IVMInstall2)) {
			return new CompatibleEnvironment[0];
		}
		IVMInstall2 vm2 = (IVMInstall2) vm;
		List<String> types = null;
		if (EEVMType.ID_EE_VM_TYPE.equals(vm.getVMInstallType().getId())) {
			String eeId = ((EEVMInstall)vm).getAttribute(EEVMInstall.ATTR_EXECUTION_ENVIRONMENT_ID);
			if (eeId != null) {
				types = getTypes(eeId);
			}
		}
		if (types == null) {
			String javaVersion = vm2.getJavaVersion();
			if (javaVersion == null) {
				// We have a contributed VM type. Check to see if its a foundation VM, if we can.
				if ((vm instanceof IVMInstall3) && isFoundation1_0((IVMInstall3) vm)) {
					types = getTypes(CDC_FOUNDATION_1_0);
				} else if ((vm instanceof IVMInstall3) && isFoundation1_1((IVMInstall3) vm)) {
					types = getTypes(CDC_FOUNDATION_1_1);
				}
			} else {
				if (javaVersion.startsWith("18")) { //$NON-NLS-1$
					types = getTypes(JavaSE_18);
				} else if (javaVersion.startsWith("17")) { //$NON-NLS-1$
					types = getTypes(JavaSE_17);
				} else if (javaVersion.startsWith("16")) { //$NON-NLS-1$
					types = getTypes(JavaSE_16);
				} else if (javaVersion.startsWith("15")) { //$NON-NLS-1$
					types = getTypes(JavaSE_15);
				} else if (javaVersion.startsWith("14")) { //$NON-NLS-1$
					types = getTypes(JavaSE_14);
				} else if (javaVersion.startsWith("13")) { //$NON-NLS-1$
					types = getTypes(JavaSE_13);
				} else if (javaVersion.startsWith("12")) { //$NON-NLS-1$
					types = getTypes(JavaSE_12);
				} else if (javaVersion.startsWith("11")) { //$NON-NLS-1$
					types = getTypes(JavaSE_11);
				} else if (javaVersion.startsWith("10")) { //$NON-NLS-1$
					types = getTypes(JavaSE_10);
				} else if (javaVersion.startsWith("9")) { //$NON-NLS-1$
					types = getTypes(JavaSE_9);
				} else if (javaVersion.startsWith("1.8")) { //$NON-NLS-1$
					types = getTypes(JavaSE_1_8);
				} else if (javaVersion.startsWith("1.7")) { //$NON-NLS-1$
					types = getTypes(JavaSE_1_7);
				} else if (javaVersion.startsWith("1.6")) { //$NON-NLS-1$
					types = getTypes(JavaSE_1_6);
				} else if (javaVersion.startsWith("1.5")) { //$NON-NLS-1$
					types = getTypes(J2SE_1_5);
				} else if (javaVersion.startsWith("1.4")) { //$NON-NLS-1$
					types = getTypes(J2SE_1_4);
				} else if (javaVersion.startsWith("1.3")) { //$NON-NLS-1$
					types = getTypes(J2SE_1_3);
				} else if (javaVersion.startsWith("1.2")) { //$NON-NLS-1$
					types = getTypes(J2SE_1_2);
				} else if (javaVersion.startsWith("1.1")) { //$NON-NLS-1$
					if ((vm instanceof IVMInstall3) && isFoundation1_1((IVMInstall3) vm)) {
						types = getTypes(CDC_FOUNDATION_1_1);
					} else {
						types = getTypes(JRE_1_1);
					}
				} else if (javaVersion.startsWith("1.0")) { //$NON-NLS-1$
					if ((vm instanceof IVMInstall3) && isFoundation1_0((IVMInstall3) vm)) {
						types = getTypes(CDC_FOUNDATION_1_0);
					}
				} else if (javaVersion.startsWith("1") && javaVersion.length() >= 2 && javaVersion.charAt(1) != '.') { //$NON-NLS-1$
					// At the moment only caters to versions 11 to 19.
					types = getTypes(JavaSE_10_Plus);
				}
			}
		}

		if (types != null) {
			for (int i=0; i < types.size(); i++) {
				addEnvironment(result, types.get(i), i ==0);
			}
		}
		return result.toArray(new CompatibleEnvironment[result.size()]);
	}

	/*
	 * Check a couple of known system properties for the word "foundation".
	 */
	private boolean isFoundation(Map<String, String> properties) {
		for (int i=0; i < VM_PROPERTIES.length; i++) {
			String value = properties.get(VM_PROPERTIES[i]);
			if (value == null) {
				continue;
			}
			for (StringTokenizer tokenizer = new StringTokenizer(value); tokenizer.hasMoreTokens(); ) {
				if (FOUNDATION.equalsIgnoreCase(tokenizer.nextToken())) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isFoundation1_0(IVMInstall3 vm) throws CoreException {
		Map<String, String> map = vm.evaluateSystemProperties(VM_PROPERTIES, null);
		return isFoundation(map) ? "1.0".equals(map.get(JAVA_SPEC_VERSION)) : false; //$NON-NLS-1$
	}

	private boolean isFoundation1_1(IVMInstall3 vm) throws CoreException {
		Map<String, String> map = vm.evaluateSystemProperties(VM_PROPERTIES, null);
		return isFoundation(map) ? "1.1".equals(map.get(JAVA_SPEC_VERSION)) : false; //$NON-NLS-1$
	}

	private void addEnvironment(ArrayList<CompatibleEnvironment> result, String id, boolean strict) {
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment env = manager.getEnvironment(id);
		if (env != null) {
			result.add(new CompatibleEnvironment(env, strict));
		}
	}

	// first entry in the list is the perfect match
	private List<String> getTypes(String type) {
		List<String> result = new ArrayList<>();
		result.add(type);
		String[] values = mappings.get(type);
		if (values != null) {
			for (int i=0; i<values.length; i++) {
				result.addAll(getTypes(values[i]));
			}
		}
		return result;
	}

}
