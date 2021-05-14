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

package net.shibboleth.utilities.java.support.ddf;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

/**
 * DDF unit tests.
 */
@SuppressWarnings("javadoc")
public class DDFTest {

    @Test
    public void testConstructors() {
        DDF obj = new DDF();
        assertNull(obj.name());
        assertTrue(obj.isempty());
        
        obj = new DDF("foo");
        assertEquals(obj.name(), "foo");
        assertTrue(obj.isempty());
        
        obj = new DDF("foo", "bar");
        assertEquals(obj.name(), "foo");
        assertTrue(obj.isstring());
        assertEquals(obj.string(), "bar");

        obj = new DDF("foo", 42);
        assertEquals(obj.name(), "foo");
        assertTrue(obj.isint());
        assertEquals(obj.integer(), Integer.valueOf(42));

        obj = new DDF("foo", 42.42);
        assertEquals(obj.name(), "foo");
        assertTrue(obj.isfloat());
        assertEquals(obj.floating(), Double.valueOf(42.42));
    }

    @Test
    public void testConversions() {
        final DDF obj = new DDF("foo");
        obj.string("bar");
        assertTrue(obj.isstring());
        assertEquals(obj.string(), "bar");
        assertNull(obj.integer());
        assertNull(obj.floating());
        
        obj.string(42);
        assertTrue(obj.isstring());
        assertEquals(obj.string(), "42");
        assertEquals(obj.integer(), Integer.valueOf(42));
        assertEquals(obj.floating(), Double.valueOf(42));

        obj.string(42.42);
        assertTrue(obj.isstring());
        assertEquals(obj.string(), "42.42");
        assertNull(obj.integer());
        assertEquals(obj.floating(), Double.valueOf(42.42));
        
        obj.integer(42);
        assertTrue(obj.isint());
        assertEquals(obj.integer(), Integer.valueOf(42));
        assertEquals(obj.floating(), Double.valueOf(42));
        
        obj.integer("42");
        assertTrue(obj.isint());
        assertEquals(obj.integer(), Integer.valueOf(42));
        assertEquals(obj.floating(), Double.valueOf(42));
        
        obj.floating(42.42);
        assertTrue(obj.isfloat());
        assertEquals(obj.integer(), Integer.valueOf(42));
        assertEquals(obj.floating(), Double.valueOf(42.42));
    }

}