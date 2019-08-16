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
     * The version string given by the <code>java.version</code>
     * property has changed format multiple times in its history.
     * This method acts as a parser for such strings. It is only
     * required to handle versions of Java from the current platform
     * baseline onwards (and thus their particular version string
     * quirks) but in practice may support previous versions.
     *
     * For Java 6, 7 and 8, the version string has a format
     * like "1.X.0_123" where X is the version of Java. This means
     * that there will always be at least two components separated
     * by periods, and that the first such component will always
     * be "1".
     *
     * Examples of versions strings observed in practice:
     *
     * <ul>
     * <li><code>1.6.0_65-b14-468</code>
     * <li><code>1.7.0_51</code>
     * <li><code>1.8.0_144</code>
     * </ul>
     *
     * For Java 9, the version string format is described in
     * <a href="http://openjdk.java.net/jeps/223">JEP 223</a>.
     *
     * For Java 10, the version string format is described in
     * <a href="http://openjdk.java.net/jeps/322">JEP 322</a>
     *
     * Both JEP 223 and JEP 322 allow for an arbitrary number of numeric
     * components separated by periods, but the possibility exists for this
     * to be a <em>single</em> component. Following this a number of other
     * components delimited by either <code>+</code> or <code>-</code> may
     * appear. This means that version strings like "10+43" are possible, and
     * have been observed.
     *
     * @param versionStr version string to extract the version from
     * @return the major version number
     *
     * @see <a href="http://openjdk.java.net/jeps/223">JEP 223</a>
     * @see <a href="http://openjdk.java.net/jeps/322">JEP 322</a>
     */
    protected static int getJavaVersion(@Nonnull final String versionStr) {
        // Split into components delimited by '.', '+' and '-'.
        // This covers both the historic, JEP 223 and JEP 332 schemes.
        final String components[] = versionStr.split("\\.|\\+|-");
        if (components[0].equals("1")) {
            // Handle 1.6, 1.7, 1.8
            return Integer.parseInt(components[1]);
        }
        // e.g., 9, 9.0.1, 10+43
        return Integer.parseInt(components[0]);
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
     * Indicates whether the tests are running under Java 9 or a later
     * version.
     *
     * @return <code>true</code> if the runtime environment is Java 9 or later
     */
    public static boolean isJavaV9OrLater() {
        return getJavaVersion(System.getProperty("java.version")) >= 9;
    }

    /**
     * Indicates whether the tests are running under Java 11 or a later
     * version.
     *
     * @return <code>true</code> if the runtime environment is Java 11 or later
     */
    public static boolean isJavaV11OrLater() {
        return getJavaVersion(System.getProperty("java.version")) >= 11;
    }

}
