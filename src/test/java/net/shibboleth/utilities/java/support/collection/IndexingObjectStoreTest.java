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

import org.testng.Assert;
import org.testng.annotations.Test;


/** Unit test for {@link IndexingObjectStore }. */
public class IndexingObjectStoreTest {

    @Test
    public void testIndexingObjectStore() {
        IndexingObjectStore<String> store = new IndexingObjectStore<>();

        String str1 = new String("foo");
        String str2 = new String("bar");

        Assert.assertTrue(store.isEmpty());
        Assert.assertEquals(store.size(), 0);
        Assert.assertFalse(store.containsInstance("foo"));

        String nullIndex = store.put(null);
        Assert.assertNull(nullIndex);
        Assert.assertTrue(store.isEmpty());
        Assert.assertEquals(store.size(), 0);
        Assert.assertFalse(store.containsInstance(null));

        String str1Index = store.put(str1);
        Assert.assertTrue(store.containsIndex(str1Index));
        Assert.assertTrue(store.containsInstance("foo"));
        Assert.assertFalse(store.isEmpty());
        Assert.assertEquals(store.size(), 1);
        Assert.assertEquals(store.get(str1Index), str1);

        String index1 = store.put("foo");
        Assert.assertTrue(store.containsIndex(index1));
        Assert.assertTrue(store.containsInstance("foo"));
        Assert.assertFalse(store.isEmpty());
        Assert.assertEquals(store.size(), 1);
        Assert.assertEquals(index1, str1Index);
        Assert.assertEquals(store.get(index1), str1);

        store.remove(str1Index);
        Assert.assertTrue(store.containsIndex(index1));
        Assert.assertTrue(store.containsInstance("foo"));
        Assert.assertFalse(store.isEmpty());
        Assert.assertEquals(store.size(), 1);
        Assert.assertEquals(index1, index1);
        Assert.assertEquals(store.get(index1), str1);

        String str2Index = store.put(str2);
        Assert.assertTrue(store.containsIndex(str2Index));
        Assert.assertTrue(store.containsInstance("bar"));
        Assert.assertFalse(store.isEmpty());
        Assert.assertEquals(store.size(), 2);
        Assert.assertEquals(store.get(str2Index), str2);

        store.remove(str1Index);
        Assert.assertFalse(store.containsIndex(str1Index));
        Assert.assertFalse(store.containsInstance("foo"));
        Assert.assertFalse(store.isEmpty());
        Assert.assertEquals(store.size(), 1);
        Assert.assertNull(store.get(str1Index));

        store.clear();
        Assert.assertTrue(store.isEmpty());
        Assert.assertEquals(store.size(), 0);
        Assert.assertFalse(store.containsInstance("foo"));
        Assert.assertFalse(store.containsInstance("bar"));
    }
    
    @Test
    public void testCollision() {
        /*
         * These String values have the same hashCode() value, using the
         * algorithm that is documented as part of the Java 7 API. As it
         * is part of the API, we do not expect it to vary between
         * implementations.
         */
        final String s1 = "FB";
        final String s2 = "Ea";
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        final IndexingObjectStore<String> store = new IndexingObjectStore<>();

        final String s1index = store.put(s1);
        final String s2index = store.put(s2);
        Assert.assertNotEquals(s1index, s2index);
        Assert.assertTrue(store.containsIndex(s1index));
        Assert.assertTrue(store.containsIndex(s2index));
        Assert.assertEquals(store.get(s1index), s1);
        Assert.assertEquals(store.get(s2index), s2);
    }
}