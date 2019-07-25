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
import java.util.Arrays;
import java.util.List;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.base.AbstractDateTime;
import org.joda.time.base.AbstractInstant;
import org.joda.time.base.BaseDateTime;
import org.joda.time.chrono.ISOChronology;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link LockableClassToInstanceMultiMap}. */
public class LockableClassToInstanceMultiMapTest {

    @Test public void testClearIsEmpty() {
        LockableClassToInstanceMultiMap<Object> map = new LockableClassToInstanceMultiMap<>();

        map.clear();
        Assert.assertTrue(map.isEmptyWithLock());

        map.put(new Object());
        Assert.assertFalse(map.isEmptyWithLock());

        map.clear();
        Assert.assertTrue(map.isEmptyWithLock());
    }

    @Test public void testKeysAndContainsKey() {
        LockableClassToInstanceMultiMap<AbstractInstant> map = new LockableClassToInstanceMultiMap<>();
        populate(map);
        Assert.assertEquals(map.keysWithLock().size(), 2);
        Assert.assertFalse(map.containsKeyWithLock(null));
        Assert.assertFalse(map.containsKeyWithLock(Chronology.class));
        Assert.assertFalse(map.containsKeyWithLock(AbstractInstant.class));
        Assert.assertFalse(map.containsKeyWithLock(AbstractDateTime.class));
        Assert.assertFalse(map.containsKeyWithLock(BaseDateTime.class));
        Assert.assertTrue(map.containsKeyWithLock(DateTime.class));
        Assert.assertFalse(map.containsKeyWithLock(Comparable.class));
        Assert.assertFalse(map.containsKeyWithLock(ReadableDateTime.class));
        Assert.assertFalse(map.containsKeyWithLock(ReadableInstant.class));
        Assert.assertFalse(map.containsKeyWithLock(Serializable.class));
        Assert.assertTrue(map.containsKeyWithLock(Instant.class));

        map = new LockableClassToInstanceMultiMap<>(true);
        populate(map);
        Assert.assertEquals(map.keysWithLock().size(), 9);
        Assert.assertFalse(map.containsKeyWithLock(null));
        Assert.assertFalse(map.containsKeyWithLock(Chronology.class));
        Assert.assertTrue(map.containsKeyWithLock(AbstractInstant.class));
        Assert.assertTrue(map.containsKeyWithLock(AbstractDateTime.class));
        Assert.assertTrue(map.containsKeyWithLock(BaseDateTime.class));
        Assert.assertTrue(map.containsKeyWithLock(DateTime.class));
        Assert.assertTrue(map.containsKeyWithLock(Comparable.class));
        Assert.assertTrue(map.containsKeyWithLock(ReadableDateTime.class));
        Assert.assertTrue(map.containsKeyWithLock(ReadableInstant.class));
        Assert.assertTrue(map.containsKeyWithLock(Serializable.class));
        Assert.assertTrue(map.containsKeyWithLock(Instant.class));
    }

    @Test public void testValuesAndContainsValues() {
        LockableClassToInstanceMultiMap<AbstractInstant> map = new LockableClassToInstanceMultiMap<>();

        // Create now as a DateTime with a specific chronology so that it
        // can be guaranteed to compare non-equals with "instant" below.
        final DateTimeZone notUTC = DateTimeZone.forID("America/Los_Angeles");
        DateTime now = new DateTime(ISOChronology.getInstance(notUTC));
        map.putWithLock(now);

        DateTime now100 = now.plus(100);
        map.putWithLock(now100);

        // instants implicitly have the ISOChronology in the UTC time zone
        Instant instant = new Instant();
        map.putWithLock(instant);

        // This test makes the assumption that "now" and "instant"
        // do not compare as "equal". If they do, the second added will
        // be omitted from the map.values() collection because the collection
        // .contains() the first one.
        Assert.assertFalse(now.equals(instant), "now is equals to instant; test assumption violated");

        Assert.assertEquals(map.valuesWithLock().size(), 3);
        Assert.assertFalse(map.containsValueWithLock(null));
        Assert.assertFalse(map.containsValueWithLock(now.minus(100)));
        Assert.assertFalse(map.containsValueWithLock(instant.minus(100)));
        Assert.assertTrue(map.containsValueWithLock(instant));
        Assert.assertTrue(map.containsValueWithLock(now));
        Assert.assertTrue(map.containsValueWithLock(now100));
    }

    @Test public void testEquals() {
        final LockableClassToInstanceMultiMap<AbstractInstant> map = new LockableClassToInstanceMultiMap<>();
        final LockableClassToInstanceMultiMap<AbstractInstant> map2 = new LockableClassToInstanceMultiMap<>();
        final LockableClassToInstanceMultiMap<AbstractInstant> map3 = new LockableClassToInstanceMultiMap<>();

        final DateTime now = new DateTime();
        map.putWithLock(now);
        map2.putWithLock(now);
        map3.putWithLock(now);

        final DateTime now100 = now.plus(100);
        map.putWithLock(now100);
        map2.putWithLock(now100);
        map3.putWithLock(now100);

        final Instant instant = new Instant();
        map.putWithLock(instant);
        map2.putWithLock(instant);

        Assert.assertTrue(map.equals(map2));
        Assert.assertFalse(map.equals(map3));

        Assert.assertEquals(map.hashCode(), map2.hashCode());
        Assert.assertNotEquals(map.hashCode(), map3.hashCode());

    }

    @Test public void testGet() {
        LockableClassToInstanceMultiMap<AbstractInstant> map = new LockableClassToInstanceMultiMap<>();
        populate(map);

        List<?> values = map.getWithLock(null);
        Assert.assertEquals(values.size(), 0);

        values = map.getWithLock(DateTime.class);
        Assert.assertEquals(values.size(), 2);

        values = map.getWithLock(Instant.class);
        Assert.assertEquals(values.size(), 1);
    }

    @Test public void testNoIndexedDuplicateValues() {
        LockableClassToInstanceMultiMap<Object> map = new LockableClassToInstanceMultiMap<>(true);

        map.putWithLock(new FooBarImpl());

        Assert.assertEquals(map.getWithLock(Foo.class).size(), 1);
        Assert.assertEquals(map.getWithLock(Bar.class).size(), 1);
        Assert.assertEquals(map.getWithLock(AbstractFoo.class).size(), 1);
        Assert.assertEquals(map.getWithLock(AbstractFooBar.class).size(), 1);
        Assert.assertEquals(map.getWithLock(FooBarImpl.class).size(), 1);
    }

    @Test public void testDuplicateInsertions() {
        LockableClassToInstanceMultiMap<Object> map = new LockableClassToInstanceMultiMap<>(true);

        FooBarImpl fb = new FooBarImpl();

        map.putWithLock(fb);
        map.putWithLock(fb);

        Assert.assertEquals(map.valuesWithLock().size(), 1);

        Assert.assertEquals(map.getWithLock(Foo.class).size(), 1);
        Assert.assertEquals(map.getWithLock(Bar.class).size(), 1);
        Assert.assertEquals(map.getWithLock(AbstractFoo.class).size(), 1);
        Assert.assertEquals(map.getWithLock(AbstractFooBar.class).size(), 1);
        Assert.assertEquals(map.getWithLock(FooBarImpl.class).size(), 1);
    }

    @Test public void testRemoveValue() {
        LockableClassToInstanceMultiMap<Object> map = new LockableClassToInstanceMultiMap<>(true);

        FooBarImpl fb = new FooBarImpl();
        FooImpl f = new FooImpl();

        map.putWithLock(fb); // This is what we'll remove.
        map.putWithLock(f); // This is canary to test that its indexes don't disappear.

        Assert.assertTrue(map.containsValueWithLock(fb));
        Assert.assertTrue(map.containsValueWithLock(f));

        Assert.assertTrue(map.containsKeyWithLock(Foo.class));
        Assert.assertTrue(map.containsKeyWithLock(AbstractFoo.class));
        Assert.assertTrue(map.containsKeyWithLock(FooImpl.class));

        Assert.assertEquals(map.getWithLock(Foo.class).size(), 2);
        Assert.assertEquals(map.getWithLock(AbstractFoo.class).size(), 2);
        Assert.assertEquals(map.getWithLock(FooImpl.class).size(), 1);

        Assert.assertTrue(map.containsKeyWithLock(Bar.class));
        Assert.assertTrue(map.containsKeyWithLock(AbstractFooBar.class));
        Assert.assertTrue(map.containsKeyWithLock(FooBarImpl.class));

        Assert.assertEquals(map.getWithLock(Bar.class).size(), 1);
        Assert.assertEquals(map.getWithLock(AbstractFooBar.class).size(), 1);
        Assert.assertEquals(map.getWithLock(FooBarImpl.class).size(), 1);

        map.remove(fb);

        Assert.assertFalse(map.containsValueWithLock(fb));
        Assert.assertTrue(map.containsValueWithLock(f));

        Assert.assertTrue(map.containsKeyWithLock(Foo.class));
        Assert.assertTrue(map.containsKeyWithLock(AbstractFoo.class));
        Assert.assertTrue(map.containsKeyWithLock(FooImpl.class));

        Assert.assertEquals(map.getWithLock(Foo.class).size(), 1);
        Assert.assertEquals(map.getWithLock(AbstractFoo.class).size(), 1);
        Assert.assertEquals(map.getWithLock(FooImpl.class).size(), 1);

        Assert.assertFalse(map.containsKeyWithLock(Bar.class));
        Assert.assertFalse(map.containsKeyWithLock(AbstractFooBar.class));
        Assert.assertFalse(map.containsKeyWithLock(FooBarImpl.class));

        Assert.assertEquals(map.getWithLock(Bar.class).size(), 0);
        Assert.assertEquals(map.getWithLock(AbstractFooBar.class).size(), 0);
        Assert.assertEquals(map.getWithLock(FooBarImpl.class).size(), 0);
    }

    @Test public void testRemoveByType() {
        LockableClassToInstanceMultiMap<Object> map = new LockableClassToInstanceMultiMap<>(true);

        FooBarImpl fb = new FooBarImpl();
        FooImpl f = new FooImpl();

        map.putWithLock(fb);
        map.putWithLock(f);

        Assert.assertTrue(map.containsValueWithLock(fb));
        Assert.assertTrue(map.containsValueWithLock(f));

        Assert.assertTrue(map.containsKeyWithLock(Foo.class));
        Assert.assertTrue(map.containsKeyWithLock(AbstractFoo.class));
        Assert.assertTrue(map.containsKeyWithLock(FooImpl.class));

        Assert.assertEquals(map.getWithLock(Foo.class).size(), 2);
        Assert.assertEquals(map.getWithLock(AbstractFoo.class).size(), 2);
        Assert.assertEquals(map.getWithLock(FooImpl.class).size(), 1);

        Assert.assertTrue(map.containsKeyWithLock(Bar.class));
        Assert.assertTrue(map.containsKeyWithLock(AbstractFooBar.class));
        Assert.assertTrue(map.containsKeyWithLock(FooBarImpl.class));

        Assert.assertEquals(map.getWithLock(Bar.class).size(), 1);
        Assert.assertEquals(map.getWithLock(AbstractFooBar.class).size(), 1);
        Assert.assertEquals(map.getWithLock(FooBarImpl.class).size(), 1);

        map.removeWithLock(Bar.class);

        Assert.assertFalse(map.containsValueWithLock(fb));
        Assert.assertTrue(map.containsValueWithLock(f));

        Assert.assertTrue(map.containsKeyWithLock(Foo.class));
        Assert.assertTrue(map.containsKeyWithLock(AbstractFoo.class));
        Assert.assertTrue(map.containsKeyWithLock(FooImpl.class));

        Assert.assertEquals(map.getWithLock(Foo.class).size(), 1);
        Assert.assertEquals(map.getWithLock(AbstractFoo.class).size(), 1);
        Assert.assertEquals(map.getWithLock(FooImpl.class).size(), 1);

        Assert.assertFalse(map.containsKeyWithLock(Bar.class));
        Assert.assertFalse(map.containsKeyWithLock(AbstractFooBar.class));
        Assert.assertFalse(map.containsKeyWithLock(FooBarImpl.class));

        Assert.assertEquals(map.getWithLock(Bar.class).size(), 0);
        Assert.assertEquals(map.getWithLock(AbstractFooBar.class).size(), 0);
        Assert.assertEquals(map.getWithLock(FooBarImpl.class).size(), 0);

    }

    @Test public void testRemoveAll() {
        LockableClassToInstanceMultiMap<Object> map = new LockableClassToInstanceMultiMap<>(true);

        FooImpl f1 = new FooImpl();
        FooImpl f2 = new FooImpl();
        FooImpl f3 = new FooImpl();

        FooBarImpl fb1 = new FooBarImpl();
        FooBarImpl fb2 = new FooBarImpl();
        FooBarImpl fb3 = new FooBarImpl();

        map.putWithLock(f1);
        map.putWithLock(f2);
        map.putWithLock(f3);
        map.putWithLock(fb1);
        map.putWithLock(fb2);
        map.putWithLock(fb3);

        Assert.assertEquals(map.valuesWithLock().size(), 6);
        Assert.assertEquals(map.getWithLock(Foo.class).size(), 6);
        Assert.assertEquals(map.getWithLock(Bar.class).size(), 3);

        map.removeAllWithLock(Arrays.asList(f1, f2, fb1));

        Assert.assertEquals(map.valuesWithLock().size(), 3);
        Assert.assertEquals(map.getWithLock(Foo.class).size(), 3);
        Assert.assertEquals(map.getWithLock(Bar.class).size(), 2);

        map.removeAllWithLock(Arrays.asList(fb2, fb3));

        Assert.assertEquals(map.valuesWithLock().size(), 1);
        Assert.assertEquals(map.getWithLock(Foo.class).size(), 1);
        Assert.assertEquals(map.getWithLock(Bar.class).size(), 0);
        Assert.assertFalse(map.containsKeyWithLock(Bar.class));

        map.removeAllWithLock(Arrays.asList(f3));

        Assert.assertEquals(map.valuesWithLock().size(), 0);
        Assert.assertTrue(map.isEmptyWithLock());
        Assert.assertEquals(map.getWithLock(Foo.class).size(), 0);
        Assert.assertEquals(map.getWithLock(Bar.class).size(), 0);
        Assert.assertFalse(map.containsKeyWithLock(Foo.class));
        Assert.assertFalse(map.containsKeyWithLock(Bar.class));
    }

    protected void populate(LockableClassToInstanceMultiMap<AbstractInstant> map) {
        DateTime now = new DateTime();
        map.putWithLock(now);

        DateTime now100 = now.plus(100);
        map.putWithLock(now100);

        Instant instant = new Instant();
        map.putWithLock(instant);
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