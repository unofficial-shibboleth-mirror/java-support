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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.collection.Pair;

/**
 * DDF unit tests.
 */
@SuppressWarnings("javadoc")
public class DDFTest {

    @Test
    public void testConstructors() {
        DDF obj = new DDF();
        assertNull(obj.name());
        assertTrue(obj.isnull());
        
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
        
        obj.unsafe_string("bar");
        System.out.print(obj);
    }
    
    @Test
    public void testLists() {
        final DDF obj = new DDF().list();
        assertTrue(obj.islist());
        assertEquals(obj.integer(), Integer.valueOf(0));
        
        obj.add(new DDF("foo", "bar"));
        obj.add(new DDF("foo2", 42));
        obj.add(new DDF("foo3").pointer(new Pair<>()));
        assertEquals(obj.integer(), Integer.valueOf(3));
        
        for (final DDF el : obj) {
            switch (el.name()) {
                case "foo":
                    assertEquals(el.string(), "bar");
                    break;
                
                case "foo2":
                    assertEquals(el.integer(), Integer.valueOf(42));
                    break;
                        
                case "foo3":
                    assertEquals(el.pointer(), new Pair<>());
                    break;
                        
                default:
                    fail("Node unrecognized");
            }
        }
        
        assertEquals(obj.getmember("[0]"), new DDF("foo", "bar"));
        assertEquals(obj.getmember("[1]"), new DDF("foo2", 42));
        assertTrue(obj.getmember("[3]").isnull());
        
        obj.addafter(new DDF(null), obj.getmember("[0]"));
        assertEquals(obj.integer(), Integer.valueOf(4));
        assertTrue(obj.getmember("[1]").isempty());

        obj.addbefore(new DDF("foo4"), obj.getmember("[2]"));
        assertEquals(obj.integer(), Integer.valueOf(5));
        assertTrue(obj.getmember("[2]").name().equals("foo4"));
        
        assertTrue(obj.asList().get(4).remove().ispointer());
        assertEquals(obj.integer(), Integer.valueOf(4));
    }

    @Test
    public void testStructures() {
        final DDF obj = new DDF().structure();
        assertTrue(obj.isstruct());
        assertEquals(obj.integer(), Integer.valueOf(0));
        
        obj.add(new DDF("foo", "bar"));
        assertEquals(obj.integer(), Integer.valueOf(1));
        assertTrue(obj.getmember("foo").name().equals("foo"));
        assertTrue(obj.getmember("foo").string().equals("bar"));
        
        obj.addmember("foo2").integer(42);
        assertEquals(obj.integer(), Integer.valueOf(2));
        
        obj.addmember("foo2.foo3").string("bar3");
        assertEquals(obj.integer(), Integer.valueOf(2));
        assertTrue(obj.getmember("foo2").isstruct());
        assertEquals(obj.getmember("foo2").integer(), Integer.valueOf(1));
        assertTrue(obj.getmember("foo2").getmember("foo3").string().equals("bar3"));
        assertTrue(obj.getmember("foo2.foo3").string().equals("bar3"));
    }
    
    @Test
    public void testEncoder() throws IOException {
        try (final ByteArrayOutputStream sink = new ByteArrayOutputStream()) {
            DDF.encode(sink, "foo".getBytes("UTF8"));
            assertEquals(sink.toString(), "foo");
            sink.reset();
            
            DDF.encode(sink, "foo bar".getBytes("UTF8"));
            assertEquals(sink.toString(), "foo%20bar");
            sink.reset();
            
            DDF.encode(sink, "foo\nbar".getBytes("UTF8"));
            assertEquals(sink.toString(), "foo%0Abar");
            sink.reset();
            
            DDF.encode(sink, "foo☯️bar".getBytes("UTF8"));
            assertEquals(sink.toString(), "foo%E2%98%AF%EF%B8%8Fbar");
            sink.reset();
            
            // -128 corresponds to 128, which is the extended ASCII Euro symbol.
            // This test demonstrates that round-tripping through such an encoding
            // will preserve the original 0x80 hex value in that position in the string
            // rather than converting through the UTF-8 representation.
            final byte[] unsafe = {102, 111, 111, -128, 98, 97, 114};
            DDF.encode(sink, new String(unsafe, "ISO-8859-1").getBytes("ISO-8859-1"));
            assertEquals(sink.toString(), "foo%80bar");
            sink.reset();
        }
    }
    
    @Test
    public void testSerialize() throws IOException {
        
        try (final ByteArrayOutputStream sink = new ByteArrayOutputStream()) {
            DDF obj = new DDF(null);
            obj.serialize(sink);
            assertEquals(sink.toByteArray(), testFile("empty-noname.ddf"));
            sink.reset();
            
            obj.name("foo bar");
            obj.serialize(sink);
            assertEquals(sink.toByteArray(), testFile("empty-name.ddf"));
            sink.reset();
            
            obj.string("zorkmid☯️");
            obj.serialize(sink);
            assertEquals(sink.toByteArray(), testFile("string-name.ddf"));
            sink.reset();

            final byte[] unsafe = {102, 111, 111, -128, 98, 97, 114};
            obj.unsafe_string(new String(unsafe, "ISO-8859-1"));
            obj.serialize(sink);
            assertEquals(sink.toByteArray(), testFile("unsafestring-name.ddf"));
            sink.reset();
            
            obj.integer(42);
            obj.serialize(sink);
            assertEquals(sink.toByteArray(), testFile("int-name.ddf"));
            sink.reset();

            obj.floating(42.1315927);
            obj.serialize(sink);
            assertEquals(sink.toByteArray(), testFile("float-name.ddf"));
            sink.reset();
            
            obj.structure();
            obj.serialize(sink);
            assertEquals(sink.toByteArray(), testFile("struct-empty.ddf"));
            sink.reset();
            
            obj.addmember("infocom.zork").list().add(new DDF().integer(1));
            obj.getmember("infocom.zork").add(new DDF().integer(2));
            obj.getmember("infocom.zork").add(new DDF().integer(3));
            obj.serialize(sink);
            assertEquals(sink.toByteArray(), testFile("struct-complex.ddf"));
            sink.reset();
        }
    }
    
    @Test
    public void testDeserialize() throws IOException {
        try (final InputStream is = getClass().getResourceAsStream("empty-noname.ddf")) {
            final DDF obj = DDF.deserialize(is);
            assertTrue(obj.isempty());
            assertNull(obj.name());
        }
        
        try (final InputStream is = getClass().getResourceAsStream("empty-name.ddf")) {
            final DDF obj = DDF.deserialize(is);
            assertTrue(obj.isempty());
            assertEquals(obj.name(), "foo bar");
        }
        
        try (final InputStream is = getClass().getResourceAsStream("string-name.ddf")) {
            final DDF obj = DDF.deserialize(is);
            assertTrue(obj.isstring());
            assertEquals(obj.name(), "foo bar");
            assertEquals(obj.string(), "zorkmid☯️");
        }

        try (final InputStream is = getClass().getResourceAsStream("unsafestring-name.ddf")) {
            final DDF obj = DDF.deserialize(is);
            assertTrue(obj.isstring());
            assertEquals(obj.name(), "foo bar");
            final byte[] unsafe = {102, 111, 111, -128, 98, 97, 114};
            assertEquals(obj.string(), new String(unsafe, "ISO-8859-1"));
        }

        try (final InputStream is = getClass().getResourceAsStream("int-name.ddf")) {
            final DDF obj = DDF.deserialize(is);
            assertTrue(obj.isint());
            assertEquals(obj.name(), "foo bar");
            assertEquals(obj.integer(), Integer.valueOf(42));
        }

        try (final InputStream is = getClass().getResourceAsStream("float-name.ddf")) {
            final DDF obj = DDF.deserialize(is);
            assertTrue(obj.isfloat());
            assertEquals(obj.name(), "foo bar");
            assertEquals(obj.floating(), Double.valueOf(42.1315927));
        }

        try (final InputStream is = getClass().getResourceAsStream("struct-empty.ddf")) {
            final DDF obj = DDF.deserialize(is);
            assertTrue(obj.isstruct());
            assertEquals(obj.name(), "foo bar");
            assertEquals(obj.integer(), Integer.valueOf(0));
        }

        try (final InputStream is = getClass().getResourceAsStream("struct-complex.ddf")) {
            final DDF obj = DDF.deserialize(is);
            assertTrue(obj.isstruct());
            assertEquals(obj.name(), "foo bar");
            assertEquals(obj.integer(), Integer.valueOf(1));
            assertTrue(obj.getmember("infocom").isstruct());
            assertTrue(obj.getmember("infocom.zork").islist());
            assertEquals(obj.getmember("infocom.zork").integer(), Integer.valueOf(3));
            assertEquals(obj.getmember("infocom.zork.[0]").integer(), Integer.valueOf(1));
            assertEquals(obj.getmember("infocom.zork.[1]").integer(), Integer.valueOf(2));
            assertEquals(obj.getmember("infocom.zork.[2]").integer(), Integer.valueOf(3));
        }
    }
    
    
    /**
     * Convert test file contents to a byte array.
     * 
     * @param name file name
     * 
     * @return byte array
     * 
     * @throws IOException on error
     */
    private byte[] testFile(@Nonnull final String name) throws IOException {
        return getClass().getResourceAsStream(name).readAllBytes();
    }
    
}