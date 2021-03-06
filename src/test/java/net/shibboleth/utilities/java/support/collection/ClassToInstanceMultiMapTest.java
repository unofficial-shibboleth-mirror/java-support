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

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.Chronology;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link ClassToInstanceMultiMap}. */
public class ClassToInstanceMultiMapTest {

    @Test public void testClearIsEmpty() {
        ClassToInstanceMultiMap<Object> map = new ClassToInstanceMultiMap<>();

        map.clear();
        Assert.assertTrue(map.isEmpty());

        map.put(new Object());
        Assert.assertFalse(map.isEmpty());

        map.clear();
        Assert.assertTrue(map.isEmpty());
    }

    @Test public void testKeysAndContainsKey() {
        ClassToInstanceMultiMap<Temporal> map = new ClassToInstanceMultiMap<>();
        populate(map);
        Assert.assertEquals(map.keys().size(), 2);
        Assert.assertFalse(map.containsKey(null));
        Assert.assertFalse(map.containsKey(Chronology.class));
        Assert.assertFalse(map.containsKey(Temporal.class));
        Assert.assertFalse(map.containsKey(TemporalAdjuster.class));
        Assert.assertTrue(map.containsKey(ZonedDateTime.class));
        Assert.assertFalse(map.containsKey(ChronoZonedDateTime.class));
        Assert.assertFalse(map.containsKey(Comparable.class));
        Assert.assertFalse(map.containsKey(Serializable.class));
        Assert.assertTrue(map.containsKey(Instant.class));

        map = new ClassToInstanceMultiMap<>(true);
        populate(map);
        Assert.assertEquals(map.keys().size(), 8);
        Assert.assertFalse(map.containsKey(null));
        Assert.assertFalse(map.containsKey(Chronology.class));
        Assert.assertTrue(map.containsKey(Temporal.class));
        Assert.assertTrue(map.containsKey(TemporalAdjuster.class));
        Assert.assertTrue(map.containsKey(ZonedDateTime.class));
        Assert.assertTrue(map.containsKey(ChronoZonedDateTime.class));
        Assert.assertTrue(map.containsKey(Comparable.class));
        Assert.assertTrue(map.containsKey(Serializable.class));
        Assert.assertTrue(map.containsKey(Instant.class));
    }

    @Test public void testValuesAndContainsValues() {
        ClassToInstanceMultiMap<Temporal> map = new ClassToInstanceMultiMap<>();

        ZonedDateTime now = ZonedDateTime.now();
        map.put(now);

        ZonedDateTime now100 = now.plusMinutes(100);
        map.put(now100);

        Instant instant = Instant.now();
        map.put(instant);

        Assert.assertEquals(map.values().size(), 3);
        Assert.assertFalse(map.containsValue(null));
        Assert.assertFalse(map.containsValue(now.minusDays(100)));
        Assert.assertFalse(map.containsValue(instant.minusSeconds(100)));
        Assert.assertTrue(map.containsValue(instant));
        Assert.assertTrue(map.containsValue(now));
        Assert.assertTrue(map.containsValue(now100));
    }

    @Test public void testEquals() {
        final ClassToInstanceMultiMap<Temporal> map = new ClassToInstanceMultiMap<>();
        final ClassToInstanceMultiMap<Temporal> map2 = new ClassToInstanceMultiMap<>();
        final ClassToInstanceMultiMap<Temporal> map3 = new ClassToInstanceMultiMap<>();

        final ZonedDateTime now = ZonedDateTime.now();
        map.put(now);
        map2.put(now);
        map3.put(now);

        final ZonedDateTime now100 = now.plusMinutes(100);
        map.put(now100);
        map2.put(now100);
        map3.put(now100);

        final Instant instant = Instant.now();
        map.put(instant);
        map2.put(instant);

        Assert.assertTrue(map.equals(map2));
        Assert.assertFalse(map.equals(map3));

        Assert.assertEquals(map.hashCode(), map2.hashCode());
        Assert.assertNotEquals(map.hashCode(), map3.hashCode());

    }

    @Test public void testGet() {
        ClassToInstanceMultiMap<Temporal> map = new ClassToInstanceMultiMap<>();
        populate(map);

        List<?> values = map.get(null);
        Assert.assertEquals(values.size(), 0);

        values = map.get(ZonedDateTime.class);
        Assert.assertEquals(values.size(), 2);

        values = map.get(Instant.class);
        Assert.assertEquals(values.size(), 1);
    }

    @Test public void testNoIndexedDuplicateValues() {
        ClassToInstanceMultiMap<Object> map = new ClassToInstanceMultiMap<>(true);

        map.put(new FooBarImpl());

        Assert.assertEquals(map.get(Foo.class).size(), 1);
        Assert.assertEquals(map.get(Bar.class).size(), 1);
        Assert.assertEquals(map.get(AbstractFoo.class).size(), 1);
        Assert.assertEquals(map.get(AbstractFooBar.class).size(), 1);
        Assert.assertEquals(map.get(FooBarImpl.class).size(), 1);
    }

    @Test public void testDuplicateInsertions() {
        ClassToInstanceMultiMap<Object> map = new ClassToInstanceMultiMap<>(true);

        FooBarImpl fb = new FooBarImpl();

        map.put(fb);
        map.put(fb);

        Assert.assertEquals(map.values().size(), 1);

        Assert.assertEquals(map.get(Foo.class).size(), 1);
        Assert.assertEquals(map.get(Bar.class).size(), 1);
        Assert.assertEquals(map.get(AbstractFoo.class).size(), 1);
        Assert.assertEquals(map.get(AbstractFooBar.class).size(), 1);
        Assert.assertEquals(map.get(FooBarImpl.class).size(), 1);
    }

    @Test public void testRemoveValue() {
        ClassToInstanceMultiMap<Object> map = new ClassToInstanceMultiMap<>(true);

        FooBarImpl fb = new FooBarImpl();
        FooImpl f = new FooImpl();

        map.put(fb); // This is what we'll remove.
        map.put(f); // This is canary to test that its indexes don't disappear.

        Assert.assertTrue(map.containsValue(fb));
        Assert.assertTrue(map.containsValue(f));

        Assert.assertTrue(map.containsKey(Foo.class));
        Assert.assertTrue(map.containsKey(AbstractFoo.class));
        Assert.assertTrue(map.containsKey(FooImpl.class));

        Assert.assertEquals(map.get(Foo.class).size(), 2);
        Assert.assertEquals(map.get(AbstractFoo.class).size(), 2);
        Assert.assertEquals(map.get(FooImpl.class).size(), 1);

        Assert.assertTrue(map.containsKey(Bar.class));
        Assert.assertTrue(map.containsKey(AbstractFooBar.class));
        Assert.assertTrue(map.containsKey(FooBarImpl.class));

        Assert.assertEquals(map.get(Bar.class).size(), 1);
        Assert.assertEquals(map.get(AbstractFooBar.class).size(), 1);
        Assert.assertEquals(map.get(FooBarImpl.class).size(), 1);

        map.remove(fb);

        Assert.assertFalse(map.containsValue(fb));
        Assert.assertTrue(map.containsValue(f));

        Assert.assertTrue(map.containsKey(Foo.class));
        Assert.assertTrue(map.containsKey(AbstractFoo.class));
        Assert.assertTrue(map.containsKey(FooImpl.class));

        Assert.assertEquals(map.get(Foo.class).size(), 1);
        Assert.assertEquals(map.get(AbstractFoo.class).size(), 1);
        Assert.assertEquals(map.get(FooImpl.class).size(), 1);

        Assert.assertFalse(map.containsKey(Bar.class));
        Assert.assertFalse(map.containsKey(AbstractFooBar.class));
        Assert.assertFalse(map.containsKey(FooBarImpl.class));

        Assert.assertEquals(map.get(Bar.class).size(), 0);
        Assert.assertEquals(map.get(AbstractFooBar.class).size(), 0);
        Assert.assertEquals(map.get(FooBarImpl.class).size(), 0);
    }

    @Test public void testRemoveByType() {
        ClassToInstanceMultiMap<Object> map = new ClassToInstanceMultiMap<>(true);

        FooBarImpl fb = new FooBarImpl();
        FooImpl f = new FooImpl();

        map.put(fb);
        map.put(f);

        Assert.assertTrue(map.containsValue(fb));
        Assert.assertTrue(map.containsValue(f));

        Assert.assertTrue(map.containsKey(Foo.class));
        Assert.assertTrue(map.containsKey(AbstractFoo.class));
        Assert.assertTrue(map.containsKey(FooImpl.class));

        Assert.assertEquals(map.get(Foo.class).size(), 2);
        Assert.assertEquals(map.get(AbstractFoo.class).size(), 2);
        Assert.assertEquals(map.get(FooImpl.class).size(), 1);

        Assert.assertTrue(map.containsKey(Bar.class));
        Assert.assertTrue(map.containsKey(AbstractFooBar.class));
        Assert.assertTrue(map.containsKey(FooBarImpl.class));

        Assert.assertEquals(map.get(Bar.class).size(), 1);
        Assert.assertEquals(map.get(AbstractFooBar.class).size(), 1);
        Assert.assertEquals(map.get(FooBarImpl.class).size(), 1);

        map.remove(Bar.class);

        Assert.assertFalse(map.containsValue(fb));
        Assert.assertTrue(map.containsValue(f));

        Assert.assertTrue(map.containsKey(Foo.class));
        Assert.assertTrue(map.containsKey(AbstractFoo.class));
        Assert.assertTrue(map.containsKey(FooImpl.class));

        Assert.assertEquals(map.get(Foo.class).size(), 1);
        Assert.assertEquals(map.get(AbstractFoo.class).size(), 1);
        Assert.assertEquals(map.get(FooImpl.class).size(), 1);

        Assert.assertFalse(map.containsKey(Bar.class));
        Assert.assertFalse(map.containsKey(AbstractFooBar.class));
        Assert.assertFalse(map.containsKey(FooBarImpl.class));

        Assert.assertEquals(map.get(Bar.class).size(), 0);
        Assert.assertEquals(map.get(AbstractFooBar.class).size(), 0);
        Assert.assertEquals(map.get(FooBarImpl.class).size(), 0);

    }

    @Test public void testRemoveAll() {
        ClassToInstanceMultiMap<Object> map = new ClassToInstanceMultiMap<>(true);

        FooImpl f1 = new FooImpl();
        FooImpl f2 = new FooImpl();
        FooImpl f3 = new FooImpl();

        FooBarImpl fb1 = new FooBarImpl();
        FooBarImpl fb2 = new FooBarImpl();
        FooBarImpl fb3 = new FooBarImpl();

        map.put(f1);
        map.put(f2);
        map.put(f3);
        map.put(fb1);
        map.put(fb2);
        map.put(fb3);

        Assert.assertEquals(map.values().size(), 6);
        Assert.assertEquals(map.get(Foo.class).size(), 6);
        Assert.assertEquals(map.get(Bar.class).size(), 3);

        map.removeAll(Arrays.asList(f1, f2, fb1));

        Assert.assertEquals(map.values().size(), 3);
        Assert.assertEquals(map.get(Foo.class).size(), 3);
        Assert.assertEquals(map.get(Bar.class).size(), 2);

        map.removeAll(Arrays.asList(fb2, fb3));

        Assert.assertEquals(map.values().size(), 1);
        Assert.assertEquals(map.get(Foo.class).size(), 1);
        Assert.assertEquals(map.get(Bar.class).size(), 0);
        Assert.assertFalse(map.containsKey(Bar.class));

        map.removeAll(Arrays.asList(f3));

        Assert.assertEquals(map.values().size(), 0);
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(map.get(Foo.class).size(), 0);
        Assert.assertEquals(map.get(Bar.class).size(), 0);
        Assert.assertFalse(map.containsKey(Foo.class));
        Assert.assertFalse(map.containsKey(Bar.class));
    }

    protected void populate(ClassToInstanceMultiMap<Temporal> map) {
        ZonedDateTime now = ZonedDateTime.now();
        map.put(now);

        ZonedDateTime now100 = now.plusMinutes(100);
        map.put(now100);

        Instant instant = Instant.now();
        map.put(instant);
    }

    // Test classes and interfaces

    public interface Foo {
    };

    public interface Bar extends Foo {
    };

    public abstract class AbstractFoo implements Foo {
    };

    public class FooImpl extends AbstractFoo {
    };

    public abstract class AbstractFooBar extends AbstractFoo implements Bar {
    };

    public class FooBarImpl extends AbstractFooBar {
    };

}