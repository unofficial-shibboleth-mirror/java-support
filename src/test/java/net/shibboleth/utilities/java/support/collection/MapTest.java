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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Generic tests {@link Map}. 
 */
public class MapTest {

    private final static String KEY_1 = "StringOne";

    private final static String KEY_2 = "StringTwo";

    private final static String KEY_3 = "StringThree";
    
    private final static Object VALUE_1 = new Object();
    
    private final static Object VALUE_2 = "Value2";
    
    private final static Integer INTEGER_3 = 2; 

    private final static Object VALUE_3 = INTEGER_3;
    
    public static void testMap(Map<String, Object> map) {
        
        Assert.assertTrue(map.isEmpty(), "Initially map must be empty");
        Assert.assertEquals(map.values().size(), 0, "Zero Keys, zero values");
        Assert.assertEquals(map.keySet().size(), 0, "Zero Keys, zero values");
        Assert.assertEquals(map.entrySet().size(), 0, "Zero Keys, zero values");
        
        Assert.assertNull(map.put(KEY_1, VALUE_1), "Initial result of put should be null");
        Assert.assertEquals(map.size(), 1, "One element expected");
        Assert.assertTrue(map.containsKey(KEY_1), "Contains key " + KEY_1);
        Assert.assertFalse(map.containsKey(VALUE_1), "Contains key " + KEY_1);
        Assert.assertFalse(map.containsKey(KEY_3), "Contains key " + KEY_3);
        Assert.assertTrue(map.containsValue(VALUE_1), "Contains value " + VALUE_1);
        Assert.assertFalse(map.containsValue(KEY_1), "Contains value " + KEY_1);

        Assert.assertEquals(map.put(KEY_1, VALUE_1), VALUE_1, "Duplicate put");
        Assert.assertEquals(map.size(), 1, "One element expected");
        Assert.assertTrue(map.containsKey(KEY_1), "Contains key " + KEY_1);
        Assert.assertFalse(map.containsKey(KEY_3), "Contains key " + KEY_3);
        Assert.assertTrue(map.containsValue(VALUE_1), "Contains value " + VALUE_1);
        
        Assert.assertEquals(map.put(KEY_1, VALUE_2), VALUE_1, "Duplicate put");
        Assert.assertEquals(map.size(), 1, "One element expected");
        Assert.assertEquals(map.values().size(), 1, "One Keys, one value");
        Assert.assertEquals(map.keySet().size(), 1, "One Keys, one value");
        Assert.assertEquals(map.entrySet().size(), 1, "One Keys, one value");
        Assert.assertTrue(map.containsKey(KEY_1), "Contains key " + KEY_1);
        Assert.assertFalse(map.containsKey(KEY_3), "Contains key " + KEY_3);
        Assert.assertFalse(map.containsValue(VALUE_1), "Contains value " + VALUE_1);
        Assert.assertTrue(map.containsValue(VALUE_2), "Contains value " + VALUE_2);
        
        
        Assert.assertNull(map.put(KEY_2, VALUE_1), "Initial result of put should be null");
        Assert.assertEquals(map.size(), 2, "Two element expected");
        Assert.assertTrue(map.containsKey(KEY_1), "Contains key " + KEY_1);
        Assert.assertTrue(map.containsKey(KEY_2), "Contains key " + KEY_2);
        Assert.assertTrue(map.containsValue(VALUE_1), "Contains value " + VALUE_1);
        Assert.assertTrue(map.containsValue(VALUE_2), "Contains value " + VALUE_2);
        Assert.assertEquals(map.put(KEY_2, VALUE_2), VALUE_1, "Should have replaced");
        Assert.assertEquals(map.size(), 2, "Two element expected");
        Assert.assertTrue(map.containsKey(KEY_1), "Contains key " + KEY_1);
        Assert.assertTrue(map.containsKey(KEY_2), "Contains key " + KEY_2);
        Assert.assertFalse(map.containsKey(KEY_3), "Contains key " + KEY_3);
        Assert.assertFalse(map.containsValue(VALUE_1), "Contains value " + VALUE_1);
        Assert.assertTrue(map.containsValue(VALUE_2), "Contains value " + VALUE_2);
        
        Assert.assertEquals(map.values().size(), 2, "Two Keys, one value");
        Set<Object> set = new HashSet<>();
        set.addAll(map.values());
        Assert.assertEquals(set.size(), 1, "Two Keys, one (distinct) value");
        Assert.assertEquals(map.keySet().size(), 2, "Two Keys, one value");
        Assert.assertEquals(map.entrySet().size(), 2, "Two Keys, one value");
        
        Assert.assertEquals(map.put(KEY_2, VALUE_3), VALUE_2, "Should have replaced");
        Assert.assertEquals(map.put(KEY_1, VALUE_3), VALUE_2, "Should have replaced");
        Assert.assertTrue(map.containsValue(INTEGER_3), "Contains value " + INTEGER_3);

        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(KEY_1, INTEGER_3);
        testMap.put(KEY_2, INTEGER_3);
        
        Assert.assertTrue(testMap.equals(map), "test against type changes");
        map.putAll(testMap);
        Assert.assertEquals(map.size(), 2, "Two element expected");
        
        map.clear();
        Assert.assertEquals(map.size(), 0, "After clear map must be empty");
        
        // Test iterator removal from singleton member cases
        map.put(KEY_1, VALUE_1);
        Set<Entry<String,Object>> entrySet = map.entrySet();
        Iterator<Entry<String,Object>> entryIter = entrySet.iterator();
        entryIter.next();
        entryIter.remove();
        
        map.clear();
        Assert.assertEquals(map.size(), 0, "After clear map must be empty");
        
        map.put(KEY_1, VALUE_1);
        Set<String> keySet = map.keySet();
        Iterator<String> keyIter = keySet.iterator();
        keyIter.next();
        keyIter.remove();
        
        map.clear();
        Assert.assertEquals(map.size(), 0, "After clear map must be empty");
        
        map.put(KEY_1, VALUE_1);
        Collection<Object> values = map.values();
        Iterator<Object> valuesIter = values.iterator();
        valuesIter.next();
        valuesIter.remove();
    }

    @Test
    public void verfiyTest() {
        testMap(new HashMap<String, Object>());
    }
}
