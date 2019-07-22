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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

/**
 * test for the various methods inside {@link StringSupport}
 */
public class StringSupportTest {

    @Nonnull @NotEmpty private static final String TRIM_TEST1 = " AARDVARK incorporated";

    @Nonnull @NotEmpty private static final String EMPTY_TRIM_TEST2 = " \t ";

    @Nonnull @NotEmpty private static final String SEPARATOR = "+";

    @Nonnull @NotEmpty private static final String TEST_LIST = "1+x2+y3+z4+5+6+";

    @Nonnull private static final List<String> TEST_LIST_AS_LIST = Arrays.asList("1", "x2", "y3", "z4", "5", "6", "");

    @Test public void testInputStreamToString() throws IOException {
        String str = null;
        final Resource resource = new ClassPathResource("/net/shibboleth/utilities/java/support/primitive/data.txt");
        try (final InputStream stream = resource.getInputStream()) {
            str = StringSupport.inputStreamToString(stream, null);
        }
        assertNotNull(str);
        assertEquals(str,
                "The quick, brown lizard jumped over the lazy fish.\n" +
                "Wait, I mean the slow, blue elephant jumped over the motivated squirrel.\n" +
                "No, that's wrong too.\n");
    }
    
    @Test public void testListToStringValue() {
        assertEquals(StringSupport.listToStringValue(TEST_LIST_AS_LIST, SEPARATOR), TEST_LIST,
                "toList<String> fails");
        boolean thrown = false;
        try {
            StringSupport.listToStringValue(TEST_LIST_AS_LIST, nullValue());
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        assertTrue(thrown, "null separator should throw an assertion");

        thrown = false;
        try {
            StringSupport.listToStringValue(nullValue(), SEPARATOR);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        assertTrue(thrown, "null list should throw an assertion");
    }

    @Test public void testStringToList() {
        assertEquals(StringSupport.stringToList(TEST_LIST, SEPARATOR), TEST_LIST_AS_LIST,
                "from List<String> fails");
        assertTrue(StringSupport.stringToList("", SEPARATOR).isEmpty(), "Empty input should give empty list");

        boolean thrown = false;
        try {
            StringSupport.stringToList(nullValue(), SEPARATOR);
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        assertTrue(thrown, "Null input should throw an assertion");

        thrown = false;
        try {
            StringSupport.stringToList(TEST_LIST, nullValue());
        } catch (ConstraintViolationException e) {
            thrown = true;
        }
        assertTrue(thrown, "Null separator should throw an assertion");
    }

    @Test public void testTrim() {

        assertEquals(StringSupport.trim(null), null, "Trimming Null should be OK");
        assertEquals(StringSupport.trim(EMPTY_TRIM_TEST2).length(), 0,
                "Trimming an empty string should return a string of zero length");

        assertEquals(StringSupport.trim(TRIM_TEST1), TRIM_TEST1.trim(), "Trimming a string");

    }

    @Test public void testTrimOrNull() {
        assertEquals(StringSupport.trimOrNull(null), null, "Trimming Null should be OK");
        assertEquals(StringSupport.trimOrNull(EMPTY_TRIM_TEST2), null,
                "Trimming an empty string should return null");

        assertEquals(StringSupport.trim(TRIM_TEST1), TRIM_TEST1.trim(), "Trimming a string");

    }
    
    @Test public void testNormalizeStringCollection() {
        Collection<String> output;
        
        output = StringSupport.normalizeStringCollection(new HashSet<>(Arrays.asList("foo", "bar", "baz")));
        assertEquals(output.size(), 3);
        assertTrue(output.contains("foo"));
        assertTrue(output.contains("bar"));
        assertTrue(output.contains("baz"));
        
        output = StringSupport.normalizeStringCollection(new HashSet<>(Arrays.asList(" \t\t foo  ", "  ", "  baz \r\n")));
        assertEquals(output.size(), 2);
        assertTrue(output.contains("foo"));
        assertTrue(output.contains("baz"));
        
        output = StringSupport.normalizeStringCollection(new HashSet<>(Arrays.asList("   foo   ", null, "baz")));
        assertEquals(output.size(), 2);
        assertTrue(output.contains("foo"));
        assertTrue(output.contains("baz"));
        
        output = StringSupport.normalizeStringCollection(new HashSet<String>());
        assertEquals(output.size(), 0);
        
        output = StringSupport.normalizeStringCollection(null);
        assertEquals(output.size(), 0);
    }

    private <T> T nullValue() {
        return null;
    }

    @Test public void testToBoolean() {
        assertNull(StringSupport.booleanOf(""));
        assertFalse(Boolean.valueOf(""));
        assertNull(StringSupport.booleanOf(null));
        assertFalse(Boolean.valueOf(null));
        assertTrue(StringSupport.booleanOf("true"));
        assertTrue(Boolean.valueOf("true"));
        assertFalse(StringSupport.booleanOf("false"));
        assertFalse(Boolean.valueOf("false"));
        assertFalse(StringSupport.booleanOf("0"));
        assertTrue(StringSupport.booleanOf("1"));
        try {
            StringSupport.booleanOf("elephant");
            fail("Should have thrown");
        } catch (ConstraintViolationException e) {
            // OK
        }
    }
}