/*******************************************************************************
 * Copyright (c) 2022 Andrey Loskutov (loskutov@gmx.de) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - extracted interface
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.util.List;

/**
 * Internal interface to access process temp files
 */
interface IProcessTempFileCreator {

	/**
	 * Necessary files that were created for starting the process. They can be deleted once the process is terminated
	 *
	 * @return created files
	 */
	List<File> getProcessTempFiles();

}
