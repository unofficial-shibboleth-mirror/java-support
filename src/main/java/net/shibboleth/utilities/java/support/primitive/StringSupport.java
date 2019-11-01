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

package net.shibboleth.utilities.java.support.primitive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

/** String utility methods. */
public final class StringSupport {

    /** Constructor. */
    private StringSupport() {
    }

    /**
     * Reads an input stream into a string. The provided stream is <strong>not</strong> closed.
     * 
     * @param input the input stream to read
     * @param decoder character decoder to use, if null, system default character set is used
     * 
     * @return the string read from the stream
     * 
     * @throws IOException thrown if there is a problem reading from the stream and decoding it
     */
    @Nonnull public static String inputStreamToString(@Nonnull final InputStream input,
            @Nullable final CharsetDecoder decoder) throws IOException {
        CharsetDecoder charsetDecoder = decoder;
        if (decoder == null) {
            charsetDecoder = Charset.defaultCharset().newDecoder();
        }

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(input, charsetDecoder))) {
            final StringBuilder stringBuffer = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                stringBuffer.append(line).append("\n");
                line = reader.readLine();
            }
            return stringBuffer.toString();
        }
    }

    /**
     * Converts a List of objects into a single string, with values separated by a specified delimiter.
     * 
     * @param values list of objects
     * @param delimiter the delimiter used between values
     * 
     * @return delimited string of values
     */
    @Nonnull public static String listToStringValue(@Nonnull final List<?> values, @Nonnull final String delimiter) {
        Constraint.isNotNull(values, "List of values can not be null");
        Constraint.isNotNull(delimiter, "String delimiter may not be null");

        final StringBuilder stringValue = new StringBuilder();
        final Iterator<?> valueItr = values.iterator();
        while (valueItr.hasNext()) {
            stringValue.append(valueItr.next());
            if (valueItr.hasNext()) {
                stringValue.append(delimiter);
            }
        }

        return stringValue.toString();
    }

    /**
     * Converts a delimited string into a list. We cannot use an ungarnished tokenizer since it doesn't add an empty
     * String if the end of the input String was the delimiter. Hence we have to explicitly check.
     * 
     * @param string the string to be split into a list
     * @param delimiter the delimiter between values. This string may contain multiple delimiter characters, as allowed
     *            by {@link StringTokenizer}
     * 
     * @return the list of values or an empty list if the given string is empty
     */
    @Nonnull public static List<String> stringToList(@Nonnull final String string, @Nonnull final String delimiter) {
        Constraint.isNotNull(string, "String data can not be null");
        Constraint.isNotNull(delimiter, "String delimiter may not be null");

        final ArrayList<String> values = new ArrayList<>();

        final String trimmedString = trimOrNull(string);
        if (trimmedString != null) {
            final StringTokenizer tokens = new StringTokenizer(trimmedString, delimiter);
            while (tokens.hasMoreTokens()) {
                values.add(tokens.nextToken());
            }
            if (string.endsWith(delimiter)) {
                values.add("");
            }
        }

        return values;
    }

    /**
     * Safely trims a string.
     * 
     * @param s the string to trim, may be null
     * 
     * @return the trimmed string or null if the given string was null
     */
    @Nullable public static String trim(@Nullable final String s) {
        if (s == null) {
            return null;
        }

        return s.trim();
    }

    /**
     * Safely trims a string and, if empty, converts it to null.
     * 
     * @param s the string to trim, may be null
     * 
     * @return the trimmed string or null if the given string was null or the trimmed string was empty
     */
    @Nullable public static String trimOrNull(@Nullable final String s) {
        final String temp = trim(s);
        if (temp == null || temp.length() == 0) {
            return null;
        }

        return temp;
    }
    
    /**
     * Normalize a string collection by:
     * <ol>
     *   <li>Safely trimming each member.</li>
     *   <li>Converting all empty members to null.</li>
     *   <li>Removing any null members.</li>
     * </ol>
     * 
     * @param values the collection of string values
     * @return the normalized collection of string values
     */
    @Nonnull @NonnullElements public static Collection<String> normalizeStringCollection(
            @Nullable @NullableElements final Collection<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        
        return values.stream().map(StringSupport::trimOrNull).filter(e->e != null).collect(Collectors.toList());
    }

    /** Null/empty preserving conversion from xs:boolean to {@link Boolean}.
     * @param what the string: potentially empty or null
     * @return null or the boolean equivalent.
     */
    @Nullable public static Boolean booleanOf(final String what) {
        final String trimmed = trimOrNull(what);
        if (trimmed == null) {
            return null;
        }
        if ("1".equals(what)) {
            return true;
        } else if ("0".equals(what)) {
            return false;
        } else if ("true".equals(what)) {
            return true;
        } else if ("false".equals(what)) {
            return false;
        }
        throw new ConstraintViolationException("XML Booleans must be 0/1/true/false");
    }
}
