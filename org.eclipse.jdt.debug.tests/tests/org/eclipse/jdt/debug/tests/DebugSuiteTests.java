/*******************************************************************************
 * Copyright (c) 2026 Carsten Hammer and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Carsten Hammer - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import org.junit.runner.Description;
import org.junit.runner.Request;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DebugSuiteTests extends TestCase {

	public void testJUnit4DescriptionIdentifiesSuiteClass() {
		Description description = Request.aClass(SampleSuite.class).getRunner().getDescription();
		assertEquals(SampleSuite.class.getName(), description.getDisplayName());
		assertEquals(SampleSuite.class.getName(), description.getClassName());
		assertEquals(2, description.testCount());
		assertEquals(1, description.getChildren().size());
		assertEquals(SampleTest.class.getName(), description.getChildren().get(0).getClassName());
	}

	public void testJUnit4DescriptionIdentifiesEmptySuiteClass() {
		Description description = Request.aClass(EmptySuite.class).getRunner().getDescription();
		assertEquals(EmptySuite.class.getName(), description.getDisplayName());
		assertEquals(EmptySuite.class.getName(), description.getClassName());
		assertTrue(description.getChildren().isEmpty());
	}

	public void testJUnit4DescriptionPreservesExplicitSuiteName() {
		Description description = Request.aClass(NamedSuite.class).getRunner().getDescription();
		assertEquals("Custom suite", description.getDisplayName());
		assertEquals(2, description.testCount());
		assertEquals(SampleTest.class.getName(), description.getChildren().get(0).getClassName());
	}

	public static class SampleSuite extends DebugSuite {
		public SampleSuite() {
			addTest(new TestSuite(SampleTest.class));
		}

		public static Test suite() {
			return new SampleSuite();
		}
	}

	public static class EmptySuite extends DebugSuite {
		public static Test suite() {
			return new EmptySuite();
		}
	}

	public static class NamedSuite extends SampleSuite {
		public NamedSuite() {
			setName("Custom suite");
		}

		public static Test suite() {
			return new NamedSuite();
		}
	}

	public static class SampleTest extends TestCase {
		public void testFirst() {
			// Only test discovery is exercised by DebugSuiteTests.
		}

		public void testSecond() {
			// Only test discovery is exercised by DebugSuiteTests.
		}
	}
}
