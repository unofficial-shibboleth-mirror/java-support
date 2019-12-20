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
import java.util.List;
import java.util.ListIterator;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Generic code to to test {@link List} */
public class ListTest {

    private final static String [] TEST_ARRAY = {"Zero", "One", "Two", "Three"};
    private final static String TEST_VALUE = "tWO";
    
    @Test public void verifyTests() {
        testListFunctions(new ArrayList<String>());
        testListIterator(new ArrayList<String>());
    }
    /**
     * Test those things that distinguish the {@link List} API from the {@link java.util.Collection} one.
     * 
     * @param list list to test
     */
    public static void testListFunctions(List<String> list) {
        boolean thrown = false;
        
        try {
            list.get(0);            
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should throw exception when lookup up after the end");

        thrown = false;
        try {
            list.get(1);            
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should throw exception when lookup up after the end");

        list.add(TEST_ARRAY[0]);
        Assert.assertEquals(list.get(0), TEST_ARRAY[0], "Single insert");
        
        thrown = false;
        try {
            list.get(1);            
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should throw exception when lookup up after the end");
            
        for (int i = 1 ; i < TEST_ARRAY.length; i++) {
            list.add(TEST_ARRAY[i]);
        }
        
        for (int i = 0 ; i < TEST_ARRAY.length; i++) {
            Assert.assertEquals(list.get(i), TEST_ARRAY[i], "Contents not as expected at " + i);
        }
        
        Assert.assertEquals(list.set(2, TEST_VALUE), TEST_ARRAY[2], "Replaced contents not as expected");
        Assert.assertEquals(list.get(2), TEST_VALUE, "Contents not as expected after replace");
        for (int i = 0; i < TEST_ARRAY.length; i++) {
            if (i != 2) {
                Assert.assertEquals(list.get(i), TEST_ARRAY[i], "Contents not as expected at " + i);
            }
        }
        
        Assert.assertEquals(list.size(), TEST_ARRAY.length, "Size matches after in place replace");
        
        Assert.assertEquals(list.set(2, TEST_ARRAY[2]), TEST_VALUE, "Replaced contents not as expected");
        list.add(2, TEST_VALUE);
        Assert.assertEquals(list.size(), TEST_ARRAY.length + 1, "Size matches after insert");
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(list.get(i), TEST_ARRAY[i], "Contents not as expected at " + i);
        }
        Assert.assertEquals(list.get(2), TEST_VALUE, "Contents not as expected after insert");
        for (int i = 2 ; i < TEST_ARRAY.length; i++) {
            Assert.assertEquals(list.get(i+1), TEST_ARRAY[i], "Contents not as expected at " + i);
        }
        
        thrown = false;
        try {
            list.set(5, TEST_VALUE);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should throw exception when inserting after the end");

        list.add(TEST_ARRAY.length, TEST_VALUE);
        Assert.assertEquals(list.get(TEST_ARRAY.length), TEST_VALUE, "Contents not as expected after insert");
        
        //
        // Now iterate up through the three different interestign number (0, 1, > 1) testing that we cannot set
        // beyond the end and can only add at the edn of earlier
        //
        
        list.clear();
        //
        // Cannot set at zero, cannot add at one, can add at zero
        //
        thrown = false;
        try {
            list.set(0, TEST_VALUE);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should throw exception when inserting after the end");

        thrown = false;
        try {
            list.add(1, TEST_VALUE);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should throw exception when inserting after the end");

        Assert.assertTrue(list.size() == 0, "Still empty after all those attempted adds");
        list.add(0, TEST_VALUE);
        Assert.assertTrue(list.size() == 1, "But one element");
        
        //
        // Cannot set at one, cannot add at two, can add at one
        //
        thrown = false;
        try {
            list.set(1, TEST_VALUE);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should throw exception when inserting after the end");

        thrown = false;
        try {
            list.add(2, TEST_VALUE);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown, "Should throw exception when inserting after the end");

        Assert.assertTrue(list.size() == 1, "Still only one element");
        list.add(1, TEST_VALUE);
        Assert.assertTrue(list.size() == 2, "Two elements");
        
        list.clear();
        Assert.assertEquals(list.indexOf(TEST_VALUE), -1, "Should not find an element");
        Assert.assertEquals(list.lastIndexOf(TEST_VALUE), -1, "Should not find an element");
        list.add(TEST_ARRAY[0]);
        Assert.assertEquals(list.indexOf(TEST_VALUE), -1, "Should not find this element");
        Assert.assertEquals(list.lastIndexOf(TEST_VALUE), -1, "Should not find this element");
        Assert.assertEquals(list.indexOf(TEST_ARRAY[0]), 0, "Should find this element");
        Assert.assertEquals(list.lastIndexOf(TEST_ARRAY[0]), 0, "Should find this element");
        for (int i = 1; i < TEST_ARRAY.length; i++) {
            list.add(TEST_ARRAY[i]);
        }
        for (int i = 0; i < TEST_ARRAY.length; i++) {
            list.add(TEST_ARRAY[i]);
        }
        for (int i = 0; i < TEST_ARRAY.length; i++) {
            list.add(TEST_ARRAY[i]);
        }
        Assert.assertEquals(list.indexOf(TEST_VALUE), -1, "Should not find this element");
        Assert.assertEquals(list.lastIndexOf(TEST_VALUE), -1, "Should not find this element");
        for (int i = 0; i < TEST_ARRAY.length; i++) {
            Assert.assertEquals(list.indexOf(TEST_ARRAY[i]), i, "Should find this element");
            Assert.assertEquals(list.lastIndexOf(TEST_ARRAY[i]), i + 2*TEST_ARRAY.length, "Should find this element");
        }
    }
    
    public static void testListIterator(List<String> list) {
        list.clear();
        int i;
        for (i = 0; i < TEST_ARRAY.length; i++) {
            list.add(TEST_ARRAY[i]);
        }
        ListIterator<String> listIterator = list.listIterator();
        
        listIterator.next();
        listIterator.add(TEST_VALUE);
        Assert.assertEquals(list.get(0), TEST_ARRAY[0], "Iterator insert");
        Assert.assertEquals(list.get(1), TEST_VALUE, "Iterator insert");
        for (i = 1; i < TEST_ARRAY.length; i++) {
            Assert.assertEquals(list.get(i+1), TEST_ARRAY[i], "Iterator insert");
        }
        
        list.clear();
        for (i = 0; i < TEST_ARRAY.length; i++) {
            list.add(TEST_ARRAY[i]);
        }
        listIterator = list.listIterator(1);
        
        listIterator.add(TEST_VALUE);
        Assert.assertEquals(list.get(0), TEST_ARRAY[0], "Iterator insert");
        Assert.assertEquals(list.get(1), TEST_VALUE, "Iterator insert");
        for (i = 1; i < TEST_ARRAY.length; i++) {
            Assert.assertEquals(list.get(i+1), TEST_ARRAY[i], "Iterator insert");
        }

        for (i = 0; i < list.size(); i++) {
            list.set(i, TEST_VALUE);
        }
        listIterator = list.listIterator();
        i = 0;
        while (listIterator.hasNext()) {
            Assert.assertEquals(listIterator.next(), TEST_VALUE, "Iterator next");
            if (i < TEST_ARRAY.length) {
                listIterator.set(TEST_ARRAY[i++]);
            }
        }
        for (i = 1; i < TEST_ARRAY.length; i++) {
            Assert.assertEquals(list.get(i), TEST_ARRAY[i], "Iterator set");
        }
        Assert.assertEquals(list.get(list.size()-1), TEST_VALUE, "Iterator set");
        i = 0;
        while (listIterator.hasPrevious()) {
            listIterator.previous();
            listIterator.set(TEST_VALUE);
        }
        for (i = 0; i < list.size(); i++) {
            Assert.assertEquals(list.get(i), TEST_VALUE, "Iterator set");
        }
    }
    

}
