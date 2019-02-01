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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Function;

import net.shibboleth.utilities.java.support.logic.TransformAndCheckFunction;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;

/**
 * Generic {@link Collection} tester
 * 
 */
public class CollectionTest {

    private final static String STRING_1 = "StringOne";

    private final static String STRING_2 = "StringTwo";

    private final static String STRING_3 = "StringThree";

    public static final Function<String, Optional<? extends String>> nullRemoveFunction =
            new TransformAndCheckFunction(Functions.identity(), Predicates.notNull(), false);

    public static final Function<String, Optional<? extends String>> upcaseNotNull = new TransformAndCheckFunction(
            new UpcaseFunction(), Predicates.notNull(), false);

    @Test public void verifyTests() {
        CollectionTest.testSimpleCollection(new ArrayList<String>(), true);
        CollectionTest.testArrayCollection(new ArrayList<String>(), new ArrayList<String>());
        CollectionTest.testIteratorCollection(new LazyList<String>(), new ArrayList<String>());

        CollectionTest.testSimpleCollection(new HashSet<String>(), false);
        CollectionTest.testArrayCollection(new HashSet<String>(), new HashSet<String>());
        CollectionTest.testIteratorCollection(new HashSet<String>(), new HashSet<String>());
    }

    protected static void
            testArrayCollection(Collection<String> testCollection, Collection<String> knownGoodCollection) {
        HashSet<String> set = new HashSet(2);

        Object[] testArray1 = testCollection.toArray();
        Object[] knownGood1 = knownGoodCollection.toArray();
        Assert.assertEquals(testArray1, knownGood1, "Results should be the same (modulo ordering issues)");

        String[] testArray2 = testCollection.toArray(new String[0]);
        String[] knownGood2 = knownGoodCollection.toArray(testArray2);
        Assert.assertEquals(testArray2, knownGood2, "Results should be the same (modulo ordering issues)");

        set.add(STRING_1);
        testCollection.addAll(set);
        knownGoodCollection.addAll(set);

        testArray1 = testCollection.toArray();
        knownGood1 = knownGoodCollection.toArray();
        Assert.assertEquals(testArray1, knownGood1, "Results should be the same (modulo ordering issues)");

        testArray2 = testCollection.toArray(new String[0]);
        knownGood2 = knownGoodCollection.toArray(testArray2);
        Assert.assertEquals(testArray2, knownGood2, "Results should be the same (modulo ordering issues)");

        testCollection.add(STRING_1);
        testCollection.add(STRING_3);
        testCollection.addAll(set);

        knownGoodCollection.add(STRING_1);
        knownGoodCollection.add(STRING_3);
        knownGoodCollection.addAll(set);

        testArray1 = testCollection.toArray();
        knownGood1 = knownGoodCollection.toArray();

        Assert.assertEquals(testArray1, knownGood1, "Results should be the same (modulo ordering issues)");

        testArray2 = testCollection.toArray(new String[0]);
        knownGood2 = knownGoodCollection.toArray(testArray2);

        Assert.assertEquals(testArray2, knownGood2, "Results should be the same (modulo ordering issues)");

    }

    /**
     * Test the iterator.
     * 
     * @param testCollection The collection under test.
     * @param workCollection A collection of the same basic type (List or Set) to test with.
     */
    protected static void testIteratorCollection(Collection<String> testCollection, Collection<String> workCollection) {

        Iterator<String> iterator = testCollection.iterator();
        Assert.assertFalse(iterator.hasNext(), "Empty set should not have an iterator.next");

        testCollection.add(STRING_1);
        iterator = testCollection.iterator();
        Assert.assertTrue(iterator.hasNext(), "Singleton set should have an iterator.next");
        Assert.assertEquals(iterator.next(), STRING_1, "Singleton set should have the correct contents");
        Assert.assertFalse(iterator.hasNext(), "Singleton set should not have a second iterator.next");

        testCollection.add(STRING_2);
        iterator = testCollection.iterator();
        Assert.assertTrue(iterator.hasNext(), "Pair set should have an iterator.next");
        String s1 = iterator.next();
        Assert.assertTrue(STRING_1.equals(s1) || STRING_2.equals(s1), "Pair set should have the correct contents");
        Assert.assertTrue(iterator.hasNext(), "Pair set should have a second iterator.next");
        String s2 = iterator.next();
        Assert.assertTrue(STRING_1.equals(s2) || STRING_2.equals(s2), "Pair set should have the correct contents");
        Assert.assertNotSame(s1, s2, "Pair set should have the correct contents");
        Assert.assertFalse(iterator.hasNext(), "Pair set should not have a third iterator.next");

        testCollection.add(STRING_3);
        iterator = testCollection.iterator();
        workCollection.addAll(testCollection);

        while (iterator.hasNext()) {
            Assert.assertTrue(workCollection.remove(iterator.next()), "Should be able to remove every element");
            iterator.remove();
        }
        Assert.assertTrue(workCollection.isEmpty(), "everything gone after iterate and empty");
        Assert.assertTrue(testCollection.isEmpty(), "everything gone after iterate and empty");

        iterator = testCollection.iterator();
        try {
            iterator.remove();
            Assert.assertTrue(false, "Should throw an exception");
        } catch (IllegalStateException e) {
            // TODO: handle exception
        }
        
        testCollection.clear();
        // Test that can remove from collection via the iterator if only has a single member
        testCollection.add(STRING_1);
        iterator = testCollection.iterator();
        Assert.assertTrue(iterator.hasNext(), "Should have a first element");
        iterator.next();
        Assert.assertFalse(iterator.hasNext(), "Should not have a second element");
        iterator.remove();
    }

    protected static void testSimpleCollection(Collection<String> collection, boolean allowDuplicates) {
        //
        // Start with empty list
        //
        Assert.assertTrue(collection.isEmpty(), "Start condition is empty");
        Assert.assertEquals(collection.size(), 0, "Start condition is empty");

        Assert.assertTrue(collection.add(STRING_1), "Should be allowed to add");
        Assert.assertFalse(collection.isEmpty(), "Non empty if filled");
        Assert.assertEquals(collection.size(), 1, "One Element after filling up");

        Assert.assertTrue(collection.add(STRING_2), "Should be allowed to add");
        Assert.assertFalse(collection.isEmpty(), "Non empty if filled");
        Assert.assertEquals(collection.size(), 2, "Two Elements after filling up");

        Assert.assertEquals(collection.add(STRING_2), allowDuplicates, "Only add if duplicates allowed");
        Assert.assertFalse(collection.isEmpty(), "Non empty if filled");
        if (allowDuplicates) {
            Assert.assertEquals(collection.size(), 3, "Three Elements after filling up (allowing for duplicates)");
        } else {
            Assert.assertEquals(collection.size(), 2, "Two Element after filling up (allowing for duplicates)");
        }

        collection.clear();
        Assert.assertTrue(collection.isEmpty(), "Empty after emptying");
        Assert.assertEquals(collection.size(), 0, "Empty after emptying");

        collection.add(STRING_1);
        Assert.assertEquals(collection.size(), 1, "After adding one");
        Assert.assertFalse(collection.remove(STRING_2), "Cannot remove if not there");
        Assert.assertTrue(collection.remove(STRING_1), "Must remove if there");
        Assert.assertTrue(collection.isEmpty(), "Not there after removal");

        collection.add(STRING_1);
        collection.add(STRING_2);
        collection.add(STRING_3);

        HashSet<String> set = new HashSet<>(2);
        set.add(STRING_1);
        set.add(STRING_2);

        Assert.assertTrue(collection.containsAll(set), "Should contain a subset");
        Assert.assertTrue(collection.removeAll(set), "Should be able to remove subset");

        Assert.assertFalse(collection.containsAll(set), "Shouldn't contain a subset");
        Assert.assertFalse(collection.removeAll(set), "Shouldn't be able to remove subset");

        collection.add(STRING_1);
        Assert.assertFalse(collection.containsAll(set), "Should not contain a subset");
        Assert.assertTrue(collection.removeAll(set), "Should be able to remove subset");

        Assert.assertFalse(collection.containsAll(set), "Shouldn't contain a subset");
        Assert.assertFalse(collection.removeAll(set), "Shouldn't be able to remove subset");

        collection.clear();
        collection.add(STRING_1);
        collection.add(STRING_3);

        Assert.assertTrue(collection.retainAll(set), "Should be remove something with first retain");
        Assert.assertEquals(collection.size(), 1, "Contain only one element after retainall");
        Assert.assertFalse(collection.retainAll(set), "Second retainall should make no difference");
        Assert.assertTrue(collection.contains(STRING_1), "After retain should just contain the intersection");

        collection.clear();
        Assert.assertTrue(collection.addAll(set), "Should be allowed to add");
        Assert.assertEquals(collection.size(), 2, "Contain two elements after addall");
        Assert.assertTrue(collection.containsAll(set), "Should contain what was added");
        Assert.assertEquals(collection.addAll(set), allowDuplicates, "May be allowed to add");
        if (allowDuplicates) {
            Assert.assertEquals(collection.size(), 2 * set.size(), "Count after adding twice");
        } else {
            Assert.assertEquals(collection.size(), set.size(), "Count after adding twice");
        }
        Assert.assertTrue(collection.removeAll(set), "Should be able to remove all");
        Assert.assertTrue(collection.isEmpty(), "Empty after remove all");
        Assert.assertFalse(collection.removeAll(set), "Should not be able to remove all twice");

        collection.add(STRING_1);
        Assert.assertTrue(collection.addAll(set), "Should be allowed to add");
        if (allowDuplicates) {
            Assert.assertEquals(collection.size(), 1 + set.size(), "Count after adding individual and set");
        } else {
            Assert.assertEquals(collection.size(), set.size(), "Count after adding individual and set");
        }
        Assert.assertTrue(collection.removeAll(set), "Should be able to remove all");
        Assert.assertTrue(collection.isEmpty(), "Empty after remove all");
        Assert.assertFalse(collection.removeAll(set), "Should not be able to remove all twice");

    }

    public static class UpcaseFunction implements Function<String, String> {

        /** {@inheritDoc} */
        public String apply(String input) {
            if (null == input) {
                return null;
            }
            return input.toUpperCase();
        }

    }
}
