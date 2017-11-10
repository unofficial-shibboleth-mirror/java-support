/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.utilities.java.support.testing;

import javax.annotation.Nonnull;

/**
 * A collection of methods supporting test class execution.
 *
 * Methods named <code>isJavaV</code><i>x</i><code>OrLater</code>
 * are for use by tests needing to vary what they test, or the results
 * they expect, depending on the version of Java under which the test
 * is executing.
 *
 * For example, code which behaves differently depending on the presence
 * of cryptographic algorithms only available from some specific version of
 * the Java platform may be gated by such a method. <i>The code under test
 * should instead operate on the basis of the presence or absence of the
 * algorithm in question.</i>
 *
 * The API provided for Java version detection provides only
 * <code>isJavaV</code><i>x</i><code>OrLater</code> and not a generic
 * <code>getJavaVersion</code> to avoid any dependency in test code of
 * knowledge of the ordering of Java version numbers. Such an <code>int</code>
 * scheme is used internally, but not exposed except for self-tests.
 *
 * Methods named <code>has</code><i>X</i>, describing the presence of
 * specific features of the run-time environment, are to be preferred
 * over version-based methods. We don't have any of these yet, though.
 */
public class TestSupport {

    /** Constructor. */
    private TestSupport() {
    }

    /**
     * Extracts the major version number from a Java version string.
     *
     * This is not part of the API of the class, but is available as
     * a protected method for self-testing.
     *
     * @param versionStr version string to extract the version from
     * @return the major version number
     */
    protected static int getJavaVersion(@Nonnull final String versionStr) {
        final String components[] = versionStr.split("\\.");
        if (components[0].equals("1")) {
            // Handle 1.6, 1.7, 1.8
            return Integer.parseInt(components[1]);
        } else {
            // e.g., 9, 9.0.1
            return Integer.parseInt(components[0]);
        }
    }

    /**
     * Indicates whether the tests are running under Java 7 or a later
     * version.
     *
     * @return <code>true</code> if the runtime environment is Java 7 or later
     */
    public static boolean isJavaV7OrLater() {
        return getJavaVersion(System.getProperty("java.version")) >= 7;
    }

    /**
     * Indicates whether the tests are running under Java 8 or a later
     * version.
     *
     * @return <code>true</code> if the runtime environment is Java 8 or later
     */
    public static boolean isJavaV8OrLater() {
        return getJavaVersion(System.getProperty("java.version")) >= 8;
    }

    /**
     * Indicates whether the tests are running under Java 8 or a later
     * version.
     *
     * @return <code>true</code> if the runtime environment is Java 8 or later
     */
    public static boolean isJavaV9OrLater() {
        return getJavaVersion(System.getProperty("java.version")) >= 9;
    }

}
