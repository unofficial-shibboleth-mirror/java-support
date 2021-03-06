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

package net.shibboleth.utilities.java.support.collection;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link ValueTypeIndexedMap} unit test. */
public class ValueTypeIndexedMapTest {

    /** Instance used for testing. */
    private ValueTypeIndexedMap<String, Object> map;

    /** Set up state for this test. */
    @BeforeMethod public void setUp() {
        map = new ValueTypeIndexedMap<>();
        map.setTypes(Arrays.asList(new Class<?>[] {Integer.class, String.class}));
        map.rebuildIndex();
    }

    /**
     * Test basic functionality.
     */
    @Test public void testBasic() {
        map.put("i1", Integer.parseInt("4"));
        map.put("s1", "first string");
        map.put("s2", "second string");

        Assert.assertEquals(map.size(), 3);
        Assert.assertEquals(map.subMap(Integer.class).size(), 1);
        Assert.assertEquals(map.subMap(String.class).size(), 2);

        map.remove("s1");
        Assert.assertEquals(map.size(), 2);
        Assert.assertEquals(map.subMap(Integer.class).size(), 1);
        Assert.assertEquals(map.subMap(String.class).size(), 1);
    }

    /**
     * Test null key support.
     */
    @Test public void testNullKeys() {
        map.put("i1", Integer.parseInt("2"));
        map.put(null, Integer.parseInt("3"));
        map.put("s1", "first string");

        Assert.assertEquals(map.size(), 3);
        Assert.assertEquals(map.subMap(Integer.class).size(), 2);
        Assert.assertEquals(map.subMap(String.class).size(), 1);

        map.put(null, "new string");
        Assert.assertEquals(map.size(), 3);
        Assert.assertEquals(map.subMap(Integer.class).size(), 1);
        Assert.assertEquals(map.subMap(String.class).size(), 2);

        Assert.assertTrue(map.containsKey(null));
        map.remove(null);
        Assert.assertFalse(map.containsKey(null));
    }

    /**
     * Test null value support.
     */
    @Test public void testNullValues() {
        map.getTypes().add(null);
        map.rebuildIndex();

        map.put("i1", Integer.parseInt("3"));
        map.put("n1", null);
        map.put("s1", "first string");

        Assert.assertEquals(map.size(), 3);
        Assert.assertEquals(map.subMap(Integer.class).size(), 1);
        Assert.assertEquals(map.subMap(String.class).size(), 1);
        Assert.assertEquals(map.subMap(null).size(), 1);

        map.put("i1", "new string");
        Assert.assertEquals(map.size(), 3);
        Assert.assertEquals(map.subMap(Integer.class).size(), 0);
        Assert.assertEquals(map.subMap(String.class).size(), 2);
        Assert.assertEquals(map.subMap(null).size(), 1);

        map.put("i1", null);
        Assert.assertEquals(map.size(), 3);
        Assert.assertEquals(map.subMap(Integer.class).size(), 0);
        Assert.assertEquals(map.subMap(String.class).size(), 1);
        Assert.assertEquals(map.subMap(null).size(), 2);
    }

    /* Test equals and hashcode */
    @Test public void testEqualsHashCode() {
        ValueTypeIndexedMap<String, Object> other = new ValueTypeIndexedMap<>();
        other.setTypes(Arrays.asList(new Class<?>[] {Integer.class}));
        other.rebuildIndex();

        Assert.assertEquals(map, other, "Empty maps should be the same");
        Assert.assertEquals(map.hashCode(), other.hashCode(), "Empty maps have same hash code");

        map.put("i1", Integer.parseInt("4"));
        map.put("s1", "first string");
        map.put("s2", "second string");
        other.put("i1", Integer.parseInt("4"));

        Assert.assertNotSame(map, other, "Different maps should differ");
        Assert.assertNotSame(map.hashCode(), other.hashCode(), "Different maps should have different hash codes");

        map.remove("s1");
        other.put("s2", "second string");

        Assert.assertEquals(map, other, "Similar maps should be equals");
        Assert.assertEquals(map.hashCode(), other.hashCode(), "Similar maps should have the same hash codes");

    }

}